package com.example.icst;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.List;

/**
 * Created by 大杨编 on 2016/7/28.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    public final static String STUDENT_ID = "com.example.icst.STUDENT";
    private List<Student> mData;
    private Context mContext;
    int state;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView mPhoto;
        public TextView mName,mRespond;
        public FrameLayout signLayout;
        public ShineButton signButton;
        public RelativeLayout mLayout;
        public MyViewHolder(View itemView){
            super(itemView);
            mPhoto=(ImageView)itemView.findViewById(R.id.Photo);
            mName=(TextView)itemView.findViewById(R.id.studentName);
            mRespond=(TextView) itemView.findViewById(R.id.studentRespond);
            signButton = (ShineButton) itemView.findViewById(R.id.signButton) ;
            signLayout = (FrameLayout) itemView.findViewById(R.id.signLayout) ;
            mLayout=(RelativeLayout)itemView.findViewById(R.id.studentLayout);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group,viewGroup,false);
        final MyViewHolder vh = new MyViewHolder(v);
        vh.signButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                mData.get(vh.getAdapterPosition()).setSigned(checked);
                mData.get(vh.getAdapterPosition()).update();
            }
        });
        return vh;
    }

    public GroupAdapter(List<Student> data, Context context) {
        mData=data;
        mContext=context;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i){
        final long id = mData.get(i).getId();
        if (state == 1){
            myViewHolder.signLayout.setVisibility(View.VISIBLE);
            myViewHolder.signButton.setChecked(mData.get(myViewHolder.getAdapterPosition()).getSigned());
        } else{
            myViewHolder.signLayout.setVisibility(View.GONE);
        }
        myViewHolder.mName.setText(mData.get(i).getName());
        myViewHolder.mRespond.setText(mData.get(i).respond);
        myViewHolder.mLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mContext, StudentActivity.class);
                intent.putExtra(STUDENT_ID,id);
                mContext.startActivity(intent);
            }
        });
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

    public void controlText(int _state){
        state=_state;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
    }

    public void setRespond(int position,String respond){
        mData.get(position).respond = respond;
        notifyItemChanged(position);
    }
}