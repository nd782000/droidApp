package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import kotlinx.android.synthetic.main.fragment_work_order_list.*


//great resource fo recyclerView inf
//https://guides.codepath.com/android/using-the-recyclerview

interface ContactCellClickListener {
    fun onContactCellClickListener(data:Contact)
}



class ContactsFragment : Fragment(), ContactCellClickListener  {


    private  var customer: Customer? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView

    lateinit var  swipeRefresh:SwipeRefreshLayout
    lateinit var adapter:ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customer = it.getParcelable<Customer?>("customer")

        }
    }







    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_contact_list, container, false)


       // var list:MutableList<Contact> = mutableListOf(customer.contacts)

       // adapter = ContactsAdapter(customer!!.contacts.toMutableList(),myView.context,this)
        //(activity as AppCompatActivity).supportActionBar?.title = "Customer List"

        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Contact List"

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)

        swipeRefresh= view.findViewById(R.id.swipeContainer)

       //recyclerView.adapter = adapter
        layoutViews()


    }




    fun layoutViews(){
        println("layoutViews")

        hideProgressView()

        list_recycler_view.apply {

            layoutManager = LinearLayoutManager(activity)


            //workOrdersList.clear()
            adapter = activity?.let {

                //ContactsAdapter(customer.contacts)

                ContactsAdapter(
                    customer!!.contacts.toMutableList(),
                    it, this@ContactsFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(
                    myView.context,
                    DividerItemDecoration.VERTICAL
                )
            recyclerView.addItemDecoration(itemDecoration)


        }

        //scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment


    }







    override fun onContactCellClickListener(data:Contact) {
        //Toast.makeText(activity, "${data.value} Clicked", Toast.LENGTH_SHORT).show()

        println("Cell clicked with contact: ${data.value}")

        data?.let { data ->

            when (data.type) {


                //Main Phone
                "1" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)
                }
                //Main Email
                "2" -> {

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf<String>(data.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                   // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                   // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    com.example.AdminMatic.myView.context.startActivity(intent)


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
                    i.data = Uri.parse(data.value!!)
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
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)

                }
                //Alt Email
                "9" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf<String>(data.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                    // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    com.example.AdminMatic.myView.context.startActivity(intent)
                }
                //Mobile
                "10" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)

                }
                //Alt Mobile
                "11" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)

                }
                //Home
                "12" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)

                }
                //Alt Email
                "13" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf<String>(data.value.toString())
                    intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                    // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                    com.example.AdminMatic.myView.context.startActivity(intent)

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
        pgsBar.visibility = View.VISIBLE

        recyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE

        recyclerView.visibility = View.VISIBLE
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactsFragment().apply {

            }
    }



}