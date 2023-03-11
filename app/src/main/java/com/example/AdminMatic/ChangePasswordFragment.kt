package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentChangePasswordBinding
import com.AdminMatic.databinding.FragmentEmployeeListSettingsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class ChangePasswordFragment : Fragment() {


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.change_password)

        return myView
    }

    /*
    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("documents")
    }

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.submitBtn.setOnClickListener {
            if (validateFields()) {
                submit()
            }
        }

    }

    private fun validateFields(): Boolean {
        if (binding.oldPasswordEt.text.isBlank() || binding.newPasswordEt.text.isBlank() || binding.verifyNewPasswordEt.text.isBlank()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.dialogue_fields_missing_body))
            return false
        }
        if (binding.newPasswordEt.text.toString().trim() != binding.verifyNewPasswordEt.text.toString().trim()) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.passwords_do_not_match))
            return false
        }
        return true
    }

    private fun submit() {
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/other/changePassword.php"

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

                        val builder = AlertDialog.Builder(context)
                        builder.setTitle(getString(R.string.password_successfully_changed))
                        builder.setPositiveButton(android.R.string.yes) { _, _ ->
                            myView.findNavController().navigateUp()
                        }

                        builder.show()

                        binding.oldPasswordEt.setText("")
                        binding.newPasswordEt.setText("")
                        binding.verifyNewPasswordEt.setText("")

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
                params["password"] = binding.oldPasswordEt.text.toString().trim()
                params["newPassword"] = binding.newPasswordEt.text.toString().trim()


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "customerNewEdit"
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
