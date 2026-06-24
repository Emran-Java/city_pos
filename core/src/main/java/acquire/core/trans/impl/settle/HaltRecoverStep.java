package acquire.core.trans.impl.settle;

import java.util.ArrayList;
import java.util.List;

import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;

/**
 * The {@link BaseStep} that recover to last settlement process
 *
 * @author Janson
 * @date 2019/10/10 9:12
 */
class HaltRecoverStep extends BaseStep {
    @Override
    public void intercept(Callback callback)  {
        List<Merchant> merchants = new MerchantRepository().findAll();
        List<Merchant> haltMerchants = new ArrayList<>();

        for (Merchant merchant : merchants) {
            int merHalt = merchant.getSettleStep();
            if (merHalt != 0){
                haltMerchants.add(merchant);
            }
        }
        if (haltMerchants.size()>0){
            pubBean.setSettleMerchants(haltMerchants);
            mActivity.runOnUiThread(()->{
                new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.core_settle_continue_last_halt)
                        .setCancelButton(dialog -> {
                            pubBean.setResultCode(ResultCode.UC);
                            pubBean.setMessage(R.string.core_settle_cancel);
                            callback.onResult(false);
                        })
                        .setConfirmButton(dialog -> callback.onResult(true))
                        .show();
            });
        }else{
            callback.onResult(true);
        }

    }
}
