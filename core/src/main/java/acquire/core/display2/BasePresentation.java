package acquire.core.display2;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.constant.ParamsConst;

/**
 * A basic class of Presentation for dual screen.
 * <p>e.g. create a Presentation extends {@link BasePresentation}:</p>
 * <pre>
 *    class PresentationDemo extends BasePresentation{
 *         public PresentationDemo(Context outerContext) {
 *             super(outerContext);
 *         }
 *         protected void onCreate(Bundle savedInstanceState) {
 *             super.onCreate(savedInstanceState);
 *             setContentView(R.layout.xxx);
 *         }
 *    }
 * @author Janson
 * @date 2022/8/29 14:09
 */
public class BasePresentation extends Presentation {
    private final String className = String.format(Locale.getDefault(),"%s{%02x}",getClass().getSimpleName(),System.identityHashCode(this));
    private final static Map<Context, List<Presentation>> PRESENTATION_MAP = new LinkedHashMap<>();
    private final Context outerContext;

    public BasePresentation(Context outerContext) {
        this(outerContext, DisplayUtils.getDisplay2(outerContext));
    }

    public BasePresentation(Context outerContext, Display display) {
        super(outerContext, display, R.style.Presentation);
        this.outerContext = outerContext;
        List<Presentation> presentations = PRESENTATION_MAP.computeIfAbsent(outerContext, k -> new ArrayList<>());
        presentations.add(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerUtils.v(className+"->onCreate");
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_SECOND_SCREEN_TOP)){
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }
    public String getString(@StringRes int resId, Object... formatArgs) {
        return outerContext.getString(resId,formatArgs);
    }
    public static void removeAllPresentations(Context context){
        ThreadPool.postOnMain(()->{
            List<Presentation> presentations = PRESENTATION_MAP.get(context);
            if (presentations != null){
                Iterator<Presentation> iterator = presentations.iterator();
                while (iterator.hasNext()){
                    Presentation presentation = iterator.next();
                    iterator.remove();
                    presentation.dismiss();
                }
                PRESENTATION_MAP.remove(context);
            }
            clearInvalidPresentations();
        });
    }
    private static void clearInvalidPresentations(){
        for (Map.Entry<Context, List<Presentation>> entry : PRESENTATION_MAP.entrySet()) {
            Context context = entry.getKey();
            if (context instanceof Activity && ((Activity)context).isDestroyed()){
                List<Presentation> presentations = entry.getValue();
                for (Presentation presentation : presentations) {
                    if (presentation != null){
                        presentation.dismiss();
                    }
                }
                PRESENTATION_MAP.remove(context);
            }

        }
    }

    public static Presentation getTopPresentation(){
        Presentation topPresentation = null;
        for (Map.Entry<Context, List<Presentation>> entry : PRESENTATION_MAP.entrySet()) {
            List<Presentation> presentations = entry.getValue();
            if (presentations != null && presentations.size()>0){
                topPresentation = presentations.get(presentations.size()-1);
            }
        }
        return topPresentation;
    }

    @Override
    public void show() {
        LoggerUtils.v(className+"->show");
        super.show();
    }

    @Override
    public void hide() {
        LoggerUtils.v(className+"->hide");
        super.hide();
    }

    @Override
    protected void onStop() {
        List<Presentation> presentations = PRESENTATION_MAP.get(outerContext);
        if (presentations != null){
            presentations.remove(this);
            if (presentations.isEmpty()){
                PRESENTATION_MAP.remove(outerContext);
            }
        }
        super.onStop();
    }

    @Override
    public void dismiss() {
        LoggerUtils.v(className+"->dismiss");
        super.dismiss();
    }
}
