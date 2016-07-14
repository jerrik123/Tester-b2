package com.enumeration;

public enum PayrollDay {
	Monday(PayType.WorkDay), Sunday(PayType.RestDay);

	private PayType payType;
	
	PayrollDay(PayType payType) {
		this.payType = payType;
	}


	public enum PayType {
		WorkDay {

			@Override
			double overTimePay(int hour, double payRate) {

				return 0;
			}

		},
		RestDay {

			@Override
			double overTimePay(int hour, double payRate) {

				return 0;
			}

		};
		abstract double overTimePay(int hour, double payRate);
	}
}
