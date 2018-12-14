package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.vitec.task.smartrule.R;

import java.util.List;

public class MySpinnerAdapter extends BaseAdapter{

    private List<String> dataList;
    private Context context;

    public MySpinnerAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_spinner, null);
            holder = new ViewHolder();
            holder.tvItem = view.findViewById(R.id.tv_item_spinner);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvItem.setText(dataList.get(i));
        return view;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }

    class ViewHolder {
        TextView tvItem;
    }
}

