package com.ramitsuri.choresclient.android.data

import androidx.room.TypeConverter
import com.ramitsuri.choresclient.android.model.CreateType
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import java.time.Instant

class ProgressStatusConverter {
    @TypeConverter
    fun from(value: Int): ProgressStatus {
        return ProgressStatus.fromKey(value)
    }

    @TypeConverter
    fun to(progressStatus: ProgressStatus): Int {
        return progressStatus.key
    }
}

class CreateTypeConverter {
    @TypeConverter
    fun from(value: Int): CreateType {
        return CreateType.fromKey(value)
    }

    @TypeConverter
    fun to(createType: CreateType): Int {
        return createType.key
    }
}

class RepeatUnitConverter {
    @TypeConverter
    fun from(value: Int): RepeatUnit {
        return RepeatUnit.fromKey(value)
    }

    @TypeConverter
    fun to(repeatUnit: RepeatUnit): Int {
        return repeatUnit.key
    }
}

class InstantConverter {
    @TypeConverter
    fun from(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun to(instant: Instant): Long {
        return instant.toEpochMilli()
    }
}
