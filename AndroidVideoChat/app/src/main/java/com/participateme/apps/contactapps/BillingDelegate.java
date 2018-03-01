package com.participateme.apps.contactapps;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.participateme.apps.contactapps.Billing.IabHelper;
import com.participateme.apps.contactapps.Billing.IabResult;
import com.participateme.apps.contactapps.Billing.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dharamvir on 24/02/2018.
 */

public class BillingDelegate {

    IabHelper mBillingHelper;
    String mPhone;
    Activity mContext;

    BillingDelegate(Activity context) {
        mContext = context;
        mPhone = mContext.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("phone", null);
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {


            Log.d("BILLING RESULT",""+"After Payment Is Called");
            if (mBillingHelper == null) return;

            if (result.isFailure()) {
                Toast.makeText(mContext,"Error in purchasing", Toast.LENGTH_LONG).show();

                return;
            }

            if (!verifyDeveloperPayload(info)) {
                Toast.makeText(mContext,"There Might Be Some Error ...Try Again", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("BILLING", "Purchase successful.");

            if (info.getSku().equals(Constants.SKU_PREMIUM)) {

              /*  mBillingHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        Log.d("BillingDelegate", "consumeAsync");

                    }
                });*/
                SharedPreferences.Editor editor = mContext.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString("isPremium", "yes");
                editor.commit();

                if(mContext instanceof EditProfileActivity) {
                    ((EditProfileActivity)mContext).updatePremium();
                }
                else {
                    ((DisplayContactsActivity)mContext).updatePremium();
                }

                updateOnServer();

                Toast.makeText(mContext,"Congratulations! You're now a premium user", Toast.LENGTH_LONG).show();

            }
            else{

                Toast.makeText(mContext,"Problem in getting subscription", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void updateOnServer() {

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = Constants.UPDATE_PREMIUM_REQUEST_URL + "?phone=" + mContext.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("phone", null);
        Log.d("Billing Delegate", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d("Billing Delegate", response);


                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Toast.makeText(mContext, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    boolean verifyDeveloperPayload(Purchase p){

        return p.getDeveloperPayload().equals(mPhone);
    }

    public void startPremiumUpgrade(){

        mBillingHelper = new IabHelper(mContext,mContext.getString(R.string.billing_key));

        if(mContext instanceof EditProfileActivity) {
            ((EditProfileActivity)mContext).setBillingHelper(mBillingHelper);
        }
        else {
            ((DisplayContactsActivity)mContext).setBillingHelper(mBillingHelper);
        }

        mBillingHelper.enableDebugLogging(true,"BILLING DEBUG");

        mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()){
                    Toast.makeText(mContext,"Problem Connecting To Billing", Toast.LENGTH_LONG).show();
                }

                mBillingHelper.launchPurchaseFlow(mContext, Constants.SKU_PREMIUM,
                        IabHelper.ITEM_TYPE_INAPP, Constants.RC_UPGRADE_REQUEST, mPurchaseFinishedListener, mPhone);
            }
        });
    }
}
