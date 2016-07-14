package com.enumeration;

public enum Operation {

	PLUS,MINUS,MUL,DIV;
	
	double apply(double x,double y){
		switch(this){
			case PLUS:return x+y;
		}
		throw new AssertionError();
	}
}
