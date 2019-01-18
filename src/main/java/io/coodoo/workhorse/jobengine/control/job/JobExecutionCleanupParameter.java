package io.coodoo.workhorse.jobengine.control.job;

/**
 * Parameters for the usage in {@link JobExecutionCleanupWorker}
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class JobExecutionCleanupParameter {

    public Long jobId;
    public String jobName;
    public int minDaysOld;

    public JobExecutionCleanupParameter() {}

    public JobExecutionCleanupParameter(Long jobId, String jobName, int minDaysOld) {
        super();
        this.jobId = jobId;
        this.jobName = jobName;
        this.minDaysOld = minDaysOld;
    }

}
