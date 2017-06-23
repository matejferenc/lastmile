package com.lastmiles;

import io.test;

import java.util.function.Consumer;

/**
 * Created by trehak on 23.6.17.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        KafkaEventsService eventsService = new KafkaEventsService("ec2-35-158-118-82.eu-central-1.compute.amazonaws.com:9092");

        eventsService.produce(new test().setTest("tets val"));

        eventsService.startPolling(test.class, new Consumer<test>() {
            @Override
            public void accept(test test) {
                System.out.println(test);
            }
        });

        eventsService.close();
    }

}
