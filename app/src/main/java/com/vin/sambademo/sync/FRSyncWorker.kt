package com.vin.sambademo.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class FRSyncWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        return Result.success()
    }

}