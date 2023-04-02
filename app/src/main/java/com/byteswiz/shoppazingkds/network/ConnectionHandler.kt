package com.byteswiz.shoppazingkds.network

import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.Socket

class ConnectionHandler(socket: Socket?
                       // , fileArray: ArrayList<File>?, filepathNameArray: ArrayList<String>?
) : Runnable {
    private val socket: Socket? = socket
  /*  private val fileArray: ArrayList<File>? = null
    var filepathNameArray: ArrayList<String>? = null*/

    init {
        //this.socket = socket
       /* this.fileArray = fileArray
        this.filepathNameArray = filepathNameArray*/
    }

    override fun run() {
        try {
            val myos: OutputStream = socket!!.getOutputStream()
            //myos.write(filepathNameArray!!.size()) //send file count
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        /*if (!fileArray!!.isEmpty()) {
            for (i in 0 until filepathNameArray!!.size()) {
                //copyFile(fileArray[i], fileArray[i].getName().toString())
                //mtv.setText(fileArray.get(i).getName().toString());
            }
        }*/
    }

   /* private fun copyFile(file: File, name: String) {
        val fis: FileInputStream
        val filesize: Long = file.length()
        try {
            fis = FileInputStream(file)
            val bis = BufferedInputStream(fis)
            val dis = DataInputStream(bis)
            val mybytearray = ByteArray(16384)
            val os: OutputStream
            if (socket != null) {
                os = socket.getOutputStream()
                val dos = DataOutputStream(os)
                dos.writeUTF(name) // filename is also sent to client
                dos.writeLong(filesize) // file size is also sent to client
                var z = filesize
                var n = 0
                while (z > 0
                        && dis.read(mybytearray, 0,
                                Math.min(mybytearray.size.toLong(), z).toInt()).also { n = it } != -1) {
                    dos.write(mybytearray, 0, n)
                    dos.flush()
                    z -= n.toLong()
                }
            }
        } catch (e1: FileNotFoundException) {
            e1.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }*/


}