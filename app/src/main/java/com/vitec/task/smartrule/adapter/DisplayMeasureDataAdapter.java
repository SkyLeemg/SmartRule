package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.fragment.MeasureFragment;

import java.util.List;

/**
 * 显示测量数据的Adapter
 */
public class DisplayMeasureDataAdapter extends BaseAdapter {
    private Context context;
    private List<RulerCheckOptionsData> checkOptionsDataList;

    public DisplayMeasureDataAdapter(Context context, List<RulerCheckOptionsData> checkOptionsDataList) {
        this.context = context;
        this.checkOptionsDataList = checkOptionsDataList;
    }

    @Override
    public int getCount() {
        return checkOptionsDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return checkOptionsDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_gridview_measure_data, null);
            holder = new ViewHolder();
            holder.etData = view.findViewById(R.id.et_measure_data);
            holder.tvContent = view.findViewById(R.id.tv_measure_content);
            holder.tvTitleIndex = view.findViewById(R.id.tv_title_index);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.etData.setText(checkOptionsDataList.get(i).getData());
        holder.tvContent.setText(checkOptionsDataList.get(i).getData());
        if (checkOptionsDataList.get(i).getNumber() != 0) {
            holder.tvTitleIndex.setText(checkOptionsDataList.get(i).getNumber()+"");
        }else  holder.tvTitleIndex.setText("");

        if (checkOptionsDataList.get(i).isQualified()) {
            holder.tvContent.setTextColor(Color.rgb(51,51,51));
        }else {
            holder.tvContent.setTextColor(Color.RED);
        }
//
        if (checkOptionsDataList.get(i).getRulerCheckOptions().getRulerOptions().getType() == 1 || checkOptionsDataList.get(i).getRulerCheckOptions().getRulerOptions().getType() == 2) {
            holder.etData.setEnabled(false);
        } else {
            holder.etData.setEnabled(true);
        }
        return view;
    }



    class ViewHolder {
        EditText etData;
        TextView tvTitleIndex;
        TextView tvContent;
    }


}
