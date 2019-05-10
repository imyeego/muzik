package com.liuzhao.muzik.model.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "student")
public class Student {
    @Column(name = "id", isId = true)
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private short age;
    @Column(name = "gender")
    private byte gender;
    @Column(name = "class")
    private byte classes;
    @Column(name = "grade")
    private byte grade;
    @Column(name = "school")
    private String school;

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
