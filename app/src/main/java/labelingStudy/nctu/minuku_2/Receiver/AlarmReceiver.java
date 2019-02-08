package labelingStudy.nctu.minuku_2.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Config;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    String url ="http://notiaboutness.nctu.me/notification/sample?userId=" + Constants.DEVICE_ID;


    RequestQueue mRequestQueue;
    private static final String TAG = "AlarmReceiver";

    public static ArrayList<ArrayList> GetDataList = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "(test Receive) onReceive");
        HttpGetDataHandler();

        Bundle extras = intent.getExtras();
        Intent i = new Intent("broadCastName");
        // Data you need to pass to activity
        i.putExtra("message", "1234");

        context.sendBroadcast(i);

    }

    public void HttpGetDataHandler(){



        // Instantiate the cache
        Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();


        StringRequest getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "HttpDataHandler (onResponse): " + response);
                        try {
                            GetDataList = new ArrayList<>();
                            JSONArray obj = new JSONArray(response);
                            for(int i = 0; i < obj.length(); i++ ){
                                String noti_id = obj.getJSONObject(i).getString("noti_id");
                                String content = obj.getJSONObject(i).getString("content");
                                String timestamp = obj.getJSONObject(i).getString("timestamp");
                                String title = obj.getJSONObject(i).getString("title");
                                String package_name = obj.getJSONObject(i).getString("package_name");

                                ArrayList<String> item = new ArrayList<>();
                                item.add(noti_id);
                                item.add(package_name);
                                item.add(timestamp);
                                item.add(title);
                                item.add(content);

                                GetDataList.add(item);

                            }

//                                Log.d(TAG, "item 0: " + GetDataList.get(0));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");

                        }

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (onErrorResponse): That didn't work " + error);
                        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (onErrorResponse): That didn't work " + error.networkResponse.statusCode);
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            // Print Error!
                            labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (onErrorResponse): That didn't work " + jsonError);

                        }
                    }
                });
        mRequestQueue.add(getRequest);



//

    }

    public static ArrayList GetAPIDataList(){
        return GetDataList;
    }
}
