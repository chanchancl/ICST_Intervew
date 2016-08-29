package com.example.icst;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private ArrayList<ForegroundColorSpan> mColorSpans = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private EditText messageText;
    private List<String> key = new ArrayList<String>(){{
        add("#姓名#");
        add("#时间#");
        add("#地点#");
        add("#部门#");
        add("#联系人#");
        add("#联系电话#");
    }};
    private Menu mMenu;
    private SmsManager sManage;
    private DaoSession session;
    private GroupDao groupDao;
    private Query<Group> groupQuery;
    private static final int SENDING = 0;
    private static final int DONE=1;
    private List<Student> studentList;
    private ProgressDialog progressDialog;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        messageText = (EditText) findViewById(R.id.editSMS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        messageText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mMenu.findItem(R.id.action_done).setVisible(messageText.hasFocus());
                mMenu.findItem(R.id.action_send).setVisible(!messageText.hasFocus());
            }
        });

        RecyclerView chooseRec = (RecyclerView) findViewById(R.id.studentRecycleView);

        //数据库相关
        session = DBUtil.getDaoSession(this);
        groupDao = session.getGroupDao();
        groupQuery = groupDao.queryBuilder().orderAsc(GroupDao.Properties.Id).build();

        messageAdapter = new MessageAdapter(groupQuery.list(),this);
        chooseRec.setAdapter(messageAdapter);
        chooseRec.setLayoutManager(new LinearLayoutManager(this));

        messageText.addTextChangedListener(textWatcher);
        messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    int selectionStart = messageText.getSelectionStart();

                    int lastPos = 0;
                    int size = key.size();
                    for (int i = 0; i < size; i++) {
                        String topic = key.get(i);
                        lastPos = messageText.getText().toString().indexOf(topic, lastPos);

                        if (lastPos != -1) {
                            if (selectionStart >= lastPos && selectionStart <= (lastPos + topic.length())) {
                                //在这position 区间就移动光标
                                messageText.setSelection(lastPos + topic.length());
                            }
                        }
                        lastPos = lastPos + topic.length();
                    }
            }
        });
        messageText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("MainActivity", "onKey");
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {

                    int selectionStart = messageText.getSelectionStart();
                    int selectionEnd = messageText.getSelectionEnd();
                    //如果光标起始和结束在同一位置,说明是选中效果,直接返回 false 交给系统执行删除动作
                    if (selectionStart != selectionEnd) {
                        return false;
                    }

                    Editable editable = messageText.getText();
                    String content = editable.toString();
                    int lastPos = 0;
                    int size = key.size();
                    //遍历判断光标的位置
                    for (int i = 0; i < size; i++) {
                        String topic = key.get(i);
                        lastPos = content.indexOf(topic, lastPos);
                        if (lastPos != -1) {
                            if (selectionStart != 0 && selectionStart >= lastPos && selectionStart <= (lastPos + topic.length())) {
                                //选中话题
                                messageText.setSelection(lastPos, lastPos + topic.length());
                                return false;
                            }
                        }
                        lastPos += topic.length();
                    }
                }
                return false;
            }
        });

        setTextColor(messageText.getText());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        menu.findItem(R.id.action_done).setVisible(false);
        mMenu=menu;
        return true;
    }
/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_done).setVisible(messageText.hasFocus());
        menu.findItem(R.id.action_settings).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (id) {
            case R.id.action_done:
                messageText.clearFocus();
                imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);
                return true;
            case R.id.action_send:
                messageText.clearFocus();
                imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);

                // 检查是否拥有发短信的权限
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {

                    studentList = messageAdapter.getStudent();
                    new AlertDialog.Builder(MessageActivity.this)
                            .setTitle("即将发送" + studentList.size() + "条信息")
                            .setMessage("自动发送程序开始后无法中断，是否继续？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    send_messages();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
                else{
                    new AlertDialog.Builder(MessageActivity.this)
                            .setTitle("没有发短信的权限")
                            .setMessage("手动再见")
                            .setPositiveButton("确定",null)
                            .show();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void send_messages() {
        progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("正在发送");
        progressDialog.setMessage("加载到"+studentList.size()+"条数据");
        progressDialog.setMax(studentList.size());
        progressDialog.setProgress(0);
        progressDialog.setProgressNumberFormat("已处理%1d条信息，共计%2d条");
        progressDialog.show();

        final Handler handler =  new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SENDING:
                        String name = (String) msg.obj;
                        progressDialog.incrementProgressBy(1);
                        progressDialog.setMessage("正在发送" + name);
                        break;
                    case DONE:
                        progressDialog.dismiss();
                        new AlertDialog.Builder(MessageActivity.this)
                            .setMessage("发送完成。")
                            .setPositiveButton("确定",null)
                            .show();
                }
            }
        };

        new Thread(new Runnable() {
            public void run() {
                for (Student student :
                        studentList) {
                    Message msg = new Message();
                    msg.what = SENDING;
                    msg.obj = student.getName();
                    handler.sendMessage(msg);
                    if (PhoneNumberUtils.isGlobalPhoneNumber(student.getPhone())) {
                        sManage = SmsManager.getDefault();
                        String message = messageText.getText().toString();
                        Group group = groupDao.load(student.getGroupId());
                        message = message.replace(key.get(0), student.getName());
                        message = message.replace(key.get(1), group.getTimes());
                        message = message.replace(key.get(2), group.getLocation());
                        message = message.replace(key.get(3), "部门");
                        message = message.replace(key.get(4), group.getHead());
                        message = message.replace(key.get(5), group.getHeadPhone());

                        //因为一条短信有字数限制，因此要将长短信拆分
                        ArrayList<String> messageParts = sManage.divideMessage(message);
                        for (String text : messageParts) {
                            sManage.sendTextMessage(student.getPhone(), null, text, null, null);
                        }
                    }
                }
                Message msg = new Message();
                msg.what = DONE;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            setTextColor(s);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
    };

    public void setTextColor(CharSequence s){
        if (s.length()<=0) return;
        //1,查找话题
        String content = s.toString();

        //2,为查找出的变色
        //首先要为editable,去除之前设置的colorSpan
        Editable editable = messageText.getText();
            for (int i = 0; i < mColorSpans.size(); i++) {
                editable.removeSpan(mColorSpans.get(i));
            }
            mColorSpans.clear();
        //为editable,中的话题加入colorSpan
        int findPos = 0;
        int size = key.size();
        for (int i = 0; i < size; i++) {//遍历话题
            String topic = key.get(i);
            findPos = content.indexOf(topic, findPos);
            if (findPos != -1) {
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this,R.color.colorPrimaryLight));
                editable.setSpan(colorSpan, findPos, findPos = findPos + topic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mColorSpans.add(colorSpan);
            }
        }

    }


}