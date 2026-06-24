package acquire.settings.fragment.system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.constant.ParamsConst;
import acquire.settings.databinding.SettingsFragmentCurrencyBinding;
import acquire.settings.databinding.SettingsItemCurrencyBinding;

public class CurrencyFragment extends BaseDialogFragment {

    private SimpleCallback mCallback;
    private SettingsFragmentCurrencyBinding mBinding ;
    private List<CurrencyUtils.CurrencyBean> mItems ;
    private CurrencyAdapter mAdapter ;

    public static CurrencyFragment newInstance(SimpleCallback callback){
        CurrencyFragment currencyFragment = new CurrencyFragment();
        currencyFragment.mCallback = callback;
        currencyFragment.mItems = CurrencyUtils.getAllCurrencies();
        return currencyFragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = SettingsFragmentCurrencyBinding.inflate(inflater, container, false);
        mAdapter = new CurrencyAdapter(mItems, mCallback);
        mBinding.rvCurrency.setAdapter(mAdapter);
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public FragmentCallback getCallback() {
        return null;
    }


    static class CurrencyAdapter extends BaseBindingRecyclerAdapter<SettingsItemCurrencyBinding> {
        private final List<CurrencyUtils.CurrencyBean> originalList;
        private final List<CurrencyUtils.CurrencyBean> filteredList;

        private SimpleCallback callback ;

        public CurrencyAdapter(List<CurrencyUtils.CurrencyBean> items,SimpleCallback callback) {
            this.originalList = new ArrayList<>(items);
            this.filteredList = new ArrayList<>(items);
            this.callback = callback;
        }

        @Override
        protected void bindItemData(SettingsItemCurrencyBinding itemBinding, int position) {
            CurrencyUtils.CurrencyBean currencyBean = filteredList.get(position);
            itemBinding.tvNumericCode.setText(currencyBean.getNumericCode());
            itemBinding.tvAlphaCode.setText(currencyBean.getAlphaCode());
            itemBinding.tvSymbol.setText(currencyBean.getSymbol());
            itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_BASE_CURRENCY_CODE, currencyBean.getNumericCode());
                    if (callback != null) {
                        callback.result();
                        callback = null;
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public void filter(String query) {
            filteredList.clear();
            if (query.isEmpty()) {
                filteredList.addAll(originalList);
            } else {
                for (CurrencyUtils.CurrencyBean currencyBean : originalList) {
                    if (currencyBean.getAlphaCode().toLowerCase().contains(query.toLowerCase()) || currencyBean.getNumericCode().contains(query)) {
                        filteredList.add(currencyBean);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

}
