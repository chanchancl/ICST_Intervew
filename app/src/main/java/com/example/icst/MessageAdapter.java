package com.example.icst;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 大杨编 on 2016/7/28.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{

    private DaoSession session;
    private GroupDao groupDao;
    private StudentDao studentDao;
    private List<Group> mData;
    private Context mContext;
    private Boolean[] checked;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView titleText,timeText,leaderText;
        public CheckBox checkBox;
        public MyViewHolder(View itemView){
            super(itemView);
            titleText=(TextView)itemView.findViewById(R.id.titleText);
            timeText=(TextView)itemView.findViewById(R.id.timeText);
            leaderText=(TextView)itemView.findViewById(R.id.leaderText);
            checkBox=(CheckBox)itemView.findViewById(R.id.checkbox);

            session = DBUtil.getDaoSession(mContext);
            groupDao = session.getGroupDao();
            studentDao = session.getStudentDao();

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message,viewGroup,false);
        final MyViewHolder vh = new MyViewHolder(v);
        vh.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked) {
                    mData.get(vh.getAdapterPosition()).fillChecked(false);
                } else if (mData.get(vh.getAdapterPosition()).numOfChecked() == 0) {
                    mData.get(vh.getAdapterPosition()).fillChecked(true);
                }
                if (mData.get(vh.getAdapterPosition()).numOfChecked() != mData.get(vh.getAdapterPosition()).getStudentList().size()) {
                    vh.titleText.setTextColor(ContextCompat.getColor(mContext, R.color.TextGray));
                } else {
                    vh.titleText.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                }
            }
        });
        return vh;
    }

    public MessageAdapter(List<Group> data, Context context) {
        mData=data;
        mContext=context;
        for (Group group:
             mData) {
            group.checkInitialization();
        }
    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder,int i) {
        final long _id = mData.get(myViewHolder.getAdapterPosition()).getId();
        myViewHolder.titleText.setText(Format.Department(mData.get(myViewHolder.getAdapterPosition()).getDepart(), "首轮") + " 第" + _id + "组");
        myViewHolder.timeText.setText(mData.get(myViewHolder.getAdapterPosition()).getTimes() + ", " + mData.get(myViewHolder.getAdapterPosition()).getLocation());
        myViewHolder.leaderText.setText(mData.get(myViewHolder.getAdapterPosition()).getHead() + ", " + mData.get(myViewHolder.getAdapterPosition()).getHeadPhone());
        int number = mData.get(myViewHolder.getAdapterPosition()).numOfChecked();
        myViewHolder.checkBox.setChecked(number != 0);
        if (mData.get(myViewHolder.getAdapterPosition()).numOfChecked() != mData.get(myViewHolder.getAdapterPosition()).getStudentList().size()) {
            myViewHolder.titleText.setTextColor(ContextCompat.getColor(mContext, R.color.TextGray));
        } else {
            myViewHolder.titleText.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                // 2. Chain together various setter methods to set the dialog characteristics
                int size = mData.get(myViewHolder.getAdapterPosition()).getStudentList().size();
                String[] list = new String[size];
                for (int ii = 0; ii < size; ii++) {
                    list[ii] = mData.get(myViewHolder.getAdapterPosition()).getStudentList().get(ii).getName();
                }

                final boolean[] checked = mData.get(myViewHolder.getAdapterPosition()).getChecked();

                builder.setTitle(Format.Department(mData.get(myViewHolder.getAdapterPosition()).getDepart(), "首轮") + " 第" + _id + "组")
                        .setMultiChoiceItems(list, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                mData.get(myViewHolder.getAdapterPosition()).check(which,isChecked);
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                notifyDataSetChanged();
                            }
                        });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<Student> getStudent(){
        List<Student> finalList = new ArrayList<>();
        for (Group group:
                mData) {
            finalList.addAll(group.getCheckedStudent());
        }
        return finalList;
    }

}
