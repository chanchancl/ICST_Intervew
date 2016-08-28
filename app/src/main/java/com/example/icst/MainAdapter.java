package com.example.icst;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 大杨编 on 2016/7/28.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder>{

    public final static String EXTRA_MESSAGE = "com.example.icst.MESSAGE";
    private List<Group> mData;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView mTitle,mTime,mLocation,mState;
        public CardView mCardView;
        public MyViewHolder(View itemView){
            super(itemView);
            mTitle=(TextView)itemView.findViewById(R.id.groupTitle);
            mTime=(TextView)itemView.findViewById(R.id.groupTime);
            mLocation=(TextView)itemView.findViewById(R.id.groupLocation);
            mState=(TextView)itemView.findViewById(R.id.groupState);
            mCardView=(CardView)itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_main,viewGroup,false);
        return new MyViewHolder(v);
    }

    public MainAdapter(List<Group> data, Context context) {
        mData=data;
        mContext=context;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        final long id = mData.get(i).getId();
        myViewHolder.mTitle.setText("第"+ id +"组");
        myViewHolder.mTime.setText(mData.get(i).getTimes());
        myViewHolder.mLocation.setText(mData.get(i).getLocation());
        myViewHolder.mState.setText(Format.State(mData.get(i).getState()));
        myViewHolder.mCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mContext,GroupActivity.class);
                intent.putExtra(EXTRA_MESSAGE, id);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
