package com.ar.echodualitestapplicationnew;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GPIOWrapper {
    private static final String GPIO_EXPORT = "/sys/class/gpio/export";
    private static final String GPIO_PIN_PATH = "/sys/class/gpio/gpio";
    private static final String VALUE = "/value";
    private static final String DIRECTION = "/direction";
    private int DI_GPIO = 41;
    public int DO_GPIO;
    private int NFC_ERROR = -106000;
    private int Qr_ERROR = -104000;
    private int Sam_ERROR = -105000;
    private int NFC_READER_GPIO = 42;
    private int QR_SCAN_GPIO = 86;
    private int SAM_GPIO = 55;
    private int DO_ERROR = -101000;
    private int DO_SUCCESS = 0;
    private int SW_GPIO = 69;
    private int POWER_GPIO = 8;
    private int SW_GPIO_ERROR = -107000;
    private int POWER_GPIO_ERROR = -107000;
    String str;

    public GPIOWrapper() {
        this.str = String.format("/sys/class/gpio/gpio%d/value", this.DI_GPIO);
    }

    protected int check_if_exported(int value) {
        String filePath = "/sys/class/gpio/gpio" + Integer.toString(value);
        File dir = new File(filePath);
        return dir.exists() ? 0 : -1;
    }

    public boolean exportNewOne(int gNumber, int value, int dir){
        if (check_if_exported(gNumber) != 0){
            //lets export
            Log.e("Mohsen", "no export");
            if (export_gpio(gNumber)){
                Log.e("Mohsen", "exported");
                if (set_gpio_direction(gNumber, dir) == 1){
                    Log.e("Mohsen", "direction set");
                    if (set_gpio_value(gNumber, value)) {
                        Log.e("Mohsen", "value set");
                        return true;
                    }
                }
            }
        }else{
            Log.e("Mohsen", "have been exported before");
            if (export_gpio(gNumber)){
                Log.e("Mohsen", "exported");
                if (set_gpio_direction(gNumber, dir) == 1){
                    Log.e("Mohsen", "direction set");
                    if (set_gpio_value(gNumber, value)) {
                        Log.e("Mohsen", "value set");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int write_to_file_node(String path, int value) {
        FileWriter vWrite = null;
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        } else {
            try {
                vWrite = new FileWriter(file);
                vWrite.write(String.valueOf(value));
                vWrite.close();
                return 1;
            } catch (IOException var6) {
                var6.printStackTrace();
                return -1;
            }
        }
    }

    protected String read_file_node(String path) {
        File file = new File(path);
        String strLine = null;
        if (!file.exists()) {
            return null;
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                strLine = br.readLine();
                return strLine == null ? null : strLine;
            } catch (IOException var6) {
                var6.printStackTrace();
                return null;
            }
        }
    }

    protected boolean export_gpio(int gpio) {
        return this.write_to_file_node("/sys/class/gpio/export", gpio) >= 0;
    }

    protected boolean set_gpio_value(int value, int gpio) {
        String filePath = "/sys/class/gpio/gpio" + Integer.toString(gpio) + "/value";
        return this.write_to_file_node(filePath, value) >= 0;
    }

    protected int set_gpio_direction(int fvalue, int gpio) {
        FileWriter vWrite = null;
        String filePath = "/sys/class/gpio/gpio" + Integer.toString(gpio) + "/direction";
        File file = new File(filePath);
        if (file.exists()) {
            String dir;
            if (fvalue == 1) {
                dir = "out";
            } else {
                dir = "in";
            }

            try {
                vWrite = new FileWriter(file);
                vWrite.write(dir);
                vWrite.close();
                return 1;
            } catch (IOException var8) {
                var8.printStackTrace();
                return -1;
            }
        } else {
            return -1;
        }
    }

    public int DI_GetValue() {
        if (this.check_if_exported(this.DI_GPIO) == -1) {
            this.export_gpio(this.DI_GPIO);
            System.out.println("Exported");
        }

        String get_val = this.read_file_node(this.str);
        return get_val == null ? this.DO_ERROR : Integer.parseInt(get_val);
    }

    public int DO_SetValue(byte gpio, Boolean value) {
        switch(gpio) {
            case 0:
                this.DO_GPIO = 81;
                break;
            case 1:
                this.DO_GPIO = 82;
                break;
            default:
                this.DO_GPIO = 0;
                return this.DO_ERROR;
        }

        if (this.check_if_exported(this.DO_GPIO) == -1) {
            this.export_gpio(this.DO_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.DO_GPIO) == 1) {
            if (value) {
                if (this.set_gpio_value(1, this.DO_GPIO)) {
                    System.out.println("DO_GPIO value changed to 1\n");
                    return this.DO_SUCCESS;
                } else {
                    return this.DO_ERROR;
                }
            } else if (this.set_gpio_value(0, this.DO_GPIO)) {
                System.out.println("DO_GPIO value changed to 0\n");
                return this.DO_SUCCESS;
            } else {
                return this.DO_ERROR;
            }
        } else {
            return this.DO_ERROR;
        }
    }

    public int CtlsReader_Enable() {
        if (this.check_if_exported(this.NFC_READER_GPIO) == -1) {
            this.export_gpio(this.NFC_READER_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.NFC_READER_GPIO) == 1) {
            this.set_gpio_value(1, this.NFC_READER_GPIO);
            System.out.println("DO_GPIO value changed to 1 \n");
            return this.DO_SUCCESS;
        } else {
            return this.NFC_ERROR;
        }
    }

    public int CtlsReader_Disable() {
        if (this.check_if_exported(this.NFC_READER_GPIO) == -1) {
            this.export_gpio(this.NFC_READER_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.NFC_READER_GPIO) == 1) {
            this.set_gpio_value(0, this.NFC_READER_GPIO);
            System.out.println("DO_GPIO value changed to 0 \n");
            return this.DO_SUCCESS;
        } else {
            return this.NFC_ERROR;
        }
    }

    public int BarcodeScanner_Enable() {
        if (this.check_if_exported(this.QR_SCAN_GPIO) == -1) {
            this.export_gpio(this.QR_SCAN_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.QR_SCAN_GPIO) == 1) {
            this.set_gpio_value(1, this.QR_SCAN_GPIO);
            System.out.println("DO_GPIO value changed to 1 \n");
            return this.DO_SUCCESS;
        } else {
            return this.Qr_ERROR;
        }
    }

    public int BarcodeScanner_Disable() {
        if (this.check_if_exported(this.QR_SCAN_GPIO) == -1) {
            this.export_gpio(this.QR_SCAN_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.QR_SCAN_GPIO) == 1) {
            this.set_gpio_value(0, this.QR_SCAN_GPIO);
            System.out.println("DO_GPIO value changed to 0 \n");
            return this.DO_SUCCESS;
        } else {
            return this.Qr_ERROR;
        }
    }

    public int Sam_Enable() {
        if (this.check_if_exported(this.SAM_GPIO) == -1) {
            this.export_gpio(this.SAM_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.SAM_GPIO) == 1) {
            this.set_gpio_value(1, this.SAM_GPIO);
            System.out.println("DO_GPIO value changed to 1 \n");
            return this.DO_SUCCESS;
        } else {
            return this.Sam_ERROR;
        }
    }



    public int Sam_Disable() {
        if (this.check_if_exported(this.SAM_GPIO) == -1) {
            this.export_gpio(this.SAM_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.SAM_GPIO) == 1) {
            this.set_gpio_value(0, this.SAM_GPIO);
            System.out.println("DO_GPIO value changed to 0 \n");
            return this.DO_SUCCESS;
        } else {
            return this.Sam_ERROR;
        }
    }

    public int RS232_RS485_Switch(int value) {
        if (this.check_if_exported(this.SW_GPIO) == -1) {
            this.export_gpio(this.SW_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.SW_GPIO) == 1) {
            this.set_gpio_value(value, this.SW_GPIO);
            System.out.println("DO_GPIO value changed to 1 \n");
            return this.DO_SUCCESS;
        } else {
            return this.SW_GPIO_ERROR;
        }
    }

    public int RS232_RS485_Enable() {
        if (this.check_if_exported(this.POWER_GPIO) == -1) {
            this.export_gpio(this.POWER_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.POWER_GPIO) == 1) {
            this.set_gpio_value(1, this.POWER_GPIO);
            System.out.println("DO_GPIO value changed to 1 \n");
            return this.DO_SUCCESS;
        } else {
            return this.POWER_GPIO_ERROR;
        }
    }

    public int RS232_RS485_Disable() {
        if (this.check_if_exported(this.POWER_GPIO) == -1) {
            this.export_gpio(this.POWER_GPIO);
            System.out.println("Exported");
        }

        if (this.set_gpio_direction(1, this.POWER_GPIO) == 1) {
            this.set_gpio_value(0, this.POWER_GPIO);
            System.out.println("DO_GPIO value changed to 0 \n");
            return this.DO_SUCCESS;
        } else {
            return this.POWER_GPIO_ERROR;
        }
    }
}

