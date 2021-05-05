package com.dextender.dextender_h2o;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class MyCustomAdapterForLogs extends ArrayAdapter<MyRowStructureForLogs>{

    // This is the constructor
     MyCustomAdapterForLogs(Context context, MyRowStructureForLogs[] inRow) {  // setting
        super(context, R.layout.fancy_logrow, inRow);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater rowInflater = LayoutInflater.from(getContext());
        View customView = rowInflater.inflate(R.layout.fancy_logrow, parent, false);

        MyRowStructureForLogs singleRowItem  = getItem(position);

        TextView  rowText1 = (TextView)  customView.findViewById(R.id.fancyLogRowText1);
        TextView  rowText2 = (TextView)  customView.findViewById(R.id.fancyLogRowText2);
        ImageView rowImg   = (ImageView) customView.findViewById(R.id.fancyLogRowImg);

        rowText1.setText(singleRowItem.field1);
        rowText2.setText(singleRowItem.field2);
        rowImg.setImageResource(singleRowItem.imgIcon);

        return customView;
    }
}
