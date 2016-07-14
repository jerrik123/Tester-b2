package com.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureTest {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		Future<String> future = executor.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				TimeUnit.SECONDS.sleep(5);
				return "ok...";
			}
			
		});
		
		
		try {
			String result = future.get(6, TimeUnit.SECONDS);
			System.out.println(result);
		} catch (TimeoutException e) {
			System.out.println("服务超时...");
			executor.shutdownNow();
		}
	}

}
