package com.example.demo.hello.schedule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class QuartzJobLauncher extends QuartzJobBean {
    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public JobLocator getJobLocator() {
        return jobLocator;
    }

    public void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
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
