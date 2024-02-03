package com.example.myapplication.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Group
import com.example.myapplication.data.Student
import com.example.myapplication.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.*

class GroupListViewModel: ViewModel() {
    //fun deleteStudent(groipID: UUID, student: Student) =
    // AppRepository.get().deleteStudent(groipID, student)
    var group: MutableLiveData<List<Student>> = MutableLiveData()
    private var groupID: Long =-1

    init {
        AppRepository.get().group.observeForever{
            group.postValue(it)
        }
    }
    //метод для изменения идентификатора факультета
    fun setGroupID(groupID : Long){
        this.groupID = groupID
        loadStudents()
        // faculty.postValue(AppRepository.get().university.value?.find {faculty -> faculty.id==facultyID })
    }

    fun loadStudents() {
        viewModelScope.launch {
            AppRepository.get().getGroupStudents(groupID)
        }
    }

    suspend fun getGroup() : Group?{
        var f : Group?=null
        val job = viewModelScope.launch {
            f = AppRepository.get().getGroup(groupID)
        }
        job.join()
        return f
    }

    suspend fun deleteStudent(student: Student) = AppRepository.get().deleteStudent(student)

//    fun loadGroup(){
//        viewModelScope.launch {
//            AppRepository.get().loadGroup()
//        }
//    }

//    fun loadStudents(groupID : Long): List<Student>{
//        var f : List<Student> = emptyList()
//        viewModelScope.launch {
//            f = AppRepository.get().getGroupStudents(groupID)
//        }
//        return f
//    }
}