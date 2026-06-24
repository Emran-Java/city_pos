# Transactions

[AbstractTrans](./core/src/main/java/acquire/core/trans/AbstractTrans.java) is the basic transaction class. `TransActivity` starts `AbstractTrans` implementation class to complete a transaction. Its has a chain to   manage the steps of transaction.

On [TransFactory](./core/src/main/java/acquire/core/config/TransFactory.java), you can config the `AbstractTrans` implementation class to a trans type. In additional, it can config the transaction switch, name  and its settle attribute.

```java
public class TransFactory {

    public Param getParam(String transType) {
        if (transType == null){
            return null;
        }
        switch (transType) {
            //Sale
            case TransType.TRANS_SALE:
                return new Param.Builder(Sale.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_SALE)
                        .name(R.string.core_transaction_name_sale)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            default:
                return null;
        }
    }
}
```

**Sale.java**

```java
public class Sale extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new TipAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackSaleStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }
}

```

