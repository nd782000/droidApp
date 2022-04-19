package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.wo_item_list_item.view.*


class WoItemsAdapter(list: MutableList<WoItem>, private val cellClickListener: WoItemCellClickListener) : RecyclerView.Adapter<WoItemViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<WoItem> = emptyList<WoItem>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WoItemViewHolder(inflater, parent)
    }

    private var listener: LogOut? = null





    override fun onBindViewHolder(holder: WoItemViewHolder, position: Int) {




        val woItem: WoItem = filterList[position]
        holder.bind(woItem)
        println("queryText = $queryText")
        //text highlighting for first string


        holder.itemView.list_name.text = filterList[position].item




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onWoItemCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,holder.itemView)
            popUp.inflate(R.menu.options_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.menu1 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu2 -> {
                        Toast.makeText(myView.context, data.ID, Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu3 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                }

                true
            }



            popUp.show()

            /*
            fun onClick(view: View?) {
                println("menu click")
                //will show popup menu here
            }*/


        }



    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class WoItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.wo_item_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)

    }

    fun bind(woItem: WoItem) {
        mNameView?.text = woItem.item

    }



}