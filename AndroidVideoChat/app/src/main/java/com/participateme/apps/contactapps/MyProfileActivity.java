package com.participateme.apps.contactapps;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

public class MyProfileActivity extends AppCompatActivity {

    private ArrayList<String> mAppNames = new ArrayList<>();
    private ArrayList<String> mRatings = new ArrayList<>();
    private ArrayList<String> mNumDownloads = new ArrayList<>();
    private ArrayList<String> mAppIcons = new ArrayList<>();

    private MaterialDialog mProgressDialog;
    private ArrayList<String> mContactApps;
    private RecyclerView mRecList;
    private ProgressBar mProgressBar;
    private InterstitialAd mInterstitialAd;
    private String mIsPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

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

        String isPremium = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("isPremium", null);

        if(isPremium != null && isPremium.equals("yes")){
            updatePremium();
        }

        mRecList = (RecyclerView) findViewById(R.id.cardList);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        ((ImageView)findViewById(R.id.backimage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyProfileActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        ((TextView)findViewById(R.id.name_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyProfileActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        ((TextView)findViewById(R.id.hide_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.hide_text)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.update_text)).setVisibility(View.GONE);

                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        mProgressDialog = new MaterialDialog.Builder(MyProfileActivity.this)
                .cancelable(false)
                .progress(true, 100)
                .content("Please wait...")
                .build();

        mProgressDialog.show();
        checkBackUpStatus();


    }

    public void updatePremium() {
        mIsPremium = "yes";
    }

    public void checkBackUpStatus() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.CHECK_BACKUP_REQUEST_URL + "?ID=" + getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("id", null);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d("MyProfileActivity", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String status = jsonObject.getString("status");

                            if(status.equals("fail")) {
                                mContactApps = new ArrayList<String>(Arrays.asList(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("apps", null).split("-")));
                                updateBackup(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("apps", null));
                            }
                            else {
                                mContactApps = new ArrayList<String>(Arrays.asList(jsonObject.getString("backup").split("-")));
                                String[] myApps = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("apps", null).split("-");

                                for(int i = 0; i < myApps.length; i++) {

                                    if(!mContactApps.contains(myApps[i])) {
                                        mContactApps.add(myApps[i]);
                                    }
                                }

                                updateBackup(mContactApps.toString().substring(1, mContactApps.toString().length() - 1).replaceAll(", " ,"-"));

                            }



                            Log.d("MyProfileActivity", jsonObject.getString("status"));
                        } catch (JSONException e) {
                            Constants.showNetworkDialog(MyProfileActivity.this);
                            e.printStackTrace();
                        }
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Constants.showNetworkDialog(MyProfileActivity.this);
                Toast.makeText(MyProfileActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void updateBackup(String apps) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.UPDATE_BACKUP_REQUEST_URL + "?id=" + getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("id", null) + "&apps=" + apps ;
Log.d("MyProfileActivity", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d("MyProfileActivity", response);
                        ContactAppsAdapter ca = new ContactAppsAdapter(MyProfileActivity.this);
                        mRecList.setAdapter(ca);

                        new VersionChecker().execute();

                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MyProfileActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private class VersionChecker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Document jsDoc = null;
            String imgSrc = null;

            for(int i = 0; i < mContactApps.size(); i++) {

                try {
                    Log.d("app is ", mContactApps.get(i));
                    jsDoc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContactApps.get(i) + "&hl=en").get();
                    // System.out.println(jsDoc);

                    Elements img = jsDoc.select("div.cover-container img[src]");
                    Elements appName = jsDoc.select("div.id-app-title");
                    Elements rating = jsDoc.select("div.score");
                    Elements numDownloads = jsDoc.select("div[itemprop=numDownloads]");
                    imgSrc = img.attr("src");

                    // Log.d("rating is ", rating.first().ownText());
                    Log.d("appname is ", appName.first().ownText());
                    Log.d("total downloads are ", numDownloads.first().ownText());
                    Log.d("image is ", imgSrc);

                    mNumDownloads.add(numDownloads.first().ownText());

                    if(imgSrc.contains("https:"))
                        mAppIcons.add(imgSrc);
                    else
                        mAppIcons.add("https:" + imgSrc);

                    mAppNames.add(appName.first().ownText());

                    if(rating != null && rating.first() != null)
                        mRatings.add(rating.first().ownText());
                    else
                        mRatings.add("no ratings");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecList.getAdapter().notifyDataSetChanged();
                            mProgressDialog.cancel();
                        }
                    });

                } catch (Exception e) {
                    mContactApps.remove(i);
                    --i;
                    e.printStackTrace();
                }


                final int j = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(j == 0)
                            ((RelativeLayout)findViewById(R.id.progress_container)).setVisibility(View.VISIBLE);

                        int progress = (j*100)/mContactApps.size();
                        mProgressBar.setProgress(progress);

                        ((TextView)findViewById(R.id.progress_value)).setText(Integer.toString(progress) + "%");

                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ( (RelativeLayout)findViewById(R.id.progress_container)).setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.update_text)).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.hide_text)).setVisibility(View.VISIBLE);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);

          /*  ContactAppsAdapter ca = new ContactAppsAdapter(MyProfileActivity.this);
            mRecList.setAdapter(ca);*/

            if(mIsPremium == null || !mIsPremium.equals("yes"))
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

             mProgressDialog.cancel();
        }
    }

    public ArrayList<String> getAppImages()
    {
        return mAppIcons;
    }

    public ArrayList<String> getAppNames()
    {
        return mAppNames;
    }

    public ArrayList<String> getNumDownloads()
    {
        return mNumDownloads;
    }

    public ArrayList<String> getRatings()
    {
        return mRatings;
    }

    public ArrayList<String> getPackages()
    {
        return mContactApps;
    }

}
