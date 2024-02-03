package com.example.myapplication.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Faculty
import com.example.myapplication.repository.AppRepository
import kotlinx.coroutines.launch

class FacultyViewModel : ViewModel() {
    // university содержит список факультетов После получения данных из репозитория
    // класса AppRepository они устанавливаются в этот MutableLiveData объект.
    var university: MutableLiveData<List<Faculty>> = MutableLiveData()
    //слушателя изменений объекта university из репозитория с помощью метода observeForever().
    // После этого, полученный объект факультетов устанавливается в MutableLiveData объект.
    init {
        AppRepository.get().university.observeForever{
            university.postValue(it)
        }
        loadFaculty()
    }
    fun loadFaculty(){
        viewModelScope.launch {
            AppRepository.get().loadFaculty()
        }
    }

    //fun newFaculty (name: String)= AppRepository.get().newFaculty(name)
}