package com.enumeration;

public enum FruitsEnum {
	Apple("苹果","山东烟台"),
	Balana("香蕉","海南");
	FruitsEnum(String name,String desc) {
		this.name = name;
		this.desc = desc;
	}
	private final String name;
	
	private final String desc;
	
	public void show(){
		System.out.println(this.name + ", " + this.desc + "的比较好~");
	}
	
}
