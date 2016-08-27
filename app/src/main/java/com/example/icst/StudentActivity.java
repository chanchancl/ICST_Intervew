package com.example.icst;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.StudentDao;

public class StudentActivity extends AppCompatActivity {

    Student student;

    private DaoSession session;
    private StudentDao studentDao;

    private LinearLayout phoneLayout;
    private TextView phoneText;
    private LinearLayout shortPhoneLayout;
    private TextView shortPhoneText;
    private LinearLayout QQLayout;
    private TextView QQText;
    private LinearLayout weChatLayout;
    private TextView weChatText;
    private TextView collegeText;
    private TextView majorText;
    private TextView dormText;
    private TextView wishText;
    private TextView adjustText;
    private TextView noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        long id = intent.getLongExtra(GroupAdapter.STUDENT_ID, -1);

        session = DBUtil.getDaoSession(this);
        studentDao = session.getStudentDao();

        student = studentDao.load(id);

        setTitle(student.getName());

        //TODO TextView 还没更改

        phoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        phoneText = (TextView) findViewById(R.id.phoneText);
        shortPhoneLayout = (LinearLayout) findViewById(R.id.shortPhoneLayout);
        shortPhoneText = (TextView) findViewById(R.id.shortPhoneText);
        QQLayout = (LinearLayout) findViewById(R.id.QQLayout);
        QQText = (TextView) findViewById(R.id.QQText);
        weChatLayout = (LinearLayout) findViewById(R.id.weChatLayout);
        weChatText = (TextView) findViewById(R.id.weChatText);
        collegeText = (TextView) findViewById(R.id.collegeText);
        majorText = (TextView) findViewById(R.id.majorText);
        dormText = (TextView) findViewById(R.id.dormText);
        wishText = (TextView) findViewById(R.id.wishText);
        adjustText = (TextView) findViewById(R.id.adjustText);
        noteText = (TextView) findViewById(R.id.noteText);


        phoneText.setText(student.getPhone());
        weChatText.setText(student.getWechat());
        collegeText.setText(Format.College(student.getCollege()));
        majorText.setText(student.getMajor());
        dormText.setText(student.getDorm());
        String wish = Format.Department(student.getWish1()) + " / " + Format.Department(student.getWish2());
        wishText.setText(wish);
        adjustText.setText(Format.Adjust(student.getAdjust()));
        noteText.setText(student.getNote());

        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("tel:"+student.getPhone());
                startActivity(new Intent(Intent.ACTION_DIAL, uri));
            }
        });

        if (student.getPhoneShort() != null) {
            shortPhoneText.setText(student.getPhoneShort());
            shortPhoneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("tel:" + student.getPhoneShort());
                    startActivity(new Intent(Intent.ACTION_DIAL, uri));
                }
            });
        }
        else{
            shortPhoneLayout.setVisibility(View.GONE);
        }

        if (student.getQQ() != null) {
            QQText.setText(student.getQQ());
            QQLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri=Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin="+student.getQQ());
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            });
        }
        else{
            QQLayout.setVisibility(View.GONE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
