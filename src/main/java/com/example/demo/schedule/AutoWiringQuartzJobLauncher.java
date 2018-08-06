package com.example.demo.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AutoWiringQuartzJobLauncher implements org.quartz.Job {
    private String jobName;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // JobRegistry 등록된 Job 을 jobName 으로 찾는다.
            Job job = jobLocator.getJob(jobName);
            JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
            log.info("{}_{} was completed successfully", job.getName(), jobExecution.getId());
        } catch (Exception e) {
            log.error("Encountered job execution exception! : {}", e.getMessage());
        }
    }
}

