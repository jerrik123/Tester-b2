package com.test;

public class StringIntern {

	public static void main(String[] args) {
		String a =  "b" ;   
	    String b =  "b" ;   
	      
	    System.out.println( a == b);   
	      
	    String c = "d" ;  
	    String d = new String( "d" ).intern() ;   
	    System.out.println( c == d);  
	    
	    String e = "d" ;  
	    String f = new String( "d" );
	    System.out.println(e == f);
	}

}
