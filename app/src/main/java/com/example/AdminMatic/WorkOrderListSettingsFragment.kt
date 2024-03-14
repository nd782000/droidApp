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

    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var crewAdapter: ArrayAdapter<String>
    private lateinit var statusAdapter: ArrayAdapter<String>
    private lateinit var sortAdapter: ArrayAdapter<String>


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
            startDateValue = LocalDate.parse(startDate, GlobalVars.dateFormatterPHP)
        }

        if (endDate != "") {
            endDateValue = LocalDate.parse(endDate, GlobalVars.dateFormatterPHP)
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
                setFragmentResult("leadListSettings", bundleOf( "startDate" to startDate,
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
                        binding.startEt.setText(startDateValue.format(GlobalVars.dateFormatterShort))
                        endDateValue = pendingDate
                        binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else if (pendingDate < endDateValue) {
                        startDateValue = pendingDate
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
            startDateValue = LocalDate.parse(startDate, GlobalVars.dateFormatterPHP)
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
                        endDateValue = pendingDate
                        binding.endEt.setText(endDateValue.format(GlobalVars.dateFormatterShort))
                        binding.allDatesSwitch.isChecked = false
                    }
                    else if (pendingDate > startDateValue) {
                        endDateValue = pendingDate
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
            endDateValue = LocalDate.parse(endDate, GlobalVars.dateFormatterPHP)
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







        /*
        val statusArray = arrayOf("No Filter", getString(R.string.not_started), getString(R.string.in_progress), getString(R.string.finished), getString(R.string.canceled), getString(R.string.waiting))
        statusAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            statusArray
        )
        binding.statusSpinner.adapter = statusAdapter
        binding.statusSpinner.onItemSelectedListener = this@LeadListSettingsFragment

        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@LeadListSettingsFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.salesRepSearchRv.addItemDecoration(itemDecoration)

            binding.salesRepSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.salesRepSearchRv.visibility = View.GONE
                    if (salesRepName.isNotBlank()) {
                        binding.salesRepSearch.setQuery(salesRepName, false)
                    }
                    else {
                        binding.salesRepSearch.setQuery("", false)
                    }
                    binding.salesRepSearch.clearFocus()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.salesRepSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.salesRepSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as EmployeesAdapter).filter.filter(newText)
                    return false
                }

            })


            val closeButton: View? = binding.salesRepSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.salesRepSearch.setQuery("", false)
                salesRep = ""
                salesRepName = ""
                //myView.hideKeyboard()
                binding.salesRepSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.salesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    //binding.salesRepSearch.setQuery(lead!!.custName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }

            binding.salesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    if (salesRepName.isNotBlank()) {
                        binding.salesRepSearch.setQuery(salesRepName, false)
                    }
                    else {
                        binding.salesRepSearch.setQuery("", false)
                    }
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        val zonesList = mutableListOf<String>()
        GlobalVars.zones!!.forEach {
            zonesList.add(it.name)
        }
        zoneAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            zonesList
        )
        binding.zoneSpinner.adapter = zoneAdapter
        binding.zoneSpinner.onItemSelectedListener = this@LeadListSettingsFragment

        binding.clearAllFiltersBtn.setOnClickListener {
            setFragmentResult("leadListSettings", bundleOf("status" to "", "salesRep" to "", "zone" to ""))
            myView.findNavController().navigateUp()
        }


        // populate fields
        if (status.isNotBlank()) {
            binding.statusSpinner.setSelection(status.toInt())
        }

        if (salesRep.isNotBlank()) {
            for (i in 0 until GlobalVars.employeeList!!.size) {
                if (salesRep == GlobalVars.employeeList!![i].ID) {
                    binding.salesRepSearch.setQuery(GlobalVars.employeeList!![i].name, false)
                    binding.salesRepSearch.clearFocus()
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                    break
                }
            }
        }

        if (zone.isNotBlank()) {
            for (i in 0 until GlobalVars.zones!!.size) {
                if (zone == GlobalVars.zones!![i].ID) {
                    binding.zoneSpinner.setSelection(i)
                    break
                }
            }
        }

         */



    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        /*
        when (parent!!.id) {
            R.id.status_spinner -> {
                status = position.toString()
                if (status == "0") {
                    status = ""
                }
            }
            R.id.zone_spinner -> {
                println("Spinner was set")
                zone = GlobalVars.zones!![position].ID
                if (zone == "0") {
                    zone = ""
                }
            }
        }

         */
    }


}
