package acquire.core.fragment.common.submenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.core.TransActivity;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.databinding.CoreFragmentFeatureSubMenuListBinding;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.R;
import acquire.core.tools.LoadMenuData;

public class CoreFeatureSubMenuListFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private CoreSubMenuAdapter adapter;
    private ArrayList<FeatureSubMenuModel> reportList = new ArrayList<>();
    private String mItleBarText;
    private static SimpleCallback mCallback;

    public static CoreFeatureSubMenuListFragment newInstance(String titleText, List<FeatureSubMenuModel> list, SimpleCallback callback) {

        CoreFeatureSubMenuListFragment fragment = new CoreFeatureSubMenuListFragment();

        Bundle bundle = new Bundle();
        if(list==null) list = new ArrayList<>();
        bundle.putSerializable(IntentParamKeyContent.TRANS_REPORT_LIST, new ArrayList<>(list));
        bundle.putSerializable(IntentParamKeyContent.TRANS_TITLE_TEXT, titleText);
        fragment.setArguments(bundle);
        mCallback = callback;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        CoreFragmentFeatureSubMenuListBinding binding = CoreFragmentFeatureSubMenuListBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(R.string.core_transaction_name_reports_print);
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());

        //        View view = inflater.inflate(R.layout.fragment_report_menu_list, container, false);
        View view = binding.getRoot();
        //FragmentReportListBinding binding = FragmentReportListBinding.inflate(LayoutInflater.from(requireContext()));

        acquire.base.widget.PrimaryToolbar toolbar = (acquire.base.widget.PrimaryToolbar)view.findViewById(R.id.toolbar);
//        toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
//        toolbar.setTitle("Prints Report".toUpperCase());

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            reportList = (ArrayList<FeatureSubMenuModel>) getArguments().getSerializable(IntentParamKeyContent.TRANS_REPORT_LIST);
            mItleBarText = (String) getArguments().getSerializable(IntentParamKeyContent.TRANS_TITLE_TEXT);

            if(mItleBarText!=null && !mItleBarText.isEmpty())
                binding.toolbar.setTitle(mItleBarText.toUpperCase());

            if(reportList==null || reportList.isEmpty()){
                loadRootReports();
            }

        } else {
            loadRootReports();
        }

        adapter = new CoreSubMenuAdapter(reportList,
                (CoreSubMenuAdapter.OnItemClickListener) getActivity());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadRootReports() {

       /* String json = ParamsUtils.getString(FileConst.REPORT_MENU_PREF_KEY, null);//JsonUtils.loadJSONFromAsset(getContext(), null);

        if(json==null){
            LoggerUtils.d("newCall get ReportMenu: is null");
            return;
        }

        LoggerUtils.d("newCall get ReportMenu: "+json);
        Gson gson = new Gson();
        ReportsResponse response =
                gson.fromJson(json, ReportsResponse.class);*/

        reportList = LoadMenuData.loadReportMenuItems();

        if (reportList.size() == 1 && !reportList.get(0).isHasChild()) {
            ((TransActivity) getActivity())
                    .onItemClick(reportList.get(0));
        }
    }

    @Override
    public FragmentCallback getCallback() {
        return mCallback;
    }
}