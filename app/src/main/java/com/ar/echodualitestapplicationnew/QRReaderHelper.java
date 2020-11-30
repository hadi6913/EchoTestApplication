package com.ar.echodualitestapplicationnew;

import com.scsoft.libecho5.BarcodeScanner;

import java.io.FileDescriptor;

public class QRReaderHelper {
    private static QRReaderHelper qrReaderHelper = null;
    private Object lock = new Object();
    private BarcodeScanner scanner;
    FileDescriptor fd;
    int QR_Timeout = 50;
    int QR_Trig_Time = 5;

    public QRReaderHelper() {
        initial();
    }

    public void initial() {
        try {
            scanner = new BarcodeScanner();
            // TODO: 6/8/2020 set below baudrate
            fd = scanner.BarcodeScanner_OpenPort(9600);
            scanner.BarcodeScanner_TriggerPulse(QR_Trig_Time);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static QRReaderHelper getInstance() {
        if (qrReaderHelper == null)
            qrReaderHelper = new QRReaderHelper();
        return qrReaderHelper;
    }

    public void setTrigger(boolean status){
        scanner.BarcodeScanner_Trigger(status);
    }
    public String readQRTicket() {
        try {

            String ret = "";
            //scanner.ignorePrevBuffer();
            int i = 0;
            boolean oneMore = false;
            while (i < 5 || oneMore) {
                byte[] buf = new byte[200];

                int bytesread = scanner.BarcodeScanner_ReadData(fd, buf, buf.length, QR_Timeout);
                ret += new String(buf);
                if (bytesread > 0)
                    return ret;
                if (i == 4 && bytesread > 0)
                    oneMore = !oneMore;
                i++;
                Thread.sleep(5);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}

