package it.tizzu.rebblememos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.token_dialog.view.*

//FIXME: THIS IS HORRENDOUS - WE NEED TO FIND A BETTER SOLUTION
var preferences: SharedPreferences? = null

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        preferences = this.getSharedPreferences(getString(R.string.preferences), 0)
        if (preferences!!.getBoolean("firstTime", true))
            tokenDialog(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}
fun tokenDialog(context: Context) {
    val mDialogView = LayoutInflater.from(context).inflate(R.layout.token_dialog, null)
    //AlertDialogBuilder
    val mBuilder = AlertDialog.Builder(context)
        .setView(mDialogView)
    val welcomeText: TextView = mDialogView.welcomeText
    if (preferences!!.getBoolean("firstTime", true))
        welcomeText.setText(R.string.first_time)
    else
        welcomeText.setText(R.string.not_first_time)

    //show dialog
    val mAlertDialog = mBuilder.show()

    //Done button click of custom layout
    mDialogView.dialogDoneBtn.setOnClickListener {
        //dismiss dialog
        mAlertDialog.dismiss()
        val dialogtoken = mDialogView.dialogToken.text.toString()
        if (preferences!!.getBoolean("firstTime", true))
            with(preferences!!.edit()) {
                putBoolean("firstTime", false)
                apply()
            }

    }
    //cancel button click
    mDialogView.dialogCancelBtn.setOnClickListener {
        //dismiss dialog
        mAlertDialog.dismiss()
        if (preferences!!.getBoolean("firstTime", true))
            with(preferences!!.edit()) {
                putBoolean("firstTime", false)
                apply()
            }
    }
}
