/*
 * Copyright (C) 2011-2015 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * This file is part of greenDAO Generator.
 * 
 * greenDAO Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * greenDAO Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with greenDAO Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.greenrobot.greendao.generator.gentest;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;

/**
 * Generates test entities for test project DaoTest.
 *
 * @author Markus
 */
public class TestDaoGenerator {

    public static void main(String[] args) throws Exception {
        TestDaoGenerator testDaoGenerator = new TestDaoGenerator();
        testDaoGenerator.generate();
    }

    private final Schema schema;
    private Entity student;
    private Entity group;

    public TestDaoGenerator() {
        schema = new Schema(1, "com.example.icst");
        schema.setDefaultJavaPackageDao("com.example.icst.dao");

        schema.enableKeepSectionsByDefault();
        schema.enableActiveEntitiesByDefault();

        createStudent();
        createGroup();
        createRelation();
    }

    public void generate() throws Exception {
        DaoGenerator daoGenerator = new DaoGenerator();
        daoGenerator.generateAll(schema, "C:/ICST/app/src/main/java");
    }

    protected void createStudent() {
        student = schema.addEntity("Student");
        student.addIdProperty().notNull();
        student.addStringProperty("Name").notNull();
        student.addBooleanProperty("Gender").notNull();
        student.addStringProperty("Photo");
        student.addIntProperty("College");
        student.addStringProperty("Major");
        student.addStringProperty("Phone").notNull();
        student.addStringProperty("PhoneShort");
        student.addStringProperty("QQ");
        student.addStringProperty("Wechat");
        student.addStringProperty("Dorm");
        student.addBooleanProperty("Adjust").notNull();
        student.addIntProperty("Wish1").notNull();
        student.addIntProperty("Wish2").notNull();
        student.addStringProperty("Note");

        student.addBooleanProperty("Noticed");
        student.addBooleanProperty("Deleted");
        student.addBooleanProperty("Signed");
        student.addIntProperty("Accepted");
    }

    protected void createGroup() {
        group = schema.addEntity("Group");
        group.addIdProperty().notNull();
        group.addDateProperty("Time").notNull();
        group.addStringProperty("Location").notNull();
        group.addStringProperty("Head").notNull();
        group.addStringProperty("HeadPhone").notNull();
        group.addIntProperty("State").notNull();
        group.addIntProperty("Depart");
    }

    protected void createRelation() {
        Property groupId = student.addLongProperty("groupId").notNull().getProperty();
        group.addToMany(student, groupId);
    }
}
