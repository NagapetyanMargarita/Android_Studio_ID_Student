package com.example.myapplication.models

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Student
import com.example.myapplication.repository.AppRepository
import java.util.UUID

class StudentViewModel : ViewModel() {
    suspend fun newStudent(student: Student, groupID: Long) = AppRepository.get().newStudent(student, groupID)
    suspend fun editStudent(student: Student) = AppRepository.get().editStudent(student)
}