package com.liuzhao.muzik.model.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Student implements Serializable{
    @Expose
    private long id;
    @Expose
    private String name;
    @Expose
    private short age;
    @Expose
    private byte gender;
    @Expose
    private byte classes;
    @Expose
    private byte grade;
    @Expose
    private String school;
    private  String uploaded;
    private String time;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
                ", time='" + time + '\'' +
                '}';
    }
}
