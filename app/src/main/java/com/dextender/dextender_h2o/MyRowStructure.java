package com.dextender.dextender_h2o;


public class MyRowStructure {
    public Integer  imgIcon;
    public String   txtTitle;
    public MyRowStructure() {
        super();
    }


    public void thisRow(Integer imgIcon, String txtTitle) {
        this.imgIcon  = imgIcon;
        this.txtTitle = txtTitle;
    }
}
