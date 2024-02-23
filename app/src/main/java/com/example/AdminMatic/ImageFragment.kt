package com.example.AdminMatic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageBinding
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

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.image_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_image_item) {
            if (GlobalVars.permissions!!.vendorsEdit == "1") {
                val directions = ImageFragmentDirections.navigateToEditImage(imageList, imageListIndex)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_vendors_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //image = it.getParcelable("image")
            //var imageListRaw : TypedArray = it.getParcelable("imageList")!!
            imageList = (it.getParcelableArray("imageList") as Array<Image>?)!!
            imageListIndex = it.getInt("imageListIndex")

        }
        setFragmentResultListener("imageEditListener") { _, bundle ->
            val shouldRefreshImages = bundle.getBoolean("shouldRefreshImages")
            if (shouldRefreshImages) {
                println("got result listener")
                setFragmentResult("imageListSettings", bundleOf("shouldRefreshImages" to true))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        primaryColor = R.color.colorPrimary
        setHasOptionsMenu(true)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cache images
        Picasso.with(context).load(R.drawable.ic_liked).fetch()
        Picasso.with(context).load(R.drawable.ic_unliked).fetch()

        val gestureDetector = GestureDetector(activity, object : SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean { return true }

            override fun onFling(e1: MotionEvent?, event1: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
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

            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleLike()
                return true
            }
        })
        binding.imageDetailsIv.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }


        ImageViewCompat.setImageTintList(binding.likeIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, primaryColor)))

        binding.likeIv.setOnClickListener {
            toggleLike()
        }

        binding.likesTv.setOnClickListener {
            if (image!!.likes != "0") {
                image.let {
                    val directions = ImageFragmentDirections.navigateImageToImageLikes(image!!)
                    myView.findNavController().navigate(directions)
                }
            }
        }

        binding.imageCustomerTv.setOnClickListener {
            if (!image!!.customer.isNullOrBlank() && image!!.customer!= "0") {
                val directions = ImageFragmentDirections.navigateImageToCustomer(image!!.customer!!)
                myView.findNavController().navigate(directions)
            }
        }

        getImage()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("image")
    }

    private fun toggleLike() {
        var likeCount:Int? = image!!.likes?.toInt()
        if (image!!.liked == "1") {
            image!!.liked = "0"
            if (likeCount != null) {
                likeCount--
            }
            Picasso.with(context).load(R.drawable.ic_unliked).into(binding.likeIv)
            ImageViewCompat.setImageTintList(binding.likeIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, primaryColor)))
            setLiked(false)
        }
        else {
            image!!.liked = "1"
            if (likeCount != null) {
                likeCount++
            }
            Picasso.with(context).load(R.drawable.ic_liked).into(binding.likeIv)
            ImageViewCompat.setImageTintList(binding.likeIv, null)
            binding.likeIv.colorFilter = null
            setLiked(true)
        }
        image!!.likes = likeCount.toString()
        setLikesViewText()
    }

    private fun setLikesViewText() {
        if (image!!.likes == "1") {
            binding.likesTv.text = getString(R.string.one_like)
        }
        else {
            binding.likesTv.text = getString(R.string.x_likes, image!!.likes)
        }
    }

    private fun setLiked(liked:Boolean) {
        println("getImages")

        //showProgressView()

        binding.likeIv.isClickable = false
        binding.likeIv.isFocusable = false

        var urlString:String = if (liked) {
            "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/new/like.php"
        } else {
            "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/like.php"
        }

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")
                binding.likeIv.isClickable = true
                binding.likeIv.isFocusable = true

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
            .into(binding.imageDetailsIv)                        //Your image view object.

        println("image.likes = ${image!!.likes}")
        if (image!!.likes!! != "0"){
            binding.likeIv.visibility = View.VISIBLE
            // val likeIcon = resources.getDrawable(R.drawable.ic_liked,null)
            //likeView.background = likeIcon
        }

        if (image!!.liked == "1") {
            Picasso.with(context).load(R.drawable.ic_liked).into(binding.likeIv)
            ImageViewCompat.setImageTintList(binding.likeIv, null)
        }

        setLikesViewText()

        binding.imageCustomerTv.text = image!!.customerName

        val imageDate = LocalDate.parse(image!!.dateAdded, GlobalVars.dateFormatterPHP)
        binding.imageDetailsTv.text = getString(R.string.image_description, image!!.createdByName, imageDate.format(GlobalVars.dateFormatterShort), image!!.description)
    }

}