package com.example.icst;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import com.example.icst.dao.DaoSession;
import org.greenrobot.greendao.DaoException;

import com.example.icst.dao.GroupDao;
import com.example.icst.dao.StudentDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.ArrayList;
import java.util.Arrays;
// KEEP INCLUDES END
/**
 * Entity mapped to table "GROUP".
 */
@Entity(active = true)
public class Group {

    @Id
    private long id;

    @NotNull
    private java.util.Date Time;

    @NotNull
    private String Location;

    @NotNull
    private String Head;

    @NotNull
    private String HeadPhone;
    private int State;
    private Integer Depart;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient GroupDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "id", referencedName = "groupId")
    })
    private List<Student> studentList;

    // KEEP FIELDS - put your custom fields here
    private boolean[] checked;
    // KEEP FIELDS END

    @Generated
    public Group() {
    }

    public Group(long id) {
        this.id = id;
    }

    @Generated
    public Group(long id, java.util.Date Time, String Location, String Head, String HeadPhone, int State, Integer Depart) {
        this.id = id;
        this.Time = Time;
        this.Location = Location;
        this.Head = Head;
        this.HeadPhone = HeadPhone;
        this.State = State;
        this.Depart = Depart;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGroupDao() : null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public java.util.Date getTime() {
        return Time;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTime(@NotNull java.util.Date Time) {
        this.Time = Time;
    }

    @NotNull
    public String getLocation() {
        return Location;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setLocation(@NotNull String Location) {
        this.Location = Location;
    }

    @NotNull
    public String getHead() {
        return Head;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setHead(@NotNull String Head) {
        this.Head = Head;
    }

    @NotNull
    public String getHeadPhone() {
        return HeadPhone;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setHeadPhone(@NotNull String HeadPhone) {
        this.HeadPhone = HeadPhone;
    }

    public int getState() {
        return State;
    }

    public void setState(int State) {
        this.State = State;
    }

    public Integer getDepart() {
        return Depart;
    }

    public void setDepart(Integer Depart) {
        this.Depart = Depart;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<Student> getStudentList() {
        if (studentList == null) {
            __throwIfDetached();
            StudentDao targetDao = daoSession.getStudentDao();
            List<Student> studentListNew = targetDao._queryGroup_StudentList(id);
            synchronized (this) {
                if(studentList == null) {
                    studentList = studentListNew;
                }
            }
        }
        return studentList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetStudentList() {
        studentList = null;
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void delete() {
        __throwIfDetached();
        myDao.delete(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void update() {
        __throwIfDetached();
        myDao.update(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void refresh() {
        __throwIfDetached();
        myDao.refresh(this);
    }

    @Generated
    private void __throwIfDetached() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
    }

    // KEEP METHODS - put your custom methods here
    public int state(boolean next){
        if (next && this.State<2) State++;
        else if (!next && this.State>0) State--;
        update();
        return State;
    }

    public Group(long id, java.util.Date Time, String Location, String Head, String HeadPhone, Integer Depart) {
        this.id = id;
        this.Time = Time;
        this.Location = Location;
        this.Head = Head;
        this.HeadPhone = HeadPhone;
        this.State = 0;
        this.Depart = Depart;
    }

    public String getTimes(){
        return Format.Time(Time);
    }

    public void checkInitialization() {
            if (checked == null) {
                checked = new boolean[getStudentList().size()];
                Arrays.fill(checked, true);
            }
    }

    public boolean[] getChecked() {
        return checked;
    }

    public void fillChecked(boolean b){
        Arrays.fill(checked,b);
    }

    public int numOfChecked() {
        int count = 0;
        for (boolean b: checked) {
            if (b) count++;
        }
        return count;
    }

    public void check(int which,boolean b){
        checked[which] = b;
    }

    public List<Student> getCheckedStudent(){
        List<Student> theList = new ArrayList<>();
        for (int i=0; i<studentList.size();i++){
            if (checked[i]) theList.add(studentList.get(i));
        }
        return theList;
    }
    // KEEP METHODS END

}
