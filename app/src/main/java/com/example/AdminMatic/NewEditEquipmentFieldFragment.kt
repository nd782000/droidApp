package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditEquipmentFieldBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject


class NewEditEquipmentFieldFragment : Fragment() {

    private var field: EquipmentField? = null
    private var tableMode = 0

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var editMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            field = it.getParcelable("equipmentField")
            tableMode = it.getInt("tableMode")
        }
        if (field != null) {
            editMode = true
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("fieldNewEdit")
    }

    private var _binding: FragmentNewEditEquipmentFieldBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditEquipmentFieldBinding.inflate(inflater, container, false)
        myView = binding.root


        globalVars = GlobalVars()
        var titleString = ""
        when (tableMode) {
            0 -> {
                titleString = "Type"
            }
            1 -> {
                titleString = "Fuel"
            }
            2 -> {
                titleString = "Engine"
            }
            3 -> {
                titleString = "Inspection"
            }
        }
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_equipment_field_bar, titleString)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_equipment_field_bar, titleString)
        }

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!editMode) {
            field = EquipmentField("0", "0")
        }

        // Submit button
        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                updateEquipmentField()
            }
        }

        // ===== POPULATE FIELDS =====
        if (editMode) {
            binding.nameEt.setText(field!!.name)
        }

    }

    private fun validateFields(): Boolean {
        if (binding.nameEt.text.isNullOrBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_empty_equipment_field_title),getString(R.string.dialogue_empty_equipment_field_body))
            return false
        }

        return true
    }

    private fun updateEquipmentField() {
        println("updateEquipmentField")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentField.php"

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

                when (tableMode) {
                    0 -> {
                        params["field"] = "TYPE"
                    }
                    1 -> {
                        params["field"] = "FUEL"
                    }
                    2 -> {
                        params["field"] = "ENGINE"
                    }
                    3 -> {
                        params["field"] = "INSPECTION"
                    }
                }
                params["fieldID"] = field!!.ID
                params["fieldName"] = binding.nameEt.text.toString()
                params["addedBy"] = GlobalVars.loggedInEmployee!!.ID
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                println("params = $params")
                return params




            }
        }
        postRequest1.tag = "contractNewEdit"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
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