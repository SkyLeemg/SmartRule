package com.vitec.task.smartrule.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.BaseActivity;
import com.vitec.task.smartrule.activity.SearchMeasureDataActivity;

import java.util.ArrayList;
import java.util.List;

public class CheckDataFragment extends Fragment {

    private View view;
    private GridView gvManager;
    private List<GvItem> itemList;
    private GvManagerAdapter managerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_check_data,null);
        intView();
        intData();
        return view;

    }

    private void intData() {
        itemList = new ArrayList<>();
        itemList.add(new GvItem(R.mipmap.icon_search, "查询测量记录"));
        itemList.add(new GvItem(R.mipmap.icon_mananger, "管理测量文件"));

        managerAdapter = new GvManagerAdapter();
        gvManager.setAdapter(managerAdapter);

        gvManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0://查询测量数据
                        startActivity(new Intent(getActivity(),SearchMeasureDataActivity.class));
                        break;
                    case 1://查询文件

                        break;
                }
            }
        });

    }

    private void intView() {
        gvManager = view.findViewById(R.id.gv_mng_list);
    }

    class GvManagerAdapter extends BaseAdapter{
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
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.item_gridview_dev_manager, null);
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


