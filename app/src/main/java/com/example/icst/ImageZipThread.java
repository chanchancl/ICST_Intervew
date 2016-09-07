package com.example.icst;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by 大杨编 on 2016/9/4.
 */
public class ImageZipThread extends Thread {
    private Context context;
    private Handler handler;
    private List<Student> students;

    public ImageZipThread(Context context, Handler handler, List<Student> students) {
        this.context = context;
        this.handler = handler;
        this.students = students;
    }

    @Override
    public void run() {
        for (int i = 0; i < students.size(); i++) {
            String filePath = context.getFilesDir() + "/thumbnail/" + students.get(i).getPhoto();
            Message message = new Message();
            message.what = 1;
            message.arg1 = i;

            try {
                if(students.get(i).getPhoto().compareTo("") != 0) {
                    message.obj = Picasso.with(context).load(new File(filePath)).get();
                } else {
                    message.obj = Picasso.with(context).load(R.drawable.ic_student).get();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.sendMessage(message);
        }
    }


}
