package labelingStudy.nctu.minuku_2.model;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import androidx.appcompat.widget.AlertDialogLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.model.Questionnaire.selectedPosition;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "MyAdapter";

    private Context mContext;
    private ArrayList<Post> mData;

//    public static int selectedPosition = -1;



    public MyAdapter(Context context, ArrayList<Post> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.adapter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.tvPackageName = (TextView) view.findViewById(R.id.tv_appname);
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.tvContent = (TextView) view.findViewById(R.id.tv_content);
        holder.tvTime = (TextView) view.findViewById(R.id.tv_time);
        holder.btnCheck = (Button) view.findViewById(R.id.btn_check);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {
        Post post = mData.get(position);
        holder.tvTime.setText(post.time);
        holder.tvContent.setText(post.content);
        holder.tvPackageName.setText(post.packageName);
        holder.tvTitle.setText(post.title);

        if(post.check) {
            holder.btnCheck.setText("已完成");
            holder.btnCheck.setBackgroundColor(Color.RED);
            holder.btnCheck.setTextColor(Color.WHITE);
            holder.btnCheck.setEnabled(false);
//            selectedPosition = -1;
        }
        else {
            holder.btnCheck.setText("填寫問卷");
            holder.btnCheck.setEnabled(true);

        }


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

        holder.btnCheck.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
//                selectedPosition = position;

                Intent intent = new Intent(mContext, Questionnaire.class);
//                mContext.startActivity(intent);

                intent.putExtra("PersonID", position);
                mContext.startActivity(intent);

//                notifyItemChanged(position);
//                notifyDataSetChanged();
            }

        });

    }



    @Override
    public int getItemCount() {
        Log.d(TAG, "size: " + mData.size());
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvPackageName;
        public TextView tvTime;
        public TextView tvContent;
        public Button btnCheck;
        public TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }


}
