package io.coodoo.workhorse.jobengine.control;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.coodoo.workhorse.jobengine.boundary.JobEngineConfig;

/**
 * @author coodoo GmbH (coodoo.io)
 */
@Singleton
public class JobQueuePoller {

    private static final String JOB_QUEUE_POLLER = "JobQueuePoller";
    private static final int ZOMBIE_HUNT_INTERVAL = 300;

    private static Logger logger = LoggerFactory.getLogger(JobQueuePoller.class);

    @Inject
    JobEngineController jobEngineController;

    @Resource
    protected TimerService timerService;

    // look out for zombies on startup
    private int zombieWatch = ZOMBIE_HUNT_INTERVAL;

    @Timeout
    public void poll() {

        jobEngineController.syncJobExecutionQueue();
        huntZombies();
    }

    private void huntZombies() {
        zombieWatch += JobEngineConfig.JOB_QUEUE_POLLER_INTERVAL;
        if (zombieWatch > ZOMBIE_HUNT_INTERVAL) { // watch for zombies every 5 minutes
            // go, hunt down zombies!
            jobEngineController.huntJobExecutionZombies();
            zombieWatch = 0;
        }
    }

    public void start() {

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/" + JobEngineConfig.JOB_QUEUE_POLLER_INTERVAL).minute("*").hour("*");

        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setInfo(JOB_QUEUE_POLLER);
        timerConfig.setPersistent(false);

        if (isRunning()) {
            stop();
        }
        timerService.createCalendarTimer(scheduleExpression, timerConfig);

        logger.info("Job Queue Poller started with a {} seconds interval", JobEngineConfig.JOB_QUEUE_POLLER_INTERVAL);
    }

    public void stop() {
        Timer timer = getPollerTimer();
        if (timer != null) {
            timer.cancel();
            logger.info("Job Queue Poller stopped");
        }
    }

    public boolean isRunning() {
        return getPollerTimer() != null;
    }

    private Timer getPollerTimer() {
        for (Timer timer : timerService.getTimers()) {
            if (timer.getInfo().toString().equals(JOB_QUEUE_POLLER)) {
                return timer;
            }
        }
        return null;
    }

}
