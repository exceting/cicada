package io.cicada.spring.demo;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    public static void main(String[] args) {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("/xxx.xml");
        System.out.println(beanFactory.containsBean("student"));
    }

}
