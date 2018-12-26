package com.vitec.task.smartrule.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HeightUtils {

    /**
     * 将gridview或者Listview的高度设置为所有item加起来的高度
     * @param gridView
     */
    public static void setGridViewHeighBaseOnChildren(GridView gridView,int colNum) {
        if (gridView == null) {
            return;
        }
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        int count = listAdapter.getCount() / colNum;
        if (listAdapter.getCount() % colNum != 0) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();

        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }


    public static int setListViewHeighBaseOnChildren(ListView listView) {
        if (listView == null) {
            return 0;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }
        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem != null) {
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }


        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        listView.setLayoutParams(params);
        return totalHeight;
    }

    /**
     *
     * @param listView
     * @param maxItem
     */
    public static void setListViewHeighBaseOnChildren(ListView listView,int maxItem) {
        if (listView == null) {
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;

        for (int i = 0; i < maxItem; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem != null) {
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

}
