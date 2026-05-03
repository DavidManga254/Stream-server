package workflows;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import constants.Jobs;
import database.Repositories.TranscodeJobRepository;
import database.Repositories.VideoRepository;
import enums.VideoResolution;

public class TranscodeWorkflow {
    private static final ArrayBlockingQueue<Job> jobQueue = new ArrayBlockingQueue<>(Jobs.JOBS_QUEUE_SIZE);

    public record Job(
        String videoId,
        VideoResolution resolution
    ){}

    public static void storeVideoAndJobsRecords(String videoId){
        //store video main
        VideoRepository.storeVideoRecord(videoId);

        //720,360,480
        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_720P);
        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_480P);
        TranscodeJobRepository.storeJob(videoId, VideoResolution.RESOLUTION_360);

        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_720P));
        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_480P));
        addJobToQueue(new Job(videoId, VideoResolution.RESOLUTION_360));
    }

    private static void addJobToQueue(Job job){
        try {
            jobQueue.put(job);
        } catch (InterruptedException e) {
            System.err.println("failed to add job to queue");

            e.printStackTrace();
        }
    }
    
}
