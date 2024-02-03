package com.example.myapplication.data


import androidx.room.*
import java.util.*

@Entity(tableName = "faculty",
    indices = [Index("group_name")],
    foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["faculty_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class Group(
    @PrimaryKey(autoGenerate = true) val id : Long?,
    @ColumnInfo(name = "group_name")val name : String?,
    @ColumnInfo(name = "faculty_id")val facultyID : Long?
)