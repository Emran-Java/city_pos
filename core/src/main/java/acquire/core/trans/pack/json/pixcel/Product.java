package acquire.core.trans.pack.json.pixcel;

import com.google.gson.Gson;

import acquire.base.utils.LoggerUtils;

public class Product {
    public String productName ;
    public Integer productQuantity ;
    public Integer price ;



    public static Product goodToProduct(String goodString){
        LoggerUtils.d("goodString:"+goodString);
        Gson gson = new Gson() ;
        GoodDetail goodDetail = gson.fromJson(goodString,GoodDetail.class);
        if (goodDetail == null){return null;}
        Product product = new Product() ;
        product.productName = goodDetail.commodity ;
        product.price = goodDetail.price.intValue() ;
        product.productQuantity = goodDetail.quantity.intValue() ;
        return product ;
    }


    class GoodDetail{
        public String commodity ;
        public Long price ;
        public Long quantity ;
    }


}
