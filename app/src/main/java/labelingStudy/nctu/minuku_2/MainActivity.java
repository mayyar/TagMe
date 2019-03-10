/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku_2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
//import labelingStudy.nctu.minuku_2.controller.CounterActivity;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku_2.Receiver.AlarmReceiver;
import labelingStudy.nctu.minuku_2.model.MyAdapter;
import labelingStudy.nctu.minuku_2.model.Post;
import labelingStudy.nctu.minuku_2.service.BackgroundService;
import labelingStudy.nctu.minuku_2.service.JobSchedulerService;

import static labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator.mNotificaitonPackageName;
import static labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator.mNotificaitonText;
import static labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator.mNotificaitonTitle;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static boolean FirstTimeFlag = true;

    private String current_task;

    private AtomicInteger loadingProcessCount = new AtomicInteger(0);
    private ProgressDialog loadingProgressDialog;
    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;

    public static String task="PART"; //default is PART
    ArrayList viewList;
    public final int REQUEST_ID_MULTIPLE_PERMISSIONS=1;
    public static View timerview,recordview,checkpointview;

    public static TabLayout mTabs;
    public static ViewPager mViewPager;

    private SharedPreferences sharedPrefs;


    private boolean firstTimeOrNot;
    private boolean dataFirstTimeOrNot;


    private AlertDialog enableNotificationListenerAlertDialog;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private RecyclerView recyclerView;
    MyAdapter myAdapter;
    public static ArrayList<Post> data;
    public static ArrayList<Post> arrayItems = new ArrayList<>();


    //job id 用以區別任務
    int JOB_ID = 0;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.d(TAG, "Creating Main activity");

        setContentView(R.layout.activity_tag_list);


        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

//        current_task = getResources().getString(R.string.current_task);

        sharedPrefs.edit().putString("currentWork", Constants.currentWork).apply();



//        if(current_task.equals("PART")) {
//            initViewPager(timerview, recordview);
//        }else{
//            initViewPager(checkpointview, recordview);
//        }

//        SettingViewPager();

//        EventBus.getDefault().register(this);
        mManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        if (!isNotificationServiceEnabled()) {
            android.util.Log.d(TAG, "notification start!!");
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        } else {
            toggleNotificationListenerService();
        }

        int sdk_int = Build.VERSION.SDK_INT;
        if (sdk_int >= 23) {
            checkAndRequestPermissions();
        } else {
            startServiceWork();

        }
        startService(new Intent(getBaseContext(), BackgroundService.class));
        startService(new Intent(getBaseContext(), NotificationListenService.class));
//        startService(new Intent(getBaseContext(), BackgroundService.class));
//        startService(new Intent(getBaseContext(), NotificationListenService.class));
//COMMENT
//        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
//        Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);
//
//        if(firstTimeOrNot) {
//            startpermission();
//            firstTimeOrNot = false;
//            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
//        }

        try {
            //for notification
            if (getIntent().getAction().equals("open_timeline")) {
                TabLayout.Tab tab = mTabs.getTabAt(1);
                tab.select();
            }
        } catch (NullPointerException e) {
//            e.printStackTrace();
//            android.util.Log.e(TAG, "exception", e);
        }
//###########################################################################

        alarmMgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
//        long selectedTimeMiliseconds = (long) (TimeUnit.MINUTES.toMillis(5));

// Set the alarm to start at 21:32 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 32);

// setRepeating() lets you specify a precise custom interval--in this case,
// 1 day
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 2, alarmIntent);

//        創建一個JobScheduler對象
//        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
//        //設置任務延遲執行的時間，單位毫秒
////        jobBuilder.setMinimumLatency(60 * 1000);
//        jobBuilder.setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
//        //設置是否在設備重啟後，要繼續執行
//        jobBuilder.setPersisted(true);
//
//        JobScheduler mJobScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        mJobScheduler.schedule(jobBuilder.build());
//
//        if ((mJobScheduler.schedule(jobBuilder.build())) <= 0) {
//            Log.i(TAG, " something goes wrong");
//        }




        recyclerView = (RecyclerView) findViewById(R.id.rcv);

        data = new ArrayList<>();


        dataFirstTimeOrNot = sharedPrefs.getBoolean("dataFirstTimeOrNot", true);
        Log.d(TAG,"dataFirstTimeOrNot : "+ dataFirstTimeOrNot);

        if(dataFirstTimeOrNot) {
            // things that first time should be done
//            for (int i = 0; i < 10; i++) {
//                data.add(new Post("PackageName", "Title", "notification content", "00:00:00", false));
//            }
//
//            //Save data to preference
//            Gson gson = new Gson();
//            String json = gson.toJson(data);
//            sharedPrefs.edit().putString("noti", json).commit();
//
//            //Get data and turn String to ArrayList<Post>
//            String serializedObject = sharedPrefs.getString("noti", null);
//            Log.d(TAG, "Get SerializeObject: " + serializedObject);
//
//            if (serializedObject != null){
//                Gson gson1 = new Gson();
//                Type type = new TypeToken<ArrayList<Post>>(){}.getType();
//                arrayItems = gson1.fromJson(serializedObject, type);
//                Log.d(TAG, "SerializeObject: " + arrayItems);
//            }
            setMyAdapter();
//
//
            dataFirstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("dataFirstTimeOrNot", dataFirstTimeOrNot).commit();
        }else{
            String serializedObject = sharedPrefs.getString("updateNoti", null);
            Log.d(TAG, "Get SerializeObject: " + serializedObject);

            if (serializedObject != null){
                Gson gson1 = new Gson();
                Type type = new TypeToken<ArrayList<Post>>(){}.getType();
                arrayItems = gson1.fromJson(serializedObject, type);
                Log.d(TAG, "SerializeObject: " + arrayItems);
            }


            setMyAdapter();

        }

        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));


    }
//    public void forceCrash(View view) {
//        throw new RuntimeException("This is a crash");
//    }


    public void setMyAdapter(){
        myAdapter = new MyAdapter(this, arrayItems);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);
    }


    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            String message = b.getString("message");
            Log.d(TAG, "(test Receive) Main OnReceive ");
            Log.d(TAG, "(test Receive) GetDataAPIList: " + AlarmReceiver.GetAPIDataList());

            recyclerView = (RecyclerView) findViewById(R.id.rcv);
            data = new ArrayList<>();
            for(int i = 0; i < AlarmReceiver.GetAPIDataList().size(); i++){
                ArrayList<String> temp = (ArrayList<String>) AlarmReceiver.GetAPIDataList().get(i);
                data.add(new Post(temp.get(1), temp.get(3), temp.get(4), temp.get(2), false));

            }
            //Save data to preference
            Gson gson = new Gson();
            String json = gson.toJson(data);
            sharedPrefs.edit().putString("updateNoti", json).apply();
            //Get data and turn String to ArrayList<Post>
            String serializedObject = sharedPrefs.getString("updateNoti", null);
            Log.d(TAG, "Get SerializeObject: " + serializedObject);
            arrayItems = new ArrayList<>();
            if (serializedObject != null){
                Gson gson1 = new Gson();
                Type type = new TypeToken<ArrayList<Post>>(){}.getType();
                arrayItems = gson1.fromJson(serializedObject, type);
                Log.d(TAG, "SerializeObject: " + arrayItems);
            }
            setMyAdapter();
        }
    };

    public void saveData(){
        //Save data to preference
        Gson gson = new Gson();
        String json = gson.toJson(arrayItems);
        sharedPrefs.edit().putString("updateNoti", json).apply();
    }



    public static ArrayList<Post> getAdapterData(){
        return arrayItems;
    }



    private boolean isNotificationServiceEnabled(){
        android.util.Log.d(TAG, "isNotificationServiceEnabled");
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void toggleNotificationListenerService() {
        android.util.Log.d(TAG, "toggleNotificationListenerService");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListenService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListenService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("start notification");
        alertDialogBuilder.setMessage("請開啟權限");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    @Override
    public void onResume(){
        myAdapter.notifyDataSetChanged();

        saveData();

        super.onResume();
        Log.d(TAG,"onResume");

    }

    public void startpermission(){
        //Maybe useless in this project.
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    private void checkAndRequestPermissions() {

        Log.e(TAG,"checkingAndRequestingPermissions");

        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();


        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            startServiceWork();
        }

    }


    public void getDeviceid(){

        TelephonyManager mngr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if(permissionStatus==PackageManager.PERMISSION_GRANTED){
            Constants.DEVICE_ID = mngr.getDeviceId();

            Log.e(TAG,"DEVICE_ID"+Constants.DEVICE_ID+" : "+mngr.getDeviceId());

        }
    }

    public void startServiceWork(){

        getDeviceid();

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.BODY_SENSORS, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED){
                        android.util.Log.d("permission", "[permission test]all permission granted");
                        startServiceWork();
                    } else {
                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    public void SettingViewPager() {

        viewList = new ArrayList<View>();

        if (current_task.equals("PART")) {
            viewList.add(timerview);
        } else {
            viewList.add(checkpointview);
        }

        viewList.add(recordview);

        mViewPager.setAdapter(new TimerOrRecordPagerAdapter(viewList, this));

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
        //TODO date button now can show on menu when switch to recordview, but need to determine where to place the date textview(default is today's date).

        mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(!Constants.tabpos)
                    //show date on menu
                    Constants.tabpos = true;
                else
                    //hide date on menu
                    Constants.tabpos = false;

                Log.d(TAG, "initialize tab (Swipe)");


                invalidateOptionsMenu();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(MainActivity.mViewPager));
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStart() {
        myAdapter.notifyDataSetChanged();

        super.onStart();

    }



    public class TimerOrRecordPagerAdapter extends PagerAdapter {
        private List<View> mListViews;
        private Context mContext;

        public TimerOrRecordPagerAdapter(){};

        public TimerOrRecordPagerAdapter(List<View> mListViews,Context mContext) {
            this.mListViews = mListViews;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mListViews.get(position);

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


}
