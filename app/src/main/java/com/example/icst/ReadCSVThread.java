package com.example.icst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 大杨编 on 2016/8/28.
 */
public class ReadCSVThread extends Thread {

    final static int
            S_ID = 1,
            S_NAME = 2,
            S_GENDER = 3,
            S_PHOTO = 4,
            S_COLLEGE = 5,
            S_MAJOR = 6,
            S_PHONE = 7,
            S_PHONESHORT = 8,
            S_QQ = 9,
            S_WECHAT = 10,
            S_DORM = 11,
            S_ADJUST = 12,
            S_WISH1 = 13,
            S_WISH2 = 14,
            S_NOTE = 15,
            S_GID = 16,
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
        List<Student> students = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                String[] theLine = line.split(",");
                switch (theLine[0]) {
                    case "STUDENT":
                        Long id = Long.getLong(theLine[S_ID]);
                        String name = theLine[S_NAME];
                        boolean gender = theLine[S_GENDER].equals("男");
                        String photo = theLine[S_PHOTO];
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
