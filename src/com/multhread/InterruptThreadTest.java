package com.multhread;

import java.util.concurrent.TimeUnit;

public class InterruptThreadTest {

	public static void main(String[] args) {
		/*
		 * try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) {
		 * System.out.println(e.getMessage());
		 * Thread.currentThread().interrupt(); }
		 */

		/*Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("status0: " + Thread.currentThread().isInterrupted());
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					System.out.println("status1: " + Thread.currentThread().isInterrupted());
					e.printStackTrace();
					Thread.currentThread().interrupt();
					System.out.println("status2: " + Thread.currentThread().isInterrupted());
				}
			}
		});

		t.start();
		
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		t.interrupt();*/
		
		
		System.out.println(Thread.currentThread().isInterrupted());
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(Thread.currentThread().isInterrupted());
	}

}
