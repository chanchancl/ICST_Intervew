package com.example.icst;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by 大杨编 on 2016/8/28.
 */
public class ReadCSVThread extends Thread {

    Context mContext;

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

    BufferedReader br;

    ReadCSVThread(String CSVPath) {
        File csv = new File(CSVPath);
        try {
            br = new BufferedReader(new FileReader(csv));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line;
        Bitmap mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_delete);
        List<Student> students = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                //把这一行切开
                String[] theLine = line.split(",");
                switch (theLine[0]) {
                    case "STUDENT":
                        //TODO 运行到这里就闪退，没有看到提示？
                        Long id = Long.getLong(theLine[S_ID]);
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
                        long groupID = Long.getLong(theLine[S_GID]);
                        //下载图片
                        try {
                            URL url = new URL("http://files.jsform.com/" + photo);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(6000);
                            conn.setRequestMethod("GET");
                            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                mBitmap = BitmapFactory.decodeStream(conn.getInputStream());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //保存图片
                        try {
                            photo = Long.toString(id);
                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), photo);
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                            mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                            bos.flush();
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//TODO
                        Student student = new Student(
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
                        );
                        students.add(student);
                        break;
                    case "GROUP":
                        //TODO
                        long g_id = Long.getLong(theLine[G_ID]);
                        Date time = new Date(1970, 1, 1, 0, 0);
                        try {
                            time = new SimpleDateFormat("yyyy.MM.dd-HH:mm", Locale.CHINA).parse(theLine[G_TIME]);
                        } catch (ParseException e) {
                            Log.d("读取时间时出现错误", theLine[G_TIME]);
                        }
                        String location = theLine[G_LOCATION];
                        String head = theLine[G_HEAD];
                        String headPhone = theLine[G_HEADPHONE];
                        int depart = Integer.getInteger(theLine[G_DEPART]);

                        Group group = new Group(g_id, time, location, head, headPhone, depart);
                        groups.add(group);
                        break;
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //TODO 通知UI界面

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
