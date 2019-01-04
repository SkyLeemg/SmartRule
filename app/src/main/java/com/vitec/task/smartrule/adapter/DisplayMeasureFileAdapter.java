package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.interfaces.IClickable;

import java.io.File;
import java.util.List;

public class DisplayMeasureFileAdapter extends BaseAdapter {

    private List<File> fileList;
    private Context context;
    private boolean isShowCheckBox = false;//是否显示checkbox控件
    private boolean isAllChecked = false;//是否全选
    private MeasureProjectListAdapter.OnChecked checked;
    private IClickable iClickable;

    public DisplayMeasureFileAdapter(Context context, List<File> fileList,IClickable iClickable) {
        this.context = context;
        this.fileList = fileList;
        this.iClickable = iClickable;
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
        final ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_listview_measure_file, null);
            holder = new ViewHolder();
            holder.checkBox = view.findViewById(R.id.cb_item);
            holder.textView = view.findViewById(R.id.tv_item_file_name);
            holder.btnDelete = view.findViewById(R.id.btn_delete);
            holder.btnShare = view.findViewById(R.id.btn_share);
            holder.swipeMenuLayout = view.findViewById(R.id.swipe_menu_layout);
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
//        复选框
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checked.onCheckedChanged(i,b);
            }
        });
        holder.swipeMenuLayout.quickClose();
//        分享按钮
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickable.onFirstClickable(i);
            }
        });

//        删除按钮
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickable.onSencondClickable(i);

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
        SwipeMenuLayout swipeMenuLayout;
        CheckBox checkBox;
        TextView textView;
        Button btnShare;
        Button btnDelete;
    }

}
