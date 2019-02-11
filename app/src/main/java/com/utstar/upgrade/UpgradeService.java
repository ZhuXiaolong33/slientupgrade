package com.utstar.upgrade;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UpgradeService extends Service{

    private static final String TAG = "SlientUpgradeService";
    public final int INSTALL_REPLACE_EXISTING = 2;

    private OnPackagedObserver onInstallPackaged;
    private PackageInstallObserver observer;
    private PackageDeleteObserver observerdelete;
    private MyUpgradeSerice myUpgradeSerice;
    private PackageManager pm;
//    private Context mContext;
    private File file;
    private Method method;
    private Method uninstallmethod;

    private  int installResultCode = 0;
    private  int uninstallResultcode = 0;

    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"---Service onCreate()---");

        observer = new PackageInstallObserver();
        observerdelete = new PackageDeleteObserver();
        pm = this.getPackageManager();

        Class<?>[] types = new Class[] {Uri.class, IPackageInstallObserver.class, int.class, String.class};
        Class<?>[] uninstalltypes = new Class[] {String.class, IPackageDeleteObserver.class, int.class};

        try {
            method = pm.getClass().getMethod("installPackage", types);
            uninstallmethod = pm.getClass().getMethod("deletePackage", uninstalltypes);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public void setOnPackagedObserver(OnPackagedObserver onInstallPackaged) {
        this.onInstallPackaged = onInstallPackaged;
    }


    public void installPackage(Uri apkFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
        method.invoke(pm,new Object[] {apkFile, observer, INSTALL_REPLACE_EXISTING, null});
        Log.i(TAG,"beginTime is "+System.currentTimeMillis()+"observer.finished="+observer.finished);
        synchronized (observer) {
            while (!observer.finished) {
                Log.i(TAG,"installPackage->observer receive notify and observer.finished="+observer.finished);
                try {
                    observer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG,"endTime is "+System.currentTimeMillis());

    }

    public void uninstallPackage(String packagename) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        uninstallmethod.invoke(pm, new Object[] {packagename,observerdelete, 0});
        synchronized (observerdelete) {
            while (!observerdelete.uninstallFinished) {
                try {
                    observerdelete.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public UpgradeService() {
    }

    public class MyUpgradeSerice extends IUpgaradeInterface.Stub {


        @Override
        public int install(String apkPath) throws RemoteException {
            Log.i(TAG,"MyUpgradeSerice->install("+apkPath+")");
            try {
                installPackage(Uri.parse(apkPath));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"MyUpgradeSerice->install("+apkPath+")"+"installResultCode="+installResultCode);
            return installResultCode;
        }

        @Override
        public int uninstall(String packageName) throws RemoteException {
            Log.i(TAG,"uninstall("+packageName+")");
            try {
                uninstallPackage(packageName);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"MyUpgradeSerice->uninstall("+packageName+")"+"uninstallResultcode="+uninstallResultcode);
            return uninstallResultcode;
        }
    }

    public class PackageInstallObserver extends IPackageInstallObserver.Stub {
        boolean finished;
        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            synchronized (observer) {
                finished = true;

                if (returnCode == 1) {
                    installResultCode = 0;
                } else {
                    installResultCode = -1;
                }
                Log.i(TAG, "packageInstalled(packageName=" + packageName + "," + "returnCode=" + returnCode + ")" + "finished=" + finished);
                notifyAll();
            }
        }
    }

    public class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        boolean uninstallFinished;
        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            synchronized (observerdelete) {
                uninstallFinished = true;
                notifyAll();
                if (returnCode == 1) {
                    uninstallResultcode = 0;
                } else {
                    uninstallResultcode = -1;
                }
                Log.i(TAG, "packageDeleted(packageName=" + packageName + ",returnCode=" + returnCode + ")" + "uninstallFinished=" + uninstallFinished);
                notifyAll();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"myUpgradeService is bind");
        myUpgradeSerice = new MyUpgradeSerice();
        return myUpgradeSerice;
    }


}
