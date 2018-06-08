
CREATE TABLE `jobengine_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8_bin NOT NULL,
  `description` varchar(2028) COLLATE utf8_bin DEFAULT NULL,
  `worker_class_name` varchar(512) COLLATE utf8_bin NOT NULL,
  `type` varchar(32) COLLATE utf8_bin NOT NULL DEFAULT 'ADHOC',
  `status` varchar(32) COLLATE utf8_bin NOT NULL DEFAULT 'ACTIVE',
  `threads` int(4) NOT NULL DEFAULT '1',
  `fail_retries` int(4) NOT NULL DEFAULT '0',
  `retry_delay` int(11) unsigned NOT NULL DEFAULT '4000',
  `unique_in_queue` bit(1) NOT NULL DEFAULT b'1',
  `days_until_clean_up` int(4) NOT NULL DEFAULT '30',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `worker_class_name` (`worker_class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `jobengine_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL,
  `second` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '0',
  `minute` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '0',
  `hour` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '0',
  `day_of_week` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '*',
  `day_of_month` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '*',
  `month` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '*',
  `year` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '*',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_id` (`job_id`),
  KEY `fk_jobengine_schedule_job_idx` (`job_id`),
  CONSTRAINT `fk_jobengine_schedule_job` FOREIGN KEY (`job_id`) REFERENCES `jobengine_job` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `jobengine_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL,
  `status` varchar(32) COLLATE utf8_bin NOT NULL DEFAULT 'QUEUED',
  `started_at` datetime DEFAULT NULL,
  `ended_at` datetime DEFAULT NULL,
  `priority` bit(1) NOT NULL DEFAULT b'0',
  `maturity` datetime DEFAULT NULL,
  `chain_id` bigint(20) DEFAULT NULL,
  `chain_previous_execution_id` bigint(20) DEFAULT NULL,
  `duration` bigint(20) DEFAULT NULL,
  `parameters` mediumtext COLLATE utf8_bin,
  `parameters_hash` int(11) DEFAULT NULL,
  `log` mediumtext COLLATE utf8_bin,
  `fail_retry` int(4) NOT NULL DEFAULT '0',
  `fail_retry_execution_id` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `fail_message` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `fail_stacktrace` mediumtext COLLATE utf8_bin,
  PRIMARY KEY (`id`),
  KEY `fk_jobengine_job_execution_job_idx` (`job_id`),
  KEY `idx_jobengine_job_execution_jobid_status` (`job_id`,`status`),
  KEY `fk_jobengine_job_execution_jobid_status` (`job_id`,`status`),
  KEY `idx_jobengine_job_execution_poller` (`job_id`,`status`,`parameters_hash`),
  CONSTRAINT `fk_jobengine_job_execution_job` FOREIGN KEY (`job_id`) REFERENCES `jobengine_job` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
