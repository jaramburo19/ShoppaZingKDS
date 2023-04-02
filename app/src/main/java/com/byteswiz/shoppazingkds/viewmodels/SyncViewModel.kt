package com.byteswiz.shoppazingkds.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.byteswiz.shoppazingkds.coroutine_workers.postUpdateOrderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException

class SyncViewModel (application: Application):  AndroidViewModel(application)
{
    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private val workManager = WorkManager.getInstance(application)

    //var dataRepository: DataRepository = DataRepository(application)



    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    //private var dbManager: DBManager? = null



    /**
     * A list of items that can be shown on the screen. This is private to avoid exposing a
     * way to set this value to observers.
     */
    // private var _itemsLiveData: LiveData<List<ItemModel>>

    /**
     * A list of Items that can be shown on the screen. Views should use this to get access
     * to the data.
     */
    /*  val ItemsMutableLiveData: LiveData<List<ItemModel>>
          get() = _itemsLiveData*/



    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)


    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * Flag to display the error message. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * Flag to display the error message. Views should use this to get access
     * to the data.
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    /**
     * init{} is called immediately when this ViewModel is created.
     */
    init {
        //refreshDataFromRepository()

        //_itemsLiveData = dataRepository.getItems()



        // This transformation makes sure that whenever the current work Id changes the WorkInfo
        // the UI is listening to changes
        //outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }


    //val itemProductList = dataRepository.getItems()

    //val payInOutList = dataRepository.getPayInOuts()


    /*fun getpayInOutList(shiftLocalId: String): LiveData<List<PayInPayOutModel>> {
        //return _itemsLiveData
        return dataRepository.getPayInOuts(shiftLocalId)
    }*/
    /**
     * Refresh data from the repository. Use a coroutine launch to run in a
     * background thread.
     */
    /*fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                //dataRepository.refreshItems()
                dataRepository.refreshItems()
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
                if(itemProductList.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }*/


    /* *//**
 * Refresh data from network and pass it via LiveData. Use a coroutine launch to get to
 * background thread.
 *//*
    private fun refreshDataFromNetwork() = viewModelScope.launch {

        try {
            val playlist = DevByteNetwork.devbytes.getPlaylist().await()
            _playlist.postValue(playlist.asDomainModel())

            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false

        } catch (networkError: IOException) {
            // Show a Toast error message and hide the progress bar.
            _eventNetworkError.value = true
        }
    }
*/






    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun StartItemSync() {
        //var networkType:NetworkType = NetworkType.CONNECTED
        // Create charging constraint
        val constraints = Constraints.Builder()
            //.setRequiresCharging(true)
            //.setRequiresBatteryNotLow(true)
            //.setRequiresStorageNotLow(true)
            //.setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val postUpdateRequest = OneTimeWorkRequestBuilder<postUpdateOrderStatus>()
            .setConstraints(constraints)
            .build()


        var continuation = workManager
            .beginUniqueWork(
                "ITEMS_DOWNLOAD_WORK_NAME", // THIS ENSURES ONE SYNC AT A TIME.
                ExistingWorkPolicy.REPLACE,
                postUpdateRequest
            )


        // Actually start the work
        continuation.enqueue()

    }





}