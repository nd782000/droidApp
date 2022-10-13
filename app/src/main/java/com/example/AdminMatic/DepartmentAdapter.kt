package com.example.AdminMatic

import android.annotation.SuppressLint
import android.content.Context
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


class DepartmentAdapter(list: MutableList<Department>, private val context: Context, cellClickListener: EmployeeCellClickListener)

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
        println("queryText = $queryText")

        val empList = department.emps!!.toMutableList()
        val adapter = EmployeesAdapter(empList, true, context, cCL)

        //holder.mRecycler!!.setHasFixedSize(true)
        holder.mRecycler!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.mRecycler!!.adapter = adapter
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        holder.mRecycler!!.addItemDecoration(itemDecoration)

    }

    override fun getItemCount(): Int{
        return filterList.size
    }
}


class DepartmentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.department_header, parent, false)) {
    private var mNameView: TextView? = null
    private var mColorView: View? = null
    var mRecycler: RecyclerView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mColorView = itemView.findViewById(R.id.section_header_color_view)
        mRecycler = itemView.findViewById(R.id.department_recycler_view)
    }

    @SuppressLint("Range")
    fun bind(department: Department) {

        mNameView!!.text = department.name
        mColorView!!.background = ColorDrawable(parseColor(department.color))

    }
}