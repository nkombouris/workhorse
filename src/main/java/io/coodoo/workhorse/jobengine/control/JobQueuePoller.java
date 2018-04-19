package io.coodoo.workhorse.jobengine.control;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobQueuePoller {

    private static final String JOB_QUEUE_POLLER = "JobQueuePoller";

    private static Logger logger = LoggerFactory.getLogger(JobQueuePoller.class);

    @Inject
    JobEngineController jobEngineController;

    @Resource
    protected TimerService timerService;

    @Timeout
    public void poll() {
        jobEngineController.syncJobExecutionQueue();
    }

    public void start(Integer interval) {

        if (interval == null) {
            interval = 5;
        }
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/" + interval).minute("*").hour("*");

        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setInfo(JOB_QUEUE_POLLER);
        timerConfig.setPersistent(false);

        if (isRunning()) {
            stop();
        }
        timerService.createCalendarTimer(scheduleExpression, timerConfig);

        logger.info("Job Queue Poller started with a {} seconds interval", interval);
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