package labelingStudy.nctu.minuku_2.service;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku_2.MainActivity;

public class JobSchedulerService extends JobService {

    private static final String TAG = "JobSchedulerService";

    String url ="http://notiaboutness.nctu.me/notification/sample?userId=" + Constants.DEVICE_ID;
    RequestQueue mRequestQueue;

    public static ArrayList<ArrayList> GetDataList = new ArrayList<>();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "(test Receive) OnStartJob");
        HttpGetDataHandler();
        MinukuStreamManager.getInstance().sendTagNotification(this);
//        if(GetAPIDataList().size() == 2){
//            JobScheduler jobScheduler =
//                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            jobScheduler.cancel(0);
//            Log.d(TAG, "cancelling scheduled job");
//        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "OnStopJob");

        return false;
    }

    public void HttpGetDataHandler(){

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

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
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "(test Receive) HttpDataHandler (onResponse): " + response);
                        try {
//                            GetDataList = new ArrayList<>();
                            JSONObject obj = new JSONObject(response);

                            String noti_id = obj.getString("_id");
                            String content = obj.getString("content");
                            String timestamp = obj.getString("localtime");
                            String title = obj.getString("title");
                            String package_name = obj.getString("package_name");

                            ArrayList<String> item = new ArrayList<>();
                            item.add(noti_id);
                            item.add(package_name);
                            item.add(timestamp);
                            item.add(title);
                            item.add(content);

                            GetDataList.add(item);



//                                Log.d(TAG, "item 0: " + GetDataList.get(0));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "(test Receive) Could not parse malformed JSON: \"" + response + "\"");

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
