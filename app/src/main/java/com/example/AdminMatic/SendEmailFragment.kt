package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentSendEmailBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

enum class SendEmailType {
    Contract,
    Invoice
}

interface EmailCheckClickListener {
    fun onEmailCheckClickListener(index: Int, checkImageView: ImageView)
    fun onAddEmailListener(email:String)
}

class SendEmailFragment : Fragment(), EmailCheckClickListener {


    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private var emailList:MutableList<String> = mutableListOf()
    private var emailNamesList:MutableList<String> = mutableListOf()
    private var emailCheckList:MutableList<Boolean> = mutableListOf()

    private var custID = ""
    private var custName = ""
    private var viewType: SendEmailType = SendEmailType.Invoice
    private var itemID = ""

    private lateinit var emailTemplate:EmailTemplate



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            custID = it.getString("customerID")!!
            custName = it.getString("customerName")!!
            viewType = SendEmailType.values()[it.getInt("type")]
            itemID = it.getString("itemID")!!
        }

        println("send email type: $viewType")
    }

    private var _binding: FragmentSendEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSendEmailBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.email_x, custName)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.with(context).load(R.drawable.ic_check_enabled).fetch()
        Picasso.with(context).load(R.drawable.ic_check_disabled).fetch()

        binding.sendBtn.setOnClickListener {
            sendEmails()
        }

        emailTemplate = when (viewType) {
            SendEmailType.Contract -> {
                GlobalVars.contractSingle!!
            }
            SendEmailType.Invoice -> {
                GlobalVars.invoiceSingle!!
            }
        }

        binding.subjectEt.setText(emailTemplate.subject)
        binding.messageEt.setText(emailTemplate.emailContent)


        getCustomerEmails()


    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("sendEmail")
    }

    private fun getCustomerEmails(){
        println("getCustomerEmails")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customerEmails.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                hideProgressView()

                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = ${parentObject.toString()}")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val contactsJSON: JSONArray = parentObject.getJSONArray("contacts")

                        val gson = GsonBuilder().create()
                        val contactsList = gson.fromJson(contactsJSON.toString(), Array<Contact>::class.java).toMutableList()

                        contactsList.forEach {
                            println("adding ${it.value}")
                            emailList.add(it.value ?: "")
                            emailNamesList.add(it.name ?: "")
                            emailCheckList.add(true)
                        }


                        binding.rv.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = EmailsSelectionAdapter(emailList, emailCheckList, this@SendEmailFragment)

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.rv.addItemDecoration(itemDecoration)
                        }

                        hideProgressView()


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
                params["custID"] = custID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "sendEmail"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun sendEmails(){
        println("sendEmails")


        val emailsToSend = mutableListOf<String>()
        val namesToSend = mutableListOf<String>()

        emailList.forEachIndexed { i, e ->
            if (emailCheckList[i]) {
                emailsToSend.add(e)
                namesToSend.add(emailNamesList[i])
            }
        }

        if (emailsToSend.size == 0) {
            globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.add_at_least_one_email_body))
            return
        }

        showProgressView()

        var urlString = when (viewType) {
            SendEmailType.Contract -> {
                "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/send/contract.php"
            }
            SendEmailType.Invoice -> {
                "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/send/invoice.php"
            }
        }


        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")


                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        globalVars.playSaveSound(myView.context)
                        setFragmentResult("sent", bundleOf("sent" to true))
                        myView.findNavController().navigateUp()

                        //hideProgressView()


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

                var jsonEmails = ""
                emailsToSend.forEachIndexed { i, e ->
                    jsonEmails += e
                    if (i < emailsToSend.count() - 1) {
                        jsonEmails += ", "
                    }
                }

                var jsonNames = ""
                namesToSend.forEachIndexed { i, e ->
                    jsonNames += e
                    if (i < namesToSend.count() - 1) {
                        jsonNames += ", "
                    }
                }

                when (viewType) {
                    SendEmailType.Contract -> {
                        params["contractID"] = itemID
                    }
                    SendEmailType.Invoice -> {
                        params["invoiceID"] = itemID
                    }
                }

                params["emails"] = jsonEmails
                params["names"] = jsonNames
                params["title"] = emailTemplate.title!!
                params["customerID"] = custID
                params["message"] = binding.messageEt.text.trim().toString()
                params["subject"] = binding.subjectEt.text.trim().toString()
                params["portal"] = emailTemplate.portal!!
                params["senderID"] = GlobalVars.loggedInEmployee!!.ID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "sendEmail"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    override fun onEmailCheckClickListener(index: Int, checkImageView: ImageView) {
        if (emailCheckList[index]) {
            emailCheckList[index] = false
            Picasso.with(context).load(R.drawable.ic_check_disabled).into(checkImageView)
        }
        else {
            emailCheckList[index] = true
            Picasso.with(context).load(R.drawable.ic_check_enabled).into(checkImageView)
        }
    }

    override fun onAddEmailListener(email: String) {
        emailList.add(email)
        emailNamesList.add("")
        emailCheckList.add(true)

        //todo: add prompt here to add the email to the customer's file

        //binding.rv.adapter!!.notifyItemInserted(emailArray.size-1)
        binding.rv.adapter!!.notifyDataSetChanged()
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
