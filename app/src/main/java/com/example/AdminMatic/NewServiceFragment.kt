package com.example.AdminMatic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_equipment_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_equipment_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_equipment_list.*
import kotlinx.android.synthetic.main.fragment_new_service.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/*
interface EquipmentDetailCellClickListener {
    fun onEquipmentDetailCellClickListener(data:String)
}
*/

class NewServiceFragment : Fragment() {

    private var equipment: Equipment? = null
    private var newService: EquipmentService? = null


    private var currentDate = LocalDateTime.now()
    private var nextDate = currentDate


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar

    lateinit var frequencyTxt:TextView
    lateinit var currentTxt:TextView
    lateinit var nextTxt:TextView

    lateinit var nameEditTxt:EditText
    lateinit var typeTxt:TextView
    lateinit var frequencyEditTxt:EditText
    lateinit var currentEditTxt:EditText
    lateinit var nextEditTxt:EditText
    lateinit var instructionsEditTxt:EditText
    lateinit var submitBtn:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable<Equipment?>("equipment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_new_service, container, false)


        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "New Service"



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)

        frequencyTxt = view.findViewById(R.id.new_service_frequency_title_txt)
        currentTxt = view.findViewById(R.id.new_service_current_title_txt)
        nextTxt = view.findViewById(R.id.new_service_next_title_txt)

        nameEditTxt = view.findViewById(R.id.new_service_name_editTxt)
        typeTxt = view.findViewById(R.id.new_service_type_txt)

        /** Using these makes it so typeEditText only requires one tap, but it won't
         * close currently opened keyboards which could create bugs when the type changes
         * and a different input type is required
        **/
        //typeEditTxt.isFocusable = false
        //typeEditTxt.isFocusableInTouchMode = false

        frequencyEditTxt = view.findViewById(R.id.new_service_frequency_editTxt)
        currentEditTxt = view.findViewById(R.id.new_service_current_editTxt)
        nextEditTxt = view.findViewById(R.id.new_service_next_editTxt)
        instructionsEditTxt = view.findViewById(R.id.new_service_instructions_editTxt)

        submitBtn = view.findViewById(R.id.new_service_submit_btn)
        submitBtn.setOnClickListener{
            println("status btn clicked")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = Date()

        newService = EquipmentService(
            "0", //temp
            "Untitled",
            "0",
            getString(R.string.service_type_one_time), //typeName
            loggedInEmployee!!.ID, //added by
            "0", // 0 = not started
            equipment!!.ID, //equipment ID
            null,
            null,
            sdf.format(currentTime),
            null,
            null,
            null,
            null,
            null,
            null,
            false
        )

        nameEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                newService!!.name = s.toString()
            }
        })
        frequencyEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.isNotBlank()) {
                    newService!!.frequency = s.toString()
                    when (newService!!.type) {
                        "1" -> { // date based
                            if (newService!!.frequency != null) {
                                nextDate = currentDate.plusDays(newService!!.frequency!!.toLong())
                            }
                            nextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                            newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
                        }
                        "2" -> { // mile/km based
                            var tempNext =
                                newService!!.currentValue!!.toInt() + newService!!.frequency!!.toInt()
                            newService!!.nextValue = tempNext.toString()
                            nextEditTxt.setText(tempNext.toString())
                        }
                        "3" -> { // engine hour based
                            nextDate = currentDate.plusDays(newService!!.frequency!!.toLong())
                            var tempNext = newService!!.currentValue!!.toInt() + newService!!.frequency!!.toInt()
                            newService!!.nextValue = tempNext.toString()
                            nextEditTxt.setText(tempNext.toString())
                        }
                    }
                }
            }

        })
        currentEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (newService!!.type != "1") { // Only change if not in date mode, in date mode it changes itself
                    newService!!.currentValue = s.toString()
                }
            }
        })
        nextEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (newService!!.type != "1") {
                    newService!!.nextValue = s.toString()
                }
            }
        })
        instructionsEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                newService!!.instruction = s.toString()
            }
        })

        typeTxt.setOnClickListener{
            showTypeMenu()
            hideKeyboard()
        }

        val dateSetListenerCurrent =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                currentDate = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0)
                currentEditTxt.setText(GlobalVars.dateFormatterShort.format(currentDate))
                newService!!.currentValue = GlobalVars.dateFormatterPHP.format(currentDate)
            }

        // Todo: Finish this (editing next date currently just applies to current date)
        val dateSetListenerNext =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                nextDate = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0)
                nextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
            }

        currentEditTxt.setOnClickListener {
            context?.let { it1 ->
                if(newService!!.type == "1") {
                    DatePickerDialog(
                        it1,
                        dateSetListenerCurrent,
                        // set DatePickerDialog to point to today's date when it loads up
                        nextDate.year,
                        nextDate.monthValue,
                        nextDate.dayOfYear
                    ).show()
                }
            }
        }

        nextEditTxt.setOnClickListener {
            context?.let { it1 ->
                if(newService!!.type == "1") {
                    DatePickerDialog(
                        it1,
                        dateSetListenerCurrent,
                        // set DatePickerDialog to point to today's date when it loads up
                        currentDate.year,
                        currentDate.monthValue,
                        currentDate.dayOfYear
                    ).show()
                }
            }
        }

        //Set up the elements to start in "one time" mode
        //nameEditTxt.setText(R.string.new_service_name)
        typeTxt.setText(R.string.service_type_one_time)
        frequencyEditTxt.isFocusable = false
        frequencyEditTxt.isFocusableInTouchMode = false
        currentEditTxt.isFocusable = false
        currentEditTxt.isFocusableInTouchMode = false
        nextEditTxt.isFocusable = false
        nextEditTxt.isFocusableInTouchMode = false

        hideProgressView()

    }

    private fun showTypeMenu(){
        println("showTypeMenu")

        var popUp = PopupMenu(com.example.AdminMatic.myView.context,new_service_type_txt)
        popUp.inflate(R.menu.new_service_type_menu)
        popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.new_service_type_menu_one_time -> {
                    newService!!.type = "0"
                    newService!!.typeName = getString(R.string.service_type_one_time)
                    typeTxt.setText(R.string.service_type_one_time)
                    frequencyTxt.setText(R.string.new_service_frequency_days)
                    currentTxt.setText(R.string.new_service_current)
                    nextTxt.setText(R.string.new_service_next)
                    frequencyEditTxt.isFocusable = false
                    currentEditTxt.isFocusable = false
                    nextEditTxt.isFocusable = false
                    frequencyEditTxt.isFocusableInTouchMode = false
                    currentEditTxt.isFocusableInTouchMode = false
                    nextEditTxt.isFocusableInTouchMode = false
                    frequencyEditTxt.text = null
                    currentEditTxt.text = null
                    nextEditTxt.text = null
                    newService!!.frequency = null
                    newService!!.currentValue = null
                    newService!!.nextValue = null
                }
                R.id.new_service_type_menu_date_based -> {
                    newService!!.type = "1"
                    newService!!.typeName = getString(R.string.service_type_date_based)
                    typeTxt.setText(R.string.service_type_date_based)
                    frequencyTxt.setText(R.string.new_service_frequency_days)
                    currentTxt.setText(R.string.new_service_current)
                    nextTxt.setText(R.string.new_service_next)
                    frequencyEditTxt.isFocusable = true
                    currentEditTxt.isFocusable = false
                    nextEditTxt.isFocusable = false
                    frequencyEditTxt.isFocusableInTouchMode = true
                    currentEditTxt.isFocusableInTouchMode = false
                    nextEditTxt.isFocusableInTouchMode = false
                    frequencyEditTxt.text = null
                    currentEditTxt.text = null
                    nextEditTxt.text = null
                    currentEditTxt.inputType = InputType.TYPE_CLASS_DATETIME
                    nextEditTxt.inputType = InputType.TYPE_CLASS_DATETIME
                    newService!!.frequency = "0"
                    currentEditTxt.setText(GlobalVars.dateFormatterShort.format(currentDate))
                    nextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                    newService!!.currentValue = GlobalVars.dateFormatterPHP.format(currentDate)
                    newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
                }
                R.id.new_service_type_menu_mile_km_based -> {
                    newService!!.type = "2"
                    newService!!.typeName = getString(R.string.service_type_mile_km_based)
                    typeTxt.setText(R.string.service_type_mile_km_based)
                    frequencyTxt.setText(R.string.new_service_frequency_miles_km)
                    currentTxt.setText(R.string.new_service_current_miles_km)
                    nextTxt.setText(R.string.new_service_next_miles_km)
                    frequencyEditTxt.isFocusable = true
                    currentEditTxt.isFocusable = true
                    nextEditTxt.isFocusable = true
                    frequencyEditTxt.isFocusableInTouchMode = true
                    currentEditTxt.isFocusableInTouchMode = true
                    nextEditTxt.isFocusableInTouchMode = true
                    frequencyEditTxt.text = null
                    currentEditTxt.setText("0")
                    nextEditTxt.setText("0")
                    currentEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    nextEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    newService!!.frequency = "0"
                    newService!!.currentValue = "0"
                    newService!!.nextValue = "0"
                }
                R.id.new_service_type_menu_engine_hour_based -> {
                    newService!!.type = "3"
                    newService!!.typeName = getString(R.string.service_type_engine_hour_based)
                    typeTxt.setText(R.string.service_type_engine_hour_based)
                    frequencyTxt.setText(R.string.new_service_frequency_engine_hours)
                    currentTxt.setText(R.string.new_service_current_engine_hours)
                    nextTxt.setText(R.string.new_service_next_engine_hours)
                    frequencyEditTxt.isFocusable = true
                    currentEditTxt.isFocusable = true
                    nextEditTxt.isFocusable = true
                    frequencyEditTxt.isFocusableInTouchMode = true
                    currentEditTxt.isFocusableInTouchMode = true
                    nextEditTxt.isFocusableInTouchMode = true
                    frequencyEditTxt.text = null
                    currentEditTxt.setText("0")
                    nextEditTxt.setText("0")
                    currentEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    nextEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    newService!!.frequency = "0"
                    newService!!.currentValue = "0"
                    newService!!.nextValue = "0"
                }
                R.id.new_service_type_menu_inspection -> {
                    newService!!.type = "4"
                    newService!!.typeName = getString(R.string.service_type_inspection)
                    typeTxt.setText(R.string.service_type_inspection)
                    frequencyTxt.setText(R.string.new_service_frequency_days)
                    currentTxt.setText(R.string.new_service_current)
                    nextTxt.setText(R.string.new_service_next)
                    frequencyEditTxt.isFocusable = false
                    currentEditTxt.isFocusable = false
                    nextEditTxt.isFocusable = false
                    frequencyEditTxt.isFocusableInTouchMode = false
                    currentEditTxt.isFocusableInTouchMode = false
                    nextEditTxt.isFocusableInTouchMode = false
                    frequencyEditTxt.text = null
                    currentEditTxt.text = null
                    nextEditTxt.text = null
                    newService!!.frequency = null
                    newService!!.currentValue = null
                    newService!!.nextValue = null
                }
            }

            true
        })

        popUp.show()

        popUp.gravity = Gravity.LEFT
        popUp.show()
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }

    private fun hideKeyboard() {
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EquipmentListFragment().apply {

            }
    }
}