package com.example.AdminMatic

import android.app.AlertDialog
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
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_employee_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.HashMap


class ShiftsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    private lateinit var datePicker: DatePickerHelper
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private lateinit var spinnerLabelText: TextView
    private lateinit var weekSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var footerText: TextView

    private var weekSpinnerPosition: Int = 0

    private var dateFrom: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var dateTo: LocalDate = LocalDate.now(ZoneOffset.UTC).plusDays(7)
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
        myView = inflater.inflate(R.layout.fragment_shifts, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_shifts, employee!!.fName)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pgsBar = view.findViewById(R.id.progress_bar)
        allCl = view.findViewById(R.id.all_cl)

        recyclerView = view.findViewById(R.id.shifts_recycler_view)
        footerText = view.findViewById(R.id.shifts_footer_tv)
        spinnerLabelText = view.findViewById(R.id.shifts_week_lbl_tv)
        spinnerLabelText.text = getString(R.string.xs_shifts_for, employee!!.fName)


        val weekSpinnerArray:Array<String> = arrayOf(
            getString(R.string.shifts_this_week),
            getString(R.string.shifts_next_week))

        weekSpinner = myView.findViewById(R.id.shifts_week_spinner)
        weekSpinner.setBackgroundResource(R.drawable.text_view_layout)

        weekSpinner.onItemSelectedListener = null

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, weekSpinnerArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        weekSpinner.adapter = adapter
        weekSpinner.onItemSelectedListener = this@ShiftsFragment

        hideProgressView()
        getShifts()
    }

    private fun getShifts(){
        println("getShifts")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/shifts.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")
                hideProgressView()
                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        val shifts:JSONArray = parentObject.getJSONArray("shifts")

                        val gson = GsonBuilder().create()
                        val shiftsList = gson.fromJson(shifts.toString() , Array<Shift>::class.java).toMutableList()

                        //employeeCountTv.text = getString(R.string.x_active_employees, employeesList.size)

                        recyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                ShiftsAdapter(shiftsList,it)
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            recyclerView.addItemDecoration(itemDecoration)


                            var totalHours = 0.0
                            shiftsList.forEach {
                                totalHours += it.qty!!.toDouble()
                            }

                            footerText.text = getString(R.string.usage_footer_text, shiftsList.size, totalHours)

                            (adapter as employeeUsageAdapter).notifyDataSetChanged()

                        }
                    }
                    /* Here 'response' is a String containing the response you received from the website... */
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
        queue.add(postRequest1)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")
        println("weekSpinner.getTag(R.id.pos) = ${weekSpinner.getTag(R.id.pos)}")

        if(weekSpinner.getTag(R.id.pos) != null && weekSpinner.getTag(R.id.pos) != position){
            println("tag != pos")
        }

        weekSpinnerPosition = position
        weekSpinner.setTag(R.id.pos, position)

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
