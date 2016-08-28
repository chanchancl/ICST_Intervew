package com.example.icst;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.annotation.NotNull;

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

    int name = 1, gender = 6, photo = 5, college = 11, major = 12, phone = 7, phoneShort = 8, QQ = 9, wechat = 10, dorm = 11, adjust = 14, wish1 = 15, wish2 = 16, note = 17;
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
                if (theLine[0].equals("ID") || theLine[0].equals("id")) {
                    for (int i = 1; i <= theLine.length; i++) {
                        switch (theLine[i]) {
                            case "姓名":
                                name = i;
                                break;
                            case "照片(文件名称)":
                                photo = i;
                                break;
                            case "性别":
                                gender = i;
                                break;
                            case "手机":
                                phone = i;
                                break;
                            case "手机短号":
                                phoneShort = i;
                                break;
                            case "QQ":
                                QQ = i;
                                break;
                            case "微信号":
                                wechat = i;
                                break;
                            case "学院":
                                college = i;
                                break;
                            case "专业":
                                major = i;
                                break;
                            case "宿舍地址":
                                dorm = i;
                                break;
                            case "服从调剂":
                                adjust = i;
                                break;
                            case "意向部门(1)":
                                wish1 = i;
                                break;
                            case "意向部门(2)":
                                wish2 = i;
                                break;
                            case "自我介绍":
                                note = i;
                                break;
                        }
                    }
                } else {
                    Long id_ = Long.valueOf(theLine[0]);
                    String name_ = "??";
                    boolean gender_ = false;
                    String photo_ = null;
                    int college_ = 0;
                    String major_ = null;
                    String phone_ = "??";
                    String phoneShort_ = null;
                    String QQ_ = null;
                    String wechat_ = null;
                    String dorm_ = null;
                    boolean adjust_ = false;
                    int wish1_ = 0;
                    int wish2_ = 0;
                    String note_ = null;

                    name_ = theLine[name];
                    gender_ = theLine[gender].equals("男");
                    photo_ = theLine[photo];
                    college_ = Format.College(theLine[college]);
                    major_ = theLine[major];
                    phone_ = theLine[phone];
                    phoneShort_ = theLine[phoneShort];
                    QQ_ = theLine[QQ];
                    wechat_ = theLine[wechat];
                    dorm_ = theLine[dorm];
                    adjust_ = theLine[adjust].equals("是");
                    wish1_ = Format.Department(theLine[wish1]);
                    wish2_ = Format.Department(theLine[wish2]);
                    note_ = theLine[note];

                    Student student = new Student(
                            id_,
                            name_,
                            gender_,
                            photo_,
                            college_,
                            major_,
                            phone_,
                            phoneShort_,
                            QQ_,
                            wechat_,
                            dorm_,
                            adjust_,
                            wish1_,
                            wish2_,
                            note_,
                            0
                    );
                    students.add(student);
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
