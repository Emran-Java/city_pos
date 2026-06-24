package acquire.core.trans.impl.settle;

import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.settle.SettleBracFragment;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;

/**
 * Select the merchants to be settled.
 *
 * @author Janson
 * @date 2021/7/20 10:25
 */
public class SelectMerchantStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (pubBean.getSettleMerchants() != null && !pubBean.getSettleMerchants().isEmpty()) {
            callback.onResult(true);
            return;
        }
      /*  if (pubBean.isSettleAll()) {
            //settle all merchants

            pubBean.setSettleMerchants(new MerchantRepository().findDefaultMerchant());
            callback.onResult(true);
            return;
        }*/
//        mActivity.mSupportDelegate.switchContent(SettleFragment.newInstance(new FragmentCallback<List<Merchant>>() {
        mActivity.mSupportDelegate.switchContent(SettleBracFragment.newInstance(new FragmentCallback<List<Merchant>>() {
            @Override
            public void onSuccess(List<Merchant> merchants) {
                if (!existRecord(merchants)) {
                    ToastUtils.showToast(R.string.core_settle_no_record);
                    return;
                }
                pubBean.setSettleMerchants(merchants);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
                    case FragmentCallback.TIMEOUT:
                    case FragmentCallback.FAIL:
                    default:
                        pubBean.setResultCode(ResultCode.FL);
                        break;
                }
                pubBean.setMessage(errorMsg);
                callback.onResult(false);
            }
        }));
    }

    private boolean existRecord(List<Merchant> merchants) {
        RecordRepository recordRepository = new RecordRepository();
        for (Merchant merchant : merchants) {
            if (recordRepository.getCountByMidTid(merchant.getMid(), merchant.getTid()) > 0) {
                return true;
            }
        }
        return false;
    }
}
