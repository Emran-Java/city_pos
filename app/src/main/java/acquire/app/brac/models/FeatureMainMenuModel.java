package acquire.app.brac.models;
import java.io.Serializable;
import java.util.List;

public class FeatureMainMenuModel implements Serializable {

    private String title;
    private int id;
    private int position;
    private String code;
    private String icon;
    private boolean isShow;
    private boolean isActive;
    private boolean hasChild;
    private String nextStep;
    private List<FeatureMainMenuModel> childData;

    public String getTitle() { return title; }
    public int getId() { return id; }
    public int getPosition() { return position; }
    public String getCode() { return code; }
    public String getIcon() { return icon; }
    public boolean isShow() { return isShow; }
    public boolean isActive() { return isActive; }
    public boolean isHasChild() { return hasChild; }
    public String getNextStep() { return nextStep; }
    public List<FeatureMainMenuModel> getChildData() { return childData; }
}