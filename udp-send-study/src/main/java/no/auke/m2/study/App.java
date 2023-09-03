package no.auke.m2.study;

import java.util.Random;

public class App {

    public static int chunkSize=100;
    private static Random rnd;
    public static long newId() {
        return rnd.nextLong();
    }

    static void main(String[] args) {
        rnd = new Random();
        rnd.setSeed(System.currentTimeMillis());
    }
}
