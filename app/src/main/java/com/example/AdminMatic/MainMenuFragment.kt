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

// Todo: Look into refactoring this, it warns about memory leaks but nearly every other file relies on this in the global space
lateinit var myView:View


class MainMenuFragment : Fragment() {


    lateinit  var globalVars:GlobalVars

    //private var globalVars: GlobalVars? = null

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     */



    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView =  inflater.inflate(R.layout.fragment_main_menu, container, false)


        // (activity as AppCompatActivity).supportActionBar?.title = "AdminMatic Home"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.app_name)



        val constraintLayout: ConstraintLayout = myView.findViewById(R.id.logged_in_employee_constraint_layout)



        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)


        val loggedInEmployeeImageView:ImageView = myView.findViewById(R.id.logged_in_employee_image_view)


        Picasso.with(context)
            .load(thumbBase + loggedInEmployee!!.pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into( loggedInEmployeeImageView)                       //Your image view object.




        val loggedInEmployeeTextView:TextView = myView.findViewById(R.id.logged_in_employee)
        loggedInEmployeeTextView.text = getString(R.string.welcome_name, loggedInEmployee!!.fName)




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
            if (GlobalVars.permissions!!.employees == "1") {
                myView.findNavController().navigate(R.id.navigateToEmployeeList)//var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_employees))
            }

            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_customers).setOnClickListener(({
            println("Go To Customers")
            //Toast.makeText(activity,"Go To Customers",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            if (GlobalVars.permissions!!.customers == "1") {
                myView.findNavController().navigate(R.id.navigateToCustomerList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers))
            }
            //var clickintent = Intent(this@MainMenu, EmployeesList::class.java)
            // startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_vendors).setOnClickListener(({
            println("Go To Vendors")
            //Toast.makeText(activity,"Go To Vendors",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.vendors == "1") {
                myView.findNavController().navigate(R.id.navigateToVendorList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_vendors))
            }

            //var clickintent = Intent(this@MainMenu, VendorList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_items).setOnClickListener(({
            println("Go To Items")
            //Toast.makeText(activity,"Go To Items",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.items == "1") {
                myView.findNavController().navigate(R.id.navigateToItemList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_items))
            }

            //var clickintent = Intent(this@MainMenu, ItemsList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_leads).setOnClickListener(({
            println("Go To Leads")
            // Toast.makeText(activity,"Go To Leads",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            if (GlobalVars.permissions!!.leads == "1") {
                myView.findNavController().navigate(R.id.navigateToLeadList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_leads))
            }
            //var clickintent = Intent(this@MainMenu, ScheduleList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_contracts).setOnClickListener(({
            println("Go To Contracts")
            // Toast.makeText(activity,"Go To Contracts",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.contracts == "1") {
                myView.findNavController().navigate(R.id.navigateToContractList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_contracts))
            }

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_schedule).setOnClickListener(({
            println("Go To Work Orders")
            //Toast.makeText(activity,"Go To Work Orders",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.schedule == "1") {
                myView.findNavController().navigate(R.id.navigateToWorkOrderList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule))
            }
            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_invoices).setOnClickListener(({
            println("Go To Invoices")
            //Toast.makeText(activity,"Go To Invoices",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.invoices == "1") {
                myView.findNavController().navigate(R.id.navigateToInvoiceList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices))
            }

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        myView.findViewById<LinearLayout>(R.id.btn_images).setOnClickListener(({
            println("Go To Images")
            //Toast.makeText(activity,"Go To Images",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.files == "1") {
                myView.findNavController().navigate(R.id.navigateToImageList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_images))
            }

            //var clickintent = Intent(this@MainMenu, ImagesList::class.java)
            //startActivity(clickintent)
        }))
        myView.findViewById<LinearLayout>(R.id.btn_equipment).setOnClickListener(({
            println("Go To Equipment")
            // Toast.makeText(activity,"Go To Equipment",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.equipment == "1") {
                myView.findNavController().navigate(R.id.navigateToEquipmentList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment))
            }
            // var clickintent = Intent(this@MainMenu, EquipmentList::class.java)
            //startActivity(clickintent)
        }))

        return myView
    }

}