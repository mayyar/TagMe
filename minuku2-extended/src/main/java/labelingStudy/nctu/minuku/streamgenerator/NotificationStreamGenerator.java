package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import androidx.room.Room;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;

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

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.NotificationDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.stream.NotificationStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

public class NotificationStreamGenerator extends AndroidStreamGenerator<NotificationDataRecord> {


    public static final String NOTIFICATION_NAME_NA = "NA";

    private Context mContext;
    String TAG = "NotificationStreamGenerator";
    String room = "room";
    private NotificationStream mStream;
    private NotificationDataRecordDAO notificationDataRecordDAO;

    private static NotificationManager notificationManager;

    public static int mNotificaitonId = 0;
    public static String mNotificaitonTitle = "";
    public static String mNotificaitonText = "";
    public static String mNotificaitonSubText = "";
    public static String mNotificationTickerText = "";
    public static  String mNotificaitonPackageName ="";

    public static String preNotificaitonTitle = "";
    public static String preNotificaitonText = "";

    private NotificationListenService notificationlistener;
    public static Integer accessid=-1;
    appDatabase db;

    String url ="http://notiaboutness.nctu.me/notification/save";


    RequestQueue mRequestQueue;



    public NotificationStreamGenerator(){
        super();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, NotificationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which NotificationDataRecord/NotificationStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides NotificationDataRecord/NotificationStream is already registered.");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("ServiceCast")
    public NotificationStreamGenerator(Context applicationContext) {
        super(applicationContext);


        mContext = applicationContext;
        notificationlistener = new NotificationListenService(this);
        this.mStream = new NotificationStream(Constants.DEFAULT_QUEUE_SIZE);
        notificationDataRecordDAO = appDatabase.getDatabase(applicationContext).notificationDataRecordDao();

        //  notificationManager = (android.app.NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);


        mNotificaitonTitle = "Default";
        mNotificaitonText = "Default";
        mNotificaitonSubText = "Default";
        mNotificationTickerText = "Default";
        mNotificaitonPackageName ="Default";
        accessid = -1;

        preNotificaitonTitle = mNotificaitonTitle;
        preNotificaitonText = mNotificaitonText;


        this.register();
    }



    @Override
    public Stream<NotificationDataRecord> generateNewStream() {
        return null;
    }

    @Override
    public boolean updateStream() {

        Log.d(TAG, "update stream called");

        NotificationDataRecord notificationDataRecord =
                new NotificationDataRecord(mNotificaitonTitle, mNotificaitonText, mNotificaitonSubText
                        , mNotificationTickerText, mNotificaitonPackageName ,accessid);

        Log.d(TAG, " " + mNotificaitonTitle + mNotificaitonText + mNotificaitonSubText
                 + mNotificationTickerText + mNotificaitonPackageName + accessid);


        if(preNotificaitonTitle.equals(mNotificaitonTitle) && preNotificaitonText.equals(mNotificaitonText))
            Log.d(TAG, "Equal = preNotificaitonText: "  + preNotificaitonText + " mNotificaitonText: " + mNotificaitonText);
        else {
            Log.d(TAG, mNotificaitonId + "Not Equal preNotificaitonText: "  + preNotificaitonText + " mNotificaitonText: " + mNotificaitonText);
            preNotificaitonTitle = mNotificaitonTitle;
            preNotificaitonText = mNotificaitonText;
            HttpDataHandler();
            mNotificaitonId++;
        }

        mStream.add(notificationDataRecord);
        Log.d(TAG, "Check notification to be sent to event bus " + notificationDataRecord);
        // also post an event.
        Log.d("creationTime : ", "notiData : "+notificationDataRecord.getCreationTime());

        EventBus.getDefault().post(notificationDataRecord);


        try {
//            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                    .allowMainThreadQueries()
//                    .build();
            notificationDataRecordDAO.insertAll(notificationDataRecord);
            List<NotificationDataRecord> notificationDataRecords = notificationDataRecordDAO.getAll();
            for (NotificationDataRecord l : notificationDataRecords) {
                Log.d(TAG, " Notification_id: " + String.valueOf(l.get_id()));
                Log.d(TAG, " NotificationPackageName: " + String.valueOf(l.getNotificaitonPackageName()));
                Log.d(TAG, " NotificationTitle: " + String.valueOf(l.getNotificaitonTitle()));
                Log.d(TAG, " NotificationText: " + String.valueOf(l.getNotificaitonText()));
                Log.d(TAG, " NotificationAccessid: " + String.valueOf(l.getaccessid()));

            }
//            List<NotificationDataRecord> NotificationDataRecords = db.notificationDataRecordDao().getAll();
//            for (NotificationDataRecord n : NotificationDataRecords) {
//                Log.d(room,"Notification: "+n.getNotificaitonPackageName());
//            }

        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        MinukuStreamManager.getInstance().setNotificationDataRecord(notificationDataRecord, mContext);


        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

    }

    public void setNotificationDataRecord(String title, String text, String subText, String tickerText,String pack,Integer accessid){

        this.mNotificaitonTitle = title;
        this.mNotificaitonText = text;
        this.mNotificaitonSubText = subText;
        this.mNotificationTickerText = tickerText;
        this.mNotificaitonPackageName = pack;
        this.accessid = accessid;
        Log.d(TAG, "title:"+title+" text: "+text + " subText: "+subText+" ticker: "+tickerText+" pack: "+pack);

    }

    public static String getNotificaitonTitle() {
        return mNotificaitonTitle;
    }

    public static String getNotificaitonText() {
        return mNotificaitonText;
    }

    public static String getNotificaitonSubText() {
        return mNotificaitonSubText;
    }

    public static String getNotificationTickerText() {
        return mNotificationTickerText;
    }

    public static String getNotificaitonPackageName() {
        return mNotificaitonPackageName;
    }

    public static Integer getaccessid() {
        return accessid;
    }
    @Override
    public void offer(NotificationDataRecord dataRecord) {

    }

    public void HttpDataHandler(){
        // Instantiate the cache
        Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();


        StringRequest myStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (onResponse): " + response);

            }
        }, new Response.ErrorListener() {
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
        }){

            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                //MyData.put("_id", "1"); //Add the data you'd like to send to the server.
                MyData.put("userId", Constants.DEVICE_ID); //Constants.DEVICE_ID
                MyData.put("notiId", String.valueOf(mNotificaitonId));
                MyData.put("title", mNotificaitonTitle); //mNotificaitonTitle
                MyData.put("packageName", mNotificaitonPackageName); //mNotificaitonPackageName
                MyData.put("category", "test");
                MyData.put("content", mNotificaitonText);//mNotificaitonText
                //MyData.put("timestamp", "7");

                labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (getParams): put Data Ready!");

                return MyData;
            }

        };


//        mRequestQueue = Volley.newRequestQueue(this);

        mRequestQueue.add(myStringRequest);
        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler: Insert success");

    }
}
