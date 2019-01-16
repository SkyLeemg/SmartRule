package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.UseInstructionActivity;
import com.vitec.task.smartrule.bean.IntructionItem;

import java.util.List;

public class UseIntructionAdapter extends BaseAdapter {

    private List<IntructionItem> itemList;
    private Context context;

    public UseIntructionAdapter(Context context, List<IntructionItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemList.get(i);
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
            view = inflater.inflate(R.layout.item_list_view_use_instruction, null);
            holder = new ViewHolder();
            holder.tvContent = view.findViewById(R.id.tv_content);
            holder.tvId = view.findViewById(R.id.tv_id);
            holder.tvTitile = view.findViewById(R.id.tv_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvTitile.setText(itemList.get(i).getTitle());
        holder.tvId.setText(i + 1 + "");
        holder.tvContent.setText(itemList.get(i).getIntruction());
        return view;
    }

    class ViewHolder {
        TextView tvId;
        TextView tvTitile;
        TextView tvContent;
    }


}
