package com.test;

public class Complex {
	public final int re;
	public final int xe;
	public Complex(int re, int xe) {
		super();
		this.re = re;
		this.xe = xe;
	}
	
	public Complex sub(Complex complex){
		return new Complex(this.re-complex.re, this.xe-complex.xe);
	}
	
	public Complex add(Complex complex){
		return new Complex(this.re+complex.re, this.xe+complex.xe);
	}

	@Override
	public String toString() {
		return "Complex [re=" + re + ", xe=" + xe + "]";
	}
	
}
