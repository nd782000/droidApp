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


class CrewAdapter(list: MutableList<Crew>, private val context: Context, cellClickListener: EmployeeCellClickListener)

    : RecyclerView.Adapter<CrewViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null
    private val cCL = cellClickListener

    var filterList:MutableList<Crew> = emptyList<Crew>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CrewViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {

        val crew: Crew = filterList[position]
        holder.bind(crew)
        println("queryText = $queryText")

        val empList = crew.emps!!.toMutableList()
        val adapter = EmployeesAdapter(empList, context, cCL)

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


class CrewViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
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
    fun bind(crew: Crew) {

        mNameView!!.text = crew.name
        mColorView!!.background = ColorDrawable(parseColor(crew.color))

    }
}