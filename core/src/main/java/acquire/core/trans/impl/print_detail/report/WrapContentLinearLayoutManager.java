package acquire.core.trans.impl.print_detail.report;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WrapContentLinearLayoutManager  extends LinearLayoutManager {


    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        //super.onMeasure(recycler, state, widthSpec, heightSpec);
        int itemCount = getItemCount();

        if (itemCount == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        int height = 0;

        for (int i = 0; i < itemCount; i++) {
            try {
                View view = recycler.getViewForPosition(i);
                measureChild(view, widthSpec, heightSpec);
                height += view.getMeasuredHeight();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
    }
}
