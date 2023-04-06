package com.example.AdminMatic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditDepartmentBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule


class NewEditDepartmentFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var department: Department? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false


    //private lateinit var colorAdapter: ColorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            department = it.getParcelable("department")
        }
        if (department != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("departmentNewEdit")
    }

    private var _binding: FragmentNewEditDepartmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditDepartmentBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade && editsMadeDelayPassed){
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_department_bar, department!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_department_bar)
        }

        return myView
    }



    @SuppressLint("Range")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!editMode) {
            department = Department("0", "")
            department!!.color = GlobalVars.colorArray[0]

        }

        // Flag edits made false after all the views have time to set their states
        Timer("DepartmentEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }


        // Title
        binding.nameEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.nameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        /*
        // Charge Type Spinner
        val chargeTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))
        chargeTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            chargeTypeArray
        )
        binding.chargeTypeSpinner.adapter = chargeTypeAdapter
        binding.chargeTypeSpinner.onItemSelectedListener = this@NewEditContractFragment

        // Payment Terms Spinner
        val paymentTermsList = mutableListOf<String>()
        GlobalVars.paymentTerms!!.forEach {
            println("Name: ${it.name}")
            paymentTermsList.add(it.name)
        }
        paymentTermsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            paymentTermsList
        )
        binding.paymentTermsSpinner.adapter = paymentTermsAdapter
        binding.paymentTermsSpinner.onItemSelectedListener = this@NewEditContractFragment
        */

        // Color view
        binding.colorView.setOnClickListener {
            showColorMenu()
        }

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateDepartment()
            }
        }



        // ===== POPULATE FIELDS =====
        if (editMode) {
            binding.nameEt.setText(department!!.name)
        }
        binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(department!!.color))

        editsMade = false

    }

    private fun validateFields(): Boolean {


        /*
        if (contract!!.customer.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_lead_select_customer))
            return false
        }

        if (contract!!.title.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_wo_set_title))
            return false
        }

        //Skipping charge type and payment terms checking here since droid spinners can't be empty so it defaults to the first option

        if (contract!!.salesRep.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contract),getString(R.string.dialogue_incomplete_contract_select_sales_rep))
            return false
        }

         */

        return true
    }

    private fun updateDepartment() {
        println("updateDepartment")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/department.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        /*
                        val gson = GsonBuilder().create()
                        val newID: String = gson.fromJson(parentObject["contractID"].toString(), String::class.java)
                        contract!!.ID = newContractID
                         */
                        globalVars.playSaveSound(myView.context)
                        editsMade = false

                        myView.findNavController().navigateUp()

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
                params["id"] = department!!.ID
                params["name"] = binding.nameEt.text.toString()
                params["status"] = department!!.status.toString()
                params["color"] = department!!.color.toString()
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                println("params = $params")
                return params


            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    @SuppressLint("Range")
    private fun showColorMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.colorView)
        popUp.inflate(R.menu.task_status_menu)

        for (i in 0 until GlobalVars.colorArray.size) {
            val colorIcon: Drawable = AppCompatResources.getDrawable(myView.context, R.drawable.ic_online)!!
            colorIcon.setTint(Color.parseColor(department!!.color))



            popUp.menu.add(0, i, 1,globalVars.menuColor(GlobalVars.colorArray[i]))
            popUp.setOnMenuItemClickListener { item: MenuItem? ->
                binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(GlobalVars.colorArray[item!!.itemId]))
                department!!.color = GlobalVars.colorArray[item.itemId]

                editsMade = true

                true
            }
        }

        popUp.gravity = Gravity.START
        popUp.show()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true

        /*
        when (parent!!.id) {
            R.id.charge_type_spinner -> {
                val positionPlus1 = position+1
                contract!!.chargeType = positionPlus1.toString()
            }
        }
         */

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