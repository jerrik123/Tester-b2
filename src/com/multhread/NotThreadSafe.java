package com.multhread;

public class NotThreadSafe {

	private volatile static boolean ready = false;;
	private static int number = 0;
	
	public static void main(String[] args) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!ready){
					
				}
				System.out.println("number: " + number);
			}
		}).start();
		number = 5;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("ready...");
		ready = true;
	}
}
