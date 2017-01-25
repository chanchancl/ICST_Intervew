package com.example.icst;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab; // 漂浮按键， +
    private DaoSession session;
    private GroupDao groupDao;
    private StudentDao studentDao;
    private Query<Group> groupQuery;
    private MainAdapter mainAdapter;
    private Menu mMenu;
    private SharedPreferences sharedPreferences;
    public final static String STUDENT_ID = "com.example.icst.STUDENT";
    public static int  mPosition;

    // 应用启动时调用的函数
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // R是自动生成的文件，
        // 设置当前的界面
        setContentView(R.layout.activity_main);

        mPosition = 0;

        // 打开数据库？
        sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE);
        Log.i("用户", sharedPreferences.getString("USER", "NULL"));

        // 根据 ROUND 来Log
        if (sharedPreferences.getInt("ROUND", 1) == 1)
            Log.i("ROUND", "第一轮");
        else
            Log.i("ROUND", "第二轮");

        //这个是toolbar
        //找到 Toolbar,并设置为这个活动窗口的Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //这个是圆形的悬浮按钮
        //获取按钮，并设置为不可见
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        //数据库相关
        session = DBUtil.getDaoSession(this);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();

        //这个是要读取csv文件
        final Intent intent = getIntent();
        String action = intent.getAction();

        // 判断下一步动作
        if (Intent.ACTION_VIEW.equals(action)) {
            // 检查下自己是否有某些权限，这个应该是读取 额外存储器，也就是内存卡
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                // 弹出一个窗口，设置标题，图标，消息，位置
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("读取数据")
                        .setIcon(R.drawable.ic_warning)
                        .setMessage("现有数据将被删除且无法恢复，确定继续？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            // 在上面的窗口点了 '确定'键则运行该函数
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // 创建一个对话框
                                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                // 窗口不能用 back键返回上一级
                                progressDialog.setCancelable(false);
                                // 设置风格
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                // 设置文本
                                progressDialog.setMessage("读取数据...");
                                //这是Handler 结尾处理UI的
                                Handler handler = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        if (msg.what == 1) {
                                            progressDialog.setMessage((String) msg.obj);
                                            return;
                                        }
                                        // 隐藏 proecess dialog
                                        progressDialog.dismiss();

                                        // 添加数据 "ROUND" -> msg.arg1
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("ROUND", msg.arg1);
                                        editor.apply();


                                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                        View layout = inflater.inflate(R.layout.dialog_user, (ViewGroup) findViewById(R.id.linearLayout));
                                        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) layout.findViewById(R.id.autoCompleteTextView);
                                        final String[] userList = (String[]) msg.obj;
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                                android.R.layout.simple_dropdown_item_1line, userList);
                                        //设置AutoCompleteTextView的Adapter
                                        autoCompleteTextView.setAdapter(adapter);
                                        new AlertDialog.Builder(MainActivity.this).setView(layout)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        SharedPreferences.Editor edit = sharedPreferences.edit();
                                                        edit.putString("USER", autoCompleteTextView.getText().toString());
                                                        edit.apply();
                                                        setContentMain();
                                                    }
                                                }).show();
                                    }
                                };
                                Uri uri = intent.getData();
                                // 显示刚才创建的对话框
                                progressDialog.show();
                                // 创建并启动一个线程，发送消息是，运行上边的 handler
                                ReadCSVThread thread = new ReadCSVThread(uri.getPath(), MainActivity.this, MainActivity.this, handler);
                                thread.start();
                            }
                        })
                        // 如果点了 '取消'键，则什么都不做
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                //没有权限
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("缺少权限")
                        .setIcon(R.drawable.ic_warning)
                        .setMessage("需要读取存储的权限")
                        .setPositiveButton("确定", null)
                        .show();
            }
        }
        setContentMain();
    }

    // 恢复时调用
    @Override
    public void onResume() {
        super.onResume();

        // Adapter 用来指示 分组数据如何在 mainActivity显示
        // 所以如果 mainAdapter 存在，则回复时应该通知其数据集是否
        if (mainAdapter != null)
            mainAdapter.notifyDataSetChanged();

        // 没有数据，返回
        if (groupDao == null)
            return;

        setContentMain();

        //菜单按钮
        if (mMenu == null)
            return;
        String user = sharedPreferences.getString("USER", "NULL");

        QueryBuilder qb = groupDao.queryBuilder();
        qb.where(qb.and(GroupDao.Properties.Head.eq(user), GroupDao.Properties.State.notEq(2)));

        Boolean admin = user.equals("Admin");

        mMenu.findItem(R.id.action_upload).setVisible((!admin) && groupDao.count() != 0 && qb.count() == 0);
        mMenu.findItem(R.id.action_round).setVisible(admin && groupDao.count() != 0 &&
                groupDao.queryBuilder().
                        where(GroupDao.Properties.State.notEq(2)).
                        count() == 0);
        mMenu.findItem(R.id.action_import).setVisible(admin);
        if (sharedPreferences.getInt("ROUND", 1) == 1)
            mMenu.findItem(R.id.action_round).setTitle("进入第二轮");
        else mMenu.findItem(R.id.action_round).setTitle("最终名单");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // 从资源绑定菜单布局
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // 隐藏按键
        menu.findItem(R.id.action_upload).setVisible(false);
        menu.findItem(R.id.action_import).setVisible(false);
        menu.findItem(R.id.action_round).setVisible(false);
        // 设置该类的 mMenu成员
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View layout;
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                layout = inflater.inflate(R.layout.dialog_user, (ViewGroup) findViewById(R.id.linearLayout));
                final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) layout.findViewById(R.id.autoCompleteTextView);
                List<String> users = new ArrayList<>();
                List<Group> groupQuery = groupDao.queryBuilder()
                        .orderAsc(GroupDao.Properties.Id)
                        .list();
                for (Group group :
                        groupQuery) {
                    if (!users.contains(group.getHead())) users.add(group.getHead());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, users);
                //设置AutoCompleteTextView的Adapter
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setText(sharedPreferences.getString("USER", "NULL"));
                new AlertDialog.Builder(MainActivity.this).setView(layout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor edit = sharedPreferences.edit();
                                edit.putString("USER", autoCompleteTextView.getText().toString());
                                edit.apply();
                                onResume();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            case R.id.action_import:
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData cd = cm.getPrimaryClip();
                final String str = cd.getItemAt(0).getText().toString();
                if (str.isEmpty()) return true;
                new AlertDialog.Builder(this)
                        .setTitle("读取剪贴板数据")
                        .setMessage(str)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
                                progressDialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog1.setMessage("正在加载数据...");
                                progressDialog1.show();
                                final Handler handler1 = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        progressDialog1.dismiss();
                                        if (msg.what == 2 && msg.arg1 != 0) {
                                            final List<Long> failList = (List<Long>) msg.obj;
                                            final String[] strings = new String[failList.size()];
                                            for (int i = 0; i < failList.size(); i++)
                                                strings[i] = String.valueOf(failList.get(i));
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setTitle("走错教室了...")
                                                    .setItems(strings, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                                                            intent.putExtra(STUDENT_ID, failList.get(which));
                                                            startActivity(intent);
                                                        }
                                                    }).show();
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setMessage("读取完成。")
                                                    .setPositiveButton("确定", null)
                                                    .show();
                                        }
                                    }
                                };
                                new ReadUploadThread(MainActivity.this, handler1, str).start();
                                mainAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            case R.id.action_upload:
                final ProgressDialog progressDialog2 = new ProgressDialog(this);
                progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog2.setMessage("正在加载数据...");
                progressDialog2.show();
                final Handler handler2 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        progressDialog2.dismiss();
                        final String message = (String) msg.obj;
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
                                }).show();
                    }
                };
                new UploadThread(MainActivity.this, handler2).start();
                return true;
            case R.id.action_round:
                final ProgressDialog progressDialog3 = new ProgressDialog(this);
                progressDialog3.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog3.setCancelable(false);
                progressDialog3.setMessage("正在处理...");
                final Handler handler3 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                mainAdapter.notifyDataSetChanged();
                                progressDialog3.dismiss();
                                String filePath = (String) msg.obj;
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + filePath), "text/*");
                                startActivity(intent);
                                break;
                            case 1:
                                mainAdapter.notifyDataSetChanged();
                                progressDialog3.dismiss();
                                new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitle("错误")
                                        .setMessage("写入文件时出错")
                                        .show();
                        }
                    }
                };
                if (sharedPreferences.getInt("ROUND", 1) == 1) {
                    //进入下一轮
                    final int[] total = new int[5];
                    for (int i = 1; i <= 5; i++) {
                        total[i - 1] = (int) studentDao.queryBuilder()
                                .where(StudentDao.Properties.Accepted.eq(i))
                                .count();
                    }
                    layout = inflater.inflate(R.layout.dialog_import1, (ViewGroup) findViewById(R.id.linearLayout));

                    final TextView totalText1 = (TextView) layout.findViewById(R.id.totalText1);
                    final TextView totalText2 = (TextView) layout.findViewById(R.id.totalText2);
                    final TextView totalText3 = (TextView) layout.findViewById(R.id.totalText3);
                    final TextView totalText4 = (TextView) layout.findViewById(R.id.totalText4);
                    final TextView totalText5 = (TextView) layout.findViewById(R.id.totalText5);
                    final TextView numText1 = (TextView) layout.findViewById(R.id.numText1);
                    final TextView numText2 = (TextView) layout.findViewById(R.id.numText2);
                    final TextView numText3 = (TextView) layout.findViewById(R.id.numText3);
                    final TextView numText4 = (TextView) layout.findViewById(R.id.numText4);
                    final TextView numText5 = (TextView) layout.findViewById(R.id.numText5);
                    final EditText editText1 = (EditText) layout.findViewById(R.id.editText1);
                    final EditText editText2 = (EditText) layout.findViewById(R.id.editText2);
                    final EditText editText3 = (EditText) layout.findViewById(R.id.editText3);
                    final EditText editText4 = (EditText) layout.findViewById(R.id.editText4);
                    final EditText editText5 = (EditText) layout.findViewById(R.id.editText5);

                    totalText1.setText("人力" + total[0] + "人，分");
                    totalText2.setText("项目" + total[1] + "人，分");
                    totalText3.setText("资传" + total[2] + "人，分");
                    totalText4.setText("知管" + total[3] + "人，分");
                    totalText5.setText("外联" + total[4] + "人，分");

                    editText1.addTextChangedListener(new MyTextWatcher(editText1, numText1, total[0]));
                    editText2.addTextChangedListener(new MyTextWatcher(editText2, numText2, total[1]));
                    editText3.addTextChangedListener(new MyTextWatcher(editText3, numText3, total[2]));
                    editText4.addTextChangedListener(new MyTextWatcher(editText4, numText4, total[3]));
                    editText5.addTextChangedListener(new MyTextWatcher(editText5, numText5, total[4]));

                    new AlertDialog.Builder(MainActivity.this).setView(layout)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (editText1.getText().length() == 0) return;
                                    if (editText2.getText().length() == 0) return;
                                    if (editText3.getText().length() == 0) return;
                                    if (editText4.getText().length() == 0) return;
                                    if (editText5.getText().length() == 0) return;
                                    int[] groupsNum = new int[]{
                                            Integer.decode(editText1.getText().toString()),
                                            Integer.decode(editText2.getText().toString()),
                                            Integer.decode(editText3.getText().toString()),
                                            Integer.decode(editText4.getText().toString()),
                                            Integer.decode(editText5.getText().toString()),
                                    };
                                    if (groupsNum[0] <= 1 || groupsNum[0] > total[0]) return;
                                    if (groupsNum[1] <= 1 || groupsNum[1] > total[1]) return;
                                    if (groupsNum[2] <= 1 || groupsNum[2] > total[2]) return;
                                    if (groupsNum[3] <= 1 || groupsNum[3] > total[3]) return;
                                    if (groupsNum[4] <= 1 || groupsNum[4] > total[4]) return;

                                    progressDialog3.show();
                                    SharedPreferences.Editor edit = sharedPreferences.edit();
                                    edit.putInt("ROUND", 2);
                                    edit.apply();
                                    new GenerateDataThread(MainActivity.this, groupsNum, handler3).start();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    //输出最终名单
                    progressDialog3.show();
                    new GenerateDataThread(MainActivity.this, handler3).start();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyTextWatcher implements TextWatcher {

        EditText editText;
        TextView textView;
        int total;

        public MyTextWatcher(EditText editText, TextView textView, int total) {
            this.editText = editText;
            this.textView = textView;
            this.total = total;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                textView.setText("组。");
                return;
            }
            int num = Integer.parseInt(s.toString());
            if (num <= 1 || num > total) {
                textView.setText("组。");
                return;
            }
            String note = "组，每组";
            if (total % num != 0) note += "约";
            note += String.valueOf(total / num);
            note += "人。";
            textView.setText(note);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
    }

    private void setContentMain() {
        // 看是否有数据
        if (groupDao.count() != 0) {
            // 显示 + 按钮，并设置按下后的动作
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
            String user = sharedPreferences.getString("USER", "NULL");
            if (user.equals("Admin")) {
                groupQuery = groupDao.queryBuilder()
                        .orderAsc(GroupDao.Properties.Id)
                        .build();
            } else {
                groupQuery = groupDao.queryBuilder()
                        .where(GroupDao.Properties.Head.eq(user))
                        .orderAsc(GroupDao.Properties.Id)
                        .build();
            }

            RecyclerView groupList = (RecyclerView) findViewById(R.id.groupRecycleView);
            groupList.setLayoutManager(new LinearLayoutManager(this));
            groupList.scrollToPosition(mPosition);
            mainAdapter = new MainAdapter(groupQuery.list(), this);

            groupList.setAdapter(mainAdapter);

        }
        return;
    }
}
