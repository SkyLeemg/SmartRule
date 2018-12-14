package com.vitec.task.smartrule.helper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.vitec.task.smartrule.bean.MeasureTable;
import com.vitec.task.smartrule.bean.MeasureTableRow;
import com.vitec.task.smartrule.interfaces.IAddExcelResultCallBack;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 导出测量数据到excel表格，需要对造模板表格来规划表格格式
 * 1.从模板中看出一共需要用到17列
 * 2.在构造方法中创建表格文件和工作簿
 * 3.初始化行号游标（rowCucor=0）为0，每用完一行行号游标+1
 * 4.在rowNum中写入工程名，合并17列且居中，rowCucor++
 * 5.接下来写入信息：
 *  5.1 写入项目名称合并4个单元格（0,rowCucor,3,rowCucor）
 *  5.2 写入检查楼栋楼层合并6个单元格(4,rowCucor,9,rowCucor)
 *  5.3 写入检查人并合并4个单元格（10,rowCucor,13,rowCucor）
 *  5.5 写入检查日期并合并3个单元格（14,rowCucor,17,rowCucor）
 *  5.6 rowCucor++
 * 6.依次写入表头信息（序号、管控要点、质量标准、检查方法、01~10、实测数、合格数和合格率）
 * 7.开启行循环
 *   7.1 初始化测量数据的行号(dataRowNum=rowCucor)
 *   7.2 根据data/10的结果加上是否有余数(有余+1，无余不加)来判断序号、管控要点、质量标准、检查方法、实测数、合格数和合格率竖向合并几个单元格
 *   7.3 rowCucor=rowCucor+合并个数
 *   7.4 写入除了data之外的数据
 *   7.5 开启data循环，依次将数据写入到01~10列下的单元格中，每10个单元格dataRowNum++
 *
 */
public class ExportMeaureDataHelper {
//    sheet表，工作簿的名
    private WritableSheet mWritableSheet;
    //    excel文件
    private WritableWorkbook mWritableWorkbook;
    //    行号游标
    private int rowCucor = 0;
    //    表头信息集合，是固定的
    private String[] titles = new String[]{"序号","管控要点","合格标准","检查方法","01","02","03","04","05","06","07","08","09","10",
            "实测数","合格数","合格率"};
    private WritableFont titleFont;
    private WritableCellFormat titleFormat;
    private WritableFont tableTiTleFont;
    private WritableCellFormat tableTitleFormat;
    private WritableFont contentFont;
    private WritableCellFormat contentFormat;
    public static final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//    public static final String path = Environment.getExternalStorageDirectory() + "/excel";

    public ExportMeaureDataHelper(Context context, String fileName) {
        createExcelFile(context,fileName);
    }


    private void createExcelFile(Context context,String fileName) {
//        path = "data/data/" + context.getPackageName() + "/excel";
//        path = "/excel";
//        /storage/emulated/0/excel
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        File excelFile = new File(dir, fileName);
        if (!excelFile.exists()) {
            try {
                excelFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Log.e("aaa", "createExcelFile: 查看文件是否能读写："+excelFile.canWrite() );
            if (excelFile.canWrite()) {
                mWritableWorkbook = Workbook.createWorkbook(excelFile);
                mWritableSheet = mWritableWorkbook.createSheet("测量数据", 0);
                /**
                 * 设置列宽
                 */
                mWritableSheet.setColumnView(1, 20);
                mWritableSheet.setColumnView(2, 20);
                mWritableSheet.setColumnView(3, 50);
                mWritableSheet.setColumnView(14, 20);
                mWritableSheet.setColumnView(15, 20);
                mWritableSheet.setColumnView(16, 20);
                format();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void format() {

        try {
            /**
             * 标题用的格式，例如：混凝土工程测量 - 实测实量 这个标题
             */
            titleFont = new WritableFont(WritableFont.ARIAL, 20, WritableFont.BOLD);
            titleFormat = new WritableCellFormat(titleFont);
//            设置格式为居中
            titleFormat.setAlignment(Alignment.CENTRE);
            titleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
//            设置边框线条
            titleFormat.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

            /**
             * 表格的标题用的格式，
             */
            tableTiTleFont = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            tableTitleFormat = new WritableCellFormat(tableTiTleFont);
            tableTitleFormat.setAlignment(Alignment.CENTRE);
            tableTitleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            tableTitleFormat.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

            /**
             * 表格内容用的格式
             */
            contentFont = new WritableFont(WritableFont.ARIAL, 14, WritableFont.NO_BOLD);
            contentFormat = new WritableCellFormat(contentFont);
            contentFormat.setAlignment(Alignment.CENTRE);
            contentFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            contentFormat.setWrap(true);//设置自动换行
            contentFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }


    }

    public void addExcelData(MeasureTable measureTable) {

        try {
            /**
             * 4.在rowNum中写入工程名，合并17列且居中，rowCucor++
             */
//            设置工程标题的行高
            mWritableSheet.setRowView(rowCucor,900);
//            合并单元格(列，行，列，行)
            mWritableSheet.mergeCells(0, rowCucor, 16, rowCucor);
            String title = measureTable.getEngineerName() + " - 实测实量记录表";
//            创建单元格的内容，将内容添加到0列，rowCucor行，并且指定内容和格式
            Label titleLabel = new Label(0, rowCucor, title, titleFormat);
            mWritableSheet.addCell(titleLabel);
            rowCucor++;
            /*============================工程标题创建完成================================*/
            /**
             *  5.接下来写入项目测量信息：
             *    5.1 写入项目名称合并4个单元格（0,rowCucor,3,rowCucor）
             *    5.2 写入检查楼栋楼层合并6个单元格(4,rowCucor,9,rowCucor)
             *    5.3 写入检查人并合并4个单元格（10,rowCucor,13,rowCucor）
             *    5.5 写入检查日期并合并3个单元格（14,rowCucor,17,rowCucor）
             *    5.6 rowCucor++
             */
            String projectName = "项目名称:" + measureTable.getProjectName();
            String checkFloor = "检查楼栋楼层:" + measureTable.getCheckFloor();
            String checkPerson = "检查人:" + measureTable.getCheckPerson();
            String checkDate = "检查日期:" + measureTable.getCheckDate();
//            设置合并单元格
            mWritableSheet.mergeCells(0, rowCucor, 3, rowCucor);
            mWritableSheet.mergeCells(4, rowCucor, 9, rowCucor);
            mWritableSheet.mergeCells(10, rowCucor, 13, rowCucor);
            mWritableSheet.mergeCells(14, rowCucor, 16, rowCucor);
//            创建单元格的内容
            Label projectLabel = new Label(0, rowCucor, projectName,contentFormat);
            Label checkFloorLabel = new Label(4, rowCucor, checkFloor,contentFormat);
            Label personLabel = new Label(10, rowCucor, checkPerson,contentFormat);
            Label dateLabel = new Label(14, rowCucor, checkDate,contentFormat);
            mWritableSheet.addCell(projectLabel);
            mWritableSheet.addCell(checkFloorLabel);
            mWritableSheet.addCell(personLabel);
            mWritableSheet.addCell(dateLabel);
            mWritableSheet.setRowView(rowCucor,500);
            rowCucor++;
            /*============================项目检查信息写入完成================================*/
            /**
             *  6.依次写入表头信息（序号、管控要点、质量标准、检查方法、01~10、实测数、合格数和合格率）
             */

//            写入单元格
            for (int i=0;i<titles.length;i++) {
                Label label = new Label(i, rowCucor, titles[i], tableTitleFormat);
                mWritableSheet.addCell(label);
            }
            rowCucor++;
            /*============================表头信息写入完成================================*/

            /**
             *  * 7.开启行循环
             *   7.1 初始化测量数据的行号(dataRowNum=rowCucor)
             *   7.2 根据data/10的结果加上是否有余数(有余+1，无余不加)来判断序号、管控要点、质量标准、检查方法、实测数、合格数和合格率竖向合并几个单元格
             *   7.3 rowCucor=rowCucor+合并个数
             *   7.4 写入除了data之外的数据
             *   7.5 开启data循环，依次将数据写入到01~10列下的单元格中，每10个单元格dataRowNum++
             */
            List<MeasureTableRow> rowList = measureTable.getRowList();

            for (int i=0; i<rowList.size();i++) {
//                数据内容的行号游标
                int dataRowCucor = rowCucor;
                List<String> dataList = rowList.get(i).getDatalist();
//                合并个数
                int mergeNum = dataList.size() / 10;
                if (dataList.size() % 10 == 0) {
                    /**
                     * 原因：例如需要合并3个单元格（0,1,2）
                     *      则实际合并到的那个行号是(3-1=2)
                     */
                    mergeNum--;
                }
//                开始进行合并单元格
                mWritableSheet.mergeCells(0, rowCucor, 0, rowCucor + mergeNum);
                mWritableSheet.mergeCells(1, rowCucor, 1, rowCucor + mergeNum);
                mWritableSheet.mergeCells(2, rowCucor, 2, rowCucor + mergeNum);
                mWritableSheet.mergeCells(3, rowCucor, 3, rowCucor + mergeNum);
                mWritableSheet.mergeCells(14, rowCucor, 14, rowCucor + mergeNum);
                mWritableSheet.mergeCells(15, rowCucor, 15, rowCucor + mergeNum);
                mWritableSheet.mergeCells(16, rowCucor, 16, rowCucor + mergeNum);
//                开始写入数据
                mWritableSheet.addCell(new Label(0,rowCucor,String.valueOf(rowList.get(i).getId()), contentFormat));
                mWritableSheet.addCell(new Label(1,rowCucor,rowList.get(i).getOptionName(), contentFormat));
                mWritableSheet.addCell(new Label(2,rowCucor,rowList.get(i).getStandard(), contentFormat));
                mWritableSheet.addCell(new Label(3,rowCucor,rowList.get(i).getCheckMethod(), contentFormat));
                mWritableSheet.addCell(new Label(14,rowCucor,String.valueOf(rowList.get(i).getRealMeasureNum()), contentFormat));
                mWritableSheet.addCell(new Label(15,rowCucor,String.valueOf(rowList.get(i).getQualifiedNum()), contentFormat));
                mWritableSheet.addCell(new Label(16,rowCucor,String.valueOf(rowList.get(i).getQualifiedRate()), contentFormat));
//                开始写入datalist的数据内容
                int dataColCucor = 4;
                for (int j = 0; j < dataList.size(); j++) {
                    mWritableSheet.addCell(new Label(dataColCucor,dataRowCucor,dataList.get(j), contentFormat));
//                    每执行完第十个数据就要开始换行，所以列游标移到开始，行游标+1
                    if (dataColCucor < 13) {
                        dataColCucor++;
                    } else {
                        dataColCucor = 4;
                    }
                    if (j != 0 && (j+1) % 10 == 0) {
                        dataRowCucor++;
                    }
                }
//                要换行了
                rowCucor = rowCucor + mergeNum + 1;

            }
            rowCucor++;
            Log.e("aaa", "addExcelData: 最后的行游标号："+rowCucor );



        } catch (RowsExceededException e) {

            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

    }

    public void write( IAddExcelResultCallBack callBack) {
        try {
            mWritableWorkbook.write();
            callBack.onSuccess(path);
        } catch (IOException e) {
            e.printStackTrace();
            callBack.onFail("写入异常");
        }
    }


    public void close() {
        try {
            if (mWritableWorkbook != null) {
                mWritableWorkbook.close();
                mWritableWorkbook = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

}
