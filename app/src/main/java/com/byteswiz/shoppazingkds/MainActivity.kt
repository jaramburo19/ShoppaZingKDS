package com.byteswiz.shoppazingkds

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.CompleteOrderRequest
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.dataadapters.ParentAdapter
import com.byteswiz.shoppazingkds.databinding.ActivityMainBinding
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.livedata.CartViewModel
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_PREPARING
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_READY
import com.byteswiz.shoppazingkds.utils.makeSound
import com.byteswiz.shoppazingkds.utils.makeStatusNotification
import com.byteswiz.shoppazingkds.viewmodels.SyncViewModel
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
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {
    //lateinit var recyclerView: RecyclerView

    var serverSocket: ServerSocket? = null
    var Thread1: Thread? = null
    var Thread2: Thread? = null
    var Thread3: Thread? = null
    var Thread4: Thread? = null

    var IsKDSConnected: Boolean=false
    //private var outputStreamKDS:OutputStream?=null
    private var objectOutputStreamKDS:ObjectOutputStream?=null
    var ThreadKDSComplete: Thread? = null

    var tvIP: TextView? = null
    var tvPort: TextView? = null
    lateinit var tvMessages: TextView
    lateinit var etMessage: EditText
    lateinit var btnSend: Button
    lateinit var tvConnectionStatus: TextView
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

    val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + jobPostStatus

    private var jobPostStatus: Job = Job()

    private fun startPostUpdateCoroutineSync() = GlobalScope.launch(Dispatchers.IO) {

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

        tvIP = findViewById(R.id.tvIP)
        tvPort = findViewById(R.id.tvPort)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)


        try {
            SERVER_IP = getLocalIpAddress()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }


        setupAdapters()

        /*viewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        viewModel.ItemsMutableLiveData.value = ShoppingCart.getOrders()
        viewModel.ItemsMutableLiveData.observe(this, ordersObserver())*/

        initRecycler()



        //TEMPORARY DELETE THIS
       // Thread1 = Thread(thread1())
       // Thread1!!.start()

         Thread1 = Thread(threadStart())
         Thread1!!.start()




    }

    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    fun setupAdapters(){

        executor.execute {
            var currentOrders =ShoppingCart.getOrders(this@MainActivity)

            handler.post {
                _adapter = ParentAdapter(this,this,currentOrders, object : OnParentButtonClicked {
                    override fun onPreparingClicked(
                        receiptNo: String,
                        localUniqueId: String,
                        position: Int
                    ) {
                        //Toast.makeText(this@MainActivity,"Preparing Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
                        //val currentOrders =ShoppingCart.getOrders().filter { it.orderStatusId != ORDER_STATUS_READY }.toMutableList()
                        updateOrderStatusExecute(localUniqueId,ORDER_STATUS_PREPARING,position)

                        //ShoppingCart.updateStatus(ORDER_STATUS_PREPARING, localUniqueId,this@MainActivity)
                        //_adapter.notifyDataSetChanged()
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
    }

    fun updateChildOrderStatusExecute(parentModelId:Int,itemId: Long, flag:Boolean){

        executor.execute {

            ShoppingCart.updateChildItemStatus(parentModelId,itemId,flag, this@MainActivity)
            handler.post {
                //finish()
            }
        }


    }


    fun refreshOrders(){


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
    }
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
    }

    fun sendtoCounterCompleteExecute(localUniqueId: String, textDuration:String){
        executor.execute {
            var orderItem = ShoppingCart.getOrderByLocalUID(localUniqueId,this)
            handler.post {
                ThreadKDSComplete = Thread(threadSendToCounterComplete(orderItem!!, ORDER_STATUS_READY, textDuration))
                ThreadKDSComplete!!.start()
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

            sendtoCounterCompleteExecute(localUniqueId, textDuration)

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
    private fun initRecycler(){
        //_adapter.setOrders(ShoppingCart.getOrders().filter { it -> it.orderStatusId != 4 }.toMutableList())
        //binding.recyclerView = findViewById(R.id.rv_parent)
        refreshOrders()
        //binding.recyclerView.adapter = _adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            //adapter = _adapter
        }

    }

    fun addNewOrderExecute(order:ParentModel){


    }
    fun addNewOrder(order: ParentModel){
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
    }


    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    private var objectInputStream: ObjectInputStream? =null
    private  var inputStream: InputStream? =null

    // private var scanner:Scanner?=null
    internal inner  class thread1 : Runnable {
        override fun run() {
            val socket: Socket
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket!!.setReuseAddress(true);
                runOnUiThread(Runnable {
                    tvConnectionStatus!!.setText("Disconnected")
                    tvConnectionStatus.setTextColor(Color.RED)
                    tvIP!!.text = "IP: $SERVER_IP"
                    tvPort!!.text = "Port: $SERVER_PORT"
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
                        tvConnectionStatus!!.setText("Connected ")
                        tvConnectionStatus.setTextColor(Color.GREEN)

                    })
                    Thread2 = Thread(thread2())
                    Thread2!!.start()

                    con=true
                    /* Thread4 = Thread(thread4())
                     Thread4!!.start()*/

                    /*while (scanner!!.hasNextLine()) {
                        var message:String = "${ scanner!!.nextLine() }"
                        runOnUiThread(Runnable {
                            tvMessages!!.append("client:" + message + "\n");
                        })
                    }
*/
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    var con: Boolean=true
    internal inner class thread2 (): Runnable {
        override fun run() {
            /* while (scanner!!.hasNextLine()) {
                 var message:String = "${ scanner!!.nextLine() }"
                 runOnUiThread(Runnable {
                     tvMessages!!.append("client:" + message + "\n");
                 })
             }*/
            //var obj: Any?
            /*while (objectInputStream!!.readObject().also { obj = it } !is EofIndicatorClass) {
                println(obj)
            }*/



            //while (objectInputStream!!.readObject().also { obj = it } !is EofIndicatorClass) {
            while (con) {
                try {
                    //var message:String? = input!!.readLine();
                    // read the list of messages from the socket

                    // read the list of messages from the socket

                    // read the list of messages from the socket
                    // val listOfMessages=  objectInputStream!!.readObject()

                    /* var listOfMessages = ArrayList<CartItemDisplay>()
                     listOfMessages = objectInputStream!!.readObject() as ArrayList<CartItemDisplay>*/

                    var newOrder = objectInputStream!!.readObject()

                    //val listOfMessages =   objectInputStream!!.readObject() as List<CDMessage>
                    /*val cartitms: ArrayList<CartItem> = ArrayList()
                    for(i in listOfMessages.iterator()){
                        cartitms.add(i)
                    }
                    viewModel!!.ItemsMutableLiveData.value =cartitms*/

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
    }

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

    internal inner class thread4 (): Runnable {
        override fun run() {
            /* while (scanner!!.hasNextLine()) {
                 var message:String = "${ scanner!!.nextLine() }"
                 runOnUiThread(Runnable {
                     tvMessages!!.append("client:" + message + "\n");
                 })
             }*/


            while (true) {
                try {
                    var listOfMessages:String? = input!!.readLine();
                    // read the list of messages from the socket

                    // read the list of messages from the socket

                    // read the list of messages from the socket
                    // val listOfMessages=  objectInputStream!!.readObject()

                    /*  var listOfMessages = ArrayList<CartItemDisplay>()
                      listOfMessages = objectInputStream!!.readObject() as ArrayList<CartItemDisplay>*/

                    //val listOfMessages =   objectInputStream!!.readObject() as List<CDMessage>
                    /*val cartitms: ArrayList<CartItem> = ArrayList()
                    for(i in listOfMessages.iterator()){
                        cartitms.add(i)
                    }
                    viewModel!!.ItemsMutableLiveData.value =cartitms*/

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

    fun getLocalIpAddress(): String {

        val wifiManager: WifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        var wifiInfo: WifiInfo = wifiManager.getConnectionInfo();
        var ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(
                        ipInt
                ).array()
        ).getHostAddress();
    }



    internal inner  class threadStart : Runnable {
        override fun run() {
            val socket: Socket
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket!!.setReuseAddress(true);
                runOnUiThread(Runnable {
                    tvConnectionStatus!!.setText("Disconnected")
                    tvConnectionStatus.setTextColor(Color.RED)
                    tvIP!!.text = "IP: $SERVER_IP"
                    tvPort!!.text = "Port: $SERVER_PORT"
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
                            tvConnectionStatus!!.text = "Connected"
                            tvConnectionStatus.setTextColor(Color.GREEN)
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
                            tvConnectionStatus!!.setText("Disconnected")
                            tvConnectionStatus.setTextColor(Color.RED)
                        })
                    }
                    catch (e: Exception) {
                        IsKDSConnected=false
                        clientSocket!!.close()
                        e.printStackTrace()
                    }
                }

               /* try {
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
                }*/
            } catch (e: IOException) {
                IsKDSConnected=false
                e.printStackTrace()
            }
        }
    }


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

}