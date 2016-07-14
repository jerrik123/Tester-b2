package com.enumeration;

public class DBManager {
	
	private OperationType operationType;

	@SuppressWarnings("unused")
	private enum OperationType  {
		INSERT("C"), UPDATE("U"), DELETE("D");
		private final String operateChar;

		OperationType(String operateChar) {
			this.operateChar = operateChar;
		}
	}
}
