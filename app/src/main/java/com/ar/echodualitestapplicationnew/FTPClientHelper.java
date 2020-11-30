package com.ar.echodualitestapplicationnew;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

public class FTPClientHelper {
    private static FTPClientHelper ftpClientUtility = null;
    private static boolean isConnected = false;

    public FTPClientHelper() {
    }

    public static FTPClientHelper getInstance(){
        if(ftpClientUtility == null)
            ftpClientUtility = new FTPClientHelper();
        return ftpClientUtility;
    }



    public Boolean uploadFile(String server, int portNumber, String user, String password){
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            FTPSClient innerFtp = new FTPSClient(sslContext);
            try {
                boolean loginRes;

                innerFtp.connect(server, portNumber);

                if(!innerFtp.isConnected()){
                    return false;
                }
                innerFtp.setDataTimeout(4*60*1000);
                innerFtp.setSoTimeout(120*1000);
                innerFtp.execPBSZ(0);
                innerFtp.execPROT("P");
                innerFtp.enterLocalPassiveMode();
                loginRes = innerFtp.login(user, password);
                if(!loginRes)
                    return false;

                File file = new File(Constants.dataFile);
                if (!file.exists())
                    return false;
                FileInputStream in = new FileInputStream(file);
                innerFtp.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                innerFtp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                innerFtp.enterLocalPassiveMode();
                String dir = "/results/"+ DataHandler.getInstance().getDevID();
                innerFtp.makeDirectory(dir);
                boolean result = innerFtp.storeFile(dir+"/"+file.getName(),in);
                if(result){
                    in.close();
                    return true;
                }else{
                    in.close();
                    return false;
                }
            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }
            finally {
                if (innerFtp != null) {
                    innerFtp.logout();
                    innerFtp.disconnect();
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}

