package acquire.core.trans.pack.json;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.gson.Gson;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.network.HttpOkHelper;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetHttpCodeException;
import acquire.base.utils.network.exception.NetReceiveException;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.pack.BaseCaller;
import acquire.core.trans.pack.CallerException;

/**
 * A tool for JSON data request.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   RequestBean request = new RequestBean();
 *   ...
 *   try {
 *       ResponseBean response = new JsonCaller.Builder(mActivity)
 *           .withPrompts("Connecting...","Receiving...")
 *           .packComm(request,ResponseBean.class);
 *   } catch (CallerException e) {
 *       int callResult = e.getCallerResult();
 *       String message = e.getMessage();
 *   }
 * </pre>
 *
 * @author Janson
 * @date 2020/9/7 17:20
 */
public class JsonGetCaller extends BaseCaller {
    private BaseActivity activity;
    /**
     * Communication prompts
     */
    private String[] commPrompts;

    private HttpOkHelper httpOkHelper;

    private JsonGetCaller() {
    }

    /**
     * Stop communication
     */
    public void abort() {
        if (httpOkHelper != null) {
            httpOkHelper.abort();
        }
    }

    /**
     * Get request ,return a object
     *
     * @param responseClass response class type
     * @return a response object
     * @throws CallerException Communication Exception
     */
    public <T> T execute(String url, Class<T> responseClass, boolean needIncreaseTraceNo)throws CallerException {
        if (needIncreaseTraceNo) {
//        trace +1
            StatisticsUtils.increaseTraceNo();
        }
        //convert object to json
        Gson gson = new Gson();
        if (TextUtils.isEmpty(url)) {
            throw new CallerException(CallerResult.FAIL_REQUEST_DATA_ERROR,BaseApplication.getAppString(R.string.core_comm_pack_fail));
        }
        String response;
        try {
            //send data
            if (commPrompts != null && commPrompts.length > 0) {
                showProgress(activity, commPrompts[0]);
            }
            response = send(url);
        } catch (NetConnectException | NetHttpCodeException e) {
            //net error
            LoggerUtils.e("connect network failed!",e);
            if (e instanceof NetHttpCodeException) {
                throw new CallerException(CallerResult.FAIL_NET_CONNECT,e.getMessage());
            } else {
                throw new CallerException(CallerResult.FAIL_NET_CONNECT, BaseApplication.getAppString(R.string.core_comm_connect_fail));
            }
        } catch (NetReceiveException e) {
            //receive failed
            LoggerUtils.e("receive network failed!",e);
            throw new CallerException(CallerResult.FAIL_NET_RECV,BaseApplication.getAppString(R.string.core_comm_recv_fail));
        } finally {
            hideProgress();
        }
        try {
            //convert response to a object
            return gson.fromJson(response, responseClass);
        } catch (Exception e) {
            LoggerUtils.e("Response JSON to bean failed!",e);
            throw new CallerException(CallerResult.FAIL_REQUEST_DATA_ERROR,BaseApplication.getAppString(R.string.core_comm_unpack_response_format_error));

        }
    }



    /**
     * Send data
     */
    private String send(String url) throws NetReceiveException, NetConnectException, NetHttpCodeException {
        //ssl certificate
//        try (InputStream cerIs = BaseApplication.getAppContext().getAssets().open(FileConst.PEM_CERT)){
//            httpOkHelper = new HttpOkHelper(url, timeout, cerIs);
//        } catch (IOException e) {
//            throw new NetConnectException(BaseApplication.getAppString(R.string.core_comm_ssl_cert_not_found));
//        }
        //create a okhttp
        httpOkHelper = new HttpOkHelper(url, 60);
        //UI
        if (commPrompts != null && commPrompts.length > 1) {
            //send success,so update progress
            httpOkHelper.setProcessListener(() -> showProgress(activity, commPrompts[1]));
        }
        //set cancel button
        setProgressCancelListener(activity, ()-> ThreadPool.execute(httpOkHelper::abort));
        //get string
        String response = httpOkHelper.httpGetString();
        if (TextUtils.isEmpty(response)) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.core_comm_recv_fail));
        }
        return response;
    }

    /**
     * A {@link JsonGetCaller} builder
     *
     * @author Janson
     * @date 2020/6/3 9:37
     */
    public static class Builder {
        private final BaseActivity activity;
        private String[] commPrompts;

        public Builder(BaseActivity activity) {
            this.activity = activity;
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
         * Set 1 or 2 progress prompts.
         */
        public Builder withPrompts(@NonNull @StringRes int... resIds) {
            this.commPrompts = new String[resIds.length];
            for (int i = 0; i < resIds.length; i++) {
                this.commPrompts[i] = BaseApplication.getAppString(resIds[i]);
            }
            return this;
        }

        /**
         * Create a {@link JsonGetCaller}
         */
        public JsonGetCaller create() {
            JsonGetCaller caller = new JsonGetCaller();
            caller.activity = activity;
            caller.commPrompts = this.commPrompts;
            return caller;
        }

        /**
         * Create a {@link JsonGetCaller} and return a object.
         *
         * @param responseClass response class type
         * @return a response object
         */
        public <T> T packComm(String url, Class<T> responseClass, boolean needIncreaseTraceNo) throws CallerException {
            JsonGetCaller caller = create();
            return caller.execute(url, responseClass, needIncreaseTraceNo);
        }
    }
} 