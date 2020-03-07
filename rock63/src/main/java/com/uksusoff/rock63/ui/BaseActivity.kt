package com.uksusoff.rock63.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.uksusoff.rock63.data.DatabaseComponent
import com.uksusoff.rock63.data.InternalPrefs_
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.sharedpreferences.Pref

@EActivity
abstract class BaseActivity : AppCompatActivity() {

    companion object {
        const val ACTION_CHECK_ALARM = "com.uksusoff.rock63.ui.ACTION_CHECK_ALARM"
    }

    private var databaseInternal: DatabaseComponent? = null

    @Pref
    protected lateinit var internalPrefs: InternalPrefs_

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendBroadcast(Intent(ACTION_CHECK_ALARM))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (databaseInternal != null) {
            OpenHelperManager.releaseHelper()
            databaseInternal = null
        }
    }

    protected val database: DatabaseComponent
        get() {
            return databaseInternal ?: run {
                val helper = OpenHelperManager.getHelper(
                    this,
                    DatabaseComponent::class.java
                ) as DatabaseComponent
                databaseInternal = helper
                return helper
            }
        }

    @UiThread
    open fun showWarning(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showWarning(@StringRes resId: Int) {
        showWarning(getString(resId))
    }
}