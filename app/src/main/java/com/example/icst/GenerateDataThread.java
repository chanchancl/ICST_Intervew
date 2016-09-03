package com.example.icst;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

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
            //TODO 输出二轮名单（CSV）
        } else {
            List<Student> students = studentDao.queryBuilder()
                    .where(StudentDao.Properties.Accepted.eq(0))
                    .list();
            studentDao.deleteInTx(students);
            //TODO 输出最终名单（CSV）
        }
    }
}
