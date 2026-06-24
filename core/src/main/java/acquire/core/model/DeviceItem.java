package acquire.core.model;

import androidx.annotation.StringRes;

public class DeviceItem {
    private final @StringRes int title;
    private final String content, sTitle;

    public DeviceItem(@StringRes int title, String content) {
        this.sTitle = null;
        this.title = title;
        this.content = content;
    }

    public DeviceItem(String sTitle, String content) {
        this.title = 0;
        this.sTitle = sTitle;
        this.content = content;
    }

    public @StringRes int getTitle() {
        return title;
    }

    public String getSTitle() {
        return sTitle;
    }

    public String getContent() {
        return content;
    }
}
