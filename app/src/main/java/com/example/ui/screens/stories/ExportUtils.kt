package com.example.ui.screens.stories

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.local.Story
import java.io.File
import java.io.FileOutputStream

object ExportUtils {
    
    fun exportAsImage(context: Context, story: Story) {
        val width = 1080
        val bitmap = Bitmap.createBitmap(width, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val typePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6200EE")
            textSize = 40f
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 36f
        }

        var yPos = 120f
        canvas.drawText(story.type.uppercase(), 80f, yPos, typePaint)
        yPos += 80f
        
        val titleLayout = StaticLayout.Builder.obtain(story.title, 0, story.title.length, titlePaint, width - 160)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
            
        canvas.save()
        canvas.translate(80f, yPos)
        titleLayout.draw(canvas)
        canvas.restore()
        
        yPos += titleLayout.height + 60f
        
        val contentLayout = StaticLayout.Builder.obtain(story.content, 0, story.content.length, bodyPaint, width - 160)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(10f, 1.2f)
            .setIncludePad(false)
            .build()
            
        canvas.save()
        canvas.translate(80f, yPos)
        contentLayout.draw(canvas)
        canvas.restore()

        val file = File(context.cacheDir, "story_export.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        shareFile(context, file, "image/jpeg")
    }

    fun exportAsPdf(context: Context, story: Story) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        
        val canvas = page.canvas
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val typePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6200EE")
            textSize = 14f
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        var yPos = 50f
        canvas.drawText(story.type.uppercase(), 50f, yPos, typePaint)
        yPos += 30f
        
        val titleLayout = StaticLayout.Builder.obtain(story.title, 0, story.title.length, titlePaint, pageInfo.pageWidth - 100)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
            
        canvas.save()
        canvas.translate(50f, yPos)
        titleLayout.draw(canvas)
        canvas.restore()
        
        yPos += titleLayout.height + 20f
        
        val contentLayout = StaticLayout.Builder.obtain(story.content, 0, story.content.length, bodyPaint, pageInfo.pageWidth - 100)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(5f, 1.1f)
            .build()
            
        canvas.save()
        canvas.translate(50f, yPos)
        contentLayout.draw(canvas)
        canvas.restore()
        
        document.finishPage(page)
        
        val file = File(context.cacheDir, "story_export.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        
        shareFile(context, file, "application/pdf")
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Story"))
    }
}
