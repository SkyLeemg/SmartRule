package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.fragment.CheckDataFragment;
import com.vitec.task.smartrule.service.GetMudelIntentService;

import java.util.ArrayList;
import java.util.List;

public class MainHomeActivity extends BaseActivity implements View.OnClickListener{

    private GridView gvManager;
    private List<GvItem> itemList;
    private GvManagerAdapter managerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        requestLocationPermissions();
        initView();
        initData();
    }

    private void initData() {
        itemList = new ArrayList<>();
        itemList.add(new GvItem(R.mipmap.icon_main_add, "新建测量"));
        itemList.add(new GvItem(R.mipmap.icon_main_record, "测量记录"));
        itemList.add(new GvItem(R.mipmap.icon_main_dev, "设备管理"));

        Intent intent = new Intent(this, GetMudelIntentService.class);
        startService(intent);

        managerAdapter = new GvManagerAdapter();
        gvManager.setAdapter(managerAdapter);

        gvManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0://查询测量数据
                        startActivity(new Intent(MainHomeActivity.this,ChooseMeasureMsgActivity.class));
                        break;
                    case 1://查询文件

                        break;
                    case 2:
                        startActivity(new Intent(MainHomeActivity.this,DeviceManagerActivity.class));
                        break;

                }
            }
        });


    }

    private void initView() {
        initWidget();
        setImgSource(R.mipmap.icon_user_unselect, R.mipmap.icon_user_unselect);
        imgIcon.setVisibility(View.VISIBLE);
        setTvTitle("自动测量靠尺");

        gvManager = findViewById(R.id.gv_mng_list);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.img_icon_toolbar:

                break;
        }
    }


    class GvManagerAdapter extends BaseAdapter {
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
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.item_gridview_for_home_activity, null);
                holder = new ViewHolder();
                holder.imageView = view.findViewById(R.id.img_item_dev_pic);
                holder.tvTitle = view.findViewById(R.id.tv_item_dev_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvTitle.setText(itemList.get(i).getTitle());
            holder.imageView.setImageResource(itemList.get(i).getImgResouse());
            return view;
        }
    }

    class ViewHolder {
        ImageView imageView;
        TextView tvTitle;
    }



    class GvItem {
        int imgResouse;
        String title;

        public GvItem(int imgResouse, String title) {
            this.imgResouse = imgResouse;
            this.title = title;
        }

        public int getImgResouse() {
            return imgResouse;
        }

        public void setImgResouse(int imgResouse) {
            this.imgResouse = imgResouse;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "GvItem{" +
                    "imgResouse=" + imgResouse +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}