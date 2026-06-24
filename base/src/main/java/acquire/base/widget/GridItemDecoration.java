package acquire.base.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


/**
 * RecyclerView grid item spacing.
 * <p><hr><b>e.g.:</b></p>
 * <pre>
 *     RecyclerView.addItemDecoration(new GridItemDecoration(2));
 *     //or
 *     RecyclerView.addItemDecoration(new GridItemDecoration(2,Color.DKGRAY));
 *
 * </pre>
 *
 * @author Janson
 * @date 2020/8/13 15:55
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint ;

    /**
     * Space size in pixels.
     */
    private final int space;
    private int spanCount = 0;

    public GridItemDecoration(int space) {
        this.space = space;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.TRANSPARENT);
    }

    public GridItemDecoration(int space, @ColorInt int color) {
        this.space = space;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (spanCount == 0){
            if (layoutManager instanceof GridLayoutManager){
                spanCount = ((GridLayoutManager)layoutManager).getSpanCount();
            }else if (layoutManager instanceof StaggeredGridLayoutManager){
                spanCount = ((StaggeredGridLayoutManager)layoutManager).getSpanCount();
            }
        }
        outRect.bottom = space;
        if ((parent.getChildAdapterPosition(view)+1)% spanCount != 0) {
            //No the end column
            outRect.right = space;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (paint == null){
            super.onDraw(c,parent,state);
            return;
        }
        drawGrid(c,parent);
    }




    private void drawGrid(@NonNull Canvas c, @NonNull RecyclerView parent){
        Rect rect = new Rect() ;
        int childCount = parent.getChildCount() ;
        if(childCount==0){
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if ((i+1)%spanCount !=0){
                //draw vertical line
                rect.left = child.getRight() ;
                rect.right = child.getRight()+ space;
                rect.top = child.getTop();
                rect.bottom = child.getBottom()+ space;
                c.drawRect(rect , paint);
            }
            //draw horizontal line
            rect.left = child.getLeft() ;
            rect.right = child.getRight();
            rect.top = child.getBottom();
            rect.bottom = child.getBottom()+ space;
            c.drawRect(rect , paint);
        }
        if (childCount % spanCount != 0){
            //draw last horizontal line
            View child = parent.getChildAt(childCount-1);
            rect.left = child.getRight() ;
            rect.right = parent.getRight();
            rect.top = child.getBottom();
            rect.bottom = child.getBottom()+ space;
            c.drawRect(rect , paint);
        }
    }
}
