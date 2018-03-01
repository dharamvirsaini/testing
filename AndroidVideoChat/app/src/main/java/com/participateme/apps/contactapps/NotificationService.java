package com.participateme.apps.contactapps;

/**
 * Created by dharamvir on 19/02/2018.
 */


        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.Arrays;

        import android.app.Activity;
        import android.app.IntentService;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Message;
        import android.os.Messenger;
        import android.util.Log;
        import android.widget.RelativeLayout;
        import android.widget.Toast;

        import com.android.volley.AuthFailureError;
        import com.android.volley.Request;
        import com.android.volley.RequestQueue;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.StringRequest;
        import com.android.volley.toolbox.Volley;
        import com.google.firebase.iid.FirebaseInstanceId;

        import org.json.JSONException;
        import org.json.JSONObject;
        import org.jsoup.Jsoup;
        import org.jsoup.nodes.Document;
        import org.jsoup.select.Elements;

public class NotificationService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.vogella.android.service.receiver";

    public NotificationService() {
        super("NotificationService");
    }

    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {
        String packageNames = intent.getStringExtra("new apps");
        ArrayList<String> tokens = new ArrayList<String>(intent.getStringArrayListExtra("tokens"));

        ArrayList<String> newApps = new ArrayList<String>();
        String notificationText = null;
        Document jsDoc;

        if(packageNames.contains("-"))
        newApps = new ArrayList<String>(Arrays.asList(intent.getStringExtra("new apps").split("-")));
        else
            newApps.add(packageNames);

            //send notification for multiple apps

            for(int i = 0; i < newApps.size(); i++) {
                try {
                   jsDoc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + newApps.get(i) + "&hl=en").get();
                   Elements appName = jsDoc.select("div.id-app-title");

                   if(notificationText == null) {
                       notificationText = appName.first().ownText();

                       if(notificationText.length() > 10) {
                           notificationText = "Your friend " + getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", "unknownname") + " has just installed " + notificationText.substring(0, 9) + "...";
                       }
else{
                           notificationText = "Your friend " + getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", "unknownname") + " has just installed " + notificationText;
                       }
                   }

                } catch (IOException e) {
                    newApps.remove(i);
                    e.printStackTrace();
                }
            }

            if(newApps.size() > 1) {
                notificationText = notificationText + " and " + (newApps.size() - 1) + " other applications";
            }

notifyCaller(tokens, notificationText);

        }
        //publishResults(output.getAbsolutePath(), result);

    private void postJSON(String requestUrl, final JSONObject js) {

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest jsonObjReq = new StringRequest(
                Request.Method.POST, requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
Log.d("NotificationService", response);
                            }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NotificationService", "Error in onErrorResponse: " + error.getMessage());

            }
        }) {

            /**
             * Passing some request headers
             */


            @Override
            public byte[] getBody () throws AuthFailureError {
                Log.d("NotificationService", js.toString());
                return js.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

        };

        requestQueue.add(jsonObjReq);
    }


    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    void notifyCaller(ArrayList<String> tokens, String message) {

        String requestUrl = Constants.NOTIFY_CALLER_REQUEST_URL;

        JSONObject jsonObject = new JSONObject();
        // JSONObject js = new JSONObject();

        try {

            jsonObject.put("name", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("name", null));
            jsonObject.put("device_tokens", tokens);
            jsonObject.put("id", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("id", null));
            jsonObject.put("msg", message);
            jsonObject.put("apps", getSharedPreferences(PhoneAuthActivity.MyPREFERENCES, MODE_PRIVATE).getString("apps", null));
        } catch (JSONException j) {
            j.printStackTrace();
        }

        //  String response = DisplayContactsActivity.postObject(requestUrl, jsonObject);
        postJSON(requestUrl, jsonObject);

    }
}
