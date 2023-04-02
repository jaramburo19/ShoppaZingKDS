package com.byteswiz.shoppazingkds

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.byteswiz.shoppazingkds.utils.SessionManager
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment


class MyCustomAppIntro : AppIntro() {
    lateinit var sessionManager: SessionManager

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        this.isColorTransitionsEnabled = true
        // setTransformer(AppIntroPageTransformerType.Zoom)

        if (sessionManager.isFirstRun()) {
            showIntroSlides()
        } else {
            gotoMain()
        }


    }
    fun showIntroSlides(){
        sessionManager.setFirstRun()
        addSlide(
            AppIntroFragment.newInstance(
                title = "Welcome to ShoppaZing KDS",
                description = "Process your orders in the kitchen efficiently",
                backgroundColor = ContextCompat.getColor(this, R.color.colorPrimary)
                //,            imageDrawable = R.drawable.logo_ap

            ))
        addSlide(
            AppIntroFragment.newInstance(
                title = "...Let's get started!",
                description = "Set this tablet to Static IP Address. e.g. 192.168.0.128",
                descriptionColor = ContextCompat.getColor(this, R.color.colorPrimary),
                backgroundColor = ContextCompat.getColor(this, R.color.colorAccent)


            ))

        addSlide(
            AppIntroFragment.newInstance(
                title = "...Shoppazing POS",
                description = "Go to settings of your ShoppaZing POS and set Kitchen display IP Address to your ShoppaZing KDS IP Address",
                backgroundColor = ContextCompat.getColor(this, R.color.colorLightBlue)


            ))

        addSlide(
            AppIntroFragment.newInstance(
                title = "You're all set!",
                description = "Start managing confirmed orders in your kitchen",
                backgroundColor = ContextCompat.getColor(this, R.color.colorPurple)


            ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        gotoMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        gotoMain()
    }

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}