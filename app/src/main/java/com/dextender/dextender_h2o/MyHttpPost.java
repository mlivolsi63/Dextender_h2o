package com.dextender.dextender_h2o;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


//-------------------------------------------------------------------------------------
// Class    : MyHttpPost
// Called by: Routines within MyService
// Author   : Mike LiVolsi / Originally by various (lots of examples on the web)
// Date     : Oct. 2014
//
// Purpose  : In keeping with the spirit of modularity, this class should be called
//            from the "services" routines.
//            Specifically to the application, once the service gets the value
//            from the USB, it should call this class and it's functions and post
//            this information to the web. Additionally, it should store this information
//            locally in a SQLite database. On the "transmit" end, the service should
//            post, while as a client, the service should "get" the results.
//            All this routine does is "post" and "get"
//-------------------------------------------------------------------------------------

public class MyHttpPost {

    //-------------------------------------------------------------------------------------
    // Method   : Call Home
    // Date     : June 2015
    // Author   : MLV
    //
    // Purpose  :
    //            Call the controller based on the IP address and port
    //            Send UID and the controller
    //            Response should be the URL + port + controller # (aka the server)
    //-------------------------------------------------------------------------------------
    public String callOperator(String url, String inUid, String inPwd, String inController) {

        url = url + "?account_id=" + inUid + "&account_pwd=" + inPwd + "&controller=" + inController + "&type=app";

        HttpParams httpParameters = new BasicHttpParams();
        //---------------------------------------------------
        // Set connection and socket timeouts
        //---------------------------------------------------
        int timeoutConnection = 2000;
        int timeoutSocket     = 2000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);


        StringBuilder result         = new StringBuilder();
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpGet request              = new HttpGet(url);
        HttpResponse response        = null;
        Boolean Rc=false;

        try {
            response = httpClient.execute(request);
            Rc=true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!Rc) {
            return "00000000|Operator failed to respond";
        }

        InputStream input = null;
        try {
            input = new BufferedInputStream(response.getEntity().getContent());
        }  catch (IOException e) {
            e.printStackTrace();
        }
        byte data[] = new byte[40000];

        int readContactsCount = 0;
        int currentByteReadCount;

        /** read response from inpus stream */
        try {
            while ((currentByteReadCount = input.read(data)) != -1) {
                String readData = new String(data, 0, currentByteReadCount);
                result.append(readData);

                // then +1 progress on every ...},{... (JSON object separator)
                if (readData.indexOf("}~{") >= 0) {
                    readContactsCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.d("MyHttpPost", "returning-->" + result.toString());
        return result.toString();

    }




    public String callHome(String url) {

        HttpParams httpParameters = new BasicHttpParams();
        //---------------------------------------------------
        // Set connection and socket timeouts
        //---------------------------------------------------
        int timeoutConnection = 3000;
        int timeoutSocket     = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);


        StringBuilder result         = new StringBuilder();
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpGet request              = new HttpGet(url);
        HttpResponse response        = null;
        boolean Rc=false;

        try {
            response = httpClient.execute(request);
            Rc=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( !Rc) {
            return "00000000|The target failed to respond";
        }

        InputStream input = null;
        try {
            input = new BufferedInputStream(response.getEntity().getContent());
        }  catch (IOException e) {
            e.printStackTrace();
        }
        byte data[] = new byte[40000];

        int readContactsCount = 0;
        int currentByteReadCount;

        /** read response from inpus stream */
        try {
            while ((currentByteReadCount = input.read(data)) != -1) {
                String readData = new String(data, 0, currentByteReadCount);
                result.append(readData);

                // then +1 progress on every ...},{... (JSON object separator)
                if (readData.indexOf("}~{") >= 0) {
                    readContactsCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Log.d("MyHttpPost", "Call home returning-->" + result.toString());
        return result.toString();
    }

}
