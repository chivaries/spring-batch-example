package com.example.demo.hello.schedule;

import com.example.demo.hello.schedule.config.AutoWiringSpringBeanJobFactory;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnExpression("'${using.spring.autowiring}'=='true'")
@Slf4j
public class AutowiringQuartzConfiguration {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("Autowiring Spring Quartz ...");
    }

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

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        log.debug("Configuring Job factory");

        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail jobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        log.debug("Setting the Scheduler up");
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(jobDetail);
        schedulerFactory.setTriggers(trigger);

        return schedulerFactory;
    }

    @Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();

        // 실행할 Job 을 소스상에서주입
        jobDetail.setJobClass(QuartzJobLauncher.class);

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
        jobDetail.setJobDataAsMap(map);
        jobDetail.setGroup("user_group");
        jobDetail.setName("import_user_job");
        jobDetail.setDurability(true);

        return jobDetail;
    }

    @Bean
    public CronTriggerFactoryBean trigger(JobDetail jobDetail) {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(jobDetail);
        trigger.setStartDelay(3000);
        trigger.setName("cron_trigger");
        trigger.setGroup("cron_group");
        trigger.setCronExpression("0 * * 1/1 * ? *");
        return trigger;
    }

//    @Bean
//    public SimpleTriggerFactoryBean trigger(JobDetail jobDetail) {
//
//        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
//        trigger.setJobDetail(jobDetail);
//
//        int frequencyInSec = 10;
//        log.info("Configuring trigger to fire every {} seconds", frequencyInSec);
//
//        trigger.setRepeatInterval(frequencyInSec * 1000);
//        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
//        trigger.setName("Qrtz_Trigger");
//        return trigger;
//    }
}
