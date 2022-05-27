package com.example.AdminMatic

import android.content.res.TypedArray
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_upload.view.*
import java.time.LocalDate
import kotlin.math.abs

class ImageFragment : Fragment() {



    private var image: Image? = null
    //private var imageList:MutableList<Image>? = null
    private lateinit var imageList:Array<Image>
    private var imageListIndex:Int = 0

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    lateinit var pgsBar: ProgressBar
    lateinit var imageView:ImageView
    private lateinit var likeView:ImageView
    private lateinit var likesTextView:TextView
    private lateinit var custNameTextView:TextView
    private lateinit var detailsTextView:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //image = it.getParcelable("image")
            //var imageListRaw : TypedArray = it.getParcelable("imageList")!!
            imageList = (it.getParcelableArray("imageList") as Array<Image>?)!!
            imageListIndex = it.getInt("imageListIndex")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_image, container, false)
        myView = inflater.inflate(R.layout.fragment_image, container, false)

        globalVars = GlobalVars()

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Todo:
        pgsBar = view.findViewById(R.id.progress_bar)
        //pgsBar.visibility = View.INVISIBLE

        imageView = myView.findViewById(R.id.image_details_iv)
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean { return true }
            override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (abs(velocityX) > abs(velocityY)) {
                    if (velocityX > 0) {
                        println("right swipe")
                        imageListIndex--
                        if (imageListIndex < 0) {
                            imageListIndex = imageList.size-1
                        }
                        getImage()
                    }
                    else {
                        println("left swipe")
                        imageListIndex++
                        if (imageListIndex >= imageList.size) {
                            imageListIndex = 0
                        }
                        getImage()
                    }
                }
                return true
            }
        })
        imageView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        getImage()

    }

    private fun getImage() {

        println("ImageList.size: ${imageList.size}")
        println("ImageListIndex: $imageListIndex")

        image = imageList[imageListIndex]
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = image!!.name
        println("image = ${image!!.ID}")
        println("image path = ${GlobalVars.mediumBase + image!!.fileName}")
        Picasso.with(context)
            .load(GlobalVars.mediumBase + image!!.fileName)
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(imageView)                        //Your image view object.

        likeView  = myView.findViewById(R.id.like_iv)
        println("image.likes = ${image!!.likes}")
        if (image!!.likes!! != "0"){
            likeView.visibility = View.VISIBLE
            // val likeIcon = resources.getDrawable(R.drawable.ic_liked,null)
            //likeView.background = likeIcon
        }

        likesTextView  = myView.findViewById(R.id.likes_tv)
        likesTextView.text = getString(R.string.x_likes, image!!.likes)

        custNameTextView  = myView.findViewById(R.id.image_customer_tv)
        custNameTextView.text = image!!.customerName

        detailsTextView  = myView.findViewById(R.id.image_details_tv)
        val imageDate = LocalDate.parse(image!!.dateAdded, GlobalVars.dateFormatterPHP)
        detailsTextView.text = getString(R.string.image_description, image!!.createdByName, imageDate.format(GlobalVars.dateFormatterShort), image!!.description)
    }

}