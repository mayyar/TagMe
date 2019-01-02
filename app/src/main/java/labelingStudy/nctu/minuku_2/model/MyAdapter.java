package labelingStudy.nctu.minuku_2.model;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

class MyAdapter extends BaseAdapter {

    private static final String TAG = "MyAdapter";
    private LayoutInflater layoutInflater;

    public MyAdapter(Context context) {
        Log.d(TAG, "Get context");

        layoutInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d(TAG, "Get view");
        View v = view;
        Holder holder;
        if(v == null){
            v = layoutInflater.inflate(R.layout.adapter, null);
            holder = new Holder();
            holder.imageCategory = (ImageView) v.findViewById(R.id.iv_cat);
            holder.imageCheck = (ImageView) v.findViewById(R.id.iv_check);
            holder.textContent = (TextView) v.findViewById(R.id.tv_content);
            holder.textTime = (TextView) v.findViewById(R.id.tv_time);

            v.setTag(holder);
        } else{
            holder = (Holder) v.getTag();
        }
        switch(i) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                holder.imageCategory.setImageResource(R.drawable.email);
                holder.imageCheck.setImageResource(R.drawable.check);
                holder.textContent.setText("cat");
                holder.textTime.setText("00:00:00");
                break;

        }
        final View.OnClickListener onclick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(layoutInflater.getContext(), Questionnaire.class);
                view.getContext().startActivity(intent);
            }
        };
        holder.imageCheck.setOnClickListener(onclick);

        return v;
    }
    class Holder{
        ImageView imageCategory;
        ImageView imageCheck;
        TextView textContent;
        TextView textTime;

    }



}
