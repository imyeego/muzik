package com.liuzhao.muzik.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TestDao {
    @Query("select * from test")
    List<Test> getAll();

    @Update()
    void save(Test test);
}
