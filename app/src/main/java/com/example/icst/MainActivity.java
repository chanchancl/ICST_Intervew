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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
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
    private Menu mMenu;
    private SharedPreferences sharedPreferences;
    public final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    public final static String STUDENT_ID = "com.example.icst.STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE);
        Log.i("用户", sharedPreferences.getString("USER", "NULL"));
        if (sharedPreferences.getInt("ROUND", 1) == 1) Log.i("ROUND", "第一轮");
        else Log.i("ROUND", "第二轮");

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
        final Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //这是Handler 结尾处理UI的
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
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
                                    }
                                });
                    }
                };
                Uri uri = intent.getData();
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("读取数据");
                progressDialog.show();
                ReadCSVThread thread = new ReadCSVThread(uri.getPath(), MainActivity.this, MainActivity.this, handler);
                thread.start();
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
        setContentMain(groupDao.count() == 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mainAdapter == null) return;
        mainAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_upload).setVisible(false);
        menu.findItem(R.id.action_import).setVisible(false);
        menu.findItem(R.id.action_round).setVisible(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (groupDao == null || groupDao.count() == 0) return true;
        //菜单按钮
        String user = sharedPreferences.getString("USER", "NULL");

        QueryBuilder qb = groupDao.queryBuilder();
        qb.where(qb.and(GroupDao.Properties.Head.eq(user), GroupDao.Properties.State.notEq(2)));

        QueryBuilder qbAdmin = groupDao.queryBuilder();
        qbAdmin.where(GroupDao.Properties.State.notEq(2));

        Boolean admin = user.equals("Admin");

        mMenu.findItem(R.id.action_upload).setVisible((!admin) && groupDao.count() != 0 && qb.count() == 0);
        mMenu.findItem(R.id.action_round).setVisible(admin && groupDao.count() != 0 && qbAdmin.count() == 0);
        mMenu.findItem(R.id.action_import).setVisible(admin);
        if (sharedPreferences.getInt("ROUND", 1) == 1)
            mMenu.findItem(R.id.action_round).setTitle("进入第二轮");
        else mMenu.findItem(R.id.action_round).setTitle("最终名单");
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
                if (sharedPreferences.getInt("ROUND", 1) == 1) {
                    //进入下一轮
                    final int[] total = new int[5];
                    for (int i = 1; i <= 5; i++) {
                        total[i - 1] = (int) studentDao.queryBuilder()
                                .where(StudentDao.Properties.Accepted.eq(i))
                                .count();
                    }
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View layout = inflater.inflate(R.layout.dialog_import1, (ViewGroup) findViewById(R.id.linearLayout));

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

                                    new GenerateDataThread(MainActivity.this, groupsNum).start();
                                    SharedPreferences.Editor edit = sharedPreferences.edit();
                                    edit.putInt("ROUND", 2);
                                    edit.apply();
                                    mainAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    //输出最终名单
                    //TODO Create Handler
                    new GenerateDataThread(MainActivity.this, new Handler());
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER", "Admin");
        editor.putInt("ROUND", 1);
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
            mainAdapter = new MainAdapter(groupQuery.list(), this);
            groupList.setAdapter(mainAdapter);
        }
        return;
    }
}
