package com.example.cyclesearch;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    // create FileWriter object with file as parameter
    private FileWriter outputfile;

    // create CSVWriter object filewriter object as parameter
    private CSVWriter writer;

    private int row;
    private File file;
    private FileOutputStream out;

    public Excel() {}

    public Excel(File file, FileWriter outputfile, CSVWriter writer) {
        this.file = file;
        row = 1;
        this.outputfile = outputfile;
        this.writer = writer;
    }

    public void writeData(ArrayList<Float> Acc, ArrayList<Float> Gyro, long timestamp, String activity) {
        // add data to csv
        String[] data1 = { Acc.get(0).toString(), Acc.get(1).toString(), Acc.get(2).toString(),
                Gyro.get(0).toString(), Gyro.get(1).toString(), Gyro.get(2).toString(),
                String.valueOf(timestamp), activity};
        writer.writeNext(data1);

    }

    public void resetRow() {
        row = 1;
    }

}
