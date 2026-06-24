package acquire.sdk.printer;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * Printer interface
 *
 * @author Janson
 * @date 2023/7/5 15:04
 */
public interface IPrinter {

    void print(@NonNull Bitmap receipt,boolean endFeed, final PrintCallback callback);
    void cutPaper();

    /**
     * print result callback
     *
     * @author Janson
     * @date 2018/5/24 2:01
     */
    interface PrintCallback {
        /**
         * print finish. It's success
         */
        void onFinish();

        /**
         * print fail
         *
         * @param message error description
         */
        void onError(String message);

        /**
         * out of paper
         */
        void onOutOfPaper();
    }
}
