package acquire.base.activity.bottom_sheet;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import acquire.base.R;

public class PrefKetEditorBottomSheet extends BottomSheetDialogFragment {

    private static final String KEY_MESSAGE = "message";
    private static final String KEY_IMAGE = "image";

    private static final String KEY_SHOW_LEFT = "show_left";
    private static final String KEY_SHOW_RIGHT = "show_right";

    private static final String KEY_LEFT_TEXT = "left_text";
    private static final String KEY_RIGHT_TEXT = "right_text";

   /* @Override
    public int getTheme() {
//        return super.getTheme();
        return R.style.BaseBottomSheetDialogStyle;
    }*/

    //    override fun getTheme(): Int = R.style.BottomSheetDialogStyle
    private BottomSheetActionListener listener;

    public static PrefKetEditorBottomSheet newInstance(
            String message,
            @DrawableRes int imageRes,
            boolean showLeftButton,
            boolean showRightButton,
            String leftButtonText,
            String rightButtonText
    ) {

        PrefKetEditorBottomSheet sheet = new PrefKetEditorBottomSheet();

        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putInt(KEY_IMAGE, imageRes);

        args.putBoolean(KEY_SHOW_LEFT, showLeftButton);
        args.putBoolean(KEY_SHOW_RIGHT, showRightButton);

        args.putString(KEY_LEFT_TEXT, leftButtonText);
        args.putString(KEY_RIGHT_TEXT, rightButtonText);

        sheet.setArguments(args);

        return sheet;
    }

    public void setActionListener(BottomSheetActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.base_bottom_sheet_message,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        Button btnLeft = view.findViewById(R.id.btnLeft);
        Button btnRight = view.findViewById(R.id.btnRight);

        Bundle args = getArguments();

        if (args == null) {
            dismiss();
            return;
        }

        String message = args.getString(KEY_MESSAGE, "");
        int imageRes = args.getInt(KEY_IMAGE, 0);

        boolean showLeft = args.getBoolean(KEY_SHOW_LEFT);
        boolean showRight = args.getBoolean(KEY_SHOW_RIGHT);

        String leftText = args.getString(KEY_LEFT_TEXT, "Cancel");
        String rightText = args.getString(KEY_RIGHT_TEXT, "OK");

        tvMessage.setText(message);

        // Image
        if (imageRes != 0) {
            ivIcon.setVisibility(View.VISIBLE);
            ivIcon.setImageResource(imageRes);
        } else {
            ivIcon.setVisibility(View.GONE);
        }

        // Left Button
        if (showLeft) {
            btnLeft.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(leftText)) {
                btnLeft.setText(leftText);
            }

            btnLeft.setOnClickListener(v -> {
                dismiss();

                if (listener != null) {
                    listener.onLeftButtonClick();
                }
            });
        }

        // Right Button
        if (showRight) {
            btnRight.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(rightText)) {
                btnRight.setText(rightText);
            }

            btnRight.setOnClickListener(v -> {
                dismiss();

                if (listener != null) {
                    listener.onRightButtonClick();
                }
            });
        }
    }

    public interface BottomSheetActionListener {
        void onLeftButtonClick();
        void onRightButtonClick();
    }
}