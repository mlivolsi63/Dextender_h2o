package com.dextender.dextender_h2o;


public class MyRowStructureForJobs {
    public Integer  imgIcon;
    public String   field1;
    public String   field2;
    public String   field3;
    public String   field4;
    public String   field5;
    public String   field6;
    public String   field7;
    public String   field8;

    public MyRowStructureForJobs() {
        super();
    }


    public void thisRow(Integer imgIcon, String inField1, String inField2, String inField3, String inField4,
                        String inField5, String inField6, String inField7, String inField8) {
        this.imgIcon  = imgIcon;
        this.field1 = inField1;
        this.field2 = inField2;
        this.field3 = inField3;
        this.field4 = inField4;
        this.field5 = inField5;
        this.field6 = inField6;
        this.field7 = inField7;
        this.field8 = inField8;
    }
}

