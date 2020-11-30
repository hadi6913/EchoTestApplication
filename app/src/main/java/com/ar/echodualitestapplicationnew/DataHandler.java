package com.ar.echodualitestapplicationnew;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

public class DataHandler {
    private static DataHandler instance = null;
    private File dataFile = new File(Constants.dataFile);
    private File dataFolder = new File(Constants.dataFolder);
    private String CARD_TAG = "card";
    private String SAM_TAG = "sam";
    private String CAMERA_TAG = "camera";
    private String SCANNER_TAG = "scanner";
    private String AUDIO_TAG = "audio";
    private String FOUR_G_TAG = "four_g";
    private String GPS_TAG = "gps";
    private String DEVICE_ID_TAG = "dev_id";
    private String IMEI_ONE_TAG = "imei_one";
    private String IMEI_TWO_TAG = "imei_two";



    private boolean card = false;
    private boolean sam = false;
    private boolean scanner = false;
    private boolean audio = false;
    private boolean fourG = false;
    private boolean gps = false;
    private boolean camera = false;
    private String devID= "0";
    private String imeiOne= "0";
    private String imeiTwo= "0";

    private Object lock = new Object();

    public DataHandler() {
        initial();
    }

    public static DataHandler getInstance(){
        if (instance == null)
            instance = new DataHandler();
        return instance;
    }

    private void initial(){
        try{
            if (!dataFile.exists()){
                dataFolder.mkdirs();
                dataFile.createNewFile();
                createNewResultFile();
            }else{
                loadAll();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void createNewResultFile(){
        try{
            synchronized (lock) {
                StringBuilder builder = new StringBuilder();
                builder.append(CARD_TAG + "=false" + "\n");
                builder.append(SAM_TAG + "=false" + "\n");
                builder.append(SCANNER_TAG + "=false" + "\n");
                builder.append(AUDIO_TAG + "=false" + "\n");
                builder.append(FOUR_G_TAG + "=false" + "\n");
                builder.append(GPS_TAG + "=false" + "\n");
                builder.append(CAMERA_TAG + "=false" + "\n");
                builder.append(DEVICE_ID_TAG + "=0" + "\n");
                builder.append(IMEI_ONE_TAG + "=0" + "\n");
                builder.append(IMEI_TWO_TAG + "=0" + "\n");

                if (dataFile.exists()) {
                    FileUtils.writeStringToFile(dataFile, builder.toString());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    private boolean changeValue(String attr, boolean val)
    {
        synchronized (lock) {
            Properties prop = new Properties();
            try {
                // load a properties file
                prop.load(new FileReader(Constants.dataFile));
                FileOutputStream out = new FileOutputStream(Constants.dataFile);
                if (val)
                    prop.setProperty(attr, "true");
                else
                    prop.setProperty(attr, "false");
                prop.store(out, null);
                out.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }


    private boolean changeValue(String attr, String val)
    {
        synchronized (lock) {
            Properties prop = new Properties();
            try {
                // load a properties file
                prop.load(new FileReader(Constants.dataFile));
                FileOutputStream out = new FileOutputStream(Constants.dataFile);
                prop.setProperty(attr, val);
                prop.store(out, null);
                out.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    private void loadAll(){
        try{
            synchronized (lock) {
                Properties prop = new Properties();
                InputStream input = null;
                input = new FileInputStream(Constants.dataFile);
                prop.load(input);

                if (prop.getProperty(CARD_TAG).equals("true"))
                    card = true;
                else {
                    card = false;
                }

                if (prop.getProperty(SAM_TAG).equals("true"))
                    sam = true;
                else {
                    sam = false;
                }

                if (prop.getProperty(SCANNER_TAG).equals("true"))
                    scanner = true;
                else {
                    scanner = false;
                }


                if (prop.getProperty(AUDIO_TAG).equals("true"))
                    audio = true;
                else {
                    audio = false;
                }

                if (prop.getProperty(FOUR_G_TAG).equals("true"))
                    fourG = true;
                else {
                    fourG = false;
                }

                if (prop.getProperty(GPS_TAG).equals("true"))
                    gps = true;
                else {
                    gps = false;
                }

                if (prop.getProperty(CAMERA_TAG).equals("true"))
                    camera = true;
                else {
                    camera = false;
                }

                devID = prop.getProperty(DEVICE_ID_TAG);
                imeiOne = prop.getProperty(IMEI_ONE_TAG);
                imeiTwo = prop.getProperty(IMEI_TWO_TAG);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Case getGeneralResults(){
        Case aCase = new Case();
        try{
            aCase.setTotal(7);
            int pass = 0;
            int fail = 0;
            if (isScanner()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isCard()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isSam()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isCamera()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isAudio()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isFourG()){
                pass+=1;
            }else{
                fail+=1;
            }

            if (isGps()){
                pass+=1;
            }else{
                fail+=1;
            }

            aCase.setPass(pass);
            aCase.setFail(fail);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return aCase;
    }

    public boolean isCard() {
        return card;
    }

    public void setCard(boolean card) {
        if (this.card != card) {
            this.card = card;
            changeValue(CARD_TAG, card);
        }
    }

    public boolean isSam() {
        return sam;
    }

    public void setSam(boolean sam) {
        if (this.sam != sam) {
            this.sam = sam;
            changeValue(SAM_TAG, sam);
        }
    }

    public boolean isScanner() {
        return scanner;
    }

    public void setScanner(boolean scanner) {
        if (this.scanner != scanner) {
            this.scanner = scanner;
            changeValue(SCANNER_TAG, scanner);
        }
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        if (this.audio != audio) {
            this.audio = audio;
            changeValue(AUDIO_TAG, audio);
        }
    }

    public boolean isFourG() {
        return fourG;
    }

    public void setFourG(boolean fourG) {
        if (this.fourG != fourG) {
            this.fourG = fourG;
            changeValue(FOUR_G_TAG, fourG);
        }
    }

    public boolean isGps() {
        return gps;
    }

    public void setGps(boolean gps) {
        if (this.gps != gps) {
            this.gps = gps;
            changeValue(GPS_TAG, gps);
        }
    }

    public String getDevID() {
        return devID;
    }

    public void setDevID(String devID) {
        if (this.devID != devID) {
            this.devID = devID;
            changeValue(DEVICE_ID_TAG, devID);
        }
    }

    public String getImeiOne() {
        return imeiOne;
    }

    public void setImeiOne(String imeiOne) {
        if (this.imeiOne != imeiOne) {
            this.imeiOne = imeiOne;
            changeValue(IMEI_ONE_TAG, imeiOne);
        }
    }

    public String getImeiTwo() {
        return imeiTwo;
    }

    public void setImeiTwo(String imeiTwo) {
        if (this.imeiTwo != imeiTwo) {
            this.imeiTwo = imeiTwo;
            changeValue(IMEI_TWO_TAG, imeiTwo);
        }
    }

    public boolean isCamera() {
        return camera;
    }

    public void setCamera(boolean camera) {
        if (this.camera != camera) {
            this.camera = camera;
            changeValue(CAMERA_TAG, camera);
        }
    }
}

