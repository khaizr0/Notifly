package com.khaizro.notifly.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.khaizro.notifly.R
import com.khaizro.notifly.model.AppInfo

class AppAdapter(
    context: Context,
    var apps: List<AppInfo>,
    private val onSelectionChanged: () -> Unit,
) : ArrayAdapter<AppInfo>(context, 0, apps) {

    fun updateData(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun getCount(): Int = apps.size
    override fun getItem(position: Int): AppInfo = apps[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        val app = apps[position]
        val checkBox = view.findViewById<CheckBox>(R.id.appCheckBox)

        view.findViewById<TextView>(R.id.appName).text = app.name
        view.findViewById<ImageView>(R.id.appIcon).setImageDrawable(app.icon)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = app.isSelected
        checkBox.setOnCheckedChangeListener { _, checked -> setSelected(app, checked) }
        view.setOnClickListener { setSelected(app, !app.isSelected) }
        return view
    }

    private fun setSelected(app: AppInfo, selected: Boolean) {
        if (app.isSelected == selected) return
        app.isSelected = selected
        notifyDataSetChanged()
        onSelectionChanged()
    }
}
