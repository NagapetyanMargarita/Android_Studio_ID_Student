package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.data.Student
import com.example.myapplication.repository.AppRepository
import com.example.myapplication.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), FacultyFragment.Callbacks, GroupListFragment.Callbacks, GroupFragment.Callbacks
{
    private var miNewFaculty: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            //замена текущий фрагмент на новый FacultyFragment, который мы создаем с помощью метода newInstance() + Тэг
            .replace(R.id.mainFrame, FacultyFragment.newInstance(), FACULTY_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)//анимацию перехода между фрагментами
            .commit()// чтобы завершить транзакцию фрагментов и применить все изменения.


        //функционал кнопки назад. передача данной активности(this)
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount>0){
                    supportFragmentManager.popBackStack()//возврат на прошлую вкладку
                }
                else
                    finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        miNewFaculty = menu?.findItem(R.id.miNewFacultyGroup)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miNewFacultyGroup -> {
                val myFragment = supportFragmentManager.findFragmentByTag(GROUP_TAG)//есть ли фрагмент с тегом GROUP_TAG в менеджере фрагментов supportFragmentManager
                if (myFragment == null){
                    showNameInputDialog(0)
                }
                else
                    showNameInputDialog(1
                    )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showNameInputDialog(index:Int=-1){//создание диалогового окна
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.name_input, null)//подсоединение html
        builder.setView(dialogView)
        val nameInput = dialogView.findViewById(R.id.editTextTextPersonName) as EditText//элементы управления в макете
        val tvInfo = dialogView.findViewById(R.id.tvInfo) as TextView//элементы управления в макете
        builder.setTitle(getString(R.string.inputTitle))//устанавливаемм заголовок
        when (index){
            0 ->{

                tvInfo.text =getString(R.string.inputFaculty)
                builder.setPositiveButton(getString(R.string.commit)){_, _ ->//обработчик нажатия ок
                    val s = nameInput.text.toString()//получение значения из поля
                    if (s.isNotBlank()){
                        CoroutineScope(Dispatchers.Main). launch {
                            AppRepository.get().newFaculty(s)
                        }//вызов метода нф в репозитории
                    }
                }
            }
            1 -> {
                tvInfo.text = getString(R.string.inputGroup)
                builder.setPositiveButton(getString(R.string.commit)) { _, _ ->
                    val s = nameInput.text.toString()
                    if (s.isNotBlank()) {
                        CoroutineScope(Dispatchers.Main). launch {
                            AppRepository.get().newGroup(GroupFragment.getFaculryID, s)
                        }//вызов метода нф в репозитории
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.cancel, null)
        val alert = builder.create()
        alert.show()
    }

    override fun setTitle(_title: String) {
        title=_title
    }
    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            AppRepository.postUniversityApp()
        }
    }

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.IO).launch {
            AppRepository.postUniversityApp()
        }
    }

    override fun showStudent(groupID: Long, student: Student?) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, StudentFragment.newInstance(groupID, student), STUDENT_TAG)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }



    override fun showFaculty(id: Long) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, GroupFragment.newInstance(id), GROUP_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(null)// позволяет пользователю вернуться к предыдущему фрагменту, когда он нажимает кнопку "назад" на устройстве.
            .commit()
    }
}