package com.example.AdminMatic

import android.os.Bundle
import android.provider.Settings.Global
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentLeadListSettingsBinding
import com.AdminMatic.databinding.FragmentWorkOrderListSettingsBinding
import java.time.LocalDate


class WorkOrderListSettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var startDateValue = LocalDate.now()
    private var endDateValue= LocalDate.now()


    private var startDate = ""
    private var endDate = ""
    private var lockedDates = ""
    private var department = ""
    private var crew = ""
    private var status = ""
    private var sort = ""

    private lateinit var departmentsAdapter: ArrayAdapter<String>
    private lateinit var crewsAdapter: ArrayAdapter<String>
    private lateinit var statusAdapter: ArrayAdapter<String>
    private lateinit var sortAdapter: ArrayAdapter<String>

    private var statusNameArray = arrayOf<String>()

    private var statusArray = arrayOf("","1", "2", "3", "4", "5")

    private var sortNameArray = arrayOf<String>()

    private var sortArray = arrayOf(
        "",
        "old",
        "new",
        "next_plan",
        "low_cost",
        "high_cost",
        "low_price",
        "high_price",
        "low_profit",
        "high_profit",
        )




    //lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            startDate = it.getString("startDate")!!
            endDate = it.getString("endDate")!!
            lockedDates = it.getString("lockedDates")!!
            department = it.getString("department")!!
            crew = it.getString("crew")!!
            status = it.getString("status")!!
            sort = it.getString("sort")!!
        }

        if (startDate != "") {
            startDateValue = LocalDate.parse(startDate, GlobalVars.dateFormatterYYYYMMDD)
        }

        if (endDate != "") {
            endDateValue = LocalDate.parse(endDate, GlobalVars.dateFormatterYYYYMMDD)
        }

    }

    private var _binding: FragmentWorkOrderListSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWorkOrderListSettingsBinding.inflate(inflater, container, false)
        myView = binding.root

        //globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_list_settings)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("handleOnBackPressed")
                setFragmentResult("workOrderListSettings", bundleOf( "startDate" to startDate,
                                                                                "endDate" to endDate,
                                                                                "lockedDates" to lockedDates,
                                                                                "department" to department,
                                                                                "crew" to crew,
                                                                                "status" to status,
                                                                                "sort" to sort))

                myView.findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // All Dates Switch
        binding.allDatesSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.startEt.text.clear()
                startDate = ""
                binding.endEt.text.clear()
                endDate = ""
            }
            else {
                startDate = GlobalVars.dateFormatterYYYYMMDD.format(startDateValue)
                binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
                endDate = GlobalVars.dateFormatterYYYYMMDD.format(endDateValue)
                binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
            }
        }

        if (startDate != "" || endDate != "") {
            binding.allDatesSwitch.isChecked = false
        }
        else {
            binding.allDatesSwitch.isChecked = true
        }
        binding.allDatesSwitch.jumpDrawablesToCurrentState()

        // Start Date Picker
        binding.startEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(startDateValue.year, startDateValue.monthValue-1, startDateValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    val pendingDate = LocalDate.of(year, month+1, dayOfMonth)

                    if (binding.endEt.text.isBlank()) {
                        startDateValue = pendingDate
                        startDate = GlobalVars.dateFormatterYYYYMMDD.format(startDateValue)
                        binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
                        endDateValue = pendingDate
                        endDate = startDate
                        binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else if (pendingDate < endDateValue) {
                        startDateValue = pendingDate
                        startDate = GlobalVars.dateFormatterYYYYMMDD.format(startDateValue)
                        binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else {
                        globalVars.simpleAlert(com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error), com.example.AdminMatic.myView.context.getString(R.string.dialogue_start_time_after_stop))
                    }
                }
            })
        }

        if (startDate != "") {
            startDateValue = LocalDate.parse(startDate, GlobalVars.dateFormatterYYYYMMDD)
            binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
        }

        // End Date Picker
        binding.endEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(endDateValue.year, endDateValue.monthValue-1, endDateValue.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    val pendingDate = LocalDate.of(year, month+1, dayOfMonth)

                    if (binding.startEt.text.isBlank()) {
                        startDateValue = pendingDate
                        binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
                        startDate = GlobalVars.dateFormatterYYYYMMDD.format(startDateValue)
                        endDateValue = pendingDate
                        endDate = startDate
                        binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else if (pendingDate > startDateValue) {
                        endDateValue = pendingDate
                        endDate = GlobalVars.dateFormatterYYYYMMDD.format(endDateValue)
                        binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else {
                        globalVars.simpleAlert(com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_error), com.example.AdminMatic.myView.context.getString(R.string.dialogue_stop_time_before_start))
                    }
                }
            })
        }

        if (endDate != "") {
            endDateValue = LocalDate.parse(endDate, GlobalVars.dateFormatterYYYYMMDD)
            binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
        }

        // Show Locked Dates Switch
        binding.lockedSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lockedDates = "1"
            }
        }

        if (lockedDates == "1") {
            binding.lockedSwitch.isChecked = true
        }
        else {
            binding.lockedSwitch.isChecked = false
        }
        binding.allDatesSwitch.jumpDrawablesToCurrentState()

        // Department Spinner
        val departmentNameList = mutableListOf<String>()
        GlobalVars.departments!!.forEach {
            departmentNameList.add(it.name)
        }
        departmentNameList[0] = getString(R.string.no_filter)

        departmentsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentNameList
        )
        binding.departmentSpinner.adapter = departmentsAdapter
        binding.departmentSpinner.onItemSelectedListener = this@WorkOrderListSettingsFragment

        if (department != "") {
            GlobalVars.departments!!.forEachIndexed { i, e ->
                if (e.ID == department) {
                    binding.departmentSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.departmentSpinner.setSelection(0)
        }

        // Crew Spinner
        val crewNameList = mutableListOf<String>()
        GlobalVars.crews!!.forEach {
            crewNameList.add(it.name)
        }
        crewNameList[0] = getString(R.string.no_filter)

        crewsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            crewNameList.toTypedArray()
        )
        binding.crewSpinner.adapter = crewsAdapter
        binding.crewSpinner.onItemSelectedListener = this@WorkOrderListSettingsFragment

        if (crew != "") {
            GlobalVars.crews!!.forEachIndexed { i, e ->
                if (e.ID == crew) {
                    binding.crewSpinner.setSelection(i)
                }
            }
        }
        else {
            binding.crewSpinner.setSelection(0)
        }

        // Status Picker
        statusNameArray = arrayOf (
            getString(R.string.no_filter),
            getString(R.string.not_started),
            getString(R.string.in_progress),
            getString(R.string.finished),
            getString(R.string.canceled),
            getString(R.string.waiting)
        )

        statusAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            statusNameArray
        )
        binding.statusSpinner.adapter = statusAdapter
        binding.statusSpinner.onItemSelectedListener = this@WorkOrderListSettingsFragment

        if (status == "") {
            binding.statusSpinner.setSelection(0)
        }
        else {
            binding.statusSpinner.setSelection(status.toInt())
        }

        // Sort Picker
        sortNameArray = arrayOf (
            getString(R.string.no_filter),
            getString(R.string.wo_sort_oldest),
            getString(R.string.wo_sort_newest),
            getString(R.string.wo_sort_next_planned),
            getString(R.string.wo_sort_lowest_cost),
            getString(R.string.wo_sort_highest_cost),
            getString(R.string.wo_sort_lowest_price),
            getString(R.string.wo_sort_highest_price),
            getString(R.string.wo_sort_lowest_profit),
            getString(R.string.wo_sort_highest_profit)
        )

        sortAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            sortNameArray
        )
        binding.sortSpinner.adapter = sortAdapter
        binding.sortSpinner.onItemSelectedListener = this@WorkOrderListSettingsFragment

        if (sort == "") {
            binding.sortSpinner.setSelection(0)
        }
        else {
            binding.sortSpinner.setSelection(sortArray.indexOf(sort))
        }

        // Clear all filters button
        binding.clearAllFiltersBtn.setOnClickListener {

            setFragmentResult("workOrderListSettings", bundleOf( "startDate" to "",
                "endDate" to "",
                "lockedDates" to "",
                "department" to "",
                "crew" to "",
                "status" to "",
                "sort" to ""))

            myView.findNavController().navigateUp()

        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (parent!!.id) {
            R.id.department_spinner -> {
                department = GlobalVars.departments?.get(position)?.ID.toString()
                if (department == "0") { department = "" }
            }
            R.id.crew_spinner -> {
                crew = GlobalVars.crews?.get(position)?.ID.toString()
                if (crew == "0") { crew = "" }
            }
            R.id.status_spinner -> {
                status = statusArray[position]
            }
            R.id.sort_spinner -> {
                sort = sortArray[position]
            }
        }

    }


}
