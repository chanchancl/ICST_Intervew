package com.example.icst;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 大杨编 on 2016/8/30.
 */
public class ReadUploadThread extends Thread {
    private Handler handler;
    private DaoSession session;
    private StudentDao studentDao;
    private GroupDao groupDao;
    private SharedPreferences sp;
    private String codes;
    private List<Long> failList = new ArrayList<>();
    private Message msg;

    public ReadUploadThread(Context context, Handler handler, String codes) {
        session = DBUtil.getDaoSession(context);
        studentDao = session.getStudentDao();
        groupDao = session.getGroupDao();
        this.handler = handler;
        this.codes = codes;
        sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
    }

    @Override
    public void run() {
        if (sp.getInt("ROUND", 1) == 1) {
            String[] departs = codes.split("\\.");
            for (int i = 0; i < departs.length; i++) {
                if (!departs[i].isEmpty()) {
                    String[] IDs = departs[i].split(",");
                    for (String ID :
                            IDs) {
                        long id = Long.parseLong(ID, 16);
                        Student student = studentDao.load(id);
                        student.setAccepted(i + 1);
                        student.update();
                        Group group = groupDao.load(studentDao.load(id).getGroupId());
                        if (group.getState() != 2) {
                            group.setState(2);
                            group.update();
                        }
                    }
                }
            }
            msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        } else {
            String[] departs = codes.split("-");
            for (int i = 0; i < departs.length; i++) {
                if (!departs[i].isEmpty()) {
                    String[] IDs = departs[i].split(",");
                    for (String ID :
                            IDs) {
                        long id = Long.decode(ID);
                        Student student = studentDao.load(id);
                        if (student.getAccepted() != i) failList.add(student.getId());
                        student.setAccepted(11 + i);
                        student.update();
                        Group group = groupDao.load(studentDao.load(id).getGroupId());
                        if (group.getState() != 2) {
                            group.setState(2);
                            group.update();
                        }
                    }
                }
            }
            msg = new Message();
            msg.what = 2;
            msg.arg1 = failList.size();
            msg.obj = failList;
            handler.sendMessage(msg);
        }
    }
}
