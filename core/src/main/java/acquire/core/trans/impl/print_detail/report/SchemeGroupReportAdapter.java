package acquire.core.trans.impl.print_detail.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import acquire.core.R;
import acquire.core.model.CardSchemeReportModel;
import acquire.core.tools.CardInfoUtility;

public class SchemeGroupReportAdapter extends RecyclerView.Adapter<SchemeGroupReportAdapter.Holder>{

    private List<CardSchemeReportModel> list;

    public SchemeGroupReportAdapter(List<CardSchemeReportModel> list){
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_scheme_report,parent,false);

        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {

        CardSchemeReportModel cardSchemeObj = list.get(position);

        if(cardSchemeObj.getTransType()!=null) {
            if (cardSchemeObj.getTransType().equalsIgnoreCase("Sale")) {
                h.trSale.setVisibility(View.VISIBLE);
            } else {
                h.trSale.setVisibility(View.GONE);
            }
            if (cardSchemeObj.getTransType().equalsIgnoreCase("VoidSale")) {
                h.trVoid.setVisibility(View.VISIBLE);
            } else {
                h.trVoid.setVisibility(View.GONE);
            }
            if (cardSchemeObj.getTransType().equalsIgnoreCase("VoidSale")) {
                h.trVoid.setVisibility(View.VISIBLE);
            } else {
                h.trVoid.setVisibility(View.GONE);
            }
            if (cardSchemeObj.getTransType().equalsIgnoreCase("AuthComplete")) {
                h.trPreAuthCmplt.setVisibility(View.VISIBLE);
            } else {
                h.trPreAuthCmplt.setVisibility(View.GONE);
            }
            if (cardSchemeObj.getTransType().equalsIgnoreCase("VoidPreAuth")) {
                h.trPreAuthVoidCmplt.setVisibility(View.VISIBLE);
            } else {
                h.trPreAuthVoidCmplt.setVisibility(View.GONE);
            }
        }
        h.tvScheme.setText(cardSchemeObj.getScheme()+":");

        h.tvTranTypeSale.setText(cardSchemeObj.getTranSaleTitle());
        h.tvTranTypeVoid.setText(cardSchemeObj.getTranVoidTitle());

        h.tvSaleCount.setText(String.valueOf(cardSchemeObj.getSaleCount()));
        h.tvSaleAmount.setText("TK "+formatAmount(cardSchemeObj.getSaleAmount()));

        h.tvVoidCount.setText(String.valueOf(cardSchemeObj.getVoidCount()));
        h.tvVoidAmount.setText("-TK "+formatAmount(cardSchemeObj.getVoidAmount()));

        int totalCount = cardSchemeObj.getSaleCount();
        long totalAmount = cardSchemeObj.getSaleAmount();

        h.tvTotalCount.setText(String.valueOf(totalCount));
        h.tvTotalAmount.setText("TK "+formatAmount(totalAmount));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TableRow trSale, trVoid, trPreAuthCmplt, trPreAuthVoidCmplt, trTipAdjust, trVoidTip, trTotal;

        TextView tvScheme;

        TextView tvSaleCount,tvSaleAmount;
        TextView tvVoidCount,tvVoidAmount;
        TextView tvTotalCount,tvTotalAmount;
        TextView tvTranTypeSale,tvTranTypeVoid;

        public Holder(@NonNull View v) {
            super(v);

            trSale = v.findViewById(R.id.trSale);
            trSale.setVisibility(View.GONE);
            trVoid = v.findViewById(R.id.trVoid);
            trVoid.setVisibility(View.GONE);
            trPreAuthCmplt = v.findViewById(R.id.trPreAuthCmplt);
            trPreAuthCmplt.setVisibility(View.GONE);
            trPreAuthVoidCmplt = v.findViewById(R.id.trPreAuthVoidCmplt);
            trPreAuthVoidCmplt.setVisibility(View.GONE);
            trTipAdjust = v.findViewById(R.id.trTipAdjust);
            trTipAdjust.setVisibility(View.GONE);
            trVoidTip = v.findViewById(R.id.trVoidTip);
            trVoidTip.setVisibility(View.GONE);
            trTotal = v.findViewById(R.id.trTotal);
            //trTotal.setVisibility(View.GONE);

            tvScheme = v.findViewById(R.id.tvSchemeName);

            tvSaleCount = v.findViewById(R.id.tvSaleCount);
            tvSaleAmount = v.findViewById(R.id.tvSaleAmount);

            tvVoidCount = v.findViewById(R.id.tvVoidCount);
            tvVoidAmount = v.findViewById(R.id.tvVoidAmount);

            tvTotalCount = v.findViewById(R.id.tvTotalCount);
            tvTotalAmount = v.findViewById(R.id.tvTotalAmount);

            tvTranTypeSale = v.findViewById(R.id.tvTranTypeSale);
            tvTranTypeVoid = v.findViewById(R.id.tvTranTypeVoid);
        }
    }


    private String formatAmount(long amount){

        return CardInfoUtility.formatAmount(amount);
//        return FormatUtils.formatAmount(amount);
//        return String.format("%.2f", amount/100.0);
    }

}