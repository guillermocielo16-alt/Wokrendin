package app.miturno

import android.app.*
import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.*
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import kotlin.math.abs
import kotlin.math.min
import android.widget.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var s: Store
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var scroll: ScrollView
    private var heroSection: View? = null
    private var monthSection: View? = null
    private var monthGrid: CalendarGrid? = null
    private var manageSection: View? = null
    private var summarySection: View? = null
    private var stripSection: HorizontalScrollView? = null
    private var stripStartDate: LocalDate? = null
    private var stripTiles = mutableMapOf<LocalDate, View>()
    private var headerDateLabel: TextView? = null
    private lateinit var drawerHost: FrameLayout
    private var drawerOverlay: FrameLayout? = null
    private var drawerPanel: View? = null
    private var drawerScrim: View? = null
    private var drawerScroll: ScrollView? = null
    private var drawerScreen: String? = null
    private var drawingInDrawer = false
    private var drawerScrollOffset = 0
    private var drawerInsetTop = 0
    private var drawerInsetBottom = 0
    private var drawerRemindersAnchor: View? = null
    private var platformDrawerBack: OnBackInvokedCallback? = null
    private var second = false
    private var team = "A"
    private var group = "CD"
    private var hasSelection = false
    private var selected = LocalDate.now()
    private var month = LocalDate.now().withDayOfMonth(1)
    private var dark = false
    private var page = "calendar"
    private var lookupPanel: View? = null
    private var reportStart = ""
    private var reportEnd = ""
    private var exportText = ""
    private var renderedToday = LocalDate.now()
    private var ambientBackdrop: NeonAmbientDrawable? = null
    private fun ambientEnabled() = s.text("ambientGlow", "on") != "off"
    private val clock = Handler(Looper.getMainLooper())
    private val refreshClock =
        object : Runnable {
            override fun run() {
                if (page == "calendar" && renderedToday != LocalDate.now()) {
                    renderedToday = LocalDate.now()
                    render()
                }
                clock.postDelayed(this, 60000)
            }
        }
    // Device events are never exported or written to the work calendar.
    private val deviceCalendar by lazy { AndroidCalendarReader(this) }
    private val deviceCalendarWorker = Executors.newSingleThreadExecutor()
    private val deviceCalendarUi = Handler(Looper.getMainLooper())
    private var deviceCalendarEvents: Map<LocalDate, List<DeviceCalendarEvent>> = emptyMap()
    private var deviceCalendarStatus = "Sin sincronizar"
    private var deviceCalendarSynced = false
    private var deviceCalendarRequest = 0
    private val sourcesPrefs by lazy { getSharedPreferences("mi_turno_calendar_sources", MODE_PRIVATE) }

    private fun chosenCalendarIds(): Set<Long> =
        sourcesPrefs.getString("selected_ids", "").orEmpty().split(",")
            .mapNotNull { it.toLongOrNull() }.toSet()

    private val es = Locale.forLanguageTag("es-MX")
    private val titleFont by lazy { Typeface.createFromAsset(assets, "fonts/sora.ttf") }
    private val mainFont by lazy { Typeface.createFromAsset(assets, "fonts/inter.ttf") }
    private val monoFont by lazy { Typeface.createFromAsset(assets, "fonts/mono.ttf") }
    private val bg
        get() =
            color(
                if (second) {
                    if (dark) "#17121A" else "#F6F1EA"
                } else if (dark) "#07070A" else "#EAECF2"
            )

    private val panel
        get() = color(if (second && dark) "#221A26" else if (dark) "#111116" else "#FFFFFF")

    private val line
        get() =
            color(
                if (second) {
                    if (dark) "#3D2E3C" else "#E8DBCB"
                } else if (dark) "#262630" else "#DCE0EA"
            )

    private val ink
        get() =
            color(
                if (second) {
                    if (dark) "#F5F0EC" else "#3E332C"
                } else if (dark) "#F8F9FA" else "#1B2333"
            )

    private val dim
        get() =
            color(
                if (second) {
                    if (dark) "#B8A8B0" else "#8C7A6C"
                } else if (dark) "#8B90A0" else "#5C6478"
            )

    private val gold
        get() =
            color(
                if (second) {
                    if (dark) "#C98FB0" else "#C17D78"
                } else if (dark) "#EAB308" else "#9C6B0B"
            )

    private fun color(v: String) = Color.parseColor(v)

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun accent(t: String) =
        color(
            when (t) {
                "dia" -> if (dark) "#E9A835" else "#C97A17"
                "noche",
                "trabajo" ->
                    if (second) {
                        if (dark) "#D4AF6A" else "#C97558"
                    } else if (dark) "#7C83DB" else "#4453C9"
                "guardia" -> if (dark) "#D85858" else "#B84A2C"
                "vacaciones" -> if (dark) "#A78BFA" else "#6E4FB0"
                "festivo" ->
                    if (second) {
                        if (dark) "#48CAE4" else "#00A8E8"
                    } else if (dark) "#A78BFA" else "#6E4FB0"
                "cambio" ->
                    if (second) {
                        if (dark) "#E0A9A0" else "#5F7A6E"
                    } else if (dark) "#34D9C4" else "#1E8A80"
                else ->
                    if (second) {
                        if (dark) "#4CAE82" else "#7E9689"
                    } else if (dark) "#24B07B" else "#2E8B57"
            }
        )

    private fun soft(t: String): Int =
        if (second) tint(accent(t), if (dark) 36 else if (t == "descanso") 46 else 38)
        else if (dark) tint(accent(t), 56)
        else
            color(
                when (t) {
                    "dia" -> "#F6DFB3"
                    "noche",
                    "trabajo" -> "#D6DAF7"
                    "guardia" -> "#F3CFC0"
                    "vacaciones" -> "#DCC9F0"
                    else -> "#C8ECD5"
                }
            )

    private fun tint(c: Int, alpha: Int) =
        Color.argb(alpha, Color.red(c), Color.green(c), Color.blue(c))

    private fun blend(from: Int, to: Int, amount: Float): Int {
        val f = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(from) * (1f - f) + Color.red(to) * f).toInt(),
            (Color.green(from) * (1f - f) + Color.green(to) * f).toInt(),
            (Color.blue(from) * (1f - f) + Color.blue(to) * f).toInt(),
        )
    }

    // The web version glows under translucent cards rather than placing light above text.
    private fun cardSurface(): Int =
        if (!dark) panel else Color.argb(222, Color.red(panel), Color.green(panel), Color.blue(panel))

    private fun stripSurface(type: String, active: Boolean): GradientDrawable {
        val statusColor = accent(type)
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                if (dark) blend(panel, statusColor, 0.08f) else soft(type),
                if (dark) blend(panel, statusColor, 0.27f) else blend(soft(type), statusColor, 0.13f),
            ),
        ).apply {
            cornerRadius = dp(10).toFloat()
            setStroke(dp(if (active) 2 else 1), if (active) gold else tint(statusColor, if (dark) 100 else 175))
        }
    }

    private fun shape(fill: Int, stroke: Int = line, radius: Int = 16) =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }

    private fun text(v: String, size: Int = 14, c: Int = ink, font: Typeface = mainFont) =
        TextView(this).apply {
            text = v
            textSize = size.toFloat()
            setTextColor(c)
            typeface = font
            setPadding(0, dp(5), 0, dp(5))
            includeFontPadding = true
        }

    private fun vertical() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    private fun row() =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun add(parent: LinearLayout, view: View, weight: Float = 0f) {
        val p = LinearLayout.LayoutParams(if (weight > 0) 0 else -1, -2, weight)
        p.bottomMargin = dp(8)
        parent.addView(view, p)
    }

    private fun button(label: String, c: Int = accent("descanso"), action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 13f
            typeface = titleFont
            gravity = Gravity.CENTER
            setTextColor(c)
            setPadding(dp(12), dp(13), dp(12), dp(13))
            minHeight = dp(48)
            background =
                RippleDrawable(
                    android.content.res.ColorStateList.valueOf(tint(c, 65)),
                    shape(tint(c, if (dark) 35 else 20), tint(c, 100), 12),
                    null,
                )
            isClickable = true
            isFocusable = true
            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                animate()
                    .scaleX(.96f)
                    .scaleY(.96f)
                    .setDuration(65)
                    .withEndAction { animate().scaleX(1f).scaleY(1f).setDuration(130).start() }
                    .start()
                action()
            }
        }

    private fun card(parent: LinearLayout = body): LinearLayout =
        vertical().also {
            it.setPadding(dp(16), dp(14), dp(16), dp(14))
            it.background = shape(cardSurface(), if (dark) tint(ink, 20) else line)
            it.elevation = dp(1).toFloat()
            val p = LinearLayout.LayoutParams(-1, -2)
            p.bottomMargin = dp(16)
            parent.addView(it, p)
        }

    private fun heading(p: LinearLayout, v: String) {
        add(p, text(v, 16, ink, titleFont))
    }

    private fun toast(v: String) {
        Toast.makeText(this, v, Toast.LENGTH_SHORT).show()
    }

    private fun format(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", es))

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        s = Store(this)
        second = s.text("calendar", "1") == "2"
        team = s.text("team", "A")
        group = s.text("group", "CD")
        dark = s.text("theme", if (LocalTime.now().hour in 7..18) "light" else "dark") == "dark"
        state?.let {
            selected = LocalDate.parse(it.getString("selected"))
            hasSelection = it.getBoolean("hasSelection")
            month = LocalDate.parse(it.getString("month"))
            page = it.getString("page", "calendar")
            drawerScreen = it.getString("drawerScreen")
            if (drawerScreen == "menu" || drawerScreen == "reminders" ||
                drawerScreen == "holidays") {
                drawerScreen = if (second) "settings" else "extras"
            }
            // Older versions stored these sections as fullscreen pages.
            if (page in listOf("extras", "settings", "holidays")) {
                drawerScreen = page
                page = "calendar"
            }
            drawerScrollOffset = it.getInt("drawerScrollOffset", 0)
            exportText = it.getString("export", "")
            reportStart = it.getString("rangeStart", "")
            reportEnd = it.getString("rangeEnd", "")
        }
        intent?.getStringExtra("openDate")?.let { openDateFromNotification(it) }
        Reminders.channel(this)
        Reminders.schedule(this)
        render()
    }

    private fun openDateFromNotification(raw: String) {
        runCatching { LocalDate.parse(raw) }.getOrNull()?.let { date ->
            page = "calendar"
            hasSelection = true
            selected = date
            month = date.withDayOfMonth(1)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("openDate")?.let {
            if (drawerScreen != null) closeDrawer(immediate = true)
            openDateFromNotification(it)
            render()
        }
    }

    override fun onSaveInstanceState(out: Bundle) {
        out.putString("selected", selected.toString())
        out.putBoolean("hasSelection", hasSelection)
        out.putString("month", month.toString())
        out.putString("page", page)
        out.putString("drawerScreen", drawerScreen)
        out.putInt("drawerScrollOffset", drawerScroll?.scrollY ?: drawerScrollOffset)
        out.putString("export", exportText)
        out.putString("rangeStart", reportStart)
        out.putString("rangeEnd", reportEnd)
        super.onSaveInstanceState(out)
    }

    private fun persist() {
        s.put("calendar", if (second) "2" else "1")
        s.put("team", team)
        s.put("group", group)
        Reminders.schedule(this)
    }

    private fun render(animate: Boolean = false) {
        val oldScroll = if (::scroll.isInitialized && !animate) scroll.scrollY else 0
        val drawerPosition = drawerScroll?.scrollY ?: drawerScrollOffset
        heroSection = null
        monthSection = null
        monthGrid = null
        manageSection = null
        summarySection = null
        stripSection = null
        stripTiles.clear()
        headerDateLabel = null
        drawerOverlay = null
        drawerPanel = null
        drawerScrim = null
        drawerScroll = null
        setTheme(
            if (dark) android.R.style.Theme_Material_NoActionBar
            else android.R.style.Theme_Material_Light_NoActionBar
        )
        root = vertical()
        if (ambientEnabled()) {
            val back = ambientBackdrop ?: NeonAmbientDrawable(bg, dark, second).also {
                ambientBackdrop = it
            }
            back.configure(bg, dark, second)
            root.background = back
            back.start()
        } else {
            ambientBackdrop?.stop()
            root.setBackgroundColor(bg)
        }
        window.statusBarColor = bg
        window.navigationBarColor = panel
        window.decorView.systemUiVisibility =
            if (dark) 0
            else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        drawerHost = FrameLayout(this)
        drawerHost.addView(root, FrameLayout.LayoutParams(-1, -1))
        drawerHost.setOnApplyWindowInsetsListener { _, insets ->
            drawerInsetTop = insets.systemWindowInsetTop
            drawerInsetBottom = insets.systemWindowInsetBottom
            root.setPadding(
                insets.systemWindowInsetLeft,
                drawerInsetTop,
                insets.systemWindowInsetRight,
                drawerInsetBottom,
            )
            drawerOverlay?.setPadding(0, drawerInsetTop, 0, drawerInsetBottom)
            insets
        }
        // Build the entire next frame offscreen: never expose an empty Activity to the GPU.
        scroll =
            ScrollView(this).apply {
                isFillViewport = true
                clipToPadding = false
            }
        body = vertical().apply { setPadding(dp(16), dp(18), dp(16), dp(12)) }
        val cap = if (second) 1040 else 480
        scroll.addView(
            body,
            android.widget.FrameLayout.LayoutParams(
                if (resources.configuration.screenWidthDp > cap) dp(cap) else -1,
                -2,
                Gravity.CENTER_HORIZONTAL,
            ),
        )
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        when (page) {
            "extras" -> extras()
            "settings" -> settings()
            "holidays" -> holidays()
            else -> calendar()
        }
        val bottom =
            row().apply {
                setPadding(dp(14), dp(10), dp(14), dp(10))
                background = shape(if (dark) tint(panel, 246) else panel, line, 0)
                elevation = dp(8).toFloat()
            }
        val switch =
            button("⇄  Alternar Tipo de Calendario") {
                second = !second
                page = "calendar"
                persist()
                render(true)
            }.apply {
                minHeight = dp(50)
                textSize = 14f
                val base = if (second) "#B68DC6" else "#22B782"
                val highlight = if (second) "#E2AE9E" else "#27CCA0"
                setTextColor(if (dark) color("#061C18") else color("#10251D"))
                background = RippleDrawable(
                    android.content.res.ColorStateList.valueOf(tint(ink, 40)),
                    GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(color(base), color(highlight)),
                    ).apply { cornerRadius = dp(12).toFloat() },
                    null,
                )
            }
        bottom.addView(switch, LinearLayout.LayoutParams(0, -2, 1f).apply {
            marginEnd = dp(9)
        })
        val themeButton = button(if (dark) "☀" else "☾", gold) {
            dark = !dark
            s.put("theme", if (dark) "dark" else "light")
            render()
        }.apply {
            textSize = 22f
            minHeight = dp(46)
            setPadding(0, 0, 0, 0)
            background = RippleDrawable(
                android.content.res.ColorStateList.valueOf(tint(gold, 50)),
                shape(if (dark) color("#16161D") else panel, tint(gold, 90), 99),
                null,
            )
        }
        bottom.addView(themeButton, LinearLayout.LayoutParams(dp(47), dp(47)))
        root.addView(bottom)
        // Atomic swap after calendar, cards and bottom controls are fully constructed.
        setContentView(drawerHost)
        drawerHost.requestApplyInsets()
        scroll.post { scroll.scrollTo(0, oldScroll) }
        if (drawerScreen != null) mountDrawer(animated = false, scrollTo = drawerPosition)
        else updateDrawerBackCallback(false)
        if (animate) {
            body.alpha = 0f
            body.translationY = dp(7).toFloat()
            body
                .animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun menu() = openDrawer(if (second) "settings" else "extras")

    /** Theme-aware hamburger: green for the 28-day calendar, mauve for 2-2-3. */
    private fun drawerAccent(): Int = if (second) {
        color(if (dark) "#C98FB0" else "#A56886")
    } else color(if (dark) "#24B07B" else "#208A65")

    private fun drawerToggle() = TextView(this).apply {
        text = "☰"
        gravity = Gravity.CENTER
        textSize = 24f
        typeface = mainFont
        setTextColor(drawerAccent())
        background = RippleDrawable(
            android.content.res.ColorStateList.valueOf(tint(drawerAccent(), 55)),
            shape(if (dark) color(if (second) "#221923" else "#151B1A") else panel,
                tint(drawerAccent(), 80), 13),
            null,
        )
        isClickable = true
        isFocusable = true
        contentDescription = "Abrir menú lateral"
        setOnClickListener { menu() }
    }

    /** The WebView has one contextual drawer per calendar, not an intermediate menu. */
    private fun openDrawer(destination: String) {
        require(destination in setOf("extras", "settings"))
        val wasClosed = drawerScreen == null
        drawerScreen = destination
        drawerScrollOffset = 0
        mountDrawer(animated = wasClosed, scrollTo = 0)
    }

    private fun drawerBack() {
        if (drawerScreen != null) closeDrawer()
    }

    private fun updateDrawerBackCallback(enable: Boolean) {
        if (Build.VERSION.SDK_INT < 33) return
        if (enable && platformDrawerBack == null) {
            val callback = OnBackInvokedCallback { drawerBack() }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY, callback
            )
            platformDrawerBack = callback
        } else if (!enable) {
            platformDrawerBack?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            platformDrawerBack = null
        }
    }

    private fun blurCalendarForDrawer(blur: Boolean) {
        if (Build.VERSION.SDK_INT >= 31) {
            root.setRenderEffect(
                if (blur) RenderEffect.createBlurEffect(
                    dp(3).toFloat(), dp(3).toFloat(), Shader.TileMode.CLAMP,
                ) else null
            )
        }
    }

    /** Native left-edge drawer; swipe left or tap the scrim to dismiss. */
    private inner class DrawerGestureFrame(context: Context) : FrameLayout(context) {
        private var startX = 0f
        private var startY = 0f
        private var swiping = false

        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    swiping = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - startX
                    val dy = event.y - startY
                    if (dx < -dp(44) && abs(dx) > 1.35f * abs(dy)) {
                        swiping = true
                        return true
                    }
                }
            }
            return super.onInterceptTouchEvent(event)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_UP && swiping) {
                swiping = false
                closeDrawer()
                return true
            }
            if (event.actionMasked == MotionEvent.ACTION_CANCEL) swiping = false
            return true
        }
    }

    private fun closeDrawer(immediate: Boolean = false) {
        drawerScreen = null
        drawerScrollOffset = 0
        updateDrawerBackCallback(false)
        val overlay = drawerOverlay ?: run {
            blurCalendarForDrawer(false)
            return
        }
        val panelView = drawerPanel
        val scrim = drawerScrim
        val host = drawerHost
        fun remove() {
            if (drawerOverlay === overlay) {
                host.removeView(overlay)
                drawerOverlay = null
                drawerPanel = null
                drawerScrim = null
                drawerScroll = null
                blurCalendarForDrawer(false)
            }
        }
        if (immediate || panelView == null || !android.animation.ValueAnimator.areAnimatorsEnabled()) {
            remove()
        } else {
            panelView.animate().translationX(-panelView.width.toFloat())
                .setDuration(260).setInterpolator(DecelerateInterpolator()).start()
            scrim?.animate()?.alpha(0f)?.setDuration(245)?.withEndAction { remove() }?.start()
                ?: remove()
        }
    }

    private fun drawerTitleBar(title: String) {
        val h = row()
        h.setPadding(0, dp(8), 0, dp(12))
        val titleView = text(title, 23, ink, titleFont)
        h.addView(titleView, LinearLayout.LayoutParams(0, -2, 1f))
        val close = TextView(this).apply {
            text = "×"
            textSize = 29f
            gravity = Gravity.CENTER
            setTextColor(dim)
            contentDescription = "Cerrar menú"
            setOnClickListener { closeDrawer() }
        }
        h.addView(close, LinearLayout.LayoutParams(dp(42), dp(42)))
        add(body, h)
        val divider = View(this).apply { setBackgroundColor(line) }
        body.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).apply {
            bottomMargin = dp(16)
        })
    }

    private fun mountDrawer(animated: Boolean, scrollTo: Int) {
        if (!::drawerHost.isInitialized || drawerScreen == null) return
        drawerOverlay?.let { previous -> (previous.parent as? ViewGroup)?.removeView(previous) }
        val shade = FrameLayout(this).apply {
            setPadding(0, drawerInsetTop, 0, drawerInsetBottom)
            clipChildren = true
            clipToPadding = false
        }
        val scrim = View(this).apply {
            setBackgroundColor(Color.argb(165, 0, 0, 0))
            setOnClickListener { closeDrawer() }
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        shade.addView(scrim, FrameLayout.LayoutParams(-1, -1))
        val widthPx = min(dp(300), (resources.displayMetrics.widthPixels * 0.84f).toInt())
        val drawer = DrawerGestureFrame(this).apply {
            background = shape(
                if (dark) color(if (second) "#211923" else "#111116")
                else color(if (second) "#F7EFF2" else "#FFFFFF"), line, 0
            )
            elevation = dp(15).toFloat()
        }
        val contentScroll = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        val content = vertical().apply {
            setPadding(dp(18), dp(20), dp(18), dp(28))
        }
        contentScroll.addView(content, FrameLayout.LayoutParams(-1, -2))
        drawer.addView(contentScroll, FrameLayout.LayoutParams(-1, -1))
        shade.addView(drawer, FrameLayout.LayoutParams(widthPx, -1, Gravity.START))
        drawerHost.addView(shade, FrameLayout.LayoutParams(-1, -1))

        drawerOverlay = shade
        drawerPanel = drawer
        drawerScrim = scrim
        drawerScroll = contentScroll
        drawerRemindersAnchor = null

        val mainBody = body
        body = content
        drawingInDrawer = true
        try {
            when (drawerScreen) {
                "extras" -> extras()
                "settings" -> settings()
            }
        } finally {
            drawingInDrawer = false
            body = mainBody
        }

        contentScroll.post { contentScroll.scrollTo(0, scrollTo.coerceAtLeast(0)) }
        updateDrawerBackCallback(true)
        blurCalendarForDrawer(true)
        if (animated && android.animation.ValueAnimator.areAnimatorsEnabled()) {
            scrim.alpha = 0f
            drawer.translationX = -widthPx.toFloat()
            scrim.animate().alpha(1f).setDuration(270).start()
            drawer.animate().translationX(0f).setDuration(290)
                .setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun header(title: String, sub: String) {
        if (drawingInDrawer) {
            drawerTitleBar(
                when (title) {
                    "Horas Extra" -> "⏱ Horas Extra"
                    "Equipos y ajustes" -> "⚙ Ajustes Cal ${if (second) "2" else "1"}"
                    "Días festivos" -> "🎺 Días festivos"
                    else -> title
                }
            )
            return
        }
        val r = row()
        r.addView(drawerToggle(), LinearLayout.LayoutParams(dp(48), dp(48)))
        val col = vertical().apply { setPadding(dp(12), 0, 0, 0) }
        val subLabel = text(sub, 10, dim, monoFont)
        if (page == "calendar" && !drawingInDrawer) headerDateLabel = subLabel
        add(col, subLabel)
        add(col, text(title, if (second && page == "calendar") 19 else 22, ink, titleFont))
        r.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        if (page == "calendar" && !second && resources.configuration.screenWidthDp >= 400) {
            val chips = row()
            chips.addView(
                button("Hoy", gold) { selectDate(LocalDate.now()) },
                LinearLayout.LayoutParams(dp(45), dp(36)),
            )
            for (t in listOf("A", "B", "C", "D")) {
                val b =
                    button(t, if (t == team) panel else dim) {
                        team = t
                        persist()
                        render()
                    }
                b.typeface = monoFont
                b.minHeight = dp(30)
                b.setPadding(0, 0, 0, 0)
                b.background =
                    RippleDrawable(
                        android.content.res.ColorStateList.valueOf(tint(ink, 40)),
                        shape(if (t == team) ink else panel, line, 8),
                        null,
                    )
                chips.addView(
                    b,
                    LinearLayout.LayoutParams(dp(30), dp(30)).apply { marginStart = dp(4) },
                )
            }
            r.addView(chips)
        }
        add(body, r)
    }

    private fun type(d: LocalDate): String =
        if (second) {
            if (CalendarRules.work223(d, group)) "trabajo" else "descanso"
        } else CalendarRules.status(d, team)

    private fun effective(d: LocalDate) =
        if (!second && s.vacation(d, team)) "vacaciones" else type(d)

    private fun hours(d: LocalDate): String {
        val t = effective(d)
        if (second && t == "trabajo") return CalendarRules.hours(s.text("shift", "dia"))
        if (t == "guardia")
            return when (s.guard(d, team)) {
                "trabaja_dia" -> "08:00 – 20:00"
                "trabaja_noche" -> "20:00 – 08:00"
                "no_trabaja" -> "Día libre"
                else -> CalendarRules.hours(t)
            }
        return CalendarRules.hours(t)
    }

    private fun calendar() {
        header(
            if (second) "Rotación 2026 · Equipos C/D" else "Mi Turno",
            if (second) "CALENDARIO DE TRABAJO · ${format(selected)}" else "CICLO 28 DÍAS · ${format(selected)}",
        )
        if (second) add(body, text("Esquema 2-2-3 · viernes a domingo agrupados", 12, dim))
        val picker = row()
        val options = if (second) listOf("AB", "CD") else listOf("A", "B", "C", "D")
        options.forEach { v ->
            val active = v == if (second) group else team
            val b =
                button(if (second) "Grupo ${v[0]} y ${v[1]}" else v, if (active) gold else dim) {
                    if (second) group = v else team = v
                    persist()
                    render()
                }
            picker.addView(b, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(4) })
        }
        if (second || resources.configuration.screenWidthDp < 400) add(body, picker)
        if (!second) hero(selected, selected == LocalDate.now())
        calendarSyncControls()
        if (!second) strip()
        if (!second) lookup()
        if (!second) manage()
        monthCard()
        if (second) {
            strip()
            lookup()
        } else summary()
    }

    private fun hero(d: LocalDate, today: Boolean = false) {
        val t = effective(d)
        val c = accent(t)
        val box = card()
        if (!drawingInDrawer) heroSection = box
        box.setPadding(dp(26), dp(20), dp(20), dp(18))
        box.background =
            object : Drawable() {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                override fun draw(canvas: Canvas) {
                    val r = RectF(bounds)
                    val path = Path()
                    path.addRoundRect(r, dp(18).toFloat(), dp(18).toFloat(), Path.Direction.CW)
                    paint.style = Paint.Style.FILL
                    paint.color = panel
                    canvas.drawPath(path, paint)
                    canvas.save()
                    canvas.clipPath(path)
                    paint.color = c
                    canvas.drawRect(0f, 0f, dp(6).toFloat(), r.height(), paint)
                    canvas.restore()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = dp(1).toFloat()
                    paint.color = line
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.FILL
                    paint.color = bg
                    canvas.drawCircle(dp(6).toFloat(), dp(85).toFloat(), dp(7).toFloat(), paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = line
                    canvas.drawCircle(dp(6).toFloat(), dp(85).toFloat(), dp(7).toFloat(), paint)
                }

                override fun setAlpha(a: Int) {}

                override fun setColorFilter(f: ColorFilter?) {}

                @Deprecated("Deprecated") override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
        val date = row()
        date.addView(text(format(d), 11, dim, monoFont), LinearLayout.LayoutParams(0, -2, 1f))
        date.addView(text(if (second) "GRUPO $group" else "EQUIPO $team", 10, c, monoFont))
        add(box, date)
        add(box, text(CalendarRules.label(t).uppercase(es), 32, c, titleFont))
        var sub =
            when (t) {
                "descanso" -> if (today) "Hoy no trabajas" else "Día libre"
                "vacaciones" -> "Día de vacaciones"
                "guardia" ->
                    when (s.guard(d, team)) {
                        "trabaja_dia" -> "Cubres guardia en turno DÍA"
                        "trabaja_noche" -> "Cubres guardia en turno NOCHE"
                        "no_trabaja" -> "Guardia cancelada (Día libre)"
                        else -> "Disponible para cubrir turno"
                    }
                else -> if (today) "Hoy trabajas" else "Día de trabajo"
            }
        val y = d.minusDays(1)
        if (
            today &&
                t == "descanso" &&
                LocalTime.now().hour < 8 &&
                (CalendarRules.status(y, team) == "noche" ||
                    (CalendarRules.status(y, team) == "guardia" &&
                        s.guard(y, team) == "trabaja_noche"))
        )
            sub = "Trabajas hasta las 8 de la mañana"
        add(box, text(sub, 14, dim, monoFont))
        val pill = text("●  ${hours(d)}", 13, c, monoFont)
        pill.setPadding(dp(12), dp(7), dp(12), dp(7))
        pill.background = shape(bg, line, 99)
        box.addView(
            pill,
            LinearLayout.LayoutParams(-2, -2).apply {
                topMargin = dp(10)
                bottomMargin = dp(8)
            },
        )
        if (CalendarRules.holiday(d)) add(box, text("🎺 Día festivo", 12, accent("festivo")))
        if (second && CalendarRules.change223(d))
            add(box, text("⇄ Cambio de turno", 12, accent("cambio")))
        if (today) {
            val next =
                (1L..28).map { d.plusDays(it) }.firstOrNull { CalendarRules.status(it, team) != t }
            next?.let {
                add(
                    box,
                    text(
                        "⇄ Próximo cambio: ${CalendarRules.label(CalendarRules.status(it,team)).lowercase(es)}\n${format(it)}",
                        11,
                        dim,
                    ),
                )
            }
        }
        if (second) {
            add(
                box,
                text(
                    "Equipo ${s.text("profileTeam","C")} · ${if(s.text("shift","dia")=="dia")"Turno Día" else "Turno Noche"} · ${s.text("area"+s.text("activeArea","1")).ifEmpty{"Sin área"}}",
                    11,
                    dim,
                ),
            )
            val ts = if (group == "AB") listOf("A", "B") else listOf("C", "D")
            ts.forEach {
                add(box, text("Equipo $it · ${s.text("member2$it").ifEmpty{"Sin nombres"}}", 12))
            }
        }
    }

    private fun strip() {
        stripTiles = mutableMapOf()
        val h = HorizontalScrollView(this).apply {

            isHorizontalScrollBarEnabled = true
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val r = row()
        // WebView used ~6.5 day chips per row, never four oversized cards.
        val tileWidth = ((resources.displayMetrics.widthPixels / resources.displayMetrics.density - 32f) / 5.9f)
            .toInt().coerceIn(52, 67)
        val today = LocalDate.now()
        val start = if (selected.isBefore(today) || selected.isAfter(today.plusDays(29)))
            selected.minusDays(3) else today
        for (i in 0L..29) {
            val d = start.plusDays(i)
            val t = type(d)
            val c = accent(t)
            val col = vertical()
            col.gravity = Gravity.CENTER
            col.setPadding(dp(3), dp(7), dp(3), dp(7))
            col.background = stripSurface(t, d == selected)
            stripTiles[d] = col
            col.addView(text(d.format(DateTimeFormatter.ofPattern("EEE", es)).uppercase(es), 10, dim, monoFont).apply {
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)
            })
            col.addView(text("${d.dayOfMonth}", 16, c, titleFont).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, dp(3))
            })
            col.addView(
                text(
                    when (t) {
                        "dia" -> "DÍA"
                        "noche" -> "NOCHE"
                        "guardia" -> "GUARDIA"
                        "trabajo" -> "TRABAJO"
                        else -> "LIBRE"
                    },
                    9,
                    c,
                    monoFont,
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 0)
                }
            )
            val tile = FrameLayout(this)
            tile.addView(col, FrameLayout.LayoutParams(-1, -1))
            if (deviceCalendarEvents[d]?.isNotEmpty() == true) {
                tile.addView(reminderBulb(11), FrameLayout.LayoutParams(
                    dp(11), dp(11), Gravity.TOP or Gravity.END,
                ).apply {
                    topMargin = dp(3)
                    marginEnd = dp(3)
                })
            }
            tile.setOnClickListener { selectDate(d) }
            tile.isFocusable = true
            tile.contentDescription = "${format(d)}: ${CalendarRules.label(t)}" +
                (if (deviceCalendarEvents[d]?.isNotEmpty() == true) ", con recordatorio" else "")
            r.addView(tile, LinearLayout.LayoutParams(dp(tileWidth), dp(72)).apply { marginEnd = dp(6) })
        }
        h.addView(r)
        stripStartDate = start
        stripSection = h
        add(body, h)
    }


    private fun reminderColor() = color(if (dark) "#FFC75B" else "#805400")

    private fun reminderBulb(size: Int) = ImageView(this).apply {
        setImageResource(R.drawable.ic_reminder_bulb)
        setColorFilter(reminderColor())
        val px = dp(size)
        layoutParams = LinearLayout.LayoutParams(px, px)
        contentDescription = "Recordatorios"
    }

    private fun calendarSyncControls() {
        val controls = card()
        val r = row()
        val label = text("Calendario Android", 13, ink, titleFont)
        r.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
        r.addView(reminderBulb(16))
        add(controls, r)
        add(controls, text(deviceCalendarStatus, 11, dim))
        add(controls, button("Sincronizar calendario", gold) {
            if (deviceCalendar.hasPermission()) {
                chooseDeviceCalendars()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Leer calendarios de Android")
                    .setMessage("Mi Turno mostrará las citas de los calendarios que elijas. Los detalles se mantienen en memoria durante esta sesión.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Continuar") { _, _ ->
                        requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), 101)
                    }
                    .show()
            }
        })
    }

    // The calendar provider can block; never query it on the UI thread.
    private fun chooseDeviceCalendars() {
        if (!deviceCalendar.hasPermission()) return
        deviceCalendarWorker.execute {
            try {
                val choices = deviceCalendar.availableCalendars()
                deviceCalendarUi.post {
                    if (isFinishing || isDestroyed || !deviceCalendar.hasPermission()) return@post
                    if (choices.isEmpty()) {
                        toast("No hay calendarios visibles en Android")
                        return@post
                    }
                    val names = choices.map { it.displayName }.toTypedArray()
                    val chosen = chosenCalendarIds().intersect(choices.map { it.id }.toSet()).toMutableSet()
                    val checked = BooleanArray(choices.size) { choices[it].id in chosen }
                    AlertDialog.Builder(this)
                        .setTitle("Elegir calendarios para leer")
                        .setMultiChoiceItems(names, checked) { _, index, on ->
                            if (on) chosen.add(choices[index].id) else chosen.remove(choices[index].id)
                        }
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Sincronizar") { _, _ ->
                            if (chosen.isEmpty()) {
                                toast("Selecciona al menos un calendario")
                            } else {
                                syncDeviceCalendars(chosen.toSet())
                            }
                        }
                        .show()
                }
            } catch (_: Exception) {
                deviceCalendarUi.post { toast("No se pudieron consultar los calendarios del dispositivo") }
            }
        }
    }

    private fun syncDeviceCalendars(ids: Set<Long>) {
        val requestId = ++deviceCalendarRequest
        val today = LocalDate.now()
        val periods = listOf(
            month to month.plusMonths(1).minusDays(1),
            today to today.plusDays(29),
            selected to selected,
        )
        val zone = ZoneId.systemDefault()
        deviceCalendarStatus = "Sincronizando…"
        render()
        deviceCalendarWorker.execute {
            try {
                val index = linkedMapOf<LocalDate, MutableList<DeviceCalendarEvent>>()
                for ((from, through) in periods) {
                    val events = deviceCalendar.readVisibleEvents(from, through, ids, zone)
                    for ((date, items) in ReminderDayIndex.build(events, from, through, zone)) {
                        index.getOrPut(date) { mutableListOf() }.addAll(items)
                    }
                }
                val ready = index.mapValues { (_, items) ->
                    items.distinctBy { it.occurrenceKey }.sortedBy { it.startMillis }
                }
                deviceCalendarUi.post {
                    if (requestId != deviceCalendarRequest || isFinishing || isDestroyed) return@post
                    if (!deviceCalendar.hasPermission()) {
                        deviceCalendarEvents = emptyMap()
                        deviceCalendarSynced = false
                        deviceCalendarStatus = "Permiso de calendario revocado"
                    } else {
                        // Commit the selected sources and their results together.
                        sourcesPrefs.edit()
                            .putString("selected_ids", ids.sorted().joinToString(","))
                            .apply()
                        deviceCalendarEvents = ready
                        deviceCalendarSynced = true
                        deviceCalendarStatus = "Sincronización completada · ${ready.values.sumOf { it.size }} recordatorios por fecha"
                    }
                    render()
                }
            } catch (_: Exception) {
                deviceCalendarUi.post {
                    if (requestId == deviceCalendarRequest && !isFinishing && !isDestroyed) {
                        deviceCalendarStatus = "No se pudo sincronizar. Se conservan los resultados anteriores."
                        render()
                    }
                }
            }
        }
    }

    private fun showDeviceReminders(panel: LinearLayout, date: LocalDate) {
        val events = deviceCalendarEvents[date].orEmpty()
        if (events.isEmpty()) return
        heading(panel, if (events.size == 1) "Tienes un recordatorio" else "Tienes ${events.size} recordatorios")
        for (event in events) {
            val zone = if (event.allDay) ZoneOffset.UTC else ZoneId.systemDefault()
            val from = Instant.ofEpochMilli(event.startMillis).atZone(zone)
            val to = Instant.ofEpochMilli(event.endMillisExclusive).atZone(zone)
            val period = if (event.allDay) {
                "Todo el día · ${from.toLocalDate()}" +
                    (if (to.toLocalDate().isAfter(from.toLocalDate().plusDays(1)))
                        " al ${to.toLocalDate().minusDays(1)}" else "")
            } else "${from.toLocalDate()} ${from.toLocalTime()} — ${to.toLocalDate()} ${to.toLocalTime()}"
            add(panel, text("💡 ${event.title}", 14, ink, titleFont))
            add(panel, text(period, 12, dim))
            event.location?.takeIf { it.isNotBlank() }?.let { add(panel, text("Lugar: $it", 12, dim)) }
            event.description?.takeIf { it.isNotBlank() }?.let { add(panel, text(it, 12, ink)) }
            event.calendarName?.let { add(panel, text("Calendario: $it", 10, dim)) }
        }
    }

    private fun selectDate(d: LocalDate) {
        hasSelection = true
        selected = d
        month = d.withDayOfMonth(1)
        render()
        scroll.post { scroll.smoothScrollTo(0, lookupPanel?.top ?: 0) }
    }

    private fun lookup() {
        val p = card()
        lookupPanel = p
        heading(p, if (second) "¿Qué me toca el...?" else "Consultar una fecha")
        val r = row()
        r.addView(
            button(selected.toString(), gold) { pick(selected) { selectDate(it) } },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        r.addView(button("📅 Hoy") { selectDate(LocalDate.now()) })
        r.addView(button("+1") { selectDate(selected.plusDays(1)) })
        add(p, r)
        val t = effective(selected)
        add(p, text(format(selected), 12, dim))
        add(p, text(CalendarRules.label(t), 22, accent(t), titleFont))
        add(p, text(hours(selected), 14, accent(t), monoFont))
        if (CalendarRules.holiday(selected)) add(p, text("🎺 Día festivo", 12, accent("festivo")))
        showDeviceReminders(p, selected)
        if (second) {
            if (CalendarRules.change223(selected))
                add(p, text("⇄ Cambio de turno", 12, accent("cambio")))
            add(
                p,
                text(
                    "Equipo ${s.text("profileTeam","C")} · ${s.text("shift","dia")} · ${s.text("area"+s.text("activeArea","1"))}",
                    12,
                    dim,
                ),
            )
            (if (group == "AB") listOf("A", "B") else listOf("C", "D")).forEach {
                add(p, text("Equipo $it · ${s.text("member2$it").ifEmpty{"Sin nombres"}}", 12))
            }
        }
    }

    private fun pick(initial: LocalDate, done: (LocalDate) -> Unit) {
        DatePickerDialog(
                this,
                { _, y, m, d -> done(LocalDate.of(y, m + 1, d)) },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth,
            )
            .show()
    }

    private fun manage() {
        val guard = CalendarRules.status(selected, team) == "guardia"
        val p = card()
        manageSection = p
        heading(p, if (guard) "🛡 Gestionar Guardia" else "🌴 Gestionar Vacaciones")
        add(p, text("${format(selected)} · Equipo $team", 12, dim))
        if (guard) {
            val vals = arrayOf("", "no_trabaja", "trabaja_dia", "trabaja_noche")
            val names =
                arrayOf(
                    "Sin definir · Disponible",
                    "No la trabajo · Día libre",
                    "Sí, turno DÍA · 08:00 – 20:00",
                    "Sí, turno NOCHE · 20:00 – 08:00",
                )
            val current = vals.indexOf(s.guard(selected, team)).coerceAtLeast(0)
            add(
                p,
                button(names[current], accent("guardia")) {
                    AlertDialog.Builder(this)
                        .setTitle("Cubrir guardia")
                        .setSingleChoiceItems(names, current) { dialog, i ->
                            s.update("guards", "${selected}_$team", vals[i])
                            dialog.dismiss()
                            Reminders.schedule(this)
                            render()
                            toast("Guardia guardada")
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                },
            )
        } else {
            val checked = s.vacation(selected, team)
            add(
                p,
                button(
                    if (checked) "🌴 Vacaciones registradas · Quitar"
                    else "Registrar día completo de vacaciones",
                    accent("vacaciones"),
                ) {
                    s.update("vacations", "${selected}_$team", if (checked) "" else "si")
                    render()
                    toast("Vacaciones guardadas")
                },
            )
        }
    }

    private fun compactMonthControl(label: String, action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 15f
            typeface = mainFont
            setTextColor(ink)
            gravity = Gravity.CENTER
            minHeight = dp(34)
            setPadding(dp(4), 0, dp(4), 0)
            background = RippleDrawable(
                android.content.res.ColorStateList.valueOf(tint(gold, 50)),
                shape(if (dark) color("#1A1A22") else color("#F5F6FA"), line, 8),
                null,
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    private fun monthCard() {
        val p = card()
        monthSection = p
        val nav = row()
        nav.addView(
            compactMonthControl("‹") {
                month = month.minusMonths(1)
                render()
            },
            LinearLayout.LayoutParams(dp(34), dp(34)),
        )
        val label =
            compactMonthControl(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", es))) {
                pick(month) {
                    month = it.withDayOfMonth(1)
                    render()
                }
            }.apply { typeface = titleFont; textSize = 14f }
        nav.addView(label, LinearLayout.LayoutParams(0, dp(34), 1f).apply {
            marginStart = dp(8)
            marginEnd = dp(8)
        })
        nav.addView(
            compactMonthControl("›") {
                month = month.plusMonths(1)
                render()
            },
            LinearLayout.LayoutParams(dp(34), dp(34)),
        )
        add(p, nav)
        val grid = CalendarGrid()
        monthGrid = grid
        add(p, grid)
        if (second) {
            add(p, text("● Trabajo    ● Descanso    🧊 No laborable\n⇄ Cambio    ◉ Hoy", 11, dim))
        } else {
            val legend = row()
            listOf("dia", "noche", "guardia", "descanso").forEach {
                legend.addView(
                    text(
                        "● " +
                            when (it) {
                                "dia" -> "Día"
                                "noche" -> "Noche"
                                "guardia" -> "Guardia"
                                else -> "Libre"
                            },
                        10,
                        accent(it),
                    ),
                    LinearLayout.LayoutParams(0, -2, 1f),
                )
            }
            add(p, legend)
            add(p, text("🎺 Festivo  ·  🌴 Vacaciones  ·  ◉ Hoy", 11, dim))
        }
        val remindersLegend = row()
        remindersLegend.addView(reminderBulb(15))
        add(remindersLegend, text("Recordatorios", 11, dim))
        add(p, remindersLegend)
        toggle(p, "⭐ Resaltar descansos compartidos con C/D", "stars")
        toggle(p, "🛡 Resaltar guardia de A en descanso C/D", "guardsVisible")
    }

    private fun toggle(p: LinearLayout, label: String, key: String) {
        val sw =
            Switch(this).apply {
                text = label
                textSize = 11f
                setTextColor(dim)
                isChecked = s.text((if (second) "2" else "1") + key, "on") == "on"
                setPadding(0, dp(10), 0, dp(5))
                setOnCheckedChangeListener { _, v ->
                    s.put((if (second) "2" else "1") + key, if (v) "on" else "off")
                    render()
                }
            }
        add(p, sw)
    }

    private fun summary() {
        val p = card()
        summarySection = p
        heading(p, "Personal del día")
        add(p, text(format(selected), 12, dim))
        for (t in listOf("dia", "noche", "guardia", "descanso", "vacaciones")) {
            val teams =
                listOf("A", "B", "C", "D").filter {
                    (if (s.vacation(selected, it)) "vacaciones"
                    else CalendarRules.status(selected, it)) == t
                }
            if (teams.isNotEmpty()) {
                add(p, text(CalendarRules.label(t).uppercase(es), 11, accent(t), monoFont))
                teams.forEach {
                    val g = s.guard(selected, it)
                    add(
                        p,
                        text(
                            "Equipo $it · ${s.text("member1$it").ifEmpty{"Sin nombres"}}${if(t=="guardia")" · "+when(g){"trabaja_dia"->"Cubre día"
"trabaja_noche"->"Cubre noche"
"no_trabaja"->"Cancelada"
else->"Disponible"}else""}",
                            13,
                        ),
                    )
                }
            }
        }
    }

    inner class CalendarGrid : View(this@MainActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private val offset = month.dayOfWeek.value - 1
        private val rows = (offset + month.lengthOfMonth() + 6) / 7
        private var cellHeight = dp(55).toFloat()

        init {
            isClickable = true
            contentDescription =
                "Calendario ${month}. Selecciona una fecha con el selector superior o toca un día."
        }

        override fun onMeasure(w: Int, h: Int) {
            cellHeight = MeasureSpec.getSize(w) / 7f
            setMeasuredDimension(MeasureSpec.getSize(w), (dp(32) + rows * cellHeight).toInt())
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cw = width / 7f
            val ch = cellHeight
            p.typeface = monoFont
            p.textAlign = Paint.Align.CENTER
            p.textSize = dp(11).toFloat()
            p.color = dim
            listOf("L", "M", "M", "J", "V", "S", "D").forEachIndexed { i, v ->
                canvas.drawText(v, cw * (i + .5f), dp(18).toFloat(), p)
            }
            for (day in 1..month.lengthOfMonth()) {
                val d = month.withDayOfMonth(day)
                val index = offset + day - 1
                val x = (index % 7) * cw
                val y = dp(30) + (index / 7) * ch
                val t = type(d)
                val c = accent(t)
                val rect = RectF(x + dp(3), y + dp(3), x + cw - dp(3), y + ch - dp(3))
                p.style = Paint.Style.FILL
                p.color = soft(t)
                canvas.drawRoundRect(rect, dp(10).toFloat(), dp(10).toFloat(), p)
                if (
                    d == LocalDate.now() ||
                        (hasSelection && d == selected) ||
                        (!second && s.vacation(d, team)) ||
                        (second && (CalendarRules.holiday(d) || CalendarRules.change223(d)))
                ) {
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = dp(2).toFloat()
                    p.color =
                        when {
                            hasSelection && d == selected -> ink
                            d == LocalDate.now() -> gold
                            CalendarRules.holiday(d) || s.vacation(d, team) -> accent("vacaciones")
                            else -> accent("cambio")
                        }
                    canvas.drawRoundRect(rect, dp(10).toFloat(), dp(10).toFloat(), p)
                    p.style = Paint.Style.FILL
                }
                p.color = c
                p.textSize = dp(if (second) 13 else 12).toFloat()
                canvas.drawText(day.toString(), x + cw / 2, y + ch / 2 + dp(4), p)
                var marks = ""
                val rests = if (second) t == "descanso" else CalendarRules.restCD(d)
                if (
                    s.text((if (second) "2" else "1") + "stars", "on") == "on" &&
                        (if (second) rests && CalendarRules.status(d, "A") == "descanso"
                        else t == "descanso" && rests)
                )
                    marks += "⭐"
                if (
                    s.text((if (second) "2" else "1") + "guardsVisible", "on") == "on" &&
                        rests &&
                        CalendarRules.status(d, "A") == "guardia"
                )
                    marks += "🛡"
                if (CalendarRules.holiday(d)) marks += if (second) "🧊" else "🎺"
                if (!second && s.vacation(d, team)) marks += "🌴"
                if (second && CalendarRules.change223(d)) marks += "↻"
                p.textSize = dp(8).toFloat()
                canvas.drawText(marks, x + cw / 2, y + dp(13), p)
                if (deviceCalendarEvents[d]?.isNotEmpty() == true) {
                    val bulb = resources.getDrawable(R.drawable.ic_reminder_bulb, theme)
                    bulb.setTint(reminderColor())
                    val size = dp(13)
                    bulb.setBounds(
                        (x + cw - size - dp(5)).toInt(),
                        (y + ch - size - dp(4)).toInt(),
                        (x + cw - dp(5)).toInt(),
                        (y + ch - dp(4)).toInt()
                    )
                    bulb.draw(canvas)
                }
            }
        }

        override fun onTouchEvent(e: android.view.MotionEvent): Boolean {
            if (e.action == android.view.MotionEvent.ACTION_UP) {
                val col = (e.x / (width / 7f)).toInt()
                val row = ((e.y - dp(30)) / cellHeight).toInt()
                val n = row * 7 + col - offset + 1
                if (e.y >= dp(30) && n in 1..month.lengthOfMonth()) {
                    performClick()
                    selectDate(month.withDayOfMonth(n))
                }
                return true
            }
            return true
        }

        override fun performClick(): Boolean {
            super.performClick()
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            return true
        }
    }

    private fun input(
        p: LinearLayout,
        label: String,
        value: String = "",
        numeric: Boolean = false,
    ): EditText {
        add(p, text(label, 12, dim))
        return EditText(this).apply {
            setText(value)
            setTextColor(ink)
            setHintTextColor(dim)
            textSize = 14f
            typeface = mainFont
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = shape(bg, line, 10)
            if (numeric)
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            add(p, this)
        }
    }

    private fun extras() {
        header("Horas Extra", "REGISTROS Y REPORTES")
        val total = card()
        add(total, text("TOTAL ACUMULADO", 11, dim, monoFont))
        add(total, text("${s.total()} horas", 34, accent("descanso"), titleFont))
        add(
            total,
            text(
                "Cada guardia trabajada suma 12 h, más las fracciones registradas. Acumulado de todos los equipos.",
                12,
                dim,
            ),
        )
        val reg = card()
        heading(reg, "⏱ Registrar fracción / Horas extra")
        var date = selected
        val dateBtn = button(date.toString(), gold) {}
        dateBtn.setOnClickListener {
            pick(date) {
                date = it
                dateBtn.text = it.toString()
            }
        }
        add(reg, dateBtn)
        val hours = input(reg, "Horas trabajadas", numeric = true)
        val note = input(reg, "Nota (opcional)")
        add(
            reg,
            text(
                "Un registro por fecha; guardar de nuevo reemplaza la fracción de ese día.",
                11,
                dim,
            ),
        )
        add(
            reg,
            button("Guardar Fracción", gold) {
                val h = hours.text.toString().replace(',', '.').toDoubleOrNull()
                if (h == null || !h.isFinite() || h <= 0)
                    toast("Introduce horas válidas mayores que cero")
                else {
                    val f = s.data("fractions")
                    f.put(
                        date.toString(),
                        JSONObject()
                            .put("hours", h)
                            .put(
                                "note",
                                note.text.toString().trim().ifEmpty { "Tiempo extra parcial" },
                            ),
                    )
                    s.put("fractions", f.toString())
                    render()
                    toast("Fracción guardada")
                }
            },
        )
        val rep = card()
        heading(rep, "Reporte de guardias y vacaciones")
        val rr = row()
        rr.addView(
            button("Mes actual") {
                val now = LocalDate.now()
                reportStart = now.withDayOfMonth(1).toString()
                reportEnd = now.withDayOfMonth(now.lengthOfMonth()).toString()
                render()
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        rr.addView(
            button("Todo") {
                reportStart = ""
                reportEnd = ""
                render()
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        add(rep, rr)
        val dates = row()
        dates.addView(
            button(reportStart.ifEmpty { "Desde" }, gold) {
                pick(LocalDate.now()) {
                    reportStart = it.toString()
                    render()
                }
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        dates.addView(
            button(reportEnd.ifEmpty { "Hasta" }, gold) {
                pick(LocalDate.now()) {
                    reportEnd = it.toString()
                    render()
                }
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        add(rep, dates)
        add(
            rep,
            button("Ver reporte") {
                validReport {
                    AlertDialog.Builder(this)
                        .setTitle("Reporte")
                        .setMessage(it)
                        .setPositiveButton("Cerrar", null)
                        .show()
                }
            },
        )
        val actions = row()
        actions.addView(
            button("↓ TXT") {
                validReport {
                    exportText = it
                    startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TITLE, "Mi-Turno-Reporte-${LocalDate.now()}.txt")
                        },
                        200,
                    )
                }
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        actions.addView(
            button("Copiar") {
                validReport {
                    getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("Reporte Mi Turno", it))
                    toast("Reporte copiado")
                }
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        actions.addView(
            button("Compartir") {
                validReport {
                    startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, it)
                            },
                            "Compartir reporte",
                        )
                    )
                }
            },
            LinearLayout.LayoutParams(0, -2, 1f),
        )
        add(rep, actions)
        val records = card()
        heading(records, "Historial")
        val g = s.data("guards")
        val f = s.data("fractions")
        if (g.length() + f.length() == 0)
            add(
                records,
                text("Aún no has registrado ninguna guardia o fracción como trabajada.", 12, dim),
            )
        g.keys().asSequence().sorted().forEach { k ->
            val v = g.getString(k)
            add(
                records,
                text(
                    "${k.substringBefore('_')} · Equipo ${k.substringAfter('_')}\n${when(v){"trabaja_dia"->"Turno DÍA · +12 h"
"trabaja_noche"->"Turno NOCHE · +12 h"
else->"Guardia cancelada"}}",
                    13,
                    accent("guardia"),
                ),
            )
        }
        f.keys().asSequence().sorted().forEach { k ->
            val v = f.getJSONObject(k)
            add(records, text("$k · +${v.getDouble("hours")} h\n${v.getString("note")}", 13, gold))
            add(records, button("Editar fracción de $k", dim) { editFraction(k) })
        }
    }

    private fun editFraction(k: String) {
        val f = s.data("fractions")
        val old = f.getJSONObject(k)
        val p = vertical().apply { setPadding(dp(20), dp(5), dp(20), 0) }
        val h = input(p, "Horas", old.getDouble("hours").toString(), true)
        val n = input(p, "Nota", old.getString("note"))
        val dialog =
            AlertDialog.Builder(this)
                .setTitle(k)
                .setView(p)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val v = h.text.toString().replace(',', '.').toDoubleOrNull()
                if (v == null || !v.isFinite() || v <= 0) toast("Horas no válidas")
                else {
                    f.put(
                        k,
                        JSONObject()
                            .put("hours", v)
                            .put(
                                "note",
                                n.text.toString().trim().ifEmpty { "Tiempo extra parcial" },
                            ),
                    )
                    s.put("fractions", f.toString())
                    dialog.dismiss()
                    render()
                }
            }
        }
        dialog.show()
    }

    private fun validReport(done: (String) -> Unit) {
        if (reportStart.isNotEmpty() && reportEnd.isNotEmpty() && reportStart > reportEnd)
            toast("La fecha inicial debe ser anterior a la final")
        else done(s.report(reportStart, reportEnd))
    }

    private fun settings() {
        header("Equipos y ajustes", "PERSONALIZA MI TURNO")
        if (second) {
            val profile = card()
            heading(profile, "Mi Perfil de Trabajo")
            val te = arrayOf("A", "B", "C", "D")
            add(
                profile,
                button("Equipo ${s.text("profileTeam","C")}", gold) {
                    AlertDialog.Builder(this)
                        .setTitle("Equipo")
                        .setItems(te) { _, i ->
                            s.put("profileTeam", te[i])
                            render()
                        }
                        .show()
                },
            )
            add(
                profile,
                button("Turno ${s.text("shift","dia")}", gold) {
                    AlertDialog.Builder(this)
                        .setTitle("Turno")
                        .setItems(arrayOf("Día · 08–20", "Noche · 20–08")) { _, i ->
                            s.put("shift", if (i == 0) "dia" else "noche")
                            persist()
                            render()
                        }
                        .show()
                },
            )
            heading(profile, "Mostrar en el Calendario")
            toggle(profile, "⭐ Resaltar descansos compartidos con el Equipo A", "stars")
            toggle(profile, "🛡 Resaltar guardias de A en mis días de descanso", "guardsVisible")
            heading(profile, "Mis Áreas (máximo 4)")
            val areas = (1..4).associateWith { input(profile, "Área $it", s.text("area$it")) }
            add(
                profile,
                button("Guardar áreas") {
                    areas.forEach { (i, v) -> s.put("area$i", v.text.toString().trim()) }
                    render()
                    toast("Áreas guardadas")
                },
            )
            add(
                profile,
                button(
                    "Área activa: ${s.text("area"+s.text("activeArea","1")).ifEmpty{"Sin área"}}",
                    gold,
                ) {
                    val values = (1..4).filter { s.text("area$it").isNotEmpty() }
                    if (values.isEmpty()) toast("Escribe y guarda un área primero")
                    else
                        AlertDialog.Builder(this)
                            .setTitle("Área activa")
                            .setItems(values.map { s.text("area$it") }.toTypedArray()) { _, i ->
                                s.put("activeArea", values[i].toString())
                                render()
                            }
                            .show()
                },
            )
        }
        val p = card()
        heading(p, "👥 Integrantes por Equipo")
        add(p, text("Los integrantes se guardan por separado para cada calendario.", 12, dim))
        val names = mutableMapOf<String, EditText>()
        for (t in listOf("A", "B", "C", "D")) names[t] =
            input(p, "Equipo $t", s.text("member${if(second)2 else 1}$t"))
        add(
            p,
            button("Guardar integrantes") {
                names.forEach { (t, v) ->
                    s.put("member${if(second)2 else 1}$t", v.text.toString())
                }
                toast("Integrantes guardados")
            },
        )
        val appearance = card()
        heading(appearance, "Iluminación ambiental")
        add(appearance, text("Luz neón suave detrás de las tarjetas. Se pausa al salir de la aplicación y respeta las animaciones desactivadas en Android.", 12, dim))
        val ambientSwitch = Switch(this).apply {
            text = "Fondo neón animado"
            textSize = 12f
            setTextColor(ink)
            isChecked = ambientEnabled()
            setOnCheckedChangeListener { _, active ->
                s.put("ambientGlow", if (active) "on" else "off")
                render()
            }
        }
        add(appearance, ambientSwitch)
        val reminders = card()
        if (drawingInDrawer && drawerScreen == "reminders") drawerRemindersAnchor = reminders
        heading(reminders, "🔔 Recordatorio del próximo turno")
        add(
            reminders,
            text(
                "Te avisa cada día del turno de mañana para el calendario y equipo seleccionados. Android puede retrasar el aviso por ahorro de batería.",
                12,
                dim,
            ),
        )
        val enabled = s.text("remind") == "on"
        add(
            reminders,
            button(
                if (enabled) "✓ Recordatorios activados · Desactivar" else "Activar recordatorios"
            ) {
                if (enabled) {
                    s.put("remind", "off")
                    Reminders.schedule(this)
                    render()
                } else if (
                    Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission("android.permission.POST_NOTIFICATIONS") !=
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                )
                    requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 100)
                else {
                    s.put("remind", "on")
                    Reminders.schedule(this)
                    render()
                }
            },
        )
        val hr = s.text("remindHour", "19").toInt()
        val min = s.text("remindMinute", "0").toInt()
        add(
            reminders,
            button("Hora del aviso: %02d:%02d".format(hr, min), gold) {
                TimePickerDialog(
                        this,
                        { _, h, m ->
                            s.put("remindHour", h.toString())
                            s.put("remindMinute", m.toString())
                            Reminders.schedule(this)
                            render()
                        },
                        hr,
                        min,
                        true,
                    )
                    .show()
            },
        )
        add(
            reminders,
            button("Probar notificación") {
                if (
                    Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission("android.permission.POST_NOTIFICATIONS") !=
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                )
                    toast("Activa primero el permiso de notificaciones")
                else {
                    if (Reminders.notify(this, true))
                        toast("Notificación de prueba enviada")
                    else
                        toast("Notificación bloqueada. Revisa los ajustes del sistema")
                }
            },
        )
        add(reminders, button("⚙ Ajustar sonido y vibración en Android", gold) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, "turnos")
            try { startActivity(intent) } catch (_: Exception) { toast("Abre las notificaciones desde Ajustes de Android") }
        })
        val backup = card()
        heading(backup, "Copia de seguridad")
        add(
            backup,
            text("Exporta o restaura tus registros locales, integrantes y ajustes.", 12, dim),
        )
        add(
            backup,
            button("Exportar copia JSON") {
                val j = JSONObject()
                s.prefs.all.forEach { (k, v) -> j.put(k, v.toString()) }
                exportText = j.toString(2)
                startActivityForResult(
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = "application/json"
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, "Mi-Turno-Copia-${LocalDate.now()}.json")
                    },
                    201,
                )
            },
        )
        add(
            backup,
            button("Importar copia JSON", gold) {
                startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "application/json"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    },
                    202,
                )
            },
        )
        val about = card()
        heading(about, "Mi Turno · Android 1.0.0")
        add(
            about,
            text(
                "Interfaz nativa Kotlin · Funciona sin conexión\nCiclo 28 días y rotación 2-2-3 del archivo original. Festivos originales de 2026.\nLos datos de la web no se transfieren automáticamente.",
                12,
                dim,
            ),
        )
        add(
            about,
            button("Licencias de fuentes y Kotlin", dim) {
                val items = assets.list("licenses") ?: emptyArray()
                AlertDialog.Builder(this)
                    .setTitle("Licencias")
                    .setItems(items) { _, i ->
                        val content =
                            assets.open("licenses/" + items[i]).bufferedReader().use {
                                it.readText()
                            }
                        AlertDialog.Builder(this)
                            .setTitle(items[i])
                            .setMessage(content)
                            .setPositiveButton("Cerrar", null)
                            .show()
                    }
                    .show()
            },
        )
    }

    private fun holidays() {
        header("Días festivos", "FECHAS DEL CALENDARIO ORIGINAL")
        val p = card()
        add(
            p,
            text(
                "Se conserva la lista original de 2026. Los festivos se marcan sin modificar los ciclos ni los horarios.",
                13,
                dim,
            ),
        )
        CalendarRules.holidays.sorted().forEach {
            val d = LocalDate.parse(it)
            add(
                p,
                button("🎺 ${format(d)}", accent("festivo")) {
                    hasSelection = true
                    selected = d
                    month = d.withDayOfMonth(1)
                    if (drawingInDrawer) closeDrawer(immediate = true)
                    page = "calendar"
                    render()
                },
            )
        }
    }

    override fun onRequestPermissionsResult(
        code: Int,
        permissions: Array<out String>,
        results: IntArray,
    ) {
        super.onRequestPermissionsResult(code, permissions, results)
        if (code == 101) {
            if (results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED)
                chooseDeviceCalendars()
            else {
                deviceCalendarRequest++
                deviceCalendarEvents = emptyMap()
                deviceCalendarSynced = false
                deviceCalendarStatus = "Permiso no concedido"
                toast("El calendario laboral sigue funcionando sin este permiso")
                render()
            }
            return
        }
        if (code == 100) {
            if (
                results.isNotEmpty() &&
                    results[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                s.put("remind", "on")
                Reminders.schedule(this)
                toast("Recordatorios activados")
            } else toast("Puedes activar notificaciones desde Ajustes de Android")
            render()
        }
    }

    @Deprecated("Legacy activity result supported on API 26+")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (result != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            if (request == 200 || request == 201) {
                contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter(Charsets.UTF_8).use {
                    if (request == 200) it.write("\uFEFF")
                    it.write(exportText)
                }
                toast("Archivo guardado")
            } else if (request == 202) {
                val raw =
                    contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
                validateBackup(raw)
                AlertDialog.Builder(this)
                    .setTitle("Restaurar copia")
                    .setMessage(
                        "Reemplazará los registros y ajustes locales por esta copia. ¿Restaurar?"
                    )
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Restaurar") { _, _ ->
                        val j = JSONObject(raw)
                        val edit = s.prefs.edit().clear()
                        j.keys().forEach { edit.putString(it, j.getString(it)) }
                        edit.commit()
                        second = s.text("calendar", "1") == "2"
                        team = s.text("team", "A")
                        group = s.text("group", "CD")
                        dark = s.text("theme") == "dark"
                        Reminders.schedule(this)
                        render()
                        toast("Copia restaurada")
                    }
                    .show()
            }
        } catch (e: Exception) {
            toast("No se pudo abrir o guardar el archivo: ${e.message}")
        }
    }

    private fun validateBackup(raw: String) {
        require(raw.length < 2_000_000) { "Copia demasiado grande" }
        val j = JSONObject(raw)
        require(j.has("calendar")) { "No es una copia de Mi Turno" }
        require(j.optString("calendar") in listOf("1", "2"))
        require(j.optString("team", "A") in listOf("A", "B", "C", "D"))
        require(j.optString("group", "CD") in listOf("AB", "CD"))
        require(j.optString("shift", "dia") in listOf("dia", "noche"))
        require((j.optString("remindHour", "19").toIntOrNull() ?: -1) in 0..23)
        require((j.optString("remindMinute", "0").toIntOrNull() ?: -1) in 0..59)
        for (kind in listOf("guards", "vacations", "fractions")) {
            val o = JSONObject(j.optString(kind, "{}"))
            o.keys().forEach { k ->
                LocalDate.parse(k.substringBefore('_'))
                if (kind == "fractions") {
                    val item = o.getJSONObject(k)
                    require(item.getDouble("hours").isFinite() && item.getDouble("hours") > 0)
                    item.getString("note")
                } else {
                    require(k.substringAfter('_') in listOf("A", "B", "C", "D"))
                    require(
                        o.getString(k) in
                            if (kind == "guards")
                                listOf("no_trabaja", "trabaja_dia", "trabaja_noche")
                            else listOf("si")
                    )
                }
            }
        }
    }

    @Deprecated("Legacy back navigation")
    override fun onBackPressed() {
        when {
            drawerScreen != null -> drawerBack()
            page != "calendar" -> {
                page = "calendar"
                render(true)
            }
            else -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::s.isInitialized && ambientEnabled()) ambientBackdrop?.start()
        if (::s.isInitialized) {
            if (deviceCalendarSynced && !deviceCalendar.hasPermission()) {
                deviceCalendarRequest++
                deviceCalendarEvents = emptyMap()
                deviceCalendarSynced = false
                deviceCalendarStatus = "Permiso de calendario revocado"
                render()
            }
            Reminders.schedule(this)
            if (page == "calendar" && renderedToday != LocalDate.now()) {
                renderedToday = LocalDate.now()
                render()
            }
        }
        clock.removeCallbacks(refreshClock)
        clock.postDelayed(refreshClock, 60000)
    }

    override fun onDestroy() {
        updateDrawerBackCallback(false)
        ambientBackdrop?.stop()
        deviceCalendarRequest++
        deviceCalendarWorker.shutdownNow()
        super.onDestroy()
    }

    override fun onPause() {
        ambientBackdrop?.stop()
        clock.removeCallbacks(refreshClock)
        super.onPause()
    }
}
