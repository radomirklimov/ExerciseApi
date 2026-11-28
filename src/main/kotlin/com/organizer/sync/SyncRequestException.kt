package com.organizer.sync

class SyncRequestException(
    val errorCode: String,
    message: String,
) : IllegalArgumentException(message)