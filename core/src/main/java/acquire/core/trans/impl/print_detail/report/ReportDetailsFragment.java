package acquire.core.trans.impl.print_detail.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.core.model.FeatureSubMenuModel;


//TODO: will be remove
public class ReportDetailsFragment extends BaseFragment {

    private static final String KEY = "data";

    private static SimpleCallback mSimpleCallback;

    public static ReportDetailsFragment newInstance(FeatureSubMenuModel model, SimpleCallback simpleCallback) {

        ReportDetailsFragment fragment = new ReportDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY, model);

        fragment.setArguments(bundle);
        mSimpleCallback = simpleCallback;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                android.R.layout.simple_list_item_1,
                container,
                false);

        TextView text = view.findViewById(android.R.id.text1);

        FeatureSubMenuModel model =
                (FeatureSubMenuModel) getArguments().getSerializable(KEY);

        text.setText("Report Details\n\n" + model.getTitle());

        return view;
    }

    @Override
    public FragmentCallback getCallback() {
        return mSimpleCallback;
    }
}