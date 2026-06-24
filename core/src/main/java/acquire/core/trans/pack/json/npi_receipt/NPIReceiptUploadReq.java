package acquire.core.trans.pack.json.npi_receipt;

public class NPIReceiptUploadReq {

    private String orderNo ;
    private Long amount ;
    private String transDate ;
    private String transDetail ; //Base64
    private String[] commodityDetail;

    public NPIReceiptUploadReq(String orderNo,Long amount,String transDate,String transDetail){
        this.orderNo = orderNo ;
        this.amount = amount ;
        this.transDate = transDate ;
        this.transDetail = transDetail ;
        this.commodityDetail = new String[0] ;
    }

    public static class Detail{
        private String commodity ;
        private Long quantity ;
        private Long price ;

        public Detail(String commodity,Long quantity,Long price){
            this.commodity = commodity ;
            this.quantity = quantity ;
            this.price = price ;
        }
    }
}
