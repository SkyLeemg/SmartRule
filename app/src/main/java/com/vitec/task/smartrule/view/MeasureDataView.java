package com.vitec.task.smartrule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasureDataView extends RelativeLayout {
    private Context context;
    private ImageView imgTitle;
    private TextView tvTitle;
    private TextView tvQualifiedStandard;//合格标准
    private GridView gvDisplayData;//展示测量数据的Gridview
    private TextView tvRealMeasureNum;//实测点数
    private TextView tvQualifiedNum;//合格点数
    private TextView tvQualifiedRate;//合格率
    private BaseAdapter adapter;

    private RulerCheckOptions rulerCheckOptions;//从界面中传递过来的rulerchekcoption
    private RulerCheckOptionsData optionsDataMudel;//为一个空的data模板

    private List<OptionMeasure> optionMeasures;//该管控要点可选的层高，还要测量数据标准都在这里
    private OptionMeasure optionMeasure;//上面是该管控要点所有的层高，这个是用户当前选择的层高



    public MeasureDataView(Context context) {
        super(context);
        this.context = context;
        initView();
    }
    public MeasureDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public MeasureDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.item_common_display_measure_data,this);
        imgTitle = findViewById(R.id.img_ver_title);
        tvTitle = findViewById(R.id.tv_title);
        tvQualifiedStandard = findViewById(R.id.tv_qualified_standard);
        gvDisplayData = findViewById(R.id.gv_vertical_measure_data);
        tvRealMeasureNum = findViewById(R.id.tv_real_measure_num);
        tvQualifiedNum = findViewById(R.id.tv_qualified_num);
        tvQualifiedRate = findViewById(R.id.tv_qualified_rate);
    }

    public void initData(RulerCheckOptions rulerCheckOptions) {
        this.rulerCheckOptions = rulerCheckOptions;
//        初始化数据模板
        optionsDataMudel = new RulerCheckOptionsData();
        optionsDataMudel.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
        optionsDataMudel.setRulerCheckOptions(rulerCheckOptions);
//        初始化计算标准的模板信息
        optionMeasures = new ArrayList<>();
        if (rulerCheckOptions.getRulerOptions().getType() == 1) {

        }
        final String measures = rulerCheckOptions.getRulerOptions().getMeasure();
        optionMeasures = OptionsMeasureUtils.getOptionMeasure(measures);
//        初始化测量标准提示信息
        String standard = rulerCheckOptions.getRulerOptions().getStandard();
        tvQualifiedStandard.setText("合格标准："+standard);

    }

    public void setDisplayDataAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        gvDisplayData.setAdapter(adapter);
    }


    public ImageView getImgTitle() {
        return imgTitle;
    }

    public void setImgTitle(ImageView imgTitle) {
        this.imgTitle = imgTitle;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public void setTvTitle(TextView tvTitle) {
        this.tvTitle = tvTitle;
    }

    public TextView getTvQualifiedStandard() {
        return tvQualifiedStandard;
    }

    public void setTvQualifiedStandard(TextView tvQualifiedStandard) {
        this.tvQualifiedStandard = tvQualifiedStandard;
    }

    public GridView getGvDisplayData() {
        return gvDisplayData;
    }

    public void setGvDisplayData(GridView gvDisplayData) {
        this.gvDisplayData = gvDisplayData;
    }

    public TextView getTvRealMeasureNum() {
        return tvRealMeasureNum;
    }

    public void setTvRealMeasureNum(TextView tvRealMeasureNum) {
        this.tvRealMeasureNum = tvRealMeasureNum;
    }

    public TextView getTvQualifiedNum() {
        return tvQualifiedNum;
    }

    public void setTvQualifiedNum(TextView tvQualifiedNum) {
        this.tvQualifiedNum = tvQualifiedNum;
    }

    public TextView getTvQualifiedRate() {
        return tvQualifiedRate;
    }

    public void setTvQualifiedRate(TextView tvQualifiedRate) {
        this.tvQualifiedRate = tvQualifiedRate;
    }
}
