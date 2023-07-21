package com.uksusoff.rock63.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.uksusoff.rock63.data.DBHelper
import com.uksusoff.rock63.data.InternalPrefs_
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.sharedpreferences.Pref

@EActivity
abstract class BaseActivity : AppCompatActivity() {

    private var dbHelper: DBHelper? = null

    @Pref
    protected lateinit var internalPrefs: InternalPrefs_

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendBroadcast(Intent(ACTION_CHECK_ALARM))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper()
            dbHelper = null
        }
    }

    protected val helper: DBHelper?
        protected get() {
            if (dbHelper == null) {
                dbHelper = OpenHelperManager.getHelper(this, DBHelper::class.java) as DBHelper
            }
            return dbHelper
        }

    @UiThread
    open fun showWarning(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showWarning(@StringRes resId: Int) {
        showWarning(getString(resId))
    }

    companion object {
        const val ACTION_CHECK_ALARM = "com.uksusoff.rock63.ui.ACTION_CHECK_ALARM"
    }
}