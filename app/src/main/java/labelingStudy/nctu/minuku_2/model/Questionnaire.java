package labelingStudy.nctu.minuku_2.model;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;


public class Questionnaire extends AppCompatActivity {

    private static final String TAG = "Questionnaire";

    TextView notificationText;
    EditText editSummary;
    Button submit;
    public static boolean checkStatus = false;
    public static int selectedPosition = -1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        notificationText = (TextView)findViewById(R.id.tv_text);
//        editTag = (EditText)findViewById(R.id.ed_tag);
        editSummary = (EditText)findViewById(R.id.ed_sum);
        submit = (Button)findViewById(R.id.btn_submit);
//        tag = (TextView)findViewById(R.id.tv_tag);




        submit.setOnClickListener(onclick);
    }

    private Button.OnClickListener onclick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            checkStatus = true;
            Log.d(TAG, "Status has been change");

            //                selectedPosition = position;
            Bundle extras = getIntent().getExtras();
            if(extras !=null)
            {
                selectedPosition = extras.getInt("PersonID");
            }
//            notifyDatasetChanged();
            finish();

        }
    };





}
