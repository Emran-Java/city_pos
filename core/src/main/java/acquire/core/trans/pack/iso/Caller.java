package acquire.core.trans.pack.iso;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.base.utils.network.SocketHelper;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetHttpCodeException;
import acquire.base.utils.network.exception.NetReceiveException;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.tools.DataConverter;
import acquire.core.tools.EmvHelper;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.pack.BaseCaller;
import acquire.database.model.Merchant;
import acquire.database.model.ReversalData;
import acquire.database.repository.ReversalDataRepository;
import acquire.sdk.emv.constant.EntryMode;


/**
 * A tool for ISO 8583 data request.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   int result = new Caller.Builder(mActivity, pubBean, iso8583)
 *                 .checkResp(true)
 *                 .withPrompts("Connecting...","Receiving...")
 *                 .packComm();
 * </pre>
 *
 * @author Janson
 * @date 2019/4/25 15:24
 */
public class Caller extends BaseCaller {
    private AppCompatActivity activity;
    private PubBean pubBean;
    /**
     * Communication prompts
     */
    private String[] commPrompts;
    /**
     * 8583
     */
    private ISO8583 iso8583;
    /**
     * Check response code
     */
    private boolean checkResp;
    /**
     * Pre-save reversal
     */
    private boolean preSaveReversal;

    private Caller() {
    }

    /**
     * Pack and send 8583 data.
     *
     * @return result status.
     * @see CallerResult
     */
    public @CallerResult.CallerResultDef int execute() {
        LoggerUtils.d("Transaction Type [" + pubBean.getTransName() + "] caller.");
        Merchant merchant = MultiMerchantUtils.getMerchant(pubBean);
        //Request
        byte[] request;
        String bitmapField = iso8583.getBitmapValue();
        try {
            //pack request 8583
            request = Packet8583.pack8583(iso8583);

            // add request head and TPDU
            Log.d("NewCall","84 request: "+LoggerUtils.bytesToHex(request)+", bitmapField: "+bitmapField);

            if(pubBean.getTransType().equals(TransType.TRANS_LOG_ON) || pubBean.getTransType().equals(TransType.TRANS_INSTALLMENT_LOG_ON)) {
                String mti = pubBean.getMessageId();
                if(pubBean.getMessageId().equals("0820")){
                    merchant.setCommTimeout(5);
                }
                request = PacketHead.packHeadTpduBitmap(merchant.getTpdu(),mti, bitmapField, request);
            }
            else if(pubBean.getTransType().equals(TransType.TRANS_TEST_TRX)) {
                String mti = "0800";
                request = PacketHead.packHeadTpduBitmap(merchant.getTpdu(),mti, bitmapField, request);
            }else if(pubBean.getTransType().equals(TransType.TRANS_SALE)) {
                String mti = "0200";
                request = PacketHead.packHeadTpduBitmap(merchant.getTpdu(),mti, bitmapField, request);
            }else if(pubBean.getTransType().equals(TransType.TRANS_TIP_SALE)) {
                String mti = "0220";
                request = PacketHead.packHeadTpduBitmap(merchant.getTpdu(),mti, bitmapField, request);
            }
            /*else if(pubBean.getMessageId().equals("0320") && !pubBean.getProcessCode().equals("000000") && pubBean.getTransType().equals(TransType.TRANS_SETTLE)) {
                String mti = "0320";
                request = PacketHead.packHeadTpduBitmap(merchant.getTpdu(),mti, bitmapField, request);
            }*/
            else{
                request = PacketHead.packHeadTpdu(merchant.getTpdu(),request);
            }
            Log.d("NewCall","87 request: "+LoggerUtils.bytesToHex(request)+", bitmapField: "+bitmapField);
        } catch (Exception e) {
            LoggerUtils.e("pack 8583 failed!",e);

            Log.d("NewCall","91 request exception: "+e.getMessage()+", bitmapField: "+bitmapField);

            pubBean.setMessage(e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            return CallerResult.FAIL_REQUEST_DATA_ERROR;
        }
        //save reversal data
        boolean packReversal55 = false;
        ReversalDataRepository reversalDataRepository = new ReversalDataRepository();
        if (preSaveReversal) {
            ReversalData reversalData = new ReversalData();
            DataConverter.pubBeanToReversal(pubBean, reversalData);
            if (!TextUtils.isEmpty(iso8583.getField(55))&& (pubBean.getEntryMode() == EntryMode.INSERT || pubBean.getEntryMode() == EntryMode.TAP)) {
                EmvHelper emvHelper = new EmvHelper();
                reversalData.setField55(emvHelper.packReversalField55(true));
                packReversal55 = true;
            }
            if (!reversalDataRepository.add(reversalData)) {
                pubBean.setMessage(R.string.core_comm_add_reversal_error);
                pubBean.setResultCode(ResultCode.FL);
                return CallerResult.FAIL_REQUEST_DATA_ERROR;
            }
        }
        //trace+1
        StatisticsUtils.increaseTraceNo();
        byte[] response;
        try {
            //send data
            if (commPrompts != null && commPrompts.length > 0) {
                showProgress(activity, commPrompts[0]);
            }
            response = send(merchant,request);

            String sRespoonse = LoggerUtils.bytesToHex(response);
            Log.d("NewCall","response: "+sRespoonse);
        } catch (NetConnectException | NetHttpCodeException e) {
            LoggerUtils.e("connect network failed!",e);

            Log.d("NewCall","response: "+e.getMessage());
            //net error
            if (preSaveReversal) {
                reversalDataRepository.deleteAllReversalData();
            }
            if (e instanceof NetHttpCodeException) {
                pubBean.setMessage(e.getMessage());
            } else {
                pubBean.setMessage(R.string.core_comm_connect_fail);
            }
            pubBean.setResultCode(ResultCode.FL);
            return CallerResult.FAIL_NET_CONNECT;
        } catch (NetReceiveException e) {
             LoggerUtils.e("receive network failed!",e);
            //receive failed
            if (packReversal55) {
                //update reversal field55
                EmvHelper emvHelper = new EmvHelper();
                reversalDataRepository.updateField55(emvHelper.packReversalField55(false));
            }
            pubBean.setMessage(R.string.core_comm_recv_fail);
            pubBean.setResultCode(ResultCode.FL);
            return CallerResult.FAIL_NET_RECV;
        } finally {
            hideProgress();
            pubBean.setRequestOnlineSucc(true);
        }
        //Response
        try {
            boolean requestMacEixt = iso8583.getField(64) != null;
            //delete response head and TPDUU
            String stResponse = LoggerUtils.bytesToHex(response);
            Log.d("NewCall", "166 response: "+stResponse);
            response = PacketHead.unpackHeadTpdu(merchant.getTpdu(),response);
            String sResponse = LoggerUtils.bytesToHex(response);
            Log.d("NewCall", "169 response: "+sResponse);
            //unpack response to ISO8583
            Packet8583.unpack8583(iso8583, response);
            //parse response fields
            Packet8583.parseBracResponse(iso8583, requestMacEixt,pubBean);
        } catch (Exception e) {
            LoggerUtils.e("parse response data failed!",e);
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(e.getMessage());
            if (packReversal55) {
                //uppdate reversal field55
                EmvHelper emvHelper = new EmvHelper();
                reversalDataRepository.updateField55(emvHelper.packReversalField55(false));
            }
            return CallerResult.FAIL_RESPONSE_DATA_ERROR;
        }
        if (!checkResp) {
            return CallerResult.OK;
        } else {
            //check response code
            String responseCode = pubBean.getResultCode();
            LoggerUtils.d("Field39 response code: " + responseCode);
            if (ResultCode.OK.equals(responseCode)) {
                //success
                return CallerResult.OK;
            } else {
                //failed
                if (preSaveReversal) {
                    //delete reversal data
                    reversalDataRepository.deleteAllReversalData();
                }
                return CallerResult.FAIL_RESPONSE_DATA_ERROR;
            }
        }
    }


    /**
     * Send data
     *
     * @param request ISO8583 request data.
     * @return ISO8583 response data.
     * @throws NetReceiveException  Failed to receive data from server.
     * @throws NetConnectException  Failed to  connected to server.
     * @throws NetHttpCodeException An HTTP error occurred  while connecting to the server.
     */
    private byte[] send(Merchant merchant,byte[] request) throws NetReceiveException, NetConnectException, NetHttpCodeException {

        String sSendRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall","219 request: "+sSendRequest);

        String ip = merchant.getIp();
        int port = merchant.getPort();
        int timeout = merchant.getCommTimeout();

        //ssl certificate
//        SocketHelper socketHelper;
//        AssetManager assetManager = BaseApplication.getAppContext().getAssets();
//        try (InputStream cerIs = assetManager.open(FileConst.PEM_CERT)) {
//            socketHelper = new SocketHelper(ip, port, timeout, cerIs);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new NetConnectException(BaseApplication.getAppString(R.string.core_comm_ssl_cert_not_found));
//        }
        //SOCKET
        SocketHelper socketHelper = new SocketHelper(ip, port, timeout);
        //UI
        if (commPrompts != null && commPrompts.length > 1) {
            //send success,so update progress

            socketHelper.setProcessListener(
                    () -> showProgress(activity, commPrompts[1])
            );
        }
        //set cancel button
        setProgressCancelListener(activity, ()-> ThreadPool.execute(socketHelper::closeComm));
        //send 8583 data
        return socketHelper.commSendRecv(request);

        //If you use http/https, please restore the following code.
        //HTTP
//        String url = "https://" + ip + ":" + port;
//        HttpOkHelper httpOkHelper;
//        //create a ok http
//        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_COMM_USE_SSL, false)) {
//            //ssl certificate
//            try (InputStream cerIs = BaseApplication.getAppContext().getAssets().open(FileConst.PEM_CERT)){
//                httpOkHelper = new HttpOkHelper(url, timeout, cerIs);
//            } catch (IOException e) {
//                throw new NetConnectException(BaseApplication.getAppString(R.string.core_comm_ssl_cert_not_found));
//            }
//        }else{
//            httpOkHelper = new HttpOkHelper(url, timeout);
//        }
//        //UI
//        if (commPrompts != null && commPrompts.length > 1) {
//            //send success,so update progress
//            httpOkHelper.setProcessListener(() -> showProgress(activity, commPrompts[1]));        }
//       
//        //set cancel button
//        setProgressCancelListener(activity,httpOkHelper::abort);
//        //send data
//        return httpOkHelper.httpPostByte(request);
    }


    /**
     * A {@link Caller} builder
     *
     * @author Janson
     * @date 2020/6/3 9:37
     */
    public static class Builder {
        private final AppCompatActivity activity;
        private final PubBean pubBean;
        private final ISO8583 iso8583;
        private String[] commPrompts;
        private boolean checkResp;
        private boolean preSaveReversal;

        public Builder(AppCompatActivity activity, PubBean pubBean, ISO8583 iso8583) {
            this.activity = activity;
            this.pubBean = pubBean;
            this.iso8583 = iso8583;
            if (activity != null){
                this.commPrompts = new String[]{BaseApplication.getAppString(R.string.core_comm_connecting),
                        BaseApplication.getAppString(R.string.core_comm_recving)};
            }
        }

        /**
         * No ui
         */
        public Builder withoutPrompts() {
            this.commPrompts = null;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts.
         */
        public Builder withPrompts(String... prompts) {
            this.commPrompts = prompts;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts
         */
        public Builder withPrompts(@NonNull @StringRes int... resIds) {
            this.commPrompts = new String[resIds.length];
            for (int i = 0; i < resIds.length; i++) {
                this.commPrompts[i] = BaseApplication.getAppString(resIds[i]);
            }
            return this;
        }

        /**
         * Check response code
         */
        public Builder checkResp(boolean checkResp) {
            this.checkResp = checkResp;
            return this;
        }

        /**
         * Pee-save current reversal data
         */
        public Builder preSaveReversal(boolean preSaveReversal) {
            this.preSaveReversal = preSaveReversal;
            return this;
        }

        /**
         * Create a {@link Caller}
         */
        public Caller create() {
            Caller caller = new Caller();
            caller.activity = this.activity;
            caller.pubBean = this.pubBean;
            caller.iso8583 = this.iso8583;
            caller.commPrompts = this.commPrompts;
            caller.checkResp = this.checkResp;
            caller.preSaveReversal = this.preSaveReversal;
            return caller;
        }

        /**
         * Create a {@link Caller} and pack and send data.
         */
        public @CallerResult.CallerResultDef int packComm() {
            Caller caller = create();
            return caller.execute();
        }
    }


}
