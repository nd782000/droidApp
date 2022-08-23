package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentContactListBinding

interface ContactCellClickListener {
    fun onContactCellClickListener(data:Contact)
}



class ContactsFragment : Fragment(), ContactCellClickListener  {

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

       //recyclerView.adapter = adapter
        layoutViews()
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
                    customer!!.contacts.toMutableList(),
                    this@ContactsFragment
                )
            }

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(
                    myView.context,
                    DividerItemDecoration.VERTICAL
                )
            binding.listRecyclerView.addItemDecoration(itemDecoration)


        }

        //scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment


    }







    override fun onContactCellClickListener(data:Contact) {
        //Toast.makeText(activity, "${data.value} Clicked", Toast.LENGTH_SHORT).show()

        println("Cell clicked with contact: ${data.value}")

        data.let {

            when (it.type) {


                //Main Phone
                "1" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + it.value))
                    com.example.AdminMatic.myView.context.startActivity(intent)
                }
                //Main Email
                "2" -> {

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf(it.value.toString())
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
                    com.example.AdminMatic.myView.context.startActivity(intent)

                }
                //Alt Email
                "9" -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this
                    val emailArray = arrayOf(data.value.toString())
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
                    val emailArray = arrayOf(data.value.toString())
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