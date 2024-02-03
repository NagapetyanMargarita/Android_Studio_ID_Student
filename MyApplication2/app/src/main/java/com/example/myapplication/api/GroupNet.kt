package com.example.myapplication.api

data class GroupNet(
    val facultyId: Int,
    val id: Int,
    val name: String,
    val students: List<StudentNet>
)