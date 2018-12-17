package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.activity.MeasureRecordActivity;
import com.vitec.task.smartrule.activity.WaitingMeasureActivity;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 补上传数据到服务器：将本地数据库里之前因为网络问题未上传成功的数据补上传到服务器
 * 每次操作数据的时候都有可能会有数据未上传成功，主要是三个表格的数据内容
 * 1.check_options_data表、check_options表和check表中server_id为0的数据项,
 *    由于数据上传格式已有server_id和没有server_id来进行区分的，所以我们也用这个来判断本条数据是否在服务器创建成功
 *    upload_flag先放着做其他用途，比如结束测量是否成功、删除等做这些操作的时候很多数据都已经有server_id了所以用upload_flag来区分
 * 2.ruler_check表的server_id为0代表刚开始创建表格的时候就没网络，
 *      这时候一次性将整个check表和跟该项有关联的另外两个表的数据都上传了
 * 3.ruler_check的server_id不为 0，而ruler_check_options为0的几率不大，因为这两个是一起请求给服务器的，如果发现为0则更改为1
 * 4.ruler_check的server_id不为 0，而ruler_check_options_data的server_id为0，则是点击新建测量时网络后来没网络的情况
 *    直接将数据补上传
 * 5.
 */
public class ReplenishDataToServerIntentService extends IntentService{

    public ReplenishDataToServerIntentService() {
        super("ReplenishDataToServerIntentService");
    }

    public ReplenishDataToServerIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        /************************补上传测量数据*****************************/
        /**
         * 搜索ruler_check表格中server_id为0的数据项
         */
        String checkWhere = " where " + DataBaseParams.server_id + "= 0";
        List<RulerCheck> rulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(checkWhere);
        if (rulerCheckList.size() > 0) {
            /**
             * 如果有未在服务器创建的数据，则走更新数据的接口，
             * 该接口上传数据需要将RulerCheckOptions集合对象和RulerCheckOptionsData对象传给PerformMeasureNetIntentService
             */
            for (int i = 0; i < rulerCheckList.size(); i++) {
                List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheckList.get(i));
                List<RulerCheckOptionsData> optionsDataList = new ArrayList<>();
                for (RulerCheckOptions checkOptions : checkOptionsList) {
                    List<RulerCheckOptionsData> dataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), checkOptions);
                    optionsDataList.addAll(dataList);
                }
                Intent uploadIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_DATA);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) checkOptionsList);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_UPDATE_DATA_KEY, (Serializable) optionsDataList);
                startService(uploadIntent);
                LogUtils.show("ReplenishDataToServerIntentService----一直没网络，"+rulerCheckList.get(i).getProjectName()+",准备开始上传");
            }
        } else {
            String dataWhere = " where " + DataBaseParams.server_id + "= 0  ";
            List<RulerCheckOptionsData> dataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), dataWhere);
            if (dataList.size() > 0) {

                Set<RulerCheckOptions> optionsSet = new HashSet<>();
                List<RulerCheckOptions> optionsList = new ArrayList<>();
                for (RulerCheckOptionsData data : dataList) {
                    if (optionsSet.add(data.getRulerCheckOptions())) {
                        optionsList.add(data.getRulerCheckOptions());
                    }
                    LogUtils.show("查看当前数据ID："+data.getId()+",查看当前ID对应的管控要点ID："+data.getRulerCheckOptions().getId());
                }
                Intent uploadIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_DATA);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) optionsList);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_UPDATE_DATA_KEY, (Serializable) dataList);
                startService(uploadIntent);
                LogUtils.show("ReplenishDataToServerIntentService---之前有网，有"+optionsList.size()+"个管控要点，"+dataList.size()+"条数据，需要上传");
            }
        }



        /***************************请求之前删除失败的记录表和数据********************************/
        /**
         * upload_flag为4的记录，代表之前用户在本地删除了的数据，但因为网络问题还没请求到服务器
         */
        String delcheckWhere = " where " + DataBaseParams.upload_flag + "= 4";
        List<RulerCheck> delRulerCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(delcheckWhere);
        if (delRulerCheckList.size() > 0) {
            StringBuffer check_ids = new StringBuffer();
            for (int i = 0; i < delRulerCheckList.size(); i++) {
                check_ids.append(delRulerCheckList.get(i).getServerId());
                if (i < (delRulerCheckList.size() - 1)) {
                    check_ids.append(",");
                }
            }
            Intent serviceIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_DEL_RECORD);
            //        传完成测量的标志，。
            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, check_ids.toString());
            startService(serviceIntent);
        }

        /********************************请求之前结束测量结束失败的****************************************/
        String finishWhere = " where " + DataBaseParams.measure_is_finish + " = 1";
        List<RulerCheck> finishRulerCheck = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(finishWhere);
        if (finishRulerCheck.size() > 0) {
            Intent serviceIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_FINISH_MEASURE);
            serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FINISH_MEASURE_KEY, (Serializable) finishRulerCheck);
            startService(serviceIntent);
            LogUtils.show("请求以前未结束测量的开始啦，。，。，。，。，。，。，。，。，。，。");
        }
    }
}