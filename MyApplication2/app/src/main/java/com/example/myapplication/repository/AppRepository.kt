package com.example.myapplication.repository

import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.myapplication.Second352_2023Application
import com.example.myapplication.api.*
import com.example.myapplication.data.Faculty
import com.example.myapplication.data.Group
import com.example.myapplication.data.Student
import com.example.myapplication.database.UniversityDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppRepository private constructor() {
    var university: MutableLiveData<List<Faculty>> = MutableLiveData()
    var faculty: MutableLiveData<List<Group>> = MutableLiveData()
    var group: MutableLiveData<List<Student>> = MutableLiveData()

    companion object{
        private var INSTANCE: AppRepository? = null

        fun newInstance(){//создание экземпляра класса,если тот не создан
            if (INSTANCE == null){
                INSTANCE = AppRepository()
                INSTANCE!!.getAPI()//инициализация сервера
                INSTANCE!!.getServer()//получение данных
            }
        }
        fun get(): AppRepository{
            return INSTANCE?: throw IllegalAccessException("Репозиторий не инициализирован")
        }
        suspend fun postUniversityApp() {//отправка данных на сервер
            if (INSTANCE?.myServerAPI != null) {
                val job = CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.myServerAPI!!.postUniversity(INSTANCE?.PostInfo()!!)
                }
                job.join()
            }
        }
    }

    val db = Room.databaseBuilder(
        Second352_2023Application.applicationContext(),
        UniversityDatabase::class.java, "un.db"
    ).build()

    val universityDao = db.getDao()

    fun PostInfo(): UniversityNet {
        val u = university.value
        val universityNet = ArrayList<FacultyNet>()
        if (u != null) {
            for (faculty in u) {
                val groups = universityDao.loadFacultyGroup(faculty.id!!)//получение групп
                val groupList = ArrayList<GroupNet>()
                for (g in groups) {
                    val students = universityDao.loadGroupStudents(g.id!!)
                    val studentList = ArrayList<StudentNet>()
                    for (s in students) {
                        val studentNet = StudentNet(
                            s.birthDate!!.toInt(),
                            s.firstName!!,
                            s.groupID!!.toInt(),
                            s.id!!.toInt(),
                            s.lastName!!,
                            s.middleName!!,
                            s.phone!!
                        )
                        studentList.add(studentNet)
                    }
                    val groupNet = GroupNet(
                        faculty.id.toInt(),
                        g.id.toInt(),
                        g.name!!,
                        studentList
                    )
                    groupList.add(groupNet)
                }
                val facultyNet = FacultyNet(
                    groupList,
                    faculty.id.toInt(),
                    faculty.name!!
                )
                universityNet.add(facultyNet)
            }
        }
        val uN = UniversityNet(universityNet)
        return uN
    }

    suspend fun newFaculty (name: String){
        val faculty =Faculty(id=null,name=name)
        withContext(Dispatchers.IO){
            universityDao.insertNewFaculty(faculty)
            university.postValue(universityDao.loadUniversity())
        }
    }
    suspend fun deleteFaculty (faculty: Faculty ){
        withContext(Dispatchers.IO){
            universityDao.deleteFaculty(faculty)//удаление факультета
            university.postValue(universityDao.loadUniversity())//вывод списка факультетов
        }
    }
    suspend fun updateFaculty (faculty: Faculty){
        withContext(Dispatchers.IO){
            universityDao.updateFaculty(faculty)//добавление факультета
            university.postValue(universityDao.loadUniversity())//вывод списка факультетов
        }
    }
    suspend fun loadFaculty (){
        withContext(Dispatchers.IO){
            university.postValue(universityDao.loadUniversity())
        }
    }
    suspend fun getFacultyGroups (facultyID: Long){
        withContext(Dispatchers.IO){
            faculty.postValue(universityDao.loadFacultyGroup(facultyID))
        }
    }
    suspend fun getfaculty(facultyID: Long): Faculty?{
        var f : Faculty?=null
        val job= CoroutineScope(Dispatchers.IO).launch {
            f=universityDao.getFaculty(facultyID)
        }
        job.join()
        return f
    }

    suspend fun getGroupStudents(groupID: Long) /*:List<Student> */{
        withContext(Dispatchers.IO){
            group.postValue(universityDao.loadGroupStudents(groupID))
        }
    }

    suspend fun newGroup(facultyID: Long, name: String) {
        val group = Group(id=null,name=name,facultyID=facultyID)
        withContext(Dispatchers.IO) {
            universityDao.insertNewGroup(group)
            getFacultyGroups(facultyID)
        }
    }


    suspend fun deleteGroup(facultyID: Long,group: Group){
        withContext(Dispatchers.IO){
            universityDao.deleteGroup(group)//удаление факультета
            getFacultyGroups(facultyID)//вывод списка факультетов
        }
    }
    suspend fun updateGroup(group: Group, facultyID: Long){
        withContext(Dispatchers.IO){
            universityDao.updateGroup(group)
            getFacultyGroups(facultyID)
        }
    }
    suspend fun getGroup(groupID: Long): Group? {
        var f : Group?=null
        val job= CoroutineScope(Dispatchers.IO).launch {
            f=universityDao.getGroup(groupID)
        }
        job.join()
        return f
    }
    suspend fun newStudent(student: Student, groupID: Long) {
        withContext(Dispatchers.IO) {
            universityDao.insertNewStudent(student)
            getGroupStudents(student.groupID!!)
        }
    }
    suspend fun editStudent(student: Student) {
        withContext(Dispatchers.IO) {
            universityDao.updateStudent(student)
            getGroupStudents(student.groupID!!)
        }
    }

    suspend fun deleteStudent(student: Student) {
        withContext(Dispatchers.IO) {
            universityDao.deleteStudent(student)
            getGroupStudents(student.groupID!!)
        }
    }
    private var myServerAPI: ServerAPI? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun getAPI() {
        val url = "http://10.0.2.2:8080/"
        Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())//json конвектор (дата класс в джсон и наоборот)
            .build().apply {
                myServerAPI = create(ServerAPI::class.java)//инициализация для использ функций в серсвере
            }
    }

    fun getServer() {
        if (myServerAPI != null) {
            CoroutineScope(Dispatchers.Main).launch {
                GetInfo()
            }
        }
    }

    private suspend fun GetInfo() {
        if (myServerAPI != null) {
            val job = CoroutineScope(Dispatchers.IO).launch {
                val r = myServerAPI!!.getUniversity()//отправка гет запроса
                if (r.isSuccessful) {//если удачно
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        universityDao.deleteAllFaculty()//удаляем все записи
                    }
                    job.join()

                    val facultyList = r.body()?.faculties//тело ответа сервера
                    if (facultyList != null) {
                        for (f in facultyList) {
                            val faculty = Faculty(f.id.toLong(), f.name)
                            universityDao.insertNewFaculty(faculty)//добавление факультета
                            for (g in f.groups) {
                                val group = Group(g.id.toLong(), g.name, faculty.id)
                                universityDao.insertNewGroup(group)//добавление группы
                                for (s in g.students) {
                                    val student = Student(
                                        s.id.toLong(),
                                        s.firstName,
                                        s.lastName,
                                        s.middleName,
                                        s.phone,
                                        s.birthDate.toLong(),
                                        group.id
                                    )
                                    universityDao.insertNewStudent(student)//добавление студента
                                }
                            }
                        }
                    }
                }
            }
            job.join()
            loadFaculty()
        }
    }

}