package no.auke.p2p.m2.systemtests;

import static org.junit.Assert.*;

import java.util.Random;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.sockets.udp.DelaySpeed;

import org.junit.Test;

public class DelaySpeedTest {

	@Test
	public void test_speed() {
		
		System.out.println("test_speed -----------------");

		int OLD_MAX_SPEED = InitVar.MAX_SPEED;
		InitVar.MAX_SPEED=5000;
		int OLD_PACKET_SIZE = InitVar.PACKET_SIZE;
		InitVar.PACKET_SIZE=512;

        Random rnd = new Random();		
        for(int speed = 1;speed < InitVar.MAX_SPEED;speed+=rnd.nextInt((speed+10)/2)){
        	
    		DelaySpeed delayspeed = new DelaySpeed();
    		
    		delayspeed.setNewSpeed(speed);
    		delayspeed.incrClient();

            long bytes = 0;
    		long start = System.currentTimeMillis();
    		long stop = start;
    		long totalDelay = 0;
    		while(System.currentTimeMillis() - start < 2000){
    			bytes+=InitVar.PACKET_SIZE;
    			delayspeed.doDelayPacketSend();
    			totalDelay+=delayspeed.getLastdelay();
    		}
    		stop=System.currentTimeMillis();
    		
    		//double speed_real = ((bytes*1000.0) / ((stop - start) * 1024));
    		double speed_real = ((((bytes+0.0)/1024.0)*1000.0) / (totalDelay + 0.0)) ;
    		System.out.println("bytes " + bytes + " time " + (stop - start) + " Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real) + " total delay " + String.valueOf(totalDelay));
    		assertEquals("Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real)
    				, delayspeed.getSpeed(),speed_real,delayspeed.getSpeed() * 0.5);
    		 		
    	}	
    	InitVar.MAX_SPEED = OLD_MAX_SPEED;
    	InitVar.PACKET_SIZE = OLD_PACKET_SIZE;
		
	}

	@Test
	public void test_packsize() {
		
        Random rnd = new Random();

		System.out.println("test_packsize -----------------");
		int OLD_MAX_SPEED = InitVar.MAX_SPEED;
		InitVar.MAX_SPEED=5000;

        int speed = 250;
        int OLD_PACKET_SIZE = InitVar.PACKET_SIZE;
    	for(int packsize = 100;packsize < 10000;packsize+=rnd.nextInt(packsize+50)){

    		InitVar.PACKET_SIZE=packsize;
    		DelaySpeed delayspeed = new DelaySpeed();
    		
    		delayspeed.incrClient();
    		delayspeed.setNewSpeed(speed);

    		long start = System.currentTimeMillis();
    		long stop = start;
    		
    		int bytes = 0;
    		long totalDelay = 0;
    		while(System.currentTimeMillis() - start < 2000){
    			
    			bytes+=InitVar.PACKET_SIZE;
    			delayspeed.doDelayPacketSend();
    			totalDelay+=delayspeed.getLastdelay();
    			stop=System.currentTimeMillis();
    		}
    		
    		//double speed_real = ((bytes * 1000.0)/1024.0) / (stop-start);
    		double speed_real = ((bytes * 1000.0)/1024.0) / (totalDelay);
    		System.out.println("bytes " + bytes + " time " + (stop - start) + " Speed kb/s: " + "Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real) + " packsize " + String.valueOf(InitVar.PACKET_SIZE));
    		assertEquals("Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real) + " packsize " + String.valueOf(InitVar.PACKET_SIZE), delayspeed.getSpeed(),speed_real,delayspeed.getSpeed() * 0.5);
    		 		
    	}				
    	InitVar.MAX_SPEED = OLD_MAX_SPEED;
    	InitVar.PACKET_SIZE = OLD_PACKET_SIZE;
		
	}
	
	@Test
	public void test_speed_packsize() {
		
        Random rnd = new Random();

		System.out.println("test_speed_packsize -----------------");
		
		int OLD_MAX_SPEED = InitVar.MAX_SPEED;
		InitVar.MAX_SPEED=5000;
		int OLD_PACKET_SIZE = InitVar.PACKET_SIZE;
    	for(int i=1;i<10;i++){

    		InitVar.PACKET_SIZE=rnd.nextInt(10000) + 10;
    		DelaySpeed delayspeed = new DelaySpeed();
    		
    		delayspeed.incrClient();
    		delayspeed.setNewSpeed(rnd.nextInt(InitVar.MAX_SPEED)+3);

    		long start = System.currentTimeMillis();
    		long stop = start;
    		long totalDelay = 0;
    		int bytes = 0;
    		while(System.currentTimeMillis() - start < 2000){
    			
    			bytes+=InitVar.PACKET_SIZE;
    			delayspeed.doDelayPacketSend();
    			stop=System.currentTimeMillis();
    			totalDelay+=delayspeed.getLastdelay();
    		}
    		
    		//double speed_real = ((bytes * 1000.0)/1024.0) / (stop-start);
    		double speed_real = ((bytes * 1000.0)/1024.0) / (totalDelay);
    		System.out.println("bytes " + bytes + " time " + (stop - start) + " Speed kb/s: " + "Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real) + " packsize " + String.valueOf(InitVar.PACKET_SIZE));
    		assertEquals("Speed kb/s: " + String.valueOf(delayspeed.getSpeed()) + " Speed real kb/s: " + String.valueOf(speed_real) + " packsize " + String.valueOf(InitVar.PACKET_SIZE), delayspeed.getSpeed(),speed_real,delayspeed.getSpeed() * 0.5);
    		 		
    	}
    	InitVar.MAX_SPEED = OLD_MAX_SPEED;
    	InitVar.PACKET_SIZE = OLD_PACKET_SIZE;
		
	}	

}
