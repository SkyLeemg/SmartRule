package com.vitec.task.smartrule.view.large_img;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.interfaces.ISelectorResultCallBack;
import com.vitec.task.smartrule.utils.HeightUtils;

import java.util.List;

public class SelectorBottomDialog extends Dialog implements View.OnClickListener{

    private Button cancel;
    private Activity activity;
    private Context context;
    private ListView lvSelectorContent;
    private List<DataRes> datalist;
    private SelectAdapter selectAdapter;
    private ISelectorResultCallBack selectorResultCallBack;

    public SelectorBottomDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public SelectorBottomDialog(@NonNull Context context, int themeResId, List<DataRes> datalist) {
        super(context, themeResId);
        this.datalist = datalist;
        this.context = context;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bottom_selector);
        //获取当前Activity所在的窗体
        Window dialogWindow = getWindow();
        if(dialogWindow == null)
        { return; }
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.x = 0;
//        lp.y = 20;
        //设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);

        lvSelectorContent = findViewById(R.id.lv_dialog_selector_list);
        cancel = findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(this);
        if (datalist != null) {
            selectAdapter = new SelectAdapter();
            lvSelectorContent.setAdapter(selectAdapter);
            if (datalist.size() < 5) {
                int height = HeightUtils.setListViewHeighBaseOnChildren(lvSelectorContent);
                ViewGroup.LayoutParams params = lvSelectorContent.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
                params.height = height;
                lvSelectorContent.setLayoutParams(params);
            } else {
                HeightUtils.setListViewHeighBaseOnChildren(lvSelectorContent,5);
            }
        }

        lvSelectorContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectorResultCallBack != null) {
                    selectorResultCallBack.onSelectCallBack(datalist.get(i).getData(), i);
                }
            }
        });

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }




    public ISelectorResultCallBack getSelectorResultCallBack() {
        return selectorResultCallBack;
    }

    public void setSelectorResultCallBack(ISelectorResultCallBack selectorResultCallBack) {
        this.selectorResultCallBack = selectorResultCallBack;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            /**
             * 取消
             */
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    class SelectAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return datalist.size();
        }

        @Override
        public Object getItem(int i) {
            return datalist.get(i);
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
                view = inflater.inflate(R.layout.item_dialog_bottom_select, null);
                holder = new ViewHolder();
                holder.tvContent = view.findViewById(R.id.tv_item_dialog_selector_content);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvContent.setText(datalist.get(i).getData());
            return view;
        }
    }

    class ViewHolder {
        TextView tvContent;
    }


    public static class DataRes {
        int id;
        String data;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "DataRes{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    '}';
        }
    }



}
