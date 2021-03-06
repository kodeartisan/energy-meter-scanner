package ds.meterscanner.mvvm.viewmodel

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import ds.bindingtools.binding
import ds.meterscanner.R
import ds.meterscanner.db.model.Snapshot
import ds.meterscanner.mvvm.BindableViewModel
import ds.meterscanner.mvvm.DetailsView
import ds.meterscanner.util.formatTimeDate
import kotlinx.coroutines.experimental.delay
import java.util.*

class DetailsViewModel(
    kodein: Kodein,
    private val snapshotId: String? = null
) : BindableViewModel(kodein) {

    val valueError = Validator(::value) { value ->
        try {
            snapshot.value = value.toDouble()
            ""
        } catch (e: Exception) {
            getString(R.string.invalid_data)
        }
    }
    var value: String by binding("0")
    var date: String by binding()
    var outsideTemp: String by binding()
    var boilerTemp: String by binding()
    var imageUrl: String by binding()
    var toolbarTitle: String by binding()

    lateinit var snapshot: Snapshot
    private val calendar: Calendar = instance()

    init {
        toolbarTitle = if (snapshotId != null) {
            getString(R.string.edit_snapshot)
        } else {
            getString(R.string.new_snapshot)
        }

        fetchSnapshot()
    }

    private fun fetchSnapshot() = async {
        delay(100)
        snapshot = if (snapshotId != null)
            db.getSnapshotById(snapshotId)
        else
            Snapshot(boilerTemp = prefs.boilerTemp, outsideTemp = prefs.currentTemperature.toInt())

        if (snapshot.value != 0.0)
            value = snapshot.value.toString()
        imageUrl = snapshot.image ?: ""
        date = formatTimeDate(snapshot.timestamp)
        outsideTemp = if (snapshot.outsideTemp != null) snapshot.outsideTemp.toString() else ""
        boilerTemp = snapshot.boilerTemp.toString()
    }

    fun onSave(view: DetailsView) {
        valueError.validate() || return
        snapshot.value = value.toDouble()
        if (boilerTemp.isNotEmpty())
            snapshot.boilerTemp = boilerTemp.toInt()

        if (outsideTemp.isNotEmpty())
            snapshot.outsideTemp = outsideTemp.toInt()
        else
            snapshot.outsideTemp = null

        db.saveSnapshot(snapshot)

        view.finish()
    }

    fun onDatePicked(date: Date) {
        snapshot.timestamp = date.time
        this.date = formatTimeDate(date.time)
    }

    fun truncDate(): Date {
        calendar.time = Date(snapshot.timestamp)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.time
    }

    fun onDatePick(view: DetailsView) = view.pickDate()

}
