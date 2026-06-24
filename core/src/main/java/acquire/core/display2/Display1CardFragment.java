package acquire.core.display2;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
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
import java.util.Locale;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.dialog.image.ImageDialog;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.constant.ParamsConst;
import acquire.core.databinding.CoreFragmentDisplay1CardBinding;
import acquire.core.databinding.CorePresentationCardBinding;
import acquire.core.fragment.card.CardFragmentArgs;
import acquire.core.fragment.card.CardFragmentCallback;
import acquire.core.fragment.card.CardViewModel;
import acquire.core.tools.SoundPlayer;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.constant.EntryMode;


/**
 * A display1 reading card {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1CardFragment extends BaseDialogFragment {
    private CoreFragmentDisplay1CardBinding fragmentBinding;
    private CardFragmentCallback callback;
    private CardFragmentArgs cardFragmentArgs;
    private CardPresentation presentation;
    private Dialog removeCardDialog;
    private DialogPresentation removePresentation;
    private CardViewModel viewModel;
    @NonNull
    public static Display1CardFragment newInstance(CardFragmentArgs cardFragmentArgs, CardFragmentCallback callback) {
        Display1CardFragment fragment = new Display1CardFragment();
        fragment.cardFragmentArgs = cardFragmentArgs;
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentBinding = CoreFragmentDisplay1CardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);
        //manual
        fragmentBinding.manual.setOnClickListener(v -> {
            viewModel.gotoManual();
        });
        //show amount
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        if (pubBean.getAmount() != 0L) {
            String amount = CurrencyUtils.getCurrencySymbol(pubBean.getCurrencyCode())
                    + FormatUtils.formatAmount(pubBean.getAmount());
            fragmentBinding.tvAmount.setText(amount);
        }
        //cancel card
        fragmentBinding.btnExit.setOnClickListener(v -> onBack());
        int supportEntry = cardFragmentArgs.getSupportEntry();
        if ((supportEntry & EntryMode.MANUAL) != 0) {
            fragmentBinding.manual.setVisibility(View.VISIBLE);
        } else {
            fragmentBinding.manual.setVisibility(View.GONE);
        }
        if (cardFragmentArgs.isAccessibilityPin()){
            fragmentBinding.cbAccessPin.setVisibility(View.VISIBLE);
            fragmentBinding.cbAccessPin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            fragmentBinding.cbAccessPin.setVisibility(View.GONE);
        }
        presentation = new CardPresentation(mActivity);
        presentation.show();
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        //exit
        if (presentation.dialogPresentation != null && presentation.dialogPresentation.isShowing()) {
            if (presentation.dialogPresentation.getCancelButton() != null) {
                presentation.dialogPresentation.getCancelButton().callOnClick();
            }
        } else {
            viewModel.cancel();
        }
        return true;
    }

    void showDisplay1Message(String prompt) {
        fragmentBinding.tvPrompt.setText(prompt);
    }

    void setDisplay1Manual(boolean enable) {
        fragmentBinding.manual.setEnabled(enable);
    }


    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        if (removeCardDialog != null) {
            removeCardDialog.dismiss();
            removeCardDialog = null;
        }
        if (removePresentation != null) {
            removePresentation.dismiss();
            removePresentation = null;
        }
        SoundPlayer.getInstance().stop();
    }


    private class CardPresentation extends BasePresentation {
        private final CardReadingPresentation cardReadingPresentation = new CardReadingPresentation(mActivity);
        private CorePresentationCardBinding presentationBinding;
        private DialogPresentation dialogPresentation;

        public CardPresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationCardBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            //show amount
            PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
            if (pubBean.getAmount() != 0L) {
                String amount = CurrencyUtils.getCurrencySymbol(pubBean.getCurrencyCode()) + FormatUtils.formatAmount(pubBean.getAmount());
                presentationBinding.tvAmount.setText(amount);
            } else {
                presentationBinding.tvAmount.setVisibility(View.INVISIBLE);
            }
            //show supported card entries
            int supportEntry = cardFragmentArgs.getSupportEntry();
            showEntryAnimation(supportEntry);
            //read card
            viewModel.init(cardFragmentArgs,callback);
            observeViewModel();
            viewModel.startReadCard();
        }

        private void observeViewModel(){
            int supportEntry = cardFragmentArgs.getSupportEntry();
            viewModel.getCardStatus().observe(getViewLifecycleOwner(), cardStatus -> {
                if (cardStatus instanceof CardViewModel.CardReadyStatus) {
                    //ready
                    CardViewModel.CardReadyStatus readyStatus = (CardViewModel.CardReadyStatus) cardStatus;
                    if ((supportEntry & EntryMode.MANUAL) != 0) {
                        setDisplay1Manual(true);
                    }

                    cardReadingPresentation.hide();
                    if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.FALLBACK) {
                        if (readyStatus.emvReadyBean.getSupportEntries() == EntryMode.MAG) {
                            presentationBinding.tvError.setText(R.string.core_card_fallback_to_mag_prompt);
                            showDisplay1Message(getString(R.string.core_card_fallback_to_mag_prompt));
                        } else {
                            presentationBinding.tvError.setText(R.string.core_card_fallback_tp_insert_mag_prompt);
                            showDisplay1Message(getString(R.string.core_card_fallback_tp_insert_mag_prompt));
                        }
                        ViewUtils.shakeAnimatie(presentationBinding.tvError);
                    } else if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.USE_CHIP) {
                        presentationBinding.tvError.setText(R.string.core_card_icc_prompt);
                        showDisplay1Message(getString(R.string.core_card_icc_prompt));
                        ViewUtils.shakeAnimatie(presentationBinding.tvError);
                    } else if (readyStatus.emvReadyBean.getStatus() == EmvReadyBean.AGAIN) {
                        presentationBinding.tvError.setText(R.string.core_card_try_again);
                        showDisplay1Message(getString(R.string.core_card_try_again));
                        ViewUtils.shakeAnimatie(presentationBinding.tvError);
                    }
                    if (cancelAnimation || supportEntry != readyStatus.emvReadyBean.getSupportEntries()) {
                        showEntryAnimation(readyStatus.emvReadyBean.getSupportEntries());
                    }
                } else if (cardStatus instanceof CardViewModel.CardReadingStatus) {
                    //reading
                    presentationBinding.tvError.setText(null);
                    cardReadingPresentation.show();
                    setDisplay1Manual(false);
                    showDisplay1Message(getString(R.string.core_card_reading_hint));
                    SoundPlayer.getInstance().stop();
                }else if (cardStatus instanceof CardViewModel.CardSelectAidStatus) {
                    //select aid
                    CardViewModel.CardSelectAidStatus selectAidStatus = (CardViewModel.CardSelectAidStatus) cardStatus;
                    showDisplay1Message(getString(R.string.core_presentation_waiting_user_aids));
                    new MenuPresentation.Builder(mActivity)
                            .setItems(selectAidStatus.preferNames)
                            .setTitle(R.string.core_emv_aids_dialog_title)
                            .setConfirmButton(selectAidStatus.integerResponser::finish)
                            .setCancelButton(dialog -> selectAidStatus.integerResponser.finish(-1))
                            .show();
                } else if (cardStatus instanceof CardViewModel.CardSeePhoneStatus) {
                    //see phone
                    CardViewModel.CardSeePhoneStatus seePhoneStatus = (CardViewModel.CardSeePhoneStatus) cardStatus;
                    showDisplay1Message(getString(R.string.core_card_see_phone));
                    dialogPresentation = new DialogPresentation.Builder(mActivity)
                            .setMessage(R.string.core_card_see_phone)
                            .setConfirmButton((dialog, which) -> seePhoneStatus.booleanResponser.finish(true))
                            .setCancelButton(R.string.base_exit, (dialog, which) -> seePhoneStatus.booleanResponser.finish(false))
                            .show();
                }else if (cardStatus instanceof CardViewModel.CardNumberStatus) {
                    //card number
                    CardViewModel.CardNumberStatus cardNumberStatus = (CardViewModel.CardNumberStatus) cardStatus;
                    cancelAnimation();
                    cardReadingPresentation.hide();
                    setDisplay1Manual(false);
                    String maskPan = FormatUtils.maskCardNo(cardNumberStatus.cardNumber);
                    showDisplay1Message(maskPan);
                    dialogPresentation = new DialogPresentation.Builder(mActivity)
                            .setMessage(maskPan)
                            .setConfirmButton((dialog, which) -> cardNumberStatus.booleanResponser.finish(true))
                            .setCancelButton(R.string.base_exit, (dialog, which) -> cardNumberStatus.booleanResponser.finish(false))
                            .show();
                }else if (cardStatus instanceof CardViewModel.CardPinStatus) {
                    //PIN
                    cancelAnimation();
                    cardReadingPresentation.hide();
                    setDisplay1Manual(false);
                }else if (cardStatus instanceof CardViewModel.CardFinishStatus) {
                    //finish
                    cardReadingPresentation.hide();
                    cancelAnimation();
                }
            });
            viewModel.getRemoveCard().observe(getViewLifecycleOwner(), type -> {
                if (type >0){
                    int image = type == 1 ? R.drawable.core_remove_insert_card_warn_x800 : R.drawable.core_remove_tap_card_warn_x800;
                    removeCardDialog = new ImageDialog.Builder(mActivity)
                            .setImage(image)
                            .setMessage(R.string.core_remove_card)
                            .show();
                    removePresentation = new DialogPresentation.Builder(mActivity)
                            .setImage(image)
                            .setMessage(R.string.core_remove_card)
                            .show();
                }else{
                    if (removeCardDialog != null) {
                        removeCardDialog.dismiss();
                        removeCardDialog = null;
                    }
                    if (removePresentation != null) {
                        removePresentation.dismiss();
                        removePresentation = null;
                    }
                }
            });
            viewModel.getToastText().observe(getViewLifecycleOwner(), text -> {
                ToastUtils.showToast(text);
                presentationBinding.tvError.setText(text);
            });
        }
        private boolean cancelAnimation;

        private void cancelAnimation() {
            cancelAnimation = true;
            presentationBinding.lottieCard.cancelAnimation();
        }


        /**
         * show the entry animations
         */
        private void showEntryAnimation(int entryMode) {
            boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
            if (external) {
                List<Integer> animations = new ArrayList<>();
                if ((entryMode & EntryMode.MAG) != 0) {
                    animations.add(R.raw.lottie_ext_card_mag);
                }
                if ((entryMode & EntryMode.INSERT) != 0) {
                    animations.add(R.raw.lottie_ext_card_insert);
                }
                if ((entryMode & EntryMode.TAP) != 0) {
                    animations.add(R.raw.lottie_ext_card_tap);
                }
                if (animations.isEmpty()) {
                    return;
                }
                cancelAnimation = false;
                presentationBinding.lottieCard.setRepeatCount(0);
                presentationBinding.lottieCard.removeAllAnimatorListeners();
                presentationBinding.lottieCard.setAnimation(animations.get(0));
                presentationBinding.tvEntryMode.setText(R.string.core_card_on_external_pinpad);
                presentationBinding.lottieCard.playAnimation();
                presentationBinding.lottieCard.addAnimatorListener(new Animator.AnimatorListener() {
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
                        presentationBinding.lottieCard.setAnimation(animations.get(index));
                        presentationBinding.lottieCard.playAnimation();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {

                    }
                });
                return;
            }
            entryMode &= EntryMode.TAP | EntryMode.INSERT | EntryMode.MAG;
            switch (entryMode) {
                case EntryMode.TAP | EntryMode.INSERT | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_insert), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_insert_mag);
                    break;
                case EntryMode.TAP | EntryMode.INSERT:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_insert)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_insert);
                    break;
                case EntryMode.TAP | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_mag);
                    break;
                case EntryMode.INSERT | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_insert), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_insert_mag);
                    break;
                case EntryMode.TAP:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_tap);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap);
                    break;
                case EntryMode.INSERT:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_insert);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_insert);
                    break;
                case EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_mag);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_mag);
                    break;
                default:
                    break;
            }
            presentationBinding.lottieCard.playAnimation();
        }
    }
}
