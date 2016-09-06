package com.example.icst;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private CollapsingToolbarLayout toolbarLayout;
    private TextView weChatText;
    private TextView collegeText;
    private TextView majorText;
    private TextView dormText;
    private TextView wishText;
    private TextView adjustText;
    private TextView noteText;
    private Uri imageUri; //图片路径

    public static final int TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(GroupAdapter.STUDENT_ID, -1);

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
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

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
                Uri uri = Uri.parse("tel:" + student.getPhone());
                startActivity(new Intent(Intent.ACTION_DIAL, uri));
            }
        });

        if (student.getPhoneShort() != null && student.getPhoneShort().length() >= 5) {
            shortPhoneText.setText(student.getPhoneShort());
            shortPhoneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("tel:" + student.getPhoneShort());
                    startActivity(new Intent(Intent.ACTION_DIAL, uri));
                }
            });
        } else {
            shortPhoneLayout.setVisibility(View.GONE);
        }

        if (student.getQQ() != null && student.getQQ().length() >= 5) {
            QQText.setText(student.getQQ());
            QQLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + student.getQQ());
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            });
        } else {
            QQLayout.setVisibility(View.GONE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(StudentActivity.this)
                        .setTitle("功能取消")
                        .setIcon(R.drawable.ic_warning)
                        .setMessage("英明神武的大杨编并不喜欢这个功能。")
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        if (student.getPhoto() == null || student.getPhoto().isEmpty()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageZipThread.compressImage(BitmapFactory.decodeFile(student.getPhoto()), 1080f, 180f, 1000);
                Message msg = new Message();
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BitmapDrawable actionBarBackground = new BitmapDrawable(getResources(), (Bitmap) msg.obj);
            toolbarLayout.setBackground(actionBarBackground);
        }
    };
}
