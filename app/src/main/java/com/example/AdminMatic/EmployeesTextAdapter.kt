package com.example.AdminMatic

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.employee_list_item.view.*
import java.util.*



class EmployeesTextAdapter(private val list: Array<Employee>, private val checkList: Array<Boolean>, private val cellClickListener: EmployeeCheckClickListener) : RecyclerView.Adapter<EmployeeTextViewHolder>() {

    lateinit var globalVars:GlobalVars

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeTextViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        globalVars = GlobalVars()
        return EmployeeTextViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EmployeeTextViewHolder, position: Int) {

        val context = myView.context

        val employee: Employee = list[position]
        holder.bind(employee)


        val employeeImageView:ImageView = holder.itemView.findViewById(R.id.employee_text_list_item_iv)
        Picasso.with(context)
            .load(GlobalVars.thumbBase + list[position].pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(employeeImageView)                       //Your image view object.

        val checkImageView:ImageView = holder.itemView.findViewById(R.id.employee_text_list_item_check_iv)

        if (checkList[position]) {
            Picasso.with(context).load(R.drawable.ic_check_enabled).into(checkImageView)
        }
        else {
            Picasso.with(context).load(R.drawable.ic_check_disabled).into(checkImageView)
        }

        checkImageView.setOnClickListener {
            cellClickListener.onEmployeeCheckClickListener(position, checkImageView)
        }


    }

    override fun getItemCount(): Int{
        print("getItemCount = ${list.size}")
        return list.size
    }





}

class EmployeeTextViewHolder(inflater: LayoutInflater, parent: ViewGroup, var employee:Employee? = null) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.employee_text_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mNumberView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.employee_text_list_item_name_tv)
        mNumberView = itemView.findViewById(R.id.employee_text_list_item_number_tv)
    }

    fun bind(employee: Employee) {
        mNameView?.text = employee.name
        mNumberView?.text = employee.phone
    }

}