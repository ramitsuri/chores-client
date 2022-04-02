package com.ramitsuri.choresclient.android.ui.assigments

import android.os.Parcel
import android.os.Parcelable
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.TaskAssignment

data class AssignmentDetails(
    val id: String,
    val name: String,
    val description: String,
    val repeatValue: Int,
    val repeatUnit: RepeatUnit
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        RepeatUnit.fromKey(parcel.readInt())
    )

    constructor(taskAssignment: TaskAssignment) : this(
        id = taskAssignment.id,
        name = taskAssignment.task.name,
        description = taskAssignment.task.description,
        repeatValue = taskAssignment.task.repeatValue,
        repeatUnit = taskAssignment.task.repeatUnit
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeInt(repeatValue)
        dest.writeInt(repeatUnit.key)
    }

    companion object CREATOR : Parcelable.Creator<AssignmentDetails> {
        override fun createFromParcel(parcel: Parcel): AssignmentDetails {
            return AssignmentDetails(parcel)
        }

        override fun newArray(size: Int): Array<AssignmentDetails?> {
            return arrayOfNulls(size)
        }
    }
}