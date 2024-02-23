package com.example.AdminMatic

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageUploadBinding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.t2r2.volleyexample.FileDataPart
import com.t2r2.volleyexample.VolleyFileUploadRequest
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ImageCellData(_uri:Uri?, _imageData:Image?, _toBeSaved:Boolean) {
    var uri = _uri
    var imageData = _imageData
    var toBeSaved = _toBeSaved
}

//private var imageCellDataList:MutableList<ImageCellData> = mutableListOf()
private var imageCellMap = LinkedHashMap<ConstraintLayout, ImageCellData>()

private const val LOGGING_TAG = "AdminMatic"

private var mode: String = "GALLERY"
private var existingImages: Array<Image> = arrayOf()
private var customerID: String = ""
private var customerName: String = ""
private var woID: String = ""
private var woItemID: String = ""
private var contractItemID: String = ""
private var leadID: String = ""
private var taskID: String = ""
private var taskStatus: String = ""
private var taskDescription: String = ""
private var employeeID: String = ""
private var equipmentID: String = ""
private var usageID: String = ""
private var uncompressed = "0"

private var customerAllowImages: Boolean = true
private var queriedCustomer: Customer? = null

private lateinit var pickMedia:ActivityResultLauncher<PickVisualMediaRequest>

private var singleSelect = false

/*
private val mRetryPolicy: RetryPolicy = DefaultRetryPolicy(
    0,
    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
)

 */


//val REQUEST_CODE = 200
//private val cameraRequest = 1888


class ImageUploadFragment : Fragment(), CustomerCellClickListener {

    //private var mode: String? = null
    //private var customer: Customer? = null
    //lateinit var  pgsBar: ProgressBar
   // private lateinit var imageView: ImageView

    private var cameraInitialed = false

    lateinit var globalVars:GlobalVars

    private var filesToDelete:MutableList<File> = mutableListOf()

    private lateinit var cameraProvider: ProcessCameraProvider

    private var imageCapture: ImageCapture? = null

    private lateinit var currentPhotoPath: String

    // private var imageData: ByteArray? = null
    private val postURL: String = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/image.php"
   // private var selectedCustId: String? = null
    //var currentCameraUri :Uri? = null
    //var selectedUris: MutableList<Uri> = mutableListOf()
    var uploadedImageCount = 0

    private lateinit var cameraExecutor: ExecutorService


    // var albumID: String = ""


    lateinit var customerRecyclerView: RecyclerView
    private lateinit var customerSearchView:androidx.appcompat.widget.SearchView
    lateinit var adapter:CustomersAdapter


    lateinit var descriptionTxt:EditText

   // private var callBack:ImageUploadInterface? = null


    private lateinit var permissionsDelegate: PermissionsDelegate

    private var permissionsGranted: Boolean = false
    //private var activeCamera: Camera = Camera()

    //private lateinit var cameraZoom: Zoom.VariableZoom

    //private var curZoom: Float = 0f

    private lateinit var captureBtn: Button

    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted ->
        println("isGranted = $isGranted")
        if (isGranted) {
            startCamera()
        } else {
            // PERMISSION NOT GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            println("onCreate")
            mode = it.getString("mode")!!


            existingImages = it.getParcelableArray("images")!! as Array<Image>
            customerID = it.getString("customerID")!!
            customerName = it.getString("customerName")!!
            woID = it.getString("woID")!!
            woItemID = it.getString("woItemID")!!
            contractItemID = it.getString("contractItemID")!!
            leadID = it.getString("leadID")!!
            taskID = it.getString("taskID")!!
            taskDescription = it.getString("taskDescription")!!
            taskStatus = it.getString("taskStatus")!!
            employeeID = it.getString("employeeID")!!
            equipmentID = it.getString("equipmentID")!!
            usageID = it.getString("usageID")!!

            println("images.count = ${existingImages.count()}")

        }


        if (mode == "EQUIPMENT") {
            singleSelect = true
        }

        if (singleSelect) {
            pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
                if (uri != null) {
                    createImageCell(ImageCellData(uri, null, true))
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        }
        else {
            pickMedia = registerForActivityResult(
                PickMultipleVisualMedia(20)
            ) { uris ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uris.isNotEmpty()) {
                    Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                    for (currentUri in uris) {
                        createImageCell(ImageCellData(currentUri, null, true))
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        }




        cameraExecutor = Executors.newSingleThreadExecutor()


    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private var _binding: FragmentImageUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentImageUploadBinding.inflate(inflater, container, false)
        myView = binding.root


        println("onCreateView")
        customerSearchView = myView.findViewById(R.id.image_upload_prep_customer_search)
        customerSearchView.setQuery(customerName, false)
        adapter = CustomersAdapter(GlobalVars.customerList!!, this, false)
        globalVars = GlobalVars()


        customerRecyclerView = myView.findViewById(R.id.customer_search_rv)
        customerRecyclerView.visibility = View.GONE

        descriptionTxt = myView.findViewById(R.id.image_upload_task_edit_txt)

        if (taskDescription != ""){
            descriptionTxt.text = Editable.Factory.getInstance().newEditable(taskDescription)
        }


      //  return inflater.inflate(R.layout.fragment_image_upload, container, false)

        return myView
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (customerID != "") {
            showProgressView()
            getAllowImages(customerID, false)
        }

        println("onViewCreated mode = $mode")
            var titleText = ""
        when (mode) {
            "GALLERY" -> {
                titleText = "Upload to Gallery"
                binding.imageUploadTaskEditTxt.visibility = View.GONE

                /*
                val params = binding.customerSearchRv.layoutParams as ConstraintLayout.LayoutParams
                params.bottomToBottom = binding.imageUploadPrepHeaderCl.id
                binding.customerSearchRv.requestLayout()
                 */

                binding.imageUploadPrepCustomerSearch.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToBottom = binding.imageUploadPrepHeaderCl.id
                }

                /*
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.frameLayout6)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                //constraintSet.connect(binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, binding.customerSearchRv.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.frameLayout6)

                 */

            }
            "CUSTOMER" -> {
                println("customer.ID = $customerID")
                titleText = "Upload to Customer"
            }
            "WO" -> {
                titleText = "Upload to W.O."
            }
            "WOITEM" -> {
                titleText = "Add Receipt Image"
                binding.imageUploadTaskEditTxt.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.imageUploadPrepHeaderCl)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.imageUploadPrepHeaderCl)
            }
            "LEADTASK" -> {
                titleText = "Upload to Lead Task"
            }
            "CONTRACTTASK" -> {
                titleText = "Upload to Contract Task"
            }
            "TASK" -> {
                titleText = "Upload to Task"
            }
            "EMPLOYEE" -> {
                titleText = "Upload to Employee"
            }
            "EQUIPMENT" -> {
                titleText = "Upload to Equipment"
                binding.imageUploadTaskEditTxt.visibility = View.GONE
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.imageUploadPrepHeaderCl)
                constraintSet.connect(binding.customerSearchRv.id, ConstraintSet.BOTTOM, binding.imageUploadPrepHeaderCl.id, ConstraintSet.BOTTOM, 0)
                constraintSet.applyTo(binding.imageUploadPrepHeaderCl)
            }

        }

        if (mode != "GALLERY") {
            customerSearchView.visibility = View.GONE
        }

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = titleText

        descriptionTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                taskDescription = descriptionTxt.text.toString()
            }
        })


        customerRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)

            if(GlobalVars.customerList != null) {
                adapter = activity?.let {


                    CustomersAdapter(
                        GlobalVars.customerList!!,
                        this@ImageUploadFragment,
                    false
                    )


                }

                val itemDecoration: RecyclerView.ItemDecoration =
                    DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                customerRecyclerView.addItemDecoration(itemDecoration)



                //(adapter as CustomersAdapter).notifyDataSetChanged()

                // Remember to CLEAR OUT old items before appending in the new ones


                //search listener
                customerSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        //customerRecyclerView.visibility = View.GONE
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("onQueryTextChange = $newText")
                        (adapter as CustomersAdapter).filter.filter(newText)
                        if(newText == ""){
                            customerRecyclerView.visibility = View.GONE
                            customerID = ""
                        }else{
                            customerRecyclerView.visibility = View.VISIBLE
                        }

                        return false
                    }

                })

            }
        }


        permissionsDelegate = PermissionsDelegate((activity as MainActivity?)!!)

        binding.cameraBtn.setOnClickListener{
            println("camera btn clicked")

            if (mode == "EQUIPMENT" && existingImages.isNotEmpty()) {
                globalVars.simpleAlert(myView.context, getString(R.string.dialogue_error), getString(R.string.equipment_one_image))
            }
            else {
                launchCamera()
            }
        }



        captureBtn = view.findViewById(R.id.capture_btn)
        captureBtn.setOnClickListener{
            println("capture btn clicked")

            takePhoto()
        }


        binding.galleryBtn.setOnClickListener{
            println("gallery btn clicked")

            launchGallery()
        }

        binding.uncompressedSwitch.setOnCheckedChangeListener { _, isChecked ->
            uncompressed = if (isChecked) {
                "1"
            } else {
                "0"
            }
        }

        binding.submitImagesBtn.setOnClickListener{
            println("submit btn clicked")

            if (!customerAllowImages && mode != "WOITEM") {
                globalVars.simpleAlert(myView.context,getString(R.string.no_image_collection),getString(R.string.no_image_collection_error))
                return@setOnClickListener
            }


            when (mode) {
                "GALLERY" -> {
                    uploadImage()
                }
                "CUSTOMER" -> {
                    uploadImage()
                }
                "WO" -> {

                }
                "WOITEM" -> {
                    uploadImage()
                }
                "LEADTASK" -> {
                    saveLeadTask()
                }
                "CONTRACTTASK" -> {
                    saveContractTask()
                }
                "TASK" -> {
                    saveTask()
                }
                "EMPLOYEE" -> {

                }
                "EQUIPMENT" -> {
                    uploadImage()
                }

            }


           //uploadImage()

        }


        imageCellMap.clear()
        for (img in existingImages) {
            createImageCell(ImageCellData(null, img, false))
        }

    }


    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageUpload")
    }


    private fun launchCamera(){
        println("launchCamera")
        permissionsGranted = permissionsDelegate.hasCameraPermission()

        if (permissionsGranted) {

            startCamera()

        } else {
            //permissionsDelegate.requestCameraPermission()
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }

    private fun startCamera() {
        println("Start Camera")

        if (cameraInitialed) {
            binding.cameraCl.visibility = View.VISIBLE
        }
        else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(myView.context)

            cameraProviderFuture.addListener({
                // Used to bind the lifecycle of cameras to the lifecycle owner
                cameraProvider = cameraProviderFuture.get()


                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder().build()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)

                } catch(exc: Exception) {
                    //Log.e(TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(myView.context))

            binding.cameraCl.visibility = View.VISIBLE
            cameraInitialed = true
        }
    }


    private fun createImageCell(imageData:ImageCellData) {
        val cl = LayoutInflater.from(this.context).inflate(R.layout.image_upload_image_list_item, binding.imageUploadPrepSelectedImagesLl, false) as ConstraintLayout
        //cl.id = imageCellDataList.count()
        //imageCellDataList.add(imageData)
        imageCellMap[cl] = imageData

        val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
        pb.visibility = View.INVISIBLE

        val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
        tv.visibility = View.INVISIBLE

        val et = cl.findViewById(R.id.description_et) as EditText

        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                imageCellMap[cl]!!.toBeSaved = true
                if (imageCellMap[cl]!!.imageData != null) {
                    imageCellMap[cl]!!.imageData!!.description = s.toString()
                }
            }
        })

        if (imageData.imageData != null) {
            et.setText(imageData.imageData!!.description)
        }

        val deleteBtn = cl.findViewById(R.id.image_upload_image_item_delete_btn) as Button
        //deleteBtn.visibility = View.INVISIBLE
        //deleteBtn.alpha = 0.25f

        deleteBtn.setOnClickListener {
            println("delete")

            val imageDataToDelete = imageCellMap[cl]


            if (imageDataToDelete!!.imageData == null) {
                imageCellMap.remove(cl)
                binding.imageUploadPrepSelectedImagesLl.removeView(cl)
            }
            else {

                val builder = androidx.appcompat.app.AlertDialog.Builder(myView.context)
                builder.setTitle(getString(R.string.delete_image_link_title))
                builder.setMessage(getString(R.string.delete_image_link_body))

                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    deleteImage(cl)
                }

                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }

                builder.show()


            }
        }

        binding.imageUploadPrepSelectedImagesLl.addView(cl)

        println("cl.findViewById(R.id.image_upload_image_item_iv) = ${cl.findViewById(R.id.image_upload_image_item_iv) as ImageView}")

        if (imageData.uri != null) {
            println("uri isn't null")
            println("uri = ${imageData.uri}")
            Glide.with(this).load(imageData.uri).into(cl.findViewById(R.id.image_upload_image_item_iv) as ImageView)
        }
        else {
            Picasso.with(context)
                .load(GlobalVars.thumbBase + imageData.imageData?.fileName)
                .placeholder(R.drawable.ic_images) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(cl.findViewById(R.id.image_upload_image_item_iv) as ImageView)
        }



        //cl.requestFocus()
        // var image_upload_prep_selected_images_sv = cl.findViewById(R.id.image_upload_prep_selected_images_sv) as ScrollView
        binding.imageUploadPrepSelectedImagesSv.fullScroll(View.FOCUS_DOWN)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(myView.context), object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                //get bitmap from image

                val planeProxy = image.planes[0]
                val buffer: ByteBuffer = planeProxy.buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                bitmap = rotate(bitmap, image.imageInfo.rotationDegrees.toFloat())


                val file = File.createTempFile("tempImage", null, myView.context.cacheDir)


                val output = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)

                filesToDelete.add(file)

                createImageCell(ImageCellData(file.toUri(), null, true))

                super.onCaptureSuccess(image)
                binding.cameraCl.visibility = View.GONE
            }

            override fun onError(exception: ImageCaptureException) {

                println("IMAGE CAPTURE ERROR")

                super.onError(exception)
                binding.cameraCl.visibility = View.GONE
            }

        })

    }


    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("onRequestPermissionsResult")
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            permissionsGranted = true
        }
    }


    private fun launchGallery() {
        println("launchGallery")

        // Include only one of the following calls to launch(), depending on the types
        // of media that you want to let the user choose from.

        // Launch the photo picker and let the user choose images and videos.

        // Include only one of the following calls to launch(), depending on the types
        // of media that you want to let the user choose from.

        // Launch the photo picker and let the user choose only images.

        if (singleSelect) {

            if (imageCellMap.size == 0) {
                pickMedia.launch(
                    PickVisualMediaRequest.Builder()
                        .setMediaType(ImageOnly)
                        .build()
                )
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.dialogue_only_one_image))
            }
        }
        else {
            pickMedia.launch(PickVisualMediaRequest(ImageOnly))
        }


    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri): ByteArray? {

        var imageData:ByteArray?
        val inputStream = ((activity as AppCompatActivity).contentResolver.openInputStream(uri))

        val bMap = BitmapFactory.decodeStream(inputStream!!)


        val inputStreamExif = ((activity as AppCompatActivity).contentResolver.openInputStream(uri))
        val ei = ExifInterface(inputStreamExif!!)
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotatedBitmap: Bitmap = when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> {
                bMap
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotate(bMap, 90f)
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotate(bMap, 180f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotate(bMap, 270f)
            }

            else -> bMap
        }

        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> {
                println("Normal Rotation")
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                println("Rotating 90 degrees")
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                println("Rotating 180 degrees")

            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                println("Rotating 270 degrees")
            }
            else -> {
                println("Undefined rotation")
            }
        }

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        imageData = outputStream.toByteArray()

        inputStream.close()
        inputStreamExif.close()
        outputStream.close()

        return imageData
    }

    private fun deleteImage(cl: ConstraintLayout){
        //Lead Task

        println("save task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/imageLink.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        imageCellMap.remove(cl)
                        binding.imageUploadPrepSelectedImagesLl.removeView(cl)
                    }
                    hideProgressView()

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
                params["type"] = "1"
                params["ID"] = taskID
                params["imageID"] = imageCellMap[cl]!!.imageData!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveLeadTask() {
    println("Save Lead Task")
    showProgressView()

    var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/leadTask.php"
    val currentTimestamp = System.currentTimeMillis()
    println("urlString = ${"$urlString?cb=$currentTimestamp"}")
    urlString = "$urlString?cb=$currentTimestamp"
    val postRequest1: StringRequest = object : StringRequest(
        POST, urlString,
        Response.Listener { response -> // response
            println("Response $response")
            try {
                val parentObject = JSONObject(response)
                println("parentObject = $parentObject")
                if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                    val gson = GsonBuilder().create()


                    taskID = gson.fromJson(parentObject["leadTaskID"].toString() , String::class.java)

                    // If the lead tasks has no images, just pop back now
                    if (imageCellMap.isNotEmpty()) {
                        uploadImage()
                    }
                    else {
                        globalVars.playSaveSound(myView.context)
                        myView.findNavController().navigateUp()
                    }
                }


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
            params["leadID"] = leadID
            params["taskID"] = taskID
            params["taskDescription"] = taskDescription
            params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
            println("params = $params")
            return params
        }
    }
    postRequest1.tag = "imageUpload"
    VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveTask() {
        println("Save Task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/task.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()


                        taskID = gson.fromJson(parentObject["newID"].toString() , String::class.java)

                        // If the lead tasks has no images, just pop back now
                        if (imageCellMap.isNotEmpty()) {
                            uploadImage()
                        }
                        else {
                            globalVars.playSaveSound(myView.context)
                            myView.findNavController().navigateUp()
                        }
                    }


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
                if (taskID.isBlank() || taskID == "0") {
                    params["ID"] = "0"
                    params["status"] = "1"
                }
                else {
                    params["ID"] = taskID
                    params["status"] = taskStatus
                }
                params["task"] = taskDescription
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["woItemID"] = woItemID
                params["woID"] = woID


                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun saveContractTask() {
        println("Save Contract Task")
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/contractTask.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()


                        taskID = gson.fromJson(parentObject["taskID"].toString() , String::class.java)

                        setFragmentResult("contractTaskItemListener", bundleOf("shouldRefreshContractItem" to true))

                        // If the lead tasks has no images, just pop back now
                        if (imageCellMap.isNotEmpty()) {
                            uploadImage()
                        }
                        else {
                            globalVars.playSaveSound(myView.context)
                            myView.findNavController().navigateUp()
                        }
                    }


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
                if (taskID.isBlank() || taskID == "0") {
                    params["taskID"] = "0"
                }
                else {
                    params["taskID"] = taskID
                }
                params["contractItemID"] = contractItemID
                params["taskDescription"] = taskDescription
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID

                /*
                "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!,
                "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                "taskID":"0",
                "contractItemID":self.contractItemID,
                "taskDescription": groupDescString as AnyObject,
                "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId) as AnyObject]

                "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!,
                "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
                "taskID":self.contractTaskID,
                "contractItemID":self.contractItemID,
                "taskDescription": groupDescString as AnyObject,
                "createdBy": self.appDelegate.defaults.string(forKey: loggedInKeys.loggedInId) as AnyObject]
                */

                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun uploadImage() {
        uploadedImageCount = 0
        uploadUri(0)
    }

    private fun uploadUri(index:Int) {
        println("uploadUri")
        val clList = mutableListOf<ConstraintLayout>()
        for (entry in imageCellMap) {
            clList.add(entry.key)
        }

        val uri:Uri? = imageCellMap[clList[index]]!!.uri

        if (uri == null || !imageCellMap[clList[index]]!!.toBeSaved) {
            println("uri was either null or not marked to be uploaded")

            if (imageCellMap[clList[index]]!!.imageData != null) {
                // try the imagedata instead (will be updated if description was changed)
                updateExistingImage(index)
            }
            else {

                uploadedImageCount += 1

                if (uploadedImageCount == imageCellMap.count()) {
                    //upload complete

                    globalVars.playSaveSound(myView.context)
                    back()
                } else {
                    uploadUri(index + 1)
                }
            }
        }
        else {

            println("uploading an uri with index $index")
            //uri.path?.let { handleRotation(uri) }
            println("views = ${binding.imageUploadPrepSelectedImagesLl.childCount}")

            for (cl in binding.imageUploadPrepSelectedImagesLl.children) {
                println(cl.id)
            }

            val cl = clList[index]
            val iv = cl.findViewById(R.id.image_upload_image_item_iv) as ImageView
            val pb = cl.findViewById(R.id.image_upload_image_item_pb) as ProgressBar
            val tv = cl.findViewById(R.id.image_upload_image_item_tv) as TextView
            val et = cl.findViewById(R.id.description_et) as EditText
            println("pb = $pb")
            iv.alpha = 0.5F
            pb.visibility = View.VISIBLE


            //create request
            val request = object : VolleyFileUploadRequest(
                POST,
                postURL,
                Response.Listener {

                    println("got image request back")

                    println("response is: ${it.headers}")
                    println("data is:${it.data}")

                    pb.visibility = View.INVISIBLE
                    tv.visibility = View.VISIBLE

                    uploadedImageCount += 1

                    if (uploadedImageCount == imageCellMap.count()) {
                        //upload complete
                        globalVars.playSaveSound(myView.context)
                        back()
                    } else {
                        uploadUri(index + 1)
                    }

                    //hideProgressView()
                    //back()
                },
                Response.ErrorListener {
                    //hideProgressView()
                    println("error is: $it")
                }

            ) {
                override fun getByteData(): MutableMap<String, FileDataPart> {
                    println("getByteData")
                    val params = HashMap<String, FileDataPart>()
                    // createImageData(uri)
                    params["pic"] = FileDataPart("droid_file.jpeg", createImageData(uri!!)!!, "image/jpeg")
                    return params
                }


                override fun getParams(): Map<String, String> {
                    println("getParams")

                    val params: MutableMap<String, String> = HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["name"] = "$mode Image"
                    params["desc"] = et.text.trim().toString()
                    params["tags"] = ""
                    params["noCompress"] = uncompressed
                    if (customerID != "") {
                        params["customer"] = customerID
                    }
                    params["createdBy"] = GlobalVars.loggedInEmployee!!.ID

                    when (mode) {
                        "LEADTASK" -> {
                            params["leadTask"] = taskID
                            params["contractTask"] = ""
                            params["task"] = ""
                        }

                        "CONTRACTTASK" -> {
                            params["leadTask"] = ""
                            params["contractTask"] = taskID
                            params["task"] = ""
                        }

                        "EQUIPMENT" -> {
                            params[equipmentID] = equipmentID
                            params["leadTask"] = ""
                            params["contractTask"] = ""
                        }

                        else -> {
                            params["leadTask"] = ""
                            params["contractTask"] = ""
                            params["task"] = taskID
                        }
                    }


                    params["woID"] = woID

                    if (mode == "WOITEM") {
                        params["usageID"] = usageID
                        params["name"] = "Receipt Image"
                    }


                    params["equipmentID"] = equipmentID
                    //params["albumID"] = albumID
                    //params["usageID"] = usageID
                    //params["vendorID"] = vendorID
                    //params["strikeID"] = strikeID
                    println("params is: $params")
                    return params
                }

            }

            request.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            //send request
            request.tag = "imageUpload"
            VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cache.clear()
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(request)

        }
    }

    private fun updateExistingImage(index:Int) {
        println("updateExistingImage")
        val clList = mutableListOf<ConstraintLayout>()
        for (entry in imageCellMap) {
            clList.add(entry.key)
        }

        val image:Image? = imageCellMap[clList[index]]!!.imageData

        if (image == null) {
            uploadedImageCount += 1

            if (uploadedImageCount == imageCellMap.count()) {
                //upload complete
                globalVars.playSaveSound(myView.context)
                back()
            } else {
                uploadUri(index + 1)
            }
        }

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/image.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response

                println("update image response: $response")

                try {
                    val parentObject = JSONObject(response)
                    //println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        uploadedImageCount += 1
                        if (uploadedImageCount == imageCellMap.count()) {
                            //upload complete
                            globalVars.playSaveSound(myView.context)
                            back()
                        } else {
                            uploadUri(index + 1)
                        }
                    }

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
                params["ID"] = image!!.ID
                params["createdBy"] = image.createdBy?:""
                params["equipmentID"] = image.equipmentID?:""
                params["task"] = image.taskID?:""
                params["name"] = image.name
                params["desc"] = image.description!!.trim()
                params["customer"] = image.customer?:""
                params["vendor"] = image.vendorID?:""
                params["tags"] = image.tags?:""
                params["noCompress"] = image.noCompress?:""
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("update image params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    private fun getAllowImages(newCustomerID:String, selectedFromRecycler:Boolean) {

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/customer.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    val customerArray = gson.fromJson(parentObject.toString() ,CustomerArray::class.java)

                    val customer = customerArray.customers[0]

                    if (customer.allowImages == "0") {
                        customerAllowImages = false
                    }

                    if (selectedFromRecycler) {
                        if (customerAllowImages) {
                            customerID = newCustomerID
                            customerSearchView.setQuery(queriedCustomer!!.sysname, false)
                            customerRecyclerView.visibility = View.GONE

                            //val imm = (activity as MainActivity?).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            //imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
                            hideSoftKeyboard((activity as MainActivity?)!!)


                            println("selcted cust = ${queriedCustomer!!.ID}")
                        }
                        else {
                            globalVars.simpleAlert(myView.context,getString(R.string.no_image_collection),getString(R.string.no_image_collection_error))
                            customerAllowImages = true
                            customerID = ""
                            customerSearchView.setQuery("", false)
                            customerRecyclerView.visibility = View.GONE
                        }
                    }
                    hideProgressView()

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
                //params["ID"] = customer!!.ID
                params["ID"] = newCustomerID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageUpload"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    override fun onCustomerCellClickListener(data:Customer) {
        println("Cell clicked with customer: ${data.sysname}")
        data.let {
            // val directions = CustomerListFragmentDirections.navigateToCustomer(data)
            queriedCustomer = it
            getAllowImages(it.ID, true)
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        if (activity.currentFocus == null){
            return
        }
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }


    fun back(){
        println("back")

        println("filesToDelete size: ${filesToDelete.size}")
        for (f in filesToDelete) {
            println("checking a file to delete...")
            if (f.delete()) {
                println("successfully deleted temp file ${f.absolutePath}")
            }
            else {
                println("failed to delete temp file ${f.absolutePath}")
            }
        }

        cameraProvider.unbindAll()

        // Flag to refresh new/edit equipment if mode is equipment
        setFragmentResult("_refreshEquipment", bundleOf("_refreshEquipment" to (mode == "EQUIPMENT")))
        setFragmentResult("_refreshImages", bundleOf("_refreshImages" to true))

        //requireActivity().supportFragmentManager.popBackStack()
        myView.findNavController().navigateUp()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}