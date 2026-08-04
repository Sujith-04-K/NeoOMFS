package com.simats.neoomfs.models

data class ReviewStatusRequest(
    val status: String,           // PENDING_REVIEW | APPROVED | NEEDS_REVISION
    val reviewComments: String?
)
