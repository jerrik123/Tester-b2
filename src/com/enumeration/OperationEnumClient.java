package com.enumeration;

public class OperationEnumClient {

	public static void main(String[] args) {
		/*double r = OperationEnum.PLUS.apply(3, 4);
		System.out.printf("PLUS: the operation is [%s],and the result is %.2f",OperationEnum.PLUS.getOperationChar(),r);
		String rr = String.format("PLUS: the operation is [%s],and the result is %.2f", OperationEnum.PLUS.getOperationChar(),r);
		System.out.println(rr);*/
		
		
		System.out.println(OperationEnum.fromString("+"));;
	}

}
