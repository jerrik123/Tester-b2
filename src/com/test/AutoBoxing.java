package com.test;

public class AutoBoxing {
	private static Integer i;
	
	public static void main(String[] args) {
		/*if(i == 2){
			System.out.println("aaa");
		}*/
		
		long start = System.currentTimeMillis();
		Long sum = 0L;
		for(int i=0;i<Integer.MAX_VALUE/2;i++){
			sum += i;
		}
		long end = System.currentTimeMillis();
		System.out.println("总耗时: " +(end-start)/1000.0 + "- " + sum);
		
		
		long start1 = System.currentTimeMillis();
		long sum1 = 0L;
		for(int i=0;i<Integer.MAX_VALUE/2;i++){
			sum1 += i;
		}
		long end1 = System.currentTimeMillis();
		System.out.println("总耗时1: " +(end1-start1)/1000.0  + "- " + sum1);
	}

}
