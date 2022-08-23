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
import com.AdminMatic.databinding.FragmentShiftsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.*


class ShiftsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    private var weekSpinnerPosition: Int = 0


    private lateinit var dateFrom: OffsetDateTime
    private lateinit var dateTo: OffsetDateTime

    private var dateFromDB: String = ""
    private var dateToDB: String = ""


    private var shifts = mutableListOf<Shift>()
    private lateinit var shiftsJSON: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    private var _binding: FragmentShiftsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentShiftsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_shifts, employee!!.fname)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.shiftsWeekLblTv.text = getString(R.string.xs_shifts_for, employee!!.fname)

        val weekSpinnerArray:Array<String> = arrayOf(
            getString(R.string.shifts_this_week),
            getString(R.string.shifts_next_week))

        binding.shiftsWeekSpinner.setBackgroundResource(R.drawable.text_view_layout)

        binding.shiftsWeekSpinner.onItemSelectedListener = null

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, weekSpinnerArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.shiftsWeekSpinner.adapter = adapter
        binding.shiftsWeekSpinner.onItemSelectedListener = this@ShiftsFragment

        binding.shiftsWeekSpinner.setSelection(0)

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("shifts")
    }

    private fun getShifts(){
        println("getShifts")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/shifts.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    shiftsJSON = JSONObject(response)

                    parseShiftsJSON()

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
        postRequest1.tag = "shifts"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun parseShiftsJSON() {

        shifts.clear()
        var numberOfValidShifts = 0

        for (day in 0..6) {

            val gson = GsonBuilder().create()



            try {
                val iShifts = shiftsJSON["shifts"] as JSONObject
                val iDay = iShifts["$day"] as JSONObject
                //val iStartTime = iDay["startTime"]

                shifts.add(gson.fromJson(iDay.toString(), Shift::class.java))

                // Catch for a broken entry and treat as empty
                // If not broken, increment count of valid shifts
                // Todo: find out if you can manually throw an exception here instead of duplicating code
                if (shifts[day].startTime == null) {
                    val startTime = dateFrom.plusDays(day.toLong()).format(GlobalVars.dateFormatterPHP)
                    shifts[day] = Shift("0", employee!!.ID, startTime, startTime,"", "","0.00")
                }
                else {
                    numberOfValidShifts++
                }



            } catch (e: Exception) {
                val startTime = dateFrom.plusDays(day.toLong()).format(GlobalVars.dateFormatterPHP)
                shifts.add(Shift("0", employee!!.ID, startTime, startTime,"", "","0.00"))
            }

        }

        binding.shiftsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = activity?.let {
                ShiftsAdapter(shifts, it)
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.shiftsRecyclerView.addItemDecoration(itemDecoration)


            var totalHours = 0.0
            shifts.forEach {
                totalHours += it.shiftQty!!.toDouble()
            }

            binding.shiftsFooterTv.text = getString(R.string.shifts_footer_text, numberOfValidShifts, totalHours)

            //(adapter as ShiftsAdapter).notifyDataSetChanged()

        }

        hideProgressView()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")
        println("weekSpinner.getTag(R.id.pos) = ${binding.shiftsWeekSpinner.getTag(R.id.pos)}")

        if(binding.shiftsWeekSpinner.getTag(R.id.pos) != null && binding.shiftsWeekSpinner.getTag(R.id.pos) != position){
            println("tag != pos")
        }

        weekSpinnerPosition = position
        binding.shiftsWeekSpinner.setTag(R.id.pos, position)


        dateFrom = LocalDate.now(ZoneOffset.UTC).with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atTime(OffsetTime.MIN)
        dateTo = dateFrom.plusDays(6)

        if (position == 1) { // next week
            println("Next Week")
            dateFrom = dateFrom.plusDays(7)
            dateTo = dateTo.plusDays(7)
        }

        dateFromDB = dateFrom.format(GlobalVars.dateFormatterYYYYMMDD)
        dateToDB = dateTo.format(GlobalVars.dateFormatterYYYYMMDD)

        getShifts()

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
