package com.participateme.apps.contactapps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TrendingAppsActivity extends AppCompatActivity {

    private static final String TAG = "TrendingAppsActivity";


    private ArrayList<String> mAppNames = new ArrayList<>();
    private ArrayList<String> mRatings = new ArrayList<>();
    private ArrayList<String> mNumDownloads = new ArrayList<>();
    private ArrayList<String> mAppIcons = new ArrayList<>();
    private ArrayList<String> mFriendsNumber = new ArrayList<>();

    private MaterialDialog mProgressDialog;
    private ArrayList<String> mContactApps = new ArrayList<>();
    private RecyclerView mRecList;
    private InterstitialAd mInterstitialAd;
    private ProgressBar mProgressBar;
    private String mAllApps;
    private int mTop = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_trending_apps_contact);
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
        mAllApps = in.getStringExtra("apps");
        Log.d("all apps are ", mAllApps);







     //   mContactApps = new ArrayList<String>(Arrays.asList(in.getStringArrayExtra("apps")));

        String name = "Back";
       // String name = "Dharamvir Saini";

        if (name.length() > 10) {
            name = name.substring(0, 10).trim() + "...";
        }
        ((TextView)findViewById(R.id.name_text)).setText(name);

       // String phone = in.getStringExtra("phone");

        mProgressDialog = new MaterialDialog.Builder(TrendingAppsActivity.this)
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
                TrendingAppsActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        ((TextView)findViewById(R.id.name_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrendingAppsActivity.this.finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        ((TextView)findViewById(R.id.filter_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        getTopApps(mTop);

    }

    private void showAlertDialog() {

        final Integer[] numArray = {5, 10, 15, 20};
        ArrayList<Integer> tempList = new ArrayList<Integer>(Arrays.asList(numArray));


     /*   AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Number")
                .setItems(R.array.numApps, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] items = getResources().getStringArray(R.array.numApps);

                        if(mTop == numArray[which])
                            dialog.dismiss();
                        else {
                            mAppNames.clear();
                            mNumDownloads.clear();
                            mRatings.clear();
                            mAppIcons.clear();
                            mContactApps.clear();
                            mFriendsNumber.clear();
                            mTop = numArray[which];
                            getTopApps(mTop);

                            ((TextView)findViewById(R.id.chats_text)).setText("Top " + (Integer.toString(numArray[which]) + " apps"));
                        }


                    }
                });
         builder.show();
*/

        new MaterialDialog.Builder(this)
                .title("Showing Top " + Integer.toString(mTop) +  " Apps")
                .items(R.array.numApps)
                .itemsCallbackSingleChoice(tempList.indexOf(mTop), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        String[] items = getResources().getStringArray(R.array.numApps);

                        if(mTop == numArray[which])
                            dialog.dismiss();
                        else {
                            mAppNames.clear();
                            mNumDownloads.clear();
                            mRatings.clear();
                            mAppIcons.clear();
                            mContactApps.clear();
                            mFriendsNumber.clear();
                            mTop = numArray[which];
                            getTopApps(mTop);

                            ((TextView)findViewById(R.id.chats_text)).setText("Top " + (Integer.toString(numArray[which]) + " Apps"));
                       dialog.dismiss();
                        }
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        return true;
                    }
                })
                .show();


    }

    private void getTopApps(int mTop) {

        String[] total = mAllApps.split("-");
        HashMap<String, Integer> mTopAppsMap = new HashMap<>();

        for(int i = 0; i < total.length; i++) {

            if(mTopAppsMap.containsKey(total[i])) {
                mTopAppsMap.put(total[i], mTopAppsMap.get(total[i]) + 1);
            }
                else {
                    mTopAppsMap.put(total[i], 1);
                }

        }

        Set<Map.Entry<String, Integer>> set = mTopAppsMap.entrySet();
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String,Integer>>()
        {
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

      //  int i = 0;
        for(Map.Entry<String, Integer> entry:list){

            mFriendsNumber.add(Integer.toString(entry.getValue()));
            mContactApps.add(entry.getKey());
           // i++;

            //if(i == mTop)
              //  break;
            //System.out.println(entry.getKey()+" ==== "+entry.getValue());
        }

        new VersionChecker().execute();

        TrendingAppsAdapter ca = new TrendingAppsAdapter(TrendingAppsActivity.this);
        mRecList.setAdapter(ca);


    }

    public ArrayList<String> getFriendsNumber() {
        return mFriendsNumber;
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
                                Constants.showNetworkDialog(TrendingAppsActivity.this);

                            }
                        });
                        break;
                    }
                }

                final int j = mAppNames.size();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(j == 0)
                            ((RelativeLayout)findViewById(R.id.progress_container)).setVisibility(View.VISIBLE);

                        int progress = (j*100)/mTop;
                        mProgressBar.setProgress(progress);

                        ((TextView)findViewById(R.id.progress_value)).setText(Integer.toString(progress) + "%");

                    }
                });

                if(mAppIcons.size() == mTop)
                {
                    break;
                }

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

        //    ContactAppsAdapter ca = new ContactAppsAdapter(TrendingAppsActivity.this);
          //  mRecList.setAdapter(ca);



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
