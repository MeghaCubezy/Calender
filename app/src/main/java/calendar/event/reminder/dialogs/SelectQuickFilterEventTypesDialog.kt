package calendar.event.reminder.dialogs

import androidx.appcompat.app.AlertDialog
import calendar.event.reminder.Activity.SimpleActivity
import calendar.event.reminder.Adapter.FilterEventTypeAdapter
import calendar.event.reminder.Extensions.config
import calendar.event.reminder.Extensions.eventsHelper
import calendar.event.reminder.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_filter_event_types.view.*

class SelectQuickFilterEventTypesDialog(val activity: SimpleActivity) {
    private lateinit var dialog: AlertDialog
    private val view = activity.layoutInflater.inflate(R.layout.dialog_filter_event_types, null)

    init {
        activity.eventsHelper.getEventTypes(activity, false) {
            val quickFilterEventTypes = activity.config.quickFilterEventTypes
            view.filter_event_types_list.adapter =
                FilterEventTypeAdapter(activity, it, quickFilterEventTypes)

            dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmEventTypes() }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
        }
    }

    private fun confirmEventTypes() {
        val selectedItems =
            (view.filter_event_types_list.adapter as FilterEventTypeAdapter).getSelectedItemsList()
                .map { it.toString() }.toHashSet()
        if (activity.config.quickFilterEventTypes != selectedItems) {
            activity.config.quickFilterEventTypes = selectedItems
        }
        dialog.dismiss()
    }
}
