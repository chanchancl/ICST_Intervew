package com.example.icst;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by 大杨编 on 2016/9/7.
 */
public class WriteCSVThread extends Thread {

    String fileName;
    Context context;
    GroupDao groupDao;
    StudentDao studentDao;
    Handler handler;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public WriteCSVThread(String fileName, Context context, Handler handler) {
        this.fileName = fileName;
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/csv_data");
        final String filename = folder.toString() + "/" + "round2" + "csv";
        DaoSession session = DBUtil.getDaoSession(context);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();
        try {
            FileWriter fw = new FileWriter(filename);
            List<Student> students = studentDao.loadAll();
            for (Student student :
                    students) {
                fw.append("STUDENT,");
                fw.append(String.valueOf(student.getId()));
                fw.append(",");
                fw.append(student.getName());
                fw.append(",");
                fw.append(Format.Gender(student.getGender()));
                fw.append(",");
                String[] photo = student.getOriginalPhoto().split("//.");
                fw.append(photo[0]);
                fw.append(",.");
                fw.append(photo[1]);
                fw.append(",");
                fw.append(Format.College(student.getCollege()));
                fw.append(",");
                fw.append(student.getMajor());
                fw.append(",");
                fw.append(student.getPhone());
                fw.append(",");
                fw.append(student.getPhoneShort());
                fw.append(",");
                fw.append(student.getQQ());
                fw.append(",");
                fw.append(student.getWechat());
                fw.append(",");
                fw.append(student.getDorm());
                fw.append(",");
                fw.append(Format.Chinese(student.getAdjust()));
                fw.append(",");
                fw.append(Format.Department(student.getWish1()));
                fw.append(",");
                fw.append(Format.Department(student.getWish2()));
                fw.append(",");
                fw.append(student.getNote());
                fw.append(",");
                fw.append(String.valueOf(student.getGroupId()));
                fw.append("/n");
            }
            List<Group> groups = groupDao.loadAll();
            for (Group group :
                    groups) {
                fw.append("GROUP,");
                fw.append(String.valueOf(group.getId()));
                fw.append(",");
                fw.append(dateFormat.format(group.getTime()));
                fw.append(",");
                fw.append(group.getLocation());
                fw.append(",");
                fw.append(group.getHead());
                fw.append(",");
                fw.append(group.getHeadPhone());
                fw.append(",");
                fw.append(String.valueOf(group.getDepart()));
                fw.append("/n");
            }
            fw.close();
        } catch (Exception e) {
            //TODO
        }
    }

}
