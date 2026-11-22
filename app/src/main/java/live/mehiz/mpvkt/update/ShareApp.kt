package live.mehiz.mpvkt.update

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import live.mehiz.mpvkt.BuildConfig
import live.mehiz.mpvkt.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Share the installed APK itself, so another phone can sideload the exact same build.
 *
 * The APK lives under /data/app, which [FileProvider] cannot expose directly, so it is copied
 * into `cacheDir/shared/` first — that is the `shared_apk` cache-path declared in
 * `res/xml/provider_paths.xml`. Nothing leaves the device except what the user picks in the
 * share sheet, and the copy is a normal cache file the OS can reclaim.
 *
 * Call from a coroutine: the copy is tens of megabytes and runs on [Dispatchers.IO], while the
 * share sheet itself is started back on the caller's (main) dispatcher. [context] must be an
 * Activity context — the chooser is started without `FLAG_ACTIVITY_NEW_TASK`.
 */
object ShareApp {
  private const val TAG = "ShareApp"

  suspend fun share(context: Context) {
    val name = "vela-${BuildConfig.VERSION_NAME}.apk"
    val result = withContext(Dispatchers.IO) { runCatching { copyInstalledApk(context, name) } }
    result.fold(
      onSuccess = { launchChooser(context, it) },
      onFailure = { reportFailure(context, it) },
    )
  }

  /** Copy the installed APK into the FileProvider-visible cache dir, then hand back the copy. */
  private fun copyInstalledApk(context: Context, name: String): File {
    val info = context.packageManager.getApplicationInfo(context.packageName, 0)
    val src = File(info.sourceDir)
    if (!src.isFile) error("installed APK not found at ${info.sourceDir}")
    val out = File(File(context.cacheDir, "shared"), name)
    out.parentFile?.mkdirs()
    src.copyTo(out, overwrite = true)
    Log.d(TAG, "copied ${src.length()} byte APK to ${out.absolutePath}")
    return out
  }

  private fun launchChooser(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
      type = "application/vnd.android.package-archive"
      putExtra(Intent.EXTRA_STREAM, uri)
      clipData = ClipData.newRawUri(null, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
      Intent.createChooser(send, context.getString(R.string.pref_about_share_app)),
    )
  }

  private fun reportFailure(context: Context, e: Throwable) {
    Log.e(TAG, "share app failed", e)
    val reason = e.message ?: e.javaClass.simpleName
    Toast.makeText(context, context.getString(R.string.pref_about_share_failed, reason), Toast.LENGTH_LONG).show()
  }
}
