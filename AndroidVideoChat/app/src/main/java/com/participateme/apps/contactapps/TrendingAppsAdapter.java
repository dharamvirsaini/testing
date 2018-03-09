
package com.participateme.apps.contactapps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class TrendingAppsAdapter extends RecyclerView.Adapter<TrendingAppsAdapter.ContactViewHolder> {

    private ArrayList<String> mAppNames;
    private ArrayList<String> mRatings;
    private ArrayList<String> mNumDownloads;
    private ArrayList<String> mAppIcons;
    private ArrayList<String> mFriendsNumber;
    private String myApps;

    private TrendingAppsActivity mContext;
    private MyProfileActivity myProfileContext;


    public TrendingAppsAdapter(final TrendingAppsActivity context) {
        this.mAppNames = context.getAppNames();
        this.mRatings = context.getRatings();
        this.mNumDownloads = context.getNumDownloads();
        this.mAppIcons = context.getAppImages();
        this.mFriendsNumber = context.getFriendsNumber();
        myApps = context.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("apps", null);

        this.mContext = context;

    }

    public TrendingAppsAdapter(final MyProfileActivity context) {
        this.mAppNames = context.getAppNames();
        this.mRatings = context.getRatings();
        this.mNumDownloads = context.getNumDownloads();
        this.mAppIcons = context.getAppImages();
        myApps = context.getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, Context.MODE_PRIVATE).getString("apps", null);

        this.myProfileContext = context;

    }


    @Override
    public int getItemCount() {
        return mAppNames.size();
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder contactViewHolder, int i) {

        contactViewHolder.vName.setText(mAppNames.get(i));
        contactViewHolder.vRatingText.setText(mFriendsNumber.get(i) + " friends using this");


        if(mContext != null) {

            if (myApps.contains(mContext.getPackages().get(i)))
                {
                    contactViewHolder.vInstallButton.setText("installed");
                    contactViewHolder.vInstallButton.setBackgroundResource(R.drawable.mybutton);
                }
                else {
                    contactViewHolder.vInstallButton.setText("install");
                    contactViewHolder.vInstallButton.setBackgroundResource(R.drawable.mybutton_install);
                }
                //contactViewHolder.vCircleProfileImage.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(mAppIcons.get(i))
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                contactViewHolder.vProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(contactViewHolder.vCircleProfileImage);
            }

    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout_top_apps, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        protected TextView vName;
        protected ImageView vCircleProfileImage;
        protected Button vInstallButton;
        protected TextView vRatingText;
        protected ProgressBar vProgressBar;

        public ContactViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.txtName);
            vCircleProfileImage = (ImageView)  v.findViewById(R.id.profile_image_circle);
            vInstallButton = (Button) v.findViewById(R.id.button3);
            vRatingText =  (TextView) v.findViewById(R.id.ratingText);
            vProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

            vInstallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mContext != null)
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackages().get(getAdapterPosition()))));
           else
                        myProfileContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + myProfileContext.getPackages().get(getAdapterPosition()))));

                }
            });
            v.setOnLongClickListener(this);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();

                    if(mContext != null)
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackages().get(pos))));
                    else
                        myProfileContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + myProfileContext.getPackages().get(pos))));
                    //mContext.get

                   // mContext.onContactClick(pos);

                }
            });
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("Adapter", "long click called");
            notifyDataSetChanged();

            return true;
        }
    }
}