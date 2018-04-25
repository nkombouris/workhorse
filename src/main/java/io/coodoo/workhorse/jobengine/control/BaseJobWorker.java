package io.coodoo.workhorse.jobengine.control;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.inject.Inject;

import io.coodoo.workhorse.jobengine.boundary.JobEngineService;
import io.coodoo.workhorse.jobengine.boundary.JobExecutionParameters;
import io.coodoo.workhorse.jobengine.boundary.JobLogger;
import io.coodoo.workhorse.jobengine.entity.Job;
import io.coodoo.workhorse.jobengine.entity.JobExecution;
import io.coodoo.workhorse.jobengine.entity.JobExecutionStatus;

/**
 * Base worker class to define the creation and execution of jobs.
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public abstract class BaseJobWorker {

    @Inject
    protected JobEngineService jobEngineService;

    @Inject
    protected JobLogger jobLogger;

    protected Job job;

    protected JobExecution jobExecution;

    /**
     * The job engine will uses this method as the entrance point to perform the execution.
     * 
     * @param jobExecution job execution object, containing parameters and meta information
     * @throws Exception in case the job execution fails
     */
    public abstract void doWork(JobExecution jobExecution) throws Exception;

    /**
     * Gets the Job from the database
     * 
     * @return the Job that belongs to this service
     */
    protected Job getJob() {
        if (job == null) {
            job = jobEngineService.getJobByClassName(getClass().getName());
        }
        return job;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    protected <T> T getParameters() {

        if (jobExecution != null && jobExecution.getParameters() != null) {

            return (T) JobEngineUtil.jsonToJobExecutionParameters(jobExecution.getParameters());
        }
        return null;
    }

    protected void logLineWithTimestamp(String message) {
        jobLogger.lineWithTimestamp(message);
    }

    protected void logLine(String message) {
        jobLogger.line(message);
    }

    /**
     * @return the log text of the current active job execution or <code>null</code> if there isn't any
     */
    public String getJobExecutionLog() {
        return jobLogger.getLog();
    }

    /**
     * This method will (mainly) be called by the schedule timer in order to check if there is stuff to do.<br>
     * Its goal is creating {@link JobExecution} objects that gets added to the job engine to be executed.
     * <p>
     * Use <code>createJobExecution(JobExecutionParameters parameters)</code> to add single JobExecutions!
     * </p>
     */
    public void scheduledJobExecutionCreation() {
        createJobExecution();
    }

    /**
     * <i>Convenience method to {@link #createJobExecution(JobExecutionParameters)}</i><br>
     * <br>
     * This creates a parameterless {@link JobExecution} object that gets added to the job engine with default options.
     * 
     * @return job execution ID
     */
    @Deprecated
    public Long createJobExecution() {
        return createJobExecution(null);
    }

    /**
     * <i>Convenience method to {@link #createJobExecution(JobExecutionParameters, Boolean, LocalDateTime)}</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine with default options.
     * 
     * @param parameters needed parameters to do the job
     * @return job execution ID
     */
    @Deprecated
    public Long createJobExecution(JobExecutionParameters parameters) {
        return createJobExecution(parameters, false, null);
    }

    /**
     * <i>This is an access point to get the job engine started with a new job with job parameters.</i><br>
     * <br>
     * 
     * This creates a {@link JobExecution} object that gets added to the job engine to be executed as soon as possible.
     * 
     * @param parameters needed parameters to do the job
     * @param priority priority queuing
     * @param maturity specified time for the execution
     * @return job execution ID
     */
    @Deprecated
    public Long createJobExecution(JobExecutionParameters parameters, Boolean priority, LocalDateTime maturity) {
        return create(parameters, priority, maturity, null, null).getId();
    }

    /**
     * <i>This is an access point to get the job engine started with a new job with job parameters.</i><br>
     * <br>
     * 
     * This creates a {@link JobExecution} object that gets added to the job engine to be executed as soon as possible.
     * 
     * @param parameters needed parameters to do the job
     * @param priority priority queuing
     * @param delayValue time to wait
     * @param delayUnit what kind of time to wait
     * @return job execution ID
     */
    @Deprecated
    public Long createJobExecution(JobExecutionParameters parameters, Boolean priority, Long delayValue, ChronoUnit delayUnit) {
        return create(parameters, priority, delayToMaturity(delayValue, delayUnit), null, null).getId();
    }

    /**
     * <i>Convenience method to {@link #createJobExecution(JobExecutionParameters, Boolean, LocalDateTime)}</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the priority queue of the job engine to be treated first class.
     * 
     * @param parameters needed parameters to do the job
     * @return job execution ID
     */
    @Deprecated
    public Long createPriorityJobExecution(JobExecutionParameters parameters) {
        return createJobExecution(parameters, true, null);
    }

    /**
     * <i>Convenience method to {@link #createJobExecution(JobExecutionParameters, Boolean, Long, ChronoUnit)}</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine after the given delay.
     * 
     * @param parameters needed parameters to do the job
     * @param delayValue time to wait
     * @param delayUnit what kind of time to wait
     * @return job execution ID
     */
    @Deprecated
    public Long createDelayedJobExecution(JobExecutionParameters parameters, Long delayValue, ChronoUnit delayUnit) {
        return createJobExecution(parameters, false, delayValue, delayUnit);
    }

    /**
     * <i>Convenience method to {@link #createJobExecution(JobExecutionParameters, Boolean, LocalDateTime)}</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine at a specified time.
     * 
     * @param parameters needed parameters to do the job
     * @param maturity specified time for the execution
     * @return job execution ID
     */
    @Deprecated
    public Long createPlannedJobExecution(JobExecutionParameters parameters, LocalDateTime maturity) {
        return createJobExecution(parameters, false, maturity);
    }

    /**
     * This creates a chain of {@link JobExecution} objects, so when the first one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @return chain ID
     */
    @Deprecated
    public Long createChainedJobExecutions(List<JobExecutionParameters> parametersList) {
        return createChainedJobExecutions(parametersList, false, null);
    }

    /**
     * This creates a chain of {@link JobExecution} objects that gets added to the priority queue of the job engine to be treated first class. So when the first
     * one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @return chain ID
     */
    @Deprecated
    public Long createPriorityChainedJobExecutions(List<JobExecutionParameters> parametersList) {
        return createChainedJobExecutions(parametersList, true, null);
    }

    /**
     * This creates a chain of {@link JobExecution} objects that gets added to the job engine after the given delay. So when the first one gets executed it will
     * bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @param maturity specified time for the execution
     * @return chain ID
     */
    @Deprecated
    public Long createPlannedChainedJobExecutions(List<JobExecutionParameters> parametersList, LocalDateTime maturity) {
        return createChainedJobExecutions(parametersList, false, maturity);
    }

    /**
     * This creates a chain of {@link JobExecution} objects that gets added to the job engine after the given delay. So when the first one gets executed it will
     * bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @param delayValue time to wait
     * @param delayUnit what kind of time to wait
     * @return chain ID
     */
    @Deprecated
    public Long createDelayedChainedJobExecutions(List<JobExecutionParameters> parametersList, Long delayValue, ChronoUnit delayUnit) {
        return createChainedJobExecutions(parametersList, false, delayToMaturity(delayValue, delayUnit));
    }

    /**
     * This creates a chain of {@link JobExecution} objects, so when the first one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @param priority priority queuing
     * @param maturity specified time for the execution
     * @return chain ID
     */
    @Deprecated
    public Long createChainedJobExecutions(List<JobExecutionParameters> parametersList, Boolean priority, LocalDateTime maturity) {

        Long chainId = null;
        Long chainPreviousExecutionId = null;

        for (JobExecutionParameters parameters : parametersList) {
            if (chainId == null) { // start of chain
                // mark as chained, so the poller wont draft it to early
                Long id = create(parameters, priority, maturity, -1L, -1L).getId();
                JobExecution jobExecution = jobEngineService.getJobExecutionById(id);

                chainPreviousExecutionId = id;
                chainId = id;
                jobExecution.setChainId(chainId);
                jobExecution.setChainPreviousExecutionId(null);

            } else { // chain peasants
                chainPreviousExecutionId = create(parameters, priority, maturity, chainId, chainPreviousExecutionId).getId();
            }
        }
        return chainId;
    }

    private LocalDateTime delayToMaturity(Long delayValue, ChronoUnit delayUnit) {

        LocalDateTime maturity = null;
        if (delayValue != null && delayUnit != null) {
            maturity = JobEngineUtil.timestamp().plus(delayValue, delayUnit);
        }
        return maturity;
    }

    @Deprecated
    private JobExecution create(JobExecutionParameters parameters, Boolean priority, LocalDateTime maturity, Long chainId, Long chainPreviousExecutionId) {

        Long jobId = getJob().getId();
        boolean uniqueInQueue = getJob().isUniqueInQueue();

        String parametersJson = JobEngineUtil.parametersToJson(parameters);

        return jobEngineService.createJobExecution(jobId, parametersJson, priority, maturity, chainId, chainPreviousExecutionId, uniqueInQueue);
    }

    public JobExecution create(Object parameters, Boolean priority, LocalDateTime maturity, Long chainId, Long chainPreviousExecutionId) {

        Long jobId = getJob().getId();
        boolean uniqueInQueue = getJob().isUniqueInQueue();

        String parametersJson = JobEngineUtil.parametersToJson(parameters);

        return jobEngineService.createJobExecution(jobId, parametersJson, priority, maturity, chainId, chainPreviousExecutionId, uniqueInQueue);
    }

    public long currentQueuedExecutions() {
        return jobEngineService.currentJobExecutions(getJob().getId(), JobExecutionStatus.QUEUED);
    }

    public long currentRunningExecutions() {
        return jobEngineService.currentJobExecutions(getJob().getId(), JobExecutionStatus.RUNNING);
    }

    public long currentFinishedExecutions() {
        return jobEngineService.currentJobExecutions(getJob().getId(), JobExecutionStatus.FINISHED);
    }

    public long currentAbortedExecutions() {
        return jobEngineService.currentJobExecutions(getJob().getId(), JobExecutionStatus.ABORTED);
    }

    public long currentFailedExecutions() {
        return jobEngineService.currentJobExecutions(getJob().getId(), JobExecutionStatus.FAILED);
    }

}
