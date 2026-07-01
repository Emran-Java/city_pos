package acquire.app.brac.ui.new_home;

import java.util.List;

import acquire.app.brac.models.FeatureMainMenuModel;

public class CityHomeUiState {

    private String merchantName;
    private String sliderPath;
    private String leftLogoPath;
    private String rightLogoPath;
    private List<FeatureMainMenuModel> mainFeatureMenuJson;


    public CityHomeUiState(List<FeatureMainMenuModel> mainFeatureMenuJson){
        this.mainFeatureMenuJson = mainFeatureMenuJson;
    }
    public CityHomeUiState(String merchantName,
                           String leftLogoPath,
                           String rightLogoPath,
                           String sliderPath) {
        this.merchantName = merchantName;
        this.leftLogoPath = leftLogoPath;
        this.rightLogoPath = rightLogoPath;
        this.sliderPath = sliderPath;
    }

    public List<FeatureMainMenuModel> getMainFeatureMenuJson() {
        return mainFeatureMenuJson;
    }

    /*public void setMainFeatureMenuJson(String mainFeatureMenuJson) {
        this.mainFeatureMenuJson = mainFeatureMenuJson;
    }*/

    public String getSliderPath() {
        return sliderPath;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getLeftLogoPath() {
        return leftLogoPath;
    }

    public String getRightLogoPath() {
        return rightLogoPath;
    }
}
