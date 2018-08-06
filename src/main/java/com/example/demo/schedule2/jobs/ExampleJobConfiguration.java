package com.example.demo.schedule2.jobs;


import com.example.demo.schedule2.BatchHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Slf4j
@Configuration
public class ExampleJobConfiguration {
    @Autowired
    public Job importUserJob;

    @Bean
    public JobDetailFactoryBean exampleJobSchedule() {
        return BatchHelper.jobDetailFactoryBeanBuilder()
                .job(importUserJob)
                .build();
    }

    @Bean
    CronTriggerFactoryBean exampleJobTrigger() {
        return BatchHelper.cronTriggerFactoryBeanBuilder()
                .cronExpression("0 0/1 * 1/1 * ? *")
                .jobDetailFactoryBean(exampleJobSchedule())
                .build();
    }
}
