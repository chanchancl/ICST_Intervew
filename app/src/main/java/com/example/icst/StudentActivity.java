package com.example.icst;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.StudentDao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
                if (ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.READ_SMS)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                    String filePath = "Student" + Long.toString(id) + ".jpg";
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File outputImage = new File(dir, filePath);
                        try {
                            if (outputImage.exists()) {
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //将File对象转换为Uri并启动照相程序

                        Intent intent = new Intent();
                        // 指定开启系统相机的Action
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        // 把文件地址转换成Uri格式
                        Uri uri = Uri.fromFile(outputImage);
                        // 设置系统相机拍摄照片完成后图片文件的存放地址
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        intent.putExtra("filePath", filePath);
                        startActivityForResult(intent, 0);
                        //拍完照startActivityForResult() 结果返回onActivityResult()函数
                    }
                } else {
                    new AlertDialog.Builder(StudentActivity.this)
                            .setTitle("缺少权限")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("需要相机和存储权限")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        student.setPhoto("Student" + Long.toString(student.getId()) + ".jpg");
        student.update();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (student.getPhoto() == null) return;
        Bitmap bitmap = BitmapFactory.decodeFile
                (getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + student.getPhoto());
        BitmapDrawable actionBarBackground = new BitmapDrawable(getResources(), bitmap);
        toolbarLayout.setBackground(actionBarBackground);
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
}
