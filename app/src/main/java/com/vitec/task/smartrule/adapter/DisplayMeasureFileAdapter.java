package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.vitec.task.smartrule.R;

import java.io.File;
import java.util.List;

public class DisplayMeasureFileAdapter extends BaseAdapter {

    private List<File> fileList;
    private Context context;
    private boolean isShowCheckBox = false;//是否显示checkbox控件
    private boolean isAllChecked = false;//是否全选
    private MeasureProjectListAdapter.OnChecked checked;

    public DisplayMeasureFileAdapter(Context context, List<File> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int i) {
        return fileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_listview_measure_file, null);
            holder = new ViewHolder();
            holder.checkBox = view.findViewById(R.id.cb_item);
            holder.textView = view.findViewById(R.id.tv_item_file_name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.textView.setText(fileList.get(i).getName());
        if (isShowCheckBox) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        if (!isAllChecked) {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checked.onCheckedChanged(i,b);
            }
        });

        return view;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    public boolean isShowCheckBox() {
        return isShowCheckBox;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        isShowCheckBox = showCheckBox;
    }

    public boolean isAllChecked() {
        return isAllChecked;
    }

    public void setAllChecked(boolean allChecked) {
        isAllChecked = allChecked;
    }

    public MeasureProjectListAdapter.OnChecked getChecked() {
        return checked;
    }

    public void setChecked(MeasureProjectListAdapter.OnChecked checked) {
        this.checked = checked;
    }

    class ViewHolder {
        CheckBox checkBox;
        TextView textView;
    }

}
