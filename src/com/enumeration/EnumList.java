package com.enumeration;

import java.util.Arrays;
import java.util.Collection;

public class EnumList {

	public static void main(String[] args) {
		Collection<OperationEnum> operationEnumList = Arrays.asList(OperationEnum.values());
		for(OperationEnum enums : operationEnumList){
			System.out.println(enums.toString());
		}
	}

}
