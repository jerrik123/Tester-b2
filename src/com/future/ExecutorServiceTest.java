package com.future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTest {

	public static void main(String[] args) {
		shutDownExecutor();
		//shutDownNowExecutor();
	}

	/**
		开始一个线程... pool-1-thread-1
		开始main线程...
		开始第二个线程... pool-1-thread-2
		第二个线程stop()...
		关闭线程池...
		第一个线程stop()...
	 */
	private static void shutDownExecutor() {
		ExecutorService exec = Executors.newFixedThreadPool(10);
		exec.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("开始一个线程... " + Thread.currentThread().getName());
				try {
					TimeUnit.SECONDS.sleep(10);
					System.out.println("第一个线程stop()...");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		exec.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("开始第二个线程... " + Thread.currentThread().getName());
				try {
					TimeUnit.SECONDS.sleep(3);
					System.out.println("第二个线程stop()...");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		
		System.out.println("开始main线程...");
		try {
			TimeUnit.SECONDS.sleep(4);
			exec.shutdown();
			System.out.println("关闭线程池...");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 *  开始一个线程... pool-1-thread-1
		开始main线程...
		开始第二个线程... pool-1-thread-2
		第二个线程stop()...
		关闭线程池...
	 */
	private static void shutDownNowExecutor() {
		ExecutorService exec = Executors.newFixedThreadPool(10);
		exec.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("开始一个线程... " + Thread.currentThread().getName());
				try {
					TimeUnit.SECONDS.sleep(10);
					System.out.println("第一个线程stop()...");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		exec.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("开始第二个线程... " + Thread.currentThread().getName());
				try {
					TimeUnit.SECONDS.sleep(3);
					System.out.println("第二个线程stop()...");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		
		System.out.println("开始main线程...");
		try {
			TimeUnit.SECONDS.sleep(4);
			exec.shutdownNow();
			System.out.println("关闭线程池...");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
