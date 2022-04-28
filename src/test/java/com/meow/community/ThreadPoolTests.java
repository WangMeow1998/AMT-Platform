package com.meow.community;


import com.meow.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    //JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private AlphaService alphaService;


    private void sleep(long m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //1.JDK普通线程池
    @Test
    public void testExecutorService(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("ThreadPool -- ExecutorService");
            }
        });
        for(int i = 0; i < 10; i++){
            executorService.submit(task);
        }
        sleep(10000);
    }

    //2.JDK定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("ThreadPool -- ScheduledExecutorService");
            }
        });
        //线程，延迟时间，运行间隔，单位
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    //3.Spring普通线程池 这个线程池相比于JDK线程池，性能好些，推荐使用
    @Test
    public void testThreadPoolTaskExecutor(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("ThreadPool -- ThreadPoolTaskExecutor");
            }
        });
        for(int i = 0; i < 10; i++){
            threadPoolTaskExecutor.submit(task);
        }
        sleep(10000);
    }

    //4.Spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("ThreadPool -- ThreadPoolTaskScheduler");
            }
        });
        Date time = new Date(System.currentTimeMillis() + 10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task, time, 1000);
        sleep(30000);
    }

    //5.Spring普通线程池（简易）
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for(int i = 0; i < 10; i++){
            alphaService.execute1();
        }
        sleep(10000);
    }


    //6.Spring定时任务线程池（简易）
    @Test
    public void testThreadPoolTaskSchedulerSimple(){
        sleep(30000);
    }
}
