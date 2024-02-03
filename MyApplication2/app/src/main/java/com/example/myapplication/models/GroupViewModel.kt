package com.example.myapplication.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Faculty
import com.example.myapplication.data.Group
import com.example.myapplication.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.*

class GroupViewModel : ViewModel() {

    var faculty: MutableLiveData<List<Group>> = MutableLiveData()
    private var facultyID: Long =-1

    init {
        AppRepository.get().faculty.observeForever{
            faculty.postValue(it)
        }
    }
    //метод для изменения идентификатора факультета
    fun setFacultyID(facultyID : Long){
        this.facultyID = facultyID
        loadGroups()
        // faculty.postValue(AppRepository.get().university.value?.find {faculty -> faculty.id==facultyID })
    }

    private fun loadGroups() {
        viewModelScope.launch {
            AppRepository.get().getFacultyGroups(facultyID)
        }
    }

    suspend fun getFaculty() : Faculty?{
        var f : Faculty?=null
        val job = viewModelScope.launch {
            f = AppRepository.get().getfaculty(facultyID)
        }
        job.join()
        return f
    }

    fun loadStudents(groupID: Long) {
        viewModelScope.launch {
            AppRepository.get().getGroupStudents(groupID)
        }
    }
}