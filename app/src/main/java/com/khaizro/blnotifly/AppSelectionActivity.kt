package com.khaizro.blnotifly

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.khaizro.blnotifly.data.AppPreferences
import com.khaizro.blnotifly.data.InstalledAppLoader
import com.khaizro.blnotifly.model.AppInfo
import com.khaizro.blnotifly.ui.AppAdapter
import com.khaizro.blnotifly.ui.SimpleTextWatcher
import kotlin.concurrent.thread

class AppSelectionActivity : AppCompatActivity() {
    private lateinit var adapter: AppAdapter
    private lateinit var apps: List<AppInfo>
    private lateinit var cbSelectAll: CheckBox
    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private val preferences by lazy { AppPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)
        listView = findViewById(R.id.appList)
        progressBar = findViewById(R.id.progressBar)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        load()
    }

    private fun load() = thread {
        val firstRun = preferences.isFirstRun()
        apps = InstalledAppLoader(packageManager).load(firstRun, preferences.allowedApps())
        runOnUiThread { show(apps) }
    }

    private fun show(loaded: List<AppInfo>) {
        adapter = AppAdapter(this, loaded, ::sync)
        listView.adapter = adapter
        progressBar.visibility = View.GONE
        listView.visibility = View.VISIBLE
        cbSelectAll.setOnClickListener { selectAll(cbSelectAll.isChecked) }
        findViewById<EditText>(R.id.searchApp).addTextChangedListener(SimpleTextWatcher(::filter))
        sync()
    }

    private fun filter(query: String) {
        val q = query.lowercase()
        adapter.updateData(apps.filter { q.isBlank() || it.name.lowercase().contains(q) })
        sync()
    }

    private fun selectAll(s: Boolean) {
        adapter.apps.forEach { it.isSelected = s }
        adapter.notifyDataSetChanged(); sync()
    }

    private fun sync() {
        cbSelectAll.isChecked = adapter.apps.isNotEmpty() && adapter.apps.all { it.isSelected }
    }

    private fun save() {
        val selected = apps.filter { it.isSelected }.map { it.packageName }.toSet()
        preferences.saveAllowedApps(selected)
        preferences.markFirstRunDone()
        Toast.makeText(this, getString(R.string.toast_saved_apps, selected.size), Toast.LENGTH_SHORT).show()
        finish()
    }
}
