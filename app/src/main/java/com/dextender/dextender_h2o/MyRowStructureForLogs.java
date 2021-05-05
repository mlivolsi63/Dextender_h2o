package com.dextender.dextender_h2o;


public class MyRowStructureForLogs {
    public Integer  imgIcon;
    public String   field1;
    public String   field2;

    public MyRowStructureForLogs() {
        super();
    }


    public void thisRow(Integer imgIcon, String inField1, String inField2) {
        this.imgIcon = imgIcon;
        this.field1  = inField1;
        this.field2  = inField2;
    }
}

