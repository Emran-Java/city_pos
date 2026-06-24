package acquire.core.tools;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.core.R;
import acquire.core.constant.FileConst;

/**
 * Response code utils
 *
 * @author Janson
 * @date 2019/10/22 16:23
 */
public class AnswerCodeProvider {
    /**
     * Response code cache map
     */
    private final static Properties ANSWER_PROP = new Properties();

    /**
     * Init
     */
    private static void init() {
        //load response code file
        AssetManager am = BaseApplication.getAppContext().getAssets();
        try (InputStream inputStream = am.open(FileConst.ANSWER_CODE)){
            ANSWER_PROP.load(inputStream);
        } catch (IOException e) {
            LoggerUtils.e("open "+ FileConst.ANSWER_CODE+" failed!",e);
        }
    }


    /**
     * Get message by response code.
     *
     * @param responseCode response code from server
     * @return response message
     */
    @NonNull
    public static String getRspMessage(String responseCode) {
        if (ANSWER_PROP.isEmpty()) {
            init();
        }
        String res = ANSWER_PROP.getProperty(responseCode);
        if (res == null) {
            res = BaseApplication.getAppString(R.string.core_answer_code_unknow);
        }
        return res;
    }

}
