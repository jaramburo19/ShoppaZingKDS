package com.byteswiz.shoppazingkds

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.dataadapters.ParentAdapter
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
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView

    var serverSocket: ServerSocket? = null
    var Thread1: Thread? = null
    var Thread2: Thread? = null
    var Thread3: Thread? = null
    var Thread4: Thread? = null


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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


        _adapter = ParentAdapter(ShoppingCart.getOrders(), object : OnParentButtonClicked {
            override fun onPreparingClicked(receiptNo: String, todaysOrderNo: String) {
                //Toast.makeText(this@MainActivity,"Preparing Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()

                ShoppingCart.updateStatus(ORDER_STATUS_PREPARING, todaysOrderNo)
                ShoppingCart.updateSyncStatus(false, todaysOrderNo)
                //_adapter.notifyDataSetChanged()
                _adapter.setOrders(ShoppingCart.getOrders().filter { it.orderStatusId != 4 }.toMutableList())
                stopSound()
            }

            override fun onCompletedClicked(receiptNo: String, todaysOrderNo: String, position: Int) {
                ShoppingCart.updateStatus(ORDER_STATUS_READY, todaysOrderNo)
                ShoppingCart.updateSyncStatus(false, todaysOrderNo)
                _adapter.setOrders(ShoppingCart.getOrders().filter { it.orderStatusId != 4 }.toMutableList())
                stopSound()
                //Toast.makeText(this@MainActivity,"Completed Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
            }

            override fun onRecallClicked(receiptNo: String, todaysOrderNo: String) {

            }

            override fun onChildItemClicked(orderNo: String, itemId: Long, flag:Boolean) {
                ShoppingCart.updateChildItemStatus(orderNo,itemId,flag)
            }


        })

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
        }
        return super.onOptionsItemSelected(item)
    }

    fun ShowRecall() {

        val intent = Intent(this, RecallActivity::class.java)
       // intent.putExtra("EXTRA_ORDER_STATUS", order.OrderStatusId.toString())

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        _adapter.setOrders(ShoppingCart.getOrders().filter { it -> it.orderStatusId != 4 }.toMutableList())
        _adapter.notifyDataSetChanged()
    }




   /* private fun ordersObserver(): Observer<MutableList<ParentModel>?> {
        return Observer { orders ->
                //_adapter.setOrders(getOrders())

        }
    }*/

    private fun initRecycler(){
        _adapter.setOrders(ShoppingCart.getOrders().filter { it -> it.orderStatusId != 4 }.toMutableList())
        recyclerView = findViewById(R.id.rv_parent)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            //adapter = _adapter
        }
        recyclerView.adapter = _adapter
        _adapter.notifyDataSetChanged()
    }

    fun addNewOrder(order: ParentModel){
        ShoppingCart.addOrder(order)
        _adapter.addOrder(order)
        makeStatusNotification("New order arrived", this@MainActivity, "Alert!")
        makeSound(this@MainActivity)
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

    internal inner class thread3(private val message: String) : Runnable {
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
                })

                //serverSocket = ServerSocket(SERVER_PORT)
                while (true) {
                    var s: Socket? = null
                    try {
                        // socket object to receive incoming client requests
                        s = serverSocket!!.accept()
                        println("A new client is connected : $s")

                        runOnUiThread(Runnable {
                            tvConnectionStatus!!.setText("Connected")
                            tvConnectionStatus.setTextColor(Color.GREEN)

                        })

                        // obtaining input and out streams
                        val dis = ObjectInputStream(s.getInputStream())
                        val dos = ObjectOutputStream(s.getOutputStream())
                        println("Assigning new thread for this client")

                        // create a new thread object
                        val t: Thread = ClientHandler(s, dis, dos)

                        // Invoking the start() method
                        t.start()
                    } catch (e: Exception) {
                        s!!.close()
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
                    var newOrder = dis!!.readObject()

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
                    s!!.close()
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