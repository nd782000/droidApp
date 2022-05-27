package com.example.AdminMatic

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.renderscript.ScriptGroup
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import java.util.Collections.copy
import kotlin.math.roundToInt


class UsageAdapter(private val list: MutableList<Usage>, private val context: Context,private val usageEditListener: UsageEditListener, private val woItem:WoItem, private val workOrder:WorkOrder)

    : RecyclerView.Adapter<UsageViewHolder>() {


    lateinit  var globalVars:GlobalVars



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        globalVars = GlobalVars()

        return UsageViewHolder(inflater, parent)
    }






    override fun onBindViewHolder(holder: UsageViewHolder, position: Int) {


        val usage: Usage = list[position]
        holder.bind(usage)



        val laborCl = holder.itemView.findViewById<ConstraintLayout>(R.id.usage_labor_cl)
        val materialCl = holder.itemView.findViewById<ConstraintLayout>(R.id.usage_material_cl)

        if(usage.type == "1") {
            //labor type

            // Lock this cell if the values are already filled and it's a different person than the logged in employee
            usage.locked = false;
            if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID
                && usage.start != null
                && usage.start != "0000-00-00 00:00:00"
                && usage.stop != null
                && usage.stop!= "0000-00-00 00:00:00") {
                usage.locked = true
                val lock:ImageView = holder.itemView.findViewById(R.id.imageViewLockedLabor)
                lock.visibility = View.VISIBLE
                val optionsButton:TextView = holder.itemView.findViewById(R.id.textViewOptions)
                optionsButton.visibility = View.INVISIBLE
            }

            materialCl.isVisible = false


            val employeeImageView:ImageView = holder.itemView.findViewById(R.id.usage_emp_iv)


            Picasso.with(context)
                .load(GlobalVars.thumbBase + list[position].pic)
                .placeholder(R.drawable.user_placeholder) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(employeeImageView)                       //Your image view object.





            val startTxt:TextView = holder.itemView.findViewById(R.id.usage_start_edit_txt)
            startTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.start != null && usage.start != "0000-00-00 00:00:00"){
                startTxt.text = usage.getTime(usage.start!!)
            }
            if (!usage.locked!!) {
                startTxt.setOnClickListener {
                    // editStart()
                    usageEditListener.editStart(position)
                }
            }


            val stopTxt:TextView = holder.itemView.findViewById(R.id.usage_stop_edit_txt)
            stopTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.stop != null && usage.stop != "0000-00-00 00:00:00"){
                stopTxt.text = usage.getTime(usage.stop!!)
            }
            if (!usage.locked!!) {
                stopTxt.setOnClickListener {
                    //editStart()
                    usageEditListener.editStop(position)
                }
            }


            val breakTxt:TextView = holder.itemView.findViewById(R.id.usage_break_edit_txt)

            breakTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            breakTxt.setSelectAllOnFocus(true)

            breakTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.lunch != null){
                breakTxt.text = usage.lunch!!
            }
            if (!usage.locked!!) {
                breakTxt.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        breakTxt.clearFocus()
                        usageEditListener.editBreak(position, breakTxt.text.toString(), actionId)
                        true
                    } else {
                        false
                    }
                }
            }
            else {
                breakTxt.inputType = InputType.TYPE_NULL
            }

            val totalTxt:TextView = holder.itemView.findViewById(R.id.usage_total_tv)


            totalTxt.text = usage.qty!! + " Hours"



            //options btn click
            holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
                println("status click")

                val popUp = PopupMenu(myView.context,holder.itemView)


                if (!usage.locked!!) {
                    popUp.inflate(R.menu.task_status_menu)

                    popUp.menu.add(0, usage.ID.toInt(), 1, globalVars.menuIconWithText(globalVars.resize(context.getDrawable(R.drawable.ic_canceled)!!,context), context.getString(R.string.delete)))

                    popUp.setOnMenuItemClickListener {
                        usageEditListener.deleteUsage(position)

                        true
                    }


                    popUp.gravity = Gravity.END
                    popUp.show()
                }

            }

        }else{
            //material type
            laborCl.isVisible = false

            usage.locked = false;
            if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID
                && usage.vendor != null
                && usage.qty!= "0.00"
                && usage.unitCost != "0.00"
                && usage.unitCost != null) {
                usage.locked = true
                val lock:ImageView = holder.itemView.findViewById(R.id.imageViewLockedMaterial)
                lock.visibility = View.VISIBLE
            }

            val vendorSelectText = holder.itemView.findViewById<TextView>(R.id.usage_vendor_select_tv)
            vendorSelectText.setBackgroundResource(R.drawable.text_view_layout)

            if (usage.vendor == "0") {
                vendorSelectText.text = R.string.other.toString()
            }

            when (usage.vendor) {
                "0" -> {
                    vendorSelectText.text = "Other"
                }
                "", null -> {
                    vendorSelectText.text = "Select Vendor"

                    woItem.vendors.forEach {
                        if (it.prefered == "1") {
                            vendorSelectText.text = it.name
                            usageEditListener.editCost(position,it.cost!!,EditorInfo.IME_ACTION_DONE, false)
                            usageEditListener.editVendor(position, it.ID)
                        }
                    }

                }
                else -> {
                    vendorSelectText.text = "Existing Vendor!"

                    woItem.vendors.forEach {
                        if (it.ID == usage.vendor) {
                            vendorSelectText.text = it.name
                        }
                    }

                }

            }
            if (!usage.locked!!) {
                vendorSelectText.setOnClickListener {
                    println("status click")
                    val popUp = PopupMenu(myView.context, holder.itemView)
                    popUp.inflate(R.menu.task_status_menu)

                    woItem.vendors.forEach { v->
                        popUp.menu.add(0, v.ID.toInt(), 1, v.name)
                    }
                    popUp.menu.add(0, 0, 1, R.string.other)

                    popUp.setOnMenuItemClickListener {

                        // Update cost from the selected vendor
                        woItem.vendors.forEach { v->
                            if (v.ID.toInt() == it.itemId) {
                                usageEditListener.editCost(position,v.cost!!,EditorInfo.IME_ACTION_DONE, true)
                            }
                        }

                        usageEditListener.editVendor(position, it.itemId.toString())


                        vendorSelectText.text = it.title
                        true
                    }
                    popUp.gravity = Gravity.END
                    popUp.show()
                }
            }

            /*
            val vendorSearch:Spinner = holder.itemView.findViewById(R.id.usage_vendor_spinner)
            vendorSearch.setBackgroundResource(R.drawable.text_view_layout)
            /*
            if (usage.vendor != null && usage.vendor != "0" && usage.vendor != ""){
                //vendorSearch.setQuery(usage.vendor!!)
            }

             */

            val vendorListCopy = mutableListOf<Vendor>()
            vendorListCopy.addAll(woItem.vendors)
            vendorListCopy.add(Vendor("", "Other", "","","","","","","", "", "",""))

            val adapter: ArrayAdapter<Vendor> = ArrayAdapter<Vendor>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, vendorListCopy
            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

            vendorSearch.adapter = adapter

            if (!usage.locked!!) {
                vendorSearch.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View?,
                        spinnerPosition: Int,
                        id: Long
                    ) {
                        if (!holder.mInitialized) {
                            holder.mInitialized = true
                        }
                        else {
                            usageEditListener.editVendor(position, vendorListCopy[spinnerPosition].ID)
                            if (vendorListCopy[spinnerPosition].name != "Other") {
                                if (vendorListCopy[spinnerPosition].cost != null && vendorListCopy[spinnerPosition].cost != "") {
                                    usageEditListener.editCost(position,vendorListCopy[spinnerPosition].cost!!.toDouble(),EditorInfo.IME_ACTION_DONE)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        // your code here
                    }
                }
            }
            else {
                vendorSearch.isEnabled = false
            }

             */

            val quantityTxt:TextView = holder.itemView.findViewById(R.id.usage_quantity_et)
            quantityTxt.text = usage.qty

            quantityTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            quantityTxt.setSelectAllOnFocus(true)

            quantityTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (!usage.locked!!) {
                quantityTxt.setOnEditorActionListener { _, actionId, _ ->
                    //var numeric = true

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Call your code here

                        quantityTxt.clearFocus()


                        //val qtyInput = quantityTxt.text.toString().toDouble()
                        //val qtyInputTrimmed = (qtyInput * 100.0).roundToInt() / 100.0
                        usageEditListener.editQty(position, quantityTxt.text.toString(), actionId)


                        true
                    } else {
                        false
                    }
                }
            }
            else {
                quantityTxt.inputType = InputType.TYPE_NULL
            }




            val unitCostTxt:EditText = holder.itemView.findViewById(R.id.usage_unit_cost_et)
            if(usage.unitCost != null){
                unitCostTxt.setText(usage.unitCost!!)
            }


            unitCostTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            unitCostTxt.setSelectAllOnFocus(true)

            unitCostTxt.setBackgroundResource(R.drawable.text_view_layout)

            if (!usage.locked!!) {
                unitCostTxt.setOnEditorActionListener { _, actionId, _ ->
                    //var numeric = true

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Call your code here

                        unitCostTxt.clearFocus()

                        if (unitCostTxt.text.toString() != "") {
                            //val costInput = unitCostTxt.text.toString().toDouble()
                            //val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                            usageEditListener.editCost(position, unitCostTxt.text.toString(), actionId, true)
                            //usageEditListener.editCost(position, "7,9", actionId, true)
                        }
                        else {
                            usageEditListener.editCost(position, "0.0", actionId, true)
                        }




                        true
                    } else {
                        false
                    }
                }
            }
            else {
                unitCostTxt.inputType = InputType.TYPE_NULL
            }


            val totalCostTxt:TextView = holder.itemView.findViewById(R.id.usage_total_cost_tv)
            if(usage.totalCost != null){
                totalCostTxt.text = usage.totalCost!!
            }
            if(usage.totalCost == ""){
                totalCostTxt.text = "0.00"
            }

            val receiptImageView:ImageView = holder.itemView.findViewById(R.id.usage_receipt_iv)
            if(list[position].receipt != null){
                Picasso.with(context)
                    .load(GlobalVars.thumbBase + list[position].receipt!!)
                    .placeholder(R.drawable.ic_images) //optional
                    //.resize(imgWidth, imgHeight)         //optional
                    //.centerCrop()                        //optional
                    .into(receiptImageView)                       //Your image view object.
            }
            if (!usage.locked!!) {
                receiptImageView.setOnClickListener{
                    if (usage.ID == "0") {
                        globalVars.simpleAlert(myView.context, "Submit Usage","Please submit usage before attempting to add a receipt.")
                    }
                    else if (usage.hasReceipt == "1") {
                        globalVars.simpleAlert(myView.context, "Receipt already uploaded","A receipt image has already been uploaded for this usage.")
                    }
                    else {
                        val directions = UsageEntryFragmentDirections.navigateUsageEntryToImageUpload("WOITEM",
                            arrayOf(),workOrder.customer, workOrder.custName, woItem.woID, woItem.itemID,"","","","","", usage.ID)
                        myView.findNavController().navigate(directions)
                    }
                }
            }
        }
    }





    override fun getItemCount(): Int{

        print("getItemCount = ${list.size}")
        return list.size

    }






}






class UsageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.usage_list_item, parent, false)) {
    private var mNameView: TextView? = null
    // This is used to tell the recycler to skip its first "onItemSelected" call so it doesn't go off automatically on load
    var mInitialized = false

    //private var thumbView:ImageView? = null
    // private var empPic:ImageView? = null



    init {
        mNameView = itemView.findViewById(R.id.usage_emp_name_tv)
        // empPic = itemView.findViewById(R.id.usage_emp_iv)

    }

    fun bind(usage: Usage) {
        mNameView?.text = usage.empName




    }





}