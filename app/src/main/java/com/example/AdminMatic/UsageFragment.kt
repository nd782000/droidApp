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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class UsageFragment : Fragment() {

    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    private lateinit var timePicker: TimePickerHelper
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private lateinit var fromEditText: EditText
    private lateinit var toEditText: EditText
    private lateinit var usageRecycler: RecyclerView


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

        fromEditText = myView.findViewById(R.id.usage_from_et)
        toEditText = myView.findViewById(R.id.usage_to_et)

        fromEditText.setBackgroundResource(R.drawable.text_view_layout)

        fromEditText.setOnClickListener {
            val h = 0
            val m = 0
            timePicker = TimePickerHelper(com.example.AdminMatic.myView.context, false, true)
            timePicker.showDialog(h, m, object : TimePickerHelper.Callback {
                override fun onTimeSelected(hourOfDay: Int, minute: Int) {

                    println("hourOfDay = $hourOfDay")
                    println("minute = $minute")

                    val current = LocalDate.now()

                    val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
                    val minuteStr = if (minute < 10) "0${minute}" else "$minute"

                    val startString = "$current $hourStr:$minuteStr:00"
                    println("startString is  $startString")

                }
            })
        }


        hideProgressView()
        //getServiceInfo()

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
