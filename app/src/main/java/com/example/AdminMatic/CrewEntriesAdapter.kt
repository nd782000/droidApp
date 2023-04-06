package com.example.AdminMatic

import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso


class CrewEntriesAdapter(private val list: MutableList<EmployeeOrEquipment>, private val crewIndex:Int, private val crewID:String, private val crewEntryDelegate: CrewEntryDelegate, private val cellClickListener: CrewEntryCellClickListener, _readOnly:Boolean): RecyclerView.Adapter<CrewEntryViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<EmployeeOrEquipment> = emptyList<EmployeeOrEquipment>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    private var readOnly = _readOnly

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

            println("picURL: ${data.equipment!!.picURL}")
            println("pic: ${data.equipment!!.pic}")
            if (data.equipment!!.image != null) {
                println("image.fileName: ${data.equipment!!.image!!.fileName}")
            }
            else {
                println("image is null")
            }

            if (data.equipment!!.image == null) {
                print("trying to use image")
                Picasso.with(myView.context)
                    .load("https://www.adminmatic.com${data.equipment!!.picURL}")
                    .placeholder(R.drawable.ic_equipment) //optional
                    .into(equipmentImageView)
            }
            else if (data.equipment!!.image!!.fileName.isNotBlank()) {
                print("trying to use image.filename")
                Picasso.with(myView.context)
                    .load(GlobalVars.thumbBase + data.equipment!!.image!!.fileName)
                    .placeholder(R.drawable.ic_equipment) //optional
                    .into(equipmentImageView)
            }

            /*
            if (data.equipment!!.picURL.isNullOrBlank()) {
                print("trying to use pic")
                Picasso.with(myView.context)
                    .load(GlobalVars.thumbBase + data.equipment!!.pic)
                    .placeholder(R.drawable.ic_equipment) //optional
                    .into(equipmentImageView)
            }
            else if (data.equipment!!.picURL != null) {
                print("trying to use picURL")
                Picasso.with(myView.context)
                    .load("https://www.adminmatic.com/${data.equipment!!.picURL}")
                    .placeholder(R.drawable.ic_equipment) //optional
                    .into(equipmentImageView)
            }
            else if (data.equipment!!.image != null) {
                print("trying to use image object")
                val filePath = GlobalVars.thumbBase + data.equipment!!.image!!.fileName
                Picasso.with(myView.context)
                    .load(filePath)
                    .placeholder(R.drawable.ic_equipment) //optional
                    .into(equipmentImageView)
            }
            else {
                print("falling back on default pic")
                Picasso.with(myView.context)
                    .load(R.drawable.ic_equipment)
                    .into(equipmentImageView)
            }

             */
        }
        else {
            val employeeImageView:ImageView = holder.itemView.findViewById(R.id.employee_iv)
            Picasso.with(myView.context)
                .load(GlobalVars.thumbBase + data.employee!!.pic)
                .placeholder(R.drawable.user_placeholder) //optional
                .into(employeeImageView)
        }


        holder.itemView.setOnClickListener {
            cellClickListener.onCrewEntryCellClickListener(data)
        }



        if (!readOnly) {
            if (data.isEquipment) {
                holder.itemView.findViewById<TextView>(R.id.equipment_options_tv).setOnClickListener {
                    println("menu click")

                    val popUp = PopupMenu(myView.context, holder.itemView.findViewById<TextView>(R.id.equipment_options_tv))
                    popUp.inflate(R.menu.crew_entry_menu)
                    if (crewIndex == -1) {
                        popUp.menu.removeItem(R.id.move_to)
                    }
                    popUp.gravity = Gravity.CENTER
                    popUp.setOnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {
                            R.id.move_to -> {
                                // These can be !!'d because any time the delegate isn't provided this code is unreachable anyways
                                crewEntryDelegate.onMoveCrewEntry(data, crewIndex, holder.itemView.findViewById<TextView>(R.id.equipment_options_tv))
                            }
                            R.id.unassign -> {
                                crewEntryDelegate.onUnassignCrewEntry(data, crewIndex)
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
                    if (crewIndex == -1) {
                        popUp.menu.removeItem(R.id.move_to)
                    }
                    if (crewID == "0") {
                        popUp.menu.removeItem(R.id.unassign)
                    }

                    popUp.gravity = Gravity.CENTER
                    popUp.setOnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {
                            R.id.move_to -> {
                                crewEntryDelegate.onMoveCrewEntry(data, crewIndex, holder.itemView.findViewById<TextView>(R.id.employee_options_tv))
                            }
                            R.id.unassign -> {
                                crewEntryDelegate.onUnassignCrewEntry(data, crewIndex)
                            }

                        }

                        true
                    }
                    popUp.show()
                }
            }
        }
        else { //readOnly
            if (data.isEquipment) {
                holder.itemView.findViewById<TextView>(R.id.equipment_options_tv).visibility = View.GONE
            }
            else {
                holder.itemView.findViewById<TextView>(R.id.employee_options_tv).visibility = View.GONE
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

class CrewEntryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
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