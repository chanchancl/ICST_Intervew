package com.example.icst;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by 大杨编 on 2016/8/31.
 */
public class GenerateDataThread extends Thread {
    private Handler handler;
    private Message msg;
    private DaoSession session;
    private StudentDao studentDao;
    private GroupDao groupDao;
    private Context context;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private boolean sp;
    private int[] groupsNum = new int[5];

    public GenerateDataThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        session = DBUtil.getDaoSession(context);
        studentDao = session.getStudentDao();
        groupDao = session.getGroupDao();
        sp = false;
    }

    public GenerateDataThread(Context context, int[] groupsNum, Handler handler) {
        this.context = context;
        this.groupsNum = groupsNum;
        this.handler = handler;
        session = DBUtil.getDaoSession(context);
        studentDao = session.getStudentDao();
        groupDao = session.getGroupDao();
        sp = true;
    }

    @Override
    public void run() {
        if (sp) {
            //删掉没有通过的学生
            List<Student> students = studentDao.queryBuilder()
                    .where(StudentDao.Properties.Accepted.eq(0))
                    .list();
            studentDao.deleteInTx(students);

            groupDao.deleteAll();

            int id = 0;

            //创建组，groupNum是这个部门有多少组
            for (int d = 0; d < 5; d++) {
                for (int i = 1; i <= groupsNum[d]; i++) {
                    id++;
                    Group group = new Group(id, d + 1);
                    groupDao.insert(group);
                }
            }

            id = 1;

            //分配学生到不同的组
            for (int d = 1; d <= 5; d++) {
                List<Student> studentList = studentDao.queryBuilder()
                        .where(StudentDao.Properties.Accepted.eq(d))
                        .list();
                for (int i = 0; i < studentList.size(); i++) {
                    studentList.get(i).setGroupId((i % groupsNum[d - 1]) + id);
                    studentList.get(i).update();
                }
                id += groupsNum[d - 1];
            }
            writeCSV("round2");
        } else {
            List<Student> students = studentDao.queryBuilder()
                    .where(StudentDao.Properties.Accepted.le(10))
                    .list();
            studentDao.deleteInTx(students);
            groupDao.deleteAll();
            for (int i = 1; i <= 5; i++) {
                Group group = new Group(i, i);
                groupDao.insert(group);
            }
            students = studentDao.loadAll();
            for (Student student :
                    students) {
                int department = student.getAccepted() - 10;
                student.setGroupId(department);
            }
            writeCSV("New_ICSTers");
        }
    }

    public void writeCSV(String name) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/csv_data");
        folder.mkdirs();
        final String filename = folder.toString() + "/" + name + ".csv";
        try {
            FileWriter fw = new FileWriter(filename);
            BufferedWriter bfw = new BufferedWriter(fw);
            bfw.write(0xFEFF);
            List<Student> students = studentDao.queryBuilder()
                    .orderAsc(StudentDao.Properties.Accepted)
                    .list();
            for (Student student :
                    students) {
                bfw.write("STUDENT,");
                bfw.write(String.valueOf(student.getId()));
                bfw.write(",");
                bfw.write(student.getName());
                bfw.write(",");
                bfw.write(Format.Gender(student.getGender()));
                bfw.write(",");
                if (student.getPhoto().isEmpty())
                    bfw.write(",,");
                else {
                    String[] photo = student.getPhoto().split("\\.");
                    if (photo.length != 2)
                        bfw.write(",,");
                    else {
                        bfw.write(photo[0]);
                        bfw.write(",.");
                        bfw.write(photo[1]);
                        bfw.write(",");
                    }
                }
                bfw.write(Format.College(student.getCollege()));
                bfw.write(",");
                bfw.write(student.getMajor());
                bfw.write(",");
                bfw.write(student.getPhone());
                bfw.write(",");
                bfw.write(student.getPhoneShort());
                bfw.write(",");
                bfw.write(student.getQQ());
                bfw.write(",");
                bfw.write(student.getWechat());
                bfw.write(",");
                bfw.write(student.getDorm());
                bfw.write(",");
                bfw.write(Format.Chinese(student.getAdjust()));
                bfw.write(",");
                bfw.write(Format.Department(student.getWish1()));
                bfw.write(",");
                bfw.write(Format.Department(student.getWish2()));
                bfw.write(",");
                bfw.write(student.getNote());
                bfw.write(",");
                bfw.write(String.valueOf(student.getGroupId()));
                bfw.newLine();
            }
            if (sp) {
                List<Group> groups = groupDao.loadAll();
                for (Group group :
                        groups) {
                    bfw.write("GROUP,");
                    bfw.write(String.valueOf(group.getId()));
                    bfw.write(",");
                    bfw.write(dateFormat.format(group.getTime()));
                    bfw.write(",");
                    bfw.write(group.getLocation());
                    bfw.write(",");
                    bfw.write(group.getHead());
                    bfw.write(",");
                    bfw.write(group.getHeadPhone());
                    bfw.write(",");
                    bfw.write(String.valueOf(group.getDepart()));
                    bfw.newLine();
                }
            }
            bfw.flush();
            bfw.close();
            Message msg = new Message();
            msg.what = 0;
            msg.obj = filename;
            handler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }
}
