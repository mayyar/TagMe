package labelingStudy.nctu.minuku_2.view.customview;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import labelingStudy.nctu.minuku.logger.Log;
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
        return 3;
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
            holder.image = (ImageView) v.findViewById(R.id.imageView2);
            holder.text = (TextView) v.findViewById(R.id.textView);

            v.setTag(holder);
        } else{
            holder = (Holder) v.getTag();
        }
        switch(i) {
            case 0:
                holder.image.setImageResource(R.drawable.check);
                holder.text.setText("cat");
                break;
            case 1:
                holder.image.setImageResource(R.drawable.check);
                holder.text.setText("monkey");
                break;
            case 2:
                holder.image.setImageResource(R.drawable.check);
                holder.text.setText("panda");
                break;
        }
        return v;
    }
    class Holder{
        ImageView image;
        TextView text;
    }

}
