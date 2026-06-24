package acquire.app.brac.ui.home;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;

public class CustomMediaController extends MediaController {

    private Context context;

    public CustomMediaController(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        this.setVisibility(View.GONE);

        // next button
        View next = findViewById(
                context.getResources().getIdentifier(
                        "next", "id", "android"));
        if (next != null) next.setVisibility(View.GONE);

        // previous button
        View prev = findViewById(
                context.getResources().getIdentifier(
                        "prev", "id", "android"));
        if (prev != null) prev.setVisibility(View.GONE);
    }

    @Override
    public void show(int timeout) {
        super.show(0); // always show
    }

    @Override
    public void show() {
        super.show();
    }
}
