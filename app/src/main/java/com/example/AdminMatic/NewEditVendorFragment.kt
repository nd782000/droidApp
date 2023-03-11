package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.provider.Settings
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
import com.AdminMatic.databinding.FragmentNewEditVendorBinding
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


class NewEditVendorFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var vendor: Vendor? = null
    private lateinit var prefixAdapter: ArrayAdapter<String>
    private lateinit var prefixArray:Array<String>
    private lateinit var prefixArrayShort:Array<String>

    private lateinit var stateAdapter: ArrayAdapter<String>

    private lateinit var stateBillingAdapter: ArrayAdapter<String>

    private lateinit var docTypeAdapter: ArrayAdapter<String>
    private lateinit var docTypeArray: Array<String>

    private lateinit var paymentTermsAdapter: ArrayAdapter<String>

    private lateinit var categoryAdapter: ArrayAdapter<String>
    //private var categoryArray = arrayOf("test1", "test2", "test3")




    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false
    private var parentName = ""

    private var referredBySpinnerPosition: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable("vendor")
        }
        if (vendor != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("vendorNewEdit")
    }

    private var _binding: FragmentNewEditVendorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditVendorBinding.inflate(inflater, container, false)
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
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_customer, vendor!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_customer)
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

        if (!editMode) {
            vendor = Vendor("0", "")
        }

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

        docTypeArray = arrayOf(
            getString(R.string.pref_doc_type_none),
            getString(R.string.pref_doc_type_mail),
            getString(R.string.pref_doc_type_email)
        )





        // Name
        binding.newVendorNamePrefixSpinner.setBackgroundResource(R.drawable.text_view_layout)
        prefixAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            prefixArray
        )
        prefixAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorNamePrefixSpinner.adapter = prefixAdapter
        binding.newVendorNamePrefixSpinner.onItemSelectedListener = this@NewEditVendorFragment

        binding.newVendorNameMiddleEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorNameMiddleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorNameLastEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorNameLastEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorNameBusinessEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorNameBusinessEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorNameSystemEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorNameSystemEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newVendorNameSystemEt.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.newVendorNameSystemEt.text.isBlank() && binding.newVendorNameFirstEt.text.isNotBlank() && binding.newVendorNameLastEt.text.isNotBlank()) {
                    binding.newVendorNameSystemEt.setText(getString(R.string.new_customer_autofill_sysname, binding.newVendorNameLastEt.text, binding.newVendorNameFirstEt.text))
                }
            }
        }

        binding.newVendorBusinessSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                binding.newVendorNamePrefixCl.visibility = View.GONE
                binding.newVendorNameFirstCl.visibility = View.GONE
                binding.newVendorNameMiddleCl.visibility = View.GONE
                binding.newVendorNameLastCl.visibility = View.GONE
                binding.newVendorNameBusinessCl.visibility = View.VISIBLE
            }
            else {
                binding.newVendorNamePrefixCl.visibility = View.VISIBLE
                binding.newVendorNameFirstCl.visibility = View.VISIBLE
                binding.newVendorNameMiddleCl.visibility = View.VISIBLE
                binding.newVendorNameLastCl.visibility = View.VISIBLE
                binding.newVendorNameBusinessCl.visibility = View.GONE
            }
        }

        // Contact
        binding.newVendorContactPhoneEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorContactPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorContactEmailEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorContactEmailEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Address
        binding.newVendorAddressStreet1Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressStreet1Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorAddressStreet2Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressStreet2Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorAddressStreet3Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressStreet3Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorAddressStreet4Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressStreet4Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorAddressCityEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressCityEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newVendorAddressStateSpinner.setBackgroundResource(R.drawable.text_view_layout)
        stateAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.states
        )
        stateAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorAddressStateSpinner.adapter = stateAdapter
        binding.newVendorAddressStateSpinner.onItemSelectedListener = this@NewEditVendorFragment

        binding.newVendorAddressZipEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorAddressZipEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        // Billing Address
        binding.newVendorBillingSameSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                binding.newVendorBillingAddressLl.visibility = View.GONE
            }
            else {
                binding.newVendorBillingAddressLl.visibility = View.VISIBLE
            }
        }
        binding.newVendorBillingAddressStreet1Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressStreet1Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorBillingAddressStreet2Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressStreet2Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorBillingAddressStreet3Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressStreet3Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorBillingAddressStreet4Et.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressStreet4Et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })
        binding.newVendorBillingAddressCityEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressCityEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newVendorBillingAddressStateSpinner.setBackgroundResource(R.drawable.text_view_layout)
        stateBillingAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            GlobalVars.states
        )
        stateBillingAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorBillingAddressStateSpinner.adapter = stateBillingAdapter
        binding.newVendorBillingAddressStateSpinner.onItemSelectedListener = this@NewEditVendorFragment

        binding.newVendorBillingAddressZipEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.newVendorBillingAddressZipEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.newVendorActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                vendor!!.active = "1"
            }
            else {
                vendor!!.active = "0"
            }
        }


        // Preferred Document Type
        binding.newVendorDocTypeSpinner.setBackgroundResource(R.drawable.text_view_layout)
        docTypeAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            docTypeArray
        )
        docTypeAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorDocTypeSpinner.adapter = docTypeAdapter
        binding.newVendorDocTypeSpinner.onItemSelectedListener = this@NewEditVendorFragment
        // Set to "none specified" by default

        // Payment Terms
        binding.newVendorTermsSpinner.setBackgroundResource(R.drawable.text_view_layout)
        val paymentTermsList = mutableListOf<String>()
        GlobalVars.paymentTerms!!.forEach {
            paymentTermsList.add(it.name)
        }
        paymentTermsAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            paymentTermsList
        )
        paymentTermsAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorTermsSpinner.adapter = paymentTermsAdapter
        binding.newVendorTermsSpinner.onItemSelectedListener = this@NewEditVendorFragment
        // Set to "none specified" by default
        binding.newVendorTermsSpinner.setSelection(0)

        // Category
        val categoryList = mutableListOf<String>()
        GlobalVars.vendorCategories!!.forEach {
            categoryList.add(it.name)
        }
        binding.newVendorCategorySpinner.setBackgroundResource(R.drawable.text_view_layout)
        categoryAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            categoryList
        )
        categoryAdapter.setDropDownViewResource(R.layout.spinner_right_aligned)
        binding.newVendorCategorySpinner.adapter = categoryAdapter
        binding.newVendorCategorySpinner.onItemSelectedListener = this@NewEditVendorFragment
        // Set to "none specified" by default
        binding.newVendorCategorySpinner.setSelection(0)


        binding.newVendorSubmitBtn.setOnClickListener {
            if (validateFields()) {
                updateVendor()
            }
        }

        if (editMode) {
            println("edit mode")
            populateFields()
        }

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        editsMade = true


        when (parent!!.id) {

            R.id.new_vendor_name_prefix_spinner -> {
                println("${prefixArray[position]} selected")
                vendor!!.salutation = prefixArray[position]
            }

            R.id.new_vendor_doc_type_spinner -> {
                println("onItemSelected position = $position")
                vendor!!.preferredDocumentType = position.toString()
            }

            R.id.new_vendor_terms_spinner -> {
                println("onItemSelected position = $position")
                vendor!!.paymentTermsID = GlobalVars.paymentTerms?.get(position)?.ID.toString()
            }
        }






    }


    private fun populateFields() {

        println("populateFields")

        println("Vendor name: ${vendor!!.companyName}")

        if (!vendor!!.companyName.isNullOrBlank()) {
            binding.newVendorBusinessSwitch.isChecked = true
            binding.newVendorBusinessSwitch.jumpDrawablesToCurrentState()
        }


        var indexOfPrefix = prefixArray.indexOf(vendor!!.salutation)
        // Also check for older inputs with "Mr" instead of "Mr." etc
        if (indexOfPrefix == -1)  {
            indexOfPrefix = prefixArrayShort.indexOf(vendor!!.salutation)
        }

        if (indexOfPrefix != -1) {
            binding.newVendorNamePrefixSpinner.setSelection(indexOfPrefix)
        }



        binding.newVendorNameFirstEt.setText(vendor!!.fname)
        binding.newVendorNameMiddleEt.setText(vendor!!.mname)
        binding.newVendorNameLastEt.setText(vendor!!.lname)
        binding.newVendorNameBusinessEt.setText(vendor!!.companyName)
        binding.newVendorNameSystemEt.setText(vendor!!.name)
        binding.newVendorContactPhoneEt.setText(vendor!!.mainPhone)
        binding.newVendorContactEmailEt.setText(vendor!!.mainEmail)


        binding.newVendorAddressStreet1Et.setText(vendor!!.addr1)
        binding.newVendorAddressStreet2Et.setText(vendor!!.addr2)
        binding.newVendorAddressStreet3Et.setText(vendor!!.addr3)
        binding.newVendorAddressStreet4Et.setText(vendor!!.addr4)
        binding.newVendorAddressCityEt.setText(vendor!!.city)
        val addressStateIndex = GlobalVars.statesShort.indexOf(vendor!!.state)
        if (addressStateIndex != -1) {
            binding.newVendorAddressStateSpinner.setSelection(addressStateIndex)
        }
        binding.newVendorAddressZipEt.setText(vendor!!.zip)

        binding.newVendorContactPhoneEt.setText(vendor!!.mainPhone)
        binding.newVendorContactEmailEt.setText(vendor!!.mainEmail)


        binding.newVendorBillingAddressStreet1Et.setText(vendor!!.baddr1)
        binding.newVendorBillingAddressStreet2Et.setText(vendor!!.baddr2)
        binding.newVendorBillingAddressStreet3Et.setText(vendor!!.baddr3)
        binding.newVendorBillingAddressStreet4Et.setText(vendor!!.baddr4)
        binding.newVendorBillingAddressCityEt.setText(vendor!!.bcity)
        val billingAddressStateIndex = GlobalVars.statesShort.indexOf(vendor!!.bstate)
        if (billingAddressStateIndex != -1) {
            binding.newVendorBillingAddressStateSpinner.setSelection(billingAddressStateIndex)
        }
        binding.newVendorBillingAddressZipEt.setText(vendor!!.bzip)


        // In edit mode, disable the same as job site address switch
        if (editMode) {
            binding.newVendorBillingSameSwitch.isChecked = false
            binding.newVendorBillingSameSwitch.jumpDrawablesToCurrentState()
            binding.newVendorBillingSameSwitch.visibility = View.GONE
            binding.newVendorCopyBillingSwitchTv.visibility = View.GONE
        }


        if (!vendor!!.preferredDocumentType.isNullOrBlank()) {
            binding.newVendorDocTypeSpinner.setSelection(vendor!!.preferredDocumentType!!.toInt())
        }


        if (!vendor!!.paymentTermsID.isNullOrBlank()) {
            for (i in 0 until GlobalVars.paymentTerms!!.size) {
                if (vendor!!.paymentTermsID == GlobalVars.paymentTerms!![i].ID) {
                    binding.newVendorTermsSpinner.setSelection(i)
                    break
                }
            }
        }

        if (!vendor!!.category.isNullOrBlank()) {
            for (i in 0 until GlobalVars.vendorCategories!!.size) {
                if (vendor!!.category == GlobalVars.vendorCategories!![i].ID) {
                    binding.newVendorCategorySpinner.setSelection(i)
                    break
                }
            }
        }

        if (vendor!!.active == "0") {
            binding.newVendorBusinessSwitch.isChecked = false
            binding.newVendorBusinessSwitch.jumpDrawablesToCurrentState()
        }
        editsMade = false
        hideProgressView()
    }

    private fun updateVendor() {
        // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/vendor.php"

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
                params["companyName"] = binding.newVendorNameBusinessEt.text.toString()
                params["salutation"] = vendor!!.salutation.toString()
                params["firstName"] = binding.newVendorNameFirstEt.text.toString()
                params["middleName"] = binding.newVendorNameMiddleEt.text.toString()
                params["lastName"] = binding.newVendorNameLastEt.text.toString()
                params["sysName"] = binding.newVendorNameSystemEt.text.toString()
                params["nameChange"] = "0"
                if (editMode) {
                    if (binding.newVendorNameSystemEt.text.toString() != vendor!!.name) {
                        println("edit mode, name was changed, so nameChange = 1")
                        params["nameChange"] = "1"
                    }
                }

                params["mainPhone"] = binding.newVendorContactPhoneEt.text.toString()
                params["mainEmail"] = binding.newVendorContactEmailEt.text.toString()
                params["street1"] = binding.newVendorAddressStreet1Et.text.toString()
                params["street2"] = binding.newVendorAddressStreet2Et.text.toString()
                params["street3"] = binding.newVendorAddressStreet3Et.text.toString()
                params["street4"] = binding.newVendorAddressStreet4Et.text.toString()
                params["city"] = binding.newVendorAddressCityEt.text.toString()
                if (binding.newVendorAddressStateSpinner.selectedItemPosition > 0) {
                    params["state"] = GlobalVars.statesShort[binding.newVendorAddressStateSpinner.selectedItemPosition]
                }
                else {
                    params["state"] = ""
                }
                params["zip"] = binding.newVendorAddressZipEt.text.toString()
                if (editMode) {
                    val newState = GlobalVars.statesShort[binding.newVendorAddressStateSpinner.selectedItemPosition]
                    if (binding.newVendorAddressStreet1Et.text.toString() != vendor!!.addr1 ||
                        binding.newVendorAddressStreet2Et.text.toString() != vendor!!.addr2 ||
                        binding.newVendorAddressStreet3Et.text.toString() != vendor!!.addr3 ||
                        binding.newVendorAddressStreet4Et.text.toString() != vendor!!.addr4 ||
                        binding.newVendorAddressCityEt.text.toString() != vendor!!.city ||
                        newState != vendor!!.state ||
                        binding.newVendorAddressZipEt.text.toString() != vendor!!.zip) {
                        params["addressChange"] = "1"
                    }
                    else {
                        params["addressChange"] = "0"
                    }
                } else {
                    params["addressChange"] = "0"
                }

                if (binding.newVendorBillingSameSwitch.isChecked) {
                    // If "same as job site address" is checked, use same values as regular address with name added
                    var nameString = ""
                    nameString += vendor!!.salutation
                    nameString += binding.newVendorNameFirstEt.text
                    nameString += binding.newVendorNameMiddleEt.text
                    nameString += binding.newVendorNameLastEt.text
                    params["billStreet1"] = nameString
                    params["billStreet2"] = binding.newVendorAddressStreet1Et.text.toString()
                    params["billStreet3"] = binding.newVendorAddressStreet2Et.text.toString()
                    params["billStreet4"] = binding.newVendorAddressStreet3Et.text.toString()
                    params["billCity"] = binding.newVendorAddressCityEt.text.toString()
                    if (binding.newVendorAddressStateSpinner.selectedItemPosition > 0) {
                        params["billState"] = GlobalVars.statesShort[binding.newVendorAddressStateSpinner.selectedItemPosition]
                    }
                    else {
                        params["billState"] = ""
                    }
                    params["billZip"] = binding.newVendorAddressZipEt.text.toString()
                }
                else {
                    // If not, take the values put in the fields
                    params["billStreet1"] = binding.newVendorBillingAddressStreet1Et.text.toString()
                    params["billStreet2"] = binding.newVendorBillingAddressStreet2Et.text.toString()
                    params["billStreet3"] = binding.newVendorBillingAddressStreet3Et.text.toString()
                    params["billStreet4"] = binding.newVendorBillingAddressStreet4Et.text.toString()
                    params["billCity"] = binding.newVendorBillingAddressCityEt.text.toString()
                    if (binding.newVendorBillingAddressStateSpinner.selectedItemPosition > 0) {
                        params["billState"] = GlobalVars.statesShort[binding.newVendorBillingAddressStateSpinner.selectedItemPosition]
                    }
                    else {
                        params["billState"] = ""
                    }
                    params["billZip"] = binding.newVendorBillingAddressZipEt.text.toString()
                }


                params["active"] = vendor!!.active
                if (editMode) {
                    params["customerID"] = vendor!!.ID
                }
                else {
                    params["customerID"] = "0"
                }

                params["documentType"] = vendor!!.preferredDocumentType.toString()
                params["paymentTermsID"] = vendor!!.paymentTermsID.toString()
                params["category"] = vendor!!.category.toString()

                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "vendorNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun validateFields(): Boolean {
        if (binding.newVendorNameSystemEt.text.length > 41) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_too_long_title),getString(R.string.dialogue_sysname_too_long_body))
            return false
        }

        if (binding.newVendorNameSystemEt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_sysname_missing_title),getString(R.string.dialogue_sysname_missing_body))
            return false
        }

        if (binding.newVendorBillingAddressStreet1Et.text.length > 31) {
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