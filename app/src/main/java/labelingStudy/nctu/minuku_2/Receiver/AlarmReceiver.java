package labelingStudy.nctu.minuku_2.Receiver;

import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.service.JobSchedulerService;

public class AlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    //job id 用以區別任務
    int JOB_ID = 0;
    int count;
    private static final String TAG = "AlarmReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "(test Receive) onReceive");
//        HttpGetDataHandler();

        Bundle extras = intent.getExtras();
        Intent i = new Intent("broadCastName");
        // Data you need to pass to activity
        i.putExtra("message", "1234");

        context.sendBroadcast(i);

//        創建一個JobScheduler對象
        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(mContext.getPackageName(), JobSchedulerService.class.getName()));

            //設置任務延遲執行的時間，單位毫秒
//            jobBuilder.setMinimumLatency(60 * 1000);
        jobBuilder.setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
//        jobBuilder.setPeriodic(15 * 60 * 1000);
        jobBuilder.setRequiresDeviceIdle(false);
            //設置是否在設備重啟後，要繼續執行
            jobBuilder.setPersisted(true);

            JobScheduler mJobScheduler = (JobScheduler)mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            mJobScheduler.schedule(jobBuilder.build());

            if ((mJobScheduler.schedule(jobBuilder.build())) <= 0) {
                Log.i(TAG, "(test Receive) something goes wrong");
            }


    }


}
