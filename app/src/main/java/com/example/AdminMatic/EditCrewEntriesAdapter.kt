package com.example.AdminMatic

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.util.*


class EditCrewEntriesAdapter(private val list: MutableList<EmployeeOrEquipment>, private val crewIndex:Int, private val crewEntryDelegate: CrewEntryDelegate, private val cellClickListener: CrewEntryCellClickListener): RecyclerView.Adapter<CrewEntryViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<EmployeeOrEquipment> = emptyList<EmployeeOrEquipment>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewEntryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CrewEntryViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CrewEntryViewHolder, position: Int) {

        val data: EmployeeOrEquipment = filterList[position]
        holder.bind(data)
        //println("queryText = $queryText")
        //text highlighting for first string

        if (data.isEquipment) {
            val equipmentImageView:ImageView = holder.itemView.findViewById(R.id.equipment_iv)
            val filePath = "https://www.adminmatic.com" + data.equipment!!.picURL!!
            Picasso.with(myView.context)
                .load(filePath)
                .placeholder(R.drawable.user_placeholder) //optional
                .into(equipmentImageView)
        }
        else {
            val employeeImageView:ImageView = holder.itemView.findViewById(R.id.employee_iv)
            Picasso.with(myView.context)
                .load(GlobalVars.thumbBase + data.employee!!.pic)
                .placeholder(R.drawable.user_placeholder) //optional
                .into(employeeImageView)
        }

        if (cellClickListener != null) {
            holder.itemView.setOnClickListener {
                cellClickListener.onCrewEntryCellClickListener(data)
            }
        }


        if (data.isEquipment) {
            holder.itemView.findViewById<TextView>(R.id.equipment_options_tv).setOnClickListener {
                println("menu click")

                val popUp = PopupMenu(myView.context, holder.itemView.findViewById<TextView>(R.id.equipment_options_tv))
                popUp.inflate(R.menu.crew_entry_menu)
                popUp.gravity = Gravity.CENTER
                popUp.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item!!.itemId) {
                        R.id.move_to -> {
                            // These can be !!'d because any time the delegate isn't provided this code is unreachable anyways
                            crewEntryDelegate!!.onMoveCrewEntry(data, crewIndex, holder.itemView.findViewById<TextView>(R.id.equipment_options_tv))
                        }
                        R.id.unassign -> {
                            crewEntryDelegate!!.onUnassignCrewEntry(data, crewIndex)
                        }

                    }

                    true
                }
                popUp.show()
            }
        } else {
            holder.itemView.findViewById<TextView>(R.id.employee_options_tv).setOnClickListener {
                println("menu click")

                val popUp = PopupMenu(myView.context, holder.itemView.findViewById<TextView>(R.id.employee_options_tv))
                popUp.inflate(R.menu.crew_entry_menu)
                popUp.gravity = Gravity.CENTER
                popUp.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item!!.itemId) {
                        R.id.move_to -> {
                            crewEntryDelegate!!.onMoveCrewEntry(data, crewIndex, holder.itemView.findViewById<TextView>(R.id.employee_options_tv))
                        }
                        R.id.unassign -> {
                            crewEntryDelegate!!.onUnassignCrewEntry(data, crewIndex)
                        }

                    }

                    true
                }
                popUp.show()
            }
        }

    }

    override fun getItemCount(): Int {

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }


    /*
    fun changeData(data: List<Customer>) {
        // checking the size of the list before
        println("filterList.size before: " + filterList.size)
        filterList = data.toMutableList()

        // checking the size of the list after
        println("filterList.size after: " + filterList.size)

        notifyDataSetChanged()
    }
*/


    // Clean all elements of the recycler
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<Customer>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class EditCrewEntryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.employee_or_equipment_list_item, parent, false)) {
    private var mEmployeeCL: ConstraintLayout? = null
    private var mEquipmentCL: ConstraintLayout? = null


    init {
        mEmployeeCL = itemView.findViewById(R.id.employee_cl)
        mEquipmentCL = itemView.findViewById(R.id.equipment_cl)
    }

    fun bind(employeeOrEquipment: EmployeeOrEquipment) {

        if (employeeOrEquipment.isEquipment) {
            mEmployeeCL!!.visibility = View.INVISIBLE
            mEquipmentCL!!.visibility = View.VISIBLE

            itemView.findViewById<TextView>(R.id.equipment_name_tv).text = employeeOrEquipment.equipment!!.name

        }
        else {
            mEmployeeCL!!.visibility = View.VISIBLE
            mEquipmentCL!!.visibility = View.INVISIBLE
            itemView.findViewById<TextView>(R.id.employee_name_tv).text = employeeOrEquipment.employee!!.name
        }

    }



}