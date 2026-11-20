package com.moviebox.app.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

data class DlItem(val id: Long, val title: String, val status: Int, val done: Long, val total: Long, val mime: String?) {
    val active get() = status != DownloadManager.STATUS_SUCCESSFUL
}

/** Reads every download this app started through Android's DownloadManager. */
fun queryDownloads(ctx: Context): List<DlItem> {
    val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val out = mutableListOf<DlItem>()
    dm.query(DownloadManager.Query()).use { c ->
        val id = c.getColumnIndex(DownloadManager.COLUMN_ID)
        val title = c.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val st = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val done = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val total = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        val mime = c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
        while (c.moveToNext()) {
            out.add(DlItem(c.getLong(id), c.getString(title) ?: "Download", c.getInt(st), c.getLong(done), c.getLong(total), c.getString(mime)))
        }
    }
    return out
}

fun formatBytes(b: Long): String = when {
    b >= 1_073_741_824 -> "%.1f GB".format(b / 1_073_741_824.0)
    b >= 1_048_576 -> "%.1f MB".format(b / 1_048_576.0)
    b > 0 -> "%.0f KB".format(b / 1024.0)
    else -> "0 KB"
}

fun enqueueDownload(ctx: Context, url: String, name: String) {
    try {
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setMimeType("video/mp4")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, name)
        (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
        Toast.makeText(ctx, "Download started", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(ctx, "Could not start download", Toast.LENGTH_SHORT).show()
    }
}
