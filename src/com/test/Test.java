package com.test;

import java.lang.reflect.Method;

public class Test {
/*	static {
		StringBuffer sb = new StringBuffer();
		sb.append(1);
		sb.append(3);
		sb.append(22);
		//System.out.println(sb.toString());
	}*/

	public static void main(String[] args) throws SecurityException, NoSuchMethodException {
		/*
		 * Complex complex = new Complex(3, -1); Complex complex2 = new
		 * Complex(2, -2); complex2 = complex.sub(complex2);
		 * 
		 * System.out.println(complex2);
		 * 
		 * for(;;){
		 * 
		 * }
		 */
		/*
		 * String s = System.getProperty("java.ext.dirs");
		 * System.out.println(s); s = System.getProperty("java.class.path");
		 * System.out.println(s);
		 */
		// new Test();new Test();new Test();

		/*
		 * System.out.println(new Date());
		 * 
		 * synchronized (lock) { //1.查询交易订单号是否存在
		 * 
		 * //2.新增交易
		 * 
		 * }
		 */

	/*	Method method = Files.class.getMethod("createPrintStr", String.class, Integer.class);
		System.out.println(method.getDeclaringClass().getName() + "." + method.getName());*/
		
		int index = show();
		System.out.println("index=: " + index);
		
		Person p = showPerson();
		System.out.println(p);
	}
	
	private static class Person{
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Person [name=" + name + "]";
		}
	}

	@SuppressWarnings("finally")
	private static int show() {
		int index = 1;
		try {
			return index;
		} catch (Exception e) {
			return index;
		}finally{
			index++;
		}
		//return index;
	}
	
	private static Person showPerson() {
		Person p = new Person();
		
		try {
			p.setName("admin");
			return p;
		} catch (Exception e) {
			return p;
		}finally{
			p.setName("finally");
		}
		
	}

}
