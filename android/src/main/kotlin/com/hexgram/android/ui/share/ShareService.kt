package com.hexgram.android.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
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

    fun shareComposable(
        context: Context,
        width: Int,
        title: String,
        content: @Composable () -> Unit
    ) {
        val activity = context as? ComponentActivity
        if (activity == null) {
            Toast.makeText(context, "分享失败: 无法获取Activity", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

            val composeView = ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent { content() }
            }

            // Attach off-screen so it gets a Recomposer from the window
            composeView.layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            composeView.translationX = -10000f  // off-screen
            rootView.addView(composeView)

            // Post to allow compose to complete layout
            Handler(Looper.getMainLooper()).post {
                try {
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    composeView.measure(widthSpec, heightSpec)
                    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

                    val bitmap = Bitmap.createBitmap(
                        composeView.measuredWidth,
                        composeView.measuredHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    Canvas(bitmap).also { composeView.draw(it) }

                    rootView.removeView(composeView)

                    shareImage(context, bitmap, title)
                } catch (e: Exception) {
                    rootView.removeView(composeView)
                    Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
