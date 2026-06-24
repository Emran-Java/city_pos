package acquire.core.trans.impl.print_detail.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import acquire.core.model.SchemeGroup;
import acquire.core.R;
import acquire.core.tools.CardInfoUtility;


public class SchemeGroupAdapter extends RecyclerView.Adapter<SchemeGroupAdapter.SchemeViewHolder> {

    private List<SchemeGroup> schemeGroups;

    public SchemeGroupAdapter(List<SchemeGroup> schemeGroups) {
        this.schemeGroups = schemeGroups;
    }

    public void updateData(List<SchemeGroup> newList) {
        this.schemeGroups.clear();

        if (newList != null) {
            this.schemeGroups.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scheme_group_card_report, parent, false);
        return new SchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        SchemeGroup scheme = schemeGroups.get(position);

        holder.tvSchemeTitle.setText(scheme.getSchemeTitle().toUpperCase()+": ");
        holder.tvTotalCount.setText(scheme.getTotalSchemeWiseTranCount()+"");

        String useSign = scheme.getTotalSchemeWiseTranAmount()<0 ? "-" : "";
        //holder.tvTotalAmount.setText(scheme.getTotalSchemeWiseTranAmount());
        holder.tvTotalAmount.setText(useSign+"TK "+formatAmount(scheme.getTotalSchemeWiseTranAmount()));

        // Setup Inner RecyclerView
        SchemeGroupInnerTranTypeAdapter childAdapter = new SchemeGroupInnerTranTypeAdapter(scheme.getGroupByTranType());
        holder.rvCardScheme.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvCardScheme.setAdapter(childAdapter);
    }

    @Override
    public int getItemCount() {
        return schemeGroups != null ? schemeGroups.size() : 0;
    }

    private String formatAmount(long amount){

        return CardInfoUtility.formatAmount(amount);
//        return FormatUtils.formatAmount(amount);
//        return String.format("%.2f", amount/100.0);
    }

    static class SchemeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchemeTitle, tvTotalCount, tvTotalAmount;
        RecyclerView rvCardScheme;

        public SchemeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchemeTitle = itemView.findViewById(R.id.tvSchemeTitle);
            tvTotalCount = itemView.findViewById(R.id.tvTotalCount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount2);
            rvCardScheme = itemView.findViewById(R.id.rvCardScheme);
        }
    }
}