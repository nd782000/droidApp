package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContactListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

interface ContactCellClickListener {
    fun onContactCellClickListener(data:Contact)
}

interface ContactEditListener {
    fun onContactEditListener(contact: Contact)
}

interface ContactDeleteListener {
    fun onContactDeleteListener(contact: Contact, position:Int)
}

class ContactsFragment : Fragment(), ContactCellClickListener, ContactEditListener, ContactDeleteListener  {

    private  var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customer = it.getParcelable("customer")

        }
    }


    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.contacts_list)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")

        binding.addContactBtn.setOnClickListener {
            if (GlobalVars.permissions!!.customersEdit == "1") {
                val directions = ContactsFragmentDirections.navigateToNewEditContact(null, customer!!)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers_edit))
            }
        }

       //recyclerView.adapter = adapter
        layoutViews()
    }


    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("contacts")
    }



    private fun layoutViews(){
        println("layoutViews")

        hideProgressView()

        binding.listRecyclerView.apply {

            layoutManager = LinearLayoutManager(activity)


            //workOrdersList.clear()
            adapter = activity?.let {

                //ContactsAdapter(customer.contacts)

                ContactsAdapter(
                    customer!!.contacts.toMutableList(), myView.context,
                    this@ContactsFragment, this@ContactsFragment, this@ContactsFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(
                    myView.context,
                    DividerItemDecoration.VERTICAL
                )
            binding.listRecyclerView.addItemDecoration(itemDecoration)

            (adapter as ContactsAdapter).notifyDataSetChanged()
        }

        //scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment


    }

    override fun onContactEditListener(contact: Contact) {
        if (GlobalVars.permissions!!.customersEdit == "0") {
            globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_schedule_edit))
        }
        else {
            val directions = ContactsFragmentDirections.navigateToNewEditContact(contact, customer!!)
            myView.findNavController().navigate(directions)
        }
    }

    override fun onContactDeleteListener(contact: Contact, position: Int) {
        if (GlobalVars.permissions!!.customersEdit == "0") {
            globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_schedule_edit))
        }
        else {
            val builder = AlertDialog.Builder(myView.context)
            builder.setTitle(getString(R.string.dialogue_delete_contact_title))
            builder.setMessage(getString(R.string.dialogue_delete_contact_body))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->

                var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/customerEmail.php"

                val currentTimestamp = System.currentTimeMillis()
                println("urlString = ${"$urlString?cb=$currentTimestamp"}")
                urlString = "$urlString?cb=$currentTimestamp"

                val postRequest1: StringRequest = object : StringRequest(
                    Method.POST, urlString,
                    Response.Listener { response -> // response
                        //Log.d("Response", response)

                        println("Response $response")

                        val parentObject = JSONObject(response)
                        if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                            (binding.listRecyclerView.adapter as ContactsAdapter).filterList.removeAt(position)
                            globalVars.playSaveSound(myView.context)

                            binding.listRecyclerView.adapter!!.notifyDataSetChanged()
                        }

                    },
                    Response.ErrorListener { // error


                        // Log.e("VOLLEY", error.toString())
                        // Log.d("Error.Response", error())
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                        params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                        params["custID"] = customer!!.ID
                        params["contactID"] = contact.ID
                        println("params = $params")
                        return params
                    }
                }
                postRequest1.tag = "contacts"
                VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
            }
            builder.setNegativeButton(getString(R.string.no)) { _, _ ->

            }
            builder.show()


        }
    }




    override fun onContactCellClickListener(data:Contact) {


        println("Cell clicked with contact: ${data.value}")

        data.let {

            when (it.type) {


                //Main Phone
                "1" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + it.value))
                    myView.context.startActivity(intent)
                }
                //Main Email
                "2" -> {

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf(it.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                    // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    myView.context.startActivity(intent)


                }
                //Billing Address
                "3" -> {

                }
                //Jobsite Address
                "4" -> {

                }
                //Website
                "5" -> {


                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(it.value!!)
                    startActivity(i)

                }
                //Alt Contact
                "6" -> {

                }
                //Fax
                "7" -> {

                }
                //Alt Phone
                "8" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + it.value))
                    myView.context.startActivity(intent)

                }
                //Alt Email
                "9" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf(data.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                    // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    myView.context.startActivity(intent)
                }
                //Mobile
                "10" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    myView.context.startActivity(intent)

                }
                //Alt Mobile
                "11" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    myView.context.startActivity(intent)

                }
                //Home
                "12" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    myView.context.startActivity(intent)

                }
                //Alt Email
                "13" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf(data.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                    // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    myView.context.startActivity(intent)

                }
                //Invoice Address
                "14" -> {

                }

            }


            //val directions = ContactsFragmentDirections
            //val directions = ContactsFra
            //val directions = ContactsFragmentDirections.nav
            //val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            //myView.findNavController().navigate(directions)

        }
    }


    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE

        binding.listRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE

        binding.listRecyclerView.visibility = View.VISIBLE
    }

    /*
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactsFragment().apply {

            }
    }
     */



}