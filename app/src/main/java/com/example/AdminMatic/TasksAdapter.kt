package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import org.json.JSONException
import org.json.JSONObject


class TasksAdapter(private val list: MutableList<Task>, private val context: Context, private val appContext: Context, private val cellClickListener: TaskCellClickListener, private val woItem:WoItem)

    : RecyclerView.Adapter<TaskViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    lateinit  var globalVars:GlobalVars



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        globalVars = GlobalVars()

        return TaskViewHolder(inflater, parent)
    }






    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {




        val task: Task = list[position]
        holder.bind(task)

        //text highlighting for first string


        holder.itemView.list_name.text = list[position].task



        if (task.images != null){
            if (task.images!!.count() > 0){
                val taskImageView:ImageView = holder.itemView.findViewById(R.id.task_thumb_iv)


                Picasso.with(context)
                    .load(GlobalVars.thumbBase + task.images!![0].fileName)
                    .placeholder(R.drawable.ic_images) //optional
                    //.resize(imgWidth, imgHeight)         //optional
                    //.centerCrop()                        //optional
                    .into(taskImageView)                       //Your image view object.
            }
        }





        val data = list[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onTaskCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<ImageView>(R.id.task_status_iv).setOnClickListener {
            println("status click")

            val popUp = PopupMenu(myView.context,holder.itemView)
            popUp.inflate(R.menu.task_status_menu)
           // popUp.menu.getItem(0).subMenu.getItem(3).setVisible(false)
            //popUp.menu.getItem(0).subMenu.getItem(4).setVisible(true)

            //val d:Drawable? = resize(context.getDrawable(R.drawable.ic_in_progress)!!)

            popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(context, R.drawable.ic_not_started)!!,context), context.getString(R.string.not_started)))
            popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(context, R.drawable.ic_in_progress)!!,context), context.getString(R.string.in_progress)))
            popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(context, R.drawable.ic_done)!!,context), context.getString(R.string.finished)))
            popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(context, R.drawable.ic_canceled)!!,context), context.getString(R.string.canceled)))

           // menu.add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.mipmap.user_2), getResources().getString(R.string.action_profile)));




            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                task.status = item!!.itemId.toString()
                Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()

                if (item.itemId == 2 || item.itemId == 3) {
                    println("prompt for image upload")

                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle("Image Upload")
                    builder.setMessage("Do you want to upload a task image now?")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        Toast.makeText(
                            myView.context,
                            android.R.string.ok, Toast.LENGTH_SHORT
                        ).show()

                        cellClickListener.uploadImage(task)
                    }

                    builder.setNegativeButton(android.R.string.cancel) { _, _ ->
                        Toast.makeText(
                            myView.context,
                            android.R.string.cancel, Toast.LENGTH_SHORT
                        ).show()
                    }

                    /*
                    builder.setNeutralButton("Maybe") { dialog, which ->
                        Toast.makeText(myView.context,
                            "Maybe", Toast.LENGTH_SHORT).show()
                    }
                    */


                    builder.show()
                }
                /*
                when (item!!.itemId) {
                    1 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        Toast.makeText(myView.context, data.ID, Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                    4 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                }
*/


                /*
                var newItemStatus:String = "1"
        var parameters:[String:String]
        parameters = [
            "taskID":_ID,
            "status":_status,
            "empID":(self.appDelegate.loggedInEmployee?.ID)!,
            "woItemID":self.woItem.ID,
            "woID":self.workOrder.ID,
            "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
            "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!

        ]
                 */

                cellClickListener.showProgressView()

                var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/taskStatus.php"

                val currentTimestamp = System.currentTimeMillis()
                println("urlString = ${"$urlString?cb=$currentTimestamp"}")
                urlString = "$urlString?cb=$currentTimestamp"

                val postRequest1: StringRequest = object : StringRequest(
                    Method.POST, urlString,
                    Response.Listener { response -> // response

                        println("Response $response")

                        //hideProgressView()


                        try {
                            val parentObject = JSONObject(response)
                            println("parentObject = $parentObject")
                            // var payrollJSON: JSONArray = parentObject.getJSONArray("payroll")
                            // println("payroll = ${payrollJSON.toString()}")
                            // println("payroll count = ${payrollJSON.length()}")

                            cellClickListener.hideProgressView()

                            // getPayroll()

                            cellClickListener.getWoItem(true)


                            /* Here 'response' is a String containing the response you received from the website... */
                        } catch (e: JSONException) {
                            println("JSONException")
                            e.printStackTrace()
                        }

                    },
                    Response.ErrorListener { // error

                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["companyUnique"] = loggedInEmployee!!.companyUnique
                        params["sessionKey"] = loggedInEmployee!!.sessionKey
                        params["taskID"] = task.ID
                        params["status"] = task.status
                        params["woItemID"] = woItem.ID
                        params["woID"] = woItem.woID
                        params["empID"] = loggedInEmployee!!.ID


                        println("params = $params")
                        return params
                    }
                }
                postRequest1.tag = "woItem"
                VolleyRequestQueue.getInstance(appContext).addToRequestQueue(postRequest1)


                true
            }




            popUp.gravity = Gravity.START
            popUp.show()




        }



    }


    /*

    // To Add Icons to Menu Items

    private fun resize(image: Drawable): Drawable? {
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 25, 25, true)
        return BitmapDrawable(context.resources, bitmapResized)
    }


    private fun menuIconWithText(
        r: Drawable,
        title: String
    ): CharSequence? {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_CENTER)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    */




    override fun getItemCount(): Int{

        print("getItemCount = ${list.size}")
        return list.size

    }






}

class TaskViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.task_list_item, parent, false)) {
    private var mNameView: TextView? = null

    //private var thumbView:ImageView? = null
    private var statusView:ImageView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        //thumbView = itemView.findViewById(R.id.task_thumb_iv)
        statusView = itemView.findViewById(R.id.task_status_iv)

    }

    fun bind(task: Task) {
        mNameView?.text = task.task

        setStatus(task.status)


    }


    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "1" -> {
                println("1")
                statusView!!.setBackgroundResource(R.drawable.ic_not_started)

            }
            "2" -> {
                println("2")
                statusView!!.setBackgroundResource(R.drawable.ic_in_progress)

            }
            "3" -> {
                println("3")
                statusView!!.setBackgroundResource(R.drawable.ic_done)

            }
            "4" -> {
                println("4")
                statusView!!.setBackgroundResource(R.drawable.ic_canceled)

            }
            "5" -> {
            println("5")
            statusView!!.setBackgroundResource(R.drawable.ic_waiting)

            }
        }





    }


}