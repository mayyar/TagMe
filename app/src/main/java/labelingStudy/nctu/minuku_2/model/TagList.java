package labelingStudy.nctu.minuku_2.model;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

public class TagList extends AppCompatActivity {

    private static final String TAG = "TagList";

    private ListView mListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_list);

        Log.d(TAG, "Creating TagList activity");

        mListView = (ListView)findViewById(R.id.listview);
        mListView.setAdapter(new MyAdapter(this));
    }

     View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(TagList.this, Questionnaire.class);
            startActivity(intent);

        }
    };

}
