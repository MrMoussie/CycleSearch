package com.example.cyclesearch;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Excel {
    private static final int ACC_X = 0;
    private static final int ACC_Y = 1;
    private static final int ACC_Z = 2;
    private static final int GYRO_X = 3;
    private static final int GYRO_Y = 4;
    private static final int GYRO_Z = 5;
    private static final int TIMESTAMP = 6;
    private static final int ACTIVITY = 7;

    private HSSFWorkbook wb;
    private HSSFSheet sheet;
    private int row;
    private File file;
    private FileOutputStream out;

    public Excel() {}

    public Excel(FileOutputStream out, HSSFWorkbook wb, HSSFSheet sheet, File file) {
        this.out = out;
        this.wb = wb;
        this.sheet = sheet;
        this.file = file;
        row = 1;
    }

    public void writeData(ArrayList<Float> Acc, ArrayList<Float> Gyro, long timestamp, String activity) {
        HSSFRow rowHead = sheet.createRow((short)(row-1));
        rowHead.createCell(ACC_X).setCellValue(Acc.get(0));
        rowHead.createCell(ACC_Y).setCellValue(Acc.get(1));
        rowHead.createCell(ACC_Z).setCellValue(Acc.get(2));
        rowHead.createCell(GYRO_X).setCellValue(Gyro.get(0));
        rowHead.createCell(GYRO_Y).setCellValue(Gyro.get(1));
        rowHead.createCell(GYRO_Z).setCellValue(Gyro.get(2));
        rowHead.createCell(TIMESTAMP).setCellValue(timestamp);
        rowHead.createCell(ACTIVITY).setCellValue(activity);
        try {
            wb.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        row++;
    }

    public void resetRow() {
        row = 1;
    }

    public void finishSheet() {
        try{
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
