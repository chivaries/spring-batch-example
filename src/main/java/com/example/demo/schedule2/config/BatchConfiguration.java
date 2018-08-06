package com.example.demo.schedule2.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@ConditionalOnExpression("'${using.spring.autowiring}'=='complete'")
//@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {
    /**
     * JobRegistry 에 Spring Batch Job 을 자동으로 등록하기 위한 설정.
     * SpringBatchJobExecutor class 에서 jobLocator.getJob(jobName) 으로 job 을 찾기 위해서 필요
     *
     * @param jobRegistry ths Spring Batch Job Registry
     * @return JobRegistry BeanPostProcessor
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    /**
     * Quartz Job 의 instance 를 생성하고 의존성 주입 (@Autowired 된 객체에 DI) 가능한 JobFactory 생성
     *
     * @param beanFactory application context beanFactory
     * @return the job factory
     */
    @Bean
    public JobFactory jobFactory(AutowireCapableBeanFactory beanFactory) {
        return new SpringBeanJobFactory(){
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return job;
            }
        };
    }

    /**
     * Scheduler 전체를 관리하는 Manager.
     *
     * @return the scheduler factory bean
     * @throws Exception the exception
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory, Trigger[] registryTrigger) throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        schedulerFactoryBean.setSchedulerName("Scheduler-2");

        //Register QuartzProperties
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));

        //Register JobFactory
        schedulerFactoryBean.setJobFactory(jobFactory);

        //Graceful Shutdown 을 위한 설정으로 Job 이 완료될 때까지 Shutdown 을 대기하는 설정
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);

        //Job Detail 데이터 Overwrite 유무
        schedulerFactoryBean.setOverwriteExistingJobs(true);

        //Schedule 관리를 Spring Datasource 에 위임
        schedulerFactoryBean.setDataSource(dataSource);

        //Register Triggers
        schedulerFactoryBean.setTriggers(registryTrigger);


        return schedulerFactoryBean;
    }

    @Bean
    public Trigger[] registryTrigger(List<CronTriggerFactoryBean> cronTriggerFactoryBeanList) {
        return cronTriggerFactoryBeanList.stream().map(CronTriggerFactoryBean::getObject).toArray(Trigger[]::new);
    }

    /**
     * Spring Framework 의 Shutdown Hook 설정.
     * Quartz 의 Shutdown 동작을 위임받아 Graceful Shutdown 을 보장.
     * Quartz 의 자체 Shutdown Plugin 을 사용하면 Spring 의 Datasource 가 먼저 Close 되므로,
     * Spring 에게 Shutdown 동작을 위임하여, 상위에서 컨트롤.
     *
     * @param schedulerFactoryBean quartz schedulerFactoryBean.
     * @return SmartLifecycle
     */
    @Bean
    public SmartLifecycle gracefulShutdownHookForQuartz(SchedulerFactoryBean schedulerFactoryBean) {
        return new SmartLifecycle() {
            private boolean isRunning = false;
            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public void stop(Runnable callback) {
                stop();
                log.info("Spring container is shutting down.");
                callback.run();
            }

            @Override
            public void start() {
                log.info("Quartz Graceful Shutdown Hook started.");
                isRunning = true;
            }

            @Override
            public void stop() {
                isRunning = false;
                try {
                    log.info("Quartz Graceful Shutdown... ");
                    schedulerFactoryBean.destroy();
                } catch (SchedulerException e) {
                    try {
                        log.info(
                                "Error shutting down Quartz: " + e.getMessage(), e);
                        schedulerFactoryBean.getScheduler().shutdown(false);
                    } catch (SchedulerException ex) {
                        log.error("Unable to shutdown the Quartz scheduler.", ex);
                    }
                }
            }

            @Override
            public boolean isRunning() {
                return isRunning;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }
        };
    }
}
