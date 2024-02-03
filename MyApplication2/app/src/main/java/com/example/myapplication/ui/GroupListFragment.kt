package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Faculty
import com.example.myapplication.data.Group
import com.example.myapplication.data.Student
import com.example.myapplication.databinding.FragmentGroupListBinding
import com.example.myapplication.models.FacultyViewModel
import com.example.myapplication.models.GroupListViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class GroupListFragment (private val group: Group): Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentGroupListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GroupListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvGroupList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewModel = ViewModelProvider(this).get(GroupListViewModel::class.java)
        //binding.rvGroupList.adapter=GroupListAdapter(view)
        viewModel.setGroupID(group.id!!)
        viewModel.group.observe(viewLifecycleOwner) {
            binding.rvGroupList.adapter = GroupListAdapter(it)
            //viewModel.loadStudents()
        }
        //viewModel.loadStudents()
//        lifecycleScope.launch {
//            val data = db.myDao().getAllData()
//            adapter.submitList(data)
//        }
    }

    fun update() = viewModel.loadStudents()

    private inner class GroupHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        lateinit var student: Student

        fun bind(student: Student) {
            this.student = student
            val s =
                "${student.lastName} ${student.firstName?.get(0)}. ${student.middleName?.get(0)}."
            itemView.findViewById<Button>(R.id.tvElement).text = s

            itemView.findViewById<ConstraintLayout>(R.id.clButtons).visibility = View.GONE
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val cl = itemView.findViewById<ConstraintLayout>(R.id.clButtons)
            cl.visibility = View.VISIBLE
            lastItemView?.findViewById<ConstraintLayout>(R.id.clButtons)?.visibility = View.GONE
            lastItemView = if (lastItemView == itemView) null else itemView
            if (cl.visibility == View.VISIBLE) {
                itemView.findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
                    commitDeleteDialog(student)
                }
                itemView.findViewById<ImageButton>(R.id.idEdit).setOnClickListener {
                    callbacks?.showStudent(group.id!!, student)
                }
            }
        }
    }

    private fun commitDeleteDialog(student: Student) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setMessage("Delete student ${student.lastName} ${student.firstName} ${student.middleName} из списка")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.commit)) { _, _ ->
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.deleteStudent(student)
            }
        }
        builder.setNegativeButton("отмена", null)
        val alert = builder.create()
        alert.show()
    }

    private var lastItemView: View? = null

    private inner class GroupListAdapter(private val items: List<Student>) :
        RecyclerView.Adapter<GroupHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GroupHolder {
            val view = layoutInflater.inflate(R.layout.layout_student_listelement, parent, false)
            return GroupHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: GroupHolder, position: Int) {
            holder.bind(items[position])
        }
    }

    //интерфейс для изменения title приложения на университет
    interface Callbacks {
        fun showStudent(groupID: Long, student: Student?)
    }

    var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        callbacks = null
        super.onDetach()
    }
}


