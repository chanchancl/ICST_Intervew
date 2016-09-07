package com.example.icst;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

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
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private boolean sp;
    private int[] groupsNum = new int[5];

    public GenerateDataThread(Context context, Handler handler) {
        session = DBUtil.getDaoSession(context);
        studentDao = session.getStudentDao();
        groupDao = session.getGroupDao();
        this.handler = handler;
        sp = false;
    }

    public GenerateDataThread(Context context, int[] groupsNum) {
        session = DBUtil.getDaoSession(context);
        studentDao = session.getStudentDao();
        groupDao = session.getGroupDao();
        this.groupsNum = groupsNum;
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
            writeCSV();
        } else {
            List<Student> students = studentDao.queryBuilder()
                    .where(StudentDao.Properties.Accepted.ge(10))
                    .list();
            studentDao.deleteInTx(students);
            writeCSV();
        }
    }

    public void writeCSV() {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/csv_data");
        final String filename = folder.toString() + "/" + "round2" + "csv";
        try {
            FileWriter fw = new FileWriter(filename);
            List<Student> students = studentDao.queryBuilder()
                    .orderAsc(StudentDao.Properties.Accepted)
                    .list();
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
            if (sp) {
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
            }
            fw.close();
        } catch (Exception e) {
            //TODO
        }
    }
}
