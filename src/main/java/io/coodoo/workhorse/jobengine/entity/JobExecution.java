package io.coodoo.workhorse.jobengine.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import io.coodoo.workhorse.jobengine.boundary.JobExecutionParameters;
import io.coodoo.workhorse.jobengine.control.JobEngineUtil;
import io.coodoo.framework.jpa.boundary.entity.RevisionDatesEntity;

/**
 * <p>
 * A JobExceuction defines a single job which will be excecuted by the job engine.
 * </p>
 * <p>
 * Every needed information to do a single job is stored with this entity.
 * </p>
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "jobengine_execution")
@NamedQueries({@NamedQuery(name = "JobExecution.getAllByJobId", query = "SELECT j FROM JobExecution j WHERE j.jobId = :jobId"),
                @NamedQuery(name = "JobExecution.deleteAllByJobId", query = "DELETE FROM JobExecution j WHERE j.jobId = :jobId"),
                @NamedQuery(name = "JobExecution.getAllByStatus", query = "SELECT j FROM JobExecution j WHERE j.status = :status"),

                // Poller
                @NamedQuery(name = "JobExecution.getNextCandidates",
                                query = "SELECT j FROM JobExecution j WHERE j.jobId = :jobId AND j.status = 'QUEUED' AND (j.maturity IS NULL OR j.maturity < :currentTime) AND j.chainPreviousExecutionId IS NULL ORDER BY j.priority, j.createdAt"),

                // Chained
                @NamedQuery(name = "JobExecution.getChain", query = "SELECT j FROM JobExecution j WHERE j.chainId = :chainId ORDER BY j.createdAt, j.id"),
                @NamedQuery(name = "JobExecution.getNextInChain",
                                query = "SELECT j FROM JobExecution j WHERE j.chainId = :chainId AND j.chainPreviousExecutionId = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.abortChain",
                                query = "UPDATE JobExecution j SET j.status = 'ABORTED' WHERE j.chainId = :chainId AND j.status = 'QUEUED'"),

                // Misc
                @NamedQuery(name = "JobExecution.deleteOlderJobExecutions",
                                query = "DELETE FROM JobExecution j WHERE j.jobId = :jobId AND j.createdAt < :preDate"),
                @NamedQuery(name = "JobExecution.selectDuration", query = "SELECT j.duration FROM JobExecution j WHERE j.id = :jobExecutionId"),

                // Status updates
                @NamedQuery(name = "JobExecution.updateStatus",
                                query = "UPDATE JobExecution j SET j.status = :status, j.updatedAt = :updatedAt WHERE j.id = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.updateStarted",
                                query = "UPDATE JobExecution j SET j.status = 'RUNNING', j.startedAt = :startedAt, j.updatedAt = :startedAt WHERE j.id = :jobExecutionId"),
                @NamedQuery(name = "JobExecution.updateEnded",
                                query = "UPDATE JobExecution j SET j.status = :status, j.endedAt = :endedAt, j.duration = :duration, j.updatedAt = :endedAt WHERE j.id = :jobExecutionId"),

                // Analytic
                @NamedQuery(name = "JobExecution.getFirstCreatedByJobIdAndParamters",
                                query = "SELECT j FROM JobExecution j WHERE j.jobId = :jobId AND j.status = 'QUEUED' AND (j.parametersJson is NULL OR j.parametersJson = :parametersJson) ORDER BY j.createdAt ASC"),
                @NamedQuery(name = "JobExecution.countQueudByJobIdAndParamters",
                                query = "SELECT COUNT(j) FROM JobExecution j WHERE j.jobId = :jobId AND j.status = 'QUEUED' and (j.parametersJson IS NULL or j.parametersJson = :parametersJson)"),
                @NamedQuery(name = "JobExecution.countByJobIdAndStatus",
                                query = "SELECT COUNT(j) FROM JobExecution j WHERE j.jobId = :jobId AND j.status = :status")

})

public class JobExecution extends RevisionDatesEntity {

    /**
     * The reference to the job description.
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * The job excecution status e.g. QUEUED or FINISHED.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobExecutionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration")
    private Long duration;

    /**
     * If a job exectution has the priority set to <code>true</code> it will be executed before all jobs with priority <code>false</code>.
     */
    @Column(name = "priority")
    private boolean priority;

    /**
     * If a maturity is given, the job execution will not be executed before this this time.
     */
    @Column(name = "maturity")
    private LocalDateTime maturity;

    @Column(name = "chain_id")
    private Long chainId;

    @Column(name = "chain_previous_execution_id")
    private Long chainPreviousExecutionId;

    @Column(name = "parameters")
    private String parametersJson;

    @Column(name = "fail_retry")
    private int failRetry;

    @Column(name = "fail_retry_execution_id")
    private Long failRetryExecutionId;

    /**
     * The exception message, if the job excecution ends in an exception.
     */
    @Column(name = "fail_message", length = 4096)
    private String failMessage;

    /**
     * The exception stacktrace, if the job excecution ends in an exception.
     */
    @Column(name = "fail_stacktrace", length = 10000)
    private String failStacktrace;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public LocalDateTime getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDateTime maturity) {
        this.maturity = maturity;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public Long getChainPreviousExecutionId() {
        return chainPreviousExecutionId;
    }

    public void setChainPreviousExecutionId(Long chainPreviousExecutionId) {
        this.chainPreviousExecutionId = chainPreviousExecutionId;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }

    public JobExecutionParameters getParameters() {
        return JobEngineUtil.jsonToJobExecutionParameters(parametersJson);
    }

    public void setParameters(JobExecutionParameters parameters) {
        this.parametersJson = JobEngineUtil.jobExecutionParametersToJson(parameters);
    }

    public int getFailRetry() {
        return failRetry;
    }

    public void setFailRetry(int failRetry) {
        this.failRetry = failRetry;
    }

    public Long getFailRetryExecutionId() {
        return failRetryExecutionId;
    }

    public void setFailRetryExecutionId(Long failRetryExecutionId) {
        this.failRetryExecutionId = failRetryExecutionId;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public String getFailStacktrace() {
        return failStacktrace;
    }

    public void setFailStacktrace(String failStacktrace) {
        this.failStacktrace = failStacktrace;
    }

    @Override
    public String toString() {
        return "JobExecution [jobId=" + jobId + ", status=" + status + ", startedAt=" + startedAt + ", endedAt=" + endedAt + ", duration=" + duration
                        + ", priority=" + priority + ", maturity=" + maturity + ", chainId=" + chainId + ", chainPreviousExecutionId="
                        + chainPreviousExecutionId + ", parametersJson=" + parametersJson + ", failRetry=" + failRetry + ", failRetryExecutionId="
                        + failRetryExecutionId + ", failMessage=" + failMessage + ", failStacktrace=" + failStacktrace + "]";
    }

    @SuppressWarnings("unchecked")
    public static List<JobExecution> getNextCandidates(EntityManager entityManager, Long jobId, int maxResults) {
        Query query = entityManager.createNamedQuery("JobExecution.getNextCandidates");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("currentTime", JobEngineUtil.timestamp());
        query = query.setMaxResults(maxResults);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.getAllByStatus' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param status the status
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<JobExecution> getAllByStatus(EntityManager entityManager, JobExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.getAllByStatus");
        query = query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.selectDuration' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobExecutionId the jobExecutionId
     * @return the result
     */
    public static Long selectDuration(EntityManager entityManager, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.selectDuration");
        query = query.setParameter("jobExecutionId", jobExecutionId);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.deleteAllByJobId' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @return Number of deleted objects
     */
    public static int deleteAllByJobId(EntityManager entityManager, Long jobId) {
        Query query = entityManager.createNamedQuery("JobExecution.deleteAllByJobId");
        query = query.setParameter("jobId", jobId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getAllByJobId' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<JobExecution> getAllByJobId(EntityManager entityManager, Long jobId) {
        Query query = entityManager.createNamedQuery("JobExecution.getAllByJobId");
        query = query.setParameter("jobId", jobId);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.updateStatus' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param status the status
     * @param updatedAt the updatedAt
     * @param jobExecutionId the jobExecutionId
     * @return Number of updated objects
     */
    public static int updateStatus(EntityManager entityManager, JobExecutionStatus status, LocalDateTime updatedAt, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.updateStatus");
        query = query.setParameter("status", status);
        query = query.setParameter("updatedAt", updatedAt);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.updateStartedAt' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param startedAt the startedAt
     * @param jobExecutionId the jobExecutionId
     * @return Number of updated objects
     */
    public static int updateStarted(EntityManager entityManager, LocalDateTime startedAt, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.updateStarted");
        query = query.setParameter("startedAt", startedAt);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.updateEnded' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param status the status
     * @param endedAt the endedAt
     * @param duration the duration
     * @param jobExecutionId the jobExecutionId
     * @return Number of updated objects
     */
    public static int updateEnded(EntityManager entityManager, JobExecutionStatus status, LocalDateTime endedAt, Long duration, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.updateEnded");
        query = query.setParameter("status", status);
        query = query.setParameter("endedAt", endedAt);
        query = query.setParameter("duration", duration);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getChain' returning a list of result objects.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @return List of result objects
     */
    @SuppressWarnings("unchecked")
    public static List<JobExecution> getChain(EntityManager entityManager, Long chainId) {
        Query query = entityManager.createNamedQuery("JobExecution.getChain");
        query = query.setParameter("chainId", chainId);
        return query.getResultList();
    }

    /**
     * Executes the query 'JobExecution.abortChain' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @return Number of updated objects
     */
    public static int abortChain(EntityManager entityManager, Long chainId) {
        Query query = entityManager.createNamedQuery("JobExecution.abortChain");
        query = query.setParameter("chainId", chainId);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getFirstCreatedByJobIdAndParamters' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param parametersJson the parametersJson
     * @return the result
     */
    public static JobExecution getFirstCreatedByJobIdAndParamters(EntityManager entityManager, Long jobId, String parametersJson) {
        Query query = entityManager.createNamedQuery("JobExecution.getFirstCreatedByJobIdAndParamters");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("parametersJson", parametersJson);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (JobExecution) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.countQueudByJobIdAndParamters' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param parametersJson the parametersJson
     * @return the result
     */
    public static Long countQueudByJobIdAndParamters(EntityManager entityManager, Long jobId, String parametersJson) {
        Query query = entityManager.createNamedQuery("JobExecution.countQueudByJobIdAndParamters");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("parametersJson", parametersJson);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.countByJobIdAndStatus' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param status the status
     * @return the result
     */
    public static Long countByJobIdAndStatus(EntityManager entityManager, Long jobId, JobExecutionStatus status) {
        Query query = entityManager.createNamedQuery("JobExecution.countByJobIdAndStatus");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("status", status);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (Long) results.get(0);
    }

    /**
     * Executes the query 'JobExecution.deleteOlderJobExecutions' returning the number of affected rows.
     *
     * @param entityManager the entityManager
     * @param jobId the jobId
     * @param preDate the preDate
     * @return Number of deleted objects
     */
    public static int deleteOlderJobExecutions(EntityManager entityManager, Long jobId, LocalDateTime preDate) {
        Query query = entityManager.createNamedQuery("JobExecution.deleteOlderJobExecutions");
        query = query.setParameter("jobId", jobId);
        query = query.setParameter("preDate", preDate);
        return query.executeUpdate();
    }

    /**
     * Executes the query 'JobExecution.getNextInChain' returning one/the first object or null if nothing has been found.
     *
     * @param entityManager the entityManager
     * @param chainId the chainId
     * @param jobExecutionId the jobExecutionId
     * @return the result
     */
    public static JobExecution getNextInChain(EntityManager entityManager, Long chainId, Long jobExecutionId) {
        Query query = entityManager.createNamedQuery("JobExecution.getNextInChain");
        query = query.setParameter("chainId", chainId);
        query = query.setParameter("jobExecutionId", jobExecutionId);
        query = query.setMaxResults(1);
        @SuppressWarnings("rawtypes")
        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return (JobExecution) results.get(0);
    }

}