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
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEquipmentDetailsBinding


interface EquipmentDetailCellClickListener {
    fun onEquipmentDetailCellClickListener(data:Int)
}


class EquipmentDetailsFragment : Fragment(), EquipmentDetailCellClickListener {

    private  var equipment: Equipment? = null
    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var adapter:EquipmentDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
        }
    }

    private var _binding: FragmentEquipmentDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentEquipmentDetailsBinding.inflate(inflater, container, false)
        myView = binding.root

        val emptyList:MutableList<String> = mutableListOf()

        adapter = EquipmentDetailAdapter(emptyList, this)


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment_details)


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

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

        binding.listRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = activity?.let {
                EquipmentDetailAdapter(
                    detailList,
                    this@EquipmentDetailsFragment
                )
            }

            val itemDecoration: ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.listRecyclerView.addItemDecoration(itemDecoration)

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
        binding.progressBar.visibility = View.VISIBLE
        binding.listRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.listRecyclerView.visibility = View.VISIBLE
    }

}