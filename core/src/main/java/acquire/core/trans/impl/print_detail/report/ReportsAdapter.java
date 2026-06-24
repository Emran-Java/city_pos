package acquire.core.trans.impl.print_detail.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import acquire.base.utils.DateUtils;
import acquire.base.utils.FormatUtils;
import acquire.core.R;
import acquire.core.constant.OnUsBinMap;
import acquire.core.tools.CardInfoUtility;
import acquire.database.model.Record;
import acquire.sdk.emv.constant.EntryMode;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private List<Record> list;

    public ReportsAdapter(List<Record> list){
        this.list = list;
    }

    public void updateData(List<Record> newList) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_report,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Record recordData = list.get(position);

        if(!recordData.getTransType().equalsIgnoreCase("TestTxn")){
            String amountS = CardInfoUtility.formatAmount(recordData.getAmount());

            String amountText = "TK "+amountS;
            String apvlText = recordData.getAuthCode();
            if(apvlText==null) apvlText = recordData.getOrigAuthCode();

//            String tranTypeTitle = TransUtils.getName(recordData.getTransType());
            //String tranTypeTitle = recordData.getDisplayTitle();

            String tranTypeTitle = recordData.getDisplayTitle();
            if(tranTypeTitle==null || tranTypeTitle.isEmpty()) tranTypeTitle = recordData.getTransType();

            if(recordData.getTransType().equalsIgnoreCase("VoidPreAuth")){
                amountText = "-TK "+amountS;
                apvlText = "APVL- "+apvlText;
                //tranTypeTitle = "VOID PRE-AUTH";
            }
            else if(recordData.getTransType().contains("Void")) {
                amountText = "-TK "+amountS;
                apvlText = "APVL- "+recordData.getOrigAuthCode();
                //tranTypeTitle = "VOID SALE";
            }

            holder.tvTransType.setText(tranTypeTitle);
            holder.tvAmount.setText(amountText);
            holder.tvApvl.setText(apvlText);

            holder.tvCardNo.setText(FormatUtils.maskCardNo(recordData.getCardNo()));
            holder.tvInvoice.setText("INV- "+recordData.getTraceNo());
            holder.tvDate.setText("DATE: "+ DateUtils.formatOnlyDate(recordData.getDate()));
            holder.tvTime.setText("TIME: "+DateUtils.formatOnlyTime(recordData.getTime()));

            holder.tvScheme.setText("OFFUS "+recordData.getCardScheme());
            try{
                String ben = recordData.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if(cardTitle!=null && !cardTitle.isEmpty()){
                    holder.tvScheme.setText(cardTitle);
                }
            }catch (Exception ex){

            }
//            holder.tvExp.setText("EXP DATE: "+holder.cardUtil.formatExp(recordData.getExpDate()));
            holder.tvExp.setText("EXP DATE: "+"XX/XX");
//            holder.tvEntryMode.setText(holder.cardUtil.getEntryMode(recordData.getEntryMode()));
            holder.tvEntryMode.setText(EntryMode.getDescription(recordData.getEntryMode()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvTransType,tvAmount,tvCardNo,tvInvoice,
                tvDate,tvScheme,tvExp,tvApvl,tvTime,tvEntryMode;

        //CardInfoUtility cardUtil = CardInfoUtility.getInstance();
        public ViewHolder(@NonNull View v) {
            super(v);

            tvTransType = v.findViewById(R.id.tvTransType);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvCardNo = v.findViewById(R.id.tvCardNoVal);
            tvInvoice = v.findViewById(R.id.tvInvoice);
            tvDate = v.findViewById(R.id.tvDate);
            tvScheme = v.findViewById(R.id.tvScheme);
            tvExp = v.findViewById(R.id.tvExp);
            tvApvl = v.findViewById(R.id.tvApvl);
            tvTime = v.findViewById(R.id.tvTime);
            tvEntryMode = v.findViewById(R.id.tvEntryMode);
        }
    }
}