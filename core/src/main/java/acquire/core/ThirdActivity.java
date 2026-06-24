package acquire.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.bean.PubBean;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransTag;
import acquire.core.tools.DataConverter;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.system.BSystem;

/**
 * Receive a call from a third party app.
 *
 * <p><hr><b>Third party application call {@link ThirdActivity}</b></p>
 * <p>URI mode:</p>
 * <pre>
 *      Intent intent = new Intent();
 *      //see core/src/main/AndroidManifest.xml, ThirdActivity ->node 'data'
 *      intent.setData(Uri.parse("newland://acquire/transaction?transType=Sale"));
 *      startActivity(intent);
 * </pre>
 * <p>Bundle mode:</p>
 * <pre>
 *      Intent intent = new Intent();
 *      intent.putExtra("transType","Sale");
 *      intent.putExtra("amount",1);
 *      intent.putExtra("outOrderNo","20210425001");
 *      intent.setAction("android.intent.action.NEWLAND.PAYMENT");
 *      //payment app package
 *      intent.setPackage("com.newland.template");
 *      startActivityForResult(intent,101);
 * </pre>
 *
 * <p><hr><b>{@link ThirdActivity} receive:</b></p>
 * <p>URI mode:</p>
 * <pre>
 *       Uri uri = getIntent().getData();
 *       String type = uri.getQueryParameter("transType");//Sale
 * </pre>
 * <p>Bundle mode:</p>
 * <pre>
 *       String type = getIntent().getStringExtra("transType");//Sale
 * </pre>
 *
 * @author Janson
 * @date 2021/9/27 17:37
 */
public class ThirdActivity extends BaseActivity {
    /**
     * return code for third party app
     */
    final static int THIRD_OK = 2700, THIRD_FAIL = 2701, THIRD_CANCEL = 2702;

    private boolean compatScap = false;

    @Override
    public int attachFragmentResId() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new View(this));
        //catch back event
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle bundle = intent.getExtras();
        if (uri != null) {
            LoggerUtils.i("ThirdCall>> Start. Uri: " + uri);
            compatScap = intent.hasExtra("scapVersion");
            if (!compatScap) {
                compatScap = uri.getQueryParameter("scapVersion") != null;
            }
        } else {
            if (bundle != null) {
                //for un-parcel
                bundle.getString("");
            }
            LoggerUtils.i("ThirdCall>> Start. Bundle: " + bundle);
        }

        //Check whether to invoke repeatedly
        if (isRepeatInvoke()) {
            //Failed
            LoggerUtils.e("Repeatedly invoke ThirdActivity.");
            ToastUtils.showToast(R.string.core_third_result_invoke_repeatedly);
            if (compatScap) {
                intent.putExtra(TransTag.RESULT_CODE, THIRD_FAIL);
            } else {
                intent.putExtra(TransTag.RESULT_CODE, ResultCode.FL);
            }
            intent.putExtra(TransTag.MESSAGE, getString(R.string.core_third_result_invoke_repeatedly));
            LoggerUtils.e("ThirdCall>> Finish. setResult failed bundle: " + intent.getExtras().toString());
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }
        addCount();
        //register a launcher to start transaction
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            removeCount();
            //handle the transaction result.
            Intent data = result.getData();
            int resultCode = result.getResultCode();
            if (data != null && data.getExtras() != null) {
                String acqResultCode = data.getStringExtra(TransTag.RESULT_CODE);

               /*
               tested: is cant work
               data.putExtra("cardNo", "1234567890987654");
                data.putExtra("issuerName", "BRAC BANK PLC");
                data.putExtra("batchNumber", "0099");
                data.putExtra("merchantId", "777777");*/

                if (compatScap) {
                    if (acqResultCode != null) {
                        switch (acqResultCode) {
                            case ResultCode.OK:
                                data.putExtra(TransTag.RESULT_CODE, THIRD_OK);
                                break;
                            case ResultCode.UC:
                                data.putExtra(TransTag.RESULT_CODE, THIRD_CANCEL);
                                break;
                            case ResultCode.FL:
                            default:
                                data.putExtra(TransTag.RESULT_CODE, THIRD_FAIL);
                                break;
                        }
                    } else {
                        data.putExtra(TransTag.RESULT_CODE, THIRD_FAIL);
                    }
                }
                if (resultCode == Activity.RESULT_OK) {
                    LoggerUtils.i("ThirdCall>> Finish. setResult success bundle: " + data.getExtras().toString());
                } else {
                    LoggerUtils.e("ThirdCall>> Finish. setResult failed bundle: " + data.getExtras().toString());
                }
            }else{
                LoggerUtils.e("ThirdCall>> Finish. setResult failed: No result data.");
            }
            setResult(resultCode, data);
            finish();
        });

        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            SelfCheckHelper.initDevice(this);
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            Intent transIntent = new Intent(this, TransActivity.class);
            if (uri != null) {
                //uri ->transIntent
                DataConverter.uriToIntent(uri, transIntent, PubBean.class);
            } else {
                //bundle ->transIntent
                if (bundle != null) {
                    transIntent.putExtras(bundle);
                }
            }
            transIntent.setAction(TransActivity.THIRD_ACTION);
            launcher.launch(transIntent);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_stay);
    }

    @Override
    protected void onDestroy() {
        removeCount();
        if (ServiceHelper.getInstance().isInit()) {
            //restore task and home button
            BSystem.setTaskButton(true);
            BSystem.setHomeButton(true);
        }
        super.onDestroy();
    }

    private final static Set<Integer> THIRD_COUNT = Collections.synchronizedSet(new HashSet<>());

    private boolean isRepeatInvoke() {
        return !THIRD_COUNT.isEmpty();
    }

    private void addCount() {
        if (THIRD_COUNT.add(hashCode())) {
            LoggerUtils.d("ThirdActivity count +1");
        }
    }

    private void removeCount() {
        if (THIRD_COUNT.remove(hashCode())) {
            LoggerUtils.d("ThirdActivity count -1");
        }
    }

}
