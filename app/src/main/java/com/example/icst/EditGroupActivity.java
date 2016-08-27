package com.example.icst;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.zip.Inflater;

public class EditGroupActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient client;
    private java.util.Calendar calendar;
    private TextView dateText,timeText,locationText,connectText;
    private DaoSession session;
    private GroupDao groupDao;
    private StudentDao studentDao;
    private Group group;
    private String location,head,headPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        long id = intent.getLongExtra(GroupActivity.EXTRA_MESSAGE, -1);
        setTitle("第" + id + "组");

        session = DBUtil.getDaoSession(this);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();
        group = groupDao.load(id);

        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        locationText = (TextView) findViewById(R.id.locationText);
        connectText = (TextView) findViewById(R.id.connectText);

        calendar = new GregorianCalendar();
        calendar.setTime(group.getTime());
        location = group.getLocation();
        head = group.getHead();
        headPhone = group.getHeadPhone();

        show();

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View textView) {
                // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                new DatePickerDialog(EditGroupActivity.this,
                        // 绑定监听器
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                calendar.set(year,monthOfYear,dayOfMonth);
                                show();
                            }
                        }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个TimePickerDialog实例，并把它显示出来
                new TimePickerDialog(EditGroupActivity.this,
                        // 绑定监听器
                        new TimePickerDialog.OnTimeSetListener() {
                            int year=calendar.get(Calendar.YEAR);
                            int month=calendar.get(Calendar.MONTH);
                            int date=calendar.get(Calendar.DAY_OF_MONTH);

                            @Override
                            public void onTimeSet(TimePicker view,int hourOfDay, int minute) {
                                calendar.set(year,month,date,hourOfDay,minute);
                                show();
                            }
                        }
                        // 设置初始时间
                        , calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        // true表示采用24小时制
                        true).show();
            }
        });

        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(EditGroupActivity.this);
                View layout = inflater.inflate(R.layout.dialog_location,(ViewGroup) findViewById(R.id.linearLayout));
                final EditText editText = (EditText) layout.findViewById(R.id.editText);
                editText.setText(locationText.getText());

                new AlertDialog.Builder(EditGroupActivity.this).setView(layout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                location = editText.getText().toString();
                                show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        connectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(EditGroupActivity.this);
                View layout = inflater.inflate(R.layout.dialog_leader,(ViewGroup) findViewById(R.id.linearLayout));
                final EditText editLeader = (EditText) layout.findViewById(R.id.editLeader);
                editLeader.setText("杨泽霖");
                final EditText editLeaderPhone = (EditText) layout.findViewById(R.id.editLeaderPhone);
                editLeaderPhone.setText("13265940755");

                new AlertDialog.Builder(EditGroupActivity.this).setView(layout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                head = editLeader.getText().toString();
                                headPhone = editLeaderPhone.getText().toString();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void show(){
        calendar.setTime(group.getTime());
        dateText.setText(String.format("%1$tY年%1$tb%1$td日", calendar));
        timeText.setText(String.format("%1$tH:%1$tM", calendar));
        locationText.setText(location);
        connectText.setText(head + "  " +headPhone);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            this.finish();  //finish当前activity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "EditGroup Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.icst/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "EditGroup Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.icst/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_group, menu);
        Log.i("创建Menu","成功");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_done:
                this.finish();  //finish当前activity
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case android.R.id.home:
                new AlertDialog.Builder(EditGroupActivity.this)
                        .setMessage("是否保存更改？")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO 保存更改
                                EditGroupActivity.this.finish();  //finish当前activity
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        })
                        .setNegativeButton("不保存",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditGroupActivity.this.finish();  //finish当前activity
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        })
                        .setNeutralButton("取消",null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
