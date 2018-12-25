package com.vitec.task.smartrule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.event.HeightFloorMsgEvent;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.helper.TextToSpeechHelper;
import com.vitec.task.smartrule.service.ConnectDeviceService;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OptionsMeasureUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.vitec.task.smartrule.utils.BleParam.UART_PROFILE_DISCONNECTED;

/**
 * 真正测量的fragment。一个管控要点为一个fragment
 * 多个管控要点重复new MeasureFragment
 */
public class MeasureRecordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MeasureFragment";
    private View view;
    private GridView gvMeasureData;
    private TextView tvProjectName;//项目类型
    private TextView tvMeasureItem;//管控要点
    private TextView tvQualifiedStandard;
    private EditText etStandardRate;
    private EditText etStandartNum;
    private EditText etRealMeasureNum;
    private Spinner spinnerFloorHeight;
    private ImageView imgAdd;


    private MeasureDataAdapter measureDataAdapter;
    private Bundle bundle;
    private int mState = UART_PROFILE_DISCONNECTED;
    private TextToSpeechHelper mTextToSpeechHelper;
    private ConnectDeviceService mService = null;
    private int currentDataNum = 0;

    private int check_option_id;//此id对应iot_ruler_check_options表的id

    private List<String> floodHeights;
    private String floodHeight;
    private ArrayAdapter spinnerAdapter;
    private String standard;//合格标准
    private int standartNum = 8;
    private int realNum = 0;//实测点数
    private int qualifiedNum = 0;//合格点数
    private float qualifiedRate = 0.0f;//合格率
    private LinearLayout llFloorHeight;//选择层高的模块，当合格标准只有以项的时候需要隐藏这个模块

    private RulerCheckOptionsData checkOptionsData;//一个空的数据的模板
    private List<RulerCheckOptionsData> checkOptionsDataList;//蓝牙发过来的数据集合
    private List<RulerCheckOptionsData> uploadOptionsDataList;//待发送给服务器的数据集合
    private RulerCheckOptions checkOptions;//一个测量的管控要点
    private List<OptionMeasure> optionMeasures;//该管控要点可选的层高，还要测量数据标准都在这里
    private OptionMeasure optionMeasure;//上面是该管控要点所有的层高，这个是用户当前选择的层高


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_measure, null);
        EventBus.getDefault().register(this);
        initView();
        initData();
        return view;
    }

    private void initView() {
        gvMeasureData = view.findViewById(R.id.gv_measure_data);
        tvProjectName = view.findViewById(R.id.tv_project_type);
        tvMeasureItem = view.findViewById(R.id.tv_measure_item);
        mTextToSpeechHelper = new TextToSpeechHelper(getActivity(),"");
        tvQualifiedStandard = view.findViewById(R.id.tv_qualified_flag);
        etStandardRate = view.findViewById(R.id.et_standard_rate);
        etStandartNum = view.findViewById(R.id.et_standard_num);
        etRealMeasureNum = view.findViewById(R.id.et_real_measure_num);
        spinnerFloorHeight = view.findViewById(R.id.spinner_floor_height);
        llFloorHeight = view.findViewById(R.id.ll_floor_height);
        imgAdd = view.findViewById(R.id.img_add);
        imgAdd.setVisibility(View.GONE);

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+
                "调用了onHiddenChanged方法："+hidden);

    }





    private void initData() {
        /**
         * TODO 还需要接受传来的数据，显示在对应的控件上
         */
        checkOptionsDataList = new ArrayList<>();
        uploadOptionsDataList = new ArrayList<>();

        /**
         * 接收在创建Fragment时发来的数据
         */
        bundle = getArguments();
        checkOptionsData = new RulerCheckOptionsData();
        /**
         * checkOptions里面包含了项目信息、工程和管控要点的模板信息
         */
        checkOptions = (RulerCheckOptions) bundle.getSerializable("checkoptions");
        checkOptionsData.setCreateTime((int) System.currentTimeMillis());
        checkOptionsData.setRulerCheckOptions(checkOptions);
//        初始化optionMeasures
        optionMeasures = new ArrayList<>();
        final String measures = checkOptions.getRulerOptions().getMeasure();
        optionMeasures = OptionsMeasureUtils.getOptionMeasure(measures);
        LogUtils.show("查看"+checkOptions.getRulerOptions().getOptionsName()+"模块的optionMeasures："+optionMeasures.toString());

        Log.e(TAG, "initData: 查看MeasureFragment收到的checkoptions:"+ checkOptions);
        tvProjectName.setText(checkOptions.getRulerCheck().getProjectName()+":");
        tvMeasureItem.setText("管控要点："+ checkOptions.getRulerOptions().getOptionsName());
//        此id对应iot_ruler_check_options表的id
        check_option_id = checkOptions.getId();
        standard = checkOptions.getRulerOptions().getStandard();
        tvQualifiedStandard.setText(standard);




        /**
         * 初始化保存蓝牙数据的集合对象
         */
        checkOptionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getActivity(), checkOptions);
        currentDataNum = checkOptionsDataList.size();
        if (checkOptionsDataList.size() == 0) {
            for (int i = 0; i < 12; i++) {
                RulerCheckOptionsData data = new RulerCheckOptionsData();
                data.setRulerCheckOptions(checkOptions);
                checkOptionsDataList.add(data);
            }
        } else {
            completeResult();
        }

        updateCompleteResult();

        /**
         * 初始化接受数据的gridview
         */
        measureDataAdapter = new MeasureDataAdapter();
        gvMeasureData.setAdapter(measureDataAdapter);
        HeightUtils.setGridViewHeighBaseOnChildren(gvMeasureData,6);

        /********************层高选择框部分开始**************************/

        floodHeights = new ArrayList<>();
        for (OptionMeasure measure : optionMeasures) {
            floodHeights.add(measure.getData());
        }
//        初始化默认选择的层高及运算标准
        optionMeasure = new OptionMeasure();
        if (optionMeasures.size() > 0) {
            optionMeasure = optionMeasures.get(0);
        }else {
            optionMeasure.setData("≤6");
            optionMeasure.setStandard(8);
            optionMeasure.setOperate(1);
            optionMeasure.setId(1);
        }
        LogUtils.show("查看"+checkOptions.getRulerOptions().getOptionsName()+"模块的floodHeights："+floodHeights.toString());
        if (floodHeights.size() > 1) {
            llFloorHeight.setVisibility(View.VISIBLE);
            spinnerAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, floodHeights);
            spinnerFloorHeight.setAdapter(spinnerAdapter);
        } else {
            llFloorHeight.setVisibility(View.GONE);
        }

        spinnerFloorHeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemSelected: 查看当前选择的：" + i + ",内容：" + floodHeights.get(i) + ",查看当前选择的标准：" + optionMeasures.get(i));
                floodHeight = floodHeights.get(i);
                if (floodHeights.get(i).equals(optionMeasures.get(i).getData())) {
                    optionMeasure = optionMeasures.get(i);
                } else {
                    for (OptionMeasure measure : optionMeasures) {
                        if (floodHeights.get(i).equals(measure.getData())) {
                            optionMeasure = measure;
                        }
                    }
                }
                EventBus.getDefault().post(new HeightFloorMsgEvent(checkOptions.getRulerOptions().getType(),optionMeasure));
                completeResult();
                Log.e(TAG, "onItemSelected: 查看最终参看值："+standartNum );
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        /********************层高选择框部分结束**************************/

    }

    /**
     * 根据计算标准去计算实测数、合格数和合格率
     */
    private void completeResult() {
        if (optionMeasure != null) {
            realNum = 0;
            qualifiedNum = 0;
            float frealnum = 0.0f;
            float fq = 0.0f;
            for (int i=0; i<currentDataNum;i++) {
                String data = checkOptionsDataList.get(i).getData().trim();
                Log.e(TAG, "completeResult: 查看字符串格式的数据:"+data+",字符串长度："+data.length());
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
                            }
                            break;
                    }
                    realNum++;
                } catch (Exception e) {
                    Log.e(TAG, "completeResult: 错误原因："+e.getMessage() );
                }
            }
            frealnum = realNum;
            fq = qualifiedNum;
            qualifiedRate = (fq / frealnum);
            Log.e(TAG, "completeResult: 查看计算出来的实测点数："+realNum+",合格点数："+qualifiedNum+",合格率："+qualifiedRate );
            checkOptions.setFloorHeight(floodHeight);
            checkOptions.setMeasuredNum(realNum);
            checkOptions.setQualifiedNum(qualifiedNum);
            checkOptions.setQualifiedRate(Float.parseFloat(String.format("%.2f",qualifiedRate*100)));
            updateCompleteResult();
        }
    }

    private void updateCompleteResult() {
        etRealMeasureNum.setText(realNum+"");
        etStandartNum.setText(qualifiedNum+"");
        if (qualifiedRate >=0) {
            etStandardRate.setText(String.format("%.2f",qualifiedRate*100));
        }else etStandardRate.setText("0.00");

//        if (currentDataNum > 0 && checkOptionsDataList.size() > 0) {
//            uploadOptionsDataList.add(checkOptionsDataList.get(currentDataNum - 1));
//        }
//        updateMeasureDataToServer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netBussCallBack(String flag) {
        LogUtils.show("netBussCallBack---查看创建好记录表后返回的标志:"+flag);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
//        先更新RulerCheck的server_id
        String where = " where id = " + checkOptions.getRulerCheck().getId();
        List<RulerCheck> rulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(where);
        if (rulerCheckList.size() > 0) {
            LogUtils.show("netBussCallBack====查看数据库查询出来的Rulercheck：" + rulerCheckList.get(0));
            RulerCheck rulerCheck = checkOptions.getRulerCheck();
            rulerCheck.setServerId(rulerCheckList.get(0).getServerId());
            checkOptions.setRulerCheck(rulerCheck);
        }
        bleDataDbHelper.close();
        //        再更新RulerCheckOption的Server_id
        String optionWhere = " where id = " + checkOptions.getId();
        List<RulerCheckOptions> rulerCheckOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getActivity(), checkOptions.getRulerCheck(), optionWhere);
        if (rulerCheckOptionsList.size() > 0) {
            checkOptions.setServerId(rulerCheckOptionsList.get(0).getServerId());
            LogUtils.show("netBussCallBack====查看数据库查询出来的RrulerCheckOptionsList：" + rulerCheckOptionsList.get(0));
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的stop方法调用了" );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的onDestroyView调用了");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("MeasureFragment--"+checkOptions.getRulerOptions().getOptionsName()+"的onDestroy调用了");
//        退出前将数据更新到数据库
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getActivity());
        bleDataDbHelper.updateMeasureOptonsToSqlite(checkOptions);
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    class MeasureDataAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return checkOptionsDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return checkOptionsDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.item_gridview_measure_data, null);
                holder = new ViewHolder();
                holder.etData = view.findViewById(R.id.et_measure_data);
                holder.tvContent = view.findViewById(R.id.tv_measure_content);
                holder.tvTitleIndex = view.findViewById(R.id.tv_title_index);
                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

//            holder.etData.setText(checkOptionsDataList.get(i).getData());

            holder.tvContent.setText(checkOptionsDataList.get(i).getData());
            holder.tvTitleIndex.setText(i+1);
            if (checkOptions.getRulerOptions().getType() == 1 || checkOptions.getRulerOptions().getType() == 2) {
                holder.etData.setEnabled(false);
            } else {
                holder.etData.setEnabled(true);
            }
            return view;
        }
    }

    class ViewHolder {
        EditText etData;
        TextView tvTitleIndex;
        TextView tvContent;
    }

}

