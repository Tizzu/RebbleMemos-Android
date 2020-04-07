package it.tizzu.rebblememos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.token_dialog.view.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import org.jsoup.Jsoup

class FirstFragment : Fragment(), AdapterView.OnItemSelectedListener {

    val icons = arrayOf(
        "NOTIFICATION_REMINDER",
        "ALARM_CLOCK",
        "AMERICAN_FOOTBALL",
        "AUDIO_CASSETTE",
        "BASKETBALL",
        "BIRTHDAY_EVENT",
        "CAR_RENTAL",
        "CLOUDY_DAY",
        "CRICKET_GAME",
        "DINNER_RESERVATION",
        "DISMISSED_PHONE_CALL",
        "GENERIC_CONFIRMATION",
        "GENERIC_EMAIL",
        "GENERIC_QUESTION",
        "GENERIC_WARNING",
        "GLUCOSE_MONITOR",
        "HEAVY_RAIN",
        "HEAVY_SNOW",
        "HOCKEY_GAME",
        "HOTEL_RESERVATION",
        "INCOMING_PHONE_CALL",
        "LIGHT_RAIN",
        "LIGHT_SNOW",
        "LOCATION",
        "MOVIE_EVENT",
        "MUSIC_EVENT",
        "NEWS_EVENT",
        "NOTIFICATION_FLAG",
        "NOTIFICATION_GENERIC",
        "NOTIFICATION_LIGHTHOUSE",
        "PARTLY_CLOUDY",
        "PAY_BILL",
        "RADIO_SHOW",
        "RAINING_AND_SNOWING",
        "REACHED_FITNESS_GOAL",
        "RESULT_DISMISSED",
        "RESULT_FAILED",
        "SCHEDULED_EVENT",
        "SCHEDULED_FLIGHT",
        "SETTINGS",
        "SOCCER_GAME",
        "STOCKS_EVENT",
        "SUNRISE",
        "SUNSET",
        "TIDE_IS_HIGH",
        "TIMELINE_BASEBALL",
        "TIMELINE_CALENDAR",
        "TIMELINE_MISSED_CALL",
        "TIMELINE_SPORTS",
        "TIMELINE_SUN",
        "TIMELINE_WEATHER",
        "TV_SHOW"
    )
    var iconDisplay: ImageView? = null
    val json: JSONObject? = JSONObject()
    var name = ""
    var preferences: SharedPreferences? = null
    var tokenDisplay: TextView? = null
    var ok = true
    var pinID: String = ""
    var url: URL? = null
    val charPool: List<Char> = ('a'..'m') + ('n'..'z')
    var toast: Toast? = null
    var result = ""

    // Creation of the fragment, pretty standard
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = activity!!.getSharedPreferences(getString(R.string.preferences), 0)
        if (preferences!!.getBoolean("firstTime", true))
            tokenDialog(context!!)

        var threadURL: Thread = Thread(Runnable {
            Looper.prepare()
            val doc = Jsoup.connect("https://tizzu.github.io/android/latest.html").get()
            println("HERE! " + doc)
            result = doc.body().text()
            println("HERE! " + result)


            if (BuildConfig.VERSION_NAME != result)
                Snackbar.make(view, "Version " + result + " avaiable!", Snackbar.LENGTH_INDEFINITE).setAction("Download") {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://tizzu.github.io/")
                    )
                    startActivity(browserIntent)
                }.show()

            Looper.loop()
        })

        threadURL.start()

        tokenDisplay = view.findViewById(R.id.token_edit)


        val titleEdit: EditText = view.findViewById(R.id.memo_title_edit)
        val subtitleEdit: EditText = view.findViewById(R.id.memo_subtitle_edit)
        val bodyEdit: EditText = view.findViewById(R.id.memo_body_edit)

        val timePickerButton: Button = view.findViewById(R.id.btn_time)
        val timeView: TextView = view.findViewById(R.id.time_display)

        val datePickerButton: Button = view.findViewById(R.id.btn_date)
        val dateView: TextView = view.findViewById(R.id.date_display)

        val sendButton: Button = view.findViewById(R.id.send_to_timeline)

        val changeTokenButton: Button = view.findViewById(R.id.change_token_button)

        val onArrivalCheck: CheckBox = view.findViewById(R.id.notify_arrival_check)
        val onEventCheckBox: CheckBox = view.findViewById(R.id.notify_on_event_check)

        //Token Display
        if (preferences!!.getString("timelineToken", "") == "")
            tokenDisplay!!.setText(R.string.no_token_set)
        else
            tokenDisplay!!.setText(preferences!!.getString("timelineToken", "Errore"))


        //Token Change

        changeTokenButton.setOnClickListener() {
            tokenDialog(view.context)
        }


        //Date / Time stuff
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val cInOneYear: Calendar = Calendar.getInstance()
        cInOneYear.set(Calendar.YEAR, year + 1)

        var hour = c.get(Calendar.HOUR_OF_DAY).toString()
        var minute = c.get(Calendar.MINUTE).toString()
        var monthAdj = month + 1

        if (hour.toInt() in 0..9) {
            hour = ("0" + hour)
        }

        if (minute.toInt() in 0..9) {
            minute = ("0" + minute)
        }

        timeView.setText("$hour:$minute")
        dateView.setText("$day/$monthAdj/$year")

        // Icon Stuff
        val spinner: Spinner = view.findViewById(R.id.icon_selector)
        val aa = ArrayAdapter(this.context!!, android.R.layout.simple_spinner_item, icons)
        iconDisplay = view.findViewById(R.id.icon_display)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(aa)
        spinner.onItemSelectedListener = this

        //Summon the Android Date Picker and save its result into the TextView
        datePickerButton.setOnClickListener {

            val datePicker = DatePickerDialog(
                this.context!!,
                DatePickerDialog.OnDateSetListener { _, year, newMonth, dayOfMonth ->
                    monthAdj = newMonth + 1
                    dateView.setText("$dayOfMonth/$monthAdj/$year")
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.YEAR, year)
                },
                year,
                month,
                day
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2)
            datePicker.datePicker.maxDate = cInOneYear.timeInMillis
            datePicker.show()

        }

        timePickerButton.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                timeView.text = SimpleDateFormat("HH:mm").format(c.time)
            }
            TimePickerDialog(
                activity,
                timeSetListener,
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        sendButton.setOnClickListener {
            ok = true
            if (titleEdit.getText().toString().trim() == "") {
                titleEdit.setError(getString(R.string.required_field))
                ok = false
            }
            if (preferences!!.getString("timelineToken", "") == "") {
                tokenDisplay!!.setError(getString(R.string.no_token_set))
                ok = false
            }

            if (ok) {
                pinID = "RMAN-"
                pinID += (1..13).map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")

                json!!.put("id", pinID)
                json.put("time", c.time.toInstant().toString())

                if (onArrivalCheck.isChecked)
                    json.put("createNotification", notification(title = getString(R.string.new_memo), body = titleEdit.text.toString(), tinyIcon = "NOTIFICATION_FLAG"))

                if (onEventCheckBox.isChecked)
                    json.put("reminders", reminder(c.time.toInstant().toString(), titleEdit.text.toString(), subtitleEdit.text.toString(), bodyEdit.text.toString(), name))

                json.put("layout", layout("genericPin", titleEdit.text.toString(), subtitleEdit.text.toString(), bodyEdit.text.toString(), name))

                url = URL("https://timeline-api.rebble.io/v1/user/pins/" + pinID)

                var thread: Thread = Thread(Runnable {
                    Looper.prepare()

                    var execute : Connection.Response = Jsoup.connect("https://timeline-api.rebble.io/v1/user/pins/" + pinID)
                        .header("Content-Type", "application/json")
                        .header(
                            "X-User-Token",
                            preferences!!.getString("timelineToken", "")
                        ).method(Connection.Method.PUT)
                        .requestBody(json.toString())
                        .timeout(0)
                        .execute()

                    var statusCode : Int = execute.statusCode()
                    Log.i("RES", statusCode.toString())

                    when (statusCode) {
                        200 -> toast = Toast.makeText(context, getString(R.string.all_good) + pinID, Toast.LENGTH_LONG)
                        400 -> toast = Toast.makeText(context, getString(R.string.contact_tizzu), Toast.LENGTH_LONG)
                        403 -> toast = Toast.makeText(context, getString(R.string.contact_tizzu), Toast.LENGTH_LONG)
                        410 -> toast = Toast.makeText(context, getString(R.string.check_your_token), Toast.LENGTH_LONG)
                        429 -> toast = Toast.makeText(context, getString(R.string.too_many_pins), Toast.LENGTH_LONG)
                        500 -> toast = Toast.makeText(context, getString(R.string.server_unreachable), Toast.LENGTH_LONG)
                    }

                    activity!!.runOnUiThread {
                        toast!!.show()
                    }
                    Looper.loop()
                })
                thread.start()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        name = icons[position]
        when (name) {
            "NOTIFICATION_REMINDER" -> iconDisplay!!.setImageResource(R.drawable.notification_reminder)
            "ALARM_CLOCK" -> iconDisplay!!.setImageResource(R.drawable.alarm_clock)
            "AMERICAN_FOOTBALL" -> iconDisplay!!.setImageResource(R.drawable.american_football)
            "AUDIO_CASSETTE" -> iconDisplay!!.setImageResource(R.drawable.audio_cassette)
            "BASKETBALL" -> iconDisplay!!.setImageResource(R.drawable.basketball)
            "BIRTHDAY_EVENT" -> iconDisplay!!.setImageResource(R.drawable.birthday_event)
            "CAR_RENTAL" -> iconDisplay!!.setImageResource(R.drawable.car_rental)
            "CLOUDY_DAY" -> iconDisplay!!.setImageResource(R.drawable.cloudy_day)
            "CRICKET_GAME" -> iconDisplay!!.setImageResource(R.drawable.cricket_game)
            "DINNER_RESERVATION" -> iconDisplay!!.setImageResource(R.drawable.dinner_reservation)
            "DISMISSED_PHONE_CALL" -> iconDisplay!!.setImageResource(R.drawable.dismissed_phone_call)
            "GENERIC_CONFIRMATION" -> iconDisplay!!.setImageResource(R.drawable.generic_confirmation)
            "GENERIC_EMAIL" -> iconDisplay!!.setImageResource(R.drawable.generic_email)
            "GENERIC_QUESTION" -> iconDisplay!!.setImageResource(R.drawable.generic_question)
            "GENERIC_WARNING" -> iconDisplay!!.setImageResource(R.drawable.generic_warning)
            "GLUCOSE_MONITOR" -> iconDisplay!!.setImageResource(R.drawable.glucose_monitor)
            "HEAVY_RAIN" -> iconDisplay!!.setImageResource(R.drawable.heavy_rain)
            "HEAVY_SNOW" -> iconDisplay!!.setImageResource(R.drawable.heavy_snow)
            "HOCKEY_GAME" -> iconDisplay!!.setImageResource(R.drawable.hockey_game)
            "HOTEL_RESERVATION" -> iconDisplay!!.setImageResource(R.drawable.hotel_reservation)
            "INCOMING_PHONE_CALL" -> iconDisplay!!.setImageResource(R.drawable.incoming_phone_call)
            "LIGHT_RAIN" -> iconDisplay!!.setImageResource(R.drawable.light_rain)
            "LIGHT_SNOW" -> iconDisplay!!.setImageResource(R.drawable.light_snow)
            "LOCATION" -> iconDisplay!!.setImageResource(R.drawable.location)
            "MOVIE_EVENT" -> iconDisplay!!.setImageResource(R.drawable.movie_event)
            "MUSIC_EVENT" -> iconDisplay!!.setImageResource(R.drawable.music_event)
            "NEWS_EVENT" -> iconDisplay!!.setImageResource(R.drawable.news_event)
            "NOTIFICATION_FLAG" -> iconDisplay!!.setImageResource(R.drawable.notification_flag)
            "NOTIFICATION_GENERIC" -> iconDisplay!!.setImageResource(R.drawable.notification_generic)
            "NOTIFICATION_LIGHTHOUSE" -> iconDisplay!!.setImageResource(R.drawable.notification_lighthouse)
            "PARTLY_CLOUDY" -> iconDisplay!!.setImageResource(R.drawable.partly_cloudy)
            "PAY_BILL" -> iconDisplay!!.setImageResource(R.drawable.pay_bill)
            "RADIO_SHOW" -> iconDisplay!!.setImageResource(R.drawable.radio_show)
            "RAINING_AND_SNOWING" -> iconDisplay!!.setImageResource(R.drawable.raining_and_snowing)
            "REACHED_FITNESS_GOAL" -> iconDisplay!!.setImageResource(R.drawable.reached_fitness_goal)
            "RESULT_DISMISSED" -> iconDisplay!!.setImageResource(R.drawable.result_dismissed)
            "RESULT_FAILED" -> iconDisplay!!.setImageResource(R.drawable.result_failed)
            "SCHEDULED_EVENT" -> iconDisplay!!.setImageResource(R.drawable.scheduled_event)
            "SCHEDULED_FLIGHT" -> iconDisplay!!.setImageResource(R.drawable.scheduled_flight)
            "SETTINGS" -> iconDisplay!!.setImageResource(R.drawable.settings)
            "SOCCER_GAME" -> iconDisplay!!.setImageResource(R.drawable.soccer_game)
            "STOCKS_EVENT" -> iconDisplay!!.setImageResource(R.drawable.stocks_event)
            "SUNRISE" -> iconDisplay!!.setImageResource(R.drawable.sunrise)
            "SUNSET" -> iconDisplay!!.setImageResource(R.drawable.sunset)
            "TIDE_IS_HIGH" -> iconDisplay!!.setImageResource(R.drawable.tide_is_high)
            "TIMELINE_BASEBALL" -> iconDisplay!!.setImageResource(R.drawable.timeline_baseball)
            "TIMELINE_CALENDAR" -> iconDisplay!!.setImageResource(R.drawable.timeline_baseball)
            "TIMELINE_MISSED_CALL" -> iconDisplay!!.setImageResource(R.drawable.timeline_missed_call)
            "TIMELINE_SPORTS" -> iconDisplay!!.setImageResource(R.drawable.timeline_sports)
            "TIMELINE_SUN" -> iconDisplay!!.setImageResource(R.drawable.timeline_sun)
            "TIMELINE_WEATHER" -> iconDisplay!!.setImageResource(R.drawable.timeline_weather)
            "TV_SHOW" -> iconDisplay!!.setImageResource(R.drawable.tv_show)
        }

    }

    fun tokenDialog(context: Context) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.token_dialog, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)
        val welcomeText: TextView = mDialogView.welcomeText
        val dialogToken: EditText = mDialogView.findViewById(R.id.dialogToken)
        if (preferences!!.getBoolean("firstTime", true))
            welcomeText.setText(R.string.first_time)
        else
            welcomeText.setText(R.string.not_first_time)

        dialogToken.setText(preferences!!.getString("timelineToken", ""))

        //show dialog
        val mAlertDialog = mBuilder.show()

        //Done button click of custom layout
        mDialogView.dialogDoneBtn.setOnClickListener {
            //Done dialog
            mAlertDialog.dismiss()
            if (preferences!!.getBoolean("firstTime", true)) {
                with(preferences!!.edit()) {
                    putBoolean("firstTime", false)
                    apply()
                }
            }

            with(preferences!!.edit()) {
                putString("timelineToken", dialogToken.text.toString())
                apply()
            }

            if (dialogToken.text.toString() != "") {
                tokenDisplay!!.setText(dialogToken.text.toString())
            } else {
                tokenDisplay!!.setText(R.string.no_token_set)
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

    fun layout(type: String, title: String, subtitle: String = "", body: String = "", tinyIcon: String): JSONObject {
        val res: JSONObject = JSONObject()
        res.put("type", type)
        res.put("title", title)
        if (subtitle != "")
            res.put("subtitle", subtitle)
        if (body != "")
            res.put("body", body)
        res.put("tinyIcon", "system://images/" + tinyIcon)
        return res
    }

    fun reminder(time: String, title: String, subtitle: String = "", body: String = "", tinyIcon: String): JSONArray {
        val res: JSONArray = JSONArray()
        val resItem: JSONObject = JSONObject()
        resItem.put("time", time)
        resItem.put("layout", layout("genericReminder", title, subtitle, body, tinyIcon))
        res.put(resItem)
        return res
    }

    fun notification(title: String, subtitle: String = "", body: String = "", tinyIcon: String): JSONObject {
        val res: JSONObject = JSONObject()
        res.put("layout", layout("genericNotification", title, subtitle, body, tinyIcon))
        return res
    }
}





