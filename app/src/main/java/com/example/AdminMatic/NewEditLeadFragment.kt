package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditLeadBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*
import kotlin.concurrent.schedule


class NewEditLeadFragment : Fragment(), CustomerCellClickListener, EmployeeCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var lead: Lead? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private var dateToday: LocalDate = LocalDate.now(ZoneOffset.UTC)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lead = it.getParcelable("lead")
        }
        if (lead != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("leadNewEdit")
    }

    private var _binding: FragmentNewEditLeadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditLeadBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade && editsMadeDelayPassed){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_lead, lead!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_lead)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flag edits made false after all the views have time to set their states
        Timer("LeadEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        if (!editMode) {
            lead = Lead("0", "1")
            lead!!.timeType = "0" //default to ASAP
            lead!!.urgent = "0"
            lead!!.requestedByCust = "0"
        }


        binding.newEditLeadStatusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }
        setStatusIcon()

        // Customer search

        println("Customer list size: ${GlobalVars.customerList!!.size}")

        binding.newEditLeadCustomerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@NewEditLeadFragment, false
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.newEditLeadCustomerSearchRv.addItemDecoration(itemDecoration)

            binding.newEditLeadCustomerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.newEditLeadCustomerSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as CustomersAdapter).filter.filter(newText)
                    return false
                }

            })


            val closeButton: View? = binding.newEditLeadCustomerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.newEditLeadCustomerSearch.setQuery("", false)
                lead!!.customer = "0"
                lead!!.custName = ""
                myView.hideKeyboard()
                binding.newEditLeadCustomerSearch.clearFocus()
                binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE
            }

            binding.newEditLeadCustomerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.newEditLeadCustomerSearch.setQuery(lead!!.custName, false)
                    binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        /*
        val closeButton: View = binding.newEditLeadCustomerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener {
            println("fdsfd")
        }

         */

        // Deadline
        binding.newEditLeadDeadlineEt.setOnClickListener {
            val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)

            datePicker.showDialog(dateToday.year, dateToday.monthValue-1, dateToday.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    editsMade = true
                    val selectedDate = LocalDate.of(year, month+1, dayOfMonth)
                    binding.newEditLeadDeadlineEt.setText(selectedDate.format(GlobalVars.dateFormatterShort))
                    lead!!.deadline = selectedDate.format(GlobalVars.dateFormatterYYYYMMDD)
                }
            })
        }

        // Urgent switch
        binding.newEditLeadUrgentSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                lead!!.urgent = "1"
            }
            else {
                lead!!.urgent = "0"
            }
        }

        // Sales rep search
        binding.newEditLeadSalesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@NewEditLeadFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.newEditLeadSalesRepSearchRv.addItemDecoration(itemDecoration)

            binding.newEditLeadSalesRepSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    if(newText == ""){
                        binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.newEditLeadSalesRepSearchRv.visibility = View.VISIBLE
                    }
                    (adapter as EmployeesAdapter).filter.filter(newText)
                    return false
                }

            })

            val closeButton: View? = binding.newEditLeadSalesRepSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.newEditLeadSalesRepSearch.setQuery("", false)
                lead!!.salesRep = ""
                lead!!.repName = ""
                myView.hideKeyboard()
                binding.newEditLeadSalesRepSearch.clearFocus()
                binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.newEditLeadSalesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.newEditLeadSalesRepSearch.setQuery(lead!!.repName, false)
                    binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        // Requested by customer switch
        binding.newEditLeadRequestedSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                lead!!.requestedByCust = "1"
            }
            else {
                lead!!.requestedByCust = "0"
            }
        }

        // Description
        binding.newEditLeadDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newEditLeadDescriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Submit button
        binding.newEditLeadSubmitBtn.setOnClickListener {
            if (validateFields()) {
                updateLead()
            }
        }



        // ===== POPULATE FIELDS =====
        if (editMode) {
            setStatusIcon()

            binding.newEditLeadCustomerSearch.setQuery(lead!!.custName, false)
            //binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE

            if (!lead!!.deadlineNice.isNullOrBlank()) {
                println("setting deadline")
                binding.newEditLeadDeadlineEt.setText(lead!!.deadlineNice!!)
            }

            if (lead!!.urgent == "1") {
                binding.newEditLeadUrgentSwitch.isChecked = true
                binding.newEditLeadUrgentSwitch.jumpDrawablesToCurrentState()
            }

            binding.newEditLeadSalesRepSearch.setQuery(lead!!.repName, false)
            //binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE

            if (lead!!.requestedByCust == "1") {
                binding.newEditLeadRequestedSwitch.isChecked = true
                binding.newEditLeadRequestedSwitch.jumpDrawablesToCurrentState()
            }

            binding.newEditLeadDescriptionEt.setText(lead!!.description)
        }

        editsMade = false
        binding.newEditLeadCustomerSearchRv.visibility = View.INVISIBLE
        binding.newEditLeadSalesRepSearchRv.visibility = View.INVISIBLE

    }

    private fun validateFields(): Boolean {
        if (lead!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_lead),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        //Skipping schedule type checking here since droid spinners can't be empty so it defaults to ASAP

        if (lead!!.timeType == "1") {
            if (lead!!.date.isNullOrBlank()) {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_lead),getString(R.string.dialogue_incomplete_lead_select_date))
                return false
            }

        }

        return true
    }

    private fun updateLead() {
         println("updateLead")
        showProgressView()

        lead!!.description = binding.newEditLeadDescriptionEt.text.toString()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/lead.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        val newLeadID: String = gson.fromJson(parentObject["leadID"].toString(), String::class.java)
                        lead!!.ID = newLeadID
                        globalVars.playSaveSound(myView.context)
                        editsMade = false


                        if (editMode) {
                            myView.findNavController().navigateUp()
                        } else {
                            val directions = NewEditLeadFragmentDirections.navigateToLead(lead!!.ID)
                            myView.findNavController().navigate(directions)
                        }
                    }


                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["leadID"] = lead!!.ID
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["urgent"] = lead!!.urgent.toString()
                params["repID"] = lead!!.salesRep.toString()

                params["requestedByCust"] = lead!!.requestedByCust.toString()
                params["description"] = lead!!.description.toString()
                params["deadline"] = lead!!.deadline.toString()
                params["status"] = lead!!.statusID
                params["custID"] = lead!!.customer.toString()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.newEditLeadStatusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))
        popUp.menu.add(0, 5, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_waiting)!!,myView.context), myView.context.getString(R.string.waiting)))

        popUp.setOnMenuItemClickListener { item: MenuItem? ->
            lead!!.statusID = item?.itemId.toString()
            setStatusIcon()

            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun setStatusIcon() {
        when (lead!!.statusID) {
            "1" -> binding.newEditLeadStatusBtn.setBackgroundResource(R.drawable.ic_not_started)
            "2" -> binding.newEditLeadStatusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            "3" -> binding.newEditLeadStatusBtn.setBackgroundResource(R.drawable.ic_done)
            "4" -> binding.newEditLeadStatusBtn.setBackgroundResource(R.drawable.ic_canceled)
            "5" -> binding.newEditLeadStatusBtn.setBackgroundResource(R.drawable.ic_waiting)
        }
    }

    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        lead!!.customer = data.ID
        lead!!.custName = data.sysname
        binding.newEditLeadCustomerSearch.setQuery(lead!!.custName, false)
        binding.newEditLeadCustomerSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onEmployeeCellClickListener(data: Employee) {
        editsMade = true
        lead!!.salesRep = data.ID
        lead!!.repName = data.name
        binding.newEditLeadSalesRepSearch.setQuery(lead!!.custName, false)
        binding.newEditLeadSalesRepSearch.clearFocus()
        myView.hideKeyboard()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }


    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}