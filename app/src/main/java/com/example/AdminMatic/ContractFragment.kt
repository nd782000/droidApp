package com.example.AdminMatic

import android.opengl.Visibility
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContractFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContractFragment : Fragment(), StackDelegate {
    // TODO: Rename and change types of parameters
    private var param2: String? = null

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

    private lateinit var  stackFragment: StackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable("contract")
            param2 = it.getString(ARG_PARAM2)
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

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contract)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Contract View")

        println("employee = ${contract!!.title}")


        pgsBar = view.findViewById(R.id.progress_bar)
        pgsBar.visibility = View.INVISIBLE

        stackFragment = StackFragment(1,contract!!.ID,this)

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
        paymentTv.text = contract!!.paymentTermsID
        salesRepTv.text =contract!!.repName
        notesTv.text = contract!!.notes
        notesTv.movementMethod = ScrollingMovementMethod()
        priceTv.text = getString(R.string.dollar_sign, contract!!.total)

        setStatus(contract!!.status)

    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1,globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_not_started)!!,myView.context)!!, myView.context.getString(R.string.new_contract)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_in_progress)!!,myView.context)!!, myView.context.getString(R.string.sent)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_awarded)!!,myView.context)!!, myView.context.getString(R.string.awarded)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_done)!!,myView.context)!!, myView.context.getString(R.string.scheduled)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_canceled)!!,myView.context)!!, myView.context.getString(R.string.declined)))
        popUp.menu.add(0, 5, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_waiting)!!,myView.context)!!, myView.context.getString(R.string.waiting)))
        popUp.menu.add(0, 6, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_canceled)!!,myView.context)!!, myView.context.getString(R.string.canceled)))
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
            val queue = Volley.newRequestQueue(com.example.AdminMatic.myView.context)


            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    try {
                        if (isResumed) {
                            val parentObject = JSONObject(response)
                            println("parentObject = $parentObject")

                            hideProgressView()
                        }

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
            queue.add(postRequest1)
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
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }


    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")


       // val directions = ContractFragmentDirections.navigateContractToLead(_lead)
      //  myView.findNavController().navigate(directions)

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