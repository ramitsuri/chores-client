package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.model.enums.ProgressStatus
import kotlinx.datetime.Instant

data class TaskAssignmentUpdate(
    val id: String,
    val progressStatus: ProgressStatus,
    val progressStatusDate: Instant,
    val shouldUpload: Boolean
)