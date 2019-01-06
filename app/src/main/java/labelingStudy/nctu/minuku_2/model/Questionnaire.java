package labelingStudy.nctu.minuku_2.model;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

public class Questionnaire extends AppCompatActivity {

    TextView notificationText;
    EditText editTag, editSummary;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        notificationText = (TextView)findViewById(R.id.tv_text);
        editTag = (EditText)findViewById(R.id.ed_tag);
        editSummary = (EditText)findViewById(R.id.ed_sum);
        submit = (Button)findViewById(R.id.btn_submit);

        submit.setOnClickListener(onclick);
    }

    private Button.OnClickListener onclick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
//            Intent intent = new Intent();
//            intent.setClass(Questionnaire.this, MainActivity.class);
//            startActivity(intent);
            finish();
        }
    };
}
