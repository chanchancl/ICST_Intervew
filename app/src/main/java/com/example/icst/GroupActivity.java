package com.example.icst;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.icst.dao.DaoSession;
import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private Paint p;
    private Group group;
    private List<Student> students = new ArrayList<>();
    private TextView stateText, timeText, locationText;

    private DaoSession session;
    private GroupDao groupDao;
    private StudentDao studentDao;
    private Query<Group> groupQuery;
    private GroupAdapter groupAdapter;
    private SMSContentObserver smsContentObserver;
    public final static String EXTRA_MESSAGE = "com.example.icst.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //开始
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //组
        Intent intent = getIntent();
        long id = intent.getLongExtra(MainAdapter.EXTRA_MESSAGE, -1);
        stateText = (TextView) findViewById(R.id.detail_state);
        timeText = (TextView) findViewById(R.id.timeText);
        locationText = (TextView) findViewById(R.id.locationText);
        setTitle("第" + id + "组");
        //数据库相关
        session = DBUtil.getDaoSession(this);
        groupDao = session.getGroupDao();
        studentDao = session.getStudentDao();
        group = groupDao.load(id);
        //RecyclerView相关
        RecyclerView studentList;
        studentList = (RecyclerView) findViewById(R.id.studentRecycleView);
        studentList.setLayoutManager(new LinearLayoutManager(this));
        students = studentDao.queryBuilder()
                .where(StudentDao.Properties.GroupId.eq(id))
                .orderAsc(StudentDao.Properties.Id)
                .list();
        groupAdapter = new GroupAdapter(students, this);
        studentList.setAdapter(groupAdapter);
        stateShow(group.getState());
        //TODO SmsObserver
        smsContentObserver = new SMSContentObserver(this, handler, students);
        Uri SMS_INBOX = Uri.parse("content://sms/");
        getContentResolver().registerContentObserver(SMS_INBOX, true, smsContentObserver);
        smsContentObserver.onChange(false);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                groupAdapter.removeItem(position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    p.setARGB(255, 183, 28, 28);
                    // Draw Rect with varying right side, equal to displacement dX
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), p);
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                    c.drawBitmap(icon,
                            (float) itemView.getLeft() + 50,
                            (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                            p);

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(studentList);

        //TODO 这个最终版本要改掉的
        final ImageButton buttonPrevious = (ImageButton) findViewById(R.id.buttonPrevious);
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateShow(group.state(false));
            }
        });

        final ImageButton buttonNext = (ImageButton) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateShow(group.state(true));
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        timeText.setText(group.getTimes() + ", ");
        locationText.setText(group.getLocation());
    }

    private void stateShow(int state) {
        stateText.setText(Format.State(state));
        groupAdapter.controlText(state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);
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
            case R.id.action_edit:
                Intent intent = new Intent();
                intent.setClass(GroupActivity.this, EditGroupActivity.class);
                intent.putExtra(EXTRA_MESSAGE, group.getId());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            groupAdapter.setRespond(msg.arg1, (String) msg.obj);
        }
    };
}