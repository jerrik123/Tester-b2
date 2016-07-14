package com.enumeration;

import java.util.HashMap;
import java.util.Map;

public enum OperationEnum {
	PLUS("+") {
		@Override
		double apply(double x, double y) {
			return x + y;
		}
	},
	SUB("-") {
		@Override
		double apply(double x, double y) {

			return x - y;
		}
	},
	MUL("*") {
		@Override
		double apply(double x, double y) {
			return x * y;
		}
	},
	DIV("/") {
		@Override
		double apply(double x, double y) {
			return x / y;
		}
	};

	public String toString() {
		return operationChar;
	}

	private final String operationChar;

	private static final Map<String, OperationEnum> CACHE_MAP = new HashMap<String, OperationEnum>();

	static {
		for (OperationEnum enums : values()) {
			CACHE_MAP.put(enums.getOperationChar(), enums);
		}
	}

	public static OperationEnum fromString(String operationChar) {
		return CACHE_MAP.get(operationChar);
	}

	public String getOperationChar() {
		return operationChar;
	}

	OperationEnum(String operationChar) {
		this.operationChar = operationChar;
	}

	/**
	 * 枚举中定义了抽象类,所以每个枚举值都必须把它覆盖掉
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	abstract double apply(double x, double y);

}
