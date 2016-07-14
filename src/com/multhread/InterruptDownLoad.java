package com.multhread;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class InterruptDownLoad {
	private static SimpleDateFormat format = new SimpleDateFormat("");

	private static final String TAG = "Main";

	public static void main(String[] args) {
		Thread t = new Thread(new NRunnable());
		t.start();
		System.out.println("is start.......");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}
		
		System.out.println("中断状态1..." + t.isInterrupted());//false
		t.interrupt();//开始中断
		System.out.println("中断状态2..." + t.isInterrupted());//true
		/*t.interrupted();
		System.out.println("中断状态3..." + t.isInterrupted());*/

	}

	public static class NRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				System.out.println("我没有中断");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
			}
		}

	}
}
