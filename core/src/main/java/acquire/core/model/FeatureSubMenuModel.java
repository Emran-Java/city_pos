package acquire.core.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@Keep
public class FeatureSubMenuModel implements Serializable {

    @SerializedName("title")
    private String title;

    @SerializedName("id")
    private int id;

    @SerializedName("position")
    private int position;

    @SerializedName("code")
    private String code;

    @SerializedName("icon")
    private String icon;

    @SerializedName("isShow")
    private boolean isShow;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("hasChild")
    private boolean hasChild;

    @SerializedName("nextStep")
    private String nextStep;

    @SerializedName("childData")
    private List<FeatureSubMenuModel> childData;

    // Required empty constructor for Gson/R8 safety
    public FeatureSubMenuModel() {
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getCode() {
        return code;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isShow() {
        return isShow;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public String getNextStep() {
        return nextStep;
    }

    public List<FeatureSubMenuModel> getChildData() {
        return childData;
    }
}