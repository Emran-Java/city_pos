package acquire.core.trans.pack.json.pixcel;

import java.util.List;

public class PixcelReceiptReq {
    public String merchantId ;
    public String deviceId ;
    public String transactionId ;
    public String terminalTransactionTime ;
    public String transactionAmount ;
    public String currencyCodeAlpha3 ;
    public String transactionType ;// CASH, CARD, WALLET, Optional
    public String technologySelected ;
    public String selectedService ;
    public String cardType ;
    public String transacitonResult ;
    public String applicationIdentifier ;
    public String applicationLable ;
    public String maskedPan ;
    public Integer numberOdProductsInBasket ;
    public List<Product> basketContent ;

}

