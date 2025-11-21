package com.stqf.academy

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.stqf.academy.view.DrawingView
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.stqf.academy.databinding.ActivityPdfViewerBinding
import java.io.File
import kotlin.math.*

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private var totalPages = 0

    private var isToolbarVisible = false

    private var lastClickTimePen = 0L
    private var lastClickTimePenSoft = 0L
    private var lastClickTimeHighlighter = 0L
    private var lastClickTimeShape = 0L

    private enum class Tool { NONE, PEN, PEN_SOFT, HIGHLIGHTER, SHAPE, ERASER }
    private enum class ShapeKind { RECT, OVAL, ARROW }

    private var currentTool: Tool = Tool.NONE
    private var shapeKind: ShapeKind = ShapeKind.RECT

    // ===== Pref keys =====
    private val PREFS = "drawing_prefs"
    private val KEY_PEN_WIDTH = "pen_width"
    private val KEY_HL_WIDTH = "hl_width"
    private val KEY_PEN_COLOR = "pen_color"
    private val KEY_HL_COLOR = "hl_color"
    private val KEY_SOFT_PEN_WIDTH = "soft_pen_width"
    private val KEY_SOFT_PEN_COLOR = "soft_pen_color"
    private val KEY_SHAPE_WIDTH = "shape_width"
    private val KEY_SHAPE_COLOR = "shape_color"

    // ===== Tool state =====
    private var penWidth: Float = 6f
    private var penColor: Int = Color.RED

    private var softPenWidth: Float = 6f
    private var softPenColor: Int = Color.RED
    private val SOFT_PEN_ALPHA = 150 // নরম/হালকা

    private var highlighterWidth: Float = 20f
    private var highlighterColor: Int = Color.YELLOW   // palette থেকে সেট হবে

    private var shapeStrokeWidth: Float = 6f
    private var shapeColor: Int = Color.RED

    // ---------- last page persistence ----------
    private val PREFS_LAST_PAGE = "reading_state_prefs"
    private var currentBookId: String = ""
    private fun makeBookId(path: String, isFile: Boolean): String =
        if (isFile) "file::$path" else "asset::$path"
    private fun getLastPage(bookId: String): Int =
        getSharedPreferences(PREFS_LAST_PAGE, Context.MODE_PRIVATE).getInt("last_page_$bookId", 0)
    private fun saveLastPage(bookId: String, page0Based: Int) {
        getSharedPreferences(PREFS_LAST_PAGE, Context.MODE_PRIVATE)
            .edit().putInt("last_page_$bookId", page0Based).apply()
    }

    // ===================== Annotation state =====================
    private val pageBoundsInView = mutableMapOf<Int, RectF>()
    private val pageSizeMap = mutableMapOf<Int, Pair<Float, Float>>() // pageW,pageH

    data class RelPoint(val x: Float, val y: Float)
    data class RelPath(
        val strokeWidth: Float,
        val color: Int,
        val alpha: Int = 255,
        val points: MutableList<RelPoint> = mutableListOf(),
        val isMultiply: Boolean = false   // <-- Soft Pen হলে true
    )
    data class RelRect(
        val left: Float, val top: Float,
        val right: Float, val bottom: Float,
        val color: Int,
        val alpha: Int = 145,           // default lighter highlight
        val isOval: Boolean = false,
        val outlineOnly: Boolean = false,
        val strokeWidth: Float = 4f
    )
    data class RelArrow(
        val sx: Float, val sy: Float,
        val ex: Float, val ey: Float,
        val color: Int,
        val strokeWidth: Float,
        val alpha: Int = 255
    )

    private val highlightsRel = mutableMapOf<Int, MutableList<RelRect>>()   // filled HL + outline rect/oval
    private val penStrokesRel = mutableMapOf<Int, MutableList<RelPath>>()
    private val arrowsRel = mutableMapOf<Int, MutableList<RelArrow>>()

    // ===== Paints =====

    // HIGHLIGHT: MULTIPLY so text stays crisp
    private val highlightFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFEB3B.toInt()   // warm soft yellow
        alpha = 145
        @Suppress("DEPRECATION")
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        if (Build.VERSION.SDK_INT >= 29) {
            xfermode = null
            blendMode = android.graphics.BlendMode.MULTIPLY
        }
    }

    private val shapeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 4f
        color = Color.RED
        alpha = 255
        pathEffect = null // solid
    }

    private val penPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 6f
        color = Color.BLACK
        alpha = 255
    }

    private val arrowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.MITER
        strokeWidth = 6f
        color = Color.RED
        alpha = 255
    }
    private val arrowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
        alpha = 255
    }

    // --- outline + preview for FB-style look ---
    private val arrowOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
        alpha = 200
        strokeWidth = 0f
    }

    // --- Live preview paints ---
    private val PREVIEW_HL_ALPHA = 110 // preview a bit lighter
    private val previewFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFEB3B.toInt()
        alpha = PREVIEW_HL_ALPHA
        @Suppress("DEPRECATION")
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        if (Build.VERSION.SDK_INT >= 29) {
            xfermode = null
            blendMode = android.graphics.BlendMode.MULTIPLY
        }
    }
    private val previewStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.YELLOW
        pathEffect = null
    }

    private var isSelectingRect = false
    private var startRelX = 0f
    private var startRelY = 0f
    private var endRelX = 0f
    private var endRelY = 0f
    private var selectingRectPage: Int? = null

    // ===== Undo/Redo =====
    private sealed class Action {
        data class AddPen(val page: Int, val path: RelPath): Action()
        data class AddHighlight(val page: Int, val rect: RelRect): Action()
        data class AddArrow(val page: Int, val arrow: RelArrow): Action()
    }
    private val undoStack = ArrayDeque<Action>()
    private val redoStack = ArrayDeque<Action>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPrefs()
        binding.floatingPen.visibility = View.GONE

        binding.drawingToolbar.bringToFront()
        binding.colorPalette.bringToFront()
        binding.floatingPen.bringToFront()
        binding.pageNumberText.bringToFront()

        // DrawingView → Activity
        binding.drawingView.setEventSink(object : DrawingView.EventSink {
            override fun onPenDown(x: Float, y: Float) {
                if (currentTool == Tool.NONE) return
                val page = pageUnder(x, y) ?: return
                val rect = pageBoundsInView[page] ?: return
                val rx = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)
                val ry = ((y - rect.top) / rect.height()).coerceIn(0f, 1f)

                when (currentTool) {
                    Tool.PEN, Tool.PEN_SOFT -> {
                        val list = penStrokesRel.getOrPut(page) { mutableListOf() }

                        val (c, w, a, mul) =
                            if (currentTool == Tool.PEN_SOFT)
                                Quad(softPenColor, softPenWidth, SOFT_PEN_ALPHA, true)
                            else
                                Quad(penColor, penWidth, 255, false)

                        val path = RelPath(
                            strokeWidth = w,
                            color = c,
                            alpha = a,
                            points = mutableListOf(RelPoint(rx, ry)),
                            isMultiply = mul
                        )
                        list.add(path)
                        pushAction(Action.AddPen(page, path))
                    }
                    Tool.HIGHLIGHTER, Tool.SHAPE -> {
                        selectingRectPage = page
                        startRelX = rx; startRelY = ry
                        endRelX = rx; endRelY = ry
                        isSelectingRect = true
                    }
                    Tool.ERASER -> eraseAt(page, rx, ry)
                    else -> {}
                }
                binding.pdfView.invalidate()
            }

            override fun onPenMove(x: Float, y: Float) {
                if (currentTool == Tool.NONE) return
                when (currentTool) {
                    Tool.PEN, Tool.PEN_SOFT -> {
                        val page = pageUnder(x, y) ?: return
                        val rect = pageBoundsInView[page] ?: return
                        val rx = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)
                        val ry = ((y - rect.top) / rect.height()).coerceIn(0f, 1f)
                        penStrokesRel[page]?.lastOrNull()?.points?.add(RelPoint(rx, ry))
                    }
                    Tool.HIGHLIGHTER, Tool.SHAPE -> {
                        if (!isSelectingRect) return
                        val page = selectingRectPage ?: return
                        val rect = pageBoundsInView[page] ?: return
                        val rx = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)
                        val ry = ((y - rect.top) / rect.height()).coerceIn(0f, 1f)
                        endRelX = rx; endRelY = ry
                    }
                    Tool.ERASER -> {
                        val page = pageUnder(x, y) ?: return
                        val rect = pageBoundsInView[page] ?: return
                        val rx = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)
                        val ry = ((y - rect.top) / rect.height()).coerceIn(0f, 1f)
                        eraseAt(page, rx, ry)
                    }
                    else -> {}
                }
                binding.pdfView.invalidate()
            }

            override fun onPenUp() {
                if ((currentTool == Tool.HIGHLIGHTER || currentTool == Tool.SHAPE) && isSelectingRect) {
                    isSelectingRect = false
                    val page = selectingRectPage ?: return

                    if (currentTool == Tool.SHAPE && shapeKind == ShapeKind.ARROW) {
                        // Arrow: start→end
                        val arr = RelArrow(
                            sx = startRelX, sy = startRelY,
                            ex = endRelX, ey = endRelY,
                            color = shapeColor,
                            strokeWidth = shapeStrokeWidth,
                            alpha = 255
                        )
                        arrowsRel.getOrPut(page) { mutableListOf() }.add(arr)
                        pushAction(Action.AddArrow(page, arr))
                    } else {
                        val (lRel, tRel, rRel, bRel) =
                            if (currentTool == Tool.SHAPE && shapeKind == ShapeKind.OVAL) {
                                val (pw, ph) = pageSizeMap[page]
                                    ?: Pair(pageBoundsInView[page]?.width() ?: 1f, pageBoundsInView[page]?.height() ?: 1f)
                                squareFromDragIsotropic(startRelX, startRelY, endRelX, endRelY, pw, ph)
                            } else {
                                rectFromDrag(startRelX, startRelY, endRelX, endRelY)
                            }

                        val w = abs(rRel - lRel)
                        val h = abs(bRel - tRel)
                        if (w > 0.01f && h > 0.01f) {
                            val list = highlightsRel.getOrPut(page) { mutableListOf() }
                            val rect = if (currentTool == Tool.SHAPE) {
                                RelRect(
                                    lRel, tRel, rRel, bRel,
                                    color = shapeColor,
                                    alpha = 255,
                                    isOval = (shapeKind == ShapeKind.OVAL),
                                    outlineOnly = true,
                                    strokeWidth = shapeStrokeWidth
                                )
                            } else {
                                // HIGHLIGHTER: alpha 145 so it's lighter
                                RelRect(
                                    lRel, tRel, rRel, bRel,
                                    color = highlighterColor,
                                    alpha = 145,
                                    isOval = false,
                                    outlineOnly = false,
                                    strokeWidth = 0f
                                )
                            }
                            list.add(rect)
                            pushAction(Action.AddHighlight(page, rect))
                        }
                    }
                    selectingRectPage = null
                    binding.pdfView.invalidate()
                }
            }
        })

        // ===================== PDF load =====================
        val filePath = intent.getStringExtra("pdf_file") ?: return
        val lastPage: Int
        if (filePath.startsWith("/")) {
            val file = File(filePath)
            currentBookId = makeBookId(file.absolutePath, isFile = true)
            lastPage = getLastPage(currentBookId)
            binding.pdfView.fromFile(file)
                .enableSwipe(true).swipeHorizontal(false).enableDoubletap(true)
                .pageFitPolicy(FitPolicy.WIDTH).defaultPage(lastPage)
                .onPageChange(onPageChangeListener).onTap(tapListener)
                .onDrawAll { canvas, pageW, pageH, pageIndex -> drawAnnotations(canvas, pageW, pageH, pageIndex) }
                .load()
        } else {
            currentBookId = makeBookId(filePath, isFile = false)
            lastPage = getLastPage(currentBookId)
            binding.pdfView.fromAsset(filePath)
                .enableSwipe(true).swipeHorizontal(false).enableDoubletap(true)
                .pageFitPolicy(FitPolicy.WIDTH).defaultPage(lastPage)
                .onPageChange(onPageChangeListener).onTap(tapListener)
                .onDrawAll { canvas, pageW, pageH, pageIndex -> drawAnnotations(canvas, pageW, pageH, pageIndex) }
                .load()
        }

        binding.pageNumberText.setOnClickListener { showPageInputDialog() }

        // ===== FloatingPen → Toolbar টগল =====
        binding.floatingPen.setOnClickListener {
            isToolbarVisible = !isToolbarVisible
            binding.drawingToolbar.visibility = if (isToolbarVisible) View.VISIBLE else View.GONE
            if (!isToolbarVisible) { hidePaletteCompletely(); setDrawingMode(false); switchTool(Tool.NONE) }
            else { hidePaletteCompletely(); setDrawingMode(false); switchTool(Tool.NONE) }
        }

        // ===== PEN =====
        binding.btnPen.setOnClickListener {
            val now = System.currentTimeMillis()
            if (binding.colorPalette.visibility == View.VISIBLE) {
                if (currentTool != Tool.PEN) switchTool(Tool.PEN)
                hidePaletteCompletely(); return@setOnClickListener
            }
            if (now - lastClickTimePen < 300) { switchTool(Tool.PEN); showPalette(false, penWidth) }
            else { if (currentTool == Tool.PEN) switchTool(Tool.NONE) else { switchTool(Tool.PEN); hidePaletteCompletely() } }
            lastClickTimePen = now
        }

        // ===== SOFT PEN =====
        binding.btnPenSoft.setOnClickListener {
            val now = System.currentTimeMillis()
            if (binding.colorPalette.visibility == View.VISIBLE) {
                if (currentTool != Tool.PEN_SOFT) switchTool(Tool.PEN_SOFT)
                hidePaletteCompletely(); return@setOnClickListener
            }
            if (now - lastClickTimePenSoft < 300) { switchTool(Tool.PEN_SOFT); showPalette(false, softPenWidth) }
            else { if (currentTool == Tool.PEN_SOFT) switchTool(Tool.NONE) else { switchTool(Tool.PEN_SOFT); hidePaletteCompletely() } }
            lastClickTimePenSoft = now
        }

        // ===== HIGHLIGHTER =====
        binding.btnHighlighter.setOnClickListener {
            val now = System.currentTimeMillis()
            if (binding.colorPalette.visibility == View.VISIBLE) {
                if (currentTool != Tool.HIGHLIGHTER) switchTool(Tool.HIGHLIGHTER)
                hidePaletteCompletely(); return@setOnClickListener
            }
            if (now - lastClickTimeHighlighter < 300) { switchTool(Tool.HIGHLIGHTER); showPalette(false, highlighterWidth) }
            else { if (currentTool == Tool.HIGHLIGHTER) switchTool(Tool.NONE) else { switchTool(Tool.HIGHLIGHTER); hidePaletteCompletely() } }
            lastClickTimeHighlighter = now
        }

        // ===== SHAPE =====
        binding.btnShape.setOnClickListener {
            val now = System.currentTimeMillis()
            if (binding.colorPalette.visibility == View.VISIBLE) {
                if (currentTool != Tool.SHAPE) switchTool(Tool.SHAPE)
                hidePaletteCompletely(); return@setOnClickListener
            }
            if (now - lastClickTimeShape < 300) {
                switchTool(Tool.SHAPE)
                showPalette(true, shapeStrokeWidth)   // show shape row (Rect/Oval/Arrow)
            } else {
                if (currentTool == Tool.SHAPE) switchTool(Tool.NONE)
                else { switchTool(Tool.SHAPE); hidePaletteCompletely() }
            }
            lastClickTimeShape = now
        }
        binding.btnShapeRect.setOnClickListener { shapeKind = ShapeKind.RECT; updateShapeRowUI() }
        binding.btnShapeOval.setOnClickListener { shapeKind = ShapeKind.OVAL; updateShapeRowUI() }
        binding.btnShapeArrow.setOnClickListener { shapeKind = ShapeKind.ARROW; updateShapeRowUI() } // keep selection UI

        // ===== ERASER =====
        binding.btnEraser.setOnClickListener {
            if (currentTool == Tool.ERASER) switchTool(Tool.NONE) else { switchTool(Tool.ERASER); hidePaletteCompletely() }
        }

        // ===== UNDO / REDO =====
        binding.btnUndo.setOnClickListener { doUndo() }
        binding.btnRedo.setOnClickListener { doRedo() }

        // ===== Colors =====
        binding.colorRed.setOnClickListener { applyColorForCurrentTool(Color.RED) }
        binding.colorBlue.setOnClickListener { applyColorForCurrentTool(Color.BLUE) }
        binding.colorGreen.setOnClickListener { applyColorForCurrentTool(Color.GREEN) }
        binding.colorYellow.setOnClickListener { applyColorForCurrentTool(0xFFFFEB3B.toInt()) }
        binding.colorBlack.setOnClickListener { applyColorForCurrentTool(Color.BLACK) }

        // ===== Thickness =====
        binding.thicknessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val p = progress.coerceAtLeast(1).toFloat()
                when (currentTool) {
                    Tool.PEN -> { penWidth = p; binding.drawingView.setStrokeWidth(penWidth); savePrefs() }
                    Tool.PEN_SOFT -> { softPenWidth = p; binding.drawingView.setStrokeWidth(softPenWidth); savePrefs() }
                    Tool.HIGHLIGHTER -> { highlighterWidth = p; binding.drawingView.setStrokeWidth(highlighterWidth); savePrefs() }
                    Tool.SHAPE -> { shapeStrokeWidth = p; binding.drawingView.setStrokeWidth(shapeStrokeWidth); savePrefs() }
                    else -> {}
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ===== Clear =====
        binding.clearButton.setOnClickListener {
            binding.drawingView.clearDrawing()
            highlightsRel.clear()
            penStrokesRel.clear()
            arrowsRel.clear()
            undoStack.clear()
            redoStack.clear()
            binding.pdfView.invalidate()
        }
    }

    // Single tap → শুধু FloatingPen টগল (Toolbar খোলা থাকলে ইগনোর)
    private val tapListener = OnTapListener {
        if (binding.drawingToolbar.visibility == View.VISIBLE) return@OnTapListener true
        binding.floatingPen.visibility =
            if (binding.floatingPen.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        true
    }

    // ======= Renders annotations (with LIVE PREVIEW) =======
    private fun drawAnnotations(canvas: Canvas, pageW: Float, pageH: Float, pageIndex: Int) {
        val m = Matrix(); canvas.getMatrix(m)
        val local = RectF(0f, 0f, pageW, pageH)
        val viewRect = RectF(); m.mapRect(viewRect, local)
        pageBoundsInView[pageIndex] = viewRect
        pageSizeMap[pageIndex] = pageW to pageH

        // Saved filled highlights + outline shapes
        highlightsRel[pageIndex]?.forEach { rr ->
            val r = RectF(rr.left * pageW, rr.top * pageH, rr.right * pageW, rr.bottom * pageH)
            if (rr.outlineOnly) {
                shapeStrokePaint.color = rr.color
                shapeStrokePaint.alpha = rr.alpha
                shapeStrokePaint.strokeWidth = rr.strokeWidth
                if (rr.isOval) canvas.drawOval(r, shapeStrokePaint) else canvas.drawRect(r, shapeStrokePaint)
            } else {
                highlightFillPaint.color = rr.color
                highlightFillPaint.alpha = rr.alpha
                if (rr.isOval) canvas.drawOval(r, highlightFillPaint) else canvas.drawRect(r, highlightFillPaint)
            }
        }

        // Saved pen (Soft Pen হলে Multiply blend)
        penStrokesRel[pageIndex]?.forEach { relPath ->
            if (relPath.points.isEmpty()) return@forEach
            val path = Path()
            relPath.points.forEachIndexed { i, p ->
                val x = p.x * pageW; val y = p.y * pageH
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            penPaint.strokeWidth = relPath.strokeWidth
            penPaint.color = relPath.color
            penPaint.alpha = relPath.alpha

            if (relPath.isMultiply) {
                @Suppress("DEPRECATION")
                penPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                if (Build.VERSION.SDK_INT >= 29) {
                    penPaint.xfermode = null
                    penPaint.blendMode = BlendMode.MULTIPLY
                }
            } else {
                penPaint.xfermode = null
                if (Build.VERSION.SDK_INT >= 29) penPaint.blendMode = null
            }

            canvas.drawPath(path, penPaint)
        }

        // ===== Saved arrows (TAPERED) =====
        arrowsRel[pageIndex]?.forEach { a ->
            val sx = a.sx * pageW; val sy = a.sy * pageH
            val ex = a.ex * pageW; val ey = a.ey * pageH
            drawTaperedArrowStyled(canvas, sx, sy, ex, ey, a.color, a.strokeWidth, a.alpha)
        }

        // --- Live previews ---
        if (isSelectingRect && selectingRectPage == pageIndex &&
            (currentTool == Tool.HIGHLIGHTER || currentTool == Tool.SHAPE)) {

            if (currentTool == Tool.SHAPE && shapeKind == ShapeKind.ARROW) {
                val sx = startRelX * pageW; val sy = startRelY * pageH
                val ex = endRelX * pageW;   val ey = endRelY * pageH
                // Tapered preview (translucent)
                drawTaperedArrowPreview(canvas, sx, sy, ex, ey, shapeColor, shapeStrokeWidth)
            } else {
                val (lRel, tRel, rRel, bRel) =
                    if (currentTool == Tool.SHAPE && shapeKind == ShapeKind.OVAL)
                        squareFromDragIsotropic(startRelX, startRelY, endRelX, endRelY, pageW, pageH)
                    else
                        rectFromDrag(startRelX, startRelY, endRelX, endRelY)

                val rr = RectF(lRel * pageW, tRel * pageH, rRel * pageW, bRel * pageH)
                if (currentTool == Tool.SHAPE) {
                    val ps = Paint(previewStrokePaint).apply {
                        color = shapeColor; strokeWidth = shapeStrokeWidth
                    }
                    if (shapeKind == ShapeKind.OVAL) canvas.drawOval(rr, ps) else canvas.drawRect(rr, ps)
                } else {
                    previewFillPaint.color = highlighterColor
                    canvas.drawRect(rr, previewFillPaint)
                }
            }
        }
    }

    // ===== Tapered arrow helpers =====
    private fun drawTaperedArrowStyled(
        canvas: Canvas,
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        color: Int,
        strokeW: Float,
        alpha: Int
    ) {
        val ang = atan2(ey - sy, ex - sx)
        val ux = cos(ang); val uy = sin(ang)
        val px = -uy;      val py =  ux

        val segLen = hypot(ex - sx, ey - sy)
        val headLen = max(10f, min(segLen * 0.24f, strokeW * 6.5f))
        val halfBase = max(strokeW * 2.4f, 9f)
        val shaftLen = max(0f, segLen - headLen)

        val bx = sx + ux * shaftLen
        val by = sy + uy * shaftLen

        val overlap = min(6f, strokeW * 0.6f)
        val bxUnder = bx + ux * overlap
        val byUnder = by + uy * overlap

        // Triangular shaft
        val bLx = bxUnder + px * (strokeW / 2f)
        val bLy = byUnder + py * (strokeW / 2f)
        val bRx = bxUnder - px * (strokeW / 2f)
        val bRy = byUnder - py * (strokeW / 2f)
        val shaftPath = Path().apply { moveTo(sx, sy); lineTo(bLx, bLy); lineTo(bRx, bRy); close() }

        arrowOutlinePaint.strokeWidth = max(2f, strokeW * 0.55f)
        arrowOutlinePaint.alpha = (alpha * 0.9f).toInt().coerceIn(0,255)
        canvas.drawPath(shaftPath, arrowOutlinePaint)

        val shaftFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; this.color = color; this.alpha = alpha
        }
        canvas.drawPath(shaftPath, shaftFill)

        outlineHeadSidesOnly(canvas, ex, ey, bx, by, headLen, halfBase, alpha)
        drawArrowHeadFilled(canvas, ex, ey, bx, by, strokeW, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; this.color = color; this.alpha = alpha
        })
    }

    private fun drawTaperedArrowPreview(
        canvas: Canvas,
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        color: Int,
        strokeW: Float
    ) {
        val ang = atan2(ey - sy, ex - sx)
        val ux = cos(ang); val uy = sin(ang)
        val px = -uy;      val py =  ux

        val segLen = hypot(ex - sx, ey - sy)
        val headLen = max(10f, min(segLen * 0.24f, strokeW * 6.5f))
        val shaftLen = max(0f, segLen - headLen)

        val bx = sx + ux * shaftLen
        val by = sy + uy * shaftLen

        val overlap = min(6f, strokeW * 0.6f)
        val bxUnder = bx + ux * overlap
        val byUnder = by + uy * overlap

        val bLx = bxUnder + px * (strokeW / 2f)
        val bLy = byUnder + py * (strokeW / 2f)
        val bRx = bxUnder - px * (strokeW / 2f)
        val bRy = byUnder - py * (strokeW / 2f)

        val shaftPath = Path().apply { moveTo(sx, sy); lineTo(bLx, bLy); lineTo(bRx, bRy); close() }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; this.color = color; alpha = 130 }
        canvas.drawPath(shaftPath, fill)

        drawArrowHeadFilled(canvas, ex, ey, bx, by, strokeW, Paint().apply {
            style = Paint.Style.FILL; this.color = color; alpha = 170
        })
    }

    private fun drawArrowHeadFilled(
        canvas: Canvas,
        ex: Float, ey: Float, sx: Float, sy: Float,
        strokeW: Float,
        fillPaint: Paint
    ) {
        val angle = atan2(ey - sy, ex - sx)
        val headLen = max(10f, strokeW * 7.2f)
        val halfBase = max(strokeW * 2.4f, 9f)
        val bx = ex - headLen * cos(angle)
        val by = ey - headLen * sin(angle)
        val px = -sin(angle)
        val py =  cos(angle)
        val x1 = bx + px * halfBase
        val y1 = by + py * halfBase
        val x2 = bx - px * halfBase
        val y2 = by - py * halfBase
        val head = Path().apply { moveTo(ex, ey); lineTo(x1, y1); lineTo(x2, y2); close() }
        canvas.drawPath(head, fillPaint)
    }

    private fun outlineHeadSidesOnly(
        canvas: Canvas,
        ex: Float, ey: Float, sx: Float, sy: Float,
        headLen: Float, halfBase: Float,
        alpha: Int
    ) {
        val angle = atan2(ey - sy, ex - sx)
        val bx = ex - headLen * cos(angle)
        val by = ey - headLen * sin(angle)
        val px = -sin(angle); val py = cos(angle)

        val x1 = bx + px * halfBase
        val y1 = by + py * halfBase
        val x2 = bx - px * halfBase
        val y2 = by - py * halfBase

        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            this.alpha = (alpha * 0.9f).toInt()
            strokeWidth = max(2f, halfBase * 0.45f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(ex, ey, x1, y1, p)
        canvas.drawLine(ex, ey, x2, y2, p)
    }

    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        totalPages = pageCount
        binding.pageNumberText.text = "${page + 1} / $pageCount"
        if (currentBookId.isNotEmpty()) saveLastPage(currentBookId, page)
    }

    /** Drawing layer enable/disable + PDF swipe toggle */
    private fun setDrawingMode(enable: Boolean) {
        binding.drawingView.isDrawingEnabled = enable
        binding.drawingView.isClickable = enable
        binding.drawingView.setPassthroughToSink(enable)
        binding.pdfView.setSwipeEnabled(!enable)

        if (enable) {
            binding.drawingToolbar.bringToFront()
            binding.colorPalette.bringToFront()
            binding.floatingPen.bringToFront()
            binding.pageNumberText.bringToFront()
        }
    }

    private fun switchTool(tool: Tool) {
        currentTool = tool
        when (tool) {
            Tool.NONE -> {
                setDrawingMode(false)
                binding.drawingView.enableHighlighterMode(false)
                markSelectedTool(Tool.NONE)
            }
            Tool.PEN -> {
                setDrawingMode(true)
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setStrokeWidth(penWidth)
                binding.drawingView.setColor(penColor)
                markSelectedTool(Tool.PEN)
            }
            Tool.PEN_SOFT -> {
                setDrawingMode(true)
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setStrokeWidth(softPenWidth)
                binding.drawingView.setColor(softPenColor)
                markSelectedTool(Tool.PEN_SOFT)
            }
            Tool.HIGHLIGHTER -> {
                setDrawingMode(true)
                binding.drawingView.enableHighlighterMode(true)
                binding.drawingView.setStrokeWidth(highlighterWidth)
                binding.drawingView.setColor(highlighterColor)
                markSelectedTool(Tool.HIGHLIGHTER)
            }
            Tool.SHAPE -> {
                setDrawingMode(true)
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setStrokeWidth(shapeStrokeWidth)
                binding.drawingView.setColor(shapeColor)
                markSelectedTool(Tool.SHAPE)
            }
            Tool.ERASER -> {
                setDrawingMode(true)
                binding.drawingView.enableHighlighterMode(false)
                markSelectedTool(Tool.ERASER)
            }
        }
    }

    private fun showPalette(showShapeOptions: Boolean, width: Float) {
        binding.colorPalette.visibility = View.VISIBLE
        binding.thicknessSeekBar.progress =
            width.toInt().coerceIn(1, binding.thicknessSeekBar.max)
        binding.shapeRow.visibility = if (showShapeOptions) View.VISIBLE else View.GONE
        updateShapeRowUI()
    }

    private fun updateShapeRowUI() {
        val sel = R.drawable.bg_tool_selected
        val nor = R.drawable.bg_tool_normal
        binding.btnShapeRect.setBackgroundResource(if (shapeKind == ShapeKind.RECT) sel else nor)
        binding.btnShapeOval.setBackgroundResource(if (shapeKind == ShapeKind.OVAL) sel else nor)
        binding.btnShapeArrow.setBackgroundResource(if (shapeKind == ShapeKind.ARROW) sel else nor)
    }

    private fun hidePaletteCompletely() {
        binding.colorPalette.visibility = View.GONE
        binding.shapeRow.visibility = View.GONE
    }

    private fun applyColorForCurrentTool(color: Int) {
        when (currentTool) {
            Tool.PEN -> {
                penColor = color
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setColor(penColor)
                savePrefs()
            }
            Tool.PEN_SOFT -> {
                softPenColor = color
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setColor(softPenColor)
                savePrefs()
            }
            Tool.HIGHLIGHTER -> {
                highlighterColor = color
                binding.drawingView.enableHighlighterMode(true)
                binding.drawingView.setColor(highlighterColor)
                savePrefs()
            }
            Tool.SHAPE -> {
                shapeColor = color
                binding.drawingView.enableHighlighterMode(false)
                binding.drawingView.setColor(shapeColor)
                savePrefs()
            }
            Tool.NONE, Tool.ERASER -> {
                Toast.makeText(this, "Select a drawing tool first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPrefs() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        penWidth = sp.getFloat(KEY_PEN_WIDTH, 6f)
        highlighterWidth = sp.getFloat(KEY_HL_WIDTH, 20f)
        penColor = sp.getInt(KEY_PEN_COLOR, Color.RED)
        highlighterColor = sp.getInt(KEY_HL_COLOR, Color.YELLOW)

        softPenWidth = sp.getFloat(KEY_SOFT_PEN_WIDTH, 6f)
        softPenColor = sp.getInt(KEY_SOFT_PEN_COLOR, Color.RED)

        shapeStrokeWidth = sp.getFloat(KEY_SHAPE_WIDTH, 6f)
        shapeColor = sp.getInt(KEY_SHAPE_COLOR, Color.RED)
    }
    private fun savePrefs() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit()
            .putFloat(KEY_PEN_WIDTH, penWidth)
            .putFloat(KEY_HL_WIDTH, highlighterWidth)
            .putInt(KEY_PEN_COLOR, penColor)
            .putInt(KEY_HL_COLOR, highlighterColor)
            .putFloat(KEY_SOFT_PEN_WIDTH, softPenWidth)
            .putInt(KEY_SOFT_PEN_COLOR, softPenColor)
            .putFloat(KEY_SHAPE_WIDTH, shapeStrokeWidth)
            .putInt(KEY_SHAPE_COLOR, shapeColor)
            .apply()
    }

    private fun showPageInputDialog() {
        val input = EditText(this)
        input.hint = "Enter page number"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Go to Page")
            .setView(input)
            .setPositiveButton("Go") { _, _ ->
                val pageNum = input.text.toString().toIntOrNull()
                if (pageNum != null && pageNum in 1..totalPages) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.pdfView.jumpTo(pageNum - 1, true)
                    }, 100)
                } else {
                    Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markSelectedTool(tool: Tool) {
        val sel = R.drawable.bg_tool_selected
        val nor = R.drawable.bg_tool_normal

        binding.btnPen.setBackgroundResource(if (tool == Tool.PEN) sel else nor)
        binding.btnPenSoft.setBackgroundResource(if (tool == Tool.PEN_SOFT) sel else nor)
        binding.btnHighlighter.setBackgroundResource(if (tool == Tool.HIGHLIGHTER) sel else nor)
        binding.btnShape.setBackgroundResource(if (tool == Tool.SHAPE) sel else nor)
        binding.btnEraser.setBackgroundResource(if (tool == Tool.ERASER) sel else nor)

        binding.btnPen.alpha = if (tool == Tool.PEN) 1f else 0.6f
        binding.btnPenSoft.alpha = if (tool == Tool.PEN_SOFT) 1f else 0.6f
        binding.btnHighlighter.alpha = if (tool == Tool.HIGHLIGHTER) 1f else 0.6f
        binding.btnShape.alpha = if (tool == Tool.SHAPE) 1f else 0.6f
        binding.btnEraser.alpha = if (tool == Tool.ERASER) 1f else 0.6f
    }

    private fun pageUnder(x: Float, y: Float): Int? =
        pageBoundsInView.entries.firstOrNull { it.value.contains(x, y) }?.key

    // ====== Undo/Redo helpers ======
    private fun pushAction(a: Action) {
        undoStack.addLast(a); redoStack.clear()
    }
    private fun doUndo() {
        if (undoStack.isEmpty()) return
        val a = undoStack.removeLast()
        when (a) {
            is Action.AddPen -> penStrokesRel[a.page]?.let { if (it.isNotEmpty()) it.removeAt(it.size - 1) }
            is Action.AddHighlight -> highlightsRel[a.page]?.let { if (it.isNotEmpty()) it.removeAt(it.size - 1) }
            is Action.AddArrow -> arrowsRel[a.page]?.let { if (it.isNotEmpty()) it.removeAt(it.size - 1) }
        }
        redoStack.addLast(a)
        binding.pdfView.invalidate()
    }
    private fun doRedo() {
        if (redoStack.isEmpty()) return
        val a = redoStack.removeLast()
        when (a) {
            is Action.AddPen -> penStrokesRel.getOrPut(a.page) { mutableListOf() }.add(a.path)
            is Action.AddHighlight -> highlightsRel.getOrPut(a.page) { mutableListOf() }.add(a.rect)
            is Action.AddArrow -> arrowsRel.getOrPut(a.page) { mutableListOf() }.add(a.arrow)
        }
        undoStack.addLast(a)
        binding.pdfView.invalidate()
    }

    // ====== Eraser ======
    private fun eraseAt(page: Int, rx: Float, ry: Float) {
        highlightsRel[page]?.let { list ->
            var i = list.size - 1
            while (i >= 0) {
                val r = list[i]
                if (rx >= r.left && rx <= r.right && ry >= r.top && ry <= r.bottom) list.removeAt(i)
                i--
            }
        }
        val th = 0.015f
        penStrokesRel[page]?.let { list ->
            var i = list.size - 1
            while (i >= 0) {
                val path = list[i]
                val hit = path.points.any { p ->
                    val dx = p.x - rx; val dy = p.y - ry
                    dx * dx + dy * dy <= th * th
                }
                if (hit) list.removeAt(i)
                i--
            }
        }
        arrowsRel[page]?.let { list ->
            var i = list.size - 1
            while (i >= 0) {
                val a = list[i]
                if (pointToSegDist(rx, ry, a.sx, a.sy, a.ex, a.ey) <= th) list.removeAt(i)
                i--
            }
        }
    }

    private fun pointToSegDist(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val vx = x2 - x1; val vy = y2 - y1
        val wx = px - x1; val wy = py - y1
        val c1 = vx * wx + vy * wy
        if (c1 <= 0) return hypot(px - x1, py - y1)
        val c2 = vx * vx + vy * vy
        if (c2 <= c1) return hypot(px - x2, py - y2)
        val t = c1 / c2
        val projx = x1 + t * vx; val projy = y1 + t * vy
        return hypot(px - projx, py - projy)
    }

    // ===== Helpers: rect & isotropic-square from drag =====
    private fun rectFromDrag(sx: Float, sy: Float, ex: Float, ey: Float): Quad<Float, Float, Float, Float> {
        val left = min(sx, ex).coerceIn(0f, 1f)
        val top = min(sy, ey).coerceIn(0f, 1f)
        val right = max(sx, ex).coerceIn(0f, 1f)
        val bottom = max(sy, ey).coerceIn(0f, 1f)
        return Quad(left, top, right, bottom)
    }

    /** Perfect circle helper: equal side in PAGE space, then back to relative */
    private fun squareFromDragIsotropic(
        sxRel: Float, syRel: Float, exRel: Float, eyRel: Float,
        pageW: Float, pageH: Float
    ): Quad<Float, Float, Float, Float> {
        val sxPx = sxRel * pageW
        val syPx = syRel * pageH
        val exPx = exRel * pageW
        val eyPx = eyRel * pageH

        val dx = exPx - sxPx
        val dy = eyPx - syPx
        val side = max(abs(dx), abs(dy))

        val rxPx = if (dx >= 0) sxPx + side else sxPx - side
        val ryPx = if (dy >= 0) syPx + side else syPx - side

        val leftPx = min(sxPx, rxPx)
        val topPx = min(syPx, ryPx)
        val rightPx = max(sxPx, rxPx)
        val bottomPx = max(syPx, ryPx)

        return Quad(leftPx / pageW, topPx / pageH, rightPx / pageW, bottomPx / pageH)
    }

    // tiny value holder
    data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
