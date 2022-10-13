package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentPayrollSummaryBinding
import com.AdminMatic.databinding.FragmentShiftsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.*


class PayrollSummaryFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    private var weekSpinnerPosition: Int = 0


    private lateinit var dateFrom: OffsetDateTime
    private lateinit var dateTo: OffsetDateTime

    private var dateFromDB: String = ""
    private var dateToDB: String = ""


    private var payrollList = mutableListOf<Payroll>()
    private lateinit var payrollArray:PayrollArray
    private lateinit var payrollJSON: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    private var _binding: FragmentPayrollSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPayrollSummaryBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.payroll_summary_bar, employee!!.fname)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.weekLblTv.text = getString(R.string.payroll_summary_for, employee!!.fname)

        val weekSpinnerArray:Array<String> = arrayOf(
            getString(R.string.shifts_this_week),
            getString(R.string.payroll_summary_last_week))

        binding.weekSpinner.setBackgroundResource(R.drawable.text_view_layout)

        binding.weekSpinner.onItemSelectedListener = null

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, weekSpinnerArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.weekSpinner.adapter = adapter
        binding.weekSpinner.onItemSelectedListener = this@PayrollSummaryFragment

        binding.weekSpinner.setSelection(0)

        if (employee!!.ID != GlobalVars.loggedInEmployee!!.ID) {
            binding.payTv.visibility = View.GONE
        }

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("payrollSummary")
    }

    private fun getPayroll(){
        println("getPayroll")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/payroll.php"

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
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val payroll: JSONArray = parentObject.getJSONArray("payroll")


                        val gson = GsonBuilder().create()
                        //payrollList = gson.fromJson(payroll.toString() , Array<Payroll>::class.java).toMutableList()
                        //println("Payroll list size: ${payrollList.size}")

                        payrollArray = gson.fromJson(response, PayrollArray::class.java)


                        binding.recyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                PayrollAdapter(payrollArray.payroll!!.toMutableList(), it)
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.recyclerView.addItemDecoration(itemDecoration)

                        }

                        // Totals view
                        if (payrollArray.pending == "1") {
                            binding.totalTv.text =
                                getString(R.string.payroll_summary_totals, payrollArray.totalShifts, getString(R.string.payroll_summary_pending))
                        } else {
                            binding.totalTv.text = getString(R.string.payroll_summary_totals, payrollArray.totalShifts, payrollArray.combinedTotal)
                        }

                        // Pay view
                        if (payrollArray.pending == "1") {
                            binding.payTv.text = getString(R.string.payroll_summary_pay, getString(R.string.payroll_summary_pending))
                        } else {
                            binding.payTv.text = getString(R.string.payroll_summary_pay, payrollArray.totalPay)
                        }
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
                //params["startDate"] = dateFromDB
                params["endDate"] = dateToDB
                params["empID"] = employee!!.ID
                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "payrollSummary"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")
        println("weekSpinner.getTag(R.id.pos) = ${binding.weekSpinner.getTag(R.id.pos)}")

        if(binding.weekSpinner.getTag(R.id.pos) != null && binding.weekSpinner.getTag(R.id.pos) != position){
            println("tag != pos")
        }

        weekSpinnerPosition = position
        binding.weekSpinner.setTag(R.id.pos, position)


        dateFrom = LocalDate.now(ZoneOffset.UTC).with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atTime(OffsetTime.MIN)
        dateTo = dateFrom.plusDays(6)

        if (position == 1) { // last week
            println("Last Week")
            dateFrom = dateFrom.minusDays(7)
            dateTo = dateTo.minusDays(7)
        }

        dateFromDB = dateFrom.format(GlobalVars.dateFormatterYYYYMMDD)
        dateToDB = dateTo.format(GlobalVars.dateFormatterYYYYMMDD)

        getPayroll()

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
