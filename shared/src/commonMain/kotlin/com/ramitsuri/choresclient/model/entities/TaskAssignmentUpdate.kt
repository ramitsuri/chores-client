package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.model.enums.ProgressStatus
import kotlin.time.Instant

data class TaskAssignmentUpdate(
    val id: String,
    val progressStatus: ProgressStatus,
    val progressStatusDate: Instant,
    val shouldUpload: Boolean
)