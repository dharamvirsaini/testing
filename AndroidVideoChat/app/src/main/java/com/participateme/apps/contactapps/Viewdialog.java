package com.participateme.apps.contactapps;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by dharamvir on 23/02/2018.
 */

public class Viewdialog {

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       // dialog.setCancelable(false);
        dialog.setContentView(R.layout.fragment_upgrade);

        Button dialogButton = (Button) dialog.findViewById(R.id.cancelButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button upgrade = (Button) dialog.findViewById(R.id.upgradeButton);
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //upgrade
                dialog.dismiss();
            }
        });


        dialog.show();

    }
}
