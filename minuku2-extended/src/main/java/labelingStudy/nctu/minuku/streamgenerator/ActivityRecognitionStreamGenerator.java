package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.service.ActivityRecognitionService;
import labelingStudy.nctu.minuku.stream.ActivityRecognitionStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/5/15.
 */

public class ActivityRecognitionStreamGenerator extends AndroidStreamGenerator<ActivityRecognitionDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final static String TAG = "ActivityRecognitionStreamGenerator";

    private PendingIntent mActivityRecognitionPendingIntent;
    private static GoogleApiClient mGoogleApiClient;

    /**label **/
    public static final String STRING_DETECTED_ACTIVITY_IN_VEHICLE = "in_vehicle";
    public static final String STRING_DETECTED_ACTIVITY_ON_FOOT = "on_foot";
    public static final String STRING_DETECTED_ACTIVITY_WALKING = "walking";
    public static final String STRING_DETECTED_ACTIVITY_RUNNING = "running";
    public static final String STRING_DETECTED_ACTIVITY_TILTING = "tilting";
    public static final String STRING_DETECTED_ACTIVITY_STILL = "still";
    public static final String STRING_DETECTED_ACTIVITY_ON_BICYCLE = "on_bicycle";
    public static final String STRING_DETECTED_ACTIVITY_UNKNOWN = "unknown";
    public static final String STRING_DETECTED_ACTIVITY_NA = "NA";
    public static final int NO_ACTIVITY_TYPE = -1;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "DetectedActivities";

    protected long recordCount;

    private Context mContext;
    private ActivityRecognitionStream mStream;
    private ActivityRecognitionDataRecordDAO activityRecognitionDataRecordDAO;


    private ActivityRecognitionDataRecord activityRecognitionDataRecord;

    /** KeepAlive **/
    protected int KEEPALIVE_MINUTE = 3;
    protected long sKeepalive;

    public static List<DetectedActivity> sProbableActivities;
    public static DetectedActivity sMostProbableActivity;
    private static long sLatestDetectionTime;

    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 5;
    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    public static ArrayList<ActivityRecognitionDataRecord> mLocalRecordPool;

    private static ActivityRecognitionStreamGenerator instance;

    private SharedPreferences sharedPrefs;

    public ActivityRecognitionStreamGenerator(Context applicationContext) { //,Context mContext
        super(applicationContext);
        //this.mContext = mMainServiceContext;
        this.mContext = applicationContext;
        this.mStream = new ActivityRecognitionStream(Constants.LOCATION_QUEUE_SIZE);
        activityRecognitionDataRecordDAO = appDatabase.getDatabase(applicationContext).activityRecognitionDataRecordDao();

        mLocalRecordPool = new ArrayList<ActivityRecognitionDataRecord>();

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, mContext.MODE_PRIVATE);

        ActivityRecognitionStreamGenerator.instance = this;

        sLatestDetectionTime = Constants.INVALID_TIME_VALUE;

        recordCount = 0;
        sKeepalive = KEEPALIVE_MINUTE * Constants.MILLISECONDS_PER_MINUTE;

        this.register();
    }

    @SuppressLint("LongLogTag")
    public static ActivityRecognitionStreamGenerator getInstance(Context applicationContext) {

        if(ActivityRecognitionStreamGenerator.instance == null) {
            try {
                Log.e(TAG,"creating new ActivityRecognitionStreamGenerator.");
                ActivityRecognitionStreamGenerator.instance = new ActivityRecognitionStreamGenerator(applicationContext);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ActivityRecognitionStreamGenerator.instance;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ActivityRecognitionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    @Override
    public void onStreamRegistration() {

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient==null){

            mGoogleApiClient =
                    new GoogleApiClient.Builder(mApplicationContext) // "mApplicationContext" is inspired by LocationStreamGenerator,it might not wrong.
                            .addApi(com.google.android.gms.location.ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();

            mGoogleApiClient.connect();
        }
    }

    @Override
    public Stream<ActivityRecognitionDataRecord> generateNewStream() {
        return mStream;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean updateStream() {
        Log.e(TAG, "Update stream called.");

//        int session_id = SessionManager.getOngoingSessionId();

        int session_id = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        activityRecognitionDataRecord = new ActivityRecognitionDataRecord(sMostProbableActivity, sProbableActivities, sLatestDetectionTime);

        //if there don't have any updates for 10 minutes, add the NA one to represent it
        if((ScheduleAndSampleManager.getCurrentTimeInMillis() - sLatestDetectionTime) >= Constants.MILLISECONDS_PER_MINUTE * 10
                && (sLatestDetectionTime != Constants.INVALID_TIME_VALUE)){

            DetectedActivity initialDetectedActivity = getInitialDetectedActivity();

            ArrayList<DetectedActivity> initialDetectedActivities = new ArrayList<>();
            initialDetectedActivities.add(initialDetectedActivity);

            ActivityRecognitionDataRecord activityRecognitionDataRecord = new ActivityRecognitionDataRecord(initialDetectedActivity, initialDetectedActivities, ScheduleAndSampleManager.getCurrentTimeInMillis());

            mStream.add(activityRecognitionDataRecord);
            try {
//                appDatabase db;
//                db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                        .allowMainThreadQueries()
//                        .build();
                activityRecognitionDataRecordDAO.insertAll(activityRecognitionDataRecord);
                List<ActivityRecognitionDataRecord> activityRecognitionDataRecords = activityRecognitionDataRecordDAO.getAll();
                for (ActivityRecognitionDataRecord a : activityRecognitionDataRecords) {
                    labelingStudy.nctu.minuku.logger.Log.d(TAG, "Detectedtime"+String.valueOf(a.getDetectedtime()));
                    labelingStudy.nctu.minuku.logger.Log.d(TAG, "ostProbableActivity"+a.getMostProbableActivity().toString());
                    labelingStudy.nctu.minuku.logger.Log.d(TAG, "ProbableActivities"+String.valueOf(a.getProbableActivities()));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(activityRecognitionDataRecord);

        if(activityRecognitionDataRecord!=null) {

            mStream.add(activityRecognitionDataRecord);
//            Log.e(TAG, "Activity to be sent to event bus" + activityRecognitionDataRecord);

            EventBus.getDefault().post(activityRecognitionDataRecord);
            try {
//                appDatabase db;
//                db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                        .allowMainThreadQueries()
//                        .build();
                activityRecognitionDataRecordDAO.insertAll(activityRecognitionDataRecord);
                List<ActivityRecognitionDataRecord> activityRecognitionDataRecords = activityRecognitionDataRecordDAO.getAll();
//                for (ActivityRecognitionDataRecord a : activityRecognitionDataRecords) {
//                    labelingStudy.nctu.minuku.logger.Log.e(TAG, "Detectedtime: "+String.valueOf(a.getDetectedtime()));
//                    labelingStudy.nctu.minuku.logger.Log.e(TAG, "ostProbableActivity: "+a.getMostProbableActivity().toString());
//                    labelingStudy.nctu.minuku.logger.Log.e(TAG, "ProbableActivities: "+String.valueOf(a.getProbableActivities()));
//                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void offer(ActivityRecognitionDataRecord dataRecord) {
        Log.e(TAG, "Offer for ActivityRecognition data record does nothing!");
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.e(TAG,"onConnected");

        startActivityRecognitionUpdates();
    }

    private void startActivityRecognitionUpdates() {

        Log.d(TAG, "[startActivityRecognitionUpdates]");

        mActivityRecognitionPendingIntent = createRequestPendingIntent();

        //request activity recognition update
        if (com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi!=null && !ActivityRecognitionService.isServiceRunning()){
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL,//detectionIntervalMillis
                    mActivityRecognitionPendingIntent);   //callbackIntent
        }

    }

    private PendingIntent createRequestPendingIntent() {
        Log.d(TAG, "createRequestPendingIntent");
        // If the PendingIntent already exists
        if (mActivityRecognitionPendingIntent != null) {
            return mActivityRecognitionPendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService

            Intent intent = new Intent(
                    mApplicationContext, ActivityRecognitionService.class);

            PendingIntent pendingIntent =
                    PendingIntent.getService(mApplicationContext, //mApplicationContext || mContext
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            mActivityRecognitionPendingIntent = pendingIntent;
            return pendingIntent;
        }
    }

    public void setActivitiesandDetectedtime (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity, long detectedtime) {
        //set activities

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        setDetectedtime(detectedtime);

        Log.d(TAG,detectedtime+"||"+ mostProbableActivity);

        // Assume isRequested.
        if(probableActivities!=null&&mostProbableActivity!=null)
            saveRecordToLocalRecordPool(mostProbableActivity,detectedtime);

    }

    public void saveRecordToLocalRecordPool(DetectedActivity MostProbableActivity,long Detectedtime) {
        /** create a Record to save timestamp, session it belongs to, and Data**/
        ActivityRecognitionDataRecord record = new ActivityRecognitionDataRecord(MostProbableActivity,Detectedtime);
        record.setProbableActivities(sProbableActivities);

        JSONObject data = new JSONObject();

        //also set data:
        JSONArray activitiesJSON = new JSONArray();

        //add all activities to JSONArray
        for (int i=0; i<sProbableActivities.size(); i++){
            DetectedActivity detectedActivity =  sProbableActivities.get(i);
            String activityAndConfidence = getActivityNameFromType(detectedActivity.getType()) + Constants.ACTIVITY_DELIMITER + detectedActivity.getConfidence();
            activitiesJSON.put(activityAndConfidence);
        }

        //add activityJSON Array to data
        try {
            data.put(RECORD_DATA_PROPERTY_NAME, activitiesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**we set data in Record**/
//        record.setData(data);
//        record.setTimestamp(sLatestDetectionTime);

//        Log.d(TAG, "testing saving records at " + record.getDetectedtime() + " data: " + record.getData());

        addRecord(record);

    }

    private DetectedActivity getInitialDetectedActivity(){

        return new DetectedActivity(-1, 100);
    }

    @SuppressLint("LongLogTag")
    protected void addRecord(ActivityRecognitionDataRecord activityRecognitionDataRecord) {
        /**1. add record to the local pool **/
        long id = recordCount++;
        activityRecognitionDataRecord.set_id(id);
        Log.d(TAG,"CreateTime:" + activityRecognitionDataRecord.getCreationTime()+ " MostProbableActivity:"+activityRecognitionDataRecord.getMostProbableActivity());

        mLocalRecordPool.add(activityRecognitionDataRecord); //it's working.
        Log.d(TAG, "[test logging]add record " + "logged at " + activityRecognitionDataRecord.getCreationTime() );
        /**2. check whether we should remove old record **/
        removeOutDatedRecord();
        //**** update the latest ActivityRecognitionDataRecord in mLocalRecordPool to MinukuStreamManager;
        //mLocalRecordPool.get(mLocalRecordPool.size()-1).set_id(999);
//        Log.d(TAG,"size : "+mLocalRecordPool.size());
//        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(mLocalRecordPool.get(mLocalRecordPool.size()-1));
//        Log.d(TAG,"CreateTime:" + mLocalRecordPool.get(mLocalRecordPool.size()-1).getCreationTime()+ " MostProbableActivity:"+mLocalRecordPool.get(mLocalRecordPool.size()-1).getMostProbableActivity());

        this.activityRecognitionDataRecord = activityRecognitionDataRecord;

        //TODO: now update the value to the TransportationMode every 5 seconds
        try {

            TransportationModeStreamGenerator transportationModeStreamGenerator
                    = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);

            transportationModeStreamGenerator.examineTransportation(activityRecognitionDataRecord);

            sharedPrefs.edit().putInt("CurrentState", TransportationModeStreamGenerator.mCurrentState).apply();
            sharedPrefs.edit().putInt("ConfirmedActivityType", TransportationModeStreamGenerator.mConfirmedActivityType).apply();

            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_TRANSPORTATION,
                    ScheduleAndSampleManager.getCurrentTimeString(),
                    TransportationModeStreamGenerator.getConfirmedActivityString(),
                    ScheduleAndSampleManager.getTimeString(TransportationModeStreamGenerator.getSuspectTime()),
                    getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStartActivityType()),
                    getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStopActivityType()),
                    activityRecognitionDataRecord.getMostProbableActivity().toString(),
                    activityRecognitionDataRecord.getProbableActivities().toString());

            String suspectedStartActivity = getActivityNameFromType(transportationModeStreamGenerator.getSuspectedStartActivityType());
            String suspectedEndActivity = getActivityNameFromType(transportationModeStreamGenerator.getSuspectedStopActivityType());

            TransportationModeDataRecord transportationModeDataRecord =
                    new TransportationModeDataRecord(transportationModeStreamGenerator.getConfirmedActivityString(),
                            transportationModeStreamGenerator.getSuspectTime(),
                            suspectedStartActivity, suspectedEndActivity);

//            MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord, mContext, sharedPrefs);



        }catch (StreamNotFoundException e){
            e.printStackTrace();
            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " StreamNotFoundException : "+ Utils.getStackTrace(e));
        }

    }

    /**
     * this function remove old record (depending on the maximum size of the local pool)
     */
    @SuppressLint("LongLogTag")
    protected void removeOutDatedRecord() {

        for (int i=0; i<mLocalRecordPool.size(); i++) {

            ActivityRecognitionDataRecord record = mLocalRecordPool.get(i);

            //calculate time difference
            long diff =  getCurrentTimeInMillis() - mLocalRecordPool.get(i).getCreationTime();

            //remove outdated records.
            if (diff >= sKeepalive){
                mLocalRecordPool.remove(record);
                //Log.d(TAG, "[test logging]remove record " + record.getSource() + record.getID() + " logged at " + record.getTimeString() + " to " + this.getName());
                Log.e(TAG,"sKeepalive");
                i--;
            }
        }
    }

    //TODO might be useless
    public static ArrayList<ActivityRecognitionDataRecord> getLocalRecordPool(){
        return mLocalRecordPool;
    }

    public static ActivityRecognitionDataRecord getLastSavedRecord(){
        if(mLocalRecordPool==null){
            Log.e("getLastSavedRecord","null");
            return null;
        }
        if (mLocalRecordPool.size()>0)
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        else{
            Log.e("getLastSavedRecord","mLocalRecordPool.size()<0");
            return null;
        }
    }

    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }


    public void setProbableActivities(List<DetectedActivity> probableActivities) {
        sProbableActivities = probableActivities;

    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        sMostProbableActivity = mostProbableActivity;

    }

    public void setDetectedtime(long detectedtime){
        sLatestDetectionTime = detectedtime;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }
    }

    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals(STRING_DETECTED_ACTIVITY_IN_VEHICLE)) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_BICYCLE)) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_FOOT)) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_STILL)) {
            return DetectedActivity.STILL;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_UNKNOWN)) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_RUNNING)) {
            return DetectedActivity.RUNNING ;
        }else if (activityName.equals(STRING_DETECTED_ACTIVITY_WALKING)){
            return DetectedActivity.WALKING;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_TILTING)) {
            return DetectedActivity.TILTING;
        }else {
            return NO_ACTIVITY_TYPE;
        }

    }

    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return STRING_DETECTED_ACTIVITY_IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return STRING_DETECTED_ACTIVITY_ON_BICYCLE;
            case DetectedActivity.ON_FOOT:
                return STRING_DETECTED_ACTIVITY_ON_FOOT;
            case DetectedActivity.STILL:
                return STRING_DETECTED_ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return STRING_DETECTED_ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return STRING_DETECTED_ACTIVITY_WALKING;
            case DetectedActivity.UNKNOWN:
                return STRING_DETECTED_ACTIVITY_UNKNOWN;
            case DetectedActivity.TILTING:
                return STRING_DETECTED_ACTIVITY_TILTING;
            case NO_ACTIVITY_TYPE:
                return STRING_DETECTED_ACTIVITY_NA;
        }
        return "NA";
    }
}
