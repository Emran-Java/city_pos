package acquire.core.tools;

import android.content.Context;

import java.io.InputStream;

public class JsonUtils {

    public static String loadJSONFromAsset(Context context, String jsonFileName) {

        String json;

        if(jsonFileName==null) return null;

        try {
            InputStream is = context.getAssets().open(jsonFileName);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }
}
