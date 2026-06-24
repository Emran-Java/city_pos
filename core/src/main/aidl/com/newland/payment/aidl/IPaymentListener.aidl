package com.newland.payment.aidl;
interface IPaymentListener{
	void onResult(inout Bundle data);
}
