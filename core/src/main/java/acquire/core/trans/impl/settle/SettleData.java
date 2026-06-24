package acquire.core.trans.impl.settle;

public class SettleData {
    private static long mTotalAmount = 0;
    private static long mTotalNumber = 0;

    private static SettleData mSettleData;

    public static SettleData getInstance(){
        if(mSettleData==null){
            mSettleData = new SettleData();
        }
        return mSettleData;
    }

    private SettleData(){}

    public long getTotalAmount() {
        return mTotalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.mTotalAmount = totalAmount;
    }

    public long getTotalNumber() {
        return this.mTotalNumber;
    }

    public void setTotalNumber(long totalNumber) {
        this.mTotalNumber = totalNumber;
    }

    public void clearData(){
        mTotalAmount = 0;
        mTotalNumber = 0;
    }

    public void destroyObject(){
        clearData();
        mSettleData=null;
    }
}
