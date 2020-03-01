package id.herocode.gaskenjogja.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppUtils {
    private static ProgressDialog mProgressDialog;

    public static void showSimpleProgressDialog(Context context, String title, String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, title, msg);
                mProgressDialog.setCancelable(isCancelable);
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } catch (IllegalArgumentException ie) {
            System.out.print("Error 1: ");
            ie.printStackTrace();
        } catch (RuntimeException re) {
            System.out.print("Error 2: ");
            re.printStackTrace();
        } catch (Exception e) {
            System.out.print("Error 3: ");
            e.printStackTrace();
        }
    }

    public static void showSimpleProgressDialog(Context context) {
        showSimpleProgressDialog(context, null, "Loading...", false);
    }

    public static void showDialogPembayaran(Context context) {
        showSimpleProgressDialog(context, "Melakukan Pembayaran", "mohon bersabar...", false);
    }

    public static void showDialogTopup(Context context) {
        showSimpleProgressDialog(context, "Melakukan Top Up Saldo", "mohon bersabar...", false);
    }

    public static void removeSimpleProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            System.out.print("Error 4: ");
            ie.printStackTrace();
        } catch (RuntimeException re) {
            System.out.print("Error 5: ");
            re.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error 6: ");
            e.printStackTrace();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }
}
