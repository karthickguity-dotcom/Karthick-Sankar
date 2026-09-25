package com.example.util.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.LineType
import com.example.data.model.ParsedSong
import com.example.data.model.SongSection
import com.example.data.parser.AccidentalMode
import com.example.data.parser.Transposer
import com.example.util.chord.ChordDatabase
import com.example.util.chord.InstrumentType
import java.io.File
import java.io.FileOutputStream

enum class PdfPageSize(val label: String, val width: Int, val height: Int) {
    A4("A4 (210 × 297 mm)", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 612, 792)
}

enum class PdfFontSizeMode(val label: String, val chordSize: Float, val lyricSize: Float, val lineSpacing: Float) {
    COMPACT("Compact (Fits more)", 9.5f, 10.5f, 4f),
    STANDARD("Standard", 11.5f, 12.5f, 6f),
    LARGE("Large (Music Stand)", 13.5f, 14.5f, 8f)
}

data class PdfExportOptions(
    val pageSize: PdfPageSize = PdfPageSize.A4,
    val fontSizeMode: PdfFontSizeMode = PdfFontSizeMode.STANDARD,
    val chordColor: Int = 0xFFB45309.toInt(), // Warm amber / burnt ochre for high contrast print
    val includeMetadata: Boolean = true,
    val includeFooter: Boolean = true,
    val chordDiagramInstrument: InstrumentType = InstrumentType.GUITAR
)

object PdfSongExporter {

    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_TOP = 42f
    private const val MARGIN_BOTTOM = 44f

    /**
     * Generates a beautifully formatted PDF document for sheet music printing.
     */
    fun generatePdf(
        context: Context,
        parsedSong: ParsedSong,
        songTitle: String,
        artist: String,
        originalKey: String,
        capo: Int,
        transposeSemitones: Int,
        accidentalMode: AccidentalMode,
        options: PdfExportOptions = PdfExportOptions()
    ): File {
        val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val cleanName = songTitle.replace(Regex("[^a-zA-Z0-9_.-]"), "_").ifBlank { "Song" }
        val pdfFile = File(pdfDir, "${cleanName}_sheet.pdf")

        val doc = PdfDocument()

        // Pass 1: Dry run to determine total page count
        val totalPages = renderSong(
            canvas = null,
            doc = null,
            parsedSong = parsedSong,
            songTitle = songTitle,
            artist = artist,
            originalKey = originalKey,
            capo = capo,
            transposeSemitones = transposeSemitones,
            accidentalMode = accidentalMode,
            options = options,
            totalPages = 1
        )

        // Pass 2: Actual rendering with known total pages
        renderSong(
            canvas = null,
            doc = doc,
            parsedSong = parsedSong,
            songTitle = songTitle,
            artist = artist,
            originalKey = originalKey,
            capo = capo,
            transposeSemitones = transposeSemitones,
            accidentalMode = accidentalMode,
            options = options,
            totalPages = totalPages
        )

        FileOutputStream(pdfFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return pdfFile
    }

    /**
     * Shared layout & rendering engine. When doc is null, runs dry-run pass counting pages.
     */
    private fun renderSong(
        canvas: Canvas?,
        doc: PdfDocument?,
        parsedSong: ParsedSong,
        songTitle: String,
        artist: String,
        originalKey: String,
        capo: Int,
        transposeSemitones: Int,
        accidentalMode: AccidentalMode,
        options: PdfExportOptions,
        totalPages: Int
    ): Int {
        val pageWidth = options.pageSize.width
        val pageHeight = options.pageSize.height
        val contentBottom = pageHeight - MARGIN_BOTTOM

        // Collect unique transposed chords in the song
        val uniqueChords = mutableListOf<String>()
        for (sec in parsedSong.sections) {
            for (line in sec.lines) {
                for (pair in line.pairs) {
                    pair.chord?.takeIf { it.isNotBlank() }?.let { raw ->
                        val transposed = Transposer.transposeChord(raw, transposeSemitones, accidentalMode)
                        if (!uniqueChords.contains(transposed)) {
                            uniqueChords.add(transposed)
                        }
                    }
                }
            }
        }

        // Side chords layout configuration (in the side of the page)
        val hasSideChords = options.chordDiagramInstrument != InstrumentType.NONE && uniqueChords.isNotEmpty()
        val sidebarWidth = when (options.chordDiagramInstrument) {
            InstrumentType.PIANO -> 70f
            InstrumentType.GUITAR -> 54f
            InstrumentType.BOTH -> 72f
            InstrumentType.NONE -> 0f
        }
        val gutter = if (hasSideChords) 14f else 0f
        val effectiveRightMargin = MARGIN_RIGHT + (if (hasSideChords) sidebarWidth + gutter else 0f)
        val sidebarStartX = pageWidth - MARGIN_RIGHT - sidebarWidth
        val contentWidth = pageWidth - MARGIN_LEFT - effectiveRightMargin

        // Paints
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val artistPaint = TextPaint().apply {
            color = Color.parseColor("#475569")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val metaBadgeBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val metaBadgeTextPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val chordPaint = TextPaint().apply {
            color = options.chordColor
            textSize = options.fontSizeMode.chordSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val lyricPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = options.fontSizeMode.lyricSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val commentPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = options.fontSizeMode.lyricSize * 0.92f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val sectionTagBgPaint = Paint().apply {
            color = Color.parseColor("#EEF2F6")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val sectionTagTextPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        var pageNumber = 1
        var currentPage: PdfDocument.Page? = null
        var currentCanvas: Canvas? = null

        fun startPage(): Canvas? {
            if (doc == null) return null
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = doc.startPage(pageInfo)
            currentCanvas = currentPage?.canvas
            return currentCanvas
        }

        // Draw Side Chords in the side column of the page
        fun drawSideChords(canvas: Canvas?) {
            if (canvas == null || !hasSideChords) return

            // Vertical divider between lyrics and side chords
            val dividerX = sidebarStartX - (gutter / 2f)
            canvas.drawLine(dividerX, MARGIN_TOP, dividerX, pageHeight - MARGIN_BOTTOM, dividerPaint)

            // Header badge for side chords
            var sideY = MARGIN_TOP
            val headerTitle = when (options.chordDiagramInstrument) {
                InstrumentType.PIANO -> "PIANO"
                InstrumentType.GUITAR -> "GUITAR"
                InstrumentType.BOTH -> "CHORDS"
                InstrumentType.NONE -> ""
            }

            val badgeHeight = 15f
            canvas.drawRoundRect(
                sidebarStartX,
                sideY,
                sidebarStartX + sidebarWidth,
                sideY + badgeHeight,
                4f,
                4f,
                metaBadgeBgPaint
            )
            val sideHeaderPaint = TextPaint().apply {
                color = Color.parseColor("#475569")
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(headerTitle, sidebarStartX + sidebarWidth / 2f, sideY + 10.5f, sideHeaderPaint)
            sideY += badgeHeight + 8f

            val isPiano = options.chordDiagramInstrument == InstrumentType.PIANO
            val isGuitar = options.chordDiagramInstrument == InstrumentType.GUITAR
            val isBoth = options.chordDiagramInstrument == InstrumentType.BOTH

            for (chord in uniqueChords) {
                val neededHeight = when {
                    isBoth -> 82f
                    isGuitar -> 56f
                    isPiano -> 40f
                    else -> 0f
                }
                if (sideY + neededHeight > pageHeight - MARGIN_BOTTOM - 6f) {
                    break // fits neatly down the side
                }

                if (isPiano) {
                    val pW = sidebarWidth.coerceAtMost(66f)
                    val pH = 34f
                    val px = sidebarStartX + (sidebarWidth - pW) / 2f
                    drawPianoDiagramPdf(canvas, px, sideY, chord, pW, pH, options.chordColor, accidentalMode)
                    sideY += pH + 10f
                } else if (isGuitar) {
                    val gW = 46f
                    val gH = 50f
                    val gx = sidebarStartX + (sidebarWidth - gW) / 2f
                    drawGuitarDiagramPdf(canvas, gx, sideY, chord, gW, gH, options.chordColor)
                    sideY += gH + 10f
                } else if (isBoth) {
                    // Both Guitar and Piano in the side of the page (Piano first, then Guitar)
                    val pW = sidebarWidth.coerceAtMost(66f)
                    val pH = 30f
                    val px = sidebarStartX + (sidebarWidth - pW) / 2f
                    drawPianoDiagramPdf(canvas, px, sideY, chord, pW, pH, options.chordColor, accidentalMode)
                    sideY += pH + 4f

                    val gW = 44f
                    val gH = 44f
                    val gx = sidebarStartX + (sidebarWidth - gW) / 2f
                    drawGuitarDiagramPdf(canvas, gx, sideY, chord, gW, gH, options.chordColor)
                    sideY += gH + 8f
                }
            }
        }

        fun finishCurrentPage() {
            if (doc != null && currentPage != null && currentCanvas != null) {
                // Draw side chords on the page
                drawSideChords(currentCanvas)

                // Draw footer before finishing
                if (options.includeFooter) {
                    val footerY = pageHeight - MARGIN_BOTTOM + 22f
                    // Left: Song Title
                    currentCanvas?.drawText(songTitle, MARGIN_LEFT, footerY, footerPaint)

                    // Center: Page number
                    val pageText = "Page $pageNumber of $totalPages"
                    val pageTextWidth = footerPaint.measureText(pageText)
                    val centerX = (pageWidth - pageTextWidth) / 2f
                    currentCanvas?.drawText(pageText, centerX, footerY, footerPaint)

                    // Right: App info & Creator
                    val rightText = "ChordPro Stage Pro • Karthick David"
                    val rightWidth = footerPaint.measureText(rightText)
                    currentCanvas?.drawText(rightText, pageWidth - MARGIN_RIGHT - rightWidth, footerY, footerPaint)

                    // Top footer divider line
                    currentCanvas?.drawLine(MARGIN_LEFT, pageHeight - MARGIN_BOTTOM + 8f, pageWidth - MARGIN_RIGHT, pageHeight - MARGIN_BOTTOM + 8f, dividerPaint)
                }
                doc.finishPage(currentPage)
                currentPage = null
                currentCanvas = null
            }
        }

        currentCanvas = startPage()
        var currentY = MARGIN_TOP

        // Draw Header on Page 1
        fun drawHeader() {
            if (!options.includeMetadata) return

            // Song Title
            currentCanvas?.drawText(songTitle, MARGIN_LEFT, currentY + 16f, titlePaint)
            currentY += 24f

            // Artist (if present)
            if (artist.isNotBlank()) {
                currentCanvas?.drawText(artist, MARGIN_LEFT, currentY + 10f, artistPaint)
                currentY += 16f
            }

            currentY += 6f

            // Metadata Badges (Key, Capo, Tempo, Accidental Mode)
            val transposedKey = if (originalKey.isNotBlank()) {
                val keyName = Transposer.transposeKey(originalKey, transposeSemitones, accidentalMode)
                if (transposeSemitones != 0) {
                    val sign = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones"
                    "$keyName ($sign)"
                } else {
                    keyName
                }
            } else ""

            val badges = mutableListOf<String>()
            if (transposedKey.isNotBlank()) badges.add("Key: $transposedKey")
            if (capo > 0) badges.add("Capo: $capo")
            if (parsedSong.tempo.isNotBlank()) badges.add("Tempo: ${parsedSong.tempo}")
            if (parsedSong.timeSignature.isNotBlank()) badges.add("Time: ${parsedSong.timeSignature}")
            badges.add("Mode: ${accidentalMode.label}")

            var badgeX = MARGIN_LEFT
            val badgeHeight = 18f
            val badgePadding = 8f

            for (badge in badges) {
                val textWidth = metaBadgeTextPaint.measureText(badge)
                val badgeWidth = textWidth + (badgePadding * 2)

                if (badgeX + badgeWidth > pageWidth - effectiveRightMargin) {
                    badgeX = MARGIN_LEFT
                    currentY += badgeHeight + 4f
                }

                currentCanvas?.drawRoundRect(
                    badgeX,
                    currentY,
                    badgeX + badgeWidth,
                    currentY + badgeHeight,
                    6f,
                    6f,
                    metaBadgeBgPaint
                )
                currentCanvas?.drawText(
                    badge,
                    badgeX + badgePadding,
                    currentY + 12.5f,
                    metaBadgeTextPaint
                )

                badgeX += badgeWidth + 8f
            }

            currentY += badgeHeight + 12f

            // Header horizontal divider
            currentCanvas?.drawLine(MARGIN_LEFT, currentY, pageWidth - effectiveRightMargin, currentY, dividerPaint)
            currentY += 14f
        }

        drawHeader()

        val chordH = options.fontSizeMode.chordSize
        val lyricH = options.fontSizeMode.lyricSize
        val lineSpacing = options.fontSizeMode.lineSpacing

        for (section in parsedSong.sections) {
            // Check if section header fits
            val hasSectionTitle = section.title.isNotBlank()
            val sectionHeaderHeight = if (hasSectionTitle) 26f else 0f

            if (currentY + sectionHeaderHeight + (chordH + lyricH + lineSpacing) > contentBottom) {
                finishCurrentPage()
                pageNumber++
                currentCanvas = startPage()
                currentY = MARGIN_TOP
            }

            // Draw Section Title (e.g. "[CHORUS]", "[VERSE 1]")
            if (hasSectionTitle) {
                val titleUpper = section.title.uppercase()
                val textWidth = sectionTagTextPaint.measureText(titleUpper)
                val tagWidth = textWidth + 16f
                val tagHeight = 18f

                currentCanvas?.drawRoundRect(
                    MARGIN_LEFT,
                    currentY,
                    MARGIN_LEFT + tagWidth,
                    currentY + tagHeight,
                    4f,
                    4f,
                    sectionTagBgPaint
                )
                currentCanvas?.drawText(
                    titleUpper,
                    MARGIN_LEFT + 8f,
                    currentY + 12.5f,
                    sectionTagTextPaint
                )

                currentY += tagHeight + 8f
            }

            // Lines in this section
            for (line in section.lines) {
                when (line.type) {
                    LineType.BLANK -> {
                        currentY += 10f
                    }
                    LineType.COMMENT -> {
                        val commentH = lyricH + 4f
                        if (currentY + commentH > contentBottom) {
                            finishCurrentPage()
                            pageNumber++
                            currentCanvas = startPage()
                            currentY = MARGIN_TOP
                        }
                        currentCanvas?.drawText(
                            line.text,
                            MARGIN_LEFT + 12f,
                            currentY + lyricH,
                            commentPaint
                        )
                        currentY += commentH
                    }
                    LineType.SECTION_HEADER -> {
                        val headH = 22f
                        if (currentY + headH > contentBottom) {
                            finishCurrentPage()
                            pageNumber++
                            currentCanvas = startPage()
                            currentY = MARGIN_TOP
                        }
                        currentCanvas?.drawText(
                            line.text.uppercase(),
                            MARGIN_LEFT,
                            currentY + 14f,
                            sectionTagTextPaint
                        )
                        currentY += headH
                    }
                    LineType.CHORD_LYRIC -> {
                        val hasChords = line.pairs.any { !it.chord.isNullOrBlank() }
                        val rowHeight = if (hasChords) (chordH + lyricH + lineSpacing + 2f) else (lyricH + lineSpacing)

                        if (currentY + rowHeight > contentBottom) {
                            finishCurrentPage()
                            pageNumber++
                            currentCanvas = startPage()
                            currentY = MARGIN_TOP
                        }

                        var x = MARGIN_LEFT
                        val chordBaselineY = currentY + chordH
                        val lyricBaselineY = if (hasChords) (currentY + chordH + lyricH + 2f) else (currentY + lyricH)

                        for (pair in line.pairs) {
                            val rawChord = pair.chord?.trim()
                            val transposedChord = if (!rawChord.isNullOrBlank()) {
                                Transposer.transposeChord(rawChord, transposeSemitones, accidentalMode)
                            } else null

                            val chordW = if (transposedChord != null) chordPaint.measureText(transposedChord) + 6f else 0f
                            val lyricW = lyricPaint.measureText(pair.lyric)
                            val pairW = maxOf(chordW, lyricW)

                            // Wrap to next line if exceeds right margin
                            if (x + pairW > pageWidth - effectiveRightMargin && x > MARGIN_LEFT) {
                                currentY += rowHeight
                                if (currentY + rowHeight > contentBottom) {
                                    finishCurrentPage()
                                    pageNumber++
                                    currentCanvas = startPage()
                                    currentY = MARGIN_TOP
                                }
                                x = MARGIN_LEFT
                            }

                            if (transposedChord != null) {
                                currentCanvas?.drawText(transposedChord, x, chordBaselineY, chordPaint)
                            }
                            if (pair.lyric.isNotEmpty()) {
                                currentCanvas?.drawText(pair.lyric, x, lyricBaselineY, lyricPaint)
                            }

                            x += pairW
                        }

                        currentY += rowHeight
                    }
                }
            }

            // Small gap between sections
            currentY += 8f
        }

        // Finish the last page
        finishCurrentPage()

        return pageNumber
    }

    /**
     * Opens the native Android Print Spooler print dialog for the generated PDF.
     */
    fun printDocument(
        context: Context,
        pdfFile: File,
        jobName: String = "ChordPro Document"
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            val adapter = PdfPrintDocumentAdapter(pdfFile, jobName)
            val printAttributes = PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()
            printManager.print(jobName, adapter, printAttributes)
        }
    }

    /**
     * Shares the generated PDF file via standard Android share sheet (Email, Drive, WhatsApp, etc.).
     */
    fun sharePdf(
        context: Context,
        pdfFile: File,
        songTitle: String
    ) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$songTitle - Sheet Music")
            putExtra(Intent.EXTRA_TEXT, "Exported sheet music for '$songTitle' from ChordPro Stage Pro.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Share or Print PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Opens the PDF directly in a PDF viewer app on the device.
     */
    fun openPdf(
        context: Context,
        pdfFile: File
    ) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(viewIntent, "Open PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun drawGuitarDiagramPdf(
        canvas: Canvas,
        x: Float,
        y: Float,
        chordName: String,
        width: Float,
        height: Float,
        chordColor: Int
    ) {
        val voicing = ChordDatabase.getGuitarChord(chordName)
        val titlePaint = TextPaint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(chordName, x + width / 2f, y + 9f, titlePaint)

        val gridTop = y + 15f
        val gridBottom = y + height - 3f
        val gridLeft = x + 8f
        val gridRight = x + width - 4f
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        val numStrings = 6
        val numFrets = 4
        val stringSpacing = gridW / (numStrings - 1)
        val fretSpacing = gridH / numFrets

        val stringPaint = Paint().apply {
            color = 0xFF64748B.toInt()
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        val nutPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            strokeWidth = if (voicing.baseFret == 1) 2.4f else 1f
            isAntiAlias = true
        }
        val dotPaint = Paint().apply {
            color = chordColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textMarkerPaint = Paint().apply {
            color = 0xFF475569.toInt()
            textSize = 7f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        if (voicing.baseFret > 1) {
            val fretTextPaint = Paint().apply {
                color = 0xFF475569.toInt()
                textSize = 6f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText("${voicing.baseFret}fr", x + 1f, gridTop + fretSpacing * 0.7f, fretTextPaint)
        }

        // Nut and Frets
        canvas.drawLine(gridLeft, gridTop, gridRight, gridTop, nutPaint)
        for (f in 1..numFrets) {
            val fy = gridTop + f * fretSpacing
            canvas.drawLine(gridLeft, fy, gridRight, fy, stringPaint)
        }

        // Strings
        for (s in 0 until numStrings) {
            val sx = gridLeft + s * stringSpacing
            canvas.drawLine(sx, gridTop, sx, gridBottom, stringPaint)
        }

        // Barres
        voicing.barres.forEach { bFret ->
            if (bFret in 1..numFrets) {
                val by = gridTop + (bFret - 0.5f) * fretSpacing
                val barreRect = RectF(gridLeft - 1.5f, by - 2.5f, gridRight + 1.5f, by + 2.5f)
                canvas.drawRoundRect(barreRect, 2f, 2f, dotPaint)
            }
        }

        // Markers & Dots
        for (s in 0 until numStrings) {
            val sx = gridLeft + s * stringSpacing
            val fVal = voicing.frets.getOrNull(s) ?: -1
            when {
                fVal == -1 -> canvas.drawText("×", sx, gridTop - 2.5f, textMarkerPaint)
                fVal == 0 -> {
                    val openCirclePaint = Paint().apply {
                        color = 0xFF475569.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = 0.8f
                        isAntiAlias = true
                    }
                    canvas.drawCircle(sx, gridTop - 4f, 2f, openCirclePaint)
                }
                fVal in 1..numFrets -> {
                    val isCoveredByBarre = voicing.barres.contains(fVal)
                    if (!isCoveredByBarre) {
                        val dy = gridTop + (fVal - 0.5f) * fretSpacing
                        canvas.drawCircle(sx, dy, stringSpacing * 0.36f, dotPaint)
                    }
                }
            }
        }
    }

    private fun drawPianoDiagramPdf(
        canvas: Canvas,
        x: Float,
        y: Float,
        chordName: String,
        width: Float,
        height: Float,
        chordColor: Int,
        accidentalMode: AccidentalMode
    ) {
        val voicing = ChordDatabase.getPianoChord(chordName, accidentalMode)
        val titlePaint = TextPaint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(chordName, x + width / 2f, y + 9f, titlePaint)

        val keyTop = y + 13f
        val keyBottom = y + height - 2f
        val keyH = keyBottom - keyTop
        val numWhiteKeys = 14
        val whiteKeyW = (width - 4f) / numWhiteKeys
        val startX = x + 2f

        val whiteKeySemitones = intArrayOf(0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23)
        val whiteKeyPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val whiteKeyStroke = Paint().apply {
            color = 0xFF94A3B8.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            isAntiAlias = true
        }
        val pressedWhitePaint = Paint().apply {
            color = 0xFFE2E8F0.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val dotPaint = Paint().apply {
            color = chordColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw White Keys
        for (i in 0 until numWhiteKeys) {
            val kx = startX + i * whiteKeyW
            val semi = whiteKeySemitones[i]
            val isPressed = voicing.semitoneOffsets.contains(semi)
            canvas.drawRect(kx, keyTop, kx + whiteKeyW, keyBottom, if (isPressed) pressedWhitePaint else whiteKeyPaint)
            canvas.drawRect(kx, keyTop, kx + whiteKeyW, keyBottom, whiteKeyStroke)
            if (isPressed) {
                canvas.drawCircle(kx + whiteKeyW / 2f, keyBottom - 4.5f, 2.2f, dotPaint)
            }
        }

        // Draw Black Keys
        val blackKeyW = whiteKeyW * 0.6f
        val blackKeyH = keyH * 0.6f
        val blackKeyPaint = Paint().apply {
            color = 0xFF1E293B.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val blackKeys = listOf(
            Pair(0, 1), Pair(1, 3), Pair(3, 6), Pair(4, 8), Pair(5, 10),
            Pair(7, 13), Pair(8, 15), Pair(10, 18), Pair(11, 20), Pair(12, 22)
        )
        blackKeys.forEach { (wIdx, semi) ->
            val isPressed = voicing.semitoneOffsets.contains(semi)
            val bkX = startX + (wIdx + 1) * whiteKeyW - blackKeyW / 2f
            canvas.drawRect(bkX, keyTop, bkX + blackKeyW, keyTop + blackKeyH, if (isPressed) dotPaint else blackKeyPaint)
        }
    }
}
