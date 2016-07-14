package com.test;


public class BaseVO<T> {
	private T instance;
	
	public T getObject(){
		return instance;
	}
	
	public void setObject(T instance){
		this.instance = instance;
	}
	
	public static void main(String[] args) {
		BaseVO<Double> vo = new BaseVO<Double>();
		vo.setObject(Double.valueOf(12.2));
		System.out.println(vo.getObject());
	}
	
}
