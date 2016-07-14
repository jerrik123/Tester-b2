package com.test;

public class Test1 {

	public static void main(String[] args) {
		Parent[] parents = new Parent[1];
		Children[] children = new Children[1];
		parents = (Parent[])children;
	}
	
	public static class Parent{
		void show(){
			System.out.println("Parent show...");
		}
	}
	
	public static class Children extends Parent{
		void show(){
			System.out.println("Children show...");
		}
	}

}
