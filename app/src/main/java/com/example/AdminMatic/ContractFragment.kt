package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


interface ContractItemCellClickListener {
    fun onContractItemCellClickListener(data:ContractItem)
}
interface AddContractItemButtonListener {
    fun onAddContractItemButtonListener()
}


class ContractFragment : Fragment(), StackDelegate, ContractItemCellClickListener, AddContractItemButtonListener {

    private  var contract: Contract? = null


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    private lateinit var customerBtn: Button
    private lateinit var statusBtn: ImageButton
    private lateinit var titleTv: TextView
    private lateinit var chargeTv: TextView
    private lateinit var paymentTv: TextView
    private lateinit var salesRepTv: TextView
    private lateinit var notesTv: TextView
    private lateinit var priceTv: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var custLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var dataLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var footerLayout: LinearLayout


    private lateinit var  stackFragment: StackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable("contract")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_contract, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_contract, container, false)

        globalVars = GlobalVars()

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract_number, contract!!.ID)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract View")

        println("employee = ${contract!!.title}")




        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE

        stackFragment = StackFragment(1,contract!!.ID,this)

        //TODO: Figure out how to hide and show the stack view here during progress bar
        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.contract_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

        statusBtn = myView.findViewById(R.id.contract_status_btn)
        statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        customerBtn = myView.findViewById(R.id.contract_customer_btn)
        customerBtn.text = getString(R.string.contract_customer_button_text, contract!!.custName, contract!!.addr)
        customerBtn.setOnClickListener{
            println("customer btn clicked")
            val customer = Customer(contract!!.customer!!)
            val directions = ContractFragmentDirections.navigateContractToCustomer(customer.ID)
            myView.findNavController().navigate(directions)

        }

        titleTv = myView.findViewById(R.id.contract_title_val_tv)
        chargeTv = myView.findViewById(R.id.contract_charge_val_tv)
        paymentTv = myView.findViewById(R.id.contract_payment_val_tv)
        salesRepTv = myView.findViewById(R.id.contract_sales_rep_val_tv)
        notesTv = myView.findViewById(R.id.contract_notes_val_tv)
        priceTv = myView.findViewById(R.id.contract_price_tv)
        recycler = myView.findViewById(R.id.contract_item_rv)


        //Stuff to hide with progress bar
        custLayout = myView.findViewById(R.id.contract_status_cust_cl)
        dataLayout = myView.findViewById(R.id.contract_data_cl)
        footerLayout = myView.findViewById(R.id.contract_footer_cl)


        val chargeName:String = when (contract!!.chargeType) {
            "1" -> {
                getString(R.string.contract_charge_nc)
            }
            "2" -> {
                getString(R.string.contract_charge_fl)
            }
            "3" -> {
                getString(R.string.contract_charge_tm)
            }
            else -> {
                ""
            }
        }

        titleTv.text = contract!!.title
        chargeTv.text = chargeName

        when (contract!!.paymentTermsID) {
            "0" -> {
                paymentTv.text = getString(R.string.contract_payment_none)
            }
            "3" -> {
                paymentTv.text = getString(R.string.contract_payment_2percent10net30)
            }
            "4" -> {
                paymentTv.text = getString(R.string.contract_payment_prepay)
            }
            "6" -> {
                paymentTv.text = getString(R.string.contract_payment_due_on_receipt)
            }
            "7" -> {
                paymentTv.text = getString(R.string.contract_payment_net_15)
            }
            "8" -> {
                paymentTv.text = getString(R.string.contract_payment_net_30)
            }
            "9" -> {
                paymentTv.text = getString(R.string.contract_payment_net_60)
            }
            else -> {
                paymentTv.text = contract!!.paymentTermsID
            }
        }

        salesRepTv.text =contract!!.repName
        notesTv.text = contract!!.notes
        notesTv.movementMethod = ScrollingMovementMethod()
        //Todo: maybe find a way to add the comma to money numbers without having to cast to a double
        // We also need to consider how this will be handled for other regions with other forms of currency
        priceTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(contract!!.total!!.toDouble()))



        setStatus(contract!!.status)

        // get items
        getContract()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contract")
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        //popUp.menu.add(0, 0, 1,globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_not_started)!!,myView.context)!!, myView.context.getString(R.string.new_contract)))
        popUp.menu.add(0, 0, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.new_contract)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.sent)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_awarded)!!,myView.context), myView.context.getString(R.string.awarded)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.scheduled)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.declined)))
        popUp.menu.add(0, 5, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_waiting)!!,myView.context), myView.context.getString(R.string.waiting)))
        popUp.menu.add(0, 6, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            contract!!.status = item!!.itemId.toString()

            /*
            setStatus(contract!!.status)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
             */

            showProgressView()

            var urlString = "https://www.adminmatic.com/cp/app/functions/update/contract.php"

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

                        hideProgressView()

                        /* Here 'response' is a String containing the response you received from the website... */
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
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["contractID"] = contract!!.ID
                    //params["createdBy"] = contract!!.createdBy
                    //params["customer"] = contract!!.customer!!
                    //params["salesRep"] = contract!!.salesRep!!
                    params["chargeType"] = contract!!.chargeType!!
                    params["paymentTerms"] = contract!!.paymentTermsID!!
                    params["status"] = contract!!.status
                    //params["total"] = contract!!.total!!
                    params["notes"] = contract!!.notes!!
                    params["repName"] = contract!!.repName!!
                    params["customerName"] = contract!!.custName!!
                    params["title"] = contract!!.title
                    //params["companySigned"] = contract!!.repSignature!!
                    //params["customerSigned"] = contract!!.customerSigned!!
                    println("params = $params")
                    return params


                    /* params from iOS
                    "contractID":self.contract.ID,
                    "createdBy":self.contract.createdBy,
                    "customer":self.contract.customerID!,
                    "salesRep":self.contract.salesRep!,
                    "chargeType":self.contract.chargeType!,
                    "paymentTerms:":self.contract.paymentTerms!,
                    "status":self.contract.status,
                    "total":self.contract.total!,
                    "notes":self.contract.notes!,
                    "repName":self.contract.repName!,
                    "customerName":self.contract.customerName!,
                    "title":self.contract.title,
                    "companySigned":self.contract.repSignature!,
                    "customerSigned":self.contract.customerSignature!,
                     */

                }
            }
            postRequest1.tag = "contract"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun setStatus(status: String) {
        println("setStatus")
        println(contract!!.status)
        println(contract!!.statusName)
        when(status) {
            "0" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "1" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "2" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_awarded)
            }
            "3" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "4" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
            "5" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_waiting)
            }
            "6" -> {
                statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        custLayout.visibility = View.INVISIBLE
        dataLayout.visibility = View.INVISIBLE
        footerLayout.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        custLayout.visibility = View.VISIBLE
        dataLayout.visibility = View.VISIBLE
        footerLayout.visibility = View.VISIBLE
    }


    private fun getContract(){
        println("getCustomer = ${contract!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/contract.php"

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
                    val contractNew = gson.fromJson(parentObject.toString() , Contract::class.java)

                    contract = contractNew

                    recycler.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            ContractItemAdapter(
                                contract!!.items!!.toMutableList(),
                                context,
                                this@ContractFragment,
                                this@ContractFragment
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        recycler.addItemDecoration(itemDecoration)
                        (adapter as ContractItemAdapter).notifyDataSetChanged()
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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["contractID"] = contract!!.ID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "contract"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onContractItemCellClickListener(data:ContractItem) {
        val directions = ContractFragmentDirections.navigateContractToContractItem(data, false)
        myView.findNavController().navigate(directions)
    }

    override fun onAddContractItemButtonListener() {

        val blankContractItem = ContractItem("0", "", "2", "0", contract!!.ID, "0")
        val directions = ContractFragmentDirections.navigateContractToContractItem(blankContractItem, true)

        if (contract!!.status == "1" || contract!!.status == "2" || contract!!.status == "3" || contract!!.status == "4") {
            val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
            builder.setTitle(getString(R.string.dialogue_add_contract_item_title))
            builder.setMessage(getString(R.string.dialogue_add_contract_item_body))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                myView.findNavController().navigate(directions)
            }
            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }
            builder.show()
        }
        else {
            myView.findNavController().navigate(directions)
        }





    }





    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        if (GlobalVars.permissions!!.leads == "1") {
            val directions = ContractFragmentDirections.navigateContractToLead(_lead)
            myView.findNavController().navigate(directions)
        }
        else {
            globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_leads))
        }

    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
       // val directions = ContractFragmentDirections.na(_lead)
       // myView.findNavController().navigate(directions)

        contract = _contract

    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")

        //val directions = ContractFragmentDirections.navigateContractToWorkOrder(_workOrder)
       // myView.findNavController().navigate(directions)

    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")

       // val directions = ContractFragmentDirections.navigateContractToInvoice(_invoice)
       // myView.findNavController().navigate(directions)
    }
}