package labelingStudy.nctu.minuku_2.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    String url ="http://notiaboutness.nctu.me/notification/sample?userId=" + Constants.DEVICE_ID;


    RequestQueue mRequestQueue;
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "onReceive");
        HttpGetDataHandler();

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
                        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (onResponse): " + response);

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
}
