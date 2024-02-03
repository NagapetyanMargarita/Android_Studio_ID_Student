package com.example.myapplication

import android.app.Application
import android.content.Context
import com.example.myapplication.repository.AppRepository

class Second352_2023Application: Application() {
    override  fun onCreate(){
        super.onCreate()
        AppRepository.newInstance()
    }
    init{
        instance = this
    }
    companion object{
        private var instance: Second352_2023Application? = null
        fun applicationContext(): Context{
            return instance!!.applicationContext
        }
    }
}