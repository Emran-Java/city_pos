package acquire.base.utils.currency;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Currency utils
 *
 * @author Janson
 * @date 2023/12/27 10:53
 */
public class CurrencyUtils {
    private final static List<CurrencyBean> RECENT = new ArrayList<>();

    /**
     * Get currency symbol by currency code.
     *
     * @param currencyCode Currency code or Numeric code.
     *                     <p>e.g. 156 or CNY -> ¥
     * @return currency symbol
     */
    public static String getCurrencySymbol(String currencyCode) {
        CurrencyBean currencyBean = match(RECENT, currencyCode);
        if (currencyBean == null) {
            List<CurrencyBean> currencyBeans = getAllCurrencies();
            currencyBean = match(currencyBeans, currencyCode);
            if (currencyBean != null) {
                RECENT.add(currencyBean);
            }
        }
        return currencyBean == null ? null : currencyBean.getSymbol();
    }

    /**
     * get the currency information
     * @param currencyCode Currency code or Numeric code.
     *                     <p>e.g. 156 or CNY -> ¥
     * @return {@link CurrencyBean}
     */
    public static CurrencyBean getCurrency(@NonNull String currencyCode){
        CurrencyBean currencyBean = match(RECENT,currencyCode);
        if (currencyBean == null){
            List<CurrencyBean> currencyBeans = getAllCurrencies();
            currencyBean =  match(currencyBeans,currencyCode);
            if (currencyBean != null){
                RECENT.add(currencyBean);
            }
        }
        return currencyBean;

    }

    private static CurrencyBean match(List<CurrencyBean> currencyBeans,@NonNull String currencyCode){
        for (CurrencyBean currencyBean : currencyBeans) {
            if (currencyCode.equals(currencyBean.getAlphaCode()) || currencyCode.equals(currencyBean.getNumericCode())){
                return currencyBean;
            }
        }
        return null;
    }

    public static List<CurrencyBean> getAllCurrencies(){
        List<CurrencyBean> currencies = new ArrayList<>();
        currencies.add(new CurrencyBean("008","ALL","L"));
        currencies.add(new CurrencyBean("032","ARS","$"));
        currencies.add(new CurrencyBean("036","AUD","$"));
        currencies.add(new CurrencyBean("044","BSD","$"));
        currencies.add(new CurrencyBean("050","BDT","৳"));
        currencies.add(new CurrencyBean("051","AMD","Դ"));
        currencies.add(new CurrencyBean("052","BBD","$"));
        currencies.add(new CurrencyBean("060","BMD","$"));
        currencies.add(new CurrencyBean("068","BOB","Bs"));
        currencies.add(new CurrencyBean("072","BWP","P"));
        currencies.add(new CurrencyBean("084","BZD","$"));
        currencies.add(new CurrencyBean("090","SBD","$"));
        currencies.add(new CurrencyBean("096","BND","$"));
        currencies.add(new CurrencyBean("104","MMK","K"));
        currencies.add(new CurrencyBean("108","BIF","₣"));
        currencies.add(new CurrencyBean("116","KHR","៛"));
        currencies.add(new CurrencyBean("124","CAD","$"));
        currencies.add(new CurrencyBean("132","CVE","$"));
        currencies.add(new CurrencyBean("136","KYD","$"));
        currencies.add(new CurrencyBean("144","LKR","Rs"));
        currencies.add(new CurrencyBean("152","CLP","$"));
        currencies.add(new CurrencyBean("156","CNY","¥"));
        currencies.add(new CurrencyBean("170","COP","$"));
        currencies.add(new CurrencyBean("188","CRC","₡"));
        currencies.add(new CurrencyBean("191","HRK","Kn"));
        currencies.add(new CurrencyBean("192","CUP","$"));
        currencies.add(new CurrencyBean("203","CZK","Kč"));
        currencies.add(new CurrencyBean("208","DKK","kr"));
        currencies.add(new CurrencyBean("214","DOP","$"));
        currencies.add(new CurrencyBean("232","ERN","Nfk"));
        currencies.add(new CurrencyBean("238","FKP","£"));
        currencies.add(new CurrencyBean("242","FJD","$"));
        currencies.add(new CurrencyBean("262","DJF","₣"));
        currencies.add(new CurrencyBean("270","GMD","D"));
        currencies.add(new CurrencyBean("292","GIP","£"));
        currencies.add(new CurrencyBean("320","GTQ","Q"));
        currencies.add(new CurrencyBean("324","GNF","₣"));
        currencies.add(new CurrencyBean("328","GYD","$"));
        currencies.add(new CurrencyBean("332","HTG","G"));
        currencies.add(new CurrencyBean("340","HNL","L"));
        currencies.add(new CurrencyBean("344","HKD","$"));
        currencies.add(new CurrencyBean("348","HUF","Ft"));
        currencies.add(new CurrencyBean("352","ISK","Kr"));
        currencies.add(new CurrencyBean("356","INR","₹"));
        currencies.add(new CurrencyBean("360","IDR","Rp"));
        currencies.add(new CurrencyBean("376","ILS","₪"));
        currencies.add(new CurrencyBean("388","JMD","$"));
        currencies.add(new CurrencyBean("392","JPY","¥"));
        currencies.add(new CurrencyBean("398","KZT","〒"));
        currencies.add(new CurrencyBean("404","KES","Sh"));
        currencies.add(new CurrencyBean("408","KPW","₩"));
        currencies.add(new CurrencyBean("410","KRW","₩"));
        currencies.add(new CurrencyBean("418","LAK","₭"));
        currencies.add(new CurrencyBean("426","LSL","L"));
        currencies.add(new CurrencyBean("430","LRD","$"));
        currencies.add(new CurrencyBean("446","MOP","P"));
        currencies.add(new CurrencyBean("454","MWK","MK"));
        currencies.add(new CurrencyBean("458","MYR","RM"));
        currencies.add(new CurrencyBean("478","MRO","UM"));
        currencies.add(new CurrencyBean("484","MXN","$"));
        currencies.add(new CurrencyBean("496","MNT","₮"));
        currencies.add(new CurrencyBean("498","MDL","L"));
        currencies.add(new CurrencyBean("516","NAD","$"));
        currencies.add(new CurrencyBean("533","AWG","ƒ"));
        currencies.add(new CurrencyBean("548","VUV","Vt"));
        currencies.add(new CurrencyBean("554","NZD","$"));
        currencies.add(new CurrencyBean("558","NIO","C$"));
        currencies.add(new CurrencyBean("566","NGN","₦"));
        currencies.add(new CurrencyBean("578","NOK","kr"));
        currencies.add(new CurrencyBean("598","PGK","K"));
        currencies.add(new CurrencyBean("600","PYG","₲"));
        currencies.add(new CurrencyBean("608","PHP","₱"));
        currencies.add(new CurrencyBean("643","RUB","р."));
        currencies.add(new CurrencyBean("646","RWF","₣"));
        currencies.add(new CurrencyBean("654","SHP","£"));
        currencies.add(new CurrencyBean("694","SLL","Le"));
        currencies.add(new CurrencyBean("682","SAR","SAR"));
        currencies.add(new CurrencyBean("702","SGD","$"));
        currencies.add(new CurrencyBean("704","VND","₫"));
        currencies.add(new CurrencyBean("706","SOS","Sh"));
        currencies.add(new CurrencyBean("710","ZAR","R"));
        currencies.add(new CurrencyBean("748","SZL","L"));
        currencies.add(new CurrencyBean("752","SEK","kr"));
        currencies.add(new CurrencyBean("756","CHF","₣"));
        currencies.add(new CurrencyBean("764","THB","฿"));
        currencies.add(new CurrencyBean("776","TOP","T$"));
        currencies.add(new CurrencyBean("780","TTD","$"));
        currencies.add(new CurrencyBean("800","UGX","Sh"));
        currencies.add(new CurrencyBean("807","MKD","ден"));
        currencies.add(new CurrencyBean("818","EGP","£"));
        currencies.add(new CurrencyBean("826","GBP","£"));
        currencies.add(new CurrencyBean("834","TZS","Sh"));
        currencies.add(new CurrencyBean("840","USD","$"));
        currencies.add(new CurrencyBean("858","UYU","$"));
        currencies.add(new CurrencyBean("882","WST","T"));
        currencies.add(new CurrencyBean("901","TWD","$"));
        currencies.add(new CurrencyBean("932","ZWL","$"));
        currencies.add(new CurrencyBean("934","TMT","m"));
        currencies.add(new CurrencyBean("936","GHS","₵"));
        currencies.add(new CurrencyBean("938","SDG","£"));
        currencies.add(new CurrencyBean("941","RSD","din"));
        currencies.add(new CurrencyBean("943","MZN","MTn"));
        currencies.add(new CurrencyBean("944","AZN","ман"));
        currencies.add(new CurrencyBean("946","RON","L"));
        currencies.add(new CurrencyBean("949","TRY","₤"));
        currencies.add(new CurrencyBean("950","XAF","₣"));
        currencies.add(new CurrencyBean("951","XCD","$"));
        currencies.add(new CurrencyBean("953","XPF","₣"));
        currencies.add(new CurrencyBean("967","ZMW","ZK"));
        currencies.add(new CurrencyBean("968","SRD","$"));
        currencies.add(new CurrencyBean("971","AFN","Af"));
        currencies.add(new CurrencyBean("972","TJS","ЅМ"));
        currencies.add(new CurrencyBean("973","AOA","Kz"));
        currencies.add(new CurrencyBean("974","BYR","Br"));
        currencies.add(new CurrencyBean("975","BGN","лв"));
        currencies.add(new CurrencyBean("976","CDF","₣"));
        currencies.add(new CurrencyBean("977","BAM","КМ"));
        currencies.add(new CurrencyBean("978","EUR","€"));
        currencies.add(new CurrencyBean("980","UAH","₴"));
        currencies.add(new CurrencyBean("985","PLN","zł"));
        currencies.add(new CurrencyBean("986","BRL","R$"));
        return currencies;
    }


    public static class CurrencyBean {
        /**
         * ISO 4217 numeric code of this currency.
         * <p>e.g. 156,840
         */
        private final String numericCode;
        /**
         * ISO 4217 alphabetic  code of this currency.
         * <p>e.g. CNY,USD
         */
        private final String alphaCode;
        /**
         * symbol of this currency.
         * <p>e.g. ¥,$
         */
        private final String symbol;

        public CurrencyBean(String numericCode, String alphaCode, String symbol) {
            this.numericCode = numericCode;
            this.alphaCode = alphaCode;
            this.symbol = symbol;
        }

        public String getNumericCode() {
            return numericCode;
        }

        public String getAlphaCode() {
            return alphaCode;
        }

        public String getSymbol() {
            return symbol;
        }

        @NonNull
        @Override
        public String toString() {
            return "CurrencyBean{" +
                    "numericCode='" + numericCode + '\'' +
                    ", currencyCode='" + alphaCode + '\'' +
                    ", symbol='" + symbol + '\'' +
                    '}';
        }
    }

} 
