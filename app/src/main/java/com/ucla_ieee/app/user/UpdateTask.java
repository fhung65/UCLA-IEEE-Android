package com.ucla_ieee.app.user;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.util.JsonServerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends a request to server to authorize and change member stats
 */
public class UpdateTask extends AsyncTask<Void, Void, String> {

    private final MainActivity mContext;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public UpdateTask(Context context) {
        mContext = (MainActivity) context;
        mSessionManager = new SessionManager(mContext);
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://ieeebruins.org/membership_serve/users.php");

        List<NameValuePair> getParams = new ArrayList<NameValuePair>();
        getParams.add(new BasicNameValuePair("service", "get_user"));
        getParams.add(new BasicNameValuePair("email", mSessionManager.getString(SessionManager.Keys.EMAIL)));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(getParams));
            HttpResponse response = httpClient.execute(httpPost);
            return mUtil.getStringFromServerResponse(response.getEntity());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        JsonObject json = mUtil.getJsonObjectFromString(response);
        if (json == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            mContext.getTaskManager().finishUpdateUserTask();
            return;
        }

        if (json.get("success") != null && json.get("success").getAsInt() == 1) {
            mSessionManager.updateSession(json.getAsJsonObject("user"));

        } else {
            if (json.get("error_message") != null) {
                Toast.makeText(mContext, json.get("error_message").getAsString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Something went really wrong", Toast.LENGTH_SHORT).show();
            }
        }

        mContext.getTaskManager().finishUpdateUserTask();
    }
}
