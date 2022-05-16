package com.example.AdminMatic

import android.opengl.Visibility
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


interface WoItemCellClickListener {
    fun onWoItemCellClickListener(data:WoItem)
}

class WorkOrderFragment : Fragment(), StackDelegate, WoItemCellClickListener{
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    private var param2: String? = null

    private  var workOrder: WorkOrder? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var context: AppCompatActivity


    lateinit var  pgsBar: ProgressBar

    private lateinit var  stackFragment: StackFragment

    private lateinit var statusBtn:ImageButton
    private lateinit var customerBtn:Button

    lateinit var titleTxt:TextView
    lateinit var scheduleTxt:TextView
    //lateinit var deptTxt:TextView
    lateinit var crewTxt:TextView
    lateinit var chargeTxt:TextView
    lateinit var repTxt:TextView

    private lateinit var priceTxt:TextView
    private lateinit var costTxt:TextView
    private lateinit var profitTxt:TextView
    private lateinit var profitPercentTxt:TextView
    private lateinit var profitBar:ProgressBar

    private lateinit var statusCustCL:ConstraintLayout
    private lateinit var dataCL:ConstraintLayout
    private lateinit var footerCL:ConstraintLayout
    private lateinit var headerCL:ConstraintLayout

    lateinit var itemRecyclerView:RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workOrder = it.getParcelable("workOrder")
            param2 = it.getString(ARG_PARAM2)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()




        myView = inflater.inflate(R.layout.fragment_work_order, container, false)


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = "WorkOrder #${workOrder!!.woID}"

        // Inflate the layout for this fragment
        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("WorkOrder View")


        pgsBar = myView.findViewById(R.id.progress_bar)

         stackFragment = StackFragment(2,workOrder!!.woID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.work_order_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()

        statusBtn = myView.findViewById(R.id.status_btn)
        statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        customerBtn = myView.findViewById(R.id.customer_btn)
        customerBtn.text = "${workOrder!!.custName} ${workOrder!!.custAddress}"
        customerBtn.setOnClickListener{
            println("customer btn clicked")


            val customer = Customer(workOrder!!.customer!!)
            val directions = WorkOrderFragmentDirections.navigateWorkOrderToCustomer(customer.ID)
            myView.findNavController().navigate(directions)


        }

        titleTxt = myView.findViewById(R.id.title_val_tv)
        scheduleTxt = myView.findViewById(R.id.schedule_val_tv)
        //deptTxt = myView.findViewById(R.id.dept_val_tv)
        crewTxt = myView.findViewById(R.id.crew_val_tv)
        chargeTxt = myView.findViewById(R.id.charge_val_tv)
        repTxt = myView.findViewById(R.id.rep_val_tv)

        priceTxt = myView.findViewById(R.id.price_tv)
        costTxt = myView.findViewById(R.id.cost_tv)
        profitTxt = myView.findViewById(R.id.profit_tv)
        profitPercentTxt = myView.findViewById(R.id.profit_percent_tv)
        profitBar = myView.findViewById(R.id.profit_bar)

        statusCustCL = myView.findViewById(R.id.status_cust_cl)
        dataCL = myView.findViewById(R.id.work_order_data_cl)
        footerCL = myView.findViewById(R.id.work_order_footer_cl)
        headerCL = myView.findViewById(R.id.header_cl)

        itemRecyclerView = myView.findViewById(R.id.work_order_items_rv)

        if (GlobalVars.permissions!!.scheduleMoney == "0") {
            footerCL.visibility = View.GONE
        }



        getWorkOrder()

        scheduleTxt.text = workOrder!!.dateNice

    }

    private fun getWorkOrder(){
        println("getWorkOrder")
        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/functions/get/workOrder.php"
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
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        val gson = GsonBuilder().create()
                        workOrder = gson.fromJson(parentObject.toString() , WorkOrder::class.java)
                        println(workOrder)

                        setStatus(workOrder!!.status)
                        titleTxt.text = workOrder!!.title
                        if(workOrder!!.nextPlannedDate != null){
                            scheduleTxt.text = workOrder!!.nextPlannedDate
                        }
                        /*
                        if(workOrder!!.department != null){
                            deptTxt.text = workOrder!!.department!!
                        }

                         */
                        if(workOrder!!.crewName != null){
                            crewTxt.text = workOrder!!.mainCrew!!
                        }
                        if(workOrder!!.salesRepName != null){
                            repTxt.text = workOrder!!.salesRepName!!
                        }

                        println("Charge Name: ${workOrder!!.chargeName}")
                        when (workOrder!!.charge) {
                            "1" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_nc)
                            }
                            "2" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_fl)
                            }
                            "3" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_tm)
                            }
                            else -> {
                                workOrder!!.chargeName = ""
                            }
                        }
                        chargeTxt.text = workOrder!!.chargeName


                        val profit:Float = workOrder!!.totalPriceRaw.toFloat() - workOrder!!.totalCostRaw.toFloat()
                        val profitPercent:Int = (profit / workOrder!!.totalPriceRaw.toFloat() * 100).toInt()

                        priceTxt.text = workOrder!!.totalPrice
                        costTxt.text = workOrder!!.totalCost
                        profitTxt.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(profit))
                        profitPercentTxt.text = profitPercent.toString()
                        profitBar.progress = 100 - profitPercent


                        val woItemJSON: JSONArray = parentObject.getJSONArray("items")
                        val itemList = gson.fromJson(woItemJSON.toString() , Array<WoItem>::class.java).toMutableList()
                        println("woItemJSON $woItemJSON")

                        work_order_items_rv.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = activity?.let {
                                WoItemsAdapter(
                                    itemList,
                                    context,
                                    this@WorkOrderFragment
                                )
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            itemRecyclerView.addItemDecoration(itemDecoration)
                            (adapter as WoItemsAdapter).notifyDataSetChanged()
                        }

                        workOrder!!.setEmps()
                        hideProgressView()
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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["woID"] = workOrder!!.woID
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_not_started)!!,myView.context)!!, myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_in_progress)!!,myView.context)!!, myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_done)!!,myView.context)!!, myView.context.getString(R.string.finished)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            workOrder!!.status = item!!.itemId.toString()

            setStatus(workOrder!!.status)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                .show()

            showProgressView()

            var urlString = "https://www.adminmatic.com/cp/app/functions/update/workOrderStatus.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"
            val queue = Volley.newRequestQueue(com.example.AdminMatic.myView.context)


            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

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
                    params["status"] = workOrder!!.status
                    params["woID"] = workOrder!!.woID
                    params["empID"] = GlobalVars.loggedInEmployee!!.ID
                    println("params = $params")
                    return params
                }
            }
            queue.add(postRequest1)
            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }



    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        val directions = WorkOrderFragmentDirections.navigateWorkOrderToLead(_lead)
        myView.findNavController().navigate(directions)
    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
        val directions = WorkOrderFragmentDirections.navigateWorkOrderToContract(_contract)
        myView.findNavController().navigate(directions)
    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")
        workOrder = _workOrder
        getWorkOrder()
    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")
        val directions = WorkOrderFragmentDirections.navigateWorkOrderToInvoice(_invoice)
        myView.findNavController().navigate(directions)
    }

    override fun onWoItemCellClickListener(data:WoItem) {

        println("Cell clicked with woItem: ${data.item}")


        data.let {

            //var woItemFragment: WoItemFragment
            //val SIMPLE_FRAGMENT_TAG = "myfragmenttag"

            //myView.findNavController().navigate()
            // ft.add(R.id.container_all, frag as Fragment).commit()


            val directions = WorkOrderFragmentDirections.navigateToWoItem(it, workOrder!!)

            myView.findNavController().navigate(directions)


        }


    }







    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "1" -> {
                println("1")
                statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "2" -> {
                println("2")
                statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "3" -> {
                println("3")
                statusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "4" -> {
                println("4")
                statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
            "5" -> {
                println("5")
                statusBtn.setBackgroundResource(R.drawable.ic_waiting)
            }
        }
    }



    fun showProgressView() {

        println("showProgressView")
        pgsBar.visibility = View.VISIBLE
        statusCustCL.visibility = View.INVISIBLE
        dataCL.visibility = View.INVISIBLE
        if (GlobalVars.permissions!!.scheduleMoney == "1") {
            footerCL.visibility = View.INVISIBLE
        }
        headerCL.visibility = View.INVISIBLE
        itemRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        statusCustCL.visibility = View.VISIBLE
        dataCL.visibility = View.VISIBLE
        if (GlobalVars.permissions!!.scheduleMoney == "1") {
            footerCL.visibility = View.VISIBLE
        }
        headerCL.visibility = View.VISIBLE
        itemRecyclerView.visibility = View.VISIBLE
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment workOrderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WorkOrderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}