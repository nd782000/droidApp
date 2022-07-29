package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.abs

class ImageFragment : Fragment() {



    private var image: Image? = null
    //private var imageList:MutableList<Image>? = null
    private lateinit var imageList:Array<Image>
    private var imageListIndex:Int = 0

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private var primaryColor: Int = 0


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
        primaryColor = R.color.colorPrimary

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cache images
        Picasso.with(context).load(R.drawable.ic_liked).fetch()
        Picasso.with(context).load(R.drawable.ic_unliked).fetch()

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

        likeView  = myView.findViewById(R.id.like_iv)
        ImageViewCompat.setImageTintList(likeView, ColorStateList.valueOf(ContextCompat.getColor(myView.context, primaryColor)))
        likeView.setOnClickListener {
            var likeCount:Int? = image!!.likes?.toInt()
            if (image!!.liked == "1") {
                image!!.liked = "0"
                if (likeCount != null) {
                    likeCount--
                }
                Picasso.with(context).load(R.drawable.ic_unliked).into(likeView)
                ImageViewCompat.setImageTintList(likeView, ColorStateList.valueOf(ContextCompat.getColor(myView.context, primaryColor)))
                setLiked(false)
            }
            else {
                image!!.liked = "1"
                if (likeCount != null) {
                    likeCount++
                }
                Picasso.with(context).load(R.drawable.ic_liked).into(likeView)
                ImageViewCompat.setImageTintList(likeView, null)
                likeView.colorFilter = null
                setLiked(true)
            }
            image!!.likes = likeCount.toString()
            setLikesViewText()
        }

        likesTextView = myView.findViewById(R.id.likes_tv)
        likesTextView.setOnClickListener {
            if (image!!.likes != "0") {
                image.let {
                    val directions = ImageFragmentDirections.navigateImageToImageLikes(image!!)
                    myView.findNavController().navigate(directions)
                }
            }
        }

        getImage()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("image")
    }

    private fun setLikesViewText() {
        if (image!!.likes == "1") {
            likesTextView.text = getString(R.string.one_like)
        }
        else {
            likesTextView.text = getString(R.string.x_likes, image!!.likes)
        }
    }

    private fun setLiked(liked:Boolean) {
        println("getImages")

        //showProgressView()

        likeView.isClickable = false
        likeView.isFocusable = false

        var urlString:String = if (liked) {
            "https://www.adminmatic.com/cp/app/functions/new/like.php"
        } else {
            "https://www.adminmatic.com/cp/app/functions/delete/like.php"
        }

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")
                likeView.isClickable = true
                likeView.isFocusable = true

                //hideProgressView()

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["empID"] = GlobalVars.loggedInEmployee!!.ID
                params["imageID"] = image!!.ID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "image"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
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

        println("image.likes = ${image!!.likes}")
        if (image!!.likes!! != "0"){
            likeView.visibility = View.VISIBLE
            // val likeIcon = resources.getDrawable(R.drawable.ic_liked,null)
            //likeView.background = likeIcon
        }

        if (image!!.liked == "1") {
            Picasso.with(context)
                .load(R.drawable.ic_liked)
                .into(likeView)
        }

        setLikesViewText()

        custNameTextView  = myView.findViewById(R.id.image_customer_tv)
        custNameTextView.text = image!!.customerName

        detailsTextView  = myView.findViewById(R.id.image_details_tv)
        val imageDate = LocalDate.parse(image!!.dateAdded, GlobalVars.dateFormatterPHP)
        detailsTextView.text = getString(R.string.image_description, image!!.createdByName, imageDate.format(GlobalVars.dateFormatterShort), image!!.description)
    }

}