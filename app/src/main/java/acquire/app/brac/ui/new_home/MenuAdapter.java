package acquire.app.brac.ui.new_home;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


import com.zztl.pos.city.R;
import com.zztl.pos.city.databinding.ItemReportMenuBinding;
import acquire.app.brac.models.FeatureMainMenuModel;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    public interface OnMenuClickListener {
        void onMenuClick(FeatureMainMenuModel menu);
    }

    private final Context context;
    private List<FeatureMainMenuModel> menuList;
    private final OnMenuClickListener listener;

    public MenuAdapter(Context context,
                       List<FeatureMainMenuModel> menuList,
                       OnMenuClickListener listener) {

        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateListData(List<FeatureMainMenuModel> menuList){
        this.menuList = menuList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemReportMenuBinding binding = ItemReportMenuBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);

        return new MenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {

        FeatureMainMenuModel item = menuList.get(position);

        holder.binding.txtTitle.setText(item.getTitle());

        //-----------------------------
        // Icon
        //-----------------------------

        int iconRes = context.getResources().getIdentifier(
                item.getIcon(),
                "drawable",
                context.getPackageName());

        if (iconRes != 0) {
            holder.binding.imgIcon.setImageResource(iconRes);
        }

        //-----------------------------
        // Active / Inactive
        //-----------------------------

        if (item.isActive()) {

            holder.binding.llReportItem.setAlpha(1f);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(item);
                }
            });

        } else {

            holder.binding.llReportItem.setAlpha(0.4f);

            holder.itemView.setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {

        ItemReportMenuBinding binding;

        public MenuViewHolder(ItemReportMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}