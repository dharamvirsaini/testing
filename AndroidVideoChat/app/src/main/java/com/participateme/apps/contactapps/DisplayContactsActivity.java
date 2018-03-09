package com.participateme.apps.contactapps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.participateme.apps.contactapps.Billing.IabHelper;
import com.participateme.apps.contactapps.Billing.IabResult;
import com.participateme.apps.contactapps.Billing.Purchase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class DisplayContactsActivity extends AppCompatActivity {

    private static final String TAG = "DisplayContactsActivity";
    public static final String IMAGE_ACCESS_URL = "http://contactsyncer.com/uploads/contact_apps/";
    public static final int REQUEST_PERMISSION_CODE = 1;
    public static DisplayContactsActivity sActivityContext;

    private Cursor mCursor;
    private RecyclerView mRecList;
    private List<String> mPhoneList;
    private List<String> mNameList;
    private ArrayList<String> mDeviceTokens = new ArrayList<>();
    private ArrayList<String> mContactApps = new ArrayList<>();
    private MaterialDialog mProgressDialog;
    private ArrayList<ContactInfo> result;
    private Boolean mFromAddContact = false;
    IabHelper mBillingHelper;
    String mPhone;
    String mIsPremium;
    private StringBuilder mAllApps;
    private InterstitialAd mInterstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //google ad
        mInterstitialAd = new InterstitialAd(this);

         mInterstitialAd.setAdUnitId("ca-app-pub-4927131029050901/1694551164");
       // mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

        //in-app billing

      //  overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        String isPremium = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("isPremium", null);

        if(isPremium != null && isPremium.equals("yes")){
            updatePremium();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdateVersion(DisplayContactsActivity.this);
            }
        }).start();

        sActivityContext = this;
        Log.d(TAG, Integer.toString(BuildConfig.VERSION_CODE));

        Intent in = getIntent();

        String code = in.getStringExtra("code");
        mPhone = in.getStringExtra("phone");
        mIsPremium = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("isPremium", null);

        if(in.getStringExtra("isupdate").equals("yes"))
        updatePackages(code, mPhone);

        mProgressDialog = new MaterialDialog.Builder(DisplayContactsActivity.this)
                .cancelable(false)
                .progress(true, 100)
                .content("Please wait...")
                .build();

        mProgressDialog.show();

        Log.d("DisplayContactsActivity", "phone is " + mPhone + " and code is " + code);


        ((ImageView) findViewById(R.id.add_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DisplayContactsActivity.this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

            }
        });

        ((ImageView) findViewById(R.id.add_contact_below)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFromAddContact = true;

                Intent intent = new Intent(Intent.ACTION_INSERT,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);

            }
        });

        ((ImageView) findViewById(R.id.trending)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DisplayContactsActivity.this, TrendingAppsActivity.class);

                Log.d("all apps ", mAllApps.toString());
                intent.putExtra("apps", mAllApps.toString());
                startActivity(intent);

            }
        });

        ((TextView) findViewById(R.id.backup_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



           /*    mIsPremium = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("isPremium", null);

                if(mIsPremium == null || !mIsPremium.equals("yes"))
                Constants.showUpgradeDialog(DisplayContactsActivity.this);
                else {*/
                   Intent intent = new Intent(DisplayContactsActivity.this, MyProfileActivity.class);
                    startActivity(intent);
              //  }
            }
        });

        mRecList = (RecyclerView) findViewById(R.id.cardList);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EnableRuntimePermission();
        } else {
            createList();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    boolean verifyDeveloperPayload(Purchase p){

        return p.getDeveloperPayload().equals(mPhone);
    }

    private void updatePackages(String code, String phone) {

        // Instantiate the RequestQueue.
        SharedPreferences sharedpreferences = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.SIGN_UP_REQUEST_URL + "?phone=" + phone + "&phone_with_code=" + code + phone + "&device_token=" + "notoken" + "&app_packages=" + sharedpreferences.getString("apps", null);

        Log.d(TAG, url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response);

                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Constants.showNetworkDialog(DisplayContactsActivity.this);
                Toast.makeText(DisplayContactsActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private void checkUpdateVersion(final Context context){


        Log.d(TAG,"Update Checking Started");


        String serverResponse = getStringFromUrl("http://www.contactsyncer.com/droidappinfo.json");


        if (serverResponse == null){
            return;
        }

        Log.d("Update Response",serverResponse);

        JSONObject jsonObject = new JSONObject();

        try
        {
            jsonObject = new JSONObject(serverResponse);
        }catch (JSONException ex){
            ex.printStackTrace();
        }

        if (jsonObject.has("app_share")){

            int versionOnPlayStore = BuildConfig.VERSION_CODE;
            final List<String> features = new ArrayList<>();

            try {

                versionOnPlayStore = Integer.parseInt(jsonObject.getString("app_share"));


            }catch(JSONException ex){
                ex.printStackTrace();
            }


            if (BuildConfig.VERSION_CODE < versionOnPlayStore){

                ((Activity) context ).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showForceUpdateDialog();
                    }
                });

            }
        }
    }


    public static String getStringFromUrl(String completeurl){
        InputStream is = null;
        JSONObject jsonObject=null;
        String jsonstring="";
        try {
            URL url = new URL(completeurl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            is = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(is).useDelimiter("\\A");
            if(s.hasNext()){
                jsonstring= s.next();
            }
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            Log.d("error", "error in getjsonfromurl MalformedUrlexception");
        } catch (IOException e) {
            Log.d("error", "error in getjsonfromurl Ioexception");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return jsonstring;
    }

    private List<ContactInfo> createList() {

        List<ContactInfo> result = new ArrayList<ContactInfo>();

        mPhoneList = new ArrayList<>();
        mNameList = new ArrayList<>();

        mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (mCursor.moveToNext()) {

            // ContactInfo ci = new ContactInfo();

            String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phonenumber = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (!phonenumber.contains("+"))

                phonenumber = phonenumber.replaceAll("[\\W_]", "").replaceFirst("^0*", "");

            else
                phonenumber = "+" + phonenumber.replaceAll("[\\W_]", "").replaceFirst("^0*", "");

          //  Log.d("phone number is ", phonenumber);

            if (!mPhoneList.contains(phonenumber)) {
                mPhoneList.add(phonenumber);

                mNameList.add(name);
            }
        }

        mCursor.close();
        new VerifyPhoneNumbers().execute();
        return result;
    }

    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                DisplayContactsActivity.this,
                Manifest.permission.READ_CONTACTS)) {

            Toast.makeText(DisplayContactsActivity.this, "CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(DisplayContactsActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case REQUEST_PERMISSION_CODE:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    createList();

                    //  Toast.makeText(DisplayContactsActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(DisplayContactsActivity.this, "Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    public static String postObject(String completeUrl, JSONObject jsonObject, final Activity context) {
        DataOutputStream dataOutputStream;
        InputStream is;
        String jsonstring1 = "";

        try {
            String jsonstring = jsonObject.toString();
            URL url = new URL(completeUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(15000);

            dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(jsonstring.getBytes());
            //Log.d("url calling in post",""+dataOutputStream);


            dataOutputStream.flush();
            dataOutputStream.close();

            int httpResult = httpURLConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                is = new BufferedInputStream(httpURLConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                if (s.hasNext()) {
                    jsonstring1 = s.next();
                }
            }

        } catch (MalformedURLException e) {
            Log.d("error", "malformedUrl in Post");
        } catch (final IOException e) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(e instanceof SocketTimeoutException || e instanceof UnknownHostException) {
                        Constants.showNetworkDialog(context);
                    }
                }
            });

            Log.d("error", "IOException in Post");
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            Log.d("error", "Exception in Post");
        }


        return jsonstring1;
    }

    public void onContactClick(int pos) {

        Log.d("name clicked is ", result.get(pos).name + "  token is " + mDeviceTokens.get(pos));

        Intent in = new Intent(this, ContactAppsActivity.class);
        in.putExtra("apps", mContactApps.get(pos).split("-"));
        in.putExtra("name", result.get(pos).name);
        startActivity(in);

        overridePendingTransition(R.anim.slide_out_one, R.anim.slide_in_one);
    }

    public void setBillingHelper(IabHelper billingHelper) {
        mBillingHelper = billingHelper;
    }

    public void updatePremium() {
        mIsPremium = "yes";
    }

    protected class VerifyPhoneNumbers extends AsyncTask<String, Void, ArrayList<ContactInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ContactInfo> doInBackground(String... params) {

            String requestUrl = Constants.VERIFY_PHONE_NUMBERS_REQUEST_URL;


            JSONObject jsonObject = new JSONObject();

            try {

                jsonObject.put("phonenumbers", mPhoneList);

            } catch (JSONException j) {
                j.printStackTrace();
            }

            String response = postObject(requestUrl, jsonObject, DisplayContactsActivity.this);

            if (response == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DisplayContactsActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });

                return null;
            }

            Log.d(TAG, response);

            JSONObject obj = null;
            try {
                obj = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

// Retrieve number array from JSON object.
            JSONArray array = obj.optJSONArray("isPhoneExists");
            JSONArray tokens = obj.optJSONArray("tokens");
            JSONArray imageUrls = obj.optJSONArray("imageUrl");
            JSONArray apps = obj.optJSONArray("packages");


            result = new ArrayList<>();

            mDeviceTokens.clear();
            mContactApps.clear();
            result.clear();

            ArrayList<String> imgurls = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {


                //check if phone number exists on server
                if (array.optString(i).equals("y") && !mDeviceTokens.contains(tokens.optString(i))) {
                    ContactInfo ci = new ContactInfo();
                    ci.phoneNumber = mPhoneList.get(i);
                    ci.name = mNameList.get(i);
                    mDeviceTokens.add(tokens.optString(i));

                    if(mAllApps == null)
                        mAllApps = new StringBuilder(apps.optString(i));
                    else
                        mAllApps = mAllApps.append("-" + apps.optString(i));

                    mContactApps.add(apps.optString(i));

                    if ((imageUrls.optString(i).equals("noimage")) || imageUrls.optString(i).equals("")) {
                        ci.imageURL = imageUrls.optString(i);
                        result.add(ci);

                    } else {
                        ci.imageURL = IMAGE_ACCESS_URL + imageUrls.optString(i) + ".png";

                        if(!imgurls.contains(ci.imageURL)) {
                            imgurls.add(ci.imageURL);
                            result.add(ci);
                        }
                    }

                }


            //    Log.d("package is ", apps.optString(i));
              //  Log.d("token is ", tokens.optString(i));

            }

            return result;

        }

        @Override
        protected void onPostExecute(ArrayList<ContactInfo> s) {
            super.onPostExecute(s);

            if (s == null) {
                Constants.showNetworkDialog(DisplayContactsActivity.this);
                mProgressDialog.cancel();
                return;
            }

            if(result.size() > 0) {
                ((ImageView)findViewById(R.id.trending)).setVisibility(View.VISIBLE);
                ((RelativeLayout)findViewById(R.id.no_contacts_layout)).setVisibility(View.GONE);
                ContactAdapter ca = new ContactAdapter(s, DisplayContactsActivity.this);
                mRecList.setAdapter(ca);

                if(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("new apps", null) != null)
                {
                    Intent msgIntent = new Intent(DisplayContactsActivity.this, NotificationService.class);
                    msgIntent.putStringArrayListExtra("tokens", mDeviceTokens);
                    msgIntent.putExtra("type", "new apps");
                    msgIntent.putExtra("new apps", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("new apps", null));
                    startService(msgIntent);
                    getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).edit().remove("new apps").commit();
                }

                if(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("new", "no").equals("yes")) {

                    (getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).edit().putString("new", "no")).commit();
                    Intent msgIntent = new Intent(DisplayContactsActivity.this, NotificationService.class);
                    msgIntent.putStringArrayListExtra("tokens", mDeviceTokens);
                    msgIntent.putExtra("type", "new user");
                    startService(msgIntent);

                }

            } else {
                ((RelativeLayout)findViewById(R.id.no_contacts_layout)).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.trending)).setVisibility(View.INVISIBLE);
            }

            if(mIsPremium == null || !mIsPremium.equals("yes"))
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            mProgressDialog.cancel();


        }
    }

    private void showForceUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(this.getString(R.string.youAreNotUpdatedTitle));
        alertDialogBuilder.setMessage(this.getString(R.string.youAreNotUpdatedMessage));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DisplayContactsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DisplayContactsActivity.this.getPackageName())));
                dialog.cancel();
                DisplayContactsActivity.this.finish();
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //new contact added, refresh the list
        if (mFromAddContact) {
            createList();
        }

        mFromAddContact = false;
        //getDelegate().onStart();
    }



}
