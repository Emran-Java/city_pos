package acquire.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseActivity;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.core.bean.PubBean;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ReportConstant;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreActivityTransBinding;
import acquire.core.display2.BasePresentation;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.tools.DataConverter;
import acquire.core.tools.SoundPlayer;
import acquire.core.tools.TransUtils;
import acquire.core.trans.AbstractTrans;
import acquire.core.fragment.common.report.AllDetailsReportFragment;
import acquire.core.fragment.common.submenu.CoreFeatureSubMenuListFragment;
import acquire.core.fragment.common.submenu.CoreSubMenuAdapter;

/**
 * A Transaction {@link Activity}.
 * <p><hr><b>start {@link TransActivity} to execute a transaction</b></p>
 * <p>e.g.</p>
 * <pre>
 *     Intent intent = new Intent();
 *     intent.putExtra({@link TransTag#TRANS_TYPE}, {@link TransType#TRANS_SALE});
 *     ActivityCompat.startActivity(mActivity, intent, null);
 * </pre>
 * <p>For more parameters, please see {@link TransTag}</p>
 *
 * @author Janson
 * @date 2020/2/12 13:58
 */
public class TransActivity extends BaseActivity implements CoreSubMenuAdapter.OnItemClickListener {

    private String mTransactionType;
    public final static String THIRD_ACTION = "android.intent.action.third";
    public final static String KEY_TASK = "TASK";

    @Override
    public int attachFragmentResId() {
        return R.id.fragment_trans_layout;
    }

    public String formatAmount(long amount) {
        BigDecimal bd = BigDecimal.valueOf(amount, 2);
        return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CoreActivityTransBinding binding = CoreActivityTransBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //catch back event
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        if (isThirdCall()) {
            //This is to avoid the blank status bar when third app calls this app
            getSupportFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
                private boolean first;

                @Override
                public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
                    if (!first) {
                        first = true;
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TransActivity.this, R.color.base_background));
                    }
                }
            });
        }
        //set immersed status bar.
        DisplayUtils.immersedStatusBar(getWindow());
        transact();
    }

    private void transact() {
        //intent to pubBean
        Intent intent = getIntent();
        //unparcel intent bundle
        String sExtra = intent.getStringExtra("");
        LoggerUtils.d("TransActivity=> sering extra: " + sExtra);
        if (intent.getExtras() != null) {
            String tmpVal = intent.getExtras().toString();
            LoggerUtils.d("TransActivity=> Transaction params: " + tmpVal);
        }
        PubBean pubBean = new PubBean();
        //intent to pubBean
        DataConverter.intentToPubBean(intent, pubBean);
        pubBean.setThirdCall(isThirdCall());

        //trans instance
        AbstractTrans trans;
        try {
            Class<? extends AbstractTrans> clz = TransUtils.getTrans(pubBean.getTransType());
            if (clz == null) {
                LoggerUtils.e("Transaction class is null.");
                intent.putExtra(TransTag.MESSAGE, getString(R.string.core_transaction_result_no_such_trans));
                intent.putExtra(TransTag.RESULT_CODE, ResultCode.FL);
                setResult(RESULT_CANCELED, intent);
                finish();
                return;
            }
            LoggerUtils.d("Instantiate transaction: " + clz);
            trans = clz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            LoggerUtils.e("Instantiate transaction failed!", e);
            intent.putExtra(TransTag.MESSAGE, getString(R.string.core_transaction_result_no_such_trans));
            intent.putExtra(TransTag.RESULT_CODE, ResultCode.FL);
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }

        mTransactionType = pubBean.getTransType();
        String transName = TransUtils.getName(pubBean.getTransType());
        long pubAmount = pubBean.getAmount();
        LoggerUtils.i("TransActivity=> Execute transaction: " + transName);
        LoggerUtils.i("TransActivity=> Execute transaction pubAmount: " + pubAmount);
        setTitle(transName);
        pubBean.setTransName(transName);

//        String amount = intent.getStringExtra(TransTag.TOTAL_AMOUNT);
//        if(amount==null || amount.isEmpty())

        String amount = "0.00";
        if (pubAmount > 0)
            amount = formatAmount(pubAmount);
        else
            amount = intent.getStringExtra(TransTag.AMOUNT);

        if (amount == null || amount.isEmpty()) amount = "0.00";

        amount = amount.replace(".", "");

        try {
            long amountLong = Long.parseLong(amount);
            pubBean.setAmount(amountLong);

        } catch (NumberFormatException ex) {
            LoggerUtils.i("Exception: " + ex);
        }

        String tmpamount = pubBean.getAmount() + "";
        Log.d("TestDev", "" + tmpamount);

        //trans init
        trans.init(this, pubBean);
        //transaction execute
        trans.transact((success) -> {
            //output transaction result
            DataConverter.pubBeanToIntent(pubBean, intent);
            if (success) {
                LoggerUtils.i(transName + "--success.");
                setResult(RESULT_OK, intent);
            } else {
                LoggerUtils.e(transName + "--failed[" + pubBean.getResultCode() + "]: " + pubBean.getMessage());
                setResult(RESULT_CANCELED, intent);
            }
            finish();
        });
    }

    @Override
    public void finish() {
        super.finish();
        if (ActivityStackManager.countActivity(getClass()) == 1) {
            //last transact activity
            SensitiveGuard.clean();
        }
        SoundPlayer.getInstance().stop();
        BasePresentation.removeAllPresentations(this);
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_stay);
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(KEY_TASK)) {
            CallbackBinder callback = (CallbackBinder) intent.getExtras().getBinder(KEY_TASK);
            if (callback != null) {
                callback.onResult(intent);
            }
        }
    }


    public boolean isThirdCall() {
        return THIRD_ACTION.equals(getIntent().getAction());
    }

    //for report feature
    private String mExtraToolbarTitle = "";

    @Override
    public void onItemClick(FeatureSubMenuModel model) {

        if (model == null) {
            //Toast.makeText(this, "Clicked Item null", Toast.LENGTH_SHORT).show();
            LoggerUtils.i("NewCall, Clicked Model NULL");
            return;
        }

        // Toast.makeText(this, "Clicked Item: "+model.getTitle(), Toast.LENGTH_SHORT).show();
        LoggerUtils.i("NewCall, Clicked Model: " + model.toString());

        Bundle bundle = new Bundle();
        String toolbarTitle = model.getTitle();
        if (toolbarTitle == null || toolbarTitle.isEmpty()) {
            toolbarTitle = mExtraToolbarTitle;
        }
        bundle.putString(IntentParamKeyContent.TRANS_TITLE_TEXT, toolbarTitle);
        bundle.putSerializable(IntentParamKeyContent.TRANS_REPORT_MODEL_KEY, model);

        if (model.isHasChild() && model.getChildData() != null
                && model.getChildData().size() > 0) {

            CoreFeatureSubMenuListFragment fragment =
                    CoreFeatureSubMenuListFragment.newInstance(model.getTitle(), model.getChildData(), null);

            fragment.setArguments(bundle);

           /* getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();*/
            mSupportDelegate.switchBracContent(fragment, 0);

        } else if (mTransactionType.equalsIgnoreCase(TransType.TRANS_PRE_AUTH_MENU)) {

            if (model.getCode().equalsIgnoreCase("PRE_AUTH")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_PRE_AUTH);
                startActivity(intent, null);
            } else if (model.getCode().equalsIgnoreCase("PRE_AUTH_VOID")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_VOID_PRE_AUTH);
                startActivity(intent, null);
                //ToastUtils.showLongToast("Call PRE_AUTH_VOID");
            } else if (model.getCode().equalsIgnoreCase("SALE_COMP")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_AUTH_COMPLETE);
                startActivity(intent, null);
            } else if (model.getCode().equalsIgnoreCase("PRE_AUTH_RPT")) {
                goForDetailsReport(model, bundle);
            }
            else if (model.getCode().equalsIgnoreCase("PRE_AUTH_VOID_RPT")) {
                goForDetailsReport(model, bundle);
            }

        } else if (mTransactionType.equalsIgnoreCase(TransType.TRANS_REPORTS_PRINT)) {
            if (model.getCode().equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_DETAILS_REPORT)
                    //|| model.getCode().equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_BATCH_TOTALS)
                    || model.getCode().equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_VOID_REPORT)
                    || model.getCode().equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_CARD_REPORT)

            ) { //Details Report
                goForDetailsReport(model, bundle);
            }
        }
        else if (mTransactionType.equalsIgnoreCase(TransType.TRANS_INSTALLMENT_MENU)) {
            if (model.getCode().equalsIgnoreCase("PAY_FLEX")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_INSTALLMENT);
                startActivity(intent, null);
            }
            else if (model.getCode().equalsIgnoreCase("PAY_FLEX_VOID")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_VOID_INSTALLMENT);
                startActivity(intent, null);
            }
            else if (model.getCode().equalsIgnoreCase("PAY_FLEX_RPT")) {
                goForDetailsReport(model, bundle);
            }
            else if (model.getCode().equalsIgnoreCase("PAY_FLEX_LOG_ON")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_INSTALLMENT_LOG_ON);
                startActivity(intent, null);
            }
        }
        else if (mTransactionType.equalsIgnoreCase(TransType.TRANS_REPRINT_RECEIPT_MENU)) {
            if (model.getCode().equalsIgnoreCase("REPRINT_LAST_RECEIPT")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_REPRINT_LAST_RECEIPT);
                startActivity(intent, null);
            }
            else if (model.getCode().equalsIgnoreCase("REPRINT_ANY_RECEIPT")) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_REPRINT_RECEIPT);
                startActivity(intent, null);
            }
        }
        else {

          /*  ReportDetailsFragment fragment =
                    ReportDetailsFragment.newInstance(model, new SimpleCallback() {
                        @Override
                        public void result() {
//                            pubBean.setResultCode(ResultCode.OK);
//                            pubBean.setMessage(R.string.core_transaction_result_success);
//                            listener.onTransResult(true);
                        }
                    });

            fragment.setArguments(bundle);

            *//*getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();*//*
            mSupportDelegate.switchBracContent(fragment,0);*/
        }
    }

    private void goForDetailsReport(FeatureSubMenuModel model, Bundle bundle) {
        AllDetailsReportFragment fragment =
                AllDetailsReportFragment.newInstance(model);

        // Toast.makeText(this, "Ready for open details report", Toast.LENGTH_SHORT).show();
        LoggerUtils.i("NewCall, Ready for open details report: " + model.toString());

        fragment.setArguments(bundle);

           /* getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();*/
        mSupportDelegate.switchBracContent(fragment, 0);
    }
}
