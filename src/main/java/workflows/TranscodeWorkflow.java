package workflows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import constants.Jobs;
import constants.Paths;
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
    private static JobDbPoller poller;

    public record Job(
        String videoId,
        VideoResolution resolution
    ){}

    public static void startWorkers() {
        poller = new JobDbPoller(jobQueue, 50);
        poller.start();

        for (int i = 0; i < Jobs.THREAD_NO; i++) {
            executor.submit(() -> transcodeJobs());
        }
    }

    public static void shutdownWorkers() {
        if (poller != null) poller.stop();

        executor.shutdown();
    }

    //resets jobs that were opharned to pending like server shutdown, errors that did not recover the job etc...
    public static void resetStuckJobs() {
        TranscodeJobRepository.resetProcessingJobs();
    }

    public static void storeVideoAndJobsRecords(String videoId) {
        VideoRepository.storeVideoRecord(videoId);

        TranscodeJobRepository.storeJobs(
            videoId,
            List.of(
                    VideoResolution.RESOLUTION_360,
                    VideoResolution.RESOLUTION_480P
                    // TODO
                    // VideoResolution.RESOLUTION_720P
            )
        );

        // addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_720P));
        // addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_480P));
        // addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_360));
    }

    private static void addJobToQueue(Job job) {
        boolean added = jobQueue.offer(job);
        if (!added) {
            System.out.println("Queue full, job will be picked up from DB: " + job.videoId());
        }
    }

    private static void transcodeJobs() {
        while (true) {
            try {

                Job job = jobQueue.take(); // blocks, no DB polling

                boolean updated = TranscodeJobRepository.updateJobStatus(
                        job.videoId(),
                        job.resolution(),
                        TranscodeJobStatus.PROCESSING
                );

                if (!updated) continue;

                new TranscodeVideo(job.videoId(), job.resolution()).transcode();

                TranscodeJobRepository.updateJobStatus(
                        job.videoId(),
                        job.resolution(),
                        TranscodeJobStatus.COMPLETE
                );

                // ensure DB commit visibility before reading
                Thread.sleep(150);


                upSertMasterPlayList(job.videoId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    private static class Variant {

        private final String uri;
        private final int bandwidth;
        private final String resolution;

        public Variant(String uri, int bandwidth, String resolution) {
            this.uri = uri;
            this.bandwidth = bandwidth;
            this.resolution = resolution;
        }

        public String getUri() {
            return uri;
        }

        public int getBandwidth() {
            return bandwidth;
        }

        public String getResolution() {
            return resolution;
        }
    }

    private static final void upSertMasterPlayList(String videoId){
        List<TranscodeJobEntity.TranscodeJobEntityInfo> completedJobs = TranscodeJobRepository.getJobsByVideoIdAndStatus(videoId,TranscodeJobStatus.COMPLETE);

        if (completedJobs.isEmpty()) return;

        List<Variant> variants = completedJobs.stream()
            .map(job -> {
                VideoResolution res = job.resolution();

                return new Variant(
                        res.getFolder() + "/playlist.m3u8",
                        res.getBitrate(),
                        res.getHlsResolution()
                );
            })
            .sorted(Comparator.comparingInt(Variant::getBandwidth))
            .toList();

        StringBuilder master = new StringBuilder();
        master.append("#EXTM3U\n");

        for (Variant v : variants) {
            master.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(v.getBandwidth())
                    .append(",RESOLUTION=")
                    .append(v.getResolution())
                    .append("\n");

            master.append(v.getUri()).append("\n");
        }

        Path output = Path.of(Paths.STREAM_VIDEO_STORAGE + "/" + videoId + "/master.m3u8");
        Path temp = Path.of(output.toString() + ".tmp");

        try {
            Files.writeString(temp, master.toString());

            Files.move(
                    temp,
                    output,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write master playlist for videoId=" + videoId, e);
        }

    }
}