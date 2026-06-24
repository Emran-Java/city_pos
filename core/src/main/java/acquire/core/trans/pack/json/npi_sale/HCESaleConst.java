package acquire.core.trans.pack.json.npi_sale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import acquire.sdk.device.BDevice;

public class HCESaleConst {
    private static final String HCE_HOST = "http://47.84.195.8:7445";
    public static final String HCE_VIEW_BASE_URL = HCE_HOST+"/hce_view/#/pay?" ;

    public static final String HCE_ORDER_CREATE = HCE_HOST+"/hce/order/create/";

    public static final String HCE_ORDER_QUERY = HCE_HOST+"/hce/order/query/";



    public static String generateThirdNo(){
        String sn = BDevice.getSn();
        if (sn == null || sn.length() < 6) {
            sn = "UD0000";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        return sn.substring(0, 2) + sn.substring(sn.length() - 4) + dateFormat.format(new Date());
    }


}
