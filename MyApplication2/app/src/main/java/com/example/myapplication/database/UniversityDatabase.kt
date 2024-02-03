package com.example.myapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.dao.UniversityDAO
import com.example.myapplication.data.Faculty
import com.example.myapplication.data.Group
import com.example.myapplication.data.Student

@Database(
    version = 1,
    entities = [
        Faculty::class,
        Group::class,
        Student::class
    ]
)
abstract class UniversityDatabase : RoomDatabase(){
    abstract fun getDao(): UniversityDAO
}