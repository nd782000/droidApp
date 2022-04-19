package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WoItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


interface TaskCellClickListener {
    fun onTaskCellClickListener(data:Task)
    fun showProgressView()
    fun getWoItem()
    fun uploadImage(_task:Task)

}




class WoItemFragment : Fragment(), TaskCellClickListener ,AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    private var param2: String? = null


    private  var woItem: WoItem? = null
    lateinit  var workOrder:WorkOrder
    lateinit  var globalVars:GlobalVars







    lateinit var myView:View

    private lateinit var  pgsBar: ProgressBar

    private lateinit var woItemSearch: SearchView

    private lateinit var estCl:ConstraintLayout
    private lateinit var estTxt:EditText
    private lateinit var chargeSpinner:Spinner

    private lateinit var hideCl:ConstraintLayout
    private lateinit var hideQtySwitch:Switch
    private lateinit var qtyTxt:EditText

    private lateinit var taxCl:ConstraintLayout
    private lateinit var taxableSwitch:Switch
    private lateinit var priceTxt:EditText

    private lateinit var totalCl:ConstraintLayout
    private lateinit var totalTxt:EditText

    private lateinit var leadTaskBtn:Button
    private lateinit var tasksRv:RecyclerView
    private lateinit var descriptionCl:ConstraintLayout

    private lateinit var usageBtn:Button
    private lateinit var profitCl:ConstraintLayout

    private lateinit var submitBtn:Button


    private var chargeTypeArray:Array<String> = arrayOf("No Charge", "Flat", "T & M")

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")
            workOrder = it.getParcelable("workOrder")!!
            param2 = it.getString(ARG_PARAM2)
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_customer, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_wo_item, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.work_order_item)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("woItem = ${woItem!!.item}")


        pgsBar = view.findViewById(R.id.progress_bar)

        woItemSearch = myView.findViewById(R.id.wo_item_search)
        estCl = myView.findViewById(R.id.wo_item_est_cl)
        estTxt = myView.findViewById(R.id.wo_item_est_val_et)
        chargeSpinner = myView.findViewById(R.id.wo_item_charge_spinner)

        hideCl = myView.findViewById(R.id.wo_item_hide_cl)
        hideQtySwitch = myView.findViewById(R.id.wo_item_hide_qty_switch)
        qtyTxt = myView.findViewById(R.id.wo_item_qty_val_et)

        taxCl = myView.findViewById(R.id.wo_item_tax_cl)
        taxableSwitch = myView.findViewById(R.id.wo_item_taxable_switch)
        priceTxt = myView.findViewById(R.id.wo_item_price_val_et)

        totalCl = myView.findViewById(R.id.wo_item_total_cl)
        totalTxt = myView.findViewById(R.id.wo_item_total_val_et)

        leadTaskBtn = myView.findViewById(R.id.wo_item_lead_task_btn)
        tasksRv = myView.findViewById(R.id.wo_item_tasks_rv)
        descriptionCl = myView.findViewById(R.id.wo_item_description_cl)
        usageBtn = myView.findViewById(R.id.wo_item_usage_btn)
        usageBtn.setOnClickListener{



            if (woItem != null){
                val directions = WoItemFragmentDirections.navigateToUsageEntry(woItem!!,workOrder)
                myView.findNavController().navigate(directions)
            }





            //al directions = WorkOrderFragmentDirections.navigateToWoItem(data)


        }
        profitCl = myView.findViewById(R.id.wo_item_profit_cl)

        submitBtn = myView.findViewById(R.id.wo_item_submit_btn)


        if (woItem == null){

            hideProgressView()
        }else{
            getWoItem()
        }



    }

    private var listener: Callbacks? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is LogOut) {
            context as Callbacks
        } else {
            throw ClassCastException(
                "$context must implement LogOut"
            )
        }
    }


    override fun getWoItem(){
        println("get woItem")


        //if (!pgsBar.isVisible){
            showProgressView()
       // }


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/workOrderItem.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")




                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")


                    val gson = GsonBuilder().create()
                    woItem = gson.fromJson(parentObject.toString() , WoItem::class.java)





                    val taskJSON: JSONArray = parentObject.getJSONArray("tasks")
                    val taskList = gson.fromJson(taskJSON.toString() , Array<Task>::class.java).toMutableList()

                    tasksRv.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            TasksAdapter(
                                taskList,
                                it,
                                this@WoItemFragment,
                                woItem as WoItem
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        tasksRv.addItemDecoration(itemDecoration)



                        (adapter as TasksAdapter).notifyDataSetChanged()
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
                params["woItemID"] = woItem!!.ID

                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)


    }

    override fun uploadImage(_task:Task){

        val images:Array<Image> = if(_task.images == null){
            arrayOf()
        }else{
            _task.images!!
        }

        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"",
            _task.ID,"${_task.task}","","")
        myView.findNavController().navigate(directions)
    }


    override fun onTaskCellClickListener(data:Task) {

        println("Cell clicked with task: ${data.task}")
        val images:Array<Image> = if(data.images == null){
            arrayOf()
        }else{
            data.images!!
        }

        data.let {

            val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"", it.ID,"${it.task}","","")

           myView.findNavController().navigate(directions)
        }


    }


    override fun showProgressView() {

        println("showProgressView")

        pgsBar.visibility = View.VISIBLE


        woItemSearch.visibility = View.INVISIBLE
        estCl.visibility = View.INVISIBLE
        hideCl.visibility = View.INVISIBLE
        taxCl.visibility = View.INVISIBLE
        totalCl.visibility = View.INVISIBLE
        leadTaskBtn.visibility = View.INVISIBLE
        tasksRv.visibility = View.INVISIBLE
        descriptionCl.visibility = View.INVISIBLE
        usageBtn.visibility = View.INVISIBLE
        profitCl.visibility = View.INVISIBLE
        submitBtn.visibility = View.INVISIBLE



    }

     fun hideProgressView() {



        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        chargeSpinner.adapter = adapter




        if (woItem == null){
            println("woItem = null")

            pgsBar.visibility = View.INVISIBLE


            woItemSearch.visibility = View.VISIBLE
            estCl.visibility = View.VISIBLE


            leadTaskBtn.visibility = View.GONE
            profitCl.visibility = View.GONE
            usageBtn.visibility = View.GONE
            tasksRv.visibility = View.GONE
            descriptionCl.visibility = View.GONE

            submitBtn.visibility = View.VISIBLE



        }else{

            // set text, spinner and switch values
            woItemSearch.isIconified = false // Expand it
            woItemSearch.setQuery(woItem!!.item,true)
            woItemSearch.clearFocus()

            estTxt.setText(woItem!!.est)





            if (woItem != null){
                chargeSpinner.setSelection(woItem!!.charge.toInt() - 1)
            }







            if (woItem!!.hideUnits == "1"){
                hideQtySwitch.isChecked = true
            }

            qtyTxt.setText(woItem!!.act)

            if (woItem!!.taxType == "1"){
                taxableSwitch.isChecked = true
            }
            priceTxt.setText(woItem!!.price)

            totalTxt.setText(woItem!!.total)

            if (editMode){
                println("editMode = true")

               // woItemSearch.isEnabled = true
                //woItemSearch.isClickable = true
                setViewAndChildrenEnabled(woItemSearch,true)
                estTxt.isEnabled = true
                estTxt.isClickable = true
                chargeSpinner.isEnabled = true
                chargeSpinner.isClickable = true
                chargeSpinner.onItemSelectedListener = this@WoItemFragment



                pgsBar.visibility = View.INVISIBLE


                woItemSearch.visibility = View.VISIBLE
                estCl.visibility = View.VISIBLE


                hideCl.visibility = View.VISIBLE
                taxCl.visibility = View.VISIBLE
                totalCl.visibility = View.VISIBLE
                leadTaskBtn.visibility = View.VISIBLE

                profitCl.visibility = View.GONE

                usageBtn.visibility = View.GONE

                submitBtn.visibility = View.VISIBLE


                profitCl.visibility = View.GONE

            }else{
                println("editMode = false")

                //woItemSearch.isEnabled = false
                //woItemSearch.isClickable = false
                setViewAndChildrenEnabled(woItemSearch,false)


                estTxt.isEnabled = false
                estTxt.isClickable = false
                chargeSpinner.isEnabled = false
                chargeSpinner.isClickable = false
                chargeSpinner.onItemSelectedListener = null




                pgsBar.visibility = View.INVISIBLE


                woItemSearch.visibility = View.VISIBLE
                estCl.visibility = View.VISIBLE



                hideCl.visibility = View.GONE
                taxCl.visibility = View.GONE
                totalCl.visibility = View.GONE
                leadTaskBtn.visibility = View.GONE

                profitCl.visibility = View.VISIBLE

                usageBtn.visibility = View.VISIBLE

                submitBtn.visibility = View.GONE

                profitCl.visibility = View.GONE
            }

            if (woItem!!.type == "1"){
                //labor
                tasksRv.visibility = View.VISIBLE
                descriptionCl.visibility = View.GONE
            }else{
                //material
                tasksRv.visibility = View.GONE
                descriptionCl.visibility = View.VISIBLE
            }





        }

    }


    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")

        if (woItem != null){
            woItem!!.charge = (position + 1).toString()
        }





    }


    /*
    override fun onDestroy() {
        println("onDestroy")

        val parentFrag: WorkOrderFragment = this@WoItemFragment.getParentFragment() as WorkOrderFragment
        parentFrag.refreshWo()


        super.onDestroy()
    }
*/






    private fun setViewAndChildrenEnabled(
        view: View,
        enabled: Boolean
    ) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WoItemFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WoItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}