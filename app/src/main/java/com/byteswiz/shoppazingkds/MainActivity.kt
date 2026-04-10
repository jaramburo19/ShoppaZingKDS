package com.byteswiz.shoppazingkds

import android.app.Activity
import android.app.ComponentCaller
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.media.MediaPlayer
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.CompleteOrderRequest
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.dataadapters.ParentAdapter
import com.byteswiz.shoppazingkds.databinding.ActivityMainBinding
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.livedata.CartViewModel
import com.byteswiz.shoppazingkds.network.KDSServerManager
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_PREPARING
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_READY
import com.byteswiz.shoppazingkds.utils.makeSound
import com.byteswiz.shoppazingkds.utils.makeStatusNotification
import com.byteswiz.shoppazingkds.viewmodels.SyncViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.paperdb.Paper
import kotlinx.coroutines.*
import java.io.*
import java.lang.Runnable
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import java.net.NetworkInterface
import java.net.Inet4Address



class MainActivity : AppCompatActivity(),CoroutineScope {
    //lateinit var recyclerView: RecyclerView

    companion object{
        var TAG="MainActivity"
    }
    /*var serverSocket: ServerSocket? = null
    var Thread1: Thread? = null
    var Thread2: Thread? = null
    var Thread3: Thread? = null
    var Thread4: Thread? = null*/

    var IsKDSConnected: Boolean=false
    //private var outputStreamKDS:OutputStream?=null
    private var objectOutputStreamKDS:ObjectOutputStream?=null
    var ThreadKDSComplete: Thread? = null

   /* var tvIP: TextView? = null
    var tvPort: TextView? = null*/
    lateinit var tvMessages: TextView
    lateinit var etMessage: EditText
    lateinit var btnSend: Button
    //lateinit var tvConnectionStatus: TextView
    lateinit var txtTotal: TextView
    var SERVER_IP = ""
    val SERVER_PORT = 8081


    //lateinit var adapter: ShoppingCartAdapter

    lateinit var _adapter: ParentAdapter
    //lateinit var parentDataFactory: ParentDataFactory


    private lateinit var viewModel: CartViewModel


    lateinit var syncViewModel: SyncViewModel

    /*fun main() = runBlocking<Unit> { // start main coroutine
        val job = GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
            delay(1000L)
            println("World!")
        }
        println("Hello,")
        job.join() // wait until child coroutine completes
    }*/

    // Put this at the top of MainActivity
    private val kdsServerManager = KDSServerManager()


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + jobPostStatus

    private var jobPostStatus: Job = Job()

    private fun startPostUpdateCoroutineSync() = launch(Dispatchers.IO) {

        do {

            withContext(Dispatchers.IO) {
                startMyOrdersSync()

            }
            delay(5000) //TODO: Change this to every 1 minute in Go-LIVE currently 30 seconds
            // num++
        }  while (true)
    }

    suspend fun startMyOrdersSync() =
        coroutineScope {
            val deferredOne = async { syncViewModel.StartItemSync()}
            deferredOne.await()

        }

    var mMediaPlayer: MediaPlayer? = null
    // 1. Plays the water sound
    fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.possitive)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    // 3. Stops playback
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)
        setContentView( binding.root)

        syncViewModel =SyncViewModel(application)

        jobPostStatus = startPostUpdateCoroutineSync()

        this.title = "ShoppaZing Kitchen Display"

        Paper.init(this)

        //tvIP = findViewById(R.id.tvIP)
        //tvPort = findViewById(R.id.tvPort)
        //tvConnectionStatus = findViewById(R.id.tvConnectionStatus)


        try {
            SERVER_IP = getLocalIpAddress()
            // ... (your other init code) ...

            // 1. Fetch the IP immediately
            SERVER_IP = getLocalIpAddress()

            // 2. Force the UI to show it right now
            binding.tvIP.text = "IP: $SERVER_IP"
            binding.nodata.txtIPAddress.text = "IP: $SERVER_IP"

        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }


        setupAdapters()

        /*viewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        viewModel.ItemsMutableLiveData.value = ShoppingCart.getOrders()
        viewModel.ItemsMutableLiveData.observe(this, ordersObserver())*/

        initRecycler()

        setupKDSClientManager()


        //TEMPORARY DELETE THIS
       // Thread1 = Thread(thread1())
       // Thread1!!.start()

         /*Thread1 = Thread(threadStart())
         Thread1!!.start()*/


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;


    }

    fun setupKDSClientManager(){

        // Put this inside onCreate(), replacing Thread1 = Thread(threadStart()).start()
        lifecycleScope.launch {
            kdsServerManager.serverState.collect { state ->
                binding.tvConnectionStatus.text = state
                if (state == "Connected") {
                    binding.tvConnectionStatus.setTextColor(Color.GREEN)
                } else {
                    binding.tvConnectionStatus.setTextColor(Color.RED)
                }
            }
        }

        lifecycleScope.launch {
            kdsServerManager.incomingOrders.collect { newOrder ->
                addNewOrder(newOrder)
                playSound()
            }
        }

        // Start the server socket in the background
        lifecycleScope.launch {
            kdsServerManager.startServer()
        }
    }

    /*val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())*/

    /*fun setupAdapters(){

        executor.execute {
            var currentOrders =ShoppingCart.getOrders(this@MainActivity)

            handler.post {
                _adapter = ParentAdapter(this,this,currentOrders, object : OnParentButtonClicked {
                    override fun onPreparingClicked(
                        receiptNo: String,
                        localUniqueId: String,
                        position: Int,
                        isLongClick: Boolean
                    ) {
                        //Toast.makeText(this@MainActivity,"Preparing Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
                        //val currentOrders =ShoppingCart.getOrders().filter { it.orderStatusId != ORDER_STATUS_READY }.toMutableList()
                        updateOrderStatusExecute(localUniqueId,ORDER_STATUS_PREPARING,position)

                        //ShoppingCart.updateStatus(ORDER_STATUS_PREPARING, localUniqueId,this@MainActivity)
                        //_adapter.notifyDataSetChanged()
                        if(isLongClick)
                            Toast.makeText(this@MainActivity,"Order preparing...", Toast.LENGTH_SHORT).show()
                        stopSound()

                    }

                    override fun onCompletedClicked(receiptNo: String, localUniqueId: String, position: Int, orderRefNo:String, textDuration:String) {
                        ConfirmComplete(receiptNo,localUniqueId,position,orderRefNo,textDuration)

                        //Toast.makeText(this@MainActivity,"Completed Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
                    }

                    override fun onRecallClicked(receiptNo: String, localUniqueId: String) {

                    }

                    override fun onChildItemClicked(
                        parentModelId: Int,
                        itemId: Long,
                        flag: Boolean
                    ) {
                        updateChildOrderStatusExecute(parentModelId,itemId,flag)
                    }


                })
            }
        }
    }*/


    fun setupAdapters() {
        // Initialize synchronously on the Main Thread with an empty list
        _adapter = ParentAdapter(this, this, mutableListOf(), object : OnParentButtonClicked {

            override fun onPreparingClicked(receiptNo: String, localUniqueId: String, position: Int, isLongClick: Boolean) {
                updateOrderStatusExecute(localUniqueId, ORDER_STATUS_PREPARING, position)
                if (isLongClick) Toast.makeText(this@MainActivity, "Order preparing...", Toast.LENGTH_SHORT).show()
                stopSound()
            }

            override fun onCompletedClicked(receiptNo: String, localUniqueId: String, position: Int, orderRefNo: String, textDuration: String) {
                ConfirmComplete(receiptNo, localUniqueId, position, orderRefNo, textDuration)
            }

            override fun onRecallClicked(receiptNo: String, localUniqueId: String) {}

            override fun onChildItemClicked(parentModelId: Int, itemId: Long, flag: Boolean) {
                updateChildOrderStatusExecute(parentModelId, itemId, flag)
            }
        })
    }

    fun updateChildOrderStatusExecute(parentModelId: Int, itemId: Long, flag: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            ShoppingCart.updateChildItemStatus(parentModelId, itemId, flag, this@MainActivity)
        }
    }
    /*

    fun updateChildOrderStatusExecute(parentModelId:Int,itemId: Long, flag:Boolean){

        executor.execute {

            ShoppingCart.updateChildItemStatus(parentModelId,itemId,flag, this@MainActivity)
            handler.post {
                //finish()
            }
        }


    }*/


   /* fun refreshOrders(){


        executor.execute {
            val currentOrders =ShoppingCart.getOrders(this)

            handler.post {
                _adapter.setOrders(currentOrders)
                _adapter.notifyDataSetChanged()

                if(isInitialLoad){
                    isInitialLoad=false
                    binding.recyclerView.adapter = _adapter
                }


                ShowHideNodata(currentOrders)

            }
        }
    }*/

    fun refreshOrders() {
        lifecycleScope.launch {
            // Fetch data in background
            val currentOrders = withContext(Dispatchers.IO) {
                ShoppingCart.getOrders(this@MainActivity)
            }

            // Push data to the already-existing adapter
            _adapter.setOrders(currentOrders)
            _adapter.notifyDataSetChanged()

            ShowHideNodata(currentOrders)
        }
    }
    /*
    fun updateOrderStatusExecute(localUniqueId:String, orderStatusId: Int, position:Int){
        if(orderStatusId== ORDER_STATUS_PREPARING)
            _adapter.notifyChanged(position, orderStatusId)
        else if(orderStatusId== ORDER_STATUS_READY)
            _adapter.removeAt(position)

        executor.execute {

            ShoppingCart.updateStatus(orderStatusId,localUniqueId, this@MainActivity)
            val currentOrders =ShoppingCart.getOrders(this@MainActivity)

            handler.post {


                //_adapter.setOrders(currentOrders)
                stopSound()
                ShowHideNodata(currentOrders)
            }
        }


    }

    fun updateSyncStatusExecute(localUniqueId:String, isSynced: Boolean){

        executor.execute {
            ShoppingCart.updateSyncStatus(false, localUniqueId,this@MainActivity)

            handler.post {

            }
        }
    }*/

    fun updateOrderStatusExecute(localUniqueId: String, orderStatusId: Int, position: Int) {
        if (orderStatusId == ORDER_STATUS_PREPARING) _adapter.notifyChanged(position, orderStatusId)
        else if (orderStatusId == ORDER_STATUS_READY) _adapter.removeAt(position)

        lifecycleScope.launch {
            val currentOrders = withContext(Dispatchers.IO) {
                ShoppingCart.updateStatus(orderStatusId, localUniqueId, this@MainActivity)
                ShoppingCart.getOrders(this@MainActivity)
            }

            stopSound()
            ShowHideNodata(currentOrders)
        }
    }

    fun updateSyncStatusExecute(localUniqueId: String, isSynced: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            ShoppingCart.updateSyncStatus(false, localUniqueId, this@MainActivity)
        }
    }

    fun sendtoCounterCompleteExecute(localUniqueId: String, textDuration: String) {
        lifecycleScope.launch {
            val orderItem = withContext(Dispatchers.IO) {
                ShoppingCart.getOrderByLocalUID(localUniqueId, this@MainActivity)
            }

            if (orderItem != null) {
                val request = CompleteOrderRequest(orderItem, ORDER_STATUS_READY, textDuration)
                kdsServerManager.sendOrderComplete(request)
            }
        }
    }

    fun addNewOrder(order: ParentModel) {
        lifecycleScope.launch {
            val parentModel = withContext(Dispatchers.IO) {
                val newOrderId = ShoppingCart.addOrder(order, this@MainActivity)
                ShoppingCart.getOrderById(this@MainActivity, newOrderId.toInt())
            }

            if (parentModel != null) {
                _adapter.addOrder(parentModel)
                makeStatusNotification("New order arrived", this@MainActivity, "Alert!")
                makeSound(this@MainActivity)
                binding.recyclerView.visibility = View.VISIBLE
                binding.nodata.root.visibility = View.GONE
            }
        }
    }

    fun ConfirmComplete(receiptNo: String, localUniqueId: String, position: Int, orderRefNo:String, textDuration:String){
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Are you sure this order is complete? Order #:$orderRefNo")
        //builder.setIcon(R.drawable.shoppazing_logo)
        builder.setPositiveButton("Complete", DialogInterface.OnClickListener { dialog, id ->
            dialog.dismiss()
            updateOrderStatusExecute(localUniqueId,ORDER_STATUS_READY,position)
            //ShoppingCart.updateStatus(ORDER_STATUS_READY, localUniqueId,this)
            updateSyncStatusExecute(localUniqueId,false)

            //refreshOrders()
            stopSound()

            //sendtoCounterCompleteExecute(localUniqueId, textDuration)

        })
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() })
        val alert: android.app.AlertDialog = builder.create()
        alert.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var view = menuInflater.inflate(R.menu.toolbar_menu, menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
           /* android.R.id.home -> {
                finish()
                return true
            }*/
            R.id.action_recall -> {
                ShowRecall()
            }
            R.id.action_settings ->{
                ShowSettings()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun ShowRecall() {

        val intent = Intent(this, RecallActivity::class.java)
       // intent.putExtra("EXTRA_ORDER_STATUS", order.OrderStatusId.toString())

        startActivity(intent)
    }

    fun ShowSettings() {

        val intent = Intent(this, SettingsActivity::class.java)
        // intent.putExtra("EXTRA_ORDER_STATUS", order.OrderStatusId.toString())

        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()


        refreshOrders()
        checkNewAppVersionStateOnResume()

/*        var currentOrders =ShoppingCart.getOrders().filter { it.orderStatusId != ORDER_STATUS_READY }.toMutableList()
        ShowHideNodata(currentOrders)

        _adapter.setOrders(currentOrders)
        _adapter.notifyDataSetChanged()*/

    }

    private fun ShowHideNodata(currentOrders: MutableList<ParentModel>) {
        if (currentOrders.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.nodata.root.visibility = View.GONE

        } else {
            binding.nodata.txtNoDataTitle.text ="No Orders Available"
            binding.nodata.txtNoDataDesc.text="There are no orders to process yet. If you haven't set this up yet, kindly configure ShoppaZing POS \n and set Kitchen Display IP address to this value: $SERVER_IP. \n" +
                    "Once done, this device will start receiving Kitchen orders from ShoppaZing POS app."
            binding.recyclerView.visibility = View.GONE
            binding.nodata.root.visibility = View.VISIBLE
        }
    }


    /* private fun ordersObserver(): Observer<MutableList<ParentModel>?> {
         return Observer { orders ->
                 //_adapter.setOrders(getOrders())

         }
     }*/

    var isInitialLoad = true
    private fun initRecycler() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = _adapter // Safely assign the adapter right away
        }

        // Now trigger the background data fetch
        refreshOrders()
    }

    fun addNewOrderExecute(order:ParentModel){


    }
    /*fun addNewOrder(order: ParentModel){
        executor.execute {
            var newOrder = ShoppingCart.addOrder(order,this)
            var parentModel = ShoppingCart.getOrderById(this , newOrder.toInt())
            handler.post {
                _adapter.addOrder(parentModel)
                makeStatusNotification("New order arrived", this@MainActivity, "Alert!")
                makeSound(this@MainActivity)
                binding.recyclerView.visibility= View.VISIBLE
                binding.nodata.root.visibility=View.GONE
            }

        }
        //viewModel.ItemsMutableLiveData.value = getOrders()
    }*/


    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    private var objectInputStream: ObjectInputStream? =null
    private  var inputStream: InputStream? =null

    // private var scanner:Scanner?=null
    /*internal inner  class thread1 : Runnable {
        override fun run() {
            val socket: Socket
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket!!.setReuseAddress(true);
                runOnUiThread(Runnable {
                    binding.tvConnectionStatus!!.setText("Disconnected")
                    binding.tvConnectionStatus.setTextColor(Color.RED)
                    binding.tvIP!!.text = "IP: $SERVER_IP"
                    binding.tvPort!!.text = "Port: $SERVER_PORT"
                    binding.nodata.txtIPAddress.text ="IP: $SERVER_IP"
                })
                try {
                    socket = serverSocket!!.accept()
                    //scanner = Scanner(socket.getInputStream())
                    output = PrintWriter(socket.getOutputStream(), true)

                    // get the input stream from the connected socket

                    // get the input stream from the connected socket
                    inputStream = socket.getInputStream()
                    // create a DataInputStream so we can read data from it.
                    // create a DataInputStream so we can read data from it.
                    objectInputStream = ObjectInputStream(inputStream)

                    input = BufferedReader(InputStreamReader(socket.getInputStream()))

                    runOnUiThread(Runnable {
                        binding.tvConnectionStatus!!.setText("Connected ")
                        binding.tvConnectionStatus.setTextColor(Color.GREEN)

                    })
                    Thread2 = Thread(thread2())
                    Thread2!!.start()

                    con=true
                    *//* Thread4 = Thread(thread4())
                     Thread4!!.start()*//*

                    *//*while (scanner!!.hasNextLine()) {
                        var message:String = "${ scanner!!.nextLine() }"
                        runOnUiThread(Runnable {
                            tvMessages!!.append("client:" + message + "\n");
                        })
                    }
*//*
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/
    var con: Boolean=true
    /*internal inner class thread2 (): Runnable {
        override fun run() {
            *//* while (scanner!!.hasNextLine()) {
                 var message:String = "${ scanner!!.nextLine() }"
                 runOnUiThread(Runnable {
                     tvMessages!!.append("client:" + message + "\n");
                 })
             }*//*
            //var obj: Any?
            *//*while (objectInputStream!!.readObject().also { obj = it } !is EofIndicatorClass) {
                println(obj)
            }*//*



            //while (objectInputStream!!.readObject().also { obj = it } !is EofIndicatorClass) {
            while (con) {
                try {
                    //var message:String? = input!!.readLine();
                    // read the list of messages from the socket

                    // read the list of messages from the socket

                    // read the list of messages from the socket
                    // val listOfMessages=  objectInputStream!!.readObject()

                    *//* var listOfMessages = ArrayList<CartItemDisplay>()
                     listOfMessages = objectInputStream!!.readObject() as ArrayList<CartItemDisplay>*//*

                    var newOrder = objectInputStream!!.readObject()

                    //val listOfMessages =   objectInputStream!!.readObject() as List<CDMessage>
                    *//*val cartitms: ArrayList<CartItem> = ArrayList()
                    for(i in listOfMessages.iterator()){
                        cartitms.add(i)
                    }
                    viewModel!!.ItemsMutableLiveData.value =cartitms*//*

                    if (newOrder != null) {
                        newOrder = newOrder  as ParentModel
                        //var ss = message.substring(0, 5)
                        runOnUiThread(Runnable {
                            addNewOrder(newOrder)


                        })
                    } else {
                        con=false
                        serverSocket!!.close()
                        Thread1 = Thread(thread1())
                        Thread1!!.start()

                        return;
                    }

                }
                catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
                catch (e: EOFException) {
                    con=false
                    e.printStackTrace()
                    serverSocket!!.close()
                    Thread1 = Thread(thread1())
                    Thread1!!.start()
                }
                catch (e: IOException) {
                    e.printStackTrace()
                }


            }
        }
    }*/

    internal inner class threadSendToCounterComplete(private val order: ParentModel, val orderStatusId:Int, val textDuration:String) : java.lang.Runnable {
        override fun run() {
            try {

                if(IsKDSConnected){
                    objectOutputStreamKDS!!.writeObject(CompleteOrderRequest(order,orderStatusId,textDuration));
                    objectOutputStreamKDS!!.flush()
                    //ShoppingCart.clearKDSItems()
                    runOnUiThread(kotlinx.coroutines.Runnable {
                        //showToast(this@MainActivity, "Order sent to kds.")
                    })
                }
                else
                    runOnUiThread{
                        //showToast(this@MainActivity,"Disconnected.")
                        //PopUpConnectKDS()
                    }

            }
            catch (ex: java.lang.Exception){
                IsKDSConnected=false
                //Log.d(TAG, "")
                runOnUiThread{
                    //showToast(this@MainActivity,"Send to kds failed.")
                    //PopUpConnectKDS()
                }
            }

        }
    }


    internal inner class threadSendMessageToCounter(private val message: String) : Runnable {
        override fun run() {
            output!!.write(message + "\n")
            output!!.flush()

            runOnUiThread(Runnable {
                tvMessages!!.append("server: $message\n")
                etMessage!!.setText("")
            })
        }
    }

   /* internal inner class thread4 (): Runnable {
        override fun run() {
            *//* while (scanner!!.hasNextLine()) {
                 var message:String = "${ scanner!!.nextLine() }"
                 runOnUiThread(Runnable {
                     tvMessages!!.append("client:" + message + "\n");
                 })
             }*//*


            while (true) {
                try {
                    var listOfMessages:String? = input!!.readLine();
                    // read the list of messages from the socket

                    // read the list of messages from the socket

                    // read the list of messages from the socket
                    // val listOfMessages=  objectInputStream!!.readObject()

                    *//*  var listOfMessages = ArrayList<CartItemDisplay>()
                      listOfMessages = objectInputStream!!.readObject() as ArrayList<CartItemDisplay>*//*

                    //val listOfMessages =   objectInputStream!!.readObject() as List<CDMessage>
                    *//*val cartitms: ArrayList<CartItem> = ArrayList()
                    for(i in listOfMessages.iterator()){
                        cartitms.add(i)
                    }
                    viewModel!!.ItemsMutableLiveData.value =cartitms*//*

                    if (listOfMessages != null) {

                        //var ss = message.substring(0, 5)
                        runOnUiThread(Runnable {
                            //updateTotals(listOfMessages)

                        })
                    } else {
                        serverSocket!!.close()
                        Thread1 = Thread(thread1())
                        Thread1!!.start()
                        return;
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
    }
*/
    /*fun getLocalIpAddress(): String {

        val wifiManager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        var wifiInfo: WifiInfo = wifiManager.getConnectionInfo();
        var ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(
                        ipInt
                ).array()
        ).getHostAddress();
    }*/



    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    // We only want IPv4 addresses that are NOT the loopback (127.0.0.1)
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("IP_FETCH", "Failed to get IP Address", ex)
        }
        return "Disconnected" // Fallback message
    }



    override fun onDestroy() {
        super.onDestroy()

        // 1. Stop the server and release Port 8081
        kdsServerManager.stopServer()

        // 2. Clean up the media player to prevent memory leaks
        stopSound()

        // 3. Cancel the background sync job
        jobPostStatus.cancel()
    }


    /*internal inner  class threadStart : Runnable {
        override fun run() {
            val socket: Socket
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket!!.setReuseAddress(true);
                runOnUiThread(Runnable {
                    binding.tvConnectionStatus!!.setText("Disconnected")
                    binding.tvConnectionStatus.setTextColor(Color.RED)
                    binding.tvIP!!.text = "IP: $SERVER_IP"
                    binding.tvPort!!.text = "Port: $SERVER_PORT"
                    binding.nodata.txtIPAddress.text = "IP: $SERVER_IP"
                })

                //serverSocket = ServerSocket(SERVER_PORT)
                while (true) {
                    var clientSocket: Socket? = null
                    try {
                        // socket object to receive incoming client requests
                        clientSocket = serverSocket!!.accept()
                        println("A new client is connected : $clientSocket")

                        runOnUiThread(Runnable {
                            binding.tvConnectionStatus!!.text = "Connected"
                            binding.tvConnectionStatus.setTextColor(Color.GREEN)
                            IsKDSConnected=true

                        })

                        // obtaining input and out streams
                        val dis = ObjectInputStream(clientSocket.getInputStream())
                        //val dos = ObjectOutputStream(clientSocket.getOutputStream())
                        //outputStreamKDS=  clientSocket!!.getOutputStream();
                        objectOutputStreamKDS = ObjectOutputStream(clientSocket!!.getOutputStream())

                        println("Assigning new thread for this client")

                        // create a new thread object
                        val t: Thread = ClientHandler(clientSocket, dis, objectOutputStreamKDS!!)

                        // Invoking the start() method
                        t.start()

                    }
                    catch (e: SocketException){
                        IsKDSConnected=false
                        runOnUiThread(Runnable {
                            binding.tvConnectionStatus!!.setText("Disconnected")
                            binding.tvConnectionStatus.setTextColor(Color.RED)
                        })
                    }
                    catch (e: Exception) {
                        IsKDSConnected=false
                        clientSocket!!.close()
                        e.printStackTrace()
                    }
                }

               *//* try {
                    socket = serverSocket!!.accept()
                    //scanner = Scanner(socket.getInputStream())
                    output = PrintWriter(socket.getOutputStream(), true)

                    // get the input stream from the connected socket

                    // get the input stream from the connected socket
                    inputStream = socket.getInputStream()
                    // create a DataInputStream so we can read data from it.
                    // create a DataInputStream so we can read data from it.
                    objectInputStream = ObjectInputStream(inputStream)

                    input = BufferedReader(InputStreamReader(socket.getInputStream()))

                    runOnUiThread(Runnable {
                        tvConnectionStatus!!.setText("Connected")
                        tvConnectionStatus.setTextColor(Color.GREEN)

                    })
                    Thread2 = Thread(thread2())
                    Thread2!!.start()

                    con=true
                    *//**//* Thread4 = Thread(thread4())
                     Thread4!!.start()*//**//*

                    *//**//*while (scanner!!.hasNextLine()) {
                        var message:String = "${ scanner!!.nextLine() }"
                        runOnUiThread(Runnable {
                            tvMessages!!.append("client:" + message + "\n");
                        })
                    }
*//**//*
                } catch (e: IOException) {
                    e.printStackTrace()
                }*//*
            } catch (e: IOException) {
                IsKDSConnected=false
                e.printStackTrace()
            }
        }
    }*/


    internal inner class ClientHandler(private val s: Socket, private val dis: ObjectInputStream, private val dos: ObjectOutputStream) : Thread() {
        var fordate: DateFormat = SimpleDateFormat("yyyy/MM/dd")
        var fortime: DateFormat = SimpleDateFormat("hh:mm:ss")
        override fun run() {
            var received: String
            var toreturn: String
            while (true) {
                try {
                    var newOrder = dis.readObject()

                    if (newOrder != null) {
                        newOrder = newOrder  as ParentModel
                        //var ss = message.substring(0, 5)
                        runOnUiThread(Runnable {
                            addNewOrder(newOrder)
                            playSound()
                        })
                    } else {
                        /*con=false
                        serverSocket!!.close()
                        Thread1 = Thread(thread1())
                        Thread1!!.start()*/
                        s.close()
                        break

                        // return;
                    }



                    // Ask user what he wants
                    /* dos.writeUTF("""
                         What do you want?[Date | Time]..
                         Type Exit to terminate connection.
                         """.trimIndent())*/


                    // receive the answer from client
                    /* received = dis.readUTF()
                     if (received == "Exit") {
                         println("Client " + s + " sends exit...")
                         println("Closing this connection.")
                         s.close()
                         println("Connection closed")
                         break
                     }

                     // creating Date object
                     val date = Date()
                     when (received) {
                         "Date" -> {
                             toreturn = fordate.format(date)
                             dos.writeUTF(toreturn)
                         }
                         "Time" -> {
                             toreturn = fortime.format(date)
                             dos.writeUTF(toreturn)
                         }
                         else -> dos.writeUTF("Invalid input")
                     }*/
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    break
                }
                catch (e: EOFException) {
                    //con=false
                    e.printStackTrace()
                    s.close()
                    break
                    /* Thread1 = Thread(thread1())
                     Thread1!!.start()*/
                }
                catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
            try {
                // closing resources
                dis.close()
                dos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        setupInAppUpdate()
        checkInAppUpdate()
    }

    //region IN APP Update
    /**
     * Creates instance of the manager.
     */
    private var appUpdateManager: AppUpdateManager? = null
    val REQ_CODE_VERSION_UPDATE = 530
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null


    var IS_SERVICE_CHARGE_ENABLED = false
    var IS_QRCODE_SCANNER_ENABLED = false




    private fun startAppUpdateImmediate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE)
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    private fun startAppUpdateFlexible(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE)
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
            unregisterInstallStateUpdListener()
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private fun popupSnackbarForCompleteUpdateAndUnregister() {
        val snackbar = Snackbar.make(binding.root, "Update Downloaded", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("RESTART", View.OnClickListener { appUpdateManager!!.completeUpdate() })
        snackbar.setActionTextColor(resources.getColor(R.color.colorPrimary))
        snackbar.show()
        unregisterInstallStateUpdListener()
    }

    private fun popupSnackbarForRetryUpdate() {
        Snackbar.make(
            binding.root,
            "Unable to download update.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RETRY") { checkInAppUpdate() }
            show()
        }
    }
    /*private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(binding.drawerLayout, "New app is ready", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Install", View.OnClickListener { appUpdateManager!!.completeUpdate() })
        snackbar.setActionTextColor(resources.getColor(R.color.colorPrimary))
        snackbar.show()
    }*/


    private fun setupInAppUpdate() {
        try{
            // Creates instance of the manager.
            appUpdateManager = AppUpdateManagerFactory.create(this)

            // Returns an intent object that you use to check for an update.
            //val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager!!.appUpdateInfo

            // Create a listener to track request state updates.
            installStateUpdatedListener = InstallStateUpdatedListener { installState ->
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED) // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister()
                else if(installState.installStatus() == InstallStatus.FAILED)
                    popupSnackbarForRetryUpdate()
                else if (installState.installStatus() == InstallStatus.INSTALLED){
                    /*if (appUpdateManager != null){
                        appUpdateManager!!.unregisterListener(installStateUpdatedListener!!);
                    }*/
                    unregisterInstallStateUpdListener()
                }
                else{
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + installState.installStatus());
                }
            }

            //checkNewAppVersionStateOnResume()

            // Checks that the platform will allow the specified type of update.
            /* appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

                 if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ) {
                     // Request the update.
                     *//*
                                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                                        // Start an update.
                                        startAppUpdateImmediate(appUpdateInfo)
                                    }
                                    else{
                                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                                         // Before starting an update, register a listener for updates.
                                         appUpdateManager!!.registerListener(installStateUpdatedListener!!)
                                         // Start an update.
                                         startAppUpdateFlexible(appUpdateInfo)
                                        }
                                    }*//*

                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        // Start an update.
                        startAppUpdateFlexible(appUpdateInfo)
                    } else {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            // Start an update.
                            startAppUpdateImmediate(appUpdateInfo)
                        }
                    }

                }else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbarForCompleteUpdateAndUnregister()
                } else {
                    Log.e(TAG, "checkForAppUpdateAvailability: something else");
                }

            }*/
        }catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }

    }

    /**
     * Checks that the update is not installed during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private fun checkNewAppVersionStateOnResume() {

        // Checks that the platform will allow the specified type of update.
        appUpdateManager!!.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdateAndUnregister()
            }  //IMMEDIATE:
            else if(appUpdateInfo.updateAvailability()
                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                // If an in-app update is already running, resume the update.
                startAppUpdateImmediate(appUpdateInfo)
            }
            /*else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ) {
                // Request the update.
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Before starting an update, register a listener for updates.
                    appUpdateManager!!.registerListener(installStateUpdatedListener!!)
                    // Start an update.
                    startAppUpdateFlexible(appUpdateInfo)
                } else if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                    // Start an update.
                    startAppUpdateImmediate(appUpdateInfo)
                }

            }*/
            else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }

        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)

        //region IN-APP UPDATE CALLBACK
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Log.d(com.byteswiz.byteswizpos.Activities.MainActivity.Companion.TAG, "" + "Result Ok")
                    //  handle user's approval }
                    Toast.makeText(this, "Update success! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
                }
                Activity.RESULT_CANCELED -> {
                    {
                        //if you want to request the update again just call checkUpdate()

                    }
                    Toast.makeText(this, "Update canceled by user! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
                    //Log.d(com.byteswiz.byteswizpos.Activities.MainActivity.Companion.TAG, "" + "Result Cancelled")
                    finish()
                    //  handle user's rejection  }
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    //if you want to request the update again just call checkUpdate()
                    Toast.makeText(this, "Update Failed! Result Code: " + resultCode, Toast.LENGTH_LONG).show();

                    //if(sessionManager.getIsLive())
                    checkNewAppVersionStateOnResume()

                    //Log.d(com.byteswiz.byteswizpos.Activities.MainActivity.Companion.TAG, "" + "Update Failure")
                    //  handle update failure
                }
            }
        }
    }

    private fun checkInAppUpdate() {
        appUpdateManager!!
            .appUpdateInfo
            .addOnSuccessListener{ appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                            // Before starting an update, register a listener for updates.
                            appUpdateManager!!.registerListener(installStateUpdatedListener!!)
                            startAppUpdateFlexible(appUpdateInfo)
                        }
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> startAppUpdateImmediate(
                            appUpdateInfo
                        )
                        else -> {
                            // No update is allowed
                        }
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> startAppUpdateImmediate(
                        appUpdateInfo
                    )
                    else -> {
                        // No op
                    }
                }
            }
    }

    /**
     * Needed only for FLEXIBLE update
     */
    private fun unregisterInstallStateUpdListener() {
        try{
            if (appUpdateManager != null && installStateUpdatedListener != null) appUpdateManager!!.unregisterListener(installStateUpdatedListener!!)
        }catch (ex:Exception){

        }
    }


    //endregion




}