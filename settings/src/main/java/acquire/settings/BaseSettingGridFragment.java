package acquire.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.settings.databinding.SettingsFragmentGridMenuBinding;
import acquire.settings.databinding.SettingsGridMenuItemBinding;

/**
 * The base grid settings{@link Fragment}
 *
 * @author Janson
 * @date 2020/8/13 11:30
 */
public abstract class BaseSettingGridFragment extends BaseFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SettingsFragmentGridMenuBinding binding = SettingsFragmentGridMenuBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.rvMenu.setAdapter(new GridMenuAdapter(getItems()));
        return binding.getRoot();
    }

    /**
     * Get title
     *
     * @return title name
     */
    protected abstract String getTitle();

    /**
     * Settings item
     *
     * @return Settings item
     */
    protected abstract List<GridItem> getItems();

    /**
     * Grid menu adapter
     *
     * @author Janson
     * @date 2020/8/13 11:53
     */
    private static class GridMenuAdapter extends BaseBindingRecyclerAdapter<SettingsGridMenuItemBinding> {
        private final List<GridItem> items;

        public GridMenuAdapter(List<GridItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(@NonNull SettingsGridMenuItemBinding itemBinding, int position) {
            GridItem item = items.get(position);
            if (item.colorResId != 0){
                itemBinding.cvIcon.setCardBackgroundColor(ContextCompat.getColor(itemBinding.getRoot().getContext(),item.colorResId));
            }
            itemBinding.icon.setImageResource(item.iconResId);
            itemBinding.tvName.setText(item.nameResId);
            itemBinding.getRoot().setOnClickListener(item.listener);
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * Menu item bean
     *
     * @author Janson
     * @date 2020/8/13 11:53
     */
    public static class GridItem {
        private @DrawableRes final int iconResId;
        private @StringRes final int nameResId;
        private @ColorRes
        final int colorResId;
        private final View.OnClickListener listener;

        public GridItem(@DrawableRes int iconResId,@ColorRes int colorResId,@StringRes int nameResId, View.OnClickListener listener) {
            this.iconResId = iconResId;
            this.colorResId = colorResId;
            this.nameResId = nameResId;
            this.listener = listener;
        }
    }

}