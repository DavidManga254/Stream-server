package workflows;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import constants.Jobs;
import database.Repositories.TranscodeJobRepository;
import database.Repositories.VideoRepository;
import database.entities.TranscodeJobEntity;
import enums.TranscodeJobStatus;
import enums.VideoResolution;
import packages.files.video.TranscodeVideo;

// Updated TranscodeWorkflow
public class TranscodeWorkflow {
    private static final ArrayBlockingQueue<Job> jobQueue = new ArrayBlockingQueue<>(Jobs.JOBS_QUEUE_SIZE);
    private static final ExecutorService executor = Executors.newFixedThreadPool(Jobs.THREAD_NO);

    public record Job(
        String videoId,
        VideoResolution resolution
    ){}

    public static void startWorkers() {
        for (int i = 0; i < Jobs.THREAD_NO; i++) {
            executor.submit(() -> transcodeJobs());
        }
    }

    public static void shutdownWorkers() {
        executor.shutdown();
    }

    //resets jobs that were opharned to pending like server shutdown, errors that did not recover the job etc...
    public static void resetStuckJobs() {
        TranscodeJobRepository.resetProcessingJobs();
    }

    public static void storeVideoAndJobsRecords(String videoId) {
        VideoRepository.storeVideoRecord(videoId);

        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_720P);
        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_480P);
        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_360);

        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_720P));
        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_480P));
        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_360));
    }

    private static void addJobToQueue(Job job) {
        boolean added = jobQueue.offer(job);
        if (!added) {
            System.out.println("Queue full, job will be picked up from DB: " + job.videoId());
        }
    }

    private static void transcodeJobs() {
        while (true) {
            Job jobItem = null;
            try {
                // try queue first
                jobItem = jobQueue.poll();

                if (jobItem == null) {
                    // queue empty, check DB
                    TranscodeJobEntity.TranscodeJobEntityInfo pendingJob =
                        TranscodeJobRepository.getNextPendingJob();

                    if (pendingJob == null) {
                        // nothing to do, wait before checking again
                        Thread.sleep(5000);
                        continue;
                    }

                    // job already claimed in DB, just process it
                    new TranscodeVideo(pendingJob.videoId(), pendingJob.resolution()).transcode();
                    TranscodeJobRepository.updateJobStatus(pendingJob.videoId(), pendingJob.resolution(), TranscodeJobStatus.COMPLETE);
                    continue;
                }

                // job came from queue — check and claim in DB
                TranscodeJobEntity.TranscodeJobEntityInfo jobRecord =
                    TranscodeJobRepository.getJob(jobItem.videoId(), jobItem.resolution());

                if (jobRecord.transcodeJobStatus() == TranscodeJobStatus.COMPLETE ||
                    jobRecord.transcodeJobStatus() == TranscodeJobStatus.PROCESSING) {
                    continue;
                }

                boolean updated = TranscodeJobRepository.updateJobStatus(
                    jobItem.videoId(), jobItem.resolution(), TranscodeJobStatus.PROCESSING);

                if (!updated) continue;

                new TranscodeVideo(jobItem.videoId(), jobItem.resolution()).transcode();
                TranscodeJobRepository.updateJobStatus(jobItem.videoId(), jobItem.resolution(), TranscodeJobStatus.COMPLETE);

            } catch (InterruptedException e) {
                System.err.println("Transcode worker interrupted");
                e.printStackTrace();
            } catch (IOException e) {
                if (jobItem != null) {
                    TranscodeJobRepository.updateJobStatus(jobItem.videoId(), jobItem.resolution(), TranscodeJobStatus.FAILED);
                }
                e.printStackTrace();
            }
        }
    }
}