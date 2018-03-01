package com.participateme.apps.contactapps;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class ContactAppsActivity extends AppCompatActivity {

    private static final String TAG = "ContactAppsActivity";


    private ArrayList<String> mAppNames = new ArrayList<>();
    private ArrayList<String> mRatings = new ArrayList<>();
    private ArrayList<String> mNumDownloads = new ArrayList<>();
    private ArrayList<String> mAppIcons = new ArrayList<>();

    private MaterialDialog mProgressDialog;
    private ArrayList<String> mContactApps;
    private RecyclerView mRecList;
    private InterstitialAd mInterstitialAd;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_apps_contact);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mInterstitialAd = new InterstitialAd(this);

            mInterstitialAd.setAdUnitId("ca-app-pub-4927131029050901/1694551164");
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

        Intent in = getIntent();
         mContactApps = new ArrayList<String>(Arrays.asList(in.getStringArrayExtra("apps")));

        String name = in.getStringExtra("name");
       // String name = "Dharamvir Saini";

        if (name.length() > 10) {
            name = name.substring(0, 10).trim() + "...";
        }
        ((TextView)findViewById(R.id.name_text)).setText(name);

       // String phone = in.getStringExtra("phone");

        mProgressDialog = new MaterialDialog.Builder(ContactAppsActivity.this)
                .cancelable(false)
                .progress(true, 100)
                .content("Please wait...")
                .build();

        mProgressDialog.show();

        mRecList = (RecyclerView) findViewById(R.id.cardList);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        ((ImageView)findViewById(R.id.backimage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactAppsActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        ((TextView)findViewById(R.id.name_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactAppsActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        new VersionChecker().execute();

        ContactAppsAdapter ca = new ContactAppsAdapter(ContactAppsActivity.this);
        mRecList.setAdapter(ca);
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

                    if(e instanceof UnknownHostException) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Constants.showNetworkDialog(ContactAppsActivity.this);

                            }
                        });
                        break;
                    }
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

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);

            if(!getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("isPremium", "no").equals("yes")) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            ContactAppsAdapter ca = new ContactAppsAdapter(ContactAppsActivity.this);
            mRecList.setAdapter(ca);



            if(!mProgressDialog.isCancelled())
                mProgressDialog.cancel();

           // mProgressDialog.cancel();
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

    @Override
    protected void onResume() {
        super.onResume();

    }

}
