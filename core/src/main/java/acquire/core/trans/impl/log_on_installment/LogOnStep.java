package acquire.core.trans.impl.log_on_installment;

import android.text.TextUtils;

import java.util.Date;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.database.model.Merchant;
import acquire.sdk.device.BDevice;

/**
 * The step that packs {@link LogOn} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/2/12 15:49
 */
class LogOnStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()){
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        initPubBean();

        LoggerUtils.d("LogOn: Start LogOn");
        pubBean.setMessageId("0800"); // Field 0 MTI
        pubBean.setProcessCode("920000"); // Field 3
       /* pubBean.setMessageId("0200");
        pubBean.setProcessCode("000000");
        pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean));*/
        //pack 8583
        iso8583.initPack();

        /*String ip = "";
        int port = 0;
        int timeout = 0;*/

        try {

            //for test print
            iso8583.setField(3, "920000");
            iso8583.setField(11, pubBean.getTraceNo());
            iso8583.setField(24, pubBean.getNii());
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid()); //Merchant ID (MID)
            //-------------------------------------

            LoggerUtils.d("LogOn: Before call LogOn ");
            //Call for response
            int result = new Caller.Builder(mActivity, pubBean, iso8583)
                    .checkResp(true)
                    .packComm();
            if (result != CallerResult.OK) {
                //failed
                ToastUtils.showToast(pubBean.getMessage());

                LoggerUtils.e("LogOn: LogOn call failed");
               // return;
            }

            LoggerUtils.d("LogOn: LogOn call Success");

            //get key from ISO field62 [e.g: 00166DCE535DCF6EFC0D404D5EC12A2963B3 (After length 4bit, 32Length encrypted TPK key )]
            String tpkKey = iso8583.getField(62);
            //test in live with fix TPK
            //String tpkKey = "A12F0E6CC116120D61ACE42758EE3D17";//Live
//            LoggerUtils.d("LogOn: tpkKey:" + tpkKey);
//            Log.d("LogOn:","LogOn: tpkKey:" + tpkKey);

            //String tpkKeyKcv = CriptoUtils.generateKCV(tpkKey);
            //LoggerUtils.d("LogOn: tpkKeyKcv:" + tpkKeyKcv);
            //Log.d("LogOn:","LogOn: tpkKeyKcv:" + tpkKeyKcv);

           /* String tpk = "";
            String pikCheck = null;
            if (tpkKey.length() == 32) { //4 is length
                tpkKey = tpkKey.substring(4);
                tpk = tpkKey.substring(4, tpkKey.length()-1);
                LoggerUtils.d("LogOn: TPK KEY:" + tpk);
                Log.d("LogOn:","LogOn: TPK KEY:" + tpk);
            }else{
                LoggerUtils.e("LogOn: LogOn call failed. field62: "+tpkKey);
                Log.d("LogOn:","LogOn: LogOn call failed. field62: "+tpkKey);
                return;
            }*/

            // send request for Acknowledgement
            LoggerUtils.e("LogOn: make data for Acknowledgement LogOn call");

            LoggerUtils.d("LogOn: Change message ID(MTI) 0800 to 0820");
            //Log.d("LogOn:","LogOn: Change message ID(MTI) 0800 to 0820");

            Merchant merchant = MultiMerchantUtils.getMerchant(pubBean);
            /*ip = merchant.getIp();
            port = merchant.getPort();
            timeout = merchant.getCommTimeout();*/

            int masterKeyIndex = merchant.getMasterKeyIndex();
            int algoType = merchant.getAlgorithm();
            LoggerUtils.d("LogOn: merchant.getMasterKeyIndex(): "+masterKeyIndex);
            LoggerUtils.d("LogOn: algoType: "+algoType);

            PinpadHelper pinpadHelper = new PinpadHelper(merchant.getMasterKeyIndex(),merchant.getAlgorithm());

            if (!TextUtils.isEmpty(tpkKey)) {
                //pin key
//                boolean pinResult = pinpadHelper.loadMkskWorkKey(WorkKeyType.PIN_KEY,tpkKey,BytesUtils.hexToBytes(tpkKey), BytesUtils.hexToBytes(tpkKeyKcv));
                boolean pinResult = pinpadHelper.loadBracMkskWorkKey(1,BytesUtils.hexToBytes(tpkKey));

                if (!pinResult) {
                    ToastUtils.showToast(R.string.core_login_load_pin_key_failed);
                    LoggerUtils.e("LogOn: : "+R.string.core_login_load_pin_key_failed);
                    //return ;//Host can't' provide tpkKey in Live mode. So for the development time we comment this case
                }
            }else{
                //<TODO>Host can't' provide tpkKey in Live mode. So for the development time we comment this case
            }

            LoggerUtils.d("LogOn: Success- masterKeyIndex: "+masterKeyIndex+", tpkKey: "+tpkKey);
            LoggerUtils.d("LogOn: Start second part request");

            //Update time
            String timeStr = iso8583.getField(12);
            String dateStr = iso8583.getField(13);
            if (!TextUtils.isEmpty(dateStr) && !TextUtils.isEmpty(timeStr)) {
                dateStr = DateUtils.formatTime(new Date(),DateUtils.YYYY) + dateStr;
                Date date = DateUtils.getDate(dateStr + timeStr,DateUtils.YYYYMMDDHHMMSS);
                BDevice.setSystemTime(date);
            }
//            pubBean.setMessage(R.string.core_login_success);
//            callback.onResult(true);

            initPubBean();

            pubBean.setMessageId("0820"); // Field 0 MTI
//            Log.d("LogOn:","LogOn: ProcessCode (Field03) 920000 to 920000");
            pubBean.setProcessCode("920000"); // Field 3

            iso8583.initPack();
            iso8583.setField(3, "920000"); // Field 3
            iso8583.setField(24, pubBean.getNii());
            iso8583.setField(39, "00");
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid()); //Merchant ID (MID)
            //Acknowledgement call
            int result2 = new Caller.Builder(mActivity, pubBean, iso8583)
                    .checkResp(true)
                    .packComm();//wait 3 sec

            if (result2 != CallerResult.OK) {
                //failed
                ToastUtils.showToast(pubBean.getMessage());
                LoggerUtils.e("LogOn: second part LogOn call failed");
                pubBean.setMessage(R.string.core_transaction_result_user_cancel);

                //callback.onResult(false);
                callback.onResult(true);//will be true
            }
            else{
                pubBean.setMessage(R.string.core_login_success);
                callback.onResult(true);
            }

        }catch (Exception e){
            LoggerUtils.e("LogOn: second part set 8583 field error!",e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
//            pubBean.setResultCode(ResultCode.FL);
            pubBean.setResultCode(ResultCode.OK);
            callback.onResult(true);
            return;
        }



        // send request for Acknowledgement
        /*if(ip.isEmpty() || port<=0 || timeout<=0){
            LoggerUtils.e("LogOn: LogOn can't able send Acknowledgement because of (ip/port/timeout) related issue");
            return;
        }*/

        //SocketHelper socketHelper = new SocketHelper(ip, port, timeout);

        //send to the server
        /*int result = new Caller.Builder(mActivity, pubBean, iso8583)
                .checkResp(false)
                .preSaveReversal(false)
                .packComm();
        callback.onResult(result == CallerResult.OK);*/
       /* pubBean.setMessage(R.string.core_login_success);

        callback.onResult(true);*/
    }

}
