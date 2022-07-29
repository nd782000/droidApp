package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class NewEditCustomerFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false

    private var customerID: String? = null
    private lateinit var customer: Customer
    private lateinit var adapter: ArrayAdapter<HearType>

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var allCl: ConstraintLayout
    private var editMode = false


    // Customer Name
    private lateinit var isBusinessSwitch: SwitchCompat
    private lateinit var namePrefixEt: EditText
    private lateinit var nameFirstEt: EditText
    private lateinit var nameMiddleEt: EditText
    private lateinit var nameLastEt: EditText
    private lateinit var nameBusinessEt: EditText
    private lateinit var nameSystemEt: EditText
        // CLs so we can hide/show sections with the switch
    private lateinit var namePrefixCl: ConstraintLayout
    private lateinit var nameFirstCl: ConstraintLayout
    private lateinit var nameMiddleCl: ConstraintLayout
    private lateinit var nameLastCl: ConstraintLayout
    private lateinit var nameBusinessCl: ConstraintLayout

    // Contact Info
    private lateinit var contactPhoneEt: EditText
    private lateinit var contactEmailEt: EditText

    // Job Site Address
    private lateinit var addressStreet1: EditText
    private lateinit var addressStreet2: EditText
    private lateinit var addressStreet3: EditText
    private lateinit var addressStreet4: EditText
    private lateinit var addressCity: EditText
    private lateinit var addressState: EditText
    private lateinit var addressZip: EditText

    // Billing Address
    private lateinit var billingAddressCopyBtn: Button
    private lateinit var billingAddressStreet1: EditText
    private lateinit var billingAddressStreet2: EditText
    private lateinit var billingAddressStreet3: EditText
    private lateinit var billingAddressStreet4: EditText
    private lateinit var billingAddressCity: EditText
    private lateinit var billingAddressState: EditText
    private lateinit var billingAddressZip: EditText
    
    private lateinit var referredBySpinner: Spinner
    private lateinit var isActiveSwitch: SwitchCompat
    
    private lateinit var submitBtn: Button

    private var referredBySpinnerPosition: Int = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customerID = it.getString("customerID")
        }
        if (customerID != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("customerNewEdit")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_new_edit_customer, container, false)

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
        if (customerID == null) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_customer)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_customer, customerID)
        }
        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customer = Customer("0", "")

        // =================================
        // ===== FETCH VIEW REFERENCES =====
        // =================================
        pgsBar = view.findViewById(R.id.progress_bar)
        allCl = view.findViewById(R.id.all_cl)
        
        // Name
        isBusinessSwitch = view.findViewById(R.id.new_customer_business_switch)

        namePrefixEt = view.findViewById(R.id.new_customer_name_prefix_et)
        nameFirstEt = view.findViewById(R.id.new_customer_name_first_et)
        nameMiddleEt = view.findViewById(R.id.new_customer_name_middle_et)
        nameLastEt = view.findViewById(R.id.new_customer_name_last_et)
        nameBusinessEt = view.findViewById(R.id.new_customer_name_business_et)
        nameSystemEt = view.findViewById(R.id.new_customer_name_system_et)
        namePrefixCl = view.findViewById(R.id.new_customer_name_prefix_cl)
        nameFirstCl = view.findViewById(R.id.new_customer_name_first_cl)
        nameMiddleCl = view.findViewById(R.id.new_customer_name_middle_cl)
        nameLastCl = view.findViewById(R.id.new_customer_name_last_cl)
        nameBusinessCl = view.findViewById(R.id.new_customer_name_business_cl)

        // Contact
        contactPhoneEt = view.findViewById(R.id.new_customer_contact_phone_et)
        contactEmailEt = view.findViewById(R.id.new_customer_contact_email_et)

        // Address
        addressStreet1 = view.findViewById(R.id.new_customer_address_street1_et)
        addressStreet2 = view.findViewById(R.id.new_customer_address_street2_et)
        addressStreet3 = view.findViewById(R.id.new_customer_address_street3_et)
        addressStreet4 = view.findViewById(R.id.new_customer_address_street4_et)
        addressCity = view.findViewById(R.id.new_customer_address_city_et)
        addressState = view.findViewById(R.id.new_customer_address_state_et)
        addressZip = view.findViewById(R.id.new_customer_address_zip_et)

        // Billing Address
        billingAddressCopyBtn = view.findViewById(R.id.new_customer_copy_address_btn)
        billingAddressStreet1 = view.findViewById(R.id.new_customer_billing_address_street1_et)
        billingAddressStreet2 = view.findViewById(R.id.new_customer_billing_address_street2_et)
        billingAddressStreet3 = view.findViewById(R.id.new_customer_billing_address_street3_et)
        billingAddressStreet4 = view.findViewById(R.id.new_customer_billing_address_street4_et)
        billingAddressCity = view.findViewById(R.id.new_customer_billing_address_city_et)
        billingAddressState = view.findViewById(R.id.new_customer_billing_address_state_et)
        billingAddressZip = view.findViewById(R.id.new_customer_billing_address_zip_et)
        
        // Referred by
        referredBySpinner = view.findViewById(R.id.new_customer_referral_spinner)
        isActiveSwitch = view.findViewById(R.id.new_customer_active_switch)

        //Submit Button
        submitBtn = view.findViewById(R.id.new_customer_submit_btn)


        // =================================
        // ========= SET UP VIEWS ==========
        // =================================

        // Name
        namePrefixEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        namePrefixEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        nameMiddleEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        nameMiddleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        nameLastEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        nameLastEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        nameBusinessEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        nameBusinessEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        nameSystemEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        nameSystemEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        isBusinessSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                namePrefixCl.visibility = View.GONE
                nameFirstCl.visibility = View.GONE
                nameMiddleCl.visibility = View.GONE
                nameLastCl.visibility = View.GONE
                nameBusinessCl.visibility = View.VISIBLE
            }
            else {
                namePrefixCl.visibility = View.VISIBLE
                nameFirstCl.visibility = View.VISIBLE
                nameMiddleCl.visibility = View.VISIBLE
                nameLastCl.visibility = View.VISIBLE
                nameBusinessCl.visibility = View.GONE
            }
        }

        // Contact
        contactPhoneEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        contactPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        contactEmailEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        contactEmailEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Address
        addressStreet1.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressStreet1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressStreet2.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressStreet2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressStreet3.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressStreet3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressStreet4.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressStreet4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressCity.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressState.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressState.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        addressZip.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        addressZip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Billing Address
        billingAddressCopyBtn.setOnClickListener {
            editsMade = true
            billingAddressStreet1.text = addressStreet1.text
            billingAddressStreet2.text = addressStreet2.text
            billingAddressStreet3.text = addressStreet3.text
            billingAddressStreet4.text = addressStreet4.text
            billingAddressCity.text = addressCity.text
            billingAddressState.text = addressState.text
            billingAddressZip.text = addressZip.text
        }
        billingAddressStreet1.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressStreet1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressStreet2.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressStreet2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressStreet3.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressStreet3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressStreet4.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressStreet4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressCity.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressState.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressState.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        billingAddressZip.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        billingAddressZip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Referred by
        referredBySpinner = view.findViewById(R.id.new_customer_referral_spinner)
        referredBySpinner.setBackgroundResource(R.drawable.text_view_layout)


        adapter = ArrayAdapter<HearType>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.hearTypes!!
        )


        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        referredBySpinner.adapter = adapter
        referredBySpinner.onItemSelectedListener = this@NewEditCustomerFragment
        // Set to "none specified" by default
        referredBySpinner.setSelection(7)


        isActiveSwitch = view.findViewById(R.id.new_customer_active_switch)
        isActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                customer.active = "1"
            }
            else {
                customer.active = "0"
            }
        }

        submitBtn.setOnClickListener {
            if (validateFields()) {
                updateCustomer()
            }
        }

        if (editMode) {
            println("edit mode")
            getCustomer()
        }

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true
        println("onItemSelected position = $position")
        println("weekSpinner.getTag(R.id.pos) = ${referredBySpinner.getTag(R.id.pos)}")

        if(referredBySpinner.getTag(R.id.pos) != null && referredBySpinner.getTag(R.id.pos) != position){
            println("tag != pos")
        }


        val hear = parent!!.selectedItem as (HearType)
        customer.hear = hear.ID
        println("heartype: ${customer.hear}")


        referredBySpinnerPosition = position
        referredBySpinner.setTag(R.id.pos, position)




    }

    private fun getCustomer(){
        println("getCustomer = ${customer.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/customer.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    val customerArray = gson.fromJson(parentObject.toString() ,CustomerArray::class.java)

                    customer = customerArray.customers[0]


                    populateFields()
                    editsMade = false

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
                params["ID"] = customerID!!
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun populateFields() {
        if (!customer.companyName.isNullOrBlank()) {
            isBusinessSwitch.isChecked = true
            isBusinessSwitch.jumpDrawablesToCurrentState()
        }
        namePrefixEt.setText(customer.salutation)
        nameFirstEt.setText(customer.fname)
        nameMiddleEt.setText(customer.mname)
        nameLastEt.setText(customer.lname)
        nameBusinessEt.setText(customer.companyName)
        nameSystemEt.setText(customer.sysname)
        contactPhoneEt.setText(customer.phone)
        contactEmailEt.setText(customer.email)
        addressStreet1.setText(customer.jobStreet1)
        addressStreet2.setText(customer.jobStreet2)
        addressStreet3.setText(customer.jobStreet3)
        addressStreet4.setText(customer.jobStreet4)
        addressCity.setText(customer.jobCity)
        addressState.setText(customer.jobState)
        addressZip.setText(customer.jobZip)
        billingAddressStreet1.setText(customer.billStreet1)
        billingAddressStreet2.setText(customer.billStreet2)
        billingAddressStreet3.setText(customer.billStreet3)
        billingAddressStreet4.setText(customer.billStreet4)
        billingAddressCity.setText(customer.billCity)
        billingAddressState.setText(customer.billState)
        billingAddressZip.setText(customer.billZip)

        println("hear: ${customer.hear}")
        referredBySpinner.setSelection(7)
        for (i in 0 until GlobalVars.hearTypes!!.size) {
            val adapterItem = adapter.getItem(i)
            if (adapterItem!!.ID.toInt() == customer.hear!!.toInt()) {
                referredBySpinner.setSelection(i)
            }
        }
        if (customer.active == "0") {
            isBusinessSwitch.isChecked = false
            isBusinessSwitch.jumpDrawablesToCurrentState()
        }
    }

    private fun updateCustomer() {
        // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/update/customer.php"

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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    val customerArray = gson.fromJson(parentObject.toString() ,CustomerArray::class.java)
                    customer = customerArray.customers[0]
                    globalVars.playSaveSound(myView.context)
                    editsMade = false

                    if (editMode) {
                        populateFields()
                        editsMade = false
                        hideProgressView()
                    }
                    else {
                        val directions = NewEditCustomerFragmentDirections.navigateToCustomer(customer.ID)
                        myView.findNavController().navigate(directions)
                    }





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
                params["companyName"] = nameBusinessEt.text.toString()
                params["salutation"] = namePrefixEt.text.toString()
                params["firstName"] = nameFirstEt.text.toString()
                params["middleName"] = nameMiddleEt.text.toString()
                params["lastName"] = nameLastEt.text.toString()
                params["sysName"] = nameSystemEt.text.toString()
                if (editMode) {
                    if (nameSystemEt.text.toString() != customer.sysname) {
                        params["nameChange"] = "1"
                    }
                    else {
                        params["nameChange"] = "0"
                    }
                }
                //Todo: re-enable these once they're in the PHP code
                //params["mainPhone"] = contactPhoneEt.text.toString()
                //params["mainEmail"] = contactEmailEt.text.toString()
                params["jobStreet1"] = addressStreet1.text.toString()
                params["jobStreet2"] = addressStreet2.text.toString()
                params["jobStreet3"] = addressStreet3.text.toString()
                params["jobStreet4"] = addressStreet4.text.toString()
                params["jobCity"] = addressCity.text.toString()
                params["jobState"] = addressState.text.toString()
                params["jobZip"] = addressZip.text.toString()
                if (editMode) {
                    if (addressStreet1.text.toString() != customer.jobStreet1 ||
                        addressStreet2.text.toString() != customer.jobStreet2 ||
                        addressStreet3.text.toString() != customer.jobStreet3 ||
                        addressStreet4.text.toString() != customer.jobStreet4 ||
                        addressCity.text.toString() != customer.jobCity ||
                        addressState.text.toString() != customer.jobState ||
                        addressZip.text.toString() != customer.jobZip) {
                        params["jobSiteChange"] = "1"
                    }
                    else {
                        params["jobSiteChange"] = "0"
                    }
                }
                params["billStreet1"] = billingAddressStreet1.text.toString()
                params["billStreet2"] = billingAddressStreet2.text.toString()
                params["billStreet3"] = billingAddressStreet3.text.toString()
                params["billStreet4"] = billingAddressStreet4.text.toString()
                params["billCity"] = billingAddressCity.text.toString()
                params["billState"] = billingAddressState.text.toString()
                params["billZip"] = billingAddressZip.text.toString()
                params["hear"] = customer.hear.toString()
                params["active"] = customer.active!!
                if (editMode) {
                    params["customerID"] = customerID.toString()
                }
                else {
                    params["customerID"] = "0"
                }
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun validateFields(): Boolean {
        if (nameSystemEt.text.length > 41) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_too_long_title),getString(R.string.dialogue_sysname_too_long_body))
            return false
        }

        if (nameSystemEt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_missing_title),getString(R.string.dialogue_sysname_missing_body))
            return false
        }

        if (billingAddressStreet1.text.length > 31) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_billing_name_too_long_title),getString(R.string.dialogue_billing_name_too_long_body))
            return false
        }

        return true
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