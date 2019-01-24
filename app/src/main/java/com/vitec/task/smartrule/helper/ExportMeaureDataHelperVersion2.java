package com.vitec.task.smartrule.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.MeasureData;
import com.vitec.task.smartrule.bean.MeasureTable;
import com.vitec.task.smartrule.bean.MeasureTableRow;
import com.vitec.task.smartrule.interfaces.IAddExcelResultCallBack;
import com.vitec.task.smartrule.utils.LogUtils;

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
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 导出测量数据到excel表格，需要对造模板表格来规划表格格式
 *
 *
 */
public class ExportMeaureDataHelperVersion2 {
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
    private WritableFont optionTitleFont;
    private WritableCellFormat optionsTitleFormat;
    //    public static final String path = Environment.getExternalStorageDirectory() + "/excel";

    public ExportMeaureDataHelperVersion2(Context context, String fileName) {
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
//                mWritableSheet.setColumnView(1, 20);
//                mWritableSheet.setColumnView(2, 20);
//                mWritableSheet.setColumnView(3, 50);
//                mWritableSheet.setColumnView(14, 20);
//                mWritableSheet.setColumnView(15, 20);
//                mWritableSheet.setColumnView(16, 20);
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
            titleFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            /***
             * 管控要点标题
             */

            optionTitleFont = new WritableFont(WritableFont.ARIAL, 15, WritableFont.NO_BOLD);
            optionsTitleFormat = new WritableCellFormat(optionTitleFont);
//            设置格式为居中
            optionsTitleFormat.setAlignment(Alignment.LEFT);
            optionsTitleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
//            设置边框线条
            optionsTitleFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
            optionsTitleFormat.setBorder(Border.RIGHT, BorderLineStyle.THIN);
            optionsTitleFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);

            /**
             * 表格的标题用的格式，
             */
            tableTiTleFont = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            tableTitleFormat = new WritableCellFormat(tableTiTleFont);
            tableTitleFormat.setAlignment(Alignment.CENTRE);
            tableTitleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            tableTitleFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

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
            mWritableSheet.mergeCells(0, rowCucor, 9, rowCucor);
            String title = measureTable.getProjectName()+ "项目实测实量";
//            创建单元格的内容，将内容添加到0列，rowCucor行，并且指定内容和格式
            Label titleLabel = new Label(0, rowCucor, title, titleFormat);
            mWritableSheet.addCell(titleLabel);
            rowCucor++;
            /*============================工程标题创建完成================================*/
            /**
             *  5.1接下来写入第一行的头部信息信息
             *    单位工程：1号楼    测量位置：3层
             */
//            设置合并单元格
            mWritableSheet.mergeCells(0, rowCucor, 1, rowCucor);
            mWritableSheet.mergeCells(2, rowCucor, 4, rowCucor);
            mWritableSheet.mergeCells(5, rowCucor, 6, rowCucor);
            mWritableSheet.mergeCells(7, rowCucor, 9, rowCucor);
//            创建单元格的内容
            Label label1 = new Label(0, rowCucor, "单位工程:",contentFormat);
            Label label2 = new Label(2, rowCucor, measureTable.getUnitEngineer(),contentFormat);
            Label label3 = new Label(5, rowCucor, "检查位置:",contentFormat);
            Label label4 = new Label(7, rowCucor, measureTable.getCheckFloor(),contentFormat);
            mWritableSheet.addCell(label1);
            mWritableSheet.addCell(label2);
            mWritableSheet.addCell(label3);
            mWritableSheet.addCell(label4);
            mWritableSheet.setRowView(rowCucor,500);
            rowCucor++;
            /**
             *  5.2接下来写入第3行的头部信息信息
             *    工程类型：混凝土工程    检查人：张三
             */
//            设置合并单元格
            mWritableSheet.mergeCells(0, rowCucor, 1, rowCucor);
            mWritableSheet.mergeCells(2, rowCucor, 4, rowCucor);
            mWritableSheet.mergeCells(5, rowCucor, 6, rowCucor);
            mWritableSheet.mergeCells(7, rowCucor, 9, rowCucor);
//            创建单元格的内容
            Label label5 = new Label(0, rowCucor, "工程类型:",contentFormat);
            Label label6 = new Label(2, rowCucor, measureTable.getEngineerName(),contentFormat);
            Label label7 = new Label(5, rowCucor, "检查人:",contentFormat);
            Label label8 = new Label(7, rowCucor, measureTable.getCheckPerson(),contentFormat);
            mWritableSheet.addCell(label5);
            mWritableSheet.addCell(label6);
            mWritableSheet.addCell(label7);
            mWritableSheet.addCell(label8);
            mWritableSheet.setRowView(rowCucor,500);
            rowCucor++;
            /**
             * 5.3 写入测量时间
             */
            mWritableSheet.mergeCells(0, rowCucor, 1, rowCucor);
            mWritableSheet.mergeCells(2, rowCucor, 9, rowCucor);
            mWritableSheet.setRowView(rowCucor,500);
            //            创建单元格的内容
            Label label9 = new Label(0, rowCucor, "测量时间:",contentFormat);
            Label label10 = new Label(2, rowCucor, measureTable.getCheckDate(),contentFormat);
            mWritableSheet.addCell(label9);
            mWritableSheet.addCell(label10);
            mWritableSheet.setRowView(rowCucor,500);
            rowCucor++;
            /*============================项目检查信息写入完成================================*/

            /**
             * 6.开始写入图片：
             */

            if (measureTable.getPicPath() != null && !measureTable.getPicPath().equals("")) {
                //设置图片的单元格
                mWritableSheet.mergeCells(0, rowCucor, 9, (rowCucor + 27));
                File imgFile = new File(measureTable.getPicPath());
                if (!imgFile.exists()) {
                    try {
                        imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getPath());
                double width = bitmap.getWidth();
                double height = bitmap.getHeight();
                double imgW = (width / height) * 27 / 3.8;
                double startX = (9 - imgW) / 2;
                WritableImage image = new WritableImage(startX, rowCucor+0.5, imgW, 27, imgFile);
                mWritableSheet.addImage(image);
                rowCucor += 28;
            } else {
                //设置图片的单元格
                mWritableSheet.mergeCells(0, rowCucor, 9, rowCucor);
                rowCucor++;
            }

            /*======================图片写入完成===============================*/
            /**
             * 7.循环写入管控要点
             */
            List<MeasureTableRow> rowList = measureTable.getRowList();
            for (int i=0;i<rowList.size();i++) {
                MeasureTableRow row = rowList.get(i);
                if (row.getOptionType() >2) {
                    continue;
                }
                rowCucor++;
//                mWritableSheet.mergeCells(0, rowCucor, 9, rowCucor);
//                LogUtils.show("导出文件工具类---查看logo图片路径：" + row.getLogoFile().getPath());
//                LogUtils.show("导出文件工具类---查看文件名：" + row.getOptionName());

                //标题: 垂直度
//                Label label11 = new Label(1, rowCucor, row.getOptionName()+"",contentFormat);
//                mWritableSheet.addCell(label11);
//                mWritableSheet.setRowView(rowCucor,500);
//                LogUtils.show("导出文件工具类---打印当前信息："+row.toString());
//
//                File file = row.getLogoFile();
//                LogUtils.show("打印文件相关信息-----是否存在："+file.exists());
//                WritableImage image = new WritableImage(0, rowCucor, 1, rowCucor, row.getLogoFile());
//
//                mWritableSheet.addImage(image);
//                rowCucor++;
                File file = row.getLogoFile();
                if (file != null) {
                    mWritableSheet.mergeCells(0, rowCucor, 0, rowCucor+1);
                    LogUtils.show("导出文件---查看logo文件是否存在："+file.exists()+",查看路径："+file.getPath());
                    WritableImage writableImage = new WritableImage(0.3, rowCucor, 0.6, 2, file);
                    WritableCellFormat  optionsTitleFormat = new WritableCellFormat(optionTitleFont);
//            设置边框线条
                    optionsTitleFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
                    optionsTitleFormat.setBorder(Border.LEFT, BorderLineStyle.THIN);
                    optionsTitleFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
                    Label lab = new Label(0, rowCucor, "",optionsTitleFormat);
                    mWritableSheet.addCell(lab);
                    mWritableSheet.addImage(writableImage);
                }


                mWritableSheet.mergeCells(1, rowCucor, 9, rowCucor+1);
                Label tL = new Label(1, rowCucor, row.getOptionName(), optionsTitleFormat);
                mWritableSheet.addCell(tL);
//                mWritableSheet.setRowView(rowCucor, 500);
                rowCucor+=2;

                //统计行,合并单元格
                mWritableSheet.mergeCells(0, rowCucor, 1, rowCucor);//检查点数
                mWritableSheet.mergeCells(3, rowCucor, 4, rowCucor);//合格点数
                mWritableSheet.mergeCells(6, rowCucor, 7, rowCucor);//合格率
                mWritableSheet.mergeCells(8, rowCucor, 9, rowCucor);//合格率的值
//                写入值
                Label lab1 = new Label(0, rowCucor, "检查点数:",contentFormat);
                Label lab2 = new Label(2, rowCucor, String.valueOf(row.getRealMeasureNum()),contentFormat);
                Label lab3 = new Label(3, rowCucor, "合格点数:",contentFormat);
                Label lab4 = new Label(5, rowCucor, String.valueOf(row.getQualifiedNum()),contentFormat);
                Label lab5 = new Label(6, rowCucor, "合格率:",contentFormat);
                Label lab6 = new Label(8, rowCucor, String.valueOf(row.getQualifiedRate()),contentFormat);
                mWritableSheet.addCell(lab1);
                mWritableSheet.addCell(lab2);
                mWritableSheet.addCell(lab3);
                mWritableSheet.addCell(lab4);
                mWritableSheet.addCell(lab5);
                mWritableSheet.addCell(lab6);
                mWritableSheet.setRowView(rowCucor,500);
                rowCucor++;
                //统计行完毕.............

                //开始循环写入数值
                List<MeasureData> dataList = row.getDatalist();
                int colCursor = 0;
                for (int j=0;j<dataList.size();j++) {
                    if (colCursor == 0) {
                        Label lab7 = new Label(colCursor, rowCucor,"序号",contentFormat);
                        mWritableSheet.addCell(lab7);
                        Label lab8 = new Label(colCursor, rowCucor+1,"数值",contentFormat);
                        mWritableSheet.addCell(lab8);
                        colCursor++;
                    }
                    Label lab7 = new Label(colCursor, rowCucor,String.valueOf(dataList.get(j).getId()),contentFormat);
                    Label lab8 = new Label(colCursor, rowCucor+1,dataList.get(j).getData(),contentFormat);
                    mWritableSheet.addCell(lab7);
                    mWritableSheet.addCell(lab8);
                    mWritableSheet.setRowView(rowCucor,400);
                    mWritableSheet.setRowView(rowCucor+1,500);
                    colCursor++;
                    if (colCursor == 10) {
                        colCursor = 0;
                        rowCucor += 2;
                    }
                }
                //补填当前行
                if (colCursor < 9) {
                    do {
                        Label lab7 = new Label(colCursor, rowCucor," ",contentFormat);
                        Label lab8 = new Label(colCursor, rowCucor+1," ",contentFormat);
                        mWritableSheet.addCell(lab8);
                        mWritableSheet.addCell(lab7);
                        colCursor++;
                    } while (colCursor < 10);
                }
                rowCucor += 2;
                mWritableSheet.mergeCells(0, rowCucor, 9, rowCucor);
                rowCucor++;
            }
            rowCucor++;
            mWritableSheet.mergeCells(0, rowCucor, 9, rowCucor+1);


            Log.e("aaa", "addExcelData: 最后的行游标号："+rowCucor );



        } catch (RowsExceededException e) {

            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

    }

    public void write( IAddExcelResultCallBack callBack) {
        try {
            LogUtils.show("=====导出完成------------------");
            mWritableWorkbook.write();
//            close();
            callBack.onSuccess(path);

        } catch (IOException e) {
            e.printStackTrace();
            callBack.onFail("表格写入异常:"+e.getMessage());
        }
    }


    public void close() {
        try {
            LogUtils.show("=====表格关闭了.....");
            if (mWritableWorkbook != null) {
                mWritableWorkbook.close();
                mWritableWorkbook = null;
            }

        } catch (IOException e) {
            LogUtils.show("表格关闭异常----"+e.getMessage());
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
            LogUtils.show("表格关闭异常----"+e.getMessage());
        }
    }

}
