package com.participateme.apps.contactapps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.participateme.apps.contactapps.Billing.IabHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;


public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mProfileImage, mOkImage;
    private final int PICK_IMAGE_REQUEST = 574;
    final int PIC_CROP = 3;
    private EditText mNameText;
    private String name;
    private Bitmap bitmap;
    private Uri picUri;
    IabHelper mBillingHelper;
    private String mIsPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_info);

        mProfileImage = (ImageView)findViewById(R.id.imageView);
        mOkImage = (ImageView) findViewById(R.id.imageView2);


        mNameText = (EditText)findViewById(R.id.editText2);
        TextView premium_text = (TextView)findViewById(R.id.premium_textview);
        TextView premium_user_text = (TextView)findViewById(R.id.premium_user);

        if(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("isPremium", "no").equals("yes")) {
            premium_user_text.setVisibility(View.VISIBLE);
            premium_text.setVisibility(View.GONE);
        }
        else {

            premium_user_text.setVisibility(View.GONE);
            premium_text.setVisibility(View.VISIBLE);
        }

//        String htmlString="<u>Premium Upgrade</u>";
//        premium_text.setText(Html.fromHtml(htmlString));

        premium_text.setOnClickListener(this);



        if(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("image_data", "noimage") != "noimage")
        {
            byte[] b = Base64.decode(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("image_data", "noimage"), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            mProfileImage.setImageBitmap(bitmap);

            mNameText.setText(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", "Unknown"));
        }
        else if(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", "unknownname") != "unknownname")
        {
            mNameText.setText(getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", "Unknown"));
        }
        
        mProfileImage.setOnClickListener(this);
        mOkImage.setOnClickListener(this);

    }

    private void updateProfileInfo() {

    if(mNameText.getText().toString().trim().equals("")){

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.profile_info_container),"Please enter your name...",Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
        return;
    }
    else{

        name = mNameText.getText().toString().trim();
        Log.d("name is " , name);
        postJSON();
      //  new UpdateInfoOnServer().execute();
    }

}


    public String getStringImage(Bitmap bmp){
        if (bmp != null){

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

        return "noimage";
    }

    private void showFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private void performCrop(){
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {

                picUri = data.getData();
                Log.d("uriGallery", picUri.toString());
                performCrop();

              /*  Uri imagePath = data.getData();

                try {
                    //Getting the Bitmap from Gallery
                    Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

                    //Scaling the bitmap as it might cause issues OPENGL RENDERING
                    //  Bitmap bitmap1= new Bitma(getResources() , bitmap2).getBitmap();
                    int nh = (int) (bitmap2.getHeight() * (96.0 / bitmap2.getWidth()));
                    bitmap = Bitmap.createScaledBitmap(bitmap2, 96, nh, true);

                    mProfileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            } else if (requestCode == PIC_CROP) {
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                bitmap = (Bitmap) extras.get("data");

                //display the returned cropped image
                mProfileImage.setImageBitmap(bitmap);
            }
            else if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            }
            else {
                Log.d("EditProfileActivity", "onActivityResult handled by IABUtil.");
            }
        }
    }

    void setBillingHelper(IabHelper helper) {
        mBillingHelper = helper;
    }
    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.imageView:
            {
                showFileChooser();
                break;
            }

            case R.id.imageView2:
            {
                updateProfileInfo();
                break;
            }

            case R.id.premium_textview:
            {
                Constants.showUpgradeDialog(this);
            }
        }

    }

    public void updatePremium() {

        mIsPremium = "yes";
        ((TextView)findViewById(R.id.premium_textview)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.premium_user)).setVisibility(View.VISIBLE);
    }

    private void postJSON() {

       final MaterialDialog progressDialog = new MaterialDialog.Builder(EditProfileActivity.this)
                .content("Please wait...")
                .progress(true,100)
                .build();

        progressDialog.show();

        String requestUrl = Constants.UPDATE_PROFILE_REQUEST_URL;

        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("name", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("phone", null));
            jsonObject.put("profileName", name);
            jsonObject.put("image", getStringImage(bitmap));

        } catch (JSONException j) {
            j.printStackTrace();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest jsonObjReq = new StringRequest(
                Request.Method.POST, requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("NotificationService", response);

                            progressDialog.cancel();

                            if(response != null && response.equals("success"))
                            {
                                SharedPreferences.Editor editor = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).edit();
                                editor.putString("name", name);

                                if(getStringImage(bitmap) != "noimage") {

                                    editor.putString("image_data", getStringImage(bitmap));
                                }

                                editor.commit();

                                Intent in = new Intent(EditProfileActivity.this, DisplayContactsActivity.class);
                                in.putExtra("code", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("code", null));
                                in.putExtra("phone", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("phone", null));


                                if(DisplayContactsActivity.sActivityContext != null) {
                                    DisplayContactsActivity.sActivityContext.finish();
                                    in.putExtra("isupdate", "no");
                                }
                                else {
                                    in.putExtra("isupdate", "yes");
                                }

                                startActivity(in);

                                EditProfileActivity.this.finish();
                                overridePendingTransition(R.anim.slide_out_one, R.anim.slide_in_one);
                            }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d("data", body);
                } catch (UnsupportedEncodingException e) {
                    // exception
                }


                if(error instanceof ServerError)
                Log.d("NotificationService", "ServerError in onErrorResponse: " + error.getMessage());

                if(error instanceof TimeoutError)
                    Log.d("NotificationService", "TimeOutError in onErrorResponse: " + error.getMessage());

            }
        }) {

            /**
             * Passing some request headers
             */


            @Override
            public byte[] getBody () throws AuthFailureError {
                Log.d("NotificationService get", jsonObject.toString());
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjReq);
    }

 /*   protected class UpdateInfoOnServer extends AsyncTask<String,Void,String> {

    MaterialDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new MaterialDialog.Builder(EditProfileActivity.this)
                    .content("Please wait...")
                    .progress(true,100)
                    .build();

            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String requestUrl = Constants.UPDATE_PROFILE_REQUEST_URL;

            JSONObject jsonObject = new JSONObject();

            try {

                jsonObject.put("name", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("phone", null));
                jsonObject.put("profileName", name);
                jsonObject.put("image", getStringImage(bitmap));

            } catch (JSONException j) {
                j.printStackTrace();
            }

            String response = DisplayContactsActivity.postObject(requestUrl, jsonObject, EditProfileActivity.this);

            if (response == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditProfileActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });

                return null;
            }

            Log.e("response is", "" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            progressDialog.cancel();

            if(response != null && response.equals("success"))
            {
                SharedPreferences.Editor editor = getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).edit();
                editor.putString("name", name);

                if(getStringImage(bitmap) != "noimage") {

                    editor.putString("image_data", getStringImage(bitmap));
                }

                editor.commit();

                Intent in = new Intent(EditProfileActivity.this, DisplayContactsActivity.class);
                in.putExtra("code", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("code", null));
                in.putExtra("phone", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("phone", null));


                if(DisplayContactsActivity.sActivityContext != null) {
                    DisplayContactsActivity.sActivityContext.finish();
                    in.putExtra("isupdate", "no");
                }
                else {
                    in.putExtra("isupdate", "yes");
                }

                startActivity(in);

                EditProfileActivity.this.finish();
                overridePendingTransition(R.anim.slide_out_one, R.anim.slide_in_one);
            }
        }
    }*/

}