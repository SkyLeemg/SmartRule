package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.interfaces.IClickable;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import java.util.List;

public class MeasureProjectListAdapter extends BaseAdapter {

    private Context context;
    private List<RulerCheck> rulerCheckList;
    private int current_id;//当前正在连接ruler_check_id，-1代表不显示测量状态，0为暂停测量，>0为正在测量的ruler_check_id显示为测量中
    private boolean isShowCheckBox = false;//是否显示checkbox控件
    private boolean isAllChecked = false;//是否全选
    private OnChecked checked;
    private int[] bgColors = {Color.rgb(53,129,251), Color.rgb(250,92,92),
            Color.rgb(254,207,27),Color.rgb(55,184,119)};
    private int clickIndex = -1;//被点击的item的序号，有则记录该序号，无则置为-1
    private IClickable iClickable;

    public MeasureProjectListAdapter(Context context, List<RulerCheck> rulerCheckList,int current_id) {
        this.context = context;
        this.rulerCheckList = rulerCheckList;
        this.current_id = current_id;
    }

    @Override
    public int getCount() {
        return rulerCheckList.size();
    }

    @Override
    public Object getItem(int i) {
        return rulerCheckList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

//    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_list_view_wait_measure, null);
            holder = new ViewHolder();
            holder.tvCheckPerson = view.findViewById(R.id.tv_item_check_person);
            holder.tvCheckPosition = view.findViewById(R.id.tv_item_position);
            holder.tvCheckTime = view.findViewById(R.id.tv_item_check_time);
            holder.tvProjectName = view.findViewById(R.id.tv_item_project_name);
            holder.tvProjectStatus = view.findViewById(R.id.tv_item_check_status);
            holder.cbItem = view.findViewById(R.id.cb_item);
            holder.llMenu = view.findViewById(R.id.ll_menu);
            holder.tvIconWord = view.findViewById(R.id.tv_icon_word);
            holder.rlIconBg = view.findViewById(R.id.rl_icon);
            holder.llBegin = view.findViewById(R.id.ll_begin);
            holder.llDel = view.findViewById(R.id.ll_del);
            holder.llEdit = view.findViewById(R.id.ll_edit);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        String projectName = rulerCheckList.get(i).getProject().getProjectName();
        if (projectName==null || projectName.equals("")) {
            projectName = "未命名";
        }
        holder.tvProjectName.setText(projectName);
        holder.tvCheckPosition.setText("检查位置："+rulerCheckList.get(i).getUnitEngineer().getLocation()+" "+rulerCheckList.get(i).getCheckFloor());
        holder.tvCheckPerson.setText("检查人："+rulerCheckList.get(i).getUser().getUserName());
        holder.llMenu.setVisibility(View.GONE);
//        设置图标的头文字
        holder.tvIconWord.setText(projectName.substring(0, 1));
//        设置图标的背景颜色

        int colorIndex = Math.abs((projectName.hashCode()+rulerCheckList.get(i).getCheckFloor().hashCode()) % 4);
//        LogUtils.show("查看颜色编号：" + colorIndex);
        if (colorIndex < bgColors.length) {
            holder.rlIconBg.setBackgroundColor(bgColors[colorIndex]);
        } else {
            holder.rlIconBg.setBackgroundColor(Color.rgb(53,129,251));
        }


//        LogUtils.show("获取到的createtime时间戳："+rulerCheckList.get(i).getCreateTime());
        String startTime = DateFormatUtil.stampToDateString(rulerCheckList.get(i).getCreateTime());
//        判断是否显示checkbox控件
        if (isShowCheckBox) {
            holder.cbItem.setVisibility(View.VISIBLE);
        } else {
            holder.cbItem.setVisibility(View.GONE);
        }
//        判断checkbox是否全选
        if (isAllChecked) {
            holder.cbItem.setChecked(true);
        } else {
            holder.cbItem.setChecked(false);
        }

        String endTime = "";
        if (rulerCheckList.get(i).getUpdateTime() != 0) {
            endTime = DateFormatUtil.stampToDateString(rulerCheckList.get(i).getUpdateTime());
        }
        holder.tvCheckTime.setText("检查时间："+startTime+" - "+endTime);
        if (current_id < 0) {
            holder.tvProjectStatus.setVisibility(View.GONE);
        } else if (current_id == 0) {
            holder.tvProjectStatus.setText("暂停测量");
            holder.tvProjectStatus.setTextColor(Color.rgb(234,160,0));
        } else if (current_id > 0) {
            if (rulerCheckList.get(i).getId() == current_id) {
                holder.tvProjectStatus.setText("正在测量...");
                holder.tvProjectStatus.setTextColor(Color.rgb(53,129,251));
            } else {
                holder.tvProjectStatus.setText("暂停测量");
                holder.tvProjectStatus.setTextColor(Color.rgb(234,160,0));
            }
        }
//        监听复选框的点击事件
        holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checked != null) {
                    checked.onCheckedChanged(i,b);
                }
            }
        });

        /**设置是否显示菜单*/
        if (clickIndex == i) {
            holder.llMenu.setVisibility(View.VISIBLE);
        } else {
            holder.llMenu.setVisibility(View.GONE);
        }
        holder.llBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iClickable != null) {
                    iClickable.onFirstClickable(i);
                    clickIndex = -1;
                }

            }
        });
        holder.llEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickable.onSencondClickable(i);
            }
        });

        holder.llDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickable.onThirdClickable(i);
            }
        });

        return view;
    }

    public IClickable getiClickable() {
        return iClickable;
    }

    public void setiClickable(IClickable iClickable) {
        this.iClickable = iClickable;
    }

    public int getClickIndex() {
        return clickIndex;
    }

    public void setClickIndex(int clickIndex) {
        this.clickIndex = clickIndex;
    }

    public OnChecked getChecked() {
        return checked;
    }

    public void setChecked(OnChecked checked) {
        this.checked = checked;
    }


    public static interface OnChecked {
        public void onCheckedChanged(int position, boolean ishChecked);
    }

    public boolean isAllChecked() {
        return isAllChecked;
    }

    public void setAllChecked(boolean allChecked) {
        isAllChecked = allChecked;
    }

    public List<RulerCheck> getRulerCheckList() {
        return rulerCheckList;
    }

    public void setRulerCheckList(List<RulerCheck> rulerCheckList) {
        this.rulerCheckList = rulerCheckList;
    }

    public int getCurrent_id() {
        return current_id;
    }

    public void setCurrent_id(int current_id) {
        this.current_id = current_id;
    }

    public boolean isShowCheckBox() {
        return isShowCheckBox;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        isShowCheckBox = showCheckBox;
    }

    class ViewHolder {
        TextView tvProjectName;
        TextView tvCheckPosition;
        TextView tvCheckPerson;
        TextView tvCheckTime;
        TextView tvProjectStatus;
        TextView tvIconWord;
        CheckBox cbItem;
        LinearLayout llMenu;
        RelativeLayout rlIconBg;
        LinearLayout llEdit;
        LinearLayout llBegin;
        LinearLayout llDel;

    }

}
