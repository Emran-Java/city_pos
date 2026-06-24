package acquire.settings.fragment.brac_setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.settings.R;
import acquire.settings.databinding.FragmentManageBinding;
import acquire.settings.models.BracSettingMenuItemModel;


public class BracSettingPrefParamListFragment extends BaseFragment {

    private String mTitle;
    private RecyclerView recyclerView;
    private BracSettingListAdapter adapter;
    private List<BracSettingMenuItemModel> menuList = new ArrayList<>();
    public static final String BUNDLE_ARG_KEY_MENU_LIST = "pref_param_key_list";
    public static final String BUNDLE_ARG_KEY_TITLE_BAR_TITLE = "titleText";
    public BracSettingPrefParamListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentManageBinding binding = FragmentManageBinding.inflate(inflater, container, false);
        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_ARG_KEY_MENU_LIST)) {
            Serializable data = args.getSerializable(BUNDLE_ARG_KEY_MENU_LIST);

            if (data instanceof ArrayList) {
                menuList = (ArrayList<BracSettingMenuItemModel>) data;
            }
        }

        if (args != null && args.containsKey(BUNDLE_ARG_KEY_TITLE_BAR_TITLE)) {
            mTitle = args.getString(BUNDLE_ARG_KEY_TITLE_BAR_TITLE);
        }

        binding.toolbar.setTitle(mTitle);
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());

//        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        View view = binding.getRoot();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        menuList = new ArrayList<>();
/*        list.add(new BracSettingMenuItemModel("Trans Enable"));
        list.add(new BracSettingMenuItemModel("Payment Type"));
        list.add(new BracSettingMenuItemModel("Tip Enable"));
        list.add(new BracSettingMenuItemModel("Fallback Enable"));
        list.add(new BracSettingMenuItemModel("CVV2"));
        list.add(new BracSettingMenuItemModel("Tip Adjust Enable"));
        list.add(new BracSettingMenuItemModel("Last Four Digit Check"));*/

        BracSettingMenuItemModel stngMenuItemObj  = new BracSettingMenuItemModel("Enable QR Receipt");
        stngMenuItemObj.setCode(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT);
        menuList.add(stngMenuItemObj );

        BracSettingMenuItemModel stngMenuItemObj2  = new BracSettingMenuItemModel("Enable QR Receipt in Slip");
        stngMenuItemObj2.setCode(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP);
        menuList.add(stngMenuItemObj2);

        adapter = new BracSettingListAdapter(menuList, (item, position) -> {

            switch (item.getTitle()) {
                case "Trans Enable":{

                    break;
                }
                case "Enable QR Receipt":
                   // Toast.makeText(getContext(), "Enable QR Receipt", Toast.LENGTH_SHORT).show();
                    break;

                case "Enable QR Receipt in Slip":
                   // Toast.makeText(getContext(), "Enable QR Receipt in Slip", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        adapter.setSwitchButtonListener(new BracSettingListAdapter.SwitchButtonClickListener() {
            @Override
            public void onItemClick(BracSettingMenuItemModel item, int position, boolean isSwitchEnable) {

                switch (item.getTitle()) {
                    case "Enable QR Receipt":
                        //Toast.makeText(getContext(), "Enable QR Receipt", Toast.LENGTH_SHORT).show();
                        ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT,isSwitchEnable);
                        break;

                    case "Enable QR Receipt in Slip":
                        //Toast.makeText(getContext(), "Enable QR Receipt in Slip", Toast.LENGTH_SHORT).show();
                        ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP,isSwitchEnable);
                        break;
                }

            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public FragmentCallback getCallback() {
        return null;
    }




}
