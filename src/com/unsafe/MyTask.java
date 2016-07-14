package com.unsafe;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;

public class MyTask implements Runnable {

	@Override
	public void run() {
		try {
			Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
				@Override
				public void uncaughtException(Thread t1, Throwable e) {
					System.out.println("正在恢复unchecked exception...threadName: " + t1.getId() + "-" + t1.getName());
					e.printStackTrace();
					 new Thread(new MyTask()).start();
				}
			});
			System.out.println("myTask准备开始休眠...");
			TimeUnit.SECONDS.sleep(3);
			throw new NullPointerException("自定义空指针异常");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
