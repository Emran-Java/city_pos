package acquire.app.brac.ui.new_home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.app.brac.models.FeatureMainMenuModel;
import acquire.app.brac.models.FeatureMainMenuResponse;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;

public class CityHomeViewModel extends ViewModel {

    private final MutableLiveData<CityHomeUiState> featureMainMenuState =
            new MutableLiveData<>();

    private final MutableLiveData<CityHomeUiState> uiState =
            new MutableLiveData<>();



    public MutableLiveData<CityHomeUiState> getFeatureMainMenuState() {
        return featureMainMenuState;
    }

    public LiveData<CityHomeUiState> getUiState() {
        return uiState;
    }

    public void loadFeatureMainMenu(){

        String mainFeatureMenuJson = ParamsUtils.getString(
                ParamsConst.PARAMS_KEY_MENU_FEATURE_MAIN,
                ""
        );

        FeatureMainMenuResponse response;
        response = new Gson().fromJson(mainFeatureMenuJson, FeatureMainMenuResponse.class);

        List<FeatureMainMenuModel> menuList =
                prepareMenuList(response.featureMainMenu());

        CityHomeUiState state = new CityHomeUiState(menuList);
        featureMainMenuState.setValue(state);

    }

    private List<FeatureMainMenuModel> prepareMenuList(List<FeatureMainMenuModel> list) {

        List<FeatureMainMenuModel> newList = new ArrayList<>();

        for (FeatureMainMenuModel item : list) {

            if (item.isShow()) {
                newList.add(item);
            }
        }

        newList.sort((o1, o2) ->
                Integer.compare(o1.getPosition(), o2.getPosition()));

        return newList;
    }

    public void loadData() {

        String sliderPath = ParamsUtils.getString(
                ParamsConst.PARAMS_KEY_SLIDER_PATH,
                "Hi,"
        );

        String merchantName = ParamsUtils.getString(
                ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW,
                "Hi,"
        );

        String logoPath = ParamsUtils.getString(
                ParamsConst.PARAMS_KEY_LOGO_PATH,
                ""
        );


        // update UI
        CityHomeUiState state = new CityHomeUiState(
                merchantName,
                logoPath + "/logo_home_left.png",
                logoPath + "/logo_home_right.png",
                sliderPath
        );

        uiState.setValue(state);
    }
}
