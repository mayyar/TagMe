package labelingStudy.nctu.minuku_2.model;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minukucore.model.question.Question;


public class Questionnaire extends AppCompatActivity {

    private static final String TAG = "Questionnaire";

    TextView notificationText;
    EditText editSummary, editTag;
    Button submit, add;
    ChipGroup chipGroup;
    public static boolean checkStatus = false;
    public static int selectedPosition = -1;





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


        add.setOnClickListener(addClick);


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

                Post post = MainActivity.getAdapterData().get(selectedPosition);
                post.check = true;


            }
//            notifyDatasetChanged();
//            Intent openMainActivity= new Intent(Questionnaire.this, MainActivity.class);
//            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivityIfNeeded(openMainActivity, 0);
            finish();
            Questionnaire.super.onResume();

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
            }

            editTag.setText(null);

        }
    };

    private Chip.OnClickListener closeClick = new Chip.OnClickListener() {
        @Override
        public void onClick(View v) {

            chipGroup.removeView(v);
        }
    };





}
