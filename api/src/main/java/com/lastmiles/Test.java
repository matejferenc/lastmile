package com.lastmiles;

import io.test;

import java.util.List;

/**
 * Created by trehak on 23.6.17.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        KafkaEventsService eventsService = new KafkaEventsService("ec2-35-158-118-82.eu-central-1.compute.amazonaws.com:9092");

        eventsService.produce(new test().setTest("tets val"));

        List<test> events = eventsService.poll(test.class);
        System.out.println(events.size());

        eventsService.close();
    }

}
