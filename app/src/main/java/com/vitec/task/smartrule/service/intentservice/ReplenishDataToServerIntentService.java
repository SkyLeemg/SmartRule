package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import com.vitec.task.smartrule.activity.MeasureRecordActivity;
import com.vitec.task.smartrule.activity.WaitingMeasureActivity;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
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
        Set<Integer> projectIdSet = new HashSet<>();
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
                projectIdSet.add(rulerCheckList.get(i).getProject().getId());
                List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheckList.get(i));
                List<RulerCheckOptionsData> optionsDataList = new ArrayList<>();
                for (RulerCheckOptions checkOptions : checkOptionsList) {
//                    j检查是否有图纸未上传
                    uploadOptionsPic(checkOptions);
//                    通过option查找数据
                    List<RulerCheckOptionsData> dataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), checkOptions);
                    optionsDataList.addAll(dataList);
                }
                Intent uploadIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_DATA);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_CREATE_OPTIONS_DATA_KEY, (Serializable) checkOptionsList);
                uploadIntent.putExtra(PerformMeasureNetIntentService.GET_UPDATE_DATA_KEY, (Serializable) optionsDataList);
                startService(uploadIntent);
                LogUtils.show("ReplenishDataToServerIntentService----一直没网络，"+rulerCheckList.get(i).getProject().getProjectName()+",准备开始上传");
            }
        } else {
            String dataWhere = " where " + DataBaseParams.server_id + "= 0  ";
            List<RulerCheckOptionsData> dataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), dataWhere);
            LogUtils.show("ReplenishDataToServerIntentService----有网络：查看需要上传的数据内容：" + dataList);
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

        /*********************请求图纸上传失败的***************************/
        String imgWhere = " where " + DataBaseParams.measure_option_img_upload_flag + "= 0";
        List<RulerCheckOptions> imgOptions = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), imgWhere);
        for (RulerCheckOptions options : imgOptions) {
            uploadOptionsPic(options);
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

        /********************请求之前修改失败的记录表和数据**************************/
        /**
         * upload_flag为5的记录，代表之前用户在本地修改了的数据，但因为网络问题还没请求到服务器
         */
        String editcheckWhere = " where " + DataBaseParams.upload_flag + "= 5";
        List<RulerCheck> editCheckList = bleDataDbHelper.queryRulerCheckTableDataFromSqlite(editcheckWhere);
        if (editCheckList.size() > 0) {
            LogUtils.show("补上传服务-----发型有修改了信息的rulercheck没有上传："+editCheckList.size());
            for (int i = 0; i < editCheckList.size(); i++) {
                Intent editintent = new Intent(getApplicationContext(),PerformMeasureNetIntentService.class);
                editintent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_UPDATE_RECORD);
                editintent.putExtra(PerformMeasureNetIntentService.GET_CREATE_RULER_DATA_KEY, editCheckList.get(i));
                startService(editintent);
            }
        }


        /*************************请求创建测量组失败的部分*******************************/
        String projectWhere = " where " + DataBaseParams.server_id + "=0";
        List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), projectWhere);
        if (projectList.size() > 0) {
            for (RulerCheckProject project : projectList) {
                if (projectIdSet.add(project.getId())) {
                    Intent pintent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(DataBaseParams.check_project_name, project.getProjectName());
                    bundle.putString(DataBaseParams.user_user_id, String.valueOf(project.getUser().getUserID()));
                    bundle.putInt(DataBaseParams.measure_project_id, project.getId());
//                bundle.putSerializable(NetConstant.group_project_list, project);
                    pintent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_create_project);
                    pintent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                    startService(pintent);
                    LogUtils.show("补上传服务-----有创建失败的测量组："+projectList.size());
                }
            }

        }

    }

    /**
     * 判断RulerCheckOptions是否有未上传的图纸。
     * 有则请求上传图纸。接着更新接口
     * @param options
     */
    private void uploadOptionsPic(RulerCheckOptions options) {
        if (options.getImg_upload_flag() == 0&& options.getImgPath() != null && options.getImgPath().length() > 5) {
            String server_id = String.valueOf(options.getServerId());
            LogUtils.show("ReplenishDataToServerIntentService---查看管控要点的服务ID：" + server_id);
             Intent uploadIntent = new Intent(getApplicationContext(), UploadPicIntentService.class);
             uploadIntent.putExtra(UploadPicIntentService.UPLOAD_FLAG, UploadPicIntentService.FLAG_UPLOAD_OPTION_IMG);
             uploadIntent.putExtra(UploadPicIntentService.VALUE_IMG_PATH, options.getImgPath());
             uploadIntent.putExtra(UploadPicIntentService.VALUE_OPTION_LIST, server_id);
            Bundle bundle = new Bundle();
            bundle.putString(NetConstant.upload_option_pic_check_options_list, server_id);
            bundle.putString(NetConstant.upload_option_pic_number_list, String.valueOf(options.getImgNumber()));
            uploadIntent.putExtra(UploadPicIntentService.VALUE_OPTION_BUNDLE, bundle);
            startService(uploadIntent);


        }
    }
}
