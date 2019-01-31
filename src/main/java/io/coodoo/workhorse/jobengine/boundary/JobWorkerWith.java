package io.coodoo.workhorse.jobengine.boundary;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.coodoo.workhorse.jobengine.control.BaseJobWorker;
import io.coodoo.workhorse.jobengine.control.JobEngineUtil;
import io.coodoo.workhorse.jobengine.entity.JobExecution;

/**
 * Job worker class to define the creation and execution of jobs with parameters. <br>
 * <tt>T</tt> can be any Object or a {@link List} of simple Java types <br>
 * Your job does not need parameters? See {@link JobWorker}!
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public abstract class JobWorkerWith<T> extends BaseJobWorker {

    private Class<?> parametersClass;

    public abstract void doWork(T parameters) throws Exception;

    @Override
    public void doWork(JobExecution jobExecution) throws Exception {

        this.jobContext.init(jobExecution);

        doWork(getParameters(jobExecution));
    }

    /**
     * Gets the parameters object of the given job execution
     * 
     * @param jobExecution job execution
     * @return parameters object
     */
    @SuppressWarnings("unchecked")
    public T getParameters(JobExecution jobExecution) {

        return (T) JobEngineUtil.jsonToParameters(jobExecution.getParameters(), getParametersClass());
    }

    /**
     * Gets the parameters object of the given job execution
     * 
     * @param jobExecutionId job execution ID
     * @return parameters object
     */
    public T getParameters(Long jobExecutionId) {

        JobExecution jobExecution = jobEngineService.getJobExecutionById(jobExecutionId);
        if (jobExecution == null) {
            return null;
        }
        return getParameters(jobExecution);
    }

    private Class<?> getParametersClass() {

        if (parametersClass != null) {
            return parametersClass;
        }
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();

            Class<?> clazz = null;
            if (className.startsWith(List.class.getName())) {
                clazz = List.class;
            } else {
                clazz = Class.forName(className);
            }
            parametersClass = clazz;
            return parametersClass;

        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type! Please use extends <> ");
        }
    }

    /**
     * <i>Convenience method to create a job execution</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine with default options.
     * 
     * @param parameters needed parameters to do the job
     * @return job execution ID
     */
    public Long createJobExecution(T parameters) {
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
    public Long createJobExecution(T parameters, Boolean priority, LocalDateTime maturity) {
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
    public Long createJobExecution(T parameters, Boolean priority, Long delayValue, ChronoUnit delayUnit) {
        return create(parameters, priority, JobEngineUtil.delayToMaturity(delayValue, delayUnit), null, null).getId();
    }

    /**
     * <i>Convenience method to create a job execution</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the priority queue of the job engine to be treated first class.
     * 
     * @param parameters needed parameters to do the job
     * @return job execution ID
     */
    public Long createPriorityJobExecution(T parameters) {
        return createJobExecution(parameters, true, null);
    }

    /**
     * <i>Convenience method to create a job execution</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine after the given delay.
     * 
     * @param parameters needed parameters to do the job
     * @param delayValue time to wait
     * @param delayUnit what kind of time to wait
     * @return job execution ID
     */
    public Long createDelayedJobExecution(T parameters, Long delayValue, ChronoUnit delayUnit) {
        return createJobExecution(parameters, false, delayValue, delayUnit);
    }

    /**
     * <i>Convenience method to create a job execution</i><br>
     * <br>
     * This creates a {@link JobExecution} object that gets added to the job engine at a specified time.
     * 
     * @param parameters needed parameters to do the job
     * @param maturity specified time for the execution
     * @return job execution ID
     */
    public Long createPlannedJobExecution(T parameters, LocalDateTime maturity) {
        return createJobExecution(parameters, false, maturity);
    }

    /**
     * This creates a chain of {@link JobExecution} objects, so when the first one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @return chain ID
     */
    public Long createChainedJobExecutions(List<T> parametersList) {
        return createChainedJobExecutions(parametersList, false, null);
    }

    /**
     * This creates a chain of {@link JobExecution} objects that gets added to the priority queue of the job engine to be treated first class. So when the first
     * one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @return chain ID
     */
    public Long createPriorityChainedJobExecutions(List<T> parametersList) {
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
    public Long createPlannedChainedJobExecutions(List<T> parametersList, LocalDateTime maturity) {
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
    public Long createDelayedChainedJobExecutions(List<T> parametersList, Long delayValue, ChronoUnit delayUnit) {
        return createChainedJobExecutions(parametersList, false, JobEngineUtil.delayToMaturity(delayValue, delayUnit));
    }

    /**
     * This creates a chain of {@link JobExecution} objects, so when the first one gets executed it will bring all its chained friends.
     * 
     * @param parametersList list of needed parameters to do the job in the order of the execution chain
     * @param priority priority queuing
     * @param maturity specified time for the execution
     * @return chain ID
     */
    public Long createChainedJobExecutions(List<T> parametersList, Boolean priority, LocalDateTime maturity) {

        Long chainId = null;
        Long chainPreviousExecutionId = null;

        // TODO: Damit dieser Code immer ohne Fehler funktioniert muss die vollständige Erstellung in einer eigenen Transaktion laufen, d.h. aktuell muss der
        // Worker dafür eine Stateless Bean sein.
        // Es kam vor, dass diese Stelle ohne Transaktion aufgerufen wurde und somit das nächsträgliche ändern der Chain ID der ersten Execution nicht mehr
        // gegriffen hat. Diese ganze Stelle sollte in den Service oder einen Controller mit eigener neuer Transaktion ausgelagert werden ausgelagert werden.
        for (T parameters : parametersList) {
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
}
