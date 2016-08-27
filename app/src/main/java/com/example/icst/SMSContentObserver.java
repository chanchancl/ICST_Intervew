package com.example.icst;

/**
 * Created by 大杨编 on 2016/8/25.
 */

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.util.List;


//用来观察系统里短消息的数据库变化  ”表“内容观察者,只要信息数据库发生变化，都会触发该ContentObserver 派生类
public class SMSContentObserver extends ContentObserver {
    private List<Student> students;

    private Context mContext  ;
    private Handler mHandler ;   //更新UI线程

    private Message msg;
    public SMSContentObserver(Context context, Handler handler, List<Student> studentList) {
        super(handler);
        mContext = context ;
        mHandler = handler ;
        students = studentList;
    }

    //当所监听的Uri发生改变时，就会回调此方法
    @Override
    public void onChange(boolean selfChange) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < students.size(); i++) {
                    //查询发件箱里的内容
                    Uri SMSUri = Uri.parse("content://sms/inbox");

                    Cursor cursor = mContext.getContentResolver().query(
                            SMSUri,
                            new String[]{"address", "body", "type", "date"},
                            "address='" + students.get(i).getPhone() +"' AND type=1",
                            null,
                            "date desc");

                    if (cursor != null && cursor.moveToFirst()) {
                        msg = new Message();
                        String msgBody = cursor.getString(cursor.getColumnIndex("body"));
                        msg.obj = msgBody.substring(0,10)+"...";
                        msg.arg1 = i;
                        mHandler.sendMessage(msg);
                        cursor.close();
                    }
                    else {
                        msg = new Message();
                        msg.obj = "（未回复）";
                        msg.arg1 = i;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }
}
