package acquire.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import acquire.base.R;
import acquire.base.databinding.BaseEditTextIpBinding;
import acquire.base.widget.keyboard.BaseKeyboard;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;


/**
 * A IPv4 edit text
 *
 * @author Janson
 * @date 2020/1/6 10:35
 */
public class IpEditText extends FrameLayout {
    /**
     * Current {@link EditText} index
     */
    private int mIndexCur;

    private BaseKeyboard mKeyboardNumber;
    /**
     * focus listener
     */
    private OnFocusChangeListener onFocusChangeListener;
    /**
     * IP edit texts
     */
    private EditText[] mEts;

    private final static int PER_MAX_LEN = 3;
    private final static int PER_MAX_VALUE = 255;
    private final static int SECTION_NUM = 4;

    public IpEditText(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public IpEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public IpEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public IpEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        acquire.base.databinding.BaseEditTextIpBinding binding = BaseEditTextIpBinding.inflate(LayoutInflater.from(context), this, true);
        mEts = new EditText[]{binding.etIp1, binding.etIp2, binding.etIp3, binding.etIp4};
        for (EditText et : mEts) {
            initEvent(et);
        }

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IpEditText);
            String ip = typedArray.getString(R.styleable.IpEditText_ip);
            typedArray.recycle();
            setIp(ip);
        }
    }

    public void setKeyboardNumber(BaseKeyboard keyboardNumber) {
        this.mKeyboardNumber = keyboardNumber;
        for (final EditText et : mEts) {
            et.setOnClickListener(v -> mKeyboardNumber.setVisibility(View.VISIBLE));
        }
    }

    private void initEvent(EditText et) {
        et.setOnFocusChangeListener(new IpFocusChangeListener());
        et.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            String oldText = dest.toString();
            String newText = oldText + source;
            if (TextUtils.isEmpty(newText)) {
                return "";
            }
            try {
                int ip = Integer.parseInt(newText);
                if (ip > PER_MAX_VALUE) {
                    focusNext();
                    return "";
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }});
        et.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_GO:
                case EditorInfo.IME_ACTION_SEARCH:
                case EditorInfo.IME_ACTION_SEND:
                case EditorInfo.IME_ACTION_NEXT:
                case EditorInfo.IME_ACTION_DONE:
                    focusNext();
                    break;
                default:
                    break;
            }
            return false;
        });
    }


    private boolean focusNext() {
        if (mIndexCur < PER_MAX_LEN) {
            EditText et = mEts[mIndexCur + 1];
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setTextKeepState(et.getText());
            et.setSelection(et.length());
            return true;
        }
        return false;
    }


    private void focusBack() {
        if (mIndexCur > 0) {
            EditText et = mEts[mIndexCur - 1];
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setSelection(et.length());
        }
    }


    public String getIp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mEts.length; i++) {
            String ipChild = mEts[i].getText().toString();
            if (TextUtils.isEmpty(ipChild)) {
                sb.append("");
            } else {
                sb.append(ipChild);
            }
            if (i != mEts.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }


    /**
     * Set ip. e.g. 192.168.1.1
     */
    public void setIp(String ip) {
        if (ip != null) {
            String[] ips = ip.split("\\.");
            if (ips.length == SECTION_NUM) {
                for (int i = 0; i < mEts.length; i++) {
                    mEts[i].setText(ips[i]);
                }
            }
        }
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        this.onFocusChangeListener = listener;
    }


    public void setFocus() {
        for (EditText et : mEts) {
            if ("".equals(et.getText().toString().trim()) || et.getId() == R.id.et_ip_4) {
                et.setFocusable(true);
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                et.setTextKeepState(et.getText().toString());
                break;
            }
        }
    }

    private EditKeyboardListener ipListener;

    private class IpFocusChangeListener implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (onFocusChangeListener != null) {
                if (hasFocus) {
                    onFocusChangeListener.onFocusChange(v, hasFocus);
                } else {
                    boolean focus = false;
                    for (EditText et : mEts) {
                        focus |= et.isFocused();
                    }
                    if (!focus) {
                        onFocusChangeListener.onFocusChange(v, false);
                    }
                }
            }
            if (hasFocus) {
                if (v.getId() == R.id.et_ip_1) {
                    mIndexCur = 0;
                } else if (v.getId() == R.id.et_ip_2) {
                    mIndexCur = 1;
                } else if (v.getId() == R.id.et_ip_3) {
                    mIndexCur = 2;
                } else if (v.getId() == R.id.et_ip_4) {
                    mIndexCur = 3;
                }
                if (mKeyboardNumber != null) {
                    if (ipListener == null) {
                        ipListener = new IpKeyboardListener((OnBackPressedDispatcherOwner) getContext(),(EditText) v, PER_MAX_LEN);
                    }
                    if (mKeyboardNumber.getKeyBoardListener() != ipListener) {
                        mKeyboardNumber.setKeyBoardListener(ipListener);
                    }
                    ipListener.setTargetView((EditText) v);
                }
            }


        }
    }

    private class IpKeyboardListener extends EditKeyboardListener {
        IpKeyboardListener(OnBackPressedDispatcherOwner dispatcherOwner, EditText editText, Integer maxLength) {
            super(dispatcherOwner, editText, maxLength);
        }

        @Override
        public void onText(int code) {
            super.onText(code);
            if (code == BaseKeyboard.K_POINT) {
                onEnter();
                return;
            }
            String text = mEditText.getText().toString();
            if (!text.isEmpty()) {
                int ip = 0;
                try {
                    ip = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (text.length() >= PER_MAX_LEN || ip > PER_MAX_VALUE) {
                    focusNext();
                }
            }
        }

        @Override
        public void onBackspace() {
            if (mEditText.getSelectionStart() == 0) {
                focusBack();
            } else {
                super.onBackspace();
            }
        }

        @Override
        public void onClear() {
            for (EditText et : mEts) {
                et.setText(null);
            }
            mEts[0].requestFocus();
        }

        @Override
        public void onEnter() {
            if (!focusNext()) {
                mKeyboardNumber.setVisibility(View.GONE);
            } else {
                mKeyboardNumber.setVisibility(View.VISIBLE);
            }
        }
    }

}
