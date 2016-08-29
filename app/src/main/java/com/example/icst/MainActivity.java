package com.example.icst;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

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
    private Menu mMenu;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("USER", "NULL");
        editor.putBoolean("ROUND", false);
        editor.apply();

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
        //如果全部group的状态都是2就显示upload按钮
        if (groupDao.count() != 0 &&
                groupDao.queryBuilder()
                        .where(GroupDao.Properties.State.notEq(2))
                        .count() == 0)
            mMenu.findItem(R.id.action_upload).setVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_upload).setVisible(false);
        menu.findItem(R.id.action_import).setVisible(sharedPreferences.getString("USER", "NULL").equals("Admin"));
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final ProgressDialog progressDialog = new ProgressDialog(this);

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_import:
                inputData();
                return true;
            case R.id.action_upload:
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("正在加载数据...");
                progressDialog.show();
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        progressDialog.dismiss();
                        final String message = (String) msg.obj;
                        final SmsManager smsManager = SmsManager.getDefault();
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(message)
                                .setPositiveButton("复制", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager clipboard = (ClipboardManager)
                                                getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("数据", message);
                                        clipboard.setPrimaryClip(clip);
                                    }
                                })
                                .setNeutralButton("短信发送", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ArrayList<String> messageParts = smsManager.divideMessage(message);
                                        smsManager.sendMultipartTextMessage("+8613265940755", null, messageParts, null, null);
                                    }
                                }).show();
                    }
                };
                new UploadThread(MainActivity.this, handler).start();
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
            Student std = new Student(i, "大帅编", true, null, 1, "机卓", "15918991022", "", "1191740498", "sheep10", "北八404", true, 3, 2, "", g);
            studentDao.insert(std);
        }
        editor.putString("USER", "Admin");
        editor.putBoolean("ROUND", false);
        editor.apply();
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
