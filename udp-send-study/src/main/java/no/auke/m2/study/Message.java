package no.auke.m2.study;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public int size;

    public List<Chunk> chunks = new ArrayList<>();
    public List<Chunk> sent_chunks = new ArrayList<>();
    public List<Chunk> confirm_chunks = new ArrayList<>();

    public Message(int size) {
        this.size=size;
    }
}
