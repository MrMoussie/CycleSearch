package com.example.cyclesearch;

import com.opencsv.CSVWriter;
import java.util.ArrayList;

public class Excel {

    // create CSVWriter object filewriter object as parameter
    private final CSVWriter writer;

    public Excel(CSVWriter writer) {
        this.writer = writer;
    }

    /**
     * Method used to add a entry set (data from sensors) to the csv file
     * @param Acc arraylist of three components of the measurement (X,Y,Z)
     * @param Gyro arraylist of three components of the measurement (X,Y,Z)
     * @param timestamp time when the measurement was performed
     * @param activity type of activity that was performed
     */
    public void writeData(ArrayList<Float> Acc, ArrayList<Float> Gyro, long timestamp, String activity) {
        String[] data1 = { Acc.get(0).toString(), Acc.get(1).toString(), Acc.get(2).toString(),
                Gyro.get(0).toString(), Gyro.get(1).toString(), Gyro.get(2).toString(),
                String.valueOf(timestamp), activity};
        writer.writeNext(data1);

    }
}
