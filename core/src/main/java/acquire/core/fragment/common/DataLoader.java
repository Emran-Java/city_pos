package acquire.core.fragment.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;

import acquire.core.R;

public class DataLoader {

    private static DataLoader instance;
    private Dialog dialog;

    private DataLoader() {
    }

    public static synchronized DataLoader getInstance() {
        if (instance == null) {
            instance = new DataLoader();
        }
        return instance;
    }

    public void show(Context context, String title, String message) {

        if (context == null) return;

        try {

            if (dialog != null && dialog.isShowing()) {
                dismiss();
            }

            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            LayoutInflater inflater = LayoutInflater.from(context);
            android.view.View view = inflater.inflate(R.layout.core_dialog_loader, null);

            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvMessage = view.findViewById(R.id.tvMessage);

            tvTitle.setText(title != null ? title : "Loading");
            tvMessage.setText(message != null ? message : "Please wait...");

            dialog.setContentView(view);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );
            }

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
