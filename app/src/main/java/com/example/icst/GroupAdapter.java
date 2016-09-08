package com.example.icst;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    public final static String STUDENT_ID = "com.example.icst.STUDENT";
    private List<Student> mData;
    private Context mContext;
    private Group mGroup;
    int state;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mPhoto;
        public TextView mName, mRespond;
        public RelativeLayout mLayout;
        public Button mButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.Photo);
            mName = (TextView) itemView.findViewById(R.id.studentName);
            mRespond = (TextView) itemView.findViewById(R.id.studentRespond);
            mLayout = (RelativeLayout) itemView.findViewById(R.id.studentLayout);
            mButton = (Button) itemView.findViewById(R.id.button);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group, viewGroup, false);
        return new MyViewHolder(v);
    }

    public GroupAdapter(List<Student> data, Context context, Group group) {
        mData = data;
        mContext = context;
        mGroup = group;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int i) {
        final long id = mData.get(i).getId();
        switch (state) {
            case 0:
                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                myViewHolder.mRespond.setText(mData.get(i).respond);
                myViewHolder.mRespond.setTextColor(ContextCompat.getColor(mContext, R.color.TextGray));
                myViewHolder.mButton.setVisibility(View.GONE);
                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                break;
            case 1:
                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                myViewHolder.mRespond.setText(mData.get(i).respond);
                myViewHolder.mRespond.setTextColor(ContextCompat.getColor(mContext, R.color.TextGray));
                myViewHolder.mButton.setVisibility(View.VISIBLE);
                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                if (mData.get(myViewHolder.getAdapterPosition()).getSigned()) {
                    myViewHolder.mButton.setText("已签到");
                    myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                } else {
                    myViewHolder.mButton.setText("签到");
                    myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                }
                myViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mData.get(myViewHolder.getAdapterPosition()).changeSign()) {
                            myViewHolder.mButton.setText("已签到");
                            myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                        } else {
                            myViewHolder.mButton.setText("签到");
                            myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                        }
                    }
                });
                break;
            case 2:
                myViewHolder.mButton.setVisibility(View.VISIBLE);
                if (mData.get(myViewHolder.getAdapterPosition()).getSigned()) {
                    myViewHolder.mRespond.setText(mData.get(i).respond);
                    myViewHolder.mRespond.setTextColor(ContextCompat.getColor(mContext, R.color.TextGray));
                } else {
                    myViewHolder.mRespond.setText("未签到");
                    myViewHolder.mRespond.setTextColor(ContextCompat.getColor(mContext, R.color.Red));
                }
                if (mGroup.getDepart() == 0) {
                    if (mData.get(myViewHolder.getAdapterPosition()).getAccepted() != 0) {
                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WhitePurple));
                        myViewHolder.mButton.setText(Format.Department(mData.get(myViewHolder.getAdapterPosition()).getAccepted()));
                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                    } else {
                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                        myViewHolder.mButton.setText("通过");
                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                    }
                    myViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("选择部门")
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setItems(Format.Department(mData.get(myViewHolder.getAdapterPosition()).getWish1(),
                                            mData.get(myViewHolder.getAdapterPosition()).getWish2(),
                                            mData.get(myViewHolder.getAdapterPosition()).getAdjust()),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //TODO
                                                    if (mData.get(myViewHolder.getAdapterPosition()).getAdjust())
                                                        mData.get(myViewHolder.getAdapterPosition()).setAccepted(which);
                                                    else {
                                                        switch (which) {
                                                            case 0:
                                                                mData.get(myViewHolder.getAdapterPosition()).setAccepted(0);
                                                                break;
                                                            case 1:
                                                                mData.get(myViewHolder.getAdapterPosition()).acceptWish1();
                                                                break;
                                                            case 2:
                                                                mData.get(myViewHolder.getAdapterPosition()).acceptWish2();
                                                                break;
                                                        }
                                                    }
                                                    if (mData.get(myViewHolder.getAdapterPosition()).getAccepted() != 0) {
                                                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WhitePurple));
                                                        myViewHolder.mButton.setText(Format.Department(mData.get(myViewHolder.getAdapterPosition()).getAccepted()));
                                                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                                                    } else {
                                                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                                                        myViewHolder.mButton.setText("通过");
                                                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                                                    }
                                                    mData.get(myViewHolder.getAdapterPosition()).update();
                                                }
                                            }).show();
                        }
                    });
                } else {
                    if (mData.get(myViewHolder.getAdapterPosition()).getAccepted() > 10) {
                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WhitePurple));
                        myViewHolder.mButton.setText("已通过");
                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                    } else {
                        myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                        myViewHolder.mButton.setText("通过");
                        myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                    }
                    myViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mData.get(myViewHolder.getAdapterPosition()).changeAccept() > 10) {
                                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WhitePurple));
                                myViewHolder.mButton.setText("已通过");
                                myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.Gray));
                            } else {
                                myViewHolder.mLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                                myViewHolder.mButton.setText("通过");
                                myViewHolder.mButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
                            }
                        }
                    });
                }
                break;
        }
        myViewHolder.mName.setText(mData.get(i).getName());
        myViewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, StudentActivity.class);
                intent.putExtra(STUDENT_ID, id);
                mContext.startActivity(intent);
            }
        });
        String filePath = mContext.getFilesDir() + "/thumbnail/" + mData.get(i).getPhoto();
        Object tag = new Object();
        if(mData.get(i).getPhoto().compareTo("") != 0) {
            Picasso.with(mContext).load(new File(filePath)).tag(tag).into(myViewHolder.mPhoto);
        } else {
            Picasso.with(mContext).load(R.drawable.ic_student).tag(tag).into(myViewHolder.mPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void controlText(int _state) {
        state = _state;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
    }

    public void setRespond(int position, String respond) {
        mData.get(position).respond = respond;
        notifyItemChanged(position);
    }


}
