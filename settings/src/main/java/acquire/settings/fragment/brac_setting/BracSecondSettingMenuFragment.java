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

import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.settings.R;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.settings.contents.SettingMenuContentList;
import acquire.settings.databinding.FragmentManageBinding;
import acquire.settings.models.BracSettingMenuItemModel;


public class BracSecondSettingMenuFragment extends BaseFragment {

    private String mTitle;
    private RecyclerView recyclerView;
    private BracSettingListAdapter adapter;
    private List<BracSettingMenuItemModel> menuList = new ArrayList<>();

    public static final String BUNDLE_ARG_KEY_MENU_LIST = "menu_list";
    public static final String BUNDLE_ARG_KEY_TITLE_BAR_TITLE = "titleText";

    public BracSecondSettingMenuFragment() {
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

       /*
        menuList = new ArrayList<>();
        menuList.add(new BracSettingMenuItemModel("Merchant Info"));
        menuList.add(new BracSettingMenuItemModel("ON-OFF"));
        menuList.add(new BracSettingMenuItemModel("Trans Params"));
        menuList.add(new BracSettingMenuItemModel("Key Management"));
        menuList.add(new BracSettingMenuItemModel("Clear"));
        menuList.add(new BracSettingMenuItemModel("System"));
        menuList.add(new BracSettingMenuItemModel("Print"));
        menuList.add(new BracSettingMenuItemModel("Other"));
       */

        adapter = new BracSettingListAdapter(menuList, (item, position) -> {
            Fragment subListFragment;
            switch (item.getTitle()) {
                case "Merchant Info":
                    //Toast.makeText(getContext(), "Merchant Info Clicked", Toast.LENGTH_SHORT).show();
                    subListFragment = SettingMenuContentList.getMerchantInfoFragment(item.getTitle());
                    mSupportDelegate.switchContent(subListFragment);
                    break;

                case "ON-OFF":
//                    Fragment subListFragment = new BracSettingSubListFragment();
                    subListFragment = SettingMenuContentList.getOnOffFragment(item.getTitle());
                    //Toast.makeText(getContext(), "ON-OFF Clicked", Toast.LENGTH_SHORT).show();
                    mSupportDelegate.switchContent(subListFragment);
                    break;

                case "Trans Params":
                    //getTransParamFragment
                    mSupportDelegate.switchContent(SettingMenuContentList.getTransParamFragment(item.getTitle()));
                    break;

                case "Key Management":
                    break;

                case "Clear":
                    break;

                case "System":
                    break;

                case "Print":
                    break;

                case "Other":
                    break;

                    //third layer
                /*case "Trans Enable":
                    mSupportDelegate.switchContent(SettingMenuContentList.getTransEnableFragment(item.getTitle()));
                    break;*/
            }

        });

        recyclerView.setAdapter(adapter);

        return view;
    }

  /*  private Fragment getMerchantInfoFragment(String title) {
        ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW, "");
        ArrayList<BracSettingMenuItemModel> list = new ArrayList<>();
        list.add(new BracSettingMenuItemModel("Merchant Name",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW, "")));
        list.add(new BracSettingMenuItemModel("Merchant ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ID, ""),false));
        list.add(new BracSettingMenuItemModel("Terminal ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_POS_ID, ""),false));
        list.add(new BracSettingMenuItemModel("PayFlex Merchant ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_EMI_MERCHANT_ID, ""),false));
        list.add(new BracSettingMenuItemModel("PayFlex Terminal ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_EMI_POS_ID, ""),false));
        list.add(new BracSettingMenuItemModel("Merchant Address",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 2",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR2, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 3",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR3, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 4",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR4, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 5",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR5, "")));
        list.add(new BracSettingMenuItemModel("Footer1",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER1, "")));
        list.add(new BracSettingMenuItemModel("Footer2",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER2, "")));
        list.add(new BracSettingMenuItemModel("Footer3",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER3, "")));
        list.add(new BracSettingMenuItemModel("Footer4",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER4, "")));
        list.add(new BracSettingMenuItemModel("Merchant Copy", ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY1, "")));
        list.add(new BracSettingMenuItemModel("Customer Copy",ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY2, "")));
        list.add(new BracSettingMenuItemModel("Vendor Copy",ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY3, "")));

        Fragment manageFragment = new BracSettingSubListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSettingSubListFragment.BUNDLE_ARG_KEY_MENU_LIST, list);
        bundle.putString(BracSettingSubListFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, title);

        manageFragment.setArguments(bundle);

        return manageFragment;
    }
    private Fragment getOnOffFragment(String title) {

        ArrayList<BracSettingMenuItemModel> list = new ArrayList<>();

        list.add(new BracSettingMenuItemModel("Trans Enable"));
        list.add(new BracSettingMenuItemModel("Payment Type"));
        list.add(new BracSettingMenuItemModel("Tip Enable"));
        list.add(new BracSettingMenuItemModel("Fallback Enable"));
        list.add(new BracSettingMenuItemModel("CVV2"));
        list.add(new BracSettingMenuItemModel("Tip Adjust Enable"));
        list.add(new BracSettingMenuItemModel("Last Four Digit Check"));
        Fragment manageFragment = new BracSettingSubListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSettingSubListFragment.BUNDLE_ARG_KEY_MENU_LIST, list);
        bundle.putString(BracSettingSubListFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, title);

        manageFragment.setArguments(bundle);

        return manageFragment;
    }*/


    @Override
    public FragmentCallback getCallback() {
        return null;
    }




}
