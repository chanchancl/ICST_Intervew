package com.example.icst;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 大杨编 on 2016/8/28.
 */
public class ReadCSVThread extends Thread {

    final static int
            S_ID = 1,
            S_NAME = 2,
            S_PHOTO = 3,
            S_PHOTOTYPE = 4,
            S_GENDER = 5,
            S_PHONE = 6,
            S_PHONESHORT = 7,
            S_QQ = 8,
            S_WECHAT = 9,
            S_COLLEGE = 10,
            S_MAJOR = 11,
            S_DORM = 12,
            S_ADJUST = 13,
            S_WISH1 = 14,
            S_WISH2 = 15,
            S_NOTE = 16,
            S_GID = 17,
            G_ID = 1,
            G_TIME = 2,
            G_LOCATION = 3,
            G_HEAD = 4,
            G_HEADPHONE = 5,
            G_DEPART = 6;


    String path;
    Context context;
    Activity activity;
    GroupDao groupDao;
    StudentDao studentDao;
    Bitmap mBitmap;
    Handler handler;
    List<String> users = new ArrayList<>();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    int round;
    Message msg;

    ReadCSVThread(String CSVPath, Context context, Activity activity, Handler handler) {
        path = CSVPath;
        this.activity = activity;
        this.context = context;
        this.handler = handler;
        DaoSession session = DBUtil.getDaoSession(context);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();
        round = 1;
    }

    @Override
    public void run() {
        File csv = new File(path);
        groupDao.deleteAll();
        studentDao.deleteAll();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(csv));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] theLine = line.split(",");
                if (theLine[0].contains("STUDENT")) {
                    Long id = Long.parseLong(theLine[S_ID]);
                    String name = theLine[S_NAME];
                    boolean gender = theLine[S_GENDER].equals("男");
                    String photo = theLine[S_PHOTO] + theLine[S_PHOTOTYPE];
                    int college = Format.College(theLine[S_COLLEGE]);
                    String major = theLine[S_MAJOR];
                    String phone = theLine[S_PHONE];
                    String phoneShort = theLine[S_PHONESHORT];
                    String qq = theLine[S_QQ];
                    String wechat = theLine[S_WECHAT];
                    String dorm = theLine[S_DORM];
                    boolean adjust = theLine[S_ADJUST].equals("是");
                    int wish1 = Format.Department(theLine[S_WISH1]);
                    int wish2 = Format.Department(theLine[S_WISH2]);
                    String note = theLine[S_NOTE];
                    long groupID = Long.parseLong(theLine[S_GID]);

                    msg = new Message();
                    msg.what = 1;
                    msg.obj = name;
                    handler.sendMessage(msg);

                    if (!photo.isEmpty()) {
                        //下载图片
                        try {
                            mBitmap = Picasso.with(context)
                                    .load("http://files.jsform.com/" + photo)
                                    .resize(100, 100)
                                    .centerCrop()
                                    .get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //保存图片
                        try {
                            File thumbnailDir = new File(Environment.getExternalStorageDirectory() + "/thumbnail");
                            File originalDir = new File(Environment.getExternalStorageDirectory() + "/original");
                            if (!thumbnailDir.exists())
                                if (thumbnailDir.mkdirs())
                                    Log.i("Create Dir", "Success");
                                else
                                    Log.e("Create Dir", "Fail");
                            if (!originalDir.exists())
                                if (originalDir.mkdirs())
                                    Log.i("Create Dir", "Success");
                                else
                                    Log.e("Create Dir", "Fail");
                            File thumbnail = new File(thumbnailDir.getPath(), photo);
                            File original = new File(originalDir.getPath(), photo);
                            BufferedOutputStream thumbnailBos = new BufferedOutputStream(new FileOutputStream(thumbnail));
                            BufferedOutputStream originalBos = new BufferedOutputStream(new FileOutputStream(original));
                            Picasso.with(context)
                                    .load("http://files.jsform.com/" + photo)
                                    .resize(100, 100)
                                    .centerCrop()
                                    .get()
                                    .compress(Bitmap.CompressFormat.JPEG, 80, thumbnailBos);
                            Picasso.with(context)
                                    .load("http://files.jsform.com/" + photo)
                                    .get()
                                    .compress(Bitmap.CompressFormat.JPEG, 80, originalBos);

                            thumbnailBos.flush();
                            thumbnailBos.close();
                            originalBos.flush();
                            originalBos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    studentDao.insert(new Student(
                            id,
                            name,
                            gender,
                            photo,
                            college,
                            major,
                            phone,
                            phoneShort,
                            qq,
                            wechat,
                            dorm,
                            adjust,
                            wish1,
                            wish2,
                            note,
                            groupID
                    ));
                }
                if (theLine[0].contains("GROUP")) {
                    try {
                        if (!theLine[G_DEPART].equals("0")) round = 2;
                        if (!users.contains(theLine[G_HEAD])) users.add(theLine[G_HEAD]);
                        groupDao.insert(new Group(
                                Long.parseLong(theLine[G_ID]),
                                dateFormat.parse(theLine[G_TIME]),
                                theLine[G_LOCATION],
                                theLine[G_HEAD],
                                theLine[G_HEADPHONE],
                                Format.Department(theLine[G_DEPART])
                        ));
                        msg = new Message();
                        msg.what = 1;
                        msg.obj = "正在分组...";
                        handler.sendMessage(msg);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[] message = new String[users.size()];
            message = users.toArray(message);

            msg = new Message();
            msg.what = 0;
            msg.arg1 = round;
            msg.obj = message;
            handler.sendMessage(msg);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
