package com.us.ramesh.securedchat.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

/**
 * Created on 10/02/2018.
 *
 * @author Ramesh
 */

/*
    Class to check internet connectivity
*/

public class NetworkUtils {
    public static boolean isConnected(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showDialog(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("No Internet Access. Connect to the Internet")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
