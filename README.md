[logo]: https://github.com/coodoo-io/workhorse/tree/master/src/main/resources/workhorse.png "Workhorse: Java EE Job Engine for background jobs and business critical tasks"

# Workhorse ![alt text][logo]

> Java EE Job Engine for background jobs and business critical tasks

## Table of Contents

- [Who is this Workhorse?](#who-is-this-workhorse)
- [Getting started](#getting-started)
- [Install](#install)
- [Changelog](#changelog)
- [Maintainers](#maintainers)
- [Contribute](#contribute)
- [License](#license)


## Who is this Workhorse?

The coodoo Workhorse is Java EE job engine for mostly all kind of tasks and background jobs as it is a combination of task scheduler and an event system. It can help you to send out thousands of e-mails or perform long running imports.

Just fire jobs on demand when ever from where ever in your code and Workhorse will take care of it. You can also define an interval or specific time the job has to be started by using the cron syntax. There are also many options like prioritizing, delaying, chaining, multithreading, uniquifying, retrying the jobs. 

## Getting started

Lets create a backup job. Therefore you need to extend the `JobWorker` class that provides you the `doWork` method. This method is where the magic happens! You also have to add the `@JobConfig` annotation to provide basic configurations.

```java
@JobConfig(name = "Backup", description = "Better backup!")
public class BackupJob extends JobWorker {

    private final Logger log = LoggerFactory.getLogger(BackupJob.class);

    @Override
    public void doWork(JobExecution jobExecution) {

        log.info("Performing some fine backup!");
    }
}
```

Now we are able to inject this backup job to a service and trigger a job execution. After calling `createJobExecution` the job gets pushed into the job queue and the job engine will take care from this point.

```java
    @Inject
    BackupJob backupJob;

    public void performBackup() {

        backupJob.createJobExecution();
    }
```

Lets add some parameters to this job! Therefore we need a POJO with the wanted attributes and implement the `JobExecutionParameters` interface.

```java
public class BackupJobParameters implements JobExecutionParameters {

    private String evironment;
    private boolean replaceOldBackup;
    // getters, setters, ...
}
```

The service can pass the parameters object to the `createJobExecution` method.

```java
    @Inject
    BackupJob backupJob;

    public void performBackup() {
    
        BackupJobParameters parameters = new BackupJobParameters();
        parameters.setEvironment("STAGE-2");
        parameters.setReplaceOldBackup(false);

        backupJob.createJobExecution(parameters);
    }
```

You can access the parameters in the `doWork` method like this.

```java
    @Override
    public void doWork(JobExecution jobExecution) {

        BackupJobParameters params = (BackupJobParameters) jobExecution.getParameters();

        log.info("Performing some fine backup on {} environment! Replace old backup: {}", params.getEvironment(), params.isReplaceOldBackup());
    }
```

Everybody knows backups should be made on a regular basis, so lets tell this job to run every night half past three by adding `@JobScheduleConfig` annotation. In this case we overwrite the method `scheduledJobExecutionCreation()` witch triggers the job to add some parameters.

```java
@JobScheduleConfig(hour = "3", minute = "30")
@JobConfig(name = "Backup", description = "Better backup!")
public class BackupJob extends JobWorker {

    private final Logger log = LoggerFactory.getLogger(BackupJob.class);

    @Override
    public void scheduledJobExecutionCreation() {

        BackupJobParameters parameters = new BackupJobParameters();
        parameters.setEvironment("STAGE-2");
        parameters.setReplaceOldBackup(false);

        createJobExecution(parameters);
    }

    @Override
    public void doWork(JobExecution jobExecution) {

        BackupJobParameters params = (BackupJobParameters) jobExecution.getParameters();

        log.info("Performing some fine backup on {} environment! Replace old backup: {}", params.getEvironment(), params.isReplaceOldBackup());
    }
}
```

Doesn't work? That is because you have to start the jobEngine using the method `start()` of the `JobEngineService` somewhere in your application. It takes the job queue polling interval in seconds as a parameter and there is also a `stop()` method to halt the job engine.

```java
    @Inject
    JobEngineService jobEngineService;

    public void start() {

        jobEngineService.start(5);
    }
```


## Install

1. Add the following dependency to your project ([published on Maven Central](http://search.maven.org/#artifactdetails%7Cio.coodoo%7Cworkhorse%7C1.0.0%7Cjar))
   
   ```xml
   <dependency>
       <groupId>io.coodoo</groupId>
       <artifactId>workhorse</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```
   
2. Create the database tables and add the JPA entities to your persistence.xml
   
   You can find SQL snippets to create the tables [here](https://github.com/coodoo-io/workhorse/tree/master/src/main/resources/sql). If you need the insert statements for another SQL database just use this [converter](http://www.sqlines.com/online).
   
   ```xml
    <class>io.coodoo.workhorse.jobengine.entity.Job</class>
    <class>io.coodoo.workhorse.jobengine.entity.JobExecution</class>
    <class>io.coodoo.workhorse.jobengine.entity.JobSchedule</class>
   ```
3. To provide the EntityManager you have to implement a `@JobEngineEntityManagerProducer` CDI producer.

   ```java
    @Stateless
    public class JobEngineEntityManagerProducer {
    
        @PersistenceContext
        private EntityManager entityManager;
    
        @Produces
        @JobEngineEntityManager
        public EntityManager getEntityManager() {
            return entityManager;
        }
    }
    ```
    *This is necessary to avoid trouble when it comes to different persistence contexts.*


## Changelog

All release changes can be viewed on our [changelog](./CHANGELOG.md).

## Maintainers

[coodoo](https://github.com/orgs/coodoo-io/people)

## Contribute

Pull requests and [issues](https://github.com/coodoo-io/workhorse/issues) are welcome.

## License

[Apache-2.0 © coodoo GmbH](./LICENSE)

Workhorse Logo: [http://www.how-to-draw-funny-cartoons.com](http://www.how-to-draw-funny-cartoons.com)