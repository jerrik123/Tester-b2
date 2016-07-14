package com.test;

public class ClassCast {

	public static void main(String[] args) {
		Object obj = convert(String.class, "123");
	}
	
	public static <T> T convert(Class<T> clazz,Object obj){
		return clazz.cast(obj);
	}

}
