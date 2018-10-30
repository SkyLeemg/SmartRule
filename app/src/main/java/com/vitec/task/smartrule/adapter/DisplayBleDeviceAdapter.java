package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.BleDevice;

import java.util.List;

/**
 * 靠尺设备管理界面  显示靠尺的Adapter
 *
 * item为上面靠尺图片+下面设备名
 */
public class DisplayBleDeviceAdapter extends BaseAdapter{

    private List<BleDevice> devs;
    private Context context;

    public DisplayBleDeviceAdapter(Context context, List<BleDevice> devs) {
        this.context = context;
        this.devs = devs;
    }

    @Override
    public int getCount() {
        return devs.size();
    }

    @Override
    public Object getItem(int i) {
        return devs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);
        if (view == null) {
            view = inflater.inflate(R.layout.item_gridview_dev_manager, null);
            holder = new ViewHolder();
            holder.imgDev = view.findViewById(R.id.img_item_dev_pic);
            holder.tvDevName = view.findViewById(R.id.tv_item_dev_name);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.imgDev.setImageResource(R.mipmap.rule);
        holder.tvDevName.setText(devs.get(i).getBleName());

        return view;
    }

    class ViewHolder {
        TextView tvDevName;
        ImageView imgDev;

    }
}
