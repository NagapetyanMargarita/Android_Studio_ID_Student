package com.example.myapplication.ui

import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Faculty
import com.example.myapplication.databinding.FragmentFacultyBinding
import com.example.myapplication.models.FacultyViewModel
import com.example.myapplication.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

const val FACULTY_TAG = "FacultyFragment"
const  val FACULTY_TITLE="Университет"

class FacultyFragment : Fragment() { //является подклассом  Fragment
    private lateinit var viewModel: FacultyViewModel //связь с фрагментом
    private var _binding: FragmentFacultyBinding? = null
    val binding
        get() = _binding!!
    //экземпляром класса `FacultyListAdapter`, который инициализируется пустым списком.
    private var adapter: FacultyListAdapter = FacultyListAdapter(emptyList())
    //возвращает новый экземпляр класса `FacultyFragment`.
    companion object {
        fun newInstance() = FacultyFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentFacultyBinding.inflate(inflater, container,false)
        //отображение по вертикали
        binding.rvFaculty.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(FacultyViewModel:: class.java)
        viewModel.university.observe(viewLifecycleOwner){
            adapter=FacultyListAdapter(it)
            binding.rvFaculty.adapter=adapter
        }
        callbacks?.setTitle(FACULTY_TITLE) //безопасный вызов метода
        viewModel.loadFaculty()
    }

    private inner class FacultyHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        lateinit var faculty: Faculty

        fun bind(faculty: Faculty){
            this.faculty=faculty
            itemView.findViewById<Button>(R.id.tvFacultyElement).text=faculty.name
            itemView.findViewById<ConstraintLayout>(R.id.crudButtons).visibility = View.GONE
            itemView.findViewById<Button>(R.id.tvFacultyElement).setOnClickListener {
                callbacks?.showFaculty (faculty.id!!)
            }
        }

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?){
            val cl = itemView.findViewById<ConstraintLayout>(R.id.crudButtons)
            cl.visibility = View.VISIBLE
            lastItemView?.findViewById<ConstraintLayout>(R.id.crudButtons)?.visibility = View.GONE
            lastItemView = if (lastItemView == itemView) null else itemView
            if (cl.visibility == View.VISIBLE) {

                itemView.findViewById<ImageButton>(R.id.delBtn).setOnClickListener {
                    commitDelete(faculty)
                }
                itemView.findViewById<ImageButton>(R.id.editBtn).setOnLongClickListener {
                    editCreate(faculty)
                    true
                }
            }
        }
    }
    private fun commitDelete(faculty: Faculty) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setMessage("Удалить авиакомпанию ${faculty.name} ?")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.commit)) { _, _ ->
            CoroutineScope(Dispatchers.Main). launch {
                AppRepository.get().deleteFaculty(faculty)
            }
            //Toast.makeText(requireContext(), "Авиакомпания удалена", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }
    private fun editCreate(faculty: Faculty?){
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.name_input, null)
        builder.setView(dialogView)

        val editTextName = dialogView.findViewById(R.id.editTextTextPersonName) as EditText
        val tvInfo = dialogView.findViewById(R.id.tvInfo) as TextView
        if(faculty != null){
            builder.setTitle("Редактирование факультета")
            tvInfo.text =getString(R.string.inputFaculty)
            editTextName.setText(faculty.name)
        }
        builder.setPositiveButton(getString(R.string.commit)) { _, _, ->
            var p = true
            editTextName.text.toString().trim().ifBlank {
                p = false
                editTextName.error = "Укажите значение названия"
            }

            if (p) {
                val s = editTextName.text.toString().trim()//получение значения из поля
                if(faculty != null) {
                    val upFaculty = Faculty(faculty.id, editTextName.text.toString().trim())
                    CoroutineScope(Dispatchers.Main).launch {
                        AppRepository.get().updateFaculty(upFaculty)
                    }
                }
                else
                    CoroutineScope(Dispatchers.Main). launch {
                        AppRepository.get().newFaculty(s)
                    }
            }
            else {
                val builder1 = AlertDialog.Builder(requireContext())
                builder1.setTitle("Некорректный ввод")
                builder1.setMessage("Пожалуйста, заполните все поля.")
                builder1.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    // Код, который будет выполнен при нажатии на кнопку OK
                })
                builder1.setCancelable(false) // Запретить закрытие диалога при нажатии на кнопку Back
                builder1.show()
            }
        }
        builder.setNegativeButton(R.string.cancel, null)
        val alert = builder.create()
        alert.show()
    }
    private var lastItemView : View? = null
    private inner class FacultyListAdapter(private val items: List<Faculty>) : RecyclerView.Adapter<FacultyHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FacultyHolder {
            val view = layoutInflater.inflate(R.layout.element_faculty_list,parent,false)
            return FacultyHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: FacultyHolder, position: Int) {
            holder.bind(items[position])
        }
    }
    //интерфейс для изменения title приложения на университет . Взаимодействие активити и фрагмента
    interface Callbacks{
        fun setTitle(_title: String)
        fun showFaculty (id: Long)
    }
    var callbacks : Callbacks? =null

    override fun onAttach(context: Context){
        super.onAttach(context)
        callbacks=context as Callbacks
    }

    override fun onDetach(){
        callbacks=null
        super.onDetach()
    }
    //
}