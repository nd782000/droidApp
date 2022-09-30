package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditWorkOrderBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class NewEditWorkOrderFragment : Fragment(), AdapterView.OnItemSelectedListener, CustomerCellClickListener, EmployeeCellClickListener {

    private var editsMade = false

    private var workOrder: WorkOrder? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private lateinit var chargeTypeAdapter: ArrayAdapter<String>
    private lateinit var invoiceTypeAdapter: ArrayAdapter<String>
    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var crewAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workOrder = it.getParcelable("workOrder")
        }
        if (workOrder != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("workOrderNewEdit")
    }

    private var _binding: FragmentNewEditWorkOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditWorkOrderBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        parentFragmentManager.popBackStackImmediate()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    parentFragmentManager.popBackStackImmediate()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_work_order, workOrder!!.woID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_work_order)
        }
        setHasOptionsMenu(true)
        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!editMode) {
            workOrder = WorkOrder("0")
        }

        // Customer search
        println("Customer list size: ${GlobalVars.customerList!!.size}")

        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@NewEditWorkOrderFragment, false
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.salesRepSearchRv.addItemDecoration(itemDecoration)

            binding.customerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    //customerRecyclerView.visibility = View.INVISIBLE
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as CustomersAdapter).filter.filter(newText)
                    if(newText == ""){
                        binding.customerSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.customerSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })


            val closeButton: View? = binding.customerSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.customerSearch.setQuery("", false)
                workOrder!!.customer = "0"
                myView.hideKeyboard()
                binding.customerSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
            }

            binding.customerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.customerSearch.setQuery(workOrder!!.custName, false)
                    binding.customerSearchRv.visibility = View.INVISIBLE
                }
            }
        }

        // Title
        binding.titleEditText.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })


        // Charge Type Spinner
        val scheduleTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))
        chargeTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            scheduleTypeArray
        )
        binding.chargeTypeSpinner.adapter = chargeTypeAdapter
        binding.chargeTypeSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        // Invoice Type Spinner
        val invoiceTypeArray = arrayOf(getString(R.string.invoice_type_upon_completion), getString(R.string.invoice_type_batch), getString(R.string.invoice_type_no_invoice))
        invoiceTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            invoiceTypeArray
        )
        binding.invoiceTypeSpinner.adapter = invoiceTypeAdapter
        binding.invoiceTypeSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        // Sales rep search
        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(),
                    myView.context, this@NewEditWorkOrderFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.salesRepSearchRv.addItemDecoration(itemDecoration)

            binding.salesRepSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    //customerRecyclerView.visibility = View.INVISIBLE
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as EmployeesAdapter).filter.filter(newText)
                    if(newText == ""){
                        binding.salesRepSearchRv.visibility = View.INVISIBLE
                    }else{
                        binding.salesRepSearchRv.visibility = View.VISIBLE
                    }
                    return false
                }

            })

            val closeButton: View? = binding.salesRepSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                binding.salesRepSearch.setQuery("", false)
                workOrder!!.salesRep = ""
                myView.hideKeyboard()
                binding.salesRepSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.salesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.salesRepSearch.setQuery(workOrder!!.salesRepName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }


        val departmentNameList = mutableListOf<String>()
        GlobalVars.departments!!.forEach {
            departmentNameList.add(it.name)
        }

        departmentAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            departmentNameList
        )
        binding.departmentSpinner.adapter = departmentAdapter
        binding.departmentSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment

        val crewNameList = mutableListOf<String>()
        GlobalVars.crews!!.forEach {
            crewNameList.add(it.name)
        }
        crewAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            crewNameList.toTypedArray()
        )
        binding.crewSpinner.adapter = crewAdapter
        binding.crewSpinner.onItemSelectedListener = this@NewEditWorkOrderFragment



        // Notes
        binding.notesEditText.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.notesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })


        // Submit button
        binding.submitBtn.setOnClickListener {

            if (validateFields()) {
                if (editMode) {
                    updateWorkOrder()
                }
                else {
                    newWorkOrder()
                }
            }


        }



        // ===== POPULATE FIELDS =====
        if (editMode) {


            binding.customerSearch.setQuery(workOrder!!.custName, false)
            binding.customerSearchRv.visibility = View.INVISIBLE

            binding.titleEditText.setText(workOrder!!.title)

            if (!workOrder!!.charge.isNullOrBlank()) {
                binding.chargeTypeSpinner.setSelection(workOrder!!.charge!!.toInt()-1)
            }

            if (!workOrder!!.invoiceType.isNullOrBlank()) {
                binding.invoiceTypeSpinner.setSelection(workOrder!!.invoiceType!!.toInt()-1)
            }

            println("Department: ${workOrder!!.department}")
            if (workOrder!!.department != "") {
                println("Setting Department ${workOrder!!.department}")
                for (i in 0 until GlobalVars.departments!!.size) {
                    if (GlobalVars.departments!![i].ID == workOrder!!.department) {
                        println("Found the department!")
                        binding.departmentSpinner.setSelection(i)
                    }
                }
            }

            println("Crew: ${workOrder!!.crew}")
            if (workOrder!!.crew != "") {
                println("Setting Crew ${workOrder!!.crew}")
                for (i in 0 until GlobalVars.crews!!.size) {
                    if (GlobalVars.crews!![i].ID == workOrder!!.crew) {
                        println("Found the crew!")
                        binding.crewSpinner.setSelection(i)
                    }
                }
            }


            binding.salesRepSearch.setQuery(workOrder!!.salesRepName, false)
            binding.salesRepSearchRv.visibility = View.INVISIBLE


            binding.notesEditText.setText(workOrder!!.notes)
        }

        println("editsmade false")
        editsMade = false

    }


    private fun validateFields(): Boolean {
        if (workOrder!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_wo),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        //Skipping charge and invoice type checking here since droid spinners can't be empty so it defaults to the first option

        if (workOrder!!.title.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_wo),getString(R.string.dialogue_incomplete_wo_set_title))
            return false
        }

        return true
    }


    private fun updateWorkOrder() {
        println("updateCustomer")
        showProgressView()

        workOrder!!.notes = binding.notesEditText.text.toString()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrder.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    globalVars.playSaveSound(myView.context)
                    editsMade = false


                    myView.findNavController().popBackStack()


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

                params["woID"] = workOrder!!.woID
                params["charge"] = workOrder!!.charge.toString()
                params["customer"] = workOrder!!.customer.toString()
                params["notes"] = workOrder!!.notes.toString()
                params["salesRep"] = workOrder!!.salesRep.toString()
                if (workOrder!!.lead == null) {
                    params["leadID"] = "0"
                }
                else {
                    params["leadID"] = workOrder!!.lead!!.ID
                }
                if (workOrder!!.contract == null) {
                    params["contractID"] = "0"
                }
                else {
                    params["contractID"] = workOrder!!.contract!!.ID
                }
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["createdByName"] = GlobalVars.loggedInEmployee!!.name
                params["title"] = binding.titleEditText.text.toString()
                params["crew"] = workOrder!!.crew.toString()
                params["crewName"] = workOrder!!.crewName.toString()
                params["departmentID"] = workOrder!!.department.toString()
                params["invoice"] = workOrder!!.invoiceType.toString()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "workOrderNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun newWorkOrder() {
        println("updateCustomer")
        showProgressView()

        workOrder!!.notes = binding.notesEditText.text.toString()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/new/workOrder.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    val newWoID:String = gson.fromJson(parentObject["woID"].toString() , String::class.java)
                    workOrder!!.woID = newWoID
                    globalVars.playSaveSound(myView.context)
                    editsMade = false


                    if (editMode) {
                        myView.findNavController().popBackStack()
                    }
                    else {
                        val directions = NewEditWorkOrderFragmentDirections.navigateToWorkOrder(null)
                        directions.workOrderID = newWoID
                        myView.findNavController().navigate(directions)
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

                params["woID"] = workOrder!!.woID
                params["charge"] = workOrder!!.charge.toString()
                params["customer"] = workOrder!!.customer.toString()
                params["notes"] = workOrder!!.notes.toString()
                params["salesRep"] = workOrder!!.salesRep.toString()
                params["leadID"] = "0"
                params["contractID"] = "0"
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["createdByName"] = GlobalVars.loggedInEmployee!!.name
                params["title"] = binding.titleEditText.text.toString()
                params["crew"] = workOrder!!.crew.toString()
                params["crewName"] = workOrder!!.crewName.toString()
                params["departmentID"] = workOrder!!.department.toString()
                params["invoice"] = workOrder!!.invoiceType.toString()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "workOrderNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true


        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                workOrder!!.charge = (position+1).toString()
            }

            R.id.invoice_type_spinner -> {
                workOrder!!.invoiceType = (position+1).toString()
            }

            R.id.department_spinner -> {
                workOrder!!.department = GlobalVars.departments?.get(position)?.ID.toString()
            }

            R.id.crew_spinner -> {
                workOrder!!.crew = GlobalVars.crews?.get(position)?.ID.toString()
            }
        }

    }

    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        workOrder!!.customer = data.ID
        workOrder!!.custName = data.sysname
        binding.customerSearch.setQuery(workOrder!!.custName, false)
        binding.customerSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onEmployeeCellClickListener(data: Employee) {
        editsMade = true
        workOrder!!.salesRep = data.ID
        workOrder!!.salesRepName = data.name
        binding.salesRepSearch.setQuery(workOrder!!.custName, false)
        binding.salesRepSearch.clearFocus()
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