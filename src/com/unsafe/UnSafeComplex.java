package com.unsafe;

import com.test.Complex;

public class UnSafeComplex {
	private  int re;
	private  int xe;
	
	public void setRe(int re) {
		this.re = re;
	}

	public void setXe(int xe) {
		this.xe = xe;
	}

	public Complex sub(Complex complex){
		return new Complex(this.re-complex.re, this.xe-complex.xe);
	}
	
	public Complex add(Complex complex){
		return new Complex(this.re+complex.re, this.xe+complex.xe);
	}
}
