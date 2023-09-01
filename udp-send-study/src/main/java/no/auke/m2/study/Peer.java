package no.auke.m2.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Peer {

    public List<Udp> outgoing = new ArrayList<>();
    public BlockingQueue<Udp> incoming = new LinkedBlockingDeque<>();

    public Peer(String id) {

    }

}
