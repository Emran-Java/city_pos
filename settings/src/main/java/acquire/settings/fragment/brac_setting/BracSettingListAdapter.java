package acquire.settings.fragment.brac_setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.settings.models.BracSettingMenuItemModel;
import acquire.settings.R;
import acquire.settings.models.PrefKeyValType;

public class BracSettingListAdapter extends RecyclerView.Adapter<BracSettingListAdapter.ViewHolder> {
    private List<BracSettingMenuItemModel> list;
    private OnItemClickListener listener;
    private SwitchButtonClickListener switchButtonListener;

    public BracSettingListAdapter(List<BracSettingMenuItemModel> list) {
        this.list = list;
    }

    public interface OnItemClickListener {
        void onItemClick(BracSettingMenuItemModel item, int position);
    }

    public interface SwitchButtonClickListener {
        void onItemClick(BracSettingMenuItemModel item, int position, boolean isSwitchEnable);
    }

    public BracSettingListAdapter(List<BracSettingMenuItemModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setSwitchButtonListener(SwitchButtonClickListener switchButtonListener) {
        this.switchButtonListener = switchButtonListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings_sub_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtTitle.setText(list.get(position).getTitle());
        BracSettingMenuItemModel item = list.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });

        //LoggerUtils.i("newCall, Title: "+item.getTitle()+", Code: "+item.getCode());

       /* if(item.getCode()!=null && item.getCode().equalsIgnoreCase(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT)) {
            holder.switchButton.setChecked(ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT,false));
        }
        else if(item.getCode()!=null && item.getCode().equalsIgnoreCase(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP)) {

            holder.switchButton.setChecked(ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP,false));
        }*/

//        if(item.getTitle()!=null && item.getTitle().contains("Enable QR")){
        if (item.isSwitch()) {
            holder.switchButton.setVisibility(View.VISIBLE);
            holder.ivArrow.setVisibility(View.GONE);

            if (item.getPrefKeyValType() != null && item.getPrefKeyValType().equals(PrefKeyValType.STRING)) {

                String inVal = ParamsUtils.getString(item.getPrefKey(), "0");
                if (inVal.equals("1")) {
                    holder.switchButton.setChecked(true);
                } else if (inVal.equals("0")) {
                    holder.switchButton.setChecked(false);
                }

            } else if (item.getPrefKeyValType() != null && item.getPrefKeyValType().equals(PrefKeyValType.BOOLEAN)) {
                holder.switchButton.setChecked(ParamsUtils.getBoolean(item.getPrefKey(), false));
            }

            /*holder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                    if (item.getPrefKeyValType().equals(PrefKeyValType.STRING)) {

                        if (b) {
                            ParamsUtils.setString(item.getPrefKey(), "1");
                        } else {
                            ParamsUtils.setString(item.getPrefKey(), "0");
                        }

                    } else if (item.getPrefKeyValType().equals(PrefKeyValType.BOOLEAN)) {
//                        boolean sval = ParamsUtils.getBoolean(item.getPrefKey(), false);
                        ParamsUtils.setBoolean(item.getPrefKey(), b);
                        //holder.switchButton.setChecked(!ParamsUtils.getBoolean(item.getPrefKey(),false));
                    }

                }
            });
*/
            if(item.getPrefKey().equals(ParamsConst.PARAMS_KEY_SALE)){
                String paraSaleVal = ParamsUtils.getString(item.getPrefKey(), "0");
                LoggerUtils.d("ParamKey: : "+paraSaleVal );
            }

            holder.switchButton.setOnClickListener(v -> {

                //this.switchButtonListener.onItemClick(item, position, holder.switchButton.isChecked());

                if (item.getPrefKeyValType().equals(PrefKeyValType.STRING)) {
                    String inVal = ParamsUtils.getString(item.getPrefKey(), "0");
                    LoggerUtils.d("ParamKey: oldVal: "+inVal+", Key: "+item.getPrefKey());
                    if (inVal.equals("0")) {
                        // holder.switchButton.setChecked(true);
                        inVal = "1";
                        //ParamsUtils.setString(item.getPrefKey(), "1");
                    } else {
                        inVal="0";
                        // holder.switchButton.setChecked(false);
                    }
                    LoggerUtils.d("ParamKey: setNewVal: "+inVal+", Key: "+item.getPrefKey());
                    ParamsUtils.setString(item.getPrefKey(), inVal);
                } else if (item.getPrefKeyValType().equals(PrefKeyValType.BOOLEAN)) {

                    boolean sval = ParamsUtils.getBoolean(item.getPrefKey(), false);
                    ParamsUtils.setBoolean(item.getPrefKey(), !sval);
                    LoggerUtils.d("ParamKey: setVal: "+sval+", Key: "+item.getPrefKey());
                    //holder.switchButton.setChecked(!ParamsUtils.getBoolean(item.getPrefKey(),false));
                }
                this.switchButtonListener.onItemClick(item, position, holder.switchButton.isChecked());

            });

        } else {
            holder.switchButton.setVisibility(View.INVISIBLE);
            holder.ivArrow.setVisibility(View.VISIBLE);
        }

        if (item.getSubTitle() != null) {
            holder.tvMessage.setText(item.getSubTitle());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessage.setText("");
            holder.tvMessage.setVisibility(View.GONE);
        }

        holder.rlSwitch.setEnabled(list.get(position).isEnable());
        if (!list.get(position).isEnable()) {
            holder.rlSwitch.setAlpha(0.5f);
        } else {
            holder.rlSwitch.setAlpha(1.0f);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, tvMessage;
        SwitchMaterial switchButton;
        ImageView ivArrow;
        RelativeLayout rlSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            switchButton = itemView.findViewById(R.id.switch_button);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            rlSwitch = itemView.findViewById(R.id.rl_switch);
        }
    }
}
