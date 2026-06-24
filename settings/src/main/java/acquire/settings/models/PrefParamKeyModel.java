package acquire.settings.models;

import java.io.Serializable;

public class PrefParamKeyModel implements Serializable {

    private String prefKey;
    private String prefKeyTitle;
    private PrefKeyValType prefKeyValueType;
    private String prefKeyStringValue;
    private String prefKeyBooleanValue;
    private String prefKeyIntValue;
    private String prefKeyFloatValue;
    private String prefKeyDoubleValue;

    public String getPrefKey() {
        return prefKey;
    }

    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }

    public String getPrefKeyTitle() {
        return prefKeyTitle;
    }

    public void setPrefKeyTitle(String prefKeyTitle) {
        this.prefKeyTitle = prefKeyTitle;
    }

    public PrefKeyValType getPrefKeyValueType() {
        return prefKeyValueType;
    }

    public void setPrefKeyValueType(PrefKeyValType prefKeyValueType) {
        this.prefKeyValueType = prefKeyValueType;
    }

    public String getPrefKeyStringValue() {
        return prefKeyStringValue;
    }

    public void setPrefKeyStringValue(String prefKeyStringValue) {
        this.prefKeyStringValue = prefKeyStringValue;
    }

    public String getPrefKeyBooleanValue() {
        return prefKeyBooleanValue;
    }

    public void setPrefKeyBooleanValue(String prefKeyBooleanValue) {
        this.prefKeyBooleanValue = prefKeyBooleanValue;
    }

    public String getPrefKeyIntValue() {
        return prefKeyIntValue;
    }

    public void setPrefKeyIntValue(String prefKeyIntValue) {
        this.prefKeyIntValue = prefKeyIntValue;
    }

    public String getPrefKeyFloatValue() {
        return prefKeyFloatValue;
    }

    public void setPrefKeyFloatValue(String prefKeyFloatValue) {
        this.prefKeyFloatValue = prefKeyFloatValue;
    }

    public String getPrefKeyDoubleValue() {
        return prefKeyDoubleValue;
    }

    public void setPrefKeyDoubleValue(String prefKeyDoubleValue) {
        this.prefKeyDoubleValue = prefKeyDoubleValue;
    }

}
