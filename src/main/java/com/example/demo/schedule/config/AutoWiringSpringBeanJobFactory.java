package com.example.demo.schedule.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/*
 * SpringBeanJobFactory 는 instance 를 생성하는 동안 scheduler context, job data map, trigger 데이터 항목의 properties 를 job bean 에 inject 하는 것을 지원한다.
 * 그러나 application context 로 부터 inject 할 방법은 없다.
 * 그래서 SpringBeanJobFactory 를 상속하여 auto-wiring 을 지원하는 factory bean 을 만든다.
 */
public final class AutoWiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        // job class 를 autowire 가능한 bean 으로 등록
        beanFactory.autowireBean(job);
        return job;
    }
}
