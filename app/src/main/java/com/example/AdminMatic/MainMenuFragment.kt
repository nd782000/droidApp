package com.example.AdminMatic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.example.AdminMatic.GlobalVars.Companion.thumbBase
import com.squareup.picasso.Picasso


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


lateinit var myView:View


class MainMenuFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //private var globalVars: GlobalVars? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }



    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView =  inflater.inflate(R.layout.fragment_main_menu, container, false)


       // (activity as AppCompatActivity).supportActionBar?.title = "AdminMatic Home"

        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "AdminMatic"



        var constraintLayout: ConstraintLayout = myView.findViewById<ConstraintLayout>(R.id.logged_in_employee_constraint_layout)



        var constraintSet:ConstraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)


        var loggedInEmployeeImageView:ImageView = myView.findViewById<ImageView>(R.id.logged_in_employee_image_view)


        Picasso.with(context)
            .load(thumbBase + loggedInEmployee!!.pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into( loggedInEmployeeImageView)                       //Your image view object.




        var loggedInEmployeeTextView:TextView = myView.findViewById<TextView>(R.id.logged_in_employee)
        loggedInEmployeeTextView.text = "Welcome ${loggedInEmployee!!.fName}!"




        println("loggedInEmployeeTextView.id = ${loggedInEmployeeTextView.id}")
        println("loggedInEmployeeImageView.id = ${loggedInEmployeeImageView.id}")


        myView.findViewById<ConstraintLayout>(R.id.logged_in_employee_constraint_layout).setOnClickListener(({

            println("Go To Logged in Employee")
           // Toast.makeText(activity,"Go To Logged in Employee",Toast.LENGTH_SHORT).show()


                val directions = MainMenuFragmentDirections.navigateToLoggedInEmployee(loggedInEmployee)
                myView.findNavController().navigate(directions)

            println("Cell clicked with employee: ${loggedInEmployee!!.name}")

            //val action = SpecifyAmountFragmentDirections.confirmationAction(amount)
            //v.findNavController().navigate(action)//var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            //startActivity(clickintent)
        }))


        myView.findViewById<LinearLayout>(R.id.btn_employees).setOnClickListener(({

            println("Go To Employees")
           // Toast.makeText(activity,"Go To Employees",Toast.LENGTH_SHORT).show()
            myView.findNavController().navigate(R.id.navigateToEmployeeList)//var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_customers).setOnClickListener(({
            println("Go To Customers")
            //Toast.makeText(activity,"Go To Customers",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)


            myView.findNavController().navigate(R.id.navigateToCustomerList)
            //var clickintent = Intent(this@MainMenu, EmployeesList::class.java)
           // startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_vendors).setOnClickListener(({
            println("Go To Vendors")
            //Toast.makeText(activity,"Go To Vendors",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            myView.findNavController().navigate(R.id.navigateToVendorList)

            //var clickintent = Intent(this@MainMenu, VendorList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_items).setOnClickListener(({
            println("Go To Items")
            //Toast.makeText(activity,"Go To Items",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            myView.findNavController().navigate(R.id.navigateToItemList)

            //var clickintent = Intent(this@MainMenu, ItemsList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_leads).setOnClickListener(({
            println("Go To Leads")
           // Toast.makeText(activity,"Go To Leads",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToLeadList)
            //var clickintent = Intent(this@MainMenu, ScheduleList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_contracts).setOnClickListener(({
            println("Go To Contracts")
           // Toast.makeText(activity,"Go To Contracts",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToContractList)

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_schedule).setOnClickListener(({
            println("Go To Work Orders")
            //Toast.makeText(activity,"Go To Work Orders",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToWorkOrderList)
            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_invoices).setOnClickListener(({
            println("Go To Invoices")
            //Toast.makeText(activity,"Go To Invoices",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToInvoiceList)

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_images).setOnClickListener(({
            println("Go To Images")
            //Toast.makeText(activity,"Go To Images",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToImageList)
            //var clickintent = Intent(this@MainMenu, ImagesList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_equipment).setOnClickListener(({
            println("Go To Equipment")
           // Toast.makeText(activity,"Go To Equipment",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            myView.findNavController().navigate(R.id.navigateToEquipmentList)
            // var clickintent = Intent(this@MainMenu, EquipmentList::class.java)
            //startActivity(clickintent)
        }))

        return myView
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainMenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainMenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}