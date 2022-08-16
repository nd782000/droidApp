package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.set


class NewEditLeadFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false

    private var leadID: String? = null
    private lateinit var lead: Lead

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private var editMode = false



    private lateinit var statusBtn: ImageButton
    private lateinit var customerSearch: SearchView
    private lateinit var customerRecycler: RecyclerView
    private lateinit var scheduleTypeSpinner: Spinner
    private lateinit var datePicker: DatePickerHelper
    private lateinit var appointmentDate: EditText
    private lateinit var appointmentTime: EditText
    private lateinit var deadline: EditText
    private lateinit var urgentSwitch: SwitchCompat
    private lateinit var salesRepSearch: SearchView
    private lateinit var salesRepRecycler: RecyclerView
    private lateinit var requestedByCustomerSwitch: SwitchCompat
    private lateinit var descriptionEditText: EditText
    private lateinit var submitBtn: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            leadID = it.getString("leadID")
        }
        if (leadID != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("leadNewEdit")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_new_edit_lead, container, false)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        parentFragmentManager.popBackStackImmediate()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    parentFragmentManager.popBackStackImmediate()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        globalVars = GlobalVars()
        if (leadID == null) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_lead)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_lead, leadID)
        }
        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lead = Lead("0", "1")

        // =================================
        // ===== FETCH VIEW REFERENCES =====
        // =================================
        pgsBar = view.findViewById(R.id.progress_bar)
        allCl = view.findViewById(R.id.all_cl)

        statusBtn = view.findViewById(R.id.new_edit_lead_status_btn)
        customerSearch = view.findViewById(R.id.new_edit_lead_customer_search)
        customerRecycler = view.findViewById(R.id.new_edit_lead_customer_search_rv)
        scheduleTypeSpinner = view.findViewById(R.id.new_edit_lead_schedule_type_spinner)
        datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
        appointmentDate = view.findViewById(R.id.new_edit_lead_appointment_date_et)
        appointmentTime = view.findViewById(R.id.new_edit_lead_appointment_time_et)
        deadline = view.findViewById(R.id.new_edit_lead_deadline_et)
        urgentSwitch = view.findViewById(R.id.new_edit_lead_urgent_switch)
        salesRepSearch = view.findViewById(R.id.new_edit_lead_sales_rep_search)
        salesRepRecycler = view.findViewById(R.id.new_edit_lead_sales_rep_search_rv)
        requestedByCustomerSwitch = view.findViewById(R.id.new_edit_lead_requested_switch)
        descriptionEditText = view.findViewById(R.id.new_edit_lead_description_et)
        submitBtn = view.findViewById(R.id.new_edit_lead_submit_btn)


        // =================================
        // ========= SET UP VIEWS ==========
        // =================================

        statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }
        setStatusIcon()

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true
    }


    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))
        popUp.menu.add(0, 5, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_waiting)!!,myView.context), myView.context.getString(R.string.waiting)))

        popUp.setOnMenuItemClickListener { item: MenuItem? ->
            lead.statusID = item?.itemId.toString()
            setStatusIcon()

            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun setStatusIcon() {
        when (lead.statusID) {
            "1" -> statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            "2" -> statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            "3" -> statusBtn.setBackgroundResource(R.drawable.ic_done)
            "4" -> statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            "5" -> statusBtn.setBackgroundResource(R.drawable.ic_waiting)
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