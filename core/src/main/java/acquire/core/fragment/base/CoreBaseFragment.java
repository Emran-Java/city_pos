package acquire.core.fragment.base;

import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.w3c.dom.Text;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.core.TransActivity;
import acquire.core.constant.TransTag;
import acquire.core.fragment.key_board.CoreNumberPadBottomSheet;

abstract public class CoreBaseFragment extends BaseFragment {

    public void showPinSubmitSheet(String transType, TextView tvAmount, boolean isCancelFragment, FragmentCallback<Long> mCallback) {
        boolean isNumberShuffle = false;
        CoreNumberPadBottomSheet bottomSheetFragment =
                CoreNumberPadBottomSheet.Companion.newInstance(new CoreNumberPadBottomSheet.ItemClickListener() {
                    @Override
                    public void onCancelButtonClick() {
                        if (isCancelFragment) {
                            mActivity.finish();
                        }

                    }

                    @Override
                    public void onBottomSheetItemClick(@Nullable String amountValue, boolean isTakeAction) {

                        if (amountValue != null && amountValue.isEmpty()) amountValue = "0.00";
                        tvAmount.setText(amountValue);
                        if (isTakeAction) {
                            Intent intent = new Intent(mActivity, TransActivity.class);
                            intent.putExtra(TransTag.TRANS_TYPE, transType);
                            intent.putExtra(TransTag.AMOUNT, amountValue);
                            ActivityCompat.startActivity(mActivity, intent, null);
                            tvAmount.setText("0.00");
                        }
                    }
                }, isNumberShuffle, true, false);

        bottomSheetFragment.setCancelable(false);
        bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
    }


}
