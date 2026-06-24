package acquire.core.fragment.bottom_sheet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import acquire.core.R;

public class QRShowBottomSheet extends BottomSheetDialogFragment {

    private static ButtonClickListener mButtonClickListener;
    public static final String  BUTTON_QR_CODE_PRINT= "BTN_PRINT";
    public static final String  BUTTON_QR_CODE_CANCEL= "BTN_CANCEL";
    private  Bitmap qrBitmap;
    private  String mTopText, mBottomText;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogStyle;
    }

    //getTheme()//: Int = R.style.BottomSheetDialogStyle

    private QRShowBottomSheet(){}

    private static QRShowBottomSheet QRShowBottomSheet;

    public static QRShowBottomSheet getInstance(){
        if(QRShowBottomSheet == null)
            QRShowBottomSheet = new QRShowBottomSheet();
        return QRShowBottomSheet;
    }

    public void setDataListener(Bitmap bitmap, String topText, String bottomText, ButtonClickListener buttonClickListener){
        mButtonClickListener  = buttonClickListener;
        qrBitmap = bitmap;
        mTopText = topText;
        mBottomText = bottomText;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.qr_show_bottom_sheet_layout, container, false);
        initialView(view);
        return view;
    }

    private void initialView(View view) {

        setDataOnUi(view);

        Button btnPrint = view.findViewById(R.id.btnPrint);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnPrint.setOnClickListener(v -> {
            //Toast.makeText(getContext(), "Button Clicked", Toast.LENGTH_SHORT).show();
            mButtonClickListener.onButtonClick(BUTTON_QR_CODE_PRINT);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            //Toast.makeText(getContext(), "Button Clicked", Toast.LENGTH_SHORT).show();
            mButtonClickListener.onButtonClick(BUTTON_QR_CODE_CANCEL);
            dismiss();
        });

    }

    private void setDataOnUi(View view) {
        ImageView imgQR = view.findViewById(R.id.imgQR);
        if(qrBitmap!=null)
            imgQR.setImageBitmap(qrBitmap);

        TextView tvAmount = view.findViewById(R.id.tvAmount);
        TextView tvSuccess = view.findViewById(R.id.tvSuccess);

        if(mTopText==null) mTopText="";
        tvAmount.setText(mTopText);

        if(mBottomText==null) mBottomText="";
        tvSuccess.setText(mBottomText);

    }

    public interface ButtonClickListener{
        void onButtonClick(String buttonCode);
    }
}