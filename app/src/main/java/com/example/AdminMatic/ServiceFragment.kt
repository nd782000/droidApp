package com.example.AdminMatic

import android.os.Bundle
import android.text.Editable
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_equipment_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_equipment_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_equipment_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/*
interface EquipmentDetailCellClickListener {
    fun onEquipmentDetailCellClickListener(data:String)
}
*/

class ServiceFragment : Fragment(), EquipmentDetailCellClickListener {

    private  var service: EquipmentService? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar
    lateinit var nameTxt:TextView
    lateinit var typeTxt:TextView
    lateinit var dueTxt:TextView
    lateinit var frequencyTxt:TextView
    lateinit var addedByTxt:TextView
    lateinit var instructionsTxt:TextView

    lateinit var currentDateEditTxt:EditText
    lateinit var nextDateEditTxt:EditText

    // lateinit var  btn: Button

    lateinit var adapter:EquipmentDetailAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable<EquipmentService?>("service")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_service, container, false)


        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Service"



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)

        nameTxt = myView.findViewById(R.id.service_name_txt)
        typeTxt = myView.findViewById(R.id.service_type_txt)
        dueTxt = myView.findViewById(R.id.service_due_txt)
        frequencyTxt = myView.findViewById(R.id.service_frequency_txt)
        addedByTxt = myView.findViewById(R.id.service_added_by_txt)
        instructionsTxt = myView.findViewById(R.id.service_instructions_txt)

        currentDateEditTxt = myView.findViewById(R.id.current_date_editTxt)
        nextDateEditTxt = myView.findViewById(R.id.next_date_editTxt)

        if(service!!.name != null){
            nameTxt.text = service!!.name
        }
        if(service!!.type != null){
            typeTxt.text = "Type: " + service!!.typeName
        }
        if(service!!.completionDate != null){
            dueTxt.text = "Due: " + service!!.completionDate
        }
        if(service!!.frequency != null){
            frequencyTxt.text = "Frequency: " + service!!.frequency
        }
        if(service!!.addedBy != null){
            addedByTxt.text = "Added by: " + service!!.addedBy + " on " + service!!.createDate
        }
        if(service!!.instruction != null){
            addedByTxt.text = service!!.instruction
        }
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        currentDateEditTxt.setText(currentDate)
        nextDateEditTxt.setText(service!!.nextValue)

        hideProgressView()

    }


    override fun onEquipmentDetailCellClickListener(data:String) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity,"${data} Clicked",Toast.LENGTH_SHORT).show()
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
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
            EquipmentListFragment().apply {

            }
    }
}