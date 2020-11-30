package com.ar.echodualitestapplicationnew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.LoudnessEnhancer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.potterhsu.Pinger;
import com.scsoft.libecho5.Gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import de.markusfisch.android.cameraview.widget.CameraView;

public class MainActivity extends AppCompatActivity implements LocationHandler.Callbacks {
    private static Logger log = LoggerFactory.getLogger(MainActivity.class);
    private ImageView qrResImg, samResImg, audioResImg, cardResImg, fourGResImg, gpsResImg, cameraResImg;
    private TextView buildVersion, kernelVersion, osVersion, samOne, samTwo, samThree, samFour, scannedTxt, deviceID, totalCases, totalPassedCases, totalFailedCases, cardDetection, uploadResTxt, simcartTxt, gps, imeiOne, imeiTwo, appVersion;
    private SeekBar volumeSeekbar;
    private Button startPreview, stopPreview;
    private MediaPlayer mediaPlayer;
    private CheckBox maxSpeaker;
    private LoudnessEnhancer loudnessEnhancer;
    private AudioManager audioManager;
    private SAM sam;
    private CaseChecker caseChecker;
    private LocationListener locationListener;
    private CameraView cameraView;
    private Intent locationHandlerIntent;
    private LocationHandler locationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUiComponent();
        //initialGPIOs();
        new InitialProcess().execute();

    }

    private void startLocService(){
        try{
            locationHandlerIntent = new Intent(getApplicationContext(), LocationHandler.class);
            startService(locationHandlerIntent);
            getApplicationContext().bindService(locationHandlerIntent, locationHandlerConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception ex){
            log.error("MainActivity > startLocService():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    private void stopLocService(){
        try{
            getApplicationContext().unbindService(locationHandlerConnection);
            stopService(locationHandlerIntent);
        }catch (Exception ex){
            log.error("MainActivity > stopLocService():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    private ServiceConnection locationHandlerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationHandler.LocalBinder binder = (LocationHandler.LocalBinder) service;
            locationHandler = binder.getServiceInstance();
            locationHandler.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private void setOSBuildNumber() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buildVersion.setText(Build.DISPLAY);
                    kernelVersion.setText(System.getProperty("os.version"));
                    osVersion.setText(Build.VERSION.RELEASE);
                    appVersion.setText(getAppVersionName());
                }
            });
        } catch (Exception ex) {
            log.error("MainActivity > setOSBuildNumber():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    public String getAppVersionName() {
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
            return info.versionName;
        } catch (Exception ex) {
            log.error("MainActivity > getAppVersionName():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
            return "UNKNOWN";
        }
    }

    private Camera getCameraInstance(){
        Camera c = null;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
                Camera.getCameraInfo(cameraIndex, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        return Camera.open(cameraIndex);
                    }
                    catch (RuntimeException ex) {
                        log.error("MainActivity > getCameraInstance1():" + ex.getMessage());
                        StringWriter errors = new StringWriter();
                        ex.printStackTrace(new PrintWriter(errors));
                        log.error(errors.toString());
                    }
                }
            }
        }
        catch (Exception ex){
            log.error("MainActivity > getCameraInstance2():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.close();
        stopLocService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null)
            cameraView.openAsync(CameraView.findCameraId(
                    Camera.CameraInfo.CAMERA_FACING_FRONT));
    }

    public void initCamera(View view){
        try{
            cameraView = findViewById(R.id.camera_view);
            cameraView.setOnCameraListener(new CameraView.OnCameraListener() {
                @Override
                public void onConfigureParameters(Camera.Parameters parameters) {

                }

                @Override
                public void onCameraError() {

                }

                @Override
                public void onCameraReady(Camera camera) {

                }

                @Override
                public void onPreviewStarted(Camera camera) {

                }

                @Override
                public void onCameraStopping(Camera camera) {

                }


            });
            cameraView.openAsync(CameraView.findCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT));

        }catch (Exception ex){
            log.error("MainActivity > initCamera():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }



    private void initialGPIOs() {
        try {
            //init card reader
            GPIOWrapper gpioWrapper = new GPIOWrapper();
            // TODO: 6/6/2020 uncomment below
            Gpio gpio = new Gpio();
            //gpio.BarcodeScanner_Disable();
            gpioWrapper.BarcodeScanner_Disable();
            gpio.Sam_Enable();
            gpio.CtlsReader_Enable();

            //init sam
            gpioWrapper.Sam_Disable();
            Thread.sleep(1000);
            gpioWrapper.Sam_Enable();
            Thread.sleep(1000);

        } catch (Exception ex) {
            log.error("MainActivity > initialGPIOs():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    @Override
    public void successSignal() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gps.setText("Provider is enable, data has been fetched successfully");
                gps.setBackgroundResource(R.drawable.check_bg);
                gpsResImg.setImageResource(R.drawable.checked);
                DataHandler.getInstance().setGps(true);
            }
        });
    }

    @Override
    public void disabledProvider() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gps.setText("Provider is disable!");
            }
        });
    }

    @Override
    public void enabledProvider() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gps.setText("Provider is enable, searching for data...");
            }
        });
    }

    private class InitialProcess extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            setOSBuildNumber();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setUiComponent();
            initialUIMembers();
            initTestResultsOnUI();
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //*********************************************************
            sam = new SAM();
            sam.start();
            //*********************************************************
            CardDetector cardDetector = new CardDetector();
            cardDetector.start();
            //*********************************************************



            initialSpeaker();



            //*********************************************************
            QRScanner qrScanner = new QRScanner();
            qrScanner.start();
            //*********************************************************
            FourGTest fourGTest = new FourGTest();
            fourGTest.start();
            //*********************************************************
            caseChecker = new CaseChecker();
            caseChecker.start();
            //*********************************************************





            //initGPS();
            startLocService();
            //initCamera();
        }
    }


    public void initGPS() {
        try {
            initListener();
            LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (locationListener != null)
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (Exception ex) {
            log.error("MainActivity > initGPS():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    private void initListener() {
        try{
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gps.setText("Provider is enable, data has been fetched successfully");
                            gps.setBackgroundResource(R.drawable.check_bg);
                            gpsResImg.setImageResource(R.drawable.checked);
                            DataHandler.getInstance().setGps(true);
                        }
                    });
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    //nothing
                }

                @Override
                public void onProviderEnabled(String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gps.setText("Provider is enable, searching for data...");
                        }
                    });
                }

                @Override
                public void onProviderDisabled(String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gps.setText("Provider is disable!");
                        }
                    });
                }
            };
        }catch (Exception ex){
            log.error("MainActivity > initListener():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    private class CardDetector extends Thread{
        private SerialPortWrapper readerSerialPortWrapper;
        private byte[] Anti_Collision_CMD = new byte[]{ 0x02, 0x00, 0x01, 0x23, 0x00 };
        private byte[] F_Card = new byte[]{0x02, 0x00, 0x05, 0x4C, 0x00, 0x00, 0x01, 0x00, 0x48};
        private byte[] REQA = new byte[]{ 0x02, 0x00, 0x01, 0x21, 0x00 };
        private byte[] RF_OFF = new byte[]{ 0x02, 0x00, 0x01, 0x11, 0x00 };
        private byte[] Select_CMD = new byte[]{ 0x02, 0x00, 0x05, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00 };

        public CardDetector() {
            try {
                readerSerialPortWrapper = new SerialPortWrapper();
                readerSerialPortWrapper.setPort("/dev/ttyMT3");
                readerSerialPortWrapper.setBaudRate(115200);
                readerSerialPortWrapper.open();
            }catch (Exception ex){
                log.error("CardDetector > CardDetector():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }

        @Override
        public void run() {
            super.run();
            try{
                while (readerSerialPortWrapper.isOpen()){
                    FindCardResult result = searchingCard();
                    if (result.getCardType().equals(CardType.Mifare_1K) || result.getCardType().equals(CardType.Mifare_4K) || result.getCardType().equals(CardType.DESFire)){
                        //detected
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardDetection.setText("Smart cart has been detected");
                                cardDetection.setBackgroundResource(R.drawable.check_bg);
                                cardResImg.setImageResource(R.drawable.checked);
                                DataHandler.getInstance().setCard(true);
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardDetection.setText("NOTHING...  Searching for card");
                            }
                        });
                    }
                    Thread.sleep(500);
                }
            }catch (Exception ex){
                log.error("CardDetector > run():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }

        public class SerialPortWrapper extends SerialHelperReader{

            public SerialPortWrapper() {
            }

            @Override
            protected void onDataReceived(ComBean ComRecData) {

            }
        }

        private FindCardResult searchingCard(){
            FindCardResult result = new FindCardResult();
            try{
                byte[] res = executeCMDonReader(RF_OFF);
                res = executeCMDonReader(REQA);

                if (res == null) {
                    return result;
                }

                if (ByteUtils.match(res[0], 0x00) && (res.length == 4)) {
                    if ((ByteUtils.match(res[1], 0x04) && ByteUtils.match(res[2], 0x00)) || (ByteUtils.match(res[1], 0x02) && ByteUtils.match(res[2], 0x00))) {
                        byte[] innerRes = antiCollide();
                        if (innerRes == null) {
                            return result;
                        }
                        if (innerRes.length >= 4) {
                            byte sak = selectCard(innerRes);
                            if (ByteUtils.match(sak, 0x08) || ByteUtils.match(sak, 0x38) || ByteUtils.match(sak, 0x18)) {
                                List<Byte> sn = new ArrayList<>();
                                for (int i = 3; i >= 0; i--) {
                                    sn.add(innerRes[i]);
                                }
                                result.setCardType(CardType.Mifare_1K);
                                result.setCardSerialNumber(ByteUtils.getValue(sn));
                                result.setCardId(innerRes);
                                return result;
                            }
                        }
                    }
                    else if (ByteUtils.match(res[1], 0x44) && ByteUtils.match(res[2], 0x03))
                    {
                        //Desfire Detected
                        byte[] innerRes = executeCMDonReader(F_Card);
                        if (innerRes == null)
                            return result;

                        if (innerRes.length == 17) {
                            result.setCardType(CardType.DESFire);
                            return result;
                        }
                        else if (ByteUtils.match(innerRes[0], 2)) {
                            return result;
                        }

                    }
                }
                else if (ByteUtils.match(res[0], 2)) {
                    return result;
                }
            }catch (Exception ex){
                log.error("CardDetector > searchingCard():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
            return result;
        }

        private byte selectCard(byte[] cardSerialNumber){
            try {
                for (int i = 0; i < 4; i++) {
                    Select_CMD[4 + i] = cardSerialNumber[i];
                }
                byte[] res = executeCMDonReader(Select_CMD);

                if (res != null) {
                    if (ByteUtils.match(res[0], 0)) {
                        return res[1];
                    }
                }
            }
            catch (Exception ex)
            {
                log.error("CardDetector > selectCard():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
            return 0x00;
        }

        private byte[] executeCMDonReader(byte[] cmd){
            try{
                readerSerialPortWrapper.send(generateCMDForReader(cmd));
                byte[] res = readerSerialPortWrapper.readReader();
                return res;
            }catch (Exception ex){
                log.error("CardDetector > executeCMDonReader():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
            return null;
        }

        private byte[] generateCMDForReader(byte[] data){
            try{
                data[data.length - 1] = 0;
                for (int i = 1; i < data.length - 1; i++)
                    data[data.length - 1] = (byte)((int)data[data.length - 1] ^ (int)data[i]);
            }catch (Exception ex){
                log.error("CardDetector > generateCMDForReader():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
            return data;
        }

        private byte[] antiCollide(){
            try {
                byte[] cardSerialNumber = new byte[4];
                byte[] res = executeCMDonReader(Anti_Collision_CMD);
                if (res == null)
                    return null;

                if (res != null && ByteUtils.match(res[0], 0) && res.length == 6)
                {
                    for (int i = 1; i < 5; i++)
                    {
                        cardSerialNumber[i - 1] = res[i];
                    }
                    return cardSerialNumber;
                }
            }
            catch (Exception ex)
            {
                log.error("CardDetector > antiCollide():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
            return null;
        }
    }

    private class QRScanner extends Thread{
        public QRScanner() {
            try{
                final String devID = DataHandler.getInstance().getDevID();
                if (!devID.equals("0")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceID.setText("Device ID : " + devID.trim());
                        }
                    });
                }
            }catch (Exception ex){
                log.error("QRScanner > QRScanner():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }

        @Override
        public void run() {
            super.run();
            try{
                while (true){
                    handleQRTicket();
                    Thread.sleep(20);
                }
            }catch (Exception ex){
                log.error("QRScanner > run():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }

        public void handleQRTicket() {
            try {
                QRReaderHelper.getInstance().setTrigger(false);
                final String barCode = QRReaderHelper.getInstance().readQRTicket();
                if (barCode != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedTxt.setText(barCode.trim());
                            if ((barCode.trim().length() == 14 || barCode.trim().length() == 13) && barCode.trim().startsWith("E5")) {
                                scannedTxt.setBackgroundResource(R.drawable.check_bg);
                                DataHandler.getInstance().setScanner(true);
                                DataHandler.getInstance().setDevID(barCode.trim());
                                deviceID.setText("Device ID : " + barCode.trim());
                                qrResImg.setImageResource(R.drawable.checked);
                            }else{
                                scannedTxt.setBackgroundResource(R.drawable.error_bg);
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                log.error("QRScanner > handleQRTicket():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            } finally {
                QRReaderHelper.getInstance().setTrigger(true);
            }
        }
    }


    private class FourGTest extends Thread {
        public FourGTest() {

        }

        @Override
        public void run() {
            super.run();
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        simcartTxt.setText("Sim card detected, pinging 'Baidu.com'...");
                    }
                });
                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                final String imeiOne_ = manager.getDeviceId(0);
                final String imeiTwo_ = manager.getDeviceId(1);
                DataHandler.getInstance().setImeiOne(imeiOne_);
                DataHandler.getInstance().setImeiTwo(imeiTwo_);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imeiOne.setText("IMEI 1: "+imeiOne_);
                        imeiTwo.setText("IMEI 2: "+imeiTwo_);
                    }
                });
                while (true) {
                    if (getSimStatus()) {
                        //simcart detected
                        Pinger pinger = new Pinger();
                        if(pinger.ping("www.baidu.com", 2)){
                            //ping successfully done
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    simcartTxt.setText("Sim card detected, ping 'Baidu.com' done");
                                    simcartTxt.setBackgroundResource(R.drawable.check_bg);
                                    fourGResImg.setImageResource(R.drawable.checked);
                                    DataHandler.getInstance().setFourG(true);
                                }
                            });
                            break;
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    simcartTxt.setText("Sim card detected, failed to ping 'Baidu.com'...");
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                simcartTxt.setText("Sim card absent or is in unknown situation, please use valid sim");
                            }
                        });
                    }
                    Thread.sleep(1000);
                }
            }catch (Exception ex){
                log.error("FourGTest > run():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }

        private boolean getSimStatus(){
            TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            boolean isAvailable = false;
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    isAvailable = false;
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    isAvailable = false;
                    break;
                default:
                    isAvailable = true;
                    break;
            }
            return isAvailable;
        }
    }


    private boolean isNeedToStopCaseChecker = false;

    private class CaseChecker extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!isNeedToStopCaseChecker) {
                    final Case aCase = DataHandler.getInstance().getGeneralResults();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            totalCases.setText("Total test cases: " + aCase.getTotal());
                            totalPassedCases.setText("Total passed items: " + aCase.getPass());
                            totalFailedCases.setText("Total failed items: " + aCase.getFail());
                        }
                    });
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                log.error("CaseChecker > run():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }
    }


    public void upload(View view) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uploadResTxt.setText("");
                    uploadResTxt.setBackground(null);
                }
            });
            if (DataHandler.getInstance().getDevID().length() == 14 || DataHandler.getInstance().getDevID().length() == 13) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadResTxt.setText("Trying to upload ... Please wait");
                                uploadResTxt.setBackground(getResources().getDrawable(R.drawable.sam_error_bg));
                            }
                        });
                        boolean res = FTPClientHelper.getInstance().uploadFile(Constants.server, Constants.port, Constants.user, Constants.password);
                        if (res) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    uploadResTxt.setText("Successfully uploaded to the server");
                                    uploadResTxt.setBackground(getResources().getDrawable(R.drawable.check_bg));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    uploadResTxt.setText("Some error occurred during upload, Try again");
                                    uploadResTxt.setBackground(getResources().getDrawable(R.drawable.error_bg));
                                }
                            });
                        }
                    }
                }).start();
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadResTxt.setText("There is no valid device ID, Scan an ID");
                        uploadResTxt.setBackground(getResources().getDrawable(R.drawable.error_bg));
                    }
                });
            }
        } catch (Exception ex) {
            log.error("MainActivity > upload():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    public void playSound(View view) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            if (loudnessEnhancer.getEnabled())
                loudnessEnhancer.setEnabled(false);
            mediaPlayer.reset();
            AssetFileDescriptor afd = getAssets().openFd("sound/kl.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            if (maxSpeaker.isChecked()) {
                loudnessEnhancer.setEnabled(true);
                //change loudness level here
                loudnessEnhancer.setTargetGain(2000);
            }
            mediaPlayer.start();
        } catch (Exception ex) {
            log.error("MainActivity > playSound():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    public void stopSound(View view) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (Exception ex) {
            log.error("MainActivity > stopSound():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    public void restart(View view) {
        try {
            File conf = new File(Constants.dataFile);
            if (conf.exists())
                conf.delete();
            Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
            int mPendingIntentId = 1;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        } catch (Exception ex) {
            log.error("MainActivity > restart():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    private void initTestResultsOnUI() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DataHandler.getInstance().isScanner()) {
                        //fine
                        qrResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        qrResImg.setImageResource(R.drawable.cancel);
                    }

                    if (DataHandler.getInstance().isCard()) {
                        //fine
                        cardResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        cardResImg.setImageResource(R.drawable.cancel);
                    }


                    if (DataHandler.getInstance().isSam()) {
                        //fine
                        samResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        samResImg.setImageResource(R.drawable.cancel);
                    }

                    if (DataHandler.getInstance().isAudio()) {
                        //fine
                        audioResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        audioResImg.setImageResource(R.drawable.cancel);
                    }

                    if (DataHandler.getInstance().isGps()) {
                        //fine
                        gpsResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        gpsResImg.setImageResource(R.drawable.cancel);
                    }

                    if (DataHandler.getInstance().isFourG()) {
                        //fine
                        fourGResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        fourGResImg.setImageResource(R.drawable.cancel);
                    }

                    if (DataHandler.getInstance().isCamera()) {
                        //fine
                        cameraResImg.setImageResource(R.drawable.checked);
                    } else {
                        //error
                        cameraResImg.setImageResource(R.drawable.cancel);
                    }
                }
            });
        } catch (Exception ex) {
            log.error("MainActivity > initTestResultsOnUI():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    private void initialSpeaker() {
        try {
            mediaPlayer = new MediaPlayer();
            loudnessEnhancer = new LoudnessEnhancer(mediaPlayer.getAudioSessionId());
            initControls();
        } catch (Exception ex) {
            log.error("MainActivity > initialSpeaker():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    private void initControls() {
        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));

            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {

                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);

                }
            });
        } catch (Exception ex) {
            log.error("MainActivity > initControls():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    private byte[] generateResetAPDU(Integer samSlot) {
        byte[] res = new byte[7];
        res[0] = (byte) 0xAA;
        res[1] = 0x66;
        res[2] = 0x00;
        res[3] = (byte) (4);
        res[4] = 0x37;

        if (samSlot == 0) {
            res[5] = 0x00;
            res[6] = (byte) 0x3B;
        } else if (samSlot == 1) {
            res[5] = 0x10;
            res[6] = (byte) 0x4B;
        } else if (samSlot == 2) {
            res[5] = 0x20;
            res[6] = (byte) 0x5B;
        } else if (samSlot == 3) {
            res[5] = 0x30;
            res[6] = (byte) 0x6B;
        }
        Log.e("SAM", ByteUtils.ByteArrToHex(res));
        return res;
    }

    private class SAM extends Thread {
        private SerialControl serialControl;
        private int samSlot = 0;
        private boolean nextTry = true;

        public SAM() {
            try {
                serialControl = new SerialControl();
                serialControl.setPort("/dev/ttyMT2");
                serialControl.setBaudRate(19200);
                serialControl.open();
            } catch (Exception ex) {
                log.error("SAM > SAM():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }


        public class SerialControl extends SerialHelper {
            public SerialControl() {
            }

            @Override
            protected void onDataReceived(final ComBean ComRecData) {
                try {
                    byte[] ret = ComRecData.bRec;
                    if (ret != null) {
                        if (ret.length > 5) {
                            Log.e("Mohr", ByteUtils.ByteArrToHex(ret));
                            Log.e("Mohe", samSlot + "");
                            if (ByteUtils.match(ret[4], 0x37)) {
                                //success
                                if (samSlot == 0) {
                                    DataHandler.getInstance().setSam(true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samOne.setBackground(getResources().getDrawable(R.drawable.check_bg));
                                            samOne.setText("SAM 1 \n Detected");
                                            samOne.setTextColor(Color.BLACK);
                                            samResImg.setImageResource(R.drawable.checked);
                                        }
                                    });
                                } else if (samSlot == 1) {
                                    DataHandler.getInstance().setSam(true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samTwo.setBackground(getResources().getDrawable(R.drawable.check_bg));
                                            samTwo.setText("SAM 2 \n Detected");
                                            samTwo.setTextColor(Color.BLACK);
                                            samResImg.setImageResource(R.drawable.checked);
                                        }
                                    });
                                } else if (samSlot == 2) {
                                    DataHandler.getInstance().setSam(true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samThree.setBackground(getResources().getDrawable(R.drawable.check_bg));
                                            samThree.setText("SAM 3 \n Detected");
                                            samThree.setTextColor(Color.BLACK);
                                            samResImg.setImageResource(R.drawable.checked);
                                        }
                                    });
                                } else if (samSlot == 3) {
                                    DataHandler.getInstance().setSam(true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samFour.setBackground(getResources().getDrawable(R.drawable.check_bg));
                                            samFour.setText("SAM 4 \n Detected");
                                            samFour.setTextColor(Color.BLACK);
                                            samResImg.setImageResource(R.drawable.checked);
                                        }
                                    });
                                }
                            } else {
                                //error
                                if (samSlot == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samOne.setBackground(getResources().getDrawable(R.drawable.sam_error_bg));
                                            samOne.setText("SAM 1 \n Not Found");
                                            samOne.setTextColor(Color.WHITE);
                                        }
                                    });
                                } else if (samSlot == 1) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samTwo.setBackground(getResources().getDrawable(R.drawable.sam_error_bg));
                                            samTwo.setText("SAM 2 \n Not Found");
                                            samTwo.setTextColor(Color.WHITE);
                                        }
                                    });
                                } else if (samSlot == 2) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samThree.setBackground(getResources().getDrawable(R.drawable.sam_error_bg));
                                            samThree.setText("SAM 3 \n Not Found");
                                            samThree.setTextColor(Color.WHITE);
                                        }
                                    });
                                } else if (samSlot == 3) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            samFour.setBackground(getResources().getDrawable(R.drawable.sam_error_bg));
                                            samFour.setText("SAM 4 \n Not Found");
                                            samFour.setTextColor(Color.WHITE);
                                        }
                                    });
                                }
                            }
                            nextTry = true;
                        }
                    }
                } catch (Exception ex) {
                    log.error("SAM > onDataReceived():" + ex.getMessage());
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    log.error(errors.toString());
                }
            }
        }

        public void close() {
            if (serialControl.isOpen())
                serialControl.close();
        }

        @Override
        public void run() {
            try {
                while (serialControl.isOpen()) {
                    for (int i = 0; i < 4; i++) {
                        nextTry = false;
                        samSlot = i;
                        byte[] data = generateResetAPDU(i);
                        ;
                        byte[] ret = null;
                        serialControl.send(data);
                        while (!nextTry) {
                            Thread.sleep(1000);
                        }
                    }
                    Thread.sleep(1000);
                }

            } catch (Exception ex) {
                log.error("SAM > run():" + ex.getMessage());
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                log.error(errors.toString());
            }
        }
    }


    private void initialUIMembers() {
        try {
            buildVersion = (TextView) findViewById(R.id.build_version);
            appVersion = findViewById(R.id.app_version);
            kernelVersion = (TextView) findViewById(R.id.kernel_version);
            osVersion = (TextView) findViewById(R.id.os_version);
            samOne = (TextView) findViewById(R.id.samOne);
            samTwo = (TextView) findViewById(R.id.samTwo);
            samThree = (TextView) findViewById(R.id.samThree);
            samFour = (TextView) findViewById(R.id.samFour);
            scannedTxt = (TextView) findViewById(R.id.scannedTxt);
            cardDetection = (TextView) findViewById(R.id.cardDetection);
            uploadResTxt = (TextView) findViewById(R.id.uploadResTxt);
            totalCases = (TextView) findViewById(R.id.totalCases);
            simcartTxt = (TextView) findViewById(R.id.fourG);
            gps = (TextView) findViewById(R.id.gps);
            imeiOne = (TextView) findViewById(R.id.imeiOne);
            imeiTwo = (TextView) findViewById(R.id.imeiTwo);
            maxSpeaker = (CheckBox) findViewById(R.id.maxSpeaker);
            totalFailedCases = (TextView) findViewById(R.id.totalFailedCases);
            totalPassedCases = (TextView) findViewById(R.id.totalPassedCases);
            qrResImg = (ImageView) findViewById(R.id.qrResImg);
            samResImg = (ImageView) findViewById(R.id.samResImg);
            audioResImg = (ImageView) findViewById(R.id.audioResImg);
            fourGResImg = (ImageView) findViewById(R.id.fourGResImg);
            cardResImg = (ImageView) findViewById(R.id.cardResImg);
            gpsResImg = (ImageView) findViewById(R.id.gpsResImg);
            cameraResImg = (ImageView) findViewById(R.id.cameraResImg);
            volumeSeekbar = (SeekBar) findViewById(R.id.volume_seekBar);




            audioResImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ImageView imageView = (ImageView) view;
                        if (DataHandler.getInstance().isAudio()) {
                            //go to error
                            DataHandler.getInstance().setAudio(false);
                            audioResImg.setImageResource(R.drawable.cancel);
                        } else {
                            //go to success
                            DataHandler.getInstance().setAudio(true);
                            audioResImg.setImageResource(R.drawable.checked);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


            cameraResImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (DataHandler.getInstance().isCamera()) {
                            //go to error
                            DataHandler.getInstance().setCamera(false);
                            cameraResImg.setImageResource(R.drawable.cancel);
                        } else {
                            //go to success
                            DataHandler.getInstance().setCamera(true);
                            cameraResImg.setImageResource(R.drawable.checked);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            deviceID = (TextView) findViewById(R.id.deviceID);
            if (DataHandler.getInstance().getDevID().equals("0")) {
                //not set
                deviceID.setText("Please scan valid QR to set the Device ID");
            } else if (DataHandler.getInstance().getDevID().length() == 16 && DataHandler.getInstance().getDevID().startsWith("V") && DataHandler.getInstance().getDevID().endsWith("A")) {
                //set
                deviceID.setText("Device ID : " + DataHandler.getInstance().getDevID());
            }

        } catch (Exception ex) {
            log.error("MainActivity > initialUIMembers():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }

    public void destroyCamera(View view) {
        try{
            if (cameraView != null){
                cameraView.close();
            }
        }catch (Exception ex){
            log.error("MainActivity > destroyCamera():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }


    public void setUiComponent(){
        try{
            final View decorView = getWindow().getDecorView();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            // This work only for android 4.4+
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                getWindow().getDecorView().setSystemUiVisibility(flags);
                decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });
            }

        }catch(Exception ex){
            log.error("MainActivity > setUiComponent():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
        }
    }
}
