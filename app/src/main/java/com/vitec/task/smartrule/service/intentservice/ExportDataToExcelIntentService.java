package com.vitec.task.smartrule.service.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.bean.MeasureTable;
import com.vitec.task.smartrule.bean.MeasureTableRow;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.RulerCheckOptionsData;
import com.vitec.task.smartrule.bean.event.ExportMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.IAddExcelResultCallBack;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.helper.ExportMeaureDataHelper;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ExportDataToExcelIntentService extends IntentService {

    public final static String GET_DATA_KEY = "com.vitec.task.smartrule.get.data.key";
    public final static String GET_FILE_NAME = "com.vitec.task.smartrule.get.file.name";

    public ExportDataToExcelIntentService() {
        super("ExportDataToExcelIntentService");
    }
    public ExportDataToExcelIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        List<RulerCheck> exportRulerCheckList = (List<RulerCheck>) intent.getSerializableExtra(GET_DATA_KEY);
        String fileName = intent.getStringExtra(GET_FILE_NAME);
        List<MeasureTable> tableList = initExportDataFormat(exportRulerCheckList);
        final ExportMeaureDataHelper exportMeaureData = new ExportMeaureDataHelper(getApplicationContext(), fileName);
        LogUtils.show("onHandleIntent---准备开始导出文件，总数："+tableList.size()+",内容："+tableList.toString());
        if (tableList.size() > 0) {
            for (int i=0;i<tableList.size();i++) {
                exportMeaureData.addExcelData(tableList.get(i));
            }
            exportMeaureData.write(new IAddExcelResultCallBack() {
                @Override
                public void onSuccess(String title) {
                    //                                一定要关闭，不然表格内容会为空
                    exportMeaureData.close();
                    ExportMsgEvent exportMsgEvent = new ExportMsgEvent(true);
                    exportMsgEvent.setMsg(title);
                    EventBus.getDefault().post(exportMsgEvent);
                    LogUtils.show("导出成功");
                }

                @Override
                public void onFail(String msg) {
                    exportMeaureData.close();
                    ExportMsgEvent exportMsgEvent = new ExportMsgEvent(false);
                    exportMsgEvent.setMsg(msg);
                    EventBus.getDefault().post(exportMsgEvent);
                    LogUtils.show("导出失败");
                }
            });
        }
    }

    /**
     * 将数据转换成为导出excel表格需要的格式
     * 我们自定义的导出工具类（ExportMeaureDataHelper）接受一个MeasureTable数据对象
     * 所以我们将每一个rulercheck和RulercheckOptionData转换成为MeasureTable对应的数据
     */
    private List<MeasureTable> initExportDataFormat( List<RulerCheck> exportRulerCheckList) {
        List<MeasureTable> tableList = new ArrayList<>();
        for (int i=0;i<exportRulerCheckList.size();i++) {
            RulerCheck rulerCheck = exportRulerCheckList.get(i);
            /**
             * MeasureTable 为一个完整的表格对象
             * 下面依次从rulercheck中添加的为表格的表头信息
             */
            MeasureTable table = new MeasureTable();
            table.setEngineerName(rulerCheck.getEngineer().getEngineerName());
            table.setProjectName(rulerCheck.getProjectName());
            table.setCheckPerson(rulerCheck.getUser().getUserName());
            table.setCheckFloor(rulerCheck.getCheckFloor());
            table.setCheckDate(DateFormatUtil.stampToDateString(rulerCheck.getCreateTime()));
            //rowList为该表格中所有的管控要点集合，一个MeasureTableRow代表一个管控要点
            List<MeasureTableRow> rowList = new ArrayList<>();
//            从数据库中取出对应的管控要点数据
            String where = " where " + DataBaseParams.measure_option_check_id + " = " + rulerCheck.getId();
            List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheck, where);
            LogUtils.show("initExportDataFormat-----查看需要导出的管控要点个数："+checkOptionsList.size());
            if (checkOptionsList.size() > 0) {
//                循环遍历管控要点
                for (int j=0;j<checkOptionsList.size();j++) {
                    RulerCheckOptions checkOptions = checkOptionsList.get(j);
                    /**
                     * 创建一个管控要点对象，即表格中的一大行
                     */
                    MeasureTableRow row = new MeasureTableRow();
                    row.setStandard(checkOptions.getRulerOptions().getStandard());
                    row.setQualifiedRate(String.valueOf(checkOptions.getQualifiedRate()));
                    row.setRealMeasureNum(checkOptions.getMeasuredNum());
                    row.setQualifiedNum(checkOptions.getQualifiedNum());
                    row.setOptionName(checkOptions.getRulerOptions().getOptionsName());
                    row.setCheckMethod(checkOptions.getRulerOptions().getMethods());
//                    设置表格中显示的管控要点序号，不是数据库的id
                    row.setId(j + 1);
//                    从数据库中取出该管控要点对应的测量数据
                    List<RulerCheckOptionsData> optionsDataList = OperateDbUtil.queryMeasureDataFromSqlite(getApplicationContext(), checkOptions);
//                    MeasureTableRow中的测量数据源
                    List<String> datalist = new ArrayList<>();
                    if (optionsDataList.size() > 0) {
                        for (int n = 0; n < optionsDataList.size(); n++) {
                            datalist.add(optionsDataList.get(n).getData());
                        }
                    } else {
                        for (int n = 0; n < 20; n++) {
                            datalist.add("");
                        }
                    }
//                    将测量数据集合添加到所属的管控要点中
                    row.setDatalist(datalist);
//                    将测量管控要点的数据添加到测量管控要点的集合中
                    rowList.add(row);
                }
            }
//            将所有的测量管控要点集合添加到表格的对象中
            table.setRowList(rowList);
//            如此一个表格的数据就添加完成了，表格集合中
            tableList.add(table);
        }

        return tableList;

    }
}
