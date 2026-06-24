package acquire.core.trans.impl.print_detail.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.core.model.GroupByTranType;
import acquire.core.R;
import acquire.core.tools.CardInfoUtility;

public class SchemeGroupInnerTranTypeAdapter extends RecyclerView.Adapter<SchemeGroupInnerTranTypeAdapter.TranViewHolder> {

    private List<GroupByTranType> tranTypes;

    public SchemeGroupInnerTranTypeAdapter(List<GroupByTranType> tranTypes) {
        this.tranTypes = tranTypes;
    }

    @NonNull
    @Override
    public TranViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scheme_group_tran_type, parent, false);
        return new TranViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TranViewHolder holder, int position) {
        try{
            GroupByTranType tran = tranTypes.get(position);
            String useSign = (tran.getTranType() != null && tran.getTranType().toLowerCase().contains("void")) ? "-" : "";

            String title = tran.getTitle().toUpperCase();
            if(title==null || title.isEmpty()) title = tran.getTranType();
            holder.tvTranType.setText(title);
            holder.tvTranWiseTotalCount.setText(tran.getCount()+"");
            holder.tvTranWiseTotalAmount.setText(useSign+"TK "+formatAmount(tran.getAmount()));

            //TIP
            if(tran.getTipAmount()>0){
                try{
                    holder.tvTipTranType.setText("TIP");
                    holder.tvTipTranWiseTotalCount.setText(tran.getCount()+"");
                    holder.tvTipTranWiseTotalAmount.setText("TK "+formatAmount(tran.getTipAmount()));
                    holder.tvTranWiseTotalAmount.setText(useSign+"TK "+formatAmount(tran.getAmount() - tran.getTipAmount()));
                    holder.llTIPTotalScheme.setVisibility(View.VISIBLE);
                }catch (Exception ex){
                    holder.llTIPTotalScheme.setVisibility(View.GONE);
                    LoggerUtils.e("newCall TIP Amount in SchemeGroupInnerTranTypeAdapter: ");
                    LoggerUtils.e("newCall TIP Amount EXCEPTION: "+ex.getMessage());
                }
            }else {
                holder.llTIPTotalScheme.setVisibility(View.GONE);
            }
        }catch (Exception ex){
            LoggerUtils.d("newCall Child adapter ex: "+ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return tranTypes != null ? tranTypes.size() : 0;
    }

    private String formatAmount(long amount){

        return CardInfoUtility.formatAmount(amount);
//        return FormatUtils.formatAmount(amount);
//        return String.format("%.2f", amount/100.0);
    }

    static class TranViewHolder extends RecyclerView.ViewHolder {
        TextView tvTranType, tvTranWiseTotalCount, tvTranWiseTotalAmount;
        LinearLayout llTIPTotalScheme;
        TextView tvTipTranType, tvTipTranWiseTotalCount, tvTipTranWiseTotalAmount;

        public TranViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTranType = itemView.findViewById(R.id.tvTranTypeInner);
            tvTranWiseTotalCount = itemView.findViewById(R.id.tvTranWiseInnerTotalCount);
            tvTranWiseTotalAmount = itemView.findViewById(R.id.tvTranWiseInnerTotalAmount);
            //TIP
            llTIPTotalScheme =itemView.findViewById(R.id.llTIPTotalScheme);
            tvTipTranType = itemView.findViewById(R.id.tvTipTranTypeInner);
            tvTipTranWiseTotalCount = itemView.findViewById(R.id.tvTipTranWiseInnerTotalCount);
            tvTipTranWiseTotalAmount = itemView.findViewById(R.id.tvTipTranWiseInnerTotalAmount);
        }
    }
}