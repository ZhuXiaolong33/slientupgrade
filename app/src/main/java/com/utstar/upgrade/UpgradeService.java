package com.utstar.upgrade;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class UpgradeService extends Service{

    private static final String TAG = "SlientUpgradeService";
    private MyUpgradeSerice myUpgradeSerice;




    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"---Service onCreate()---");


    }

    public UpgradeService() {
    }

    public class MyUpgradeSerice extends IUpgaradeInterface.Stub {


        int installResultCode = 0;
        int uninstallResultcode = 0;

        @Override
        public int install(String apkPath) throws RemoteException {
            String installCmd = "";
            String installErrorStr = "";
            String installSucessStr = "";
            String installLine = "";
            Log.i(TAG,"MyUpgradeSerice->install("+apkPath+")"+" Build.VERSION.SDK_INT="+Build.VERSION.SDK_INT);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                installCmd = "pm install -r -d -i com.utstar.upgrade --user 0 " +apkPath;
            } else {
                installCmd = "pm install -r -d " + apkPath;
            }

            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec(installCmd);
                InputStream errorInput = process.getErrorStream();
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while ((installLine = bufferedReader.readLine()) != null) {
                    installSucessStr += installLine;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
                while ((installLine = bufferedReader.readLine()) != null) {
                    installErrorStr += installLine;
                }

                if (installSucessStr.equals("Success")) {
                    Log.i(TAG,"install:Success,"+installSucessStr+" return 0");
                    installResultCode = 0;
                } else {
                    Log.i(TAG,"install:error, "+installErrorStr+" return -1");
                    installResultCode = -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return installResultCode;
        }

        @Override
        public int uninstall(String packageName) throws RemoteException {
            String uninstallCmd = "";
            String uninstallErrorStr = "";
            String uninstallSucessStr = "";
            String uninstallLine = "";
            Log.i(TAG,"MyUpgradeSerice->uninstall("+packageName+")");
            uninstallCmd = "pm uninstall "+ packageName;

            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec(uninstallCmd);
                InputStream errorInput = process.getErrorStream();
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while ((uninstallLine = bufferedReader.readLine()) != null) {
                    uninstallSucessStr += uninstallLine;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
                while ((uninstallLine = bufferedReader.readLine()) != null) {
                    Log.i(TAG,"uninstallErrorLine=" + uninstallLine);
                    uninstallErrorStr += uninstallLine;
                }

                if (uninstallSucessStr.equals("Success")) {
                    Log.i(TAG,"unintall:Success " + uninstallSucessStr + " return 0");
                    uninstallResultcode = 0;
                } else {
                    Log.i(TAG,"uninstall:error "+uninstallErrorStr+" return -1");
                    uninstallResultcode = -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return uninstallResultcode;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"myUpgradeService is bind");
        myUpgradeSerice = new MyUpgradeSerice();
        return myUpgradeSerice;
    }


}
