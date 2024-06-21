package com.example.AdminMatic

//import android.R
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso


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

        if (usage.type == "1") {
            //labor type

            laborCl.visibility = View.VISIBLE
            materialCl.visibility = View.GONE

            if (usage.total_only == "1") {
                holder.itemView.findViewById<ConstraintLayout>(R.id.usage_times_cl).visibility = View.INVISIBLE
                holder.itemView.findViewById<ConstraintLayout>(R.id.usage_total_cl).visibility = View.VISIBLE
            }
            else {
                holder.itemView.findViewById<ConstraintLayout>(R.id.usage_times_cl).visibility = View.VISIBLE
                holder.itemView.findViewById<ConstraintLayout>(R.id.usage_total_cl).visibility = View.INVISIBLE
            }


            // Lock this cell if the values are already filled and it's a different person than the logged in employee
            usage.locked = false

            if (usage.total_only == "0") {
                if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID
                    && usage.start != null
                    && usage.start != "0000-00-00 00:00:00"
                    && usage.stop != null
                    && usage.stop != "0000-00-00 00:00:00"
                ) {
                    usage.locked = true
                }
            }
            else {
                if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID && usage.qty != "" && usage.qty != "0") {
                    usage.locked = true
                }
            }

            val lock:ImageView = holder.itemView.findViewById(R.id.imageViewLockedLabor)
            val menuBtn:TextView = holder.itemView.findViewById(R.id.textViewOptions)

            if (usage.locked) {
                lock.visibility = View.VISIBLE
                menuBtn.visibility = View.GONE
            }
            else {
                lock.visibility = View.INVISIBLE
                menuBtn.visibility = View.VISIBLE
            }



            val employeeImageView:ImageView = holder.itemView.findViewById(R.id.usage_emp_iv)

            Picasso.with(context)
                .load(GlobalVars.thumbBase + list[position].pic)
                .placeholder(R.drawable.user_placeholder) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(employeeImageView)                       //Your image view object.





            val startTxt:TextView = holder.itemView.findViewById(R.id.usage_start_edit_txt)
            startTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.startDateTime != null) {
                startTxt.text = usage.startDateTime!!.format(GlobalVars.dateFormatterHMMA)
            }
            else {
                startTxt.text = ""
            }
            if (!usage.locked) {
                startTxt.setOnClickListener {
                    // editStart()
                    usageEditListener.editStart(position)
                }
                startTxt.isEnabled = true
            }
            else {
                startTxt.isEnabled = false
            }

            val stopTxt:TextView = holder.itemView.findViewById(R.id.usage_stop_edit_txt)
            stopTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.stopDateTime != null) {
                stopTxt.text = usage.stopDateTime!!.format(GlobalVars.dateFormatterHMMA)
            }
            else {
                stopTxt.text = ""
            }
            if (!usage.locked) {
                stopTxt.setOnClickListener {
                    //editStart()
                    usageEditListener.editStop(position)
                }
                stopTxt.isEnabled = true
            }
            else {
                stopTxt.isEnabled = false
            }

            val breakTxt:TextView = holder.itemView.findViewById(R.id.usage_break_edit_txt)

            //breakTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            //breakTxt.setSelectAllOnFocus(true)

            breakTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.lunch != null){
                breakTxt.text = usage.lunch!!
            }
            else {
                breakTxt.text = ""
            }

            if (!usage.locked) {
                breakTxt.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        breakTxt.clearFocus()
                        usageEditListener.editBreak(position, breakTxt.text.toString(), actionId)
                        true
                    }
                    else {
                        false
                    }
                }
                breakTxt.isEnabled = true
            }
            else {
                breakTxt.inputType = InputType.TYPE_NULL
                breakTxt.isEnabled = false
            }

            val totalEditTxt:TextView = holder.itemView.findViewById(R.id.usage_total_edit_txt)
            if (usage.total_only == "1") {
                totalEditTxt.text = ((usage.qty.toDouble() * 60)+0.5).toInt().toString()
            }

            if (!usage.locked) {
                totalEditTxt.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (totalEditTxt.text.isBlank()) {
                            totalEditTxt.text = "0"
                        }
                        totalEditTxt.clearFocus()
                        usageEditListener.editTotal(position, totalEditTxt.text.toString(), actionId)
                        true
                    }
                    else {
                        false
                    }
                }
                totalEditTxt.isEnabled = true
            }
            else {
                totalEditTxt.inputType = InputType.TYPE_NULL
                totalEditTxt.isEnabled = false
            }

            val totalTxt:TextView = holder.itemView.findViewById(R.id.usage_total_tv)
            totalTxt.text = context.getString(R.string.usage_hours, usage.qty)

            val byTxt:TextView = holder.itemView.findViewById(R.id.usage_by_tv)
            if (usage.addedByName == null || usage.addedByName == "") {
                byTxt.text = context.getString(R.string.usage_added_by_x, "---")
            }
            else {
                byTxt.text = context.getString(R.string.usage_added_by_x, usage.addedByName + " " + usage.addedNice)
            }

            menuBtn.setOnClickListener {
                println("menu click")

                val popUp = PopupMenu(myView.context, menuBtn)

                popUp.inflate(R.menu.task_status_menu)

                popUp.menu.add(0, 0, 1, context.getString(R.string.delete))
                if (usage.total_only == "1") {
                    popUp.menu.add(0, 1, 1, context.getString(R.string.usage_edit_times))
                }
                else {
                    popUp.menu.add(0, 1, 1, context.getString(R.string.usage_edit_total))
                }
                popUp.gravity = Gravity.CENTER
                popUp.setOnMenuItemClickListener { item: MenuItem? ->
                    println("test")
                    when (item?.itemId) {
                        0 -> {
                            println("delete click")
                            usageEditListener.deleteUsage(position)
                        }
                        1 -> {
                            println("toggle total only")
                            usageEditListener.toggleTotalOnly(position)
                        }
                    }

                    true

                }
                popUp.show()
            }




        }
        else {
            //material type
            laborCl.visibility = View.GONE
            materialCl.visibility = View.VISIBLE

            usage.locked = false
            if (usage.total_only == "1") {
                usage.locked = true
            }
            if (usage.addedBy != GlobalVars.loggedInEmployee!!.ID
                && usage.vendor != null
                && usage.qty!= "0.00"
                && usage.unitCost != "0.00"
                && usage.unitCost != null) {
                usage.locked = true
            }

            val lock:ImageView = holder.itemView.findViewById(R.id.imageViewLockedMaterial)
            if (usage.locked) {
                lock.visibility = View.VISIBLE
            }
            else {
                lock.visibility = View.INVISIBLE
            }



            val vendorSelectText = holder.itemView.findViewById<TextView>(R.id.usage_vendor_select_tv)
            vendorSelectText.setBackgroundResource(R.drawable.text_view_layout)

            if (usage.vendor == "0") {
                vendorSelectText.text = R.string.other.toString()
            }

            when (usage.vendor) {
                "0" -> {
                    vendorSelectText.text = context.getString(R.string.other)
                }
                "", null -> {
                    vendorSelectText.text = context.getString(R.string.select_vendor)

                    woItem.vendors!!.forEach {
                        if (it.preferred == "1") {
                            vendorSelectText.text = it.name
                            usageEditListener.editCost(position,it.cost!!,EditorInfo.IME_ACTION_DONE, false)
                            usageEditListener.editVendor(position, it.ID)
                        }
                    }

                }
                else -> {
                    vendorSelectText.text = context.getString(R.string.existing_vendor)

                    woItem.vendors!!.forEach {
                        if (it.ID == usage.vendor) {
                            vendorSelectText.text = it.name
                        }
                    }

                }

            }
            if (!usage.locked) {
                vendorSelectText.setOnClickListener {
                    println("status click")
                    println("woItem.vendors!!.size: ${woItem.vendors!!.size}")
                    val popUp = PopupMenu(myView.context, holder.itemView.findViewById<TextView>(R.id.usage_vendor_select_tv))
                    popUp.inflate(R.menu.task_status_menu)

                    woItem.vendors!!.forEach { v->
                        popUp.menu.add(0, v.ID.toInt(), 1, v.name)
                    }
                    popUp.menu.add(0, 0, 1, R.string.other)

                    popUp.setOnMenuItemClickListener {

                        // Update cost from the selected vendor
                        woItem.vendors!!.forEach { v->
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
                vendorSelectText.isEnabled = true
            }
            else {
                vendorSelectText.isEnabled = false
            }


            val quantityTxt:TextView = holder.itemView.findViewById(R.id.usage_quantity_et)
            val unitCostTxt:EditText = holder.itemView.findViewById(R.id.usage_unit_cost_et)
            quantityTxt.text = usage.qty

            quantityTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            quantityTxt.setSelectAllOnFocus(true)

            val unitsTxt:TextView = holder.itemView.findViewById(R.id.usage_units_tv)
            if (woItem.unitName.isNotBlank()) {
                unitsTxt.text = context.getString(R.string.units_x, woItem.unitName)
            }
            else {
                unitsTxt.text = context.getString(R.string.units)

            }

            quantityTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (!usage.locked) {
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
                quantityTxt.isEnabled = true
            }
            else {
                quantityTxt.inputType = InputType.TYPE_NULL
                quantityTxt.isEnabled = false
            }

            /*
            quantityTxt.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    quantityTxt.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    //usageEditListener.editQty(position, quantityTxt.text.toString(), EditorInfo.IME_ACTION_DONE)
                }
            }

             */




            if (usage.unitCost != null) {
                unitCostTxt.setText(usage.unitCost!!)
            }


            unitCostTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            unitCostTxt.setSelectAllOnFocus(true)

            if (!usage.locked) {
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
                unitCostTxt.isEnabled = true
            }
            else {
                unitCostTxt.inputType = InputType.TYPE_NULL
                unitCostTxt.isEnabled = false
            }


            val totalCostTxt:TextView = holder.itemView.findViewById(R.id.usage_total_cost_tv)
            if (usage.totalCost != null) {
                totalCostTxt.text = usage.totalCost!!
            }
            if (usage.totalCost == "") {
                usage.totalCost = "0.00"
                totalCostTxt.text = usage.totalCost
            }

            val receiptImageView:ImageView = holder.itemView.findViewById(R.id.usage_receipt_iv)
            if (list[position].receipt != null) {
                Picasso.with(context)
                    .load(GlobalVars.thumbBase + list[position].receipt!!.fileName)
                    .placeholder(R.drawable.ic_images) //optional
                    //.resize(imgWidth, imgHeight)         //optional
                    //.centerCrop()                        //optional
                    .into(receiptImageView)                       //Your image view object.
            }
            if (!usage.locked) {
                receiptImageView.setOnClickListener{
                    if (usage.ID == "0") {
                        globalVars.simpleAlert(myView.context, "Submit Usage","Please submit usage before attempting to add a receipt.")
                    }
                    else if (usage.hasReceipt == "1") {
                        globalVars.simpleAlert(myView.context, "Receipt already uploaded","A receipt image has already been uploaded for this usage.")
                    }
                    else {
                        val directions = UsageEntryFragmentDirections.navigateUsageEntryToImageUpload("WOITEM",
                            arrayOf(),workOrder.customer, workOrder.custName, woItem.woID, woItem.itemID, "","","","","", "","", usage.ID)
                        myView.findNavController().navigate(directions)
                    }
                }
            }

            val byTxt:TextView = holder.itemView.findViewById(R.id.usage_material_by_tv)
            if (usage.addedByName == null || usage.addedByName == "") {
                byTxt.text = context.getString(R.string.usage_added_by_x, "---")
            }
            else {
                byTxt.text = context.getString(R.string.usage_added_by_x, usage.addedByName + " " + usage.addedNice)
            }

        }


        val loadingCl:ConstraintLayout = holder.itemView.findViewById(R.id.loading_overlay_cl)
        if (usage.progressViewVisible) {
            loadingCl.visibility = View.VISIBLE
        }
        else {
            loadingCl.visibility = View.INVISIBLE
        }

    }





    override fun getItemCount(): Int{

        //print("getItemCount = ${list.size}")
        return list.size

    }






}






class UsageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.usage_list_item, parent, false)) {
    private var mNameView: TextView? = null
    // This is used to tell the recycler to skip its first "onItemSelected" call so it doesn't go off automatically on load
    //var mInitialized = false

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