package com.unsafe;

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtExceptionHandlerTest {
	public void start(){
		final Thread t = new Thread(new MyTask());
		t.start();
	}
	
	public static void main(String[] args) {
		new UncaughtExceptionHandlerTest().start();
	}
}
