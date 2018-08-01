package com.example.demo.hello.schedule;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnExpression("'${using.spring.autowiring}'=='false'")
public class QuartzConfiguration {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    /*
     * JobRegistry 에 Job 을 자동으로 등록하기 위한 설정.
     * QuartzJobLauncher class 에서 jobLocator.getJob(jobName) 으로 job 을 찾기 위해서 필요
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    /*
     * Quartz 의 JobDetail instance 를 생성하는 Spring FactoryBean
     */
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();

        // 실행할 Job 을 소스상에서주입
        factory.setJobClass(QuartzJobLauncher.class);

        Map map = new HashMap<String, Object>();
        map.put("jobName", "importUserJob");
        map.put("jobLauncher", jobLauncher);
        map.put("jobLocator", jobLocator);

        // JobDataMap 은 Job 이 실행될 때 Job 인스턴스에서 사용 가능하게 하려는 모든 양의 데이터를 가지는데 사용
        // jobName 과 jobLauncher, jobLocator 를 제공
        // Quartz 의 기본 JobFactory 에 의해 Job 이 인스턴스될때 자동으로 각각의 setter 를 호출
        // QuartzJobLauncher.setJobName("importUserJob");
        // QuartzJobLauncher.setJobLaucher(jobLauncher);
        // QuartzJobLauncher.setJobLocator(jobLocator);
        factory.setJobDataAsMap(map);
        factory.setGroup("user_group");
        factory.setName("import_user_job");
        return factory;
    }

    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean() {
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        // jobDetail 소스상에서 dependency 주입
        triggerFactoryBean.setJobDetail(jobDetailFactoryBean().getObject());
        triggerFactoryBean.setStartDelay(3000);
        triggerFactoryBean.setName("cron_trigger");
        triggerFactoryBean.setGroup("cron_group");
        triggerFactoryBean.setCronExpression("0 * * 1/1 * ? *");
        return triggerFactoryBean;
    }

    /**
     * Scheduling 전체를 관리하는 manager
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        // Trigger 소스상에서 dependency 주입
        scheduler.setTriggers(cronTriggerFactoryBean().getObject());
        return scheduler;
    }

}
