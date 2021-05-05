package com.dextender.dextender_h2o;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class MyCustomAdapterForJobs extends ArrayAdapter<MyRowStructureForJobs>{

    // This is the constructor
     MyCustomAdapterForJobs(Context context, MyRowStructureForJobs[] inRow) {  // setting
        super(context, R.layout.fancy_row02, inRow);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater rowInflater = LayoutInflater.from(getContext());
        View customView = rowInflater.inflate(R.layout.fancy_row02, parent, false);

        MyRowStructureForJobs singleRowItem  = getItem(position);
        ImageView singleRowImg = (ImageView) customView.findViewById(R.id.fancyRow02Img);

        TextView text1 = (TextView) customView.findViewById(R.id.fancyRow02text1);
        TextView text2 = (TextView) customView.findViewById(R.id.fancyRow02text2);
        TextView text3 = (TextView) customView.findViewById(R.id.fancyRow02text3);
        TextView text4 = (TextView) customView.findViewById(R.id.fancyRow02text4);
        TextView text5 = (TextView) customView.findViewById(R.id.fancyRow02text5);
        TextView text6 = (TextView) customView.findViewById(R.id.fancyRow02text6);
        TextView text7 = (TextView) customView.findViewById(R.id.fancyRow02text7);
        TextView text8 = (TextView) customView.findViewById(R.id.fancyRow02text8);


        singleRowImg.setImageResource(singleRowItem.imgIcon);
        text1.setText(singleRowItem.field1);
        text2.setText(singleRowItem.field2);
        text3.setText(singleRowItem.field3);
        text4.setText(singleRowItem.field4);
        text5.setText(singleRowItem.field5);
        text6.setText(singleRowItem.field6);
        text7.setText(singleRowItem.field7);
        text8.setText(singleRowItem.field8);

        return customView;
    }
}
