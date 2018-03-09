package com.participateme.apps.contactapps;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

/**
 * Created by dharamvir on 25/07/2017.
 */

public class Constants {

    public static final int TYPE_REMOTE_TEXT = 1;
    public static final int TYPE_REMOTE_IMAGE = 2;
    public static final int TYPE_SELF_TEXT = 3;
    public static final int TYPE_SELF_IMAGE = 4;

    //Web services URLs
    public static final String SERVER_URL = "https://meetvideo.herokuapp.com/room/";
    public static final String SKU_PREMIUM = "premium_production";
    public static final int RC_UPGRADE_REQUEST = 10005;
    public static final String SIGN_UP_REQUEST_URL = "http://participateme.com/signin_app_contacts.php";
    public static final String UPDATE_PREMIUM_REQUEST_URL = "http://participateme.com/update_premium_info.php";
    public static final String CHECK_BACKUP_REQUEST_URL = "http://participateme.com/apps_backup.php";
    public static final String UPDATE_BACKUP_REQUEST_URL = "http://participateme.com/update_backup_apps.php";
    public static final String UPDATE_PROFILE_REQUEST_URL = "http://participateme.com/profile_upload_contacts.php";
    public static final String IMAGE_ACCESS_URL = "http://participateme.com/uploads/";
    public static final String VERIFY_PHONE_NUMBERS_REQUEST_URL = "http://participateme.com/verifyphonenumbers_contacts.php";
    public static final String NOTIFY_CALLER_REQUEST_URL = "http://participateme.com/notifycaller_contactapps.php";

    public static void showNetworkDialog(final Activity context) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            alertDialogBuilder.setTitle("Network Error");
            alertDialogBuilder.setMessage("Please check your network connection and try again later.");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //DisplayContactsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DisplayContactsActivity.this.getPackageName())));
                    dialog.cancel();
                    context.finish();
                }
            });

        alertDialogBuilder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                //context.finish();
                dialog.cancel();
context.finish();
                Intent in = new Intent(context, context.getClass());
              //  in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if(context instanceof DisplayContactsActivity) {
                    in.putExtra("code", context.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("code", null));
                    in.putExtra("phone", context.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("phone", null));
                    in.putExtra("isupdate", "yes");
                }
                if(!(context instanceof ContactAppsActivity))
                context.startActivity(in);
            }
        });

            alertDialogBuilder.show();
    }

    public static void showUpgradeDialog(final Activity context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        alertDialogBuilder.setTitle("Premium Upgrade");
        alertDialogBuilder.setMessage("Enjoy ad free experience! ");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("UPGRADE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //DisplayContactsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DisplayContactsActivity.this.getPackageName())));
               // if(context instanceof DisplayContactsActivity) {
                BillingDelegate billingDelegate = new BillingDelegate(context);

                billingDelegate.startPremiumUpgrade();
               // }
                dialog.cancel();
               // context.finish();
            }
        });

        alertDialogBuilder.setNegativeButton("NO, THANKS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
               // if(context instanceof DisplayContactsActivity) {

                  //  BillingDelegate billingDelegate = new BillingDelegate(context);
                    //billingDelegate.startPremiumUpgrade();
//                    DisplayContactsActivity displayContactsActivity = (DisplayContactsActivity)context;
//
//                    displayContactsActivity.startPremiumUpgrade();
              //  }
                //context.finish();
                dialog.cancel();

            }
        });

        Dialog d = alertDialogBuilder.show();
    }


}
