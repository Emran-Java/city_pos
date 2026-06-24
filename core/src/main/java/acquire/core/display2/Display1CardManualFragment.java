package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentDisplay1CardManualBinding;
import acquire.core.databinding.CorePresentationCardManualBinding;
import acquire.core.fragment.card.CardManualViewModel;


/**
 * A manual card {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1CardManualFragment extends BaseDialogFragment {
    private FragmentCallback<String[]> callback;
    @NonNull
    public static Display1CardManualFragment newInstance(FragmentCallback<String[]> callback) {
        Display1CardManualFragment fragment = new Display1CardManualFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentDisplay1CardManualBinding fragmentBinding = CoreFragmentDisplay1CardManualBinding.inflate(inflater, container, false);
        fragmentBinding.btnExit.setOnClickListener(v -> callback.onFail(FragmentCallback.CANCEL, getString(R.string.core_transaction_result_user_cancel)));
        CardManualPresentation presentation = new CardManualPresentation(mActivity);
        presentation.show();
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public FragmentCallback<String[]> getCallback() {
        return callback;
    }

    private class CardManualPresentation extends BasePresentation {
        private CardManualViewModel viewModel;

        public CardManualPresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CorePresentationCardManualBinding presentationBinding = CorePresentationCardManualBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            viewModel = new ViewModelProvider(Display1CardManualFragment.this).get(CardManualViewModel.class);

            //add text watcher to format card No.
            presentationBinding.etCardNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    viewModel.formatCardNumber(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            //Card NO keyboard
            EditKeyboardListener cardNoKeyboardListener = new EditKeyboardListener(mActivity,presentationBinding.etCardNo) {
                @Override
                public void onEnter() {
                    if (presentationBinding.etExpdate.getText().length() == 0
                            && presentationBinding.etCardNo.getText().length() != 0
                            && presentationBinding.etCardNo.isFocused()) {
                        ViewUtils.setFocus(presentationBinding.etExpdate);
                        return;
                    }
                    viewModel.checkResult(presentationBinding.etCardNo.getText().toString(),
                            presentationBinding.etExpdate.getText().toString());
                }

                @Override
                public void onCancel() {
                    callback.onFail(FragmentCallback.CANCEL, getString(R.string.core_transaction_result_user_cancel));
                }
            };
            presentationBinding.keyboardNumber.setKeyBoardListener(cardNoKeyboardListener);
            presentationBinding.etCardNo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    presentationBinding.keyboardNumber.setKeyBoardListener(cardNoKeyboardListener);
                }
            });
            //add text watcher to format expire date.
            presentationBinding.etExpdate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    viewModel.formatExpDate(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
            //Expire Date keyboard
            EditKeyboardListener expdateKeyboardListener = new EditKeyboardListener(mActivity,presentationBinding.etExpdate){
                @Override
                public void onEnter() {
                    viewModel.checkResult(presentationBinding.etCardNo.getText().toString(),presentationBinding.etExpdate.getText().toString());
                }
                @Override
                public void onCancel() {
                    callback.onFail(FragmentCallback.CANCEL, getString(R.string.core_transaction_result_user_cancel));
                }
            };
            presentationBinding.etExpdate.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    presentationBinding.keyboardNumber.setKeyBoardListener(expdateKeyboardListener);
                }
            });
            //card NO
            viewModel.getCardNo().observe(getViewLifecycleOwner(), cardNo -> {
                presentationBinding.etCardNo.setText(cardNo);
                presentationBinding.etCardNo.setSelection(cardNo.length());

            });
            viewModel.getCardNoError().observe(getViewLifecycleOwner(), error -> {
                presentationBinding.tilCardNo.setError(error);
                if (error != null) {
                    ViewUtils.setFocus(presentationBinding.etCardNo);
                }
            });
            //Expire date
            viewModel.getExpDate().observe(getViewLifecycleOwner(), expDate -> {
                presentationBinding.etExpdate.setText(expDate);
                presentationBinding.etExpdate.setSelection(expDate.length());
            });
            viewModel.getExpDateError().observe(getViewLifecycleOwner(), error -> {
                presentationBinding.tilExpdate.setError(error);
                if (error != null) {
                    ViewUtils.setFocus(presentationBinding.etExpdate);
                }
            });
            //result
            viewModel.getResult().observe(getViewLifecycleOwner(), cardAndExpdate -> {
                callback.onSuccess(cardAndExpdate);
            });
        }
    }
}
