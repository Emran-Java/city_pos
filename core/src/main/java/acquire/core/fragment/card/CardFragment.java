package acquire.core.fragment.card;

import android.animation.Animator;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.dialog.image.ImageDialog;
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.constant.ParamsConst;
import acquire.core.databinding.CoreFragmentCardBinding;
import acquire.core.tools.SoundPlayer;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.constant.EntryMode;


/**
 * A reading card {@link Fragment}
 *
 * @author Janson
 * @date 2020/9/25 9:20
 */
public class CardFragment extends BaseFragment {
    private CoreFragmentCardBinding binding;
    private CardFragmentCallback callback;
    private CardFragmentArgs cardFragmentArgs;
    private MessageDialog exitDialog;
    private CardReadingDialog cardReadingDialog;
    private boolean exitEnable = true;
    private CardViewModel viewModel;

    private Dialog removeCardDialog;
    @NonNull
    public static CardFragment newInstance(CardFragmentArgs cardFragmentArgs, CardFragmentCallback callback) {
        CardFragment fragment = new CardFragment();
        fragment.cardFragmentArgs = cardFragmentArgs;
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentCardBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardReadingDialog = new CardReadingDialog(mActivity);
        //manual
        binding.manual.setOnClickListener(v -> viewModel.gotoManual());
        //show amount
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        if (pubBean.getAmount() != 0L) {
            String amount = CurrencyUtils.getCurrencySymbol(pubBean.getCurrencyCode())
                    + FormatUtils.formatAmount(pubBean.getAmount());
            binding.tvAmount.setText(amount);
        } else {
            binding.tvAmount.setVisibility(View.INVISIBLE);
            binding.tvAmountTag.setVisibility(View.INVISIBLE);
        }
        //show supported card entries
        int supportEntry = cardFragmentArgs.getSupportEntry();
        showEntryAnimation(supportEntry);
        if ((supportEntry & EntryMode.MANUAL) != 0) {
            binding.manual.setVisibility(View.VISIBLE);
        } else {
            binding.manual.setVisibility(View.GONE);
        }
        if (binding.tvAmount.getVisibility() != View.VISIBLE && binding.manual.getVisibility() != View.VISIBLE) {
            binding.rlTop.setVisibility(View.GONE);
        }
        if (cardFragmentArgs.isAccessibilityPin()){
            binding.cbAccessPin.setVisibility(View.VISIBLE);
            binding.cbAccessPin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                private boolean played;
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        if (!played){
                            played = true;
                            SoundPlayer.getInstance().playAmount(pubBean.getAmount());
                        }
                    }else {
                        SoundPlayer.getInstance().stop();
                        played = false;
                    }
                    cardFragmentArgs.getStepBean().getPubBean().setAccessPin(isChecked);
                }
            });
        }else{
            binding.cbAccessPin.setVisibility(View.GONE);
        }
        //read card
        viewModel.init(cardFragmentArgs,callback);
        observeViewModel();
        viewModel.startReadCard();
        return binding.getRoot();
    }

    private void observeViewModel(){
        int supportEntry = cardFragmentArgs.getSupportEntry();
        viewModel.getCardStatus().observe(getViewLifecycleOwner(), cardStatus -> {
            if (cardStatus instanceof CardViewModel.CardReadyStatus) {
                exitEnable = true;
                //ready
                CardViewModel.CardReadyStatus readyStatus = (CardViewModel.CardReadyStatus) cardStatus;
                if ((supportEntry & EntryMode.MANUAL) != 0) {
                    binding.manual.setEnabled(true);
                }
                cardReadingDialog.dismiss();
                if (cancelAnimation || supportEntry != readyStatus.emvReadyBean.getSupportEntries()) {
                    showEntryAnimation(readyStatus.emvReadyBean.getSupportEntries());
                }
                if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.FALLBACK) {
                    if (readyStatus.emvReadyBean.getSupportEntries() == EntryMode.MAG){
                        binding.tvContent.setText(R.string.core_card_fallback_to_mag_prompt);
                    }else{
                        binding.tvContent.setText(R.string.core_card_fallback_tp_insert_mag_prompt);
                    }
                    ViewUtils.shakeAnimatie(binding.tvContent);
                } else if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.USE_CHIP) {
                    binding.tvContent.setText(R.string.core_card_icc_prompt);
                    ViewUtils.shakeAnimatie(binding.tvContent);
                }else if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.AGAIN) {
                    binding.tvContent.setText(R.string.core_card_try_again);
                    ViewUtils.shakeAnimatie(binding.tvContent);
                }
            } else if (cardStatus instanceof CardViewModel.CardReadingStatus) {
                //reading
                SoundPlayer.getInstance().stop();
                exitEnable = false;
                dismissExitDialog();
                cardReadingDialog.show();
                binding.manual.setEnabled(false);
            }else if (cardStatus instanceof CardViewModel.CardSelectAidStatus) {
                //select aid
                CardViewModel.CardSelectAidStatus selectAidStatus = (CardViewModel.CardSelectAidStatus) cardStatus;
                cardReadingDialog.dismiss();
                dismissExitDialog();
                new MenuDialog.Builder(mActivity)
                        .setItems(selectAidStatus.preferNames)
                        .setTitle(R.string.core_emv_aids_dialog_title)
                        .setConfirmButton(selectAidStatus.integerResponser::finish)
                        .setCancelButton(dialog -> selectAidStatus.integerResponser.finish(-1))
                        .show();
            } else if (cardStatus instanceof CardViewModel.CardSeePhoneStatus) {
                //see phone
                CardViewModel.CardSeePhoneStatus seePhoneStatus = (CardViewModel.CardSeePhoneStatus) cardStatus;
                cardReadingDialog.dismiss();
                dismissExitDialog();
                new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.core_card_see_phone)
                        .setConfirmButton(dialog -> seePhoneStatus.booleanResponser.finish(true))
                        .setCancelButton(dialog -> seePhoneStatus.booleanResponser.finish(false))
                        .show();
            } else if (cardStatus instanceof CardViewModel.CardNumberStatus) {
                //card number
                exitEnable = false;
                CardViewModel.CardNumberStatus cardNumberStatus = (CardViewModel.CardNumberStatus) cardStatus;
                cancelAnimation();
                cardReadingDialog.dismiss();
                String maskPan = FormatUtils.maskCardNo(cardNumberStatus.cardNumber);
                new MessageDialog.Builder(mActivity)
                        .setMessage(maskPan)
                        .setConfirmButton(dialog -> cardNumberStatus.booleanResponser.finish(true))
                        .setCancelButton(dialog -> cardNumberStatus.booleanResponser.finish(false))
                        .show();
            }else if (cardStatus instanceof CardViewModel.CardPinStatus) {
                //PIN
                exitEnable = false;
                cancelAnimation();
                cardReadingDialog.dismiss();
            }else if (cardStatus instanceof CardViewModel.CardFinishStatus) {
                //Before finish
                cancelAnimation();
                cardReadingDialog.dismiss();
            }
        });
        viewModel.getRemoveCard().observe(getViewLifecycleOwner(), type -> {
            if (type >0){
                int image = type == 1?R.drawable.core_remove_insert_card_warn:R.drawable.core_remove_tap_card_warn;
                removeCardDialog = new ImageDialog.Builder(mActivity)
                        .setImage(image)
                        .setMessage(R.string.core_remove_card)
                        .show();
            }else{
                if (removeCardDialog != null) {
                    removeCardDialog.dismiss();
                    removeCardDialog = null;
                }
            }
        });
        viewModel.getToastText().observe(getViewLifecycleOwner(), ToastUtils::showToast);
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        if (exitEnable) {
            //exit prompt dialog
            exitDialog = new MessageDialog.Builder(mActivity)
                    .setMessage(R.string.core_card_cancel_dialog_message)
                    .setConfirmButton(dialog -> viewModel.cancel())
                    .setCancelButton(dialog -> {})
                    .show();
        }
        return true;
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        dismissExitDialog();
        if (removeCardDialog != null){
            removeCardDialog.dismiss();
        }
        if (cardReadingDialog != null){
            cardReadingDialog.dismiss();
        }
        cancelAnimation();
    }

    /**
     * dismiss exit dialog
     */
    private void dismissExitDialog() {
        mActivity.runOnUiThread(() -> {
            if (exitDialog != null && exitDialog.isShowing()) {
                exitDialog.dismiss();
            }
        });
    }
    private boolean cancelAnimation;

    /**
     * close card animations
     */
    private void cancelAnimation(){
        cancelAnimation = true;
    }
    /**
     * show the entry animations
     */
    private void showEntryAnimation(int entryMode) {
        List<Integer> animations = new ArrayList<>();
        List<Integer> names = new ArrayList<>();
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
        if ((entryMode & EntryMode.MAG) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_mag);
            }else{
                animations.add(R.raw.lottie_card_mag);
            }
            names.add(R.string.core_card_mag);
        }
        if ((entryMode & EntryMode.INSERT) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_insert);
            }else{
                animations.add(R.raw.lottie_card_insert);
            }
            names.add(R.string.core_card_insert);
        }
        if ((entryMode & EntryMode.TAP) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_tap);
            }else{
                animations.add(R.raw.lottie_card_tap);
            }
            names.add(R.string.core_card_tap);
        }
        if (animations.isEmpty()) {
            return;
        }
        cancelAnimation = false;
        binding.lottieAnimation.removeAllAnimatorListeners();
        binding.lottieAnimation.setAnimation(animations.get(0));
        binding.tvName.setText(names.get(0));
        binding.lottieAnimation.playAnimation();
        binding.lottieAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            int index = 0;

            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (cancelAnimation) {
                    return;
                }
                index++;
                if (index >= animations.size()) {
                    index = 0;
                }
                binding.lottieAnimation.setAnimation(animations.get(index));
                binding.lottieAnimation.playAnimation();
                binding.tvName.setText(names.get(index));
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }
}
