package acquire.settings.fragment.brac_setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
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
import acquire.settings.contents.MenuChangeListen;
import acquire.settings.contents.SettingMenuContentList;
import acquire.settings.databinding.FragmentManageBinding;
import acquire.settings.models.BracSettingMenuItemModel;
import acquire.settings.models.PrefKeyValType;


public class BracSettingThirdSettingMenuFragment extends BaseFragment {

    private String mTitle;
    private RecyclerView recyclerView;
    private BracSettingListAdapter adapter;
    private List<BracSettingMenuItemModel> menuList = new ArrayList<>();
    public static final String BUNDLE_ARG_KEY_MENU_LIST = "sub_menu_list";
    public static final String BUNDLE_ARG_KEY_TITLE_BAR_TITLE = "titleText";


    public BracSettingThirdSettingMenuFragment() {
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
                menuList.clear();
                menuList = (ArrayList<BracSettingMenuItemModel>) data;
            }
        }
        if (args != null && args.containsKey(BUNDLE_ARG_KEY_TITLE_BAR_TITLE)) {
            mTitle = args.getString(BUNDLE_ARG_KEY_TITLE_BAR_TITLE);
        }

        binding.toolbar.setTitle(mTitle);
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

//        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        View view = binding.getRoot();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // menuList = new ArrayList<>();
        /*  list.add(new BracSettingMenuItemModel("Trans Enable"));
            list.add(new BracSettingMenuItemModel("Payment Type"));
            list.add(new BracSettingMenuItemModel("Tip Enable"));
            list.add(new BracSettingMenuItemModel("Fallback Enable"));
            list.add(new BracSettingMenuItemModel("CVV2"));
            list.add(new BracSettingMenuItemModel("Tip Adjust Enable"));
            list.add(new BracSettingMenuItemModel("Last Four Digit Check"));
        */

        if (mTitle.equalsIgnoreCase("ON-OFF")) {
            BracSettingMenuItemModel stngMenuItemObj = new BracSettingMenuItemModel("Enable QR Receipt", ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT, PrefKeyValType.STRING, true, true);
            stngMenuItemObj.setCode(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT);
            menuList.add(stngMenuItemObj);

            BracSettingMenuItemModel stngMenuItemObj2 = new BracSettingMenuItemModel("Enable QR Receipt in Slip", ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP, PrefKeyValType.STRING, true, true);
            stngMenuItemObj2.setCode(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP);
            menuList.add(stngMenuItemObj2);
        }

        adapter = new BracSettingListAdapter(menuList, (item, position) -> {

            MenuChangeListen.getInstance().updateMemo(true);

            switch (item.getTitle()) {
                case "Trans Enable": {
                    Fragment subListFragment = SettingMenuContentList.getTransEnableFragment(item.getTitle());
                    //Toast.makeText(getContext(), "ON-OFF Clicked", Toast.LENGTH_SHORT).show();
                    mSupportDelegate.switchContent(subListFragment);

                    break;
                }
            }
        });

        adapter.setSwitchButtonListener(new BracSettingListAdapter.SwitchButtonClickListener() {
            @Override
            public void onItemClick(BracSettingMenuItemModel item, int position, boolean isSwitchEnable) {

                MenuChangeListen.getInstance().updateMemo(true);

                switch (item.getTitle()) {

                    case "Enable QR Receipt":
                        //Toast.makeText(getContext(), "Enable QR Receipt", Toast.LENGTH_SHORT).show();
                        ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT, isSwitchEnable);
                        break;

                    case "Enable QR Receipt in Slip":
                        //Toast.makeText(getContext(), "Enable QR Receipt in Slip", Toast.LENGTH_SHORT).show();
                        ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP, isSwitchEnable);
                        break;
                }

            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

/*    private Fragment getTranEnbleFragment(String title) {
        ArrayList<BracSettingMenuItemModel> list = new ArrayList<>();

        list.add(new BracSettingMenuItemModel("Trans Enable"));
        list.add(new BracSettingMenuItemModel("Payment Type"));
        list.add(new BracSettingMenuItemModel("Tip Enable"));
        list.add(new BracSettingMenuItemModel("Fallback Enable"));
        list.add(new BracSettingMenuItemModel("CVV2"));
        list.add(new BracSettingMenuItemModel("Tip Adjust Enable"));
        list.add(new BracSettingMenuItemModel("Last Four Digit Check"));
        Fragment manageFragment = new BracSettingThirdSettingMenuFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSettingThirdSettingMenuFragment.BUNDLE_ARG_KEY_MENU_LIST, list);
        bundle.putString(BracSettingThirdSettingMenuFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, title);

        manageFragment.setArguments(bundle);

        return manageFragment;
    }*/


    @Override
    public FragmentCallback getCallback() {
        return null;
    }


}
