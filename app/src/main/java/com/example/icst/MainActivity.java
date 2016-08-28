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

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("读取数据");
            progressDialog.show();
            ReadCSVThread thread = new ReadCSVThread(uri.getPath());
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
