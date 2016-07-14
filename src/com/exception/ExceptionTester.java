package com.exception;

public class ExceptionTester {

	public static void main(String[] args) {
		try {
			throw new IllegalArgumentException("无效参数异常");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

}
