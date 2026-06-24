package acquire.core.fragment.input;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import acquire.core.R;

public class EmiMonthOptionAdapter extends RecyclerView.Adapter<EmiMonthOptionAdapter.ViewHolder> {

    private List<String> list;
    private int selectedPosition = -1;

    private OnMonthClickListener onMonthClickListener;

    public EmiMonthOptionAdapter(List<String> list, OnMonthClickListener onMonthClickListener) {
        this.list = list;
        this.onMonthClickListener = onMonthClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emi_installment_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvMonth.setText(list.get(position));

        // selection handling (optional)
        holder.itemView.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();

            if (onMonthClickListener != null) {
                onMonthClickListener.onMonthClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnMonthClickListener {
        void onMonthClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvNumberOfMonth);
        }
    }
}