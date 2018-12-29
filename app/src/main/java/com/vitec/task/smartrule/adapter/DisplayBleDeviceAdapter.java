package com.vitec.task.smartrule.adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.BleDevice;
import com.vitec.task.smartrule.db.BleDeviceDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.interfaces.IDevManager;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;

import java.util.List;

/**
 * 靠尺设备管理界面  显示靠尺的Adapter
 *
 * item为上面靠尺图片+下面设备名
 */
public class DisplayBleDeviceAdapter extends BaseAdapter{

    private List<BleDevice> devs;
    private Context context;
    private IDevManager devManager;

    public DisplayBleDeviceAdapter(Context context, List<BleDevice> devs) {
        this.context = context;
        this.devs = devs;
    }
    public DisplayBleDeviceAdapter(Context context, List<BleDevice> devs, IDevManager devManager) {
        this.context = context;
        this.devs = devs;
        this.devManager = devManager;
    }

    @Override
    public int getCount() {
        return devs.size()+1;
    }

    @Override
    public Object getItem(int i) {
        if (i < devs.size()) {
            return devs.get(i);
        } else {
            return i;
        }

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);
        if (view == null) {
            view = inflater.inflate(R.layout.item_gridview_dev_manager, null);
            holder = new ViewHolder();
            holder.imgDev = view.findViewById(R.id.img_item_dev_pic);
            holder.tvDevName = view.findViewById(R.id.tv_item_dev_name);
            holder.tvEditor = view.findViewById(R.id.tv_edit);
            holder.llDisplayDev = view.findViewById(R.id.ll_display_dev);
            holder.rlAddDev = view.findViewById(R.id.ll_add_dev);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (i == devs.size()) {
            holder.rlAddDev.setVisibility(View.VISIBLE);
            holder.llDisplayDev.setVisibility(View.GONE);
            return view;
        }
        holder.llDisplayDev.setVisibility(View.VISIBLE);
        holder.rlAddDev.setVisibility(View.GONE);
        holder.imgDev.setImageResource(devs.get(i).getImgResouce());
        if (devs.get(i).getBleAlias() != null && !devs.get(i).getBleAlias().equals("")) {
            holder.tvDevName.setText(devs.get(i).getBleAlias());
        } else {
            holder.tvDevName.setText("未命名");
        }

        holder.tvEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                上面放一张图片
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(devs.get(i).getImgResouce());
                imageView.setMaxWidth(ScreenSizeUtil.getScreenWidth(context)/3);
//                将图片添加到垂直的ll中
                LinearLayout llV = new LinearLayout(context);
                llV.setOrientation(LinearLayout.VERTICAL);
                llV.setGravity(Gravity.CENTER);
                llV.addView(imageView);
//                添加一个水平的ll，在里面添加设备名称，之后添加到垂直的ll中
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView textView = new TextView(context);
                textView.setText("设备名称：");
                final EditText editText = new EditText(context);
                editText.setText(devs.get(i).getBleAlias());
                editText.setWidth(ScreenSizeUtil.getScreenWidth(context)/2);
                linearLayout.addView(textView);
                linearLayout.addView(editText);
                linearLayout.setGravity(Gravity.CENTER);

                llV.addView(linearLayout);
                builder.setView(llV);
                builder.setTitle("编辑" + devs.get(i).getBleAlias());
                builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        ContentValues values = new ContentValues();
                        values.put(DataBaseParams.ble_alias,editText.getText().toString().trim());
                        BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(context);
                        if (bleDeviceDbHelper.updateDevice(values, new String[]{String.valueOf(devs.get(i).getId())})) {
                            Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                            devs.get(i).setBleAlias(editText.getText().toString().trim());
//                            更新列表数据
                            if (devManager != null) {
                                devManager.setDevs(devs);
                            }

                        } else {
                            Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                builder.setNegativeButton("删除设备", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        BleDeviceDbHelper bleDeviceDbHelper = new BleDeviceDbHelper(context);
                        String id = String.valueOf(devs.get(i).getId());
                        if (bleDeviceDbHelper.delDevice(new String[]{id})) {
                            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                            devs.remove(i);
//                            更新列表数据
                            if (devManager != null) {
                                devManager.setDevs(devs);
                            }
                        } else {
                            Toast.makeText(context,"删除失败",Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                builder.show();
            }
        });


        return view;
    }

    public List<BleDevice> getDevs() {
        return devs;
    }

    public void setDevs(List<BleDevice> devs) {
        this.devs = devs;
    }

    class ViewHolder {
        TextView tvDevName;
        TextView tvEditor;
        ImageView imgDev;
        LinearLayout llDisplayDev;
        RelativeLayout rlAddDev;

    }
}
