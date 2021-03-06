package io.coodoo.workhorse.jobengine.control;

import io.coodoo.workhorse.jobengine.entity.Job;
import io.coodoo.workhorse.jobengine.entity.JobExecution;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public interface JobThread {

    void run(Job job);

    void stop();

    public JobExecution getActiveJobExecution();
}
