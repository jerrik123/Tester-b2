package com.enumeration;

public enum Operation1 {
	PLUS{
		@Override
		double apply(double x, double y) {
			return x+y;
		}
	};
	
	abstract double apply(double x,double y);
}
