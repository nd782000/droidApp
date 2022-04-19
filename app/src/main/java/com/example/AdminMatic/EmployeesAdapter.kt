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


class EmployeesAdapter(private val list: MutableList<Employee>, private val context: Context,private val cellClickListener: EmployeeCellClickListener)

    : RecyclerView.Adapter<EmployeeViewHolder>(), Filterable {


    var filterList:MutableList<Employee> = emptyList<Employee>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EmployeeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {




        val employee: Employee = filterList[position]
        holder.bind(employee)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        println("queryText = $queryText")
        //text highlighting for first string
        if (queryText.isNotEmpty() && queryText != "") {

            val startPos1: Int = filterList[position].name.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos1 = startPos1 + queryText.length
            if (startPos1 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].name)
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(Color.parseColor("#005100"))
                )
                val textAppearanceSpan =
                    TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                spannable.setSpan(
                    textAppearanceSpan,
                    startPos1,
                    endPos1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.itemView.list_name.text = spannable
            } else {
                holder.itemView.list_name.text = filterList[position].name
            }
        } else {
            holder.itemView.list_name.text = filterList[position].name
        }

        val employeeImageView:ImageView = holder.itemView.findViewById(R.id.employee_item_image_view)


        Picasso.with(context)
            .load(GlobalVars.thumbBase + filterList[position].pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(employeeImageView)                       //Your image view object.

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onEmployeeCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,holder.itemView)
            popUp.inflate(R.menu.emp_options_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.call -> {
                        //Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                        println("phone btn clicked ${employee.phone}")

                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.phone))
                        myView.context.startActivity(intent)
                    }
                    R.id.text -> {


                        // val intent = Intent(Intent.ACTION_SEND, Uri.parse("tel:" + data.phone))
                        //intent.putExtra("sms_body", "Here goes your message...")
                        // myView.context.startActivity(intent)
                        // val number =
                        // "12346556" // The number on which you want to send SMS

                        myView.context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.fromParts("sms", data.phone, null)
                            )
                        )
                        //startActivity(Intent.createChooser(intent, "Send Email Using: "));

                        // Toast.makeText(myView.context, data.ID, Toast.LENGTH_SHORT).show()
                        // val intent = Intent(Intent., Uri.parse("tel:" + employee!!.phone))
                        // myView.context.startActivity(intent)
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

            holder.employee = employee


        }



    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }




    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                queryText = charSearch

                if (charSearch.isEmpty()) {
                    //filterList.clear()
                    filterList = list
                } else {

                    val resultList:MutableList<Employee> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

                            println("add row")

                            resultList.add(row)

                            println("resultList.count = ${resultList.count()}")
                        }
                    }
                    filterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList

               println("filterResults = ${filterResults.values}")
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                println("publishResults")

                filterList = results?.values as MutableList<Employee>
                notifyDataSetChanged()
            }

        }
    }

    // Clean all elements of the recycler
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<Employee>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class EmployeeViewHolder(inflater: LayoutInflater, parent: ViewGroup, var employee:Employee? = null) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.employee_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)

    }

    fun bind(employee: Employee) {
        mNameView?.text = employee.name

    }



}