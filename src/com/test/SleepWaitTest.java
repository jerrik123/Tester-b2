package com.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class SleepWaitTest {
	private  List<String> list = new ArrayList<String>();

	public static void main(String[] args) {
		for(int i=0;i<1111111111;i++){
			
		}
		new Thread(new Thread1()).start();
		new Thread(new Thread2()).start();
	}

	public static class Thread1 implements Runnable {

		@Override
		public void run() {
			synchronized (SleepWaitTest.class) {
				System.out.println("thread1...begin");
				for(int i=0;i<11111111;i++){
					System.out.print("");
				}
				System.out.println("thread1...end");
			}
		}

	}

	public static class Thread2 implements Runnable {

		@Override
		public void run() {
			synchronized (List.class) {
				System.out.println("thread2...begin");
				System.out.println("thread2...end");
			}
		}

	}

}
