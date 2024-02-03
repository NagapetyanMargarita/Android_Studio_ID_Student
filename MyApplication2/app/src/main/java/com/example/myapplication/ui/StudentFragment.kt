package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.myapplication.R
import com.example.myapplication.data.Student
import com.example.myapplication.databinding.FragmentFacultyBinding
import com.example.myapplication.databinding.FragmentStudentBinding
import com.example.myapplication.models.GroupViewModel
import com.example.myapplication.models.StudentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

const val STUDENT_TAG="StudentFragment"

class StudentFragment : Fragment() {
    private var _binding: FragmentStudentBinding? = null
    val binding
        get() = _binding!!

    companion object {
        var groupID: Long = -1
        var student: Student?=null
        fun newInstance(groupID: Long, student: Student?): StudentFragment{
            this.student=student
            this.groupID= groupID
            return StudentFragment()
        }
    }

    private lateinit var viewModel: StudentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentStudentBinding.inflate(inflater, container,false)
        return binding.root
    }
    val backPressedCallback=object: OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            showCommitDialog()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

//    private val selectedDate = GregorianCalendar()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider (this).get(StudentViewModel::class.java)

        if(student != null){
            binding.elFirstName.setText(student!!.firstName)
            binding.elMiddleName.setText(student!!.middleName)
            binding.elLastName.setText(student!!.lastName)
            binding.elPhone.setText(student!!.phone)
            val dt = GregorianCalendar().apply {
                time = Date(student!!.birthDate!!)
            }
            binding.dpCalendar.init(dt.get(Calendar.YEAR),dt.get(Calendar.MONTH),
                dt.get(Calendar.DAY_OF_MONTH),null)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StudentViewModel::class.java)
        // TODO: Use the ViewModel
    }
    private fun showCommitDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setCancelable(true)
        builder.setMessage("Сохранить изменения")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.commit)) { _, _ ->
            var p = true
            binding.elFirstName.text.toString().ifBlank {
                p = false
                binding.elFirstName.error = "Укажите значение"
            }
            binding.elLastName.text.toString().ifBlank {
                p = false
                binding.elLastName.error = "Укажите значение"
            }
            binding.elMiddleName.text.toString().ifBlank {
                p = false
                binding.elMiddleName.error = "Укажите значение"
            }
            binding.elPhone.text.toString().ifBlank {
                p = false
                binding.elPhone.error = "Укажите значение"
            }


            if (GregorianCalendar().get(GregorianCalendar.YEAR) - binding.dpCalendar.year < 10) {
                p = false
                Toast.makeText(context, "Укажите правильно возраст", Toast.LENGTH_LONG).show()
            }
            if (p) {
                val selectedDate = GregorianCalendar().apply {
                    set(GregorianCalendar.YEAR, binding.dpCalendar.year)
                    set(GregorianCalendar.MONTH, binding.dpCalendar.month)
                    set(GregorianCalendar.DAY_OF_MONTH, binding.dpCalendar.dayOfMonth)
                }
                if(student==null){
                    student = Student(
                        id  = null,
                        firstName = binding.elFirstName.text.toString(),
                        lastName = binding.elLastName.text.toString(),
                        middleName = binding.elMiddleName.text.toString(),
                        phone = binding.elPhone.text.toString(),
                        birthDate = selectedDate.time.time,
                        groupID = groupID
                    )
                    CoroutineScope(Dispatchers.Main). launch {
                        viewModel.newStudent(student!!, groupID)
                    }
                }
                else{
                    student?.apply{
                        firstName = binding.elFirstName.text.toString()
                        lastName = binding.elLastName.text.toString()
                        middleName = binding.elMiddleName.text.toString()
                        phone = binding.elPhone.text.toString()
                        birthDate = selectedDate.time.time
                    }
                    CoroutineScope(Dispatchers.Main). launch {
                        viewModel.editStudent(student!!)
                    }
                }
                backPressedCallback.isEnabled=false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        builder.setNegativeButton("отмена") { _, _, ->
            backPressedCallback.isEnabled=false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val alert = builder.create()
        alert.show()

    }

}