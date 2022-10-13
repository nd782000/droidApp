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
import com.AdminMatic.databinding.FragmentNewEditContractBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class NewEditContractFragment : Fragment(), AdapterView.OnItemSelectedListener, CustomerCellClickListener, EmployeeCellClickListener {

    private var editsMade = false

    private var contract: Contract? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private lateinit var chargeTypeAdapter: ArrayAdapter<String>
    private lateinit var paymentTermsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable("contract")
        }
        if (contract != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contractNewEdit")
    }

    private var _binding: FragmentNewEditContractBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditContractBinding.inflate(inflater, container, false)
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
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_contract_bar, contract!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_contract_bar)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!editMode) {
            contract = Contract("0", "0")
        }

        binding.statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }
        setStatusIcon()


        // Customer search
        println("Customer list size: ${GlobalVars.customerList!!.size}")
        binding.customerSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                CustomersAdapter(
                    GlobalVars.customerList!!,
                    this@NewEditContractFragment, false
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.customerSearchRv.addItemDecoration(itemDecoration)

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
                contract!!.customer = "0"
                myView.hideKeyboard()
                binding.customerSearch.clearFocus()
                binding.customerSearchRv.visibility = View.INVISIBLE
            }

            binding.customerSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.customerSearch.setQuery(contract!!.custName, false)
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
        val chargeTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))
        chargeTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            chargeTypeArray
        )
        binding.chargeTypeSpinner.adapter = chargeTypeAdapter
        binding.chargeTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        // Payment Terms Spinner
        val paymentTermsList = mutableListOf<String>()
        GlobalVars.paymentTerms!!.forEach {
            println("Name: ${it.name}")
            paymentTermsList.add(it.name)
        }
        paymentTermsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            paymentTermsList
        )
        binding.paymentTermsSpinner.adapter = paymentTermsAdapter
        binding.paymentTermsSpinner.onItemSelectedListener = this@NewEditContractFragment


        // Sales rep search
        binding.salesRepSearchRv.apply {
            layoutManager = LinearLayoutManager(activity)


            adapter = activity?.let {
                EmployeesAdapter(
                    GlobalVars.employeeList!!.toMutableList(), false, myView.context, this@NewEditContractFragment
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
                contract!!.salesRep = ""
                myView.hideKeyboard()
                binding.salesRepSearch.clearFocus()
                binding.salesRepSearchRv.visibility = View.INVISIBLE
            }

            binding.salesRepSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    binding.salesRepSearch.setQuery(contract!!.repName, false)
                    binding.salesRepSearchRv.visibility = View.INVISIBLE
                }
            }
        }


        // Notes
        binding.notesEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.notesEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateContract()
            }
        }



        // ===== POPULATE FIELDS =====
        if (editMode) {
            setStatusIcon()

            binding.customerSearch.setQuery(contract!!.custName, false)
            binding.customerSearchRv.visibility = View.INVISIBLE

            binding.titleEditText.setText(contract!!.title)


            if (!contract!!.chargeType.isNullOrBlank()) {
                binding.chargeTypeSpinner.setSelection(contract!!.chargeType!!.toInt()-1)
            }


            if (!contract!!.paymentTermsID.isNullOrBlank()) {

                for (i in 0 until GlobalVars.paymentTerms!!.size) {
                    if (contract!!.paymentTermsID == GlobalVars.paymentTerms!![i].ID) {
                        binding.paymentTermsSpinner.setSelection(i)
                        break
                    }
                }

            }


            binding.salesRepSearch.setQuery(contract!!.repName, false)
            binding.salesRepSearchRv.visibility = View.INVISIBLE

            binding.notesEt.setText(contract!!.notes)
        }

        editsMade = false

    }

    private fun validateFields(): Boolean {



        if (contract!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        if (contract!!.title.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_wo_set_title))
            return false
        }

        //Skipping charge type and payment terms checking here since droid spinners can't be empty so it defaults to the first option

        if (contract!!.salesRep.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_contract_select_sales_rep))
            return false
        }

        return true
    }

    private fun updateContract() {
        println("updateContract")
        showProgressView()

        contract!!.notes = binding.notesEt.text.toString()
        println("Contract Title: ${contract!!.title}")
        contract!!.title = binding.titleEditText.text.toString()
        println("Contract Title: ${contract!!.title}")
        if (contract!!.total.isNullOrBlank()) {
            contract!!.total = "0"
        }

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contract.php"

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
                        val newContractID: String = gson.fromJson(parentObject["contractID"].toString(), String::class.java)
                        contract!!.ID = newContractID
                        globalVars.playSaveSound(myView.context)
                        editsMade = false


                        if (editMode) {
                            myView.findNavController().navigateUp()
                        } else {
                            val directions = NewEditContractFragmentDirections.navigateToContract(contract!!.ID)
                            myView.findNavController().navigate(directions)
                        }
                    }
                    hideProgressView()


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
                params["contractID"] = contract!!.ID
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["customer"] = contract!!.customer.toString()
                params["salesRep"] = contract!!.salesRep.toString()
                params["chargeType"] = contract!!.chargeType.toString()
                params["status"] = contract!!.status
                params["total"] = contract!!.total.toString()
                params["notes"] = contract!!.notes.toString()
                params["customerName"] = contract!!.custName.toString()
                params["title"] = contract!!.title
                params["paymentTermsID"] = contract!!.paymentTermsID.toString()



                params["repName"] = contract!!.repName.toString()


                //params["companySigned"] = contract!!.repSignature.toString()
                //params["customerSigned"] = contract!!.customerSignature.toString()
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                println("params = $params")
                return params


                /*
                if self.contract.lead != nil{
                    "contractID": self.contract.ID,
                    "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId),
                    "customer": self.contract.customerID!,
                    "salesRep": rep,
                    "chargeType": contract.chargeType! ,
                    "status": self.contract.status,
                    "total":self.contract.total!,
                    "notes":notes, "repName":repName,
                    "customerName":customerName,
                    "title":self.contract.title,
                    "companySigned":self.contract.repSignature!,
                    "customerSigned":self.contract.customerSignature!,
                    "leadID":self.contract.lead!.ID,
                    "paymentTermsID":self.contract.paymentTerms!,
                    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!] as! [String : String]
                }else{
                    "contractID": self.contract.ID,
                    "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId),
                    "customer": self.contract.customerID!,
                    "salesRep": rep,
                    "chargeType": contract.chargeType ,
                    "status": self.contract.status,
                    "total":self.contract.total,
                    "notes":notes,
                    "repName":repName,
                    "customerName":customerName,
                    "title":self.contract.title,
                    "companySigned":self.contract.repSignature,
                    "customerSigned":self.contract.customerSignature,
                    "paymentTermsID":self.contract.paymentTerms!,
                    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!] as! [String : String]
                }

                 */


            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.contract_status_new)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.contract_status_sent)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_awarded)!!,myView.context), myView.context.getString(R.string.contract_status_awarded)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.contract_status_scheduled)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.declined)))
        popUp.menu.add(0, 5, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_waiting)!!,myView.context), myView.context.getString(R.string.waiting)))
        popUp.menu.add(0, 6, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))

        popUp.setOnMenuItemClickListener { item: MenuItem? ->
            contract!!.status = item?.itemId.toString()
            setStatusIcon()

            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun setStatusIcon() {
        when (contract!!.status) {
            "0" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            "1" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            "2" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_awarded)
            "3" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_done)
            "4" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            "5" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_waiting)
            "6" -> binding.statusBtn.setBackgroundResource(R.drawable.ic_canceled)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true
        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                val positionPlus1 = position+1
                contract!!.chargeType = positionPlus1.toString()
            }

            R.id.payment_terms_spinner -> {
                contract!!.paymentTermsID = GlobalVars.paymentTerms?.get(position)?.ID.toString()
                println("Selected Payment terms ID: ${GlobalVars.paymentTerms?.get(position)?.ID.toString()}")
            }
        }

    }

    override fun onCustomerCellClickListener(data: Customer) {
        editsMade = true
        contract!!.customer = data.ID
        contract!!.custName = data.sysname
        binding.customerSearch.setQuery(contract!!.custName, false)
        binding.customerSearch.clearFocus()
        myView.hideKeyboard()
    }

    override fun onEmployeeCellClickListener(data: Employee) {
        editsMade = true
        contract!!.salesRep = data.ID
        contract!!.repName = data.name
        binding.salesRepSearch.setQuery(contract!!.custName, false)
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