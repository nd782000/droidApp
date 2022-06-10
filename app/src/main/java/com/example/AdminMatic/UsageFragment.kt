package com.example.AdminMatic

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
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_employee_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.HashMap

interface EmployeeUsageCellClickListener {
    fun onWoItemCellClickListener(data:Usage)
}

class UsageFragment : Fragment(), EmployeeUsageCellClickListener {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    private lateinit var datePicker: DatePickerHelper
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private lateinit var fromEditText: EditText
    private lateinit var toEditText: EditText
    private lateinit var usageRecycler: RecyclerView
    private lateinit var footerText: TextView

    private var dateFrom: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var dateTo: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var dateFromDB = dateFrom.format(GlobalVars.dateFormatterYYYYMMDD)
    private var dateToDB = dateTo.format(GlobalVars.dateFormatterYYYYMMDD)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_usage, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_usage, employee!!.fName)



        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pgsBar = view.findViewById(R.id.progress_bar)
        allCl = view.findViewById(R.id.all_cl)

        usageRecycler = view.findViewById(R.id.usage_recycler_view)
        footerText = view.findViewById(R.id.usage_footer_tv)


        fromEditText = myView.findViewById(R.id.usage_from_et)
        fromEditText.setBackgroundResource(R.drawable.text_view_layout)
        fromEditText.setText(dateFrom.format(GlobalVars.dateFormatterShortDashes))
        fromEditText.setOnClickListener {
            datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
            datePicker.showDialog(dateFrom.year, dateFrom.monthValue-1, dateFrom.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    dateFrom = LocalDate.of(year, month+1, dayOfMonth)
                    fromEditText.setText(dateFrom.format(GlobalVars.dateFormatterShortDashes))
                    dateFromDB = dateFrom.format(GlobalVars.dateFormatterYYYYMMDD)
                    getUsage()
                }
            })
        }


        toEditText = myView.findViewById(R.id.usage_to_et)
        toEditText.setBackgroundResource(R.drawable.text_view_layout)
        toEditText.setText(dateTo.format(GlobalVars.dateFormatterShortDashes))
        toEditText.setOnClickListener {
            datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
            datePicker.showDialog(dateTo.year, dateTo.monthValue-1, dateTo.dayOfMonth, object : DatePickerHelper.Callback {
                override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                    dateTo = LocalDate.of(year, month+1, dayOfMonth)
                    toEditText.setText(dateTo.format(GlobalVars.dateFormatterShortDashes))
                    dateToDB = dateTo.format(GlobalVars.dateFormatterYYYYMMDD)
                    getUsage()
                }
            })
        }

        hideProgressView()
        getUsage()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("usage")
    }

    private fun getUsage(){
        println("getUsage")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/usageByEmp.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")
                hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    val employees:JSONArray = parentObject.getJSONArray("usages")


                    val gson = GsonBuilder().create()
                    val usageList = gson.fromJson(employees.toString() , Array<Usage>::class.java).toMutableList()

                    //employeeCountTv.text = getString(R.string.x_active_employees, employeesList.size)

                    usageRecycler.apply {
                        layoutManager = LinearLayoutManager(activity)

                        adapter = activity?.let {
                            EmployeeUsageAdapter(usageList,
                                it, this@UsageFragment)
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        usageRecycler.addItemDecoration(itemDecoration)


                        var totalHours = 0.0
                        usageList.forEach {
                            totalHours += it.qty.toDouble()
                        }

                        footerText.text = getString(R.string.usage_footer_text, usageList.size, totalHours)

                        (adapter as EmployeeUsageAdapter).notifyDataSetChanged()

                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
                // var intent:Intent = Intent(applicationContext,MainActivity2::class.java)
                // startActivity(intent)
            },
            Response.ErrorListener { // error
                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["startDate"] = dateFromDB
                params["endDate"] = dateToDB
                params["empID"] = employee!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "usage"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onWoItemCellClickListener(data:Usage) {
        println("Cell clicked with usage: ${data.custName}")
        data.let {
            val directions = UsageFragmentDirections.navigateUsageToWorkOrder(null)
            directions.workOrderID = it.woID
            myView.findNavController().navigate(directions)
        }
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        allCl.visibility = View.VISIBLE
    }

}
