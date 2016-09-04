package com.example.icst;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    public void onBindViewHolder(final MyViewHolder myViewHolder, int i) {
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
                                            mData.get(myViewHolder.getAdapterPosition()).getAdjust()
                                    ), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mData.get(myViewHolder.getAdapterPosition()).setAccepted(which);
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
        if (!mData.get(myViewHolder.getAdapterPosition()).getPhoto().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile
                    (mData.get(myViewHolder.getAdapterPosition()).getPhoto());
            if (bitmap != null) {
                new ImageZipThread(bitmap, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Bitmap mBitmap = (Bitmap) msg.obj;
                        myViewHolder.mPhoto.setImageBitmap(mBitmap);
                    }
                }).start();
            }
        }
        /*
        myViewHolder.buttonControl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                //    设置Title的内容
                builder.setTitle("这么早就说支持？");
                //    设置Content来显示一个信息
                builder.setMessage("会不会给人一种内定、钦点的感觉？");
                //    设置一个PositiveButton
                builder.setPositiveButton("支持", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                //    设置一个NegativeButton
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                //    显示出该对话框
                builder.show();
                mData.get(i).change(state);
                notifyDataSetChanged();
            }
        });

        if (mData.get(i).getState(state)) myViewHolder.mLayout.setBackgroundColor(Color.argb(100,189,189,189));
        else myViewHolder.mLayout.setBackgroundColor(Color.WHITE);

        switch (state){
            case 0:
                if (mData.get(i).getState(0)) myViewHolder.buttonControl.setText("不支持");
                else myViewHolder.buttonControl.setText("支持");
                return;
            case 1:
                if (mData.get(i).getState(1)) myViewHolder.buttonControl.setText("取消");
                else myViewHolder.buttonControl.setText("签到");
                return;
            case 2:
                if (mData.get(i).getState(2)) myViewHolder.buttonControl.setText("取消");
                else myViewHolder.buttonControl.setText("通过");
                return;
            default:
                myViewHolder.buttonControl.setText("？？");
                return;
        }*/
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
