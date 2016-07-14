package com.multhread;

import java.text.SimpleDateFormat;

public class InterruptDownLoad2 {
	public static void main(String[] args) {
		NRunnable runnable = new NRunnable();
		Thread t = new Thread(runnable);
		t.start();
		System.out.println("is start.......");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {

		}
		runnable.cancel();
		System.out.println("cancel ..." + System.currentTimeMillis());
	}

	public static class NRunnable implements Runnable {
		public boolean isCancel = false;

		@Override
		public void run() {
			while (!isCancel) {
				System.out.println("我没有种中断");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			System.out.println("我已经结束了..." + System.currentTimeMillis());
		}

		public void cancel() {
			this.isCancel = true;
		}
	}
}
