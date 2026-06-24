package acquire.core.fragment.common.submenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import acquire.core.model.FeatureSubMenuModel;

import acquire.core.R;
//import com.zztl.pos.city.R;
//import acquire.app.brac.models.ReportModel;

public class CoreSubMenuAdapter
        extends RecyclerView.Adapter<CoreSubMenuAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FeatureSubMenuModel model);
    }

    private List<FeatureSubMenuModel> list;
    private OnItemClickListener listener;

    public CoreSubMenuAdapter(List<FeatureSubMenuModel> list,
                              OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_menu, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {


        FeatureSubMenuModel model = list.get(position);
        /*if(model.isHasChild() && model.getChildData().size()>0){
            holder.itemView.setOnClickListener(v ->
                    listener.onItemClick(model));
        }
        */

        if(model.isShow()){
            holder.itemView.setVisibility(View.VISIBLE);
        }else {
            holder.itemView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(model));

        holder.text.setText(String.format("%d.%s", position + 1, model.getTitle()));


        String iconName = model.getIcon();

        int resId = holder.itemIcon.getContext()
                .getResources()
                .getIdentifier(iconName, "drawable",
                        holder.itemIcon.getContext().getPackageName());

        if (resId != 0) {
            holder.itemIcon.setImageResource(resId);
        } else {
           // holder.icon.setImageResource(R.drawable.ic_default_report);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView itemIcon;
        LinearLayout llReportItem;
        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            text = itemView.findViewById(R.id.txtTitle);
            itemIcon= itemView.findViewById(R.id.imgIcon);
            llReportItem= itemView.findViewById(R.id.llReportItem);
        }
    }
}