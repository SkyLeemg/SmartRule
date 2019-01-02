package com.vitec.task.smartrule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.DisplayMeasureDataAdapter;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
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
    private List<RulerCheckOptionsData> usingCheckOptionsDataList;//给GridView使用的数据源

    private List<OptionMeasure> optionMeasures;//该管控要点可选的层高，还要测量数据标准都在这里
    private OptionMeasure optionMeasure;//上面是该管控要点所有的层高，这个是用户当前选择的层高
    private int realDataCount;

    private int realNum = 0;
    private int qualifiedNum=0;
    private float qualifiedRate=0f;
    private DisplayMeasureDataAdapter measureDataAdapter;


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
        LogUtils.show("已初始化控件");
    }

    /**
     * 新建对象后，要初始化数据
     * @param rulerCheckOptions
     */
    public void initData(RulerCheckOptions rulerCheckOptions) {
        LogUtils.show("正在初始化数据");
        this.rulerCheckOptions = rulerCheckOptions;
//        初始化数据模板
        optionsDataMudel = new RulerCheckOptionsData();
        optionsDataMudel.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
        optionsDataMudel.setRulerCheckOptions(rulerCheckOptions);
//        初始化计算标准的模板信息
        optionMeasures = new ArrayList<>();
        //初始化标题信息
        if (rulerCheckOptions.getRulerOptions().getType() == 1) {
            tvTitle.setText("垂直度");
            imgTitle.setImageResource(R.mipmap.icon_measure_vertical);
        } else if (rulerCheckOptions.getRulerOptions().getType() == 2) {
            tvTitle.setText("平整度");
            imgTitle.setImageResource(R.mipmap.icon_measure_lever);
        } else {
            tvTitle.setText(rulerCheckOptions.getRulerOptions().getOptionsName());
        }
//        获取测量计算参考标准的数据
        final String measures = rulerCheckOptions.getRulerOptions().getMeasure();
        optionMeasures = OptionsMeasureUtils.getOptionMeasure(measures);
        if (optionMeasures.size() > 1) {
            int count = 0;
            for (int k = 0; k < optionMeasures.size(); k++) {
                if (optionMeasures.get(k).getData().equals(rulerCheckOptions.getFloorHeight())) {
                    optionMeasure = optionMeasures.get(k);
                    count++;
                }
            }
        } else if (optionMeasures.size() > 0) {
            optionMeasure = optionMeasures.get(0);
        }
        LogUtils.show("查看计算标准optionMeasure----"+optionMeasure);

//        初始化测量标准提示信息
        String standard = rulerCheckOptions.getRulerOptions().getStandard();
        tvQualifiedStandard.setText("合格标准："+standard);

//        接下来根据rulerOptionsID去数据库查找有没有相同的数据，有则显示出来，无则新建
        usingCheckOptionsDataList= OperateDbUtil.queryMeasureDataFromSqlite(context, rulerCheckOptions);
        //实际有数据的统计个数
        realDataCount = usingCheckOptionsDataList.size();
//        如果没有测量数据，则模拟空的数据传过来
        if (usingCheckOptionsDataList.size() == 0) {
            rulerCheckOptions.setMeasuredNum(0);
            rulerCheckOptions.setQualifiedNum(0);
            for (int i = 0; i < 12; i++) {
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setData("");
                data.setQualified(true);
                data.setRulerCheckOptions(rulerCheckOptions);
                usingCheckOptionsDataList.add(data);
            }
            updateCompleteResult();
        } else {
            completeResult();
        }

        measureDataAdapter = new DisplayMeasureDataAdapter(context, usingCheckOptionsDataList);
        gvDisplayData.setAdapter(measureDataAdapter);
        HeightUtils.setGridViewHeighBaseOnChildren(gvDisplayData,6);

    }

    /**
     * 计算测量数据的结果
     */
    public void completeResult() {
        if (optionMeasure != null) {
            realNum = 0;
            qualifiedNum = 0;
            float frealnum = 0.0f;
            float fq = 0.0f;
            for (int i=0; i<realDataCount;i++) {
                String data = usingCheckOptionsDataList.get(i).getData().trim();
                try {
                    float datanum = Float.valueOf(data);
                    /**
                     * 根据操作标志来计算结果，
                     * 1 - 代表要 小于等于 才合格
                     * 2 -
                     */
                    switch (optionMeasure.getOperate()) {
                        case 1:
                            if (datanum < optionMeasure.getStandard() || datanum == optionMeasure.getStandard()) {
                                qualifiedNum++;
//                               设置合格
                                usingCheckOptionsDataList.get(i).setQualified(true);
                            } else {
                                usingCheckOptionsDataList.get(i).setQualified(false);
                            }
                            break;
                        case 2:

                            break;
                    }
                    realNum++;
                } catch (Exception e) {
                }
            }
            frealnum = realNum;
            fq = qualifiedNum;
            qualifiedRate = (fq / frealnum);
            rulerCheckOptions.setMeasuredNum(realNum);
            rulerCheckOptions.setQualifiedNum(qualifiedNum);
            rulerCheckOptions.setQualifiedRate(Float.parseFloat(String.format("%.2f", qualifiedRate *100)));
            updateCompleteResult();
        }
    }

    /**
     * 更新显示计算结果的控件
     */
    private void updateCompleteResult() {
        tvQualifiedNum.setText(rulerCheckOptions.getQualifiedNum()+"");
        tvRealMeasureNum.setText(rulerCheckOptions.getMeasuredNum()+"");
        if (qualifiedRate >=0) {
            tvQualifiedRate.setText(String.format("%.2f",qualifiedRate*100));
        }else tvQualifiedRate.setText("0.00");
        HeightUtils.setGridViewHeighBaseOnChildren(gvDisplayData,6);
    }

    /**以下是需要跟调用此View者需要交互的数据变量**/
    public DisplayMeasureDataAdapter getMeasureDataAdapter() {
        return measureDataAdapter;
    }

    public void setMeasureDataAdapter(DisplayMeasureDataAdapter measureDataAdapter) {
        this.measureDataAdapter = measureDataAdapter;
    }

    public List<RulerCheckOptionsData> getUsingCheckOptionsDataList() {
        return usingCheckOptionsDataList;
    }

    public void setUsingCheckOptionsDataList(List<RulerCheckOptionsData> usingCheckOptionsDataList) {
        this.usingCheckOptionsDataList = usingCheckOptionsDataList;
    }

    public int getRealDataCount() {
        return realDataCount;
    }

    public void setRealDataCount(int realDataCount) {
        this.realDataCount = realDataCount;
    }

    public RulerCheckOptions getRulerCheckOptions() {
        return rulerCheckOptions;
    }

    public void setRulerCheckOptions(RulerCheckOptions rulerCheckOptions) {
        this.rulerCheckOptions = rulerCheckOptions;
    }

    public GridView getGvDisplayData() {
        return gvDisplayData;
    }

    public void setGvDisplayData(GridView gvDisplayData) {
        this.gvDisplayData = gvDisplayData;
    }
}
