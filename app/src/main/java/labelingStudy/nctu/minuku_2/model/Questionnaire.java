package labelingStudy.nctu.minuku_2.model;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.JobSchedulerService;
import labelingStudy.nctu.minukucore.model.question.Question;

import static labelingStudy.nctu.minuku_2.MainActivity.arrayItems;
import static labelingStudy.nctu.minuku_2.service.JobSchedulerService.GetDataList;


public class Questionnaire extends AppCompatActivity {

    private static final String TAG = "Questionnaire";

    TextView notificationText;
    EditText editSummary, editTag;
    Button submit, add;
    ChipGroup chipGroup;
    public static boolean checkStatus = false;
    public static int selectedPosition = -1;

    private static String mNotiId = "";
    private static String mSummary = "";
    private static String mTags = "";
    private static String mTimeStamp = "";
    private static String mLocalTime = "";

    private static ArrayList<String> TagList = new ArrayList<>();
    private  ArrayList<Integer> AddPosition = new ArrayList<>();

    String url ="http://notiaboutness.nctu.me/form/submit";


    JSONObject jsonObject;
    RequestQueue mRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        notificationText = (TextView)findViewById(R.id.tv_text);
        editTag = (EditText)findViewById(R.id.ed_tag);
        editSummary = (EditText)findViewById(R.id.ed_sum);
        submit = (Button)findViewById(R.id.btn_submit);
        add = (Button)findViewById(R.id.btn_add);
        chipGroup = (ChipGroup)findViewById(R.id.chip_group);
//        tag = (TextView)findViewById(R.id.tv_tag);
        Bundle extra = getIntent().getExtras();
        if(extra !=null)
        {
            int pos = extra.getInt("PersonID");

            Post post = MainActivity.getAdapterData().get(pos);
//            post.check = true;
            notificationText.setText(post.content.toString());

            Log.d(TAG, "pos: " + pos);

        }

        add.setOnClickListener(addClick);


        submit.setOnClickListener(onclick);
    }

    private Button.OnClickListener onclick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            checkStatus = true;
            Log.d(TAG, "Status has been change");

            new AlertDialog.Builder(Questionnaire.this)
                    .setTitle("Notice")
                    .setMessage("This record will be removed from list after you press the YES button")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle extras = getIntent().getExtras();
                            if(extras !=null)
                            {
                                int pos = extras.getInt("PersonID");

                                Post post = MainActivity.getAdapterData().get(pos);
                                post.check = true;
//                                MainActivity.getAdapterData().remove(pos);
//                                arrayItems.remove(pos);

                                mNotiId = GetDataList.get(pos).get(0).toString();
                                ArrayList<String> temp = new ArrayList<>();
                                for(int l = 0 ; l < AddPosition.size(); l++){
                                    temp.add(TagList.get(AddPosition.get(l)));
                                }
                                for(int j = 0 ; j < temp.size(); j++){
                                    TagList.remove(temp.get(j));
                                    Log.d(TAG, "HttpDataHandler2 AddPosition: " +  AddPosition);
                                }

//                                mTags = TagList.toString();
                                mSummary = editSummary.getText().toString();
                                mTimeStamp = GetDataList.get(pos).get(5).toString();
                                mLocalTime = GetDataList.get(pos).get(2).toString();

                                try {
                                    jsonObject = new JSONObject();

                                    jsonObject.put("notiId", mNotiId);
                                    jsonObject.put("userId", Constants.DEVICE_ID);
                                    jsonObject.put("summary", mSummary);
                                    JSONArray arr = new JSONArray();
                                    for(int k = 0; k < TagList.size(); k++){
                                        arr.put(TagList.get(k));

                                    }
                                    jsonObject.put("tags", arr);
                                    jsonObject.put("timestamp", mTimeStamp);
                                    jsonObject.put("localtime", mLocalTime);

                                    Log.d(TAG,"HttpDataHandler2 put json object: " + jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d(TAG, "HttpDataHandler2 pos: " + pos);

                                if(isConnected())
                                    HttpDataHandler2();
                                else
                                    Toast.makeText(Questionnaire.this, "Connection error", Toast.LENGTH_LONG).show();


                                GetDataList.remove(pos);
                                finish();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
            //                selectedPosition = position;

//            notifyDatasetChanged();
//            Intent openMainActivity= new Intent(Questionnaire.this, MainActivity.class);
//            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivityIfNeeded(openMainActivity, 0);
//                notifyDataSetChanged();


        }
    };

    private Button.OnClickListener addClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(editTag.getText().toString().trim().length()!= 0){
//                Log.d(TAG, "TAG" + editTag.getText().toString()+ "123");
                Chip chip = new Chip(Questionnaire.this);
                chip.setText(editTag.getText().toString());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(closeClick);

                chipGroup.addView(chip);
                TagList.add(editTag.getText().toString());
            }

            editTag.setText(null);

        }
    };

    private Chip.OnClickListener closeClick = new Chip.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "position " + v.getId());
            AddPosition.add(v.getId());
            chipGroup.removeView(v);

        }
    };



    public void HttpDataHandler(){


        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

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
                MyData.put("notiId", mNotiId);
                MyData.put("userId", Constants.DEVICE_ID);
                MyData.put("summary", mSummary);
                MyData.put("tags", mTags);
                MyData.put("timestamp", mTimeStamp);
                MyData.put("localtime", mLocalTime);


                labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler (getParams): put Data Ready!");

                return MyData;
            }

        };


//        mRequestQueue = Volley.newRequestQueue(this);

        mRequestQueue.add(myStringRequest);
        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler: Insert success");

    }

    public void HttpDataHandler2() {

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "HttpDataHandler2 :" + response.toString());

//                        msgResponse.setText(response.toString());
//                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "HttpDataHandler2 Error0: " + error.getMessage());
                Log.e(TAG, "HttpDataHandler2 Error1: " + error.networkResponse.statusCode);
                Log.e(TAG, "HttpDataHandler2 Error2: " + jsonObject.toString());

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    // Print Error!
                    labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler2 (onErrorResponse): That didn't work " + jsonError);

                }
//                hideProgressDialog();
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json;charset=UTF-8");
                return headers;
            }
        };
        mRequestQueue.add(jsonObjReq);
        labelingStudy.nctu.minuku.logger.Log.e(TAG, "HttpDataHandler: Insert success");

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
