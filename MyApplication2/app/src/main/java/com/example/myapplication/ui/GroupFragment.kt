package com.example.myapplication.ui

import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.get
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.R
import com.example.myapplication.data.Faculty
import com.example.myapplication.data.Group
import com.example.myapplication.data.Student
import com.example.myapplication.databinding.FragmentFacultyBinding
import com.example.myapplication.databinding.FragmentGroupBinding
import com.example.myapplication.models.GroupViewModel
import com.example.myapplication.repository.AppRepository
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

const val  GROUP_TAG = "GroupFragment"
class GroupFragment : Fragment() {
    private var miDelAirlane: MenuItem? = null
    private var miModAirlane: MenuItem? = null
    private var _binding: FragmentGroupBinding? = null
    val binding
        get() = _binding!!


    companion object {
        private var id : Long = -1
        private  var _group: Group?=null
        fun newInstance(id: Long): GroupFragment{
            GroupFragment()
            this.id=id
            return GroupFragment()
        }
        val getFaculryID
            get() = id
        val getGroup
            get() = _group
    }

    private lateinit var viewModel: GroupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding= FragmentGroupBinding.inflate(inflater, container,false)
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu_group, menu)
        miDelAirlane = menu?.findItem(R.id.miDelAirlanePlace)
        miModAirlane = menu?.findItem(R.id.miModAirlanePlace)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var per = -1
        when (item?.itemId) {
            R.id.miDelAirlanePlace -> {
                per = 0
            }
            R.id.miModAirlanePlace -> {
                per = 1
            }
        }
        if(binding.tabGroup.tabCount > 0){
            if (per == 0){
                if(binding.tabGroup.tabCount > 0)
                commitDelete(_group!!)
            else
                Toast.makeText(requireContext(), "Список групп пуст.", Toast.LENGTH_SHORT).show()
        }
            if (per == 1)
            {  if(binding.tabGroup.tabCount > 0)
                editCreate(_group)
            else
                Toast.makeText(requireContext(), "Список групп пуст.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun commitDelete(group: Group) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setMessage("Удалить группу ${group.name} ?")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.commit)) { _, _ ->
            CoroutineScope(Dispatchers.Main). launch {
                AppRepository.get().deleteGroup(getFaculryID,group)
            }
            // Toast.makeText(requireContext(), "Город удалён", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }
    private fun editCreate(group: Group ?){
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.name_input, null)
        builder.setView(dialogView)

        val editTextName = dialogView.findViewById(R.id.editTextTextPersonName) as EditText
        val tvInfo = dialogView.findViewById(R.id.tvInfo) as TextView
        if(group != null){
            builder.setTitle("Редактирование факультета")
            tvInfo.text =getString(R.string.inputFaculty)
            editTextName.setText(group.name)
        }
        builder.setPositiveButton(getString(R.string.commit)) { _, _, ->
            var p = true
            editTextName.text.toString().trim().ifBlank {
                p = false
                editTextName.error = "Укажите значение названия"
            }

            if (p) {
                val s = editTextName.text.toString().trim()//получение значения из поля
                if(group != null) {
                    val upGroup = Group(group.id, editTextName.text.toString().trim(),getFaculryID)
                    CoroutineScope(Dispatchers.Main).launch {
                        AppRepository.get().updateGroup(upGroup, getFaculryID)
                    }
                }
                else
                    CoroutineScope(Dispatchers.Main). launch {
                        AppRepository.get().newGroup(getFaculryID,s)
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider (this).get(GroupViewModel::class.java)
        viewModel.setFacultyID(getFaculryID)
//        viewModel.faculty.observe(viewLifecycleOwner){
//            updateUI(it)
//            callbacks?.setTitle(it?.name?: "")
//        }
        CoroutineScope(Dispatchers.Main).launch {
            val f = viewModel.getFaculty()
            callbacks?.setTitle(f?.name ?: "UNKNOWN")
        }

        viewModel.faculty.observe(viewLifecycleOwner){
            updateUI(it)
        }

    }
    private var tabPosition : Int =0

    private fun updateUI (groups : List<Group>){
        binding.tabGroup.clearOnTabSelectedListeners()
        binding.tabGroup.removeAllTabs()

        binding.faBtnNewStudent.visibility= if((groups.size) > 0) {
            binding.faBtnNewStudent.setOnClickListener {
                callbacks?.showStudent(groups.get(tabPosition).id!!, null)
            }
            View.VISIBLE
        }
        else View.GONE

        for (i in 0 until (groups.size ?: 0)){
            binding.tabGroup.addTab(binding.tabGroup.newTab().apply { text= i.toString() })
        }

        val adapter = GroupPageAdapter(requireActivity(), groups)
        binding.vpGroup.adapter=adapter
        TabLayoutMediator(binding.tabGroup, binding.vpGroup, true, true){
                tab,pos -> tab.text = groups[pos].name
        }.attach()
        if (tabPosition < binding.tabGroup.tabCount){
            binding.tabGroup.selectTab(binding.tabGroup.getTabAt(tabPosition))
            if (groups.size > 0){
                _group = groups[tabPosition]
            }
        }
        else{
            binding.tabGroup.selectTab(binding.tabGroup.getTabAt(tabPosition - 1))
            if (groups.size > 0){
                _group = groups[tabPosition - 1]
            }
        }
        binding.tabGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabPosition=tab?.position!!
                _group= groups[tabPosition]
                viewModel.loadStudents(groups[tabPosition].id!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private inner class GroupPageAdapter(fa: FragmentActivity, private val groups: List<Group>):
        FragmentStateAdapter(fa){
        override fun getItemCount(): Int {
            return (groups.size ?: 0)
        }

        override fun createFragment(position: Int): Fragment {
            return  GroupListFragment(groups[position])
        }

//        fun updateFragments(newFragments: List<Fragment>) {
//            fragments = newFragments
//            notifyDataSetChanged()
//        }
    }


    //интерфейс для изменения title приложения на университет
    interface Callbacks{
        fun setTitle(_title: String)
        fun showStudent(groupID: Long, student: Student?)
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


}