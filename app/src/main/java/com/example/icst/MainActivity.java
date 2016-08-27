package com.example.icst;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import org.greenrobot.greendao.query.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private DaoSession session;
    private GroupDao groupDao;
    private StudentDao studentDao;
    private Query<Group> groupQuery;
    private File csv;
    private Thread thread;
    private Intent intent;
    private MainAdapter mainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //这个是toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //这个是圆形的悬浮按钮
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        //数据库相关
        session = DBUtil.getDaoSession(this);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();

        //这个是要读取csv文件
        Intent intent = getIntent();
        String action = intent.getAction();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这是一个进度条
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("读取数据");
                progressDialog.show();
                BufferedReader br;
                String line;
                List<Student> students = new ArrayList<>();
                if (csv != null) {
                    try {
                        int name = 1, gender = 6, photo = 5, college = 11, major = 12, phone = 7, phoneShort = 8, QQ = 9, wechat = 10, dorm = 11, adjust = 14, wish1 = 15, wish2 = 16, note = 17;
                        br = new BufferedReader(new FileReader(csv));
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

                        //读取完数据的提示
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("读取到" + students.size() + "条数据")
                                .setPositiveButton("确定", null)
                                .show();


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            csv = new File(uri.getPath());
            thread.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentMain(groupDao.count() == 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inputData() {
        Date date = new Date(116, 8, 17, 12, 00);
        for (int i = 1; i <= 7; i++) {
            Group grp = new Group(i, date, "A4-201", "杨编", "13265940755", 0);
            groupDao.insert(grp);
        }
        for (int i = 1; i <= 70; i++) {
            int g = i % 7 + 1;
            Student std = new Student(i, "大帅编", true, null ,1, "机卓", "15918991022", "", "1191740498", "sheep10", "北八404", true, 3, 2, "", g);
            studentDao.insert(std);
        }
    }

    private void setContentMain(boolean empty) {
        if (empty) {
            final Button buttonImport = (Button) findViewById(R.id.buttonImport);
            buttonImport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputData();
                    setContentMain(groupDao.count() == 0);
                }
            });
        } else {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, MessageActivity.class);
                    startActivity(intent);
                }
            });
            //隐藏“无数据”提示
            LinearLayout show = (LinearLayout) findViewById(R.id.emptyShow);
            show.setVisibility(View.GONE);
            //获取RecyclerView
            groupQuery = groupDao.queryBuilder().orderAsc(GroupDao.Properties.Id).build();
            RecyclerView groupList = (RecyclerView) findViewById(R.id.groupRecycleView);
            groupList.setLayoutManager(new LinearLayoutManager(this));
            mainAdapter = new MainAdapter(groupQuery.list(), this);
            groupList.setAdapter(mainAdapter);
        }
        return;
    }
}
