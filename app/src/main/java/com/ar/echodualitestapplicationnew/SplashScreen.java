package com.ar.echodualitestapplicationnew;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.scsoft.libecho5.Gpio;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SplashScreen extends AppCompatActivity {

    private workerTask workerTask = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Thread.setDefaultUncaughtExceptionHandler(new FatalExceptionHandler(this));
        setUiComponent();
        workerTask = new workerTask();
        workerTask.execute();
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
            ex.printStackTrace();
        }
    }

    private class workerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           initialGPIOs();
            new LogBackConfigurations();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                DataHandler.getInstance();
                Thread.sleep(4000);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
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
            Log.e("Main", "Main() --> setUiComponent():" + ex.getMessage());
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            Log.e("Main", errors.toString());
        }
    }
}

