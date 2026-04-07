package com.hexgram.android.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.io.File
import java.io.FileOutputStream

object ShareService {

    fun shareImage(context: Context, bitmap: Bitmap, title: String) {
        val dir = File(context.cacheDir, "shared_images")
        dir.mkdirs()
        val file = File(dir, "${title}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享$title"))
    }

    fun captureBitmap(
        context: Context,
        width: Int,
        content: @Composable () -> Unit
    ): Bitmap {
        val activity = context as? ComponentActivity
            ?: throw IllegalStateException("Context is not a ComponentActivity")

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent { content() }
        }

        composeView.setViewTreeLifecycleOwner(activity)
        composeView.setViewTreeSavedStateRegistryOwner(activity)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            composeView.measuredWidth,
            composeView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        return bitmap
    }

    fun shareComposable(
        context: Context,
        width: Int,
        title: String,
        content: @Composable () -> Unit
    ) {
        try {
            val bitmap = captureBitmap(context, width, content)
            shareImage(context, bitmap, title)
        } catch (e: Exception) {
            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
