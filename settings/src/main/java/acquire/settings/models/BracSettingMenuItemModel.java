package acquire.settings.models;

import java.io.Serializable;

public class BracSettingMenuItemModel implements Serializable {
    private String title;
    private String subTitle;

    private String code;

    private String prefKey;
    private PrefKeyValType prefKeyValType;

    private boolean isEnable=true;
    private boolean isShow=true;
    private boolean isEditable=true;
    private boolean isSwitch;


    public BracSettingMenuItemModel(String title) {
        this.title = title;
    }

    public BracSettingMenuItemModel(String title, String subTitle) {
        this.title = title;
        this.subTitle = subTitle;
    }
    public BracSettingMenuItemModel(String title, String subTitle, boolean isEnable) {
        this.title = title;
        this.subTitle = subTitle;
        this.isEnable = isEnable;
    }
    public BracSettingMenuItemModel(String title, String subTitle, boolean isEnable, boolean isSwitch) {
        this.title = title;
        this.subTitle = subTitle;
        this.isEnable = isEnable;
        this.isSwitch = isSwitch;
    }

    public BracSettingMenuItemModel(String title,  String prefKey, PrefKeyValType prefKeyValType, boolean isEnable, boolean isSwitch) {
        this.title = title;
        this.isEnable = isEnable;
        this.isSwitch = isSwitch;
        this.prefKey = prefKey;
        this.prefKeyValType = prefKeyValType;
    }

    public BracSettingMenuItemModel(String title, String subTitle, String prefKey, PrefKeyValType prefKeyValType, boolean isEnable, boolean isSwitch) {
        this.title = title;
        this.subTitle = subTitle;
        this.isEnable = isEnable;
        this.isSwitch = isSwitch;
        this.prefKey = prefKey;
        this.prefKeyValType = prefKeyValType;
    }

    public BracSettingMenuItemModel(String title, boolean isEnable) {
        this.title = title;
        this.isEnable = isEnable;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }

    public PrefKeyValType getPrefKeyValType() {
        return prefKeyValType;
    }

    public void setPrefKeyValType(PrefKeyValType prefKeyValType) {
        this.prefKeyValType = prefKeyValType;
    }

    public boolean isSwitch() {
        return isSwitch;
    }

    public void setSwitch(boolean aSwitch) {
        isSwitch = aSwitch;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
