package ds.meterscanner.mvvm.viewmodel

import com.evernote.android.job.JobRequest
import com.evernote.android.job.scheduledTo
import com.github.salomonbrys.kodein.Kodein
import ds.bindingtools.binding
import ds.meterscanner.mvvm.AlarmsView
import ds.meterscanner.mvvm.BindableViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmsViewModel(kodein: Kodein) : BindableViewModel(kodein) {

    var listItems: List<JobRequest> by binding(emptyList())

    init {
        fillList()
    }

    private fun fillList() {
        val alarms = scheduler.getScheduledJobs().sortedBy { it.scheduledTo() }
        listItems = alarms
    }

    fun onNewAlarm(view: AlarmsView) {
        val time = Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
        view.pickTime(time) { hours, minutes ->
            scheduler.scheduleSnapshotJob(hours, minutes)
            scheduler.saveToPrefs()
            fillList()
        }
    }

    fun onEditAlarm(view: AlarmsView, jobId: Int) {
        val time = Date(scheduler.getJob(jobId)?.scheduledTo() ?: System.currentTimeMillis())
        view.pickTime(time) { hours, minutes ->
            scheduler.clearJob(jobId)
            scheduler.scheduleSnapshotJob(hours, minutes)
            scheduler.saveToPrefs()
            fillList()
        }
    }

    fun onDeleteAlarm(jobId: Int) {
        scheduler.clearJob(jobId)
        scheduler.saveToPrefs()
        fillList()
    }
}
