package io.github.exceting.cicada.spring.demo;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class Test {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"/xxx.xml"}, false, null);
        context.addApplicationListener(new ApplicationListener<>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                System.out.println("========== " + event.getTimestamp());
            }
        });

        //context.addBeanFactoryPostProcessor();
        BeanFactory beanFactory = (BeanFactory) context;
        ResourceLoader resource = (ResourceLoader) context;
        context.refresh();
        System.out.println(resource.getClassLoader());
        System.out.println(beanFactory.containsBean("student"));
    }

}
