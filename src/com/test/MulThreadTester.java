package com.test;

import java.util.concurrent.TimeUnit;

public class MulThreadTester {

	private static boolean stop = false;
	
	public static void main(String[] args) throws InterruptedException {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!stopRequested()){
					System.out.println("线程stop...");
				}
			}
			
		}).start();
		
		TimeUnit.SECONDS.sleep(5);
		stopCommand();
	}
	
	private static synchronized void stopCommand() {
		stop = true;
	}
	
	private static synchronized boolean stopRequested(){
		return stop;
	}

}
