package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R

import kotlinx.android.synthetic.main.fragment_equipment_list.list_recycler_view


interface EquipmentDetailCellClickListener {
    fun onEquipmentDetailCellClickListener(data:Int)
}


class EquipmentDetailsFragment : Fragment(), EquipmentDetailCellClickListener {

    private  var equipment: Equipment? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView

    lateinit var adapter:EquipmentDetailAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_equipment_details, container, false)


        //var progBar: ProgressBar = myView.findViewById(R.id.progressBar)
        // progBar.alpha = 0.2f

        val emptyList:MutableList<String> = mutableListOf()

        adapter = EquipmentDetailAdapter(emptyList, this)





        //(activity as AppCompatActivity).supportActionBar?.title = "Equipment List"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment_details)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)


        val detailList = mutableListOf<String>()

        detailList.add(0, "Description: ${equipment!!.description}")
        if (equipment!!.plannerShow == "0") {
            detailList.add(0, "Show in Planners: NO")
        }
        else {
            detailList.add(0, "Show in Planners: YES")
        }
        detailList.add(0, "Vendor: ${equipment!!.dealerName}")
        detailList.add(0, "Purchased: ${equipment!!.purchaseDate}")
        detailList.add(0, "Engine: ${equipment!!.engineTypeName}")
        detailList.add(0, "Fuel: ${equipment!!.fuelTypeName}")
        detailList.add(0, "Serial/VIN: ${equipment!!.serial}")
        detailList.add(0, "Model: ${equipment!!.model}")
        detailList.add(0, "Make: ${equipment!!.make}")
        detailList.add(0, "Crew: ${equipment!!.crewName}")
        detailList.add(0, "Type: ${equipment!!.typeName}")
        detailList.add(0, "Name: ${equipment!!.name}")

        list_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = activity?.let {
                EquipmentDetailAdapter(
                    detailList,
                    this@EquipmentDetailsFragment
                )
            }

            val itemDecoration: ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)

            //for item animations
            // recyclerView.itemAnimator = SlideInUpAnimator()

            //(adapter as EquipmentDetailAdapter).notifyDataSetChanged()

            // Remember to CLEAR OUT old items before appending in the new ones

            // ...the data has come back, add new items to your adapter...

            // Now we call setRefreshing(false) to signal refresh has finished
            //customerSwipeContainer.isRefreshing = false;

            //  Toast.makeText(activity,"${equipmentList.count()} Equipment Loaded",Toast.LENGTH_SHORT).show(
        }

        hideProgressView()

    }

    override fun onEquipmentDetailCellClickListener(data:Int) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        if (data == 9) { //vendor cell
            val directions = VendorListFragmentDirections.navigateToVendor(null, equipment!!.dealer)
            myView.findNavController().navigate(directions)
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

}