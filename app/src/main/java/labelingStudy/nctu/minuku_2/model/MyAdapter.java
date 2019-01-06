package labelingStudy.nctu.minuku_2.model;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "MyAdapter";

    private Context mContext;
    private ArrayList<Post> mData;


    public MyAdapter(Context context, ArrayList<Post> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.adapter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.ivCategory = (ImageView) view.findViewById(R.id.iv_cat);
        holder.tvContent = (TextView) view.findViewById(R.id.tv_content);
        holder.tvTime = (TextView) view.findViewById(R.id.tv_time);
        holder.ivcheck = (ImageView) view.findViewById(R.id.iv_check);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, final int position) {
        Post post = mData.get(position);
        holder.tvTime.setText(post.time);
        holder.tvContent.setText(post.content);
        holder.ivcheck.setImageResource(R.drawable.uncheck);
        holder.ivCategory.setImageResource(R.drawable.email);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("")
                        .setMessage("Delete or not?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mData.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mData.size());
                                Toast.makeText(mContext, "Deletion occurred", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(mContext, "Undo deletion", Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

                return true;
            }
        });

        holder.ivcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Questionnaire.class);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "size: " + mData.size());
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivCategory;
        public TextView tvTime;
        public TextView tvContent;
        public ImageView ivcheck;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
