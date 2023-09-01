package no.auke.m2.study;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {

    public AtomicInteger lagDelayMs=new AtomicInteger();
    public AtomicInteger speedDelayMs=new AtomicInteger();

    public BlockingQueue<Udp> outgoing = new LinkedBlockingDeque<>();
    public BlockingQueue<Udp> incoming = new LinkedBlockingDeque<>();

    public Peer(String id) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                long last_sent = 0;
                while(true) {
                    try {
                        Udp u = outgoing.take();
                        if(u.timeStamp<System.currentTimeMillis()-lagDelayMs.get()) {
                            long send_time = u.timeStamp+speedDelayMs.get();
                            Long wait = send_time - last_sent;
                            if(wait>0) {
                                Thread.sleep(wait);
                            }

                            // ready to send

                            last_sent=System.currentTimeMillis();

                        } else {
                            Thread.sleep(0,1000);
                            outgoing.add(u);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

    }

}
