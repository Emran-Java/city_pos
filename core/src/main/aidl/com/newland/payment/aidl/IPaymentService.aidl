package com.newland.payment.aidl;
import com.newland.payment.aidl.IPaymentListener;
interface IPaymentService{
	/**
     * set param
     */
    boolean setParam(String key, String value);
    /**
     * get param
     */
	String getParam(String key);
    /**
     * inquiry record by out order number
     * @param origOutOrderNo the out order number of the original record
     * @param iPaymentListener  result listener
     */
    void findByOutOrderNo(String origOutOrderNo, IPaymentListener iPaymentListener);
    /**
     * inquiry record by trace number
     * @param oriTraceNo the trace number of the original record
     * @param iPaymentListener  result listener
     */
    void findByTraceNo(String oriTraceNo, IPaymentListener iPaymentListener);

}
