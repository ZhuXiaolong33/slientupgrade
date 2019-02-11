package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageInstallObserver extends IInterface {
    public abstract static class Stub extends Binder implements IPackageInstallObserver {
        public Stub() {

        }

        public static IPackageInstallObserver asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            throw new RuntimeException("Stub!");
        }
    }

    public abstract void packageInstalled(String packageName, int returnCode) throws RemoteException;

}
