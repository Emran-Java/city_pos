package acquire.settings.contents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MenuChangeListen {

    private static MenuChangeListen instance;

    public static synchronized MenuChangeListen getInstance() {

        if (instance == null) {
            instance = new MenuChangeListen();
        }
        return instance;
    }

    private final MutableLiveData<Boolean> memoLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getMemo() {
        return memoLiveData;
    }

    public void updateMemo(Boolean memo) {
        memoLiveData.setValue(memo);
    }
}
