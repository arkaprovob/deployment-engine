package io.spaship;

import io.smallrye.mutiny.Multi;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class RandomTest {

    @Test
    public void testMulti() {

        Multi.createFrom().items(1, 2, 3, 4,5,6,7,8,9)
                .onItem()
                .invoke(item -> {
                    System.out.println("per " + item);
                    item = item * 10;
                }).map(item -> {
                    if(item==5){
                        //throw new RuntimeException("congratz its an error");
                    }

            return item * 10;
        }).onFailure()
                .recoverWithItem(11)
                .onSubscription().invoke(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("subscribed "+Thread.currentThread().getName());
        }).subscribe()
                .with(item-> System.out.println("consuming  "+item +" from "+Thread.currentThread().getName()));

        //scb.onComplete();
    }



}
