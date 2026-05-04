package workflows;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import database.Repositories.TranscodeJobRepository;
import database.entities.TranscodeJobEntity;

public class JobDbPoller {

        private final ArrayBlockingQueue<TranscodeWorkflow.Job> queue;
        private final int batchSize;
        private volatile boolean running = true;

        public JobDbPoller(ArrayBlockingQueue<TranscodeWorkflow.Job> queue, int batchSize) {
            this.queue = queue;
            this.batchSize = batchSize;
        }

        public void start() {
            Thread pollerThread = new Thread(this::pollLoop);
            pollerThread.setDaemon(true);
            pollerThread.start();
        }

        private void pollLoop() {
            while (running) {
                try {

                    List<TranscodeJobEntity.TranscodeJobEntityInfo> jobs =
                            TranscodeJobRepository.getBatchPendingJobs(batchSize);

                    if (jobs.isEmpty()) {
                        Thread.sleep(2000);
                        continue;
                    }

                    for (TranscodeJobEntity.TranscodeJobEntityInfo job : jobs) {

                        boolean added = queue.offer(
                                new TranscodeWorkflow.Job(job.videoId(), job.resolution())
                        );

                        if (!added) {
                            // queue full → stop pushing, try later
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                }
            }
        }

        public void stop() {
            running = false;
        }
    }
