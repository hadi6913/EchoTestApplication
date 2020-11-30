package com.ar.echodualitestapplicationnew;

import java.text.SimpleDateFormat;

/**
 * Created by Mohsen on 9/17/2019.
 */

public class ComBean {
    public byte[] bRec=null;
    public String sRecTime="";
    public String sComPort="";
    public ComBean(String sPort, byte[] buffer, int size){
        sComPort=sPort;
        bRec=new byte[size];
        for (int i = 0; i < size; i++)
        {
            bRec[i]=buffer[i];
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
        sRecTime = sDateFormat.format(new java.util.Date());
    }
}
