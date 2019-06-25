package com.liuzhao.muzik.model.bean;

import com.google.gson.annotations.Expose;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "student")
public class Student implements Serializable{
    @Column(name = "id", isId = true)
    @Expose
    private long id;
    @Column(name = "name")
    @Expose
    private String name;
    @Column(name = "age")
    @Expose
    private short age;
    @Column(name = "gender")
    @Expose
    private byte gender;
    @Column(name = "class")
    @Expose
    private byte classes;
    @Column(name = "grade")
    @Expose
    private byte grade;
    @Column(name = "school")
    @Expose
    private String school;
    @Column(name = "upload")
    private  String uploaded;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public byte getClasses() {
        return classes;
    }

    public void setClasses(byte classes) {
        this.classes = classes;
    }

    public byte getGrade() {
        return grade;
    }

    public void setGrade(byte grade) {
        this.grade = grade;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", classes=" + classes +
                ", grade=" + grade +
                ", school='" + school + '\'' +
                '}';
    }
}
