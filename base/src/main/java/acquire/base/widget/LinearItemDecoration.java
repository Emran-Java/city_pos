package acquire.base.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * RecyclerView linear item spacing.
 * <p><hr><b>e.g.:</b></p>
 * <pre>
 *     RecyclerView.addItemDecoration(new LinearItemDecoration(2));
 *     //or
 *     RecyclerView.addItemDecoration(new LinearItemDecoration(2,false,Color.DKGRAY));
 *
 * </pre>
 *
 * @author Janson
 * @date 2020/8/13 15:55
 */
public class LinearItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint ;

    private boolean isHorizontal;
    /**
     * Space size in pixels.
     */
    private final int space;

    public LinearItemDecoration(int space) {
        this.space = space;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.TRANSPARENT);
    }


    public LinearItemDecoration(int space, boolean isHorizontal) {
        this.space = space;
        this.isHorizontal = isHorizontal;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.TRANSPARENT);
    }

    public LinearItemDecoration(int space, boolean isHorizontal, @ColorInt int color) {
        this.space = space;
        this.isHorizontal = isHorizontal;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        RecyclerView.Adapter<?> adapter = parent.getAdapter();
        int count =  parent.getChildCount();
        if (adapter != null){
            count = adapter.getItemCount();
        }
        if (position!= count-1) {
            if (isHorizontal){
                outRect.right = space;
            }else{
                outRect.bottom = space;
            }
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (paint == null){
            super.onDraw(c,parent,state);
            return;
        }
        if (isHorizontal){
            drawLinearHorizontal(c,parent);
        }else{
            drawLinearVertical(c,parent);
        }
    }


    private void drawLinearVertical(@NonNull Canvas c, @NonNull RecyclerView parent){
        Rect rect = new Rect() ;
        int childCount = parent.getChildCount() ;
        //  not draw the end line
        for (int i = 0; i < childCount-1; i++) {
            View child = parent.getChildAt(i);
            rect.left = child.getLeft() ;
            rect.right = child.getRight();
            rect.top = child.getBottom();
            rect.bottom = rect.top+ space;
            c.drawRect(rect , paint);
        }
    }

    private void drawLinearHorizontal(@NonNull Canvas c, @NonNull RecyclerView parent){
        Rect rect = new Rect() ;
        int childCount = parent.getChildCount() ;
        //  not draw the end line
        for (int i = 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            rect.left = child.getRight();
            rect.right = rect.left+ space;
            rect.top = child.getTop();
            rect.bottom = child.getBottom() ;
            c.drawRect(rect , paint);
        }
    }

}
