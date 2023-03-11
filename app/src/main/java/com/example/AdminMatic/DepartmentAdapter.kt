package com.example.AdminMatic

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class DepartmentAdapter(list: MutableList<Department>, private val context: Context, cellClickListener: DepartmentCellClickListener)

    : RecyclerView.Adapter<DepartmentViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null
    private val cCL = cellClickListener

    var filterList:MutableList<Department> = emptyList<Department>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DepartmentViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {

        val department: Department = filterList[position]
        holder.bind(department)

        holder.itemView.setOnClickListener {
            cCL.onDepartmentCellClickListener(department)
        }

    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class DepartmentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.department_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mColorView: View? = null


    init {
        mNameView = itemView.findViewById(R.id.department_name)
        mColorView = itemView.findViewById(R.id.department_color_view)
    }

    @SuppressLint("Range")
    fun bind(department: Department) {

        mNameView!!.text = department.name
        mColorView!!.backgroundTintList = ColorStateList.valueOf(parseColor(department.color))

    }
}