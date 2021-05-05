package com.dextender.dextender_h2o;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.wear.R;

public class MyCanvas extends View {

    final static int MAX_DISPLAY_HOURS=3;       // Max hours to display
    final static int MAX_BG_VALUE=400;          // Max bg value the dex displays
    final static int graph_paddingEnd=30;       // 30 seems to be the point where you can see points on a round watch
    MyDatabase myDb = new MyDatabase(getContext());


    private Paint paint = new Paint();
    Context context;


    public MyCanvas (Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

    }


    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Tools tools = new Tools();                                                // Generic tools class

        int bgRecords=0;
        int lowLimit=0;
        int highLimit=0;
        String canvasBackground="0";
        boolean linePreference=true;
        long currentTime=System.currentTimeMillis()/1000;

        long[] longX = new long[36];
        int[]  intY  = new int[36];
        try {
            myDb.open();
            bgRecords=myDb.getBgDataAsArray(longX, intY);
            lowLimit=myDb.getLowLimit();
            highLimit=myDb.getHighLimit();
            canvasBackground=myDb.getCanvasBackground();
            linePreference=myDb.getChartLines();
            myDb.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        float h=canvas.getHeight();
        float w=canvas.getWidth();

        float y_divisor=MAX_BG_VALUE/h;      // ie. we'll have to convert a value of 100 to 20, since 100 is 25% of 400, and 20 is 25% of 80 (our displaymax)
        float x_multiplier=(w - 2*graph_paddingEnd)/(MAX_DISPLAY_HOURS*12);                         // w - 2*padding is how much visible space we have on a round watch
                                                                                                    // The x_multiplier is the calculated distance between each 5 minute point

        //------------------------------------------
        // Static - Based on canvas size
        //------------------------------------------
        if(canvasBackground.equals("0")) {
            paint.setColor(Color.TRANSPARENT);                                                      // Was TRANSPARENT / "CLEAR"
        }
        else {
            //mRenderer.setBackgroundColor(R.drawable.blue_red_widget);                                              // Was TRANSPARENT / "CLEAR"
            paint.setColor(Color.parseColor(canvasBackground));                                              // Was TRANSPARENT / "CLEAR"
        }
        canvas.drawRect(0,0,w,h, paint);


        //-----------------------------------------------------------------------------
        // Calculated - High/Low setting, determine ratio to screen size, then draw
        //-----------------------------------------------------------------------------
        //Log.d("MyCanvas", " High Value " + highLimit);
        paint.setColor(ContextCompat.getColor(context, R.color.gold));
        canvas.drawLine(0,(MAX_BG_VALUE-highLimit)/y_divisor, w, (MAX_BG_VALUE-highLimit)/y_divisor, paint );
        paint.setColor(ContextCompat.getColor(context, R.color.redline));
        canvas.drawLine(0,(MAX_BG_VALUE-lowLimit)/y_divisor, w, (MAX_BG_VALUE-lowLimit)/y_divisor, paint );

        //----------------------------
        // Draw time lines
        //----------------------------
        if(linePreference) {
            paint.setColor(Color.GRAY);
            float x1 = Math.round(w / 6);
            for (int i = 0; i < 6; i++) {
                canvas.drawLine(x1 * i, 0, x1 * i, h, paint);
            }
            canvas.drawLine(0, 0, w, 0, paint);
            canvas.drawLine(0, h - 2, w, h - 2, paint);

            //----------------------------
            // Draw the time
            //----------------------------

            canvas.save();
            canvas.rotate(-90);

            paint.setTextSize(12);
            paint.setColor(Color.WHITE);

            for (int i = 0; i < 6; i++) {
                canvas.drawText(tools.epoch2FmtTime(System.currentTimeMillis() / 1000 - (i * 1800), "HH:mm"), -1 * h + 10, w - (i * x1) - 3, paint);
            }
            canvas.restore();
        }

        //---------------------------------------------------------------------------------
        // For each point, calculate it's x and y. First, set the color of the point based
        // on its value vs the threshold, then place it in the graphing area
        //---------------------------------------------------------------------------------

        for(int i=0; i< bgRecords; i++) {
            if(intY[i] != 0) {
                if (intY[i] <= lowLimit) {
                    paint.setColor(ContextCompat.getColor(context, R.color.redline));
                } else {
                    if (intY[i] >= highLimit) {
                        paint.setColor(ContextCompat.getColor(context, R.color.gold));
                    } else {
                        paint.setColor(Color.WHITE);
                    }
                }

                //------------------------------------------------------------------------------------
                // Explanation for the 'x' part
                // w - padding is what we have to work with
                // starting from the right, subtract that value..
                // .. based on a 5 minute refresh, every point should be 'n' (aka x_multiplier) points
                // away from each other..
                //------------------------------------------------------------------------------------

                Float X=(w-graph_paddingEnd) - ( (currentTime-longX[i])/60/5 * x_multiplier);
                Float Y = (MAX_BG_VALUE - intY[i]) / y_divisor;
                //Log.d("Canvas", "Current time  " + currentTime + " and bg time " + longX[i]);
                //Log.d("Canvas ", "Calculated X " + X  + "| Calculated Y " + Y);
                canvas.drawCircle( X , Y, 3, paint);
            }
        }



    }





}
