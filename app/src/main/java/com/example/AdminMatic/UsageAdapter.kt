package com.example.AdminMatic

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import java.lang.Double
import kotlin.math.roundToInt


class UsageAdapter(private val list: MutableList<Usage>, private val context: Context,private val usageEditListener: UsageEditListener, private val woItem:WoItem)

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

        if(usage.type == "1"){
            //labor type
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
            startTxt.setOnClickListener {
                // editStart()
                usageEditListener.editStart(position)
            }


            val stopTxt:TextView = holder.itemView.findViewById(R.id.usage_stop_edit_txt)
            stopTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.stop != null && usage.stop != "0000-00-00 00:00:00"){
                stopTxt.text = usage.getTime(usage.stop!!)
            }

            stopTxt.setOnClickListener {
                //editStart()
                usageEditListener.editStop(position)
            }


            val breakTxt:TextView = holder.itemView.findViewById(R.id.usage_break_edit_txt)

            breakTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            breakTxt.setSelectAllOnFocus(true)

            breakTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.lunch != null){
                breakTxt.text = usage.lunch!!
            }

            breakTxt.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    breakTxt.clearFocus()
                    usageEditListener.editBreak(position, breakTxt.text.toString(), actionId)
                    true
                } else {
                    false
                }
            }


            val totalTxt:TextView = holder.itemView.findViewById(R.id.usage_total_tv)


            totalTxt.text = usage.qty!! + " Hours"



            //options btn click
            holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
                println("status click")

                val popUp = PopupMenu(myView.context,holder.itemView)
                popUp.inflate(R.menu.task_status_menu)

                popUp.menu.add(0, usage.ID.toInt(), 1, globalVars.menuIconWithText(globalVars.resize(context.getDrawable(R.drawable.ic_canceled)!!,context), context.getString(R.string.delete)))

                popUp.setOnMenuItemClickListener {

                    usageEditListener.deleteUsage(position)

                    true
                }

                popUp.gravity = Gravity.END
                popUp.show()

            }

        }else{
            //material type
            laborCl.isVisible = false

            val vendorSearch:Spinner = holder.itemView.findViewById(R.id.usage_vendor_spinner)
            vendorSearch.setBackgroundResource(R.drawable.text_view_layout)
            /*
            if (usage.vendor != null && usage.vendor != "0" && usage.vendor != ""){
                //vendorSearch.setQuery(usage.vendor!!)
            }

             */

            val adapter: ArrayAdapter<Vendor> = ArrayAdapter<Vendor>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, woItem.vendors

            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

            vendorSearch.adapter = adapter


            val quantityTxt:TextView = holder.itemView.findViewById(R.id.usage_quantity_et)
            quantityTxt.text = usage.qty

            quantityTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            quantityTxt.setSelectAllOnFocus(true)

            quantityTxt.setBackgroundResource(R.drawable.text_view_layout)

            quantityTxt.setOnEditorActionListener { _, actionId, _ ->
                //var numeric = true

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Call your code here

                    quantityTxt.clearFocus()

                    val qtyInput = quantityTxt.text.toString().toDouble().roundToInt()

                    usageEditListener.editQty(position, qtyInput, actionId)


                    true
                } else {
                    false
                }
            }



            val unitCostTxt:EditText = holder.itemView.findViewById(R.id.usage_unit_cost_et)
            if(usage.unitCost != null){
                unitCostTxt.setText(usage.unitCost!!)
            }


            unitCostTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            unitCostTxt.setSelectAllOnFocus(true)

            unitCostTxt.setBackgroundResource(R.drawable.text_view_layout)

            unitCostTxt.setOnEditorActionListener { _, actionId, _ ->
                //var numeric = true

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Call your code here

                    unitCostTxt.clearFocus()

                    val costInput = unitCostTxt.text.toString().toDouble()
                    val costInputTrimmed = (costInput * 100.0).roundToInt() / 100.0
                    usageEditListener.editCost(position, costInputTrimmed, actionId)


                    true
                } else {
                    false
                }
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