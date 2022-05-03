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
import android.widget.TextView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.equipment_list_item.*
import kotlinx.android.synthetic.main.fragment_stack.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.util.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

lateinit var stackView:View
         var type:Int = 1
         var ID:String = ""
lateinit var delegate:StackDelegate

lateinit var leadTxt:TextView
lateinit var contractTxt:TextView
lateinit var workOrderTxt:TextView
lateinit var invoiceTxt:TextView

lateinit var leadSpinner:Spinner
lateinit var contractSpinner:Spinner
lateinit var workOrderSpinner:Spinner
lateinit var invoiceSpinner:Spinner



var leadsList:MutableList<Lead>? = null
var contractsList:MutableList<Contract>? = null
var workOrdersList:MutableList<WorkOrder>? = null
var invoicesList:MutableList<Invoice>? = null


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



class StackFragment(val _type: Int, val _ID: String, _delegate: StackDelegate) : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        println("onCreateView")
        stackView = inflater.inflate(R.layout.fragment_stack, container, false)
        return stackView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Stack View")

       leadSpinner = stackView.findViewById(R.id.lead_spinner)
        contractSpinner = stackView.findViewById(R.id.contract_spinner)
        workOrderSpinner = stackView.findViewById(R.id.work_order_spinner)
        invoiceSpinner = stackView.findViewById(R.id.invoice_spinner)

        leadTxt = stackView.findViewById(R.id.lead_txt)
        contractTxt = stackView.findViewById(R.id.contract_txt)
        workOrderTxt = stackView.findViewById(R.id.work_order_txt)
        invoiceTxt = stackView.findViewById(R.id.invoice_txt)

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

    fun getWorkOrderSpinner():Spinner{
        println("getWorkOrderSpinner")
        return workOrderSpinner
    }


    @SuppressLint("ResourceType")
    fun getStack()
    {
        //print("get stack")
        //type = _type


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/systemStack.php"

        val params: MutableMap<String, String> = HashMap()




        when(type) {
            0 -> {
                println("stack leads")

                leadTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))
                //leadSpinner.te
                //leadSpinner.setBackgroundColor(Color.parseColor(resources.getString(R.color.button.toString())))

                 params["leadID"] = ID

            }
            1 -> {
                println("stack contracts")
                contractTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))

                 params["contractID"] = ID

            }
            2 -> {
                println("stack work orders")
                workOrderTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))

                params["workOrderID"] = ID

            }
            3 -> {
                println("stack invoices")
                invoiceTxt.setBackgroundColor(Color.parseColor(resources.getString(R.color.button)))
                 params["invoiceID"] = ID
            }
        }


        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    if (isResumed) {
                        val gson = GsonBuilder().create()


                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")


                        val leadsJSON: JSONArray = parentObject.getJSONArray("leads")
                        println("leadsJSON = $leadsJSON")
                        println("leadsJSON count = ${leadsJSON.length()}")
                        leadsList = gson.fromJson(leadsJSON.toString() , Array<Lead>::class.java).toMutableList()

                        val dummyLead = Lead()
                        dummyLead.description = "Select Leads"
                        leadsList!!.add(0,dummyLead)

                        leadTxt.text = getString(R.string.leads_amount, leadsJSON.length())

                        val contractsJSON: JSONArray = parentObject.getJSONArray("contracts")
                        println("contractsJSON = $contractsJSON")
                        println("contractsJSON count = ${contractsJSON.length()}")
                        contractsList = gson.fromJson(contractsJSON.toString() , Array<Contract>::class.java).toMutableList()
                        //leadsList.add(0,"Leads(${leadsList.count()})")

                        val dummyContract = Contract()
                        dummyContract.title = "Select Contracts"
                        contractsList!!.add(0,dummyContract)

                        contractTxt.text = getString(R.string.contracts_amount, contractsJSON.length())



                        val workOrdersJSON: JSONArray = parentObject.getJSONArray("workOrders")
                        println("workOrdersJSON = $workOrdersJSON")
                        println("workOrdersJSON count = ${workOrdersJSON.length()}")
                        workOrdersList = gson.fromJson(workOrdersJSON.toString() , Array<WorkOrder>::class.java).toMutableList()
                        val dummyWorkOrder = WorkOrder()
                        dummyWorkOrder.title = "Select WorkOrders"

                        workOrdersList!!.add(0,dummyWorkOrder)

                       // leadsList.add(0,"Leads(${leadsList.count()})")
                        workOrderTxt.text = getString(R.string.work_orders_amount, workOrdersJSON.length())

                        val invoicesJSON: JSONArray = parentObject.getJSONArray("invoices")
                        println("invoicesJSON = $invoicesJSON")

                        println("invoicesJSON count = ${invoicesJSON.length()}")
                        invoicesList = gson.fromJson(invoicesJSON.toString() , Array<Invoice>::class.java).toMutableList()
                        //leadsList.add(0,"Leads(${leadsList.count()})")

                        val dummyInvoice = Invoice()
                        dummyInvoice.title = "Select Invoices"
                        invoicesList!!.add(0,dummyInvoice)

                        invoiceTxt.text = getString(R.string.invoices_amount, invoicesJSON.length())


                        val leadAdapter: ArrayAdapter<Lead> = ArrayAdapter<Lead>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, leadsList!!
                        )
                        leadAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        leadSpinner.adapter = leadAdapter
                       /// leadSpinner.setSpinnerText("Leads(${leadsList!!.count()})")

                        val contractAdapter: ArrayAdapter<Contract> = ArrayAdapter<Contract>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, contractsList!!
                        )
                        contractAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        contractSpinner.adapter = contractAdapter
                        //contractSpinner.setSpinnerText("Contracts(${contractsList!!.count()})")

                        val workOrderAdapter: ArrayAdapter<WorkOrder> = ArrayAdapter<WorkOrder>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, workOrdersList!!
                        )
                        workOrderAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        workOrderSpinner.adapter = workOrderAdapter

                        val invoiceAdapter: ArrayAdapter<Invoice> = ArrayAdapter<Invoice>(
                            myView.context,
                            android.R.layout.simple_spinner_dropdown_item, invoicesList!!
                        )
                        invoiceAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
                        invoiceSpinner.adapter = invoiceAdapter



                    //Listeners

                        //leads
                        if (leadsJSON.length() > 0) {
                            leadSpinner.setSelection(0, false)

                            if (leadsJSON.length() == 1) {

                                leadTxt.setOnClickListener {
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
                                leadSpinner.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0){


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

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            leadSpinner.isEnabled = false
                            leadSpinner.isClickable = false
                        }

                        //contracts
                        if (contractsJSON.length() > 0) {
                            contractSpinner.setSelection(0, false)

                            if (contractsJSON.length() == 1) {

                                contractTxt.setOnClickListener {
                                    // your code to run when the user clicks on the TextView
                                    println("contractSpinner click")
                                    val contract: Contract = gson.fromJson(
                                        contractsJSON[0].toString(),
                                        Contract::class.java
                                    )
                                    println("contract = $contract")

                                    delegate.newContractView(contract)
                                }

                            } else {
                                contractSpinner.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0){
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

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            contractSpinner.isEnabled = false
                            contractSpinner.isClickable = false
                        }


                        //work orders
                        if (workOrdersJSON.length() > 0) {
                            workOrderSpinner.setSelection(0, false)

                            if (workOrdersJSON.length() == 1) {

                                workOrderTxt.setOnClickListener {
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
                                workOrderSpinner.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0){
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

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            workOrderSpinner.isEnabled = false
                            workOrderSpinner.isClickable = false
                        }

                        //invoices
                        if (invoicesJSON.length() > 0) {
                            invoiceSpinner.setSelection(0, false)

                            if (invoicesJSON.length() == 1) {

                                invoiceTxt.setOnClickListener {
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
                                invoiceSpinner.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long
                                        ) {
                                            println("onItemSelected position = $position id = $id")

                                            if (position != 0){

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

                        }else{
                            //leadTxt.isEnabled = false
                            //leadTxt.isClickable = false
                            invoiceSpinner.isEnabled = false
                            invoiceSpinner.isClickable = false
                        }




                       // delegate.setListeners()
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

                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey


                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)




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