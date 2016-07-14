package com.enumeration;

public class Operation1Test {

	public static void main(String[] args) {
		double x = 1.2;
		double y = 3.1;
		double r = Operation1.PLUS.apply(x, y);
		System.out.println(r);
	}

}
