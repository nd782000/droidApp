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

            var employeeImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.usage_emp_iv)


            Picasso.with(context)
                .load("${GlobalVars.thumbBase + list[position].pic}")
                .placeholder(R.drawable.user_placeholder) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(employeeImageView)                       //Your image view object.





            var startTxt:TextView = holder.itemView.findViewById<TextView>(R.id.usage_start_edit_txt)
            startTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.start != null && usage.start != "0000-00-00 00:00:00"){
                startTxt.text = usage.getTime(usage.start!!)
            }
            startTxt.setOnClickListener({
                // editStart()
                usageEditListener.editStart(position)
            })



            var stopTxt:TextView = holder.itemView.findViewById<TextView>(R.id.usage_stop_edit_txt)
            stopTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.stop != null && usage.stop != "0000-00-00 00:00:00"){
                stopTxt.text = usage.getTime(usage.stop!!)
            }

            stopTxt.setOnClickListener({
                //editStart()
                usageEditListener.editStop(position)
            })


            var breakTxt:TextView = holder.itemView.findViewById<TextView>(R.id.usage_break_edit_txt)

            breakTxt.setRawInputType(Configuration.KEYBOARD_12KEY)
            breakTxt.setSelectAllOnFocus(true);

            breakTxt.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.lunch != null){
                breakTxt.text = usage.lunch!!
            }







            breakTxt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override  fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {

                    //var numeric = true

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Call your code here

                        breakTxt.clearFocus()

                        usageEditListener.editBreak(position,breakTxt.text.toString(),actionId)


                        return true
                    }else{
                        return false
                    }








                }
            })












            var totalTxt:TextView = holder.itemView.findViewById<TextView>(R.id.usage_total_tv)

            if (usage.qty != null){
                totalTxt.text = usage.qty!! + " Hours"
            }



            //options btn click
            holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener(){
                println("status click")

                var popUp:PopupMenu = PopupMenu(myView.context,holder.itemView)
                popUp.inflate(R.menu.task_status_menu)

                popUp.menu.add(0, usage.ID.toInt(), 1, globalVars.menuIconWithText(globalVars.resize(context.getDrawable(R.drawable.ic_canceled)!!,context)!!, context.getString(R.string.delete)))

                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

                    usageEditListener.deleteUsage(position)

                    true
                })

                popUp.gravity = Gravity.RIGHT
                popUp.show()

            }

        }else{
            //material type
            laborCl.isVisible = false

            var vendorSearch:SearchView = holder.itemView.findViewById<SearchView>(R.id.usage_vendor_sv)
            vendorSearch.setBackgroundResource(R.drawable.text_view_layout)
            if (usage.vendor != null && usage.vendor != "0" && usage.vendor != ""){
                //vendorSearch.setQuery(usage.vendor!!)
            }

            var quantityTxt:EditText = holder.itemView.findViewById(R.id.usage_quantity_et)
           quantityTxt.setText(usage.qty)

            var unitCostTxt:EditText = holder.itemView.findViewById(R.id.usage_quantity_et)
            if(usage.unitCost != null){
                unitCostTxt.setText(usage.unitCost!!)
            }


            var totalCostTxt:EditText = holder.itemView.findViewById(R.id.usage_quantity_et)
            if(usage.totalCost != null){
                totalCostTxt.setText(usage.totalCost!!)
            }



            var receiptImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.usage_receipt_iv)
            if(list[position].receipt != null){
                Picasso.with(context)
                    .load("${GlobalVars.thumbBase + list[position].receipt!!}")
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