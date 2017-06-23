package com.lastmiles;

import io.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by trehak on 23.6.17.
 */
public class Test {

//    public static void main(String[] args) throws Exception {
//        KafkaEventsService eventsService = new KafkaEventsService("ec2-35-158-118-82.eu-central-1.compute.amazonaws.com:9092");
//
//        eventsService.listen(test.class, new Consumer<test>() {
//            @Override
//            public void accept(test test) {
//                System.out.println(test);
//            }
//        });
//
//        final AtomicInteger cnt = new AtomicInteger(0);
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i < 100; i++) {
//                        eventsService.produce(new test().setTest(cnt.incrementAndGet() + ""));
//                        Thread.sleep(1000);
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
//        thread.start();
//
//
//        Thread.sleep(5000);
//
//    }

}
