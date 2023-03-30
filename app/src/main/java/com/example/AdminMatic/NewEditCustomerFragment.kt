package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditCustomerBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.concurrent.schedule


class NewEditCustomerFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var customerID: String? = null
    private lateinit var customer: Customer
    private lateinit var prefixAdapter: ArrayAdapter<String>
    private lateinit var stateAdapter: ArrayAdapter<String>
    private lateinit var stateBillingAdapter: ArrayAdapter<String>
    private lateinit var referredByAdapter: ArrayAdapter<HearType>
    private lateinit var prefixArray:Array<String>
    private lateinit var prefixArrayShort:Array<String>

    lateinit var globalVars:GlobalVars
    lateinit var myView:View
    
    private var editMode = false

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

    private var _binding: FragmentNewEditCustomerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditCustomerBinding.inflate(inflater, container, false)
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

        // Flag edits made false after all the views have time to set their states
        Timer("CustomerEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        customer = Customer("0", "")

        prefixArray = arrayOf(getString(R.string.new_customer_name_prefix_none_selected),
            getString(R.string.new_customer_name_prefix_mr),
            getString(R.string.new_customer_name_prefix_mrs),
            getString(R.string.new_customer_name_prefix_ms),
            getString(R.string.new_customer_name_prefix_miss),
            getString(R.string.new_customer_name_prefix_dr))

        prefixArrayShort = arrayOf(getString(R.string.new_customer_name_prefix_none_selected),
            getString(R.string.new_customer_name_prefix_mr_short),
            getString(R.string.new_customer_name_prefix_mrs_short),
            getString(R.string.new_customer_name_prefix_ms_short),
            getString(R.string.new_customer_name_prefix_miss_short),
            getString(R.string.new_customer_name_prefix_dr_short))


        // Name
        binding.newCustomerNamePrefixSpinner.setBackgroundResource(R.drawable.text_view_layout)
        prefixAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            prefixArray
        )
        prefixAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newCustomerNamePrefixSpinner.adapter = prefixAdapter
        binding.newCustomerNamePrefixSpinner.onItemSelectedListener = this@NewEditCustomerFragment

        binding.newCustomerNameMiddleEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerNameMiddleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerNameLastEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerNameLastEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerNameBusinessEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerNameBusinessEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerNameSystemEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerNameSystemEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newCustomerBusinessSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                binding.newCustomerNamePrefixCl.visibility = View.GONE
                binding.newCustomerNameFirstCl.visibility = View.GONE
                binding.newCustomerNameMiddleCl.visibility = View.GONE
                binding.newCustomerNameLastCl.visibility = View.GONE
                binding.newCustomerNameBusinessCl.visibility = View.VISIBLE
            }
            else {
                binding.newCustomerNamePrefixCl.visibility = View.VISIBLE
                binding.newCustomerNameFirstCl.visibility = View.VISIBLE
                binding.newCustomerNameMiddleCl.visibility = View.VISIBLE
                binding.newCustomerNameLastCl.visibility = View.VISIBLE
                binding.newCustomerNameBusinessCl.visibility = View.GONE
            }
        }

        // Contact
        binding.newCustomerContactPhoneEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerContactPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerContactEmailEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerContactEmailEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Address
        binding.newCustomerAddressStreet1Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressStreet1Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerAddressStreet2Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressStreet2Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerAddressStreet3Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressStreet3Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerAddressStreet4Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressStreet4Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerAddressCityEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressCityEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newCustomerAddressStateSpinner.setBackgroundResource(R.drawable.text_view_layout)
        stateAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.states
        )
        stateAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newCustomerAddressStateSpinner.adapter = stateAdapter
        binding.newCustomerAddressStateSpinner.onItemSelectedListener = this@NewEditCustomerFragment

        binding.newCustomerAddressZipEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerAddressZipEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Billing Address
        binding.newCustomerBillingSameSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                binding.newCustomerBillingAddressLl.visibility = View.GONE
            }
            else {
                binding.newCustomerBillingAddressLl.visibility = View.VISIBLE
            }
        }
        binding.newCustomerBillingAddressStreet1Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressStreet1Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerBillingAddressStreet1Et.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (customer.parentID != "0" && binding.newCustomerNameSystemEt.text.isBlank()) {
                    binding.newCustomerNameSystemEt.text = binding.newCustomerBillingAddressStreet1Et.text
                }
            }
        }
        binding.newCustomerBillingAddressStreet2Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressStreet2Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerBillingAddressStreet3Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressStreet3Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerBillingAddressStreet4Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressStreet4Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newCustomerBillingAddressCityEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressCityEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newCustomerBillingAddressStateSpinner.setBackgroundResource(R.drawable.text_view_layout)
        stateBillingAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.states
        )
        stateBillingAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newCustomerBillingAddressStateSpinner.adapter = stateBillingAdapter
        binding.newCustomerBillingAddressStateSpinner.onItemSelectedListener = this@NewEditCustomerFragment

        binding.newCustomerBillingAddressZipEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newCustomerBillingAddressZipEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Referred by
        binding.newCustomerReferralSpinner.setBackgroundResource(R.drawable.text_view_layout)
        referredByAdapter = ArrayAdapter<HearType>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.hearTypes!!
        )
        referredByAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newCustomerReferralSpinner.adapter = referredByAdapter
        binding.newCustomerReferralSpinner.onItemSelectedListener = this@NewEditCustomerFragment
        // Set to "none specified" by default
        binding.newCustomerReferralSpinner.setSelection(7)


        binding.newCustomerActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                customer.active = "1"
            }
            else {
                customer.active = "0"
            }
        }

        binding.newCustomerSubmitBtn.setOnClickListener {
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


        when (parent!!.id) {

            R.id.new_customer_name_prefix_spinner -> {
                println("${prefixArray[position]} selected")
                customer.salutation = prefixArray[position]
            }

            R.id.new_customer_referral_spinner -> {
                println("onItemSelected position = $position")
                println("weekSpinner.getTag(R.id.pos) = ${binding.newCustomerReferralSpinner.getTag(R.id.pos)}")

                if(binding.newCustomerReferralSpinner.getTag(R.id.pos) != null && binding.newCustomerReferralSpinner.getTag(R.id.pos) != position){
                    println("tag != pos")
                }


                val hear = parent.selectedItem as (HearType)
                customer.hear = hear.ID
                println("heartype: ${customer.hear}")


                referredBySpinnerPosition = position
                binding.newCustomerReferralSpinner.setTag(R.id.pos, position)
            }
        }






    }

    private fun getCustomer(){
        println("getCustomer = ${customer.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customer.php"

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
                        val customerArray = gson.fromJson(parentObject.toString(), CustomerArray::class.java)

                        customer = customerArray.customers[0]


                        populateFields(false)
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
                params["ID"] = customerID!!
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun populateFields(fromSelected: Boolean) {

        println("populateFields")

        if (!customer.companyName.isNullOrBlank()) {
            binding.newCustomerBusinessSwitch.isChecked = true
            binding.newCustomerBusinessSwitch.jumpDrawablesToCurrentState()
        }


        var indexOfPrefix = prefixArray.indexOf(customer.salutation)
        // Also check for older inputs with "Mr" instead of "Mr." etc
        if (indexOfPrefix == -1)  {
            indexOfPrefix = prefixArrayShort.indexOf(customer.salutation)
        }

        if (indexOfPrefix != -1) {
            binding.newCustomerNamePrefixSpinner.setSelection(indexOfPrefix)
        }



        binding.newCustomerNameFirstEt.setText(customer.fname)
        binding.newCustomerNameMiddleEt.setText(customer.mname)
        binding.newCustomerNameLastEt.setText(customer.lname)
        binding.newCustomerNameBusinessEt.setText(customer.companyName)
        binding.newCustomerNameSystemEt.setText(customer.sysname)
        binding.newCustomerContactPhoneEt.setText(customer.phone)
        binding.newCustomerContactEmailEt.setText(customer.email)

        if (!fromSelected) {
            binding.newCustomerAddressStreet1Et.setText(customer.jobStreet1)
            binding.newCustomerAddressStreet2Et.setText(customer.jobStreet2)
            binding.newCustomerAddressStreet3Et.setText(customer.jobStreet3)
            binding.newCustomerAddressStreet4Et.setText(customer.jobStreet4)
            binding.newCustomerAddressCityEt.setText(customer.jobCity)
            val addressStateIndex = GlobalVars.statesShort.indexOf(customer.jobState)
            if (addressStateIndex != -1) {
                binding.newCustomerAddressStateSpinner.setSelection(addressStateIndex)
            }
            binding.newCustomerAddressZipEt.setText(customer.jobZip)

            binding.newCustomerContactPhoneEt.setText(customer.phone)
            binding.newCustomerContactEmailEt.setText(customer.email)

        }

        binding.newCustomerBillingAddressStreet1Et.setText(customer.billStreet1)
        binding.newCustomerBillingAddressStreet2Et.setText(customer.billStreet2)
        binding.newCustomerBillingAddressStreet3Et.setText(customer.billStreet3)
        binding.newCustomerBillingAddressStreet4Et.setText(customer.billStreet4)
        binding.newCustomerBillingAddressCityEt.setText(customer.billCity)
        val billingAddressStateIndex = GlobalVars.statesShort.indexOf(customer.billState)
        if (billingAddressStateIndex != -1) {
            binding.newCustomerBillingAddressStateSpinner.setSelection(billingAddressStateIndex)
        }
        binding.newCustomerBillingAddressZipEt.setText(customer.billZip)


        // In edit mode, disable the same as job site address switch
        if (editMode) {
            binding.newCustomerBillingSameSwitch.isChecked = false
            binding.newCustomerBillingSameSwitch.jumpDrawablesToCurrentState()
            binding.newCustomerBillingSameSwitch.visibility = View.GONE
            binding.newCustomerCopyBillingSwitchTv.visibility = View.GONE
        }

        if (fromSelected) {
            binding.newCustomerBillingSameSwitch.isChecked = false
            binding.newCustomerBillingSameSwitch.jumpDrawablesToCurrentState()
        }



        println("hear: ${customer.hear}")
        binding.newCustomerReferralSpinner.setSelection(7)
        for (i in 0 until GlobalVars.hearTypes!!.size) {
            val adapterItem = referredByAdapter.getItem(i)
            if (adapterItem!!.ID.toInt() == customer.hear!!.toInt()) {
                binding.newCustomerReferralSpinner.setSelection(i)
            }
        }
        if (customer.active == "0") {
            binding.newCustomerBusinessSwitch.isChecked = false
            binding.newCustomerBusinessSwitch.jumpDrawablesToCurrentState()
        }
        editsMade = false
        hideProgressView()
    }

    private fun updateCustomer() {
        // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/customer.php"

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
                        val newCustID: String = gson.fromJson(parentObject["custID"].toString(), String::class.java)


                        globalVars.playSaveSound(myView.context)
                        editsMade = false

                        if (editMode) {
                            editsMade = false
                            myView.findNavController().navigateUp()

                        } else {
                            val directions =
                                NewEditCustomerFragmentDirections.navigateToCustomer(newCustID)
                            myView.findNavController().navigate(directions)
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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["parentID"] = customer.parentID!!
                params["companyName"] = binding.newCustomerNameBusinessEt.text.toString()
                params["salutation"] = customer.salutation.toString()
                params["firstName"] = binding.newCustomerNameFirstEt.text.toString()
                params["middleName"] = binding.newCustomerNameMiddleEt.text.toString()
                params["lastName"] = binding.newCustomerNameLastEt.text.toString()
                params["sysName"] = binding.newCustomerNameSystemEt.text.toString()
                params["nameChange"] = "0"
                if (editMode) {
                    if (binding.newCustomerNameSystemEt.text.toString() != customer.sysname) {
                        println("edit mode, name was changed, so nameChange = 1")
                        params["nameChange"] = "1"
                    }
                }

                params["mainPhone"] = binding.newCustomerContactPhoneEt.text.toString()
                params["mainEmail"] = binding.newCustomerContactEmailEt.text.toString()
                params["jobStreet1"] = binding.newCustomerAddressStreet1Et.text.toString()
                params["jobStreet2"] = binding.newCustomerAddressStreet2Et.text.toString()
                params["jobStreet3"] = binding.newCustomerAddressStreet3Et.text.toString()
                params["jobStreet4"] = binding.newCustomerAddressStreet4Et.text.toString()
                params["jobCity"] = binding.newCustomerAddressCityEt.text.toString()
                if (binding.newCustomerAddressStateSpinner.selectedItemPosition > 0) {
                    params["jobState"] = GlobalVars.statesShort[binding.newCustomerAddressStateSpinner.selectedItemPosition]
                }
                else {
                    params["jobState"] = ""
                }
                params["jobZip"] = binding.newCustomerAddressZipEt.text.toString()
                if (editMode) {
                    val newState = GlobalVars.statesShort[binding.newCustomerAddressStateSpinner.selectedItemPosition]
                    if (binding.newCustomerAddressStreet1Et.text.toString() != customer.jobStreet1 ||
                        binding.newCustomerAddressStreet2Et.text.toString() != customer.jobStreet2 ||
                        binding.newCustomerAddressStreet3Et.text.toString() != customer.jobStreet3 ||
                        binding.newCustomerAddressStreet4Et.text.toString() != customer.jobStreet4 ||
                        binding.newCustomerAddressCityEt.text.toString() != customer.jobCity ||
                        newState != customer.jobState ||
                        binding.newCustomerAddressZipEt.text.toString() != customer.jobZip) {
                        params["jobSiteChange"] = "1"
                    }
                    else {
                        params["jobSiteChange"] = "0"
                    }
                } else {
                    params["jobSiteChange"] = "0"
                }

                if (binding.newCustomerBillingSameSwitch.isChecked) {
                    // If "same as job site address" is checked, use same values as regular address with name added
                    var nameString = ""
                    nameString += customer.salutation
                    nameString += binding.newCustomerNameFirstEt.text
                    nameString += binding.newCustomerNameMiddleEt.text
                    nameString += binding.newCustomerNameLastEt.text
                    params["billStreet1"] = nameString
                    params["billStreet2"] = binding.newCustomerAddressStreet1Et.text.toString()
                    params["billStreet3"] = binding.newCustomerAddressStreet2Et.text.toString()
                    params["billStreet4"] = binding.newCustomerAddressStreet3Et.text.toString()
                    params["billCity"] = binding.newCustomerAddressCityEt.text.toString()
                    if (binding.newCustomerAddressStateSpinner.selectedItemPosition > 0) {
                        params["billState"] = GlobalVars.statesShort[binding.newCustomerAddressStateSpinner.selectedItemPosition]
                    }
                    else {
                        params["billState"] = ""
                    }
                    params["billZip"] = binding.newCustomerAddressZipEt.text.toString()
                }
                else {
                    // If not, take the values put in the fields
                    params["billStreet1"] = binding.newCustomerBillingAddressStreet1Et.text.toString()
                    params["billStreet2"] = binding.newCustomerBillingAddressStreet2Et.text.toString()
                    params["billStreet3"] = binding.newCustomerBillingAddressStreet3Et.text.toString()
                    params["billStreet4"] = binding.newCustomerBillingAddressStreet4Et.text.toString()
                    params["billCity"] = binding.newCustomerBillingAddressCityEt.text.toString()
                    if (binding.newCustomerBillingAddressStateSpinner.selectedItemPosition > 0) {
                        params["billState"] = GlobalVars.statesShort[binding.newCustomerBillingAddressStateSpinner.selectedItemPosition]
                    }
                    else {
                        params["billState"] = ""
                    }
                    params["billZip"] = binding.newCustomerBillingAddressZipEt.text.toString()
                }

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
        if (binding.newCustomerNameSystemEt.text.length > 41) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_too_long_title),getString(R.string.dialogue_sysname_too_long_body))
            return false
        }

        if (binding.newCustomerNameSystemEt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_missing_title),getString(R.string.dialogue_sysname_missing_body))
            return false
        }

        if (binding.newCustomerBillingAddressStreet1Et.text.length > 31) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_billing_name_too_long_title),getString(R.string.dialogue_billing_name_too_long_body))
            return false
        }

        return true
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