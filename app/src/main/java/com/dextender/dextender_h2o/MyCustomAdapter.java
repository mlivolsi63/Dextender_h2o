package com.dextender.dextender_h2o;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class MyCustomAdapter extends ArrayAdapter<MyRowStructure>{

    // This is the constructor
     MyCustomAdapter(Context context, MyRowStructure[] inRow) {  // setting
        super(context, R.layout.fancy_row01, inRow);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater rowInflater = LayoutInflater.from(getContext());
        View customView = rowInflater.inflate(R.layout.fancy_row01, parent, false);

        MyRowStructure singleRowItem  = getItem(position);
        TextView singleRowText = (TextView) customView.findViewById(R.id.fancy_row_text);
        ImageView singleRowImg = (ImageView) customView.findViewById(R.id.fancy_row_img);

        singleRowText.setText(singleRowItem.txtTitle);
        singleRowImg.setImageResource(singleRowItem.imgIcon);

        return customView;
    }
}
