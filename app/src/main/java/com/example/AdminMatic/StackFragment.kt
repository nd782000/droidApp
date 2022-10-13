package com.example.AdminMatic

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentStackBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


//Todo: more memory leak warnings, investigate how to refactor
lateinit var stackView:View
         var type:Int = 1
         var ID:String = ""
lateinit var delegate:StackDelegate

/*
lateinit var leadTxt:TextView
lateinit var contractTxt:TextView
lateinit var workOrderTxt:TextView
lateinit var invoiceTxt:TextView

lateinit var leadSpinner:Spinner
lateinit var contractSpinner:Spinner
lateinit var workOrderSpinner:Spinner
lateinit var invoiceSpinner:Spinner

 */

lateinit  var globalVars:GlobalVars


var leadsList:MutableList<Lead>? = null
var contractsList:MutableList<Contract>? = null
var workOrdersList:MutableList<WorkOrder>? = null
var invoicesList:MutableList<Invoice>? = null


/*
var datesArray:Array<String> = arrayOf(
    "All Dates (${GlobalVars.loggedInEmployee!!.fName})",
    "All Dates (Everyone)",
    "Today (${GlobalVars.loggedInEmployee!!.fName})",
    "Today (Everyone)",
    "Tomorrow (${GlobalVars.loggedInEmployee!!.fName})",
    "Tomorrow (Everyone)",
    "This Week (${GlobalVars.loggedInEmployee!!.fName})",
    "This Week (Everyone)",
    "Next Week (${GlobalVars.loggedInEmployee!!.fName})",
    "Next Week (Everyone)",
    "Next 14 Days (${GlobalVars.loggedInEmployee!!.fName})",
    "Next 14 Days (Everyone)",
    "Next 30 Days (${GlobalVars.loggedInEmployee!!.fName})",
    "Next 30 Days (Everyone)",
    "This Year (${GlobalVars.loggedInEmployee!!.fName})",
    "This Year (Everyone)")

 */



class StackFragment(_type: Int, _ID: String, _delegate: StackDelegate) : Fragment(){



    /*constructor(_type: Int, _ID: String):{

        type = _type
        ID = _ID
    }
    */


    // initializer block
    init {
        type = _type
        ID = _ID
        delegate = _delegate

        println("stack init")
        println("type = $type")
        println("ID = $ID")
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     */

    private var _binding: FragmentStackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentStackBinding.inflate(inflater, container, false)
        stackView = binding.root
        return stackView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Stack View")

        /*
       leadSpinner = stackView.findViewById(R.id.lead_spinner)
        contractSpinner = stackView.findViewById(R.id.contract_spinner)
        workOrderSpinner = stackView.findViewById(R.id.work_order_spinner)
        invoiceSpinner = stackView.findViewById(R.id.invoice_spinner)

        leadTxt = stackView.findViewById(R.id.lead_txt)
        contractTxt = stackView.findViewById(R.id.contract_txt)
        workOrderTxt = stackView.findViewById(R.id.work_order_txt)
        invoiceTxt = stackView.findViewById(R.id.invoice_txt)

         */

/*
        workOrderSpinner.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                println("onItemClickListener")
                println(parent!!.getItemAtPosition(position).toString())
            }
        }

        */



        /*
        workOrderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                println("onItemSelected")
                println(parent.getItemAtPosition(position).toString())

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }
        */

        getStack()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("stack")
    }

    fun getWorkOrderSpinner():Spinner{
        println("getWorkOrderSpinner")
        return binding.workOrderSpinner
    }


    @SuppressLint("ResourceType")
    fun getStack()
    {
        //print("get stack")
        //type = _type


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/systemStack.php"

        val params: MutableMap<String, String> = HashMap()




        when(type) {
            0 -> {
                println("stack leads")

                binding.leadTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))
                //leadSpinner.te
                //leadSpinner.setBackgroundColor(Color.parseColor(resources.getString(R.color.button.toString())))

                 params["leadID"] = ID

            }
            1 -> {
                println("stack contracts")
                binding.contractTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))

                 params["contractID"] = ID

            }
            2 -> {
                println("stack work orders")
                binding.workOrderTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))

                params["workOrderID"] = ID

            }
            3 -> {
                println("stack invoices")
                binding.invoiceTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))
                 params["invoiceID"] = ID
            }
        }


        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val leadsJSON: JSONArray = parentObject.getJSONArray("leads")
                        println("leadsJSON = $leadsJSON")
                        println("leadsJSON count = ${leadsJSON.length()}")
                        leadsList = gson.fromJson(leadsJSON.toString() , Array<Lead>::class.java).toMutableList()

                        val dummyLead = Lead()
                        dummyLead.description = "Select Leads"
                        leadsList!!.add(0,dummyLead)

                        binding.leadTxt.text = getString(R.string.leads_amount, leadsJSON.length())

                        val contractsJSON: JSONArray = parentObject.getJSONArray("contracts")
                        println("contractsJSON = $contractsJSON")
                        println("contractsJSON count = ${contractsJSON.length()}")
                        contractsList = gson.fromJson(contractsJSON.toString() , Array<Contract>::class.java).toMutableList()
                        //leadsList.add(0,"Leads(${leadsList.count()})")

                        val dummyContract = Contract()
                        dummyContract.title = "Select Contracts"
                        contractsList!!.add(0,dummyContract)

                        binding.contractTxt.text = getString(R.string.contracts_amount, contractsJSON.length())



                        val workOrdersJSON: JSONArray = parentObject.getJSONArray("workOrders")
                        println("workOrdersJSON = $workOrdersJSON")
                        println("workOrdersJSON count = ${workOrdersJSON.length()}")
                        workOrdersList = gson.fromJson(workOrdersJSON.toString() , Array<WorkOrder>::class.java).toMutableList()
                        val dummyWorkOrder = WorkOrder()
                        dummyWorkOrder.title = "Select WorkOrders"

                        workOrdersList!!.add(0,dummyWorkOrder)

                       // leadsList.add(0,"Leads(${leadsList.count()})")
                        binding.workOrderTxt.text = getString(R.string.work_orders_amount, workOrdersJSON.length())

                        val invoicesJSON: JSONArray = parentObject.getJSONArray("invoices")
                        println("invoicesJSON = $invoicesJSON")

                        println("invoicesJSON count = ${invoicesJSON.length()}")
                        invoicesList = gson.fromJson(invoicesJSON.toString() , Array<Invoice>::class.java).toMutableList()
                        //leadsList.add(0,"Leads(${leadsList.count()})")

                        val dummyInvoice = Invoice()
                        dummyInvoice.title = "Select Invoices"
                        invoicesList!!.add(0,dummyInvoice)

                        binding.invoiceTxt.text = getString(R.string.invoices_amount, invoicesJSON.length())


                        val leadAdapter: ArrayAdapter<Lead> = ArrayAdapter<Lead>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, leadsList!!
                        )
                        leadAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        binding.leadSpinner.adapter = leadAdapter
                       /// leadSpinner.setSpinnerText("Leads(${leadsList!!.count()})")

                        val contractAdapter: ArrayAdapter<Contract> = ArrayAdapter<Contract>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, contractsList!!
                        )
                        contractAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        binding.contractSpinner.adapter = contractAdapter
                        //contractSpinner.setSpinnerText("Contracts(${contractsList!!.count()})")

                        val workOrderAdapter: ArrayAdapter<WorkOrder> = ArrayAdapter<WorkOrder>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, workOrdersList!!
                        )
                        workOrderAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        binding.workOrderSpinner.adapter = workOrderAdapter

                        val invoiceAdapter: ArrayAdapter<Invoice> = ArrayAdapter<Invoice>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, invoicesList!!
                        )
                        invoiceAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        binding.invoiceSpinner.adapter = invoiceAdapter



                    //Listeners

                        //leads



                        if (leadsJSON.length() > 0) {

                            if (GlobalVars.permissions!!.leads == "1") {

                                binding.leadSpinner.setSelection(0, false)

                                if (leadsJSON.length() == 1) {

                                    binding.leadTxt.setOnClickListener {
                                        // your code to run when the user clicks on the TextView
                                        println("leadSpinner click")
                                        val lead: Lead = gson.fromJson(
                                            leadsJSON[0].toString(),
                                            Lead::class.java
                                        )
                                        println("lead = $lead")

                                        delegate.newLeadView(lead)
                                    }

                                } else {
                                    binding.leadSpinner.onItemSelectedListener =
                                        object : AdapterView.OnItemSelectedListener {
                                            override fun onItemSelected(
                                                parent: AdapterView<*>,
                                                view: View,
                                                position: Int,
                                                id: Long
                                            ) {
                                                println("onItemSelected position = $position id = $id")

                                                if (position != 0) {


                                                    val lead: Lead = gson.fromJson(
                                                        leadsJSON[position - 1].toString(),
                                                        Lead::class.java
                                                    )
                                                    println("lead = $lead")


                                                    delegate.newLeadView(lead)
                                                }
                                            }

                                            override fun onNothingSelected(parent: AdapterView<*>) {

                                            }
                                        }
                                }
                            }
                            else {
                                binding.leadTxt.setOnClickListener {
                                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_leads))
                                }
                            }

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            binding.leadSpinner.isEnabled = false
                            binding.leadSpinner.isClickable = false
                        }

                        //contracts
                        if (contractsJSON.length() > 0) {

                            if (GlobalVars.permissions!!.contracts == "1") {

                                binding.contractSpinner.setSelection(0, false)

                                if (contractsJSON.length() == 1) {

                                    binding.contractTxt.setOnClickListener {
                                        // your code to run when the user clicks on the TextView
                                        println("contractSpinner click")
                                        val contract: Contract = gson.fromJson(
                                            contractsJSON[0].toString(),
                                            Contract::class.java
                                        )
                                        println("contract = $contract")

                                        delegate.newContractView(contract)
                                    }

                                }
                                else {
                                    binding.contractSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0) {
                                                // var contract:WorkOrder = gson.fromJson(contractsJSON[position],WorkOrder)

                                                val contract: Contract = gson.fromJson(
                                                    contractsJSON[position - 1].toString(),
                                                    Contract::class.java
                                                )
                                                println("contract = $contract")


                                                delegate.newContractView(contract)
                                            }
                                        }

                                        override fun onNothingSelected(parent: AdapterView<*>) {

                                        }
                                    }
                                }
                            }
                            else {
                                binding.contractTxt.setOnClickListener {
                                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_contracts))
                                }
                            }

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            binding.contractSpinner.isEnabled = false
                            binding.contractSpinner.isClickable = false
                        }


                        //work orders
                        if (workOrdersJSON.length() > 0) {

                            if (GlobalVars.permissions!!.schedule == "1") {

                                binding.workOrderSpinner.setSelection(0, false)

                                if (workOrdersJSON.length() == 1) {

                                    binding.workOrderTxt.setOnClickListener {
                                        // your code to run when the user clicks on the TextView
                                        println("workOrderSpinner click")
                                        val workOrder: WorkOrder = gson.fromJson(
                                            workOrdersJSON[0].toString(),
                                            WorkOrder::class.java
                                        )
                                        println("workOrder = $workOrder")

                                        delegate.newWorkOrderView(workOrder)
                                    }

                                } else {
                                    binding.workOrderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0) {
                                                // var workOrder:WorkOrder = gson.fromJson(workOrdersJSON[position],WorkOrder)

                                                val workOrder: WorkOrder = gson.fromJson(
                                                    workOrdersJSON[position - 1].toString(),
                                                    WorkOrder::class.java
                                                )
                                                println("workOrder = $workOrder")


                                                delegate.newWorkOrderView(workOrder)
                                            }
                                        }

                                        override fun onNothingSelected(parent: AdapterView<*>) {

                                        }
                                    }
                                }
                            }
                            else {
                                binding.workOrderTxt.setOnClickListener {
                                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule))
                                }
                            }

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            binding.workOrderSpinner.isEnabled = false
                            binding.workOrderSpinner.isClickable = false
                        }

                        //invoices
                        if (invoicesJSON.length() > 0) {

                            if (GlobalVars.permissions!!.invoices == "1") {
                                binding.invoiceSpinner.setSelection(0, false)

                                if (invoicesJSON.length() == 1) {

                                    binding.invoiceTxt.setOnClickListener {
                                        // your code to run when the user clicks on the TextView
                                        println("invoiceSpinner click")
                                        val invoice: Invoice = gson.fromJson(
                                            invoicesJSON[0].toString(),
                                            Invoice::class.java
                                        )
                                        println("invoice = $invoice")

                                        delegate.newInvoiceView(invoice)
                                    }

                                } else {
                                    binding.invoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0) {

                                                val invoice: Invoice = gson.fromJson(
                                                    invoicesJSON[position - 1].toString(),
                                                    Invoice::class.java
                                                )
                                                println("invoice = $invoice")


                                                delegate.newInvoiceView(invoice)
                                            }
                                        }

                                        override fun onNothingSelected(parent: AdapterView<*>) {

                                        }
                                    }
                                }
                            }
                            else {
                                binding.invoiceTxt.setOnClickListener {
                                    globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices))
                                }
                            }

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            binding.invoiceSpinner.isEnabled = false
                            binding.invoiceSpinner.isClickable = false
                        }
                    }



                   // delegate.setListeners()




                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {

                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "stack"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    /*
    fun setSpinnerListners(){
        println("setSpinnerListeners")

        println("workOrderSpinner = $workOrderSpinner")

        leadSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                delegate.newLeadView(leadsList!![position])
            }

        }

        contractSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                delegate.newContractView(contractsList!![position])
            }

        }

       // workOrderSpinner = stackView.findViewById(R.id.work_order_spinner)
        workOrderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("onNothingSelected for workOrder")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                println("onItemSelected for workOrder")
                delegate.newWorkOrderView(workOrdersList!![position])
            }

        }

        invoice_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                delegate.newInvoiceView(invoicesList!![position])
            }

        }


    }
*/




}