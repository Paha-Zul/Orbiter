package com.quickbite.spaceslingshot.objects

/**
 * Created by Paha on 2/7/2016.
 * Creates the timer as one time.
 * @param secondsInterval The interval to be used for the timer. If no delay is defined, the interval will be used for the initial timer delay.
 * @param _callback The callback to be
 */
class CustomTimer(private var seconds: Float, var oneShot:Boolean = false, private var _callback: (() -> Unit)? = null){

    var userData:Any? = null

    var callback:(()->Unit)?
        get() = _callback
        set(value){_callback = value}

    /** If the timer is done. */
    val done:Boolean
        get() = currTime >= seconds


    var stopped:Boolean = false
        get
        private set

    val remainingTime:Float
        get() = Math.max(0f, seconds - currTime)

    private var currTime:Float = 0f

    init{
        callback = _callback
    }

    fun update(delta:Float){
        //If we're not stopped...
        if(!stopped) {
            currTime += delta   //Increment timer
            if(done){     //Otherwise, if is done
                finish()        //Finish the timer.
            }
        }
    }

    /**
     * Finishes (and restarts if there is an interval) the timer when the timer expires.
     */
    private fun finish(){
        callback?.invoke()
        stop()
        if(seconds >= 0 && !oneShot)
            restart()
    }

    /**
     * Stops the timer.
     */
    fun stop() {
        stopped = true
    }

    /**
     * Starts the timer (if it hasn't expired)
     */
    fun start(){
        if(!done)
            stopped = false
    }

    /**
     * Restarts the timer with the optional settings.
     * @param seconds The number of seconds for the timer to run
     */
    fun restart(seconds: Float = this.seconds){
        currTime = 0f
        this.seconds = seconds
        start()
    }
}