package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCustomerNotesBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class CustomerNotesFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var editsMade = false

    private var customer: Customer? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View



    private lateinit var zoneAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customer = it.getParcelable("customer")
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("customerSettings")
    }

    private var _binding: FragmentCustomerNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCustomerNotesBinding.inflate(inflater, container, false)
        myView = binding.root


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

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.customer_settings_bar)


        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val zonesList = mutableListOf<String>()
        GlobalVars.zones!!.forEach {
            zonesList.add(it.name)
        }
        zoneAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            zonesList
        )
        binding.zoneSpinner.adapter = zoneAdapter
        binding.zoneSpinner.onItemSelectedListener = this@CustomerNotesFragment

        // Allow images switch
        binding.allowImagesSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                customer!!.allowImages = "1"
                println("allow images true")
            }
            else {
                customer!!.allowImages = "0"
                println("allow images false")
            }
        }

        // Active switch
        binding.activeSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (isChecked) {
                customer!!.active = "1"
                println("active true")
            }
            else {
                customer!!.active = "0"
                println("active false")
            }
        }

        // Submit button
        binding.submitBtn.setOnClickListener {
            updateNotes()
        }




        // ===== POPULATE FIELDS =====
        binding.notesEditText.setText(customer!!.custNotes)
        binding.propertySizeEditText.setText(customer!!.propertySize)
        binding.lawnSizeEditText.setText(customer!!.lawnSize)
        binding.gardenSizeEditText.setText(customer!!.gardenSize)
        binding.drivewaySizeEditText.setText(customer!!.drivewaySize)
        binding.floorSizeEditText.setText(customer!!.floorSize)

        if (!customer!!.zone.isNullOrBlank()) {

            for (i in 0 until GlobalVars.zones!!.size) {
                if (customer!!.zone == GlobalVars.zones!![i].ID) {
                    binding.zoneSpinner.setSelection(i)
                    break
                }
            }

        }

        if (customer!!.allowImages == "1") {
            binding.allowImagesSwitch.isChecked = true
            binding.allowImagesSwitch.jumpDrawablesToCurrentState()
        }

        if (customer!!.active == "1") {
            binding.activeSwitch.isChecked = true
            binding.activeSwitch.jumpDrawablesToCurrentState()
        }


        editsMade = false

    }

    private fun updateNotes() {
        println("updateNotes")
        showProgressView()

        customer!!.custNotes = binding.notesEditText.text.toString()
        customer!!.propertySize = binding.propertySizeEditText.text.toString()
        customer!!.lawnSize = binding.lawnSizeEditText.text.toString()
        customer!!.gardenSize = binding.gardenSizeEditText.text.toString()
        customer!!.drivewaySize = binding.drivewaySizeEditText.text.toString()
        customer!!.floorSize = binding.floorSizeEditText.text.toString()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/customerSettings.php"

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


                        globalVars.playSaveSound(myView.context)
                        editsMade = false

                        myView.findNavController().navigateUp()
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
                params["customerID"] = customer!!.ID
                params["custNotes"] = customer!!.custNotes.toString()
                params["propertySize"] = customer!!.propertySize.toString()
                params["lawnSize"] = customer!!.lawnSize.toString()
                params["gardenSize"] = customer!!.gardenSize.toString()
                params["drivewaySize"] = customer!!.drivewaySize.toString()
                params["floorSize"] = customer!!.floorSize.toString()
                params["zone"] = customer!!.zone.toString()
                params["allowImages"] = customer!!.allowImages.toString()
                params["active"] = customer!!.active.toString()
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique

                println("allow images: ${customer!!.allowImages}")
                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Spinner was set")
        editsMade = true
        customer!!.zone = GlobalVars.zones!![position].ID
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