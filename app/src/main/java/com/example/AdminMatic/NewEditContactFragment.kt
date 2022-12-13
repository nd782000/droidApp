package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditContactBinding
import com.AdminMatic.databinding.FragmentNewEditLeadBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class NewEditContactFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var contact: Contact? = null
    private var customer: Customer? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false

    private lateinit var typeAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contact = it.getParcelable("contact")
            customer = it.getParcelable("customer")
        }
        if (contact != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contactNewEdit")
    }

    private var _binding: FragmentNewEditContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditContactBinding.inflate(inflater, container, false)
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
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_contact_bar)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_contact_bar)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!editMode) {
            contact = Contact("0", "1")
        }

        // Flag edits made false after all the views have time to set their states
        Timer("ContactEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        // Type Spinner
        val contactTypesList = mutableListOf<String>()
        GlobalVars.contactTypes!!.forEach {
            contactTypesList.add(it.name)
        }

        typeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            contactTypesList
        )
        binding.typeSpinner.adapter = typeAdapter
        binding.typeSpinner.onItemSelectedListener = this@NewEditContactFragment


        // Preferred switch
        binding.preferredSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                contact!!.preferred = "1"
            }
            else {
                contact!!.preferred = "0"
            }
        }

        // Name
        binding.nameEditText.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Value
        binding.valueEditText.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.valueEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateContact()
            }
        }



        // ===== POPULATE FIELDS =====
        if (editMode) {

            if (contact!!.type.isNotBlank()) {
                for (i in 0 until GlobalVars.contactTypes!!.size) {
                    if (contact!!.type == GlobalVars.contactTypes!![i].ID) {
                        binding.typeSpinner.setSelection(i)
                        break
                    }
                }
            }

            binding.nameEditText.setText(contact!!.name)
            binding.valueEditText.setText(contact!!.value)

            if (contact!!.preferred == "1") {
                binding.preferredSwitch.isChecked = true
                binding.preferredSwitch.jumpDrawablesToCurrentState()
            }
        }

        editsMade = false

    }

    private fun validateFields(): Boolean {

        if (binding.nameEditText.text.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contact),getString(R.string.dialogue_incomplete_contact_enter_name))
            return false
        }

        if (binding.valueEditText.text.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_incomplete_contact),getString(R.string.dialogue_incomplete_contact_enter_value))
            return false
        }

        return true
    }

    private fun updateContact() {
        println("updateContact")

        showProgressView()
        contact!!.name = binding.nameEditText.text.toString()
        contact!!.value = binding.valueEditText.text.toString()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contact.php"

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

                        val gson = GsonBuilder().create()
                        val newContactID: String = gson.fromJson(parentObject["contactID"].toString(), String::class.java)
                        contact!!.ID = newContactID


                        val errorArray: JSONArray = parentObject.getJSONArray("errorArray")
                        val errors = gson.fromJson(errorArray.toString(), Array<String>::class.java)

                        if (errors.isEmpty()) {
                            globalVars.playSaveSound(myView.context)
                            editsMade = false

                            if (!editMode) {
                                val tempMutableList = customer!!.contacts.toMutableList()
                                tempMutableList.add(contact!!)
                                customer!!.contacts = tempMutableList.toTypedArray()
                            }

                            myView.findNavController().navigateUp()
                        }
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
                params["type"] = contact!!.type
                params["name"] = contact!!.name.toString()
                params["value"] = contact!!.value.toString()
                params["contactID"] = contact!!.ID
                params["custID"] = customer!!.ID
                params["preferred"] = contact!!.preferred.toString()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true
        contact!!.type = GlobalVars.contactTypes!![position].ID

        when (contact!!.type) {
            "1" -> { // Main Phone
                binding.nameText.text = getString(R.string.new_contact_name_main_phone)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "2" -> { // Main Email
                binding.nameText.text = getString(R.string.new_contact_name_main_email)
                binding.valueText.text = getString(R.string.new_contact_value_email)
                binding.valueEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            "5" -> { // Website
                binding.nameText.text = getString(R.string.new_contact_name_website)
                binding.valueText.text = getString(R.string.new_contact_value_website)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_TEXT
            }
            "6" -> { // Alt Contact
                binding.nameText.text = getString(R.string.new_contact_name_alt_contact)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "7" -> { // Fax
                binding.nameText.text = getString(R.string.new_contact_name_fax)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "8" -> { // Alt Phone
                binding.nameText.text = getString(R.string.new_contact_name_alt_phone)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "9" -> { // Alt Email
                binding.nameText.text = getString(R.string.new_contact_name_alt_email)
                binding.valueText.text = getString(R.string.new_contact_value_email)
                binding.valueEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            "10" -> { // Mobile
                binding.nameText.text = getString(R.string.new_contact_name_cell)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "11" -> { // Alt Mobile
                binding.nameText.text = getString(R.string.new_contact_name_alt_cell)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "12" -> { // Home
                binding.nameText.text = getString(R.string.new_contact_name_home)
                binding.valueText.text = getString(R.string.new_contact_value_phone)
                binding.valueEditText.inputType = InputType.TYPE_CLASS_PHONE
            }
            "13" -> { // Alt Email
                binding.nameText.text = getString(R.string.new_contact_name_alt_email)
                binding.valueText.text = getString(R.string.new_contact_value_email)
                binding.valueEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
        }

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