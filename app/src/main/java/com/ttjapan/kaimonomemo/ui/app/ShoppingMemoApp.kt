package com.ttjapan.kaimonomemo.ui.app

import android.accounts.AccountManager
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ttjapan.kaimonomemo.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ttjapan.kaimonomemo.data.MicrophoneSettings
import com.ttjapan.kaimonomemo.data.assignDefaultTitleIfBlank
import com.ttjapan.kaimonomemo.data.loadKeepCompletedItemsInPlace
import com.ttjapan.kaimonomemo.data.loadLeftHandModeEnabled
import com.ttjapan.kaimonomemo.data.loadMicrophoneSettings
import com.ttjapan.kaimonomemo.data.loadModeSelectionCompleted
import com.ttjapan.kaimonomemo.data.loadMemos
import com.ttjapan.kaimonomemo.data.loadOneHandModeEnabled
import com.ttjapan.kaimonomemo.data.loadSimpleModeEnabled
import com.ttjapan.kaimonomemo.data.loadHomeTitlePattern
import com.ttjapan.kaimonomemo.data.loadEditHelpVisible
import com.ttjapan.kaimonomemo.data.loadLargeFontEnabled
import com.ttjapan.kaimonomemo.data.loadSupportAdWatchDate
import com.ttjapan.kaimonomemo.data.loadTemporaryTitlePattern
import com.ttjapan.kaimonomemo.data.saveLeftHandModeEnabled
import com.ttjapan.kaimonomemo.data.saveEditHelpVisible
import com.ttjapan.kaimonomemo.data.saveLargeFontEnabled
import com.ttjapan.kaimonomemo.data.saveMicrophoneSettings
import com.ttjapan.kaimonomemo.data.saveMemos
import com.ttjapan.kaimonomemo.data.saveOneHandModeEnabled
import com.ttjapan.kaimonomemo.data.saveSimpleModeEnabled
import com.ttjapan.kaimonomemo.data.saveHomeTitlePattern
import com.ttjapan.kaimonomemo.data.saveKeepCompletedItemsInPlace
import com.ttjapan.kaimonomemo.data.saveModeSelectionCompleted
import com.ttjapan.kaimonomemo.data.saveSupportAdWatchDate
import com.ttjapan.kaimonomemo.data.saveTemporaryTitlePattern
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import com.ttjapan.kaimonomemo.voice.ContinuousSpeechController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class Screen {
    Ads,
    Home,
    Detail,
    PatternPicker,
    TitlePatternPicker,
    Edit,
    Settings,
    Favorites
}

private enum class TitlePatternTarget {
    Home,
    Temporary
}

private enum class VoiceTarget {
    Title,
    Item
}

private enum class EditPaneSide {
    Left,
    Right
}

private data class EditDraggingEntry(
    val side: EditPaneSide,
    val memo: ShoppingMemo,
    val entry: ShoppingEntry
)

private enum class ScrollAnchor {
    Item,
    AddButton
}

private val BottomBarHeight = 86.dp
private val FabKeyboardGap = 10.dp
private val DetailListExtraBottom = 96.dp
private val ListTopAnchorHeight = 1.dp
private const val ListTopAnchorIndex = 0
private const val ListTopAnchorKey = "list-top-anchor"
private const val AddListItemKey = "add-list-item"
private const val DetailBackSwipeThresholdPx = 140f
private const val DragAutoReorderDelayMillis = 100L
private const val VoiceScrollStepPx = 24f
private const val VoiceScrollDelayMillis = 20L
private const val OneHandMaxOffsetRatio = 0.46f
private const val OneHandScrollSpeedMultiplier = 1.5f
private const val TemporaryMemoId = "temporary-shopping-memo"
private const val GoogleAccountType = "com.google"
private const val GmailPackageName = "com.google.android.gm"
private const val AdMobBannerAdUnitId = "ca-app-pub-2043305448409536/3094419810"
private const val AdMobRewardedAdUnitId = "ca-app-pub-2043305448409536/1888736665"
private const val HomeOperationScale = 0.88f
private val TrashTabSelectedColor = Color(0xFFE91E63)
private val SettingsPaleYellow = Color(0xFFFFF8D7)
private val SettingsPaleGreen = Color(0xFFEAF8E8)
private val GoldHighlightColors = listOf(
    Color(0xFFFFFDE7),
    Color(0xFFFFF59D),
    Color(0xFFFFFDE7)
)
private val PurpleHighlightColors = listOf(
    Color(0xFFF7F2FF),
    Color(0xFFE7DCFF),
    Color(0xFFF7F2FF)
)
private val HomeTitleHeaderHeight = 52.dp
private val HomeProgressBarHeight = 44.dp
private const val HomeCarouselTraceTag = "HomeCarouselTrace"
private const val EditMoveTraceTag = "EditMoveTrace"
private val CompletedEntryBackground = Color(0xFFE8E8E8)

private fun keyboardAnchoredFabBottomPadding(
    view: android.view.View,
    containerBounds: Rect?,
    imeVisible: Boolean,
    density: Density
): Dp {
    if (!imeVisible) return 22.dp
    val bounds = containerBounds ?: return FabKeyboardGap
    val visibleFrame = android.graphics.Rect()
    view.getWindowVisibleDisplayFrame(visibleFrame)
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val visibleKeyboardTop = (visibleFrame.bottom - location[1]).toFloat().takeIf { it > 0f }
    val imeKeyboardTop = ViewCompat.getRootWindowInsets(view)
        ?.getInsets(WindowInsetsCompat.Type.ime())
        ?.bottom
        ?.takeIf { it > 0 }
        ?.let { imeBottomPx ->
            val rootLocation = IntArray(2)
            view.rootView.getLocationOnScreen(rootLocation)
            (rootLocation[1] + view.rootView.height - imeBottomPx - location[1]).toFloat()
        }
        ?.takeIf { it > 0f }
    val keyboardTopInWindow = listOfNotNull(visibleKeyboardTop, imeKeyboardTop).minOrNull()
        ?: return FabKeyboardGap
    if (keyboardTopInWindow <= 0f) return FabKeyboardGap
    val overlapPx = (bounds.bottom - keyboardTopInWindow).coerceAtLeast(0f)
    return with(density) {
        (overlapPx + FabKeyboardGap.toPx()).toDp().coerceAtLeast(FabKeyboardGap)
    }
}

private fun Modifier.homeReturnSwipeOnRelease(
    enabled: Boolean = true,
    thresholdPx: Float = DetailBackSwipeThresholdPx,
    onHome: () -> Unit
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled, thresholdPx) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var totalX = 0f
            var totalY = 0f
            var verticalScrollDetected = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val delta = change.positionChange()
                totalX += delta.x
                totalY += delta.y
                if (
                    kotlin.math.abs(totalY) > viewConfiguration.touchSlop &&
                    kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 0.75f
                ) {
                    verticalScrollDetected = true
                }
            }
            if (
                totalX > thresholdPx &&
                !verticalScrollDetected &&
                kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f
            ) {
                onHome()
            }
        }
    }
}

private data class AppFontSizes(
    val listText: TextUnit,
    val listLineHeight: TextUnit,
    val listAction: TextUnit,
    val listPlaceholder: TextUnit,
    val settingsTitle: TextUnit,
    val settingsBody: TextUnit,
    val settingsBodyLineHeight: TextUnit,
    val settingsValue: TextUnit,
    val editEntry: TextUnit,
    val editEntryLineHeight: TextUnit,
    val editHelpTitle: TextUnit,
    val editHelpBody: TextUnit,
    val editHelpLineHeight: TextUnit
)

private fun appFontSizes(large: Boolean): AppFontSizes {
    return if (large) {
        AppFontSizes(
            listText = 23.sp,
            listLineHeight = 28.sp,
            listAction = 18.sp,
            listPlaceholder = 21.sp,
            settingsTitle = 20.sp,
            settingsBody = 16.sp,
            settingsBodyLineHeight = 21.sp,
            settingsValue = 18.sp,
            editEntry = 21.sp,
            editEntryLineHeight = 26.sp,
            editHelpTitle = 22.sp,
            editHelpBody = 17.sp,
            editHelpLineHeight = 22.sp
        )
    } else {
        AppFontSizes(
            listText = 20.sp,
            listLineHeight = 24.sp,
            listAction = 16.sp,
            listPlaceholder = 18.sp,
            settingsTitle = 17.sp,
            settingsBody = 13.sp,
            settingsBodyLineHeight = 18.sp,
            settingsValue = 16.sp,
            editEntry = 18.sp,
            editEntryLineHeight = 22.sp,
            editHelpTitle = 19.sp,
            editHelpBody = 15.sp,
            editHelpLineHeight = 19.sp
        )
    }
}

private val LocalAppFontSizes = staticCompositionLocalOf { appFontSizes(false) }

private fun Context.appLocale(): Locale = resources.configuration.locales[0]

private fun appLayoutDirection(locale: Locale): LayoutDirection {
    return if (locale.language.lowercase(Locale.ROOT) in RtlLanguageCodes) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
}

private val RtlLanguageCodes = setOf(
    "ar",
    "fa",
    "he",
    "iw",
    "ur",
    "ps",
    "sd",
    "ug",
    "yi"
)

private val PhysicalBottomLeftAlignment = object : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        return IntOffset(0, space.height - size.height)
    }
}

private val PhysicalBottomRightAlignment = object : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        return IntOffset(space.width - size.width, space.height - size.height)
    }
}

private val PhysicalTopLeftAlignment = object : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        return IntOffset.Zero
    }
}

private fun physicalBottomSideAlignment(leftSide: Boolean): Alignment {
    return if (leftSide) PhysicalBottomLeftAlignment else PhysicalBottomRightAlignment
}

private fun isTemporaryMemo(memo: ShoppingMemo): Boolean = memo.id == TemporaryMemoId

private fun memoDisplayTitle(context: Context, memo: ShoppingMemo): String {
    return if (isTemporaryMemo(memo)) {
        context.getString(R.string.temporary_memo_title)
    } else {
        memo.title.ifBlank { context.getString(R.string.title_untitled) }
    }
}

@Composable
private fun localizedMemoDisplayTitle(
    memo: ShoppingMemo,
    fallbackResId: Int = R.string.title_untitled
): String {
    val temporaryTitle = stringResource(R.string.temporary_memo_title)
    val fallback = stringResource(fallbackResId)
    return if (isTemporaryMemo(memo)) temporaryTitle else memo.title.ifBlank { fallback }
}

@Composable
private fun completedCountSummary(doneCount: Int, totalCount: Int): String {
    return stringResource(R.string.completed_count_summary, doneCount, totalCount)
}

private fun sendableShoppingMemos(memos: List<ShoppingMemo>): List<ShoppingMemo> {
    return memos.filter { memo ->
        memo.entries.any { it.name.isNotBlank() }
    }
}

private fun shoppingListEmailSubject(context: Context, memos: List<ShoppingMemo>): String {
    return if (memos.size == 1) {
        context.getString(R.string.email_subject_single, memoDisplayTitle(context, memos.first()))
    } else {
        context.getString(R.string.email_subject_multi)
    }
}

private fun shoppingListEmailBody(context: Context, memos: List<ShoppingMemo>): String {
    val builder = StringBuilder()
    builder.appendLine(context.getString(R.string.email_body_title))
    builder.appendLine()
    memos.forEachIndexed { memoIndex, memo ->
        if (memoIndex > 0) builder.appendLine()
        builder.appendLine(context.getString(R.string.email_body_card_title, memoDisplayTitle(context, memo)))
        val activeEntries = memo.entries.filter { it.name.isNotBlank() && !it.checked }
        val completedEntries = memo.entries.filter { it.name.isNotBlank() && it.checked }
        if (activeEntries.isEmpty() && completedEntries.isEmpty()) {
            builder.appendLine(context.getString(R.string.item_none))
        } else {
            if (activeEntries.isNotEmpty()) {
                builder.appendLine(context.getString(R.string.email_section_active))
                activeEntries.forEachIndexed { index, entry ->
                    builder.appendLine("${index + 1}. ${entry.name}")
                }
            }
            if (completedEntries.isNotEmpty()) {
                if (activeEntries.isNotEmpty()) builder.appendLine()
                builder.appendLine(context.getString(R.string.completed))
                completedEntries.forEachIndexed { index, entry ->
                    builder.appendLine("${index + 1}. ${entry.name}")
                }
            }
        }
    }
    return builder.toString().trimEnd()
}

private fun parseEmailRecipients(address: String): Array<String> {
    return address.split(',', ';', '、', '\n', '\t', ' ')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toTypedArray()
}

@Suppress("DEPRECATION")
private fun gmailAccountChooserIntent(title: String): Intent {
    return AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf(GoogleAccountType),
        true,
        title,
        null,
        null,
        null
    )
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun loadSupportRewardedAd(
    context: Context,
    onLoaded: (RewardedAd) -> Unit,
    onFailed: (String) -> Unit
) {
    RewardedAd.load(
        context,
        AdMobRewardedAdUnitId,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                onLoaded(ad)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                onFailed(error.message)
            }
        }
    )
}

private fun launchShoppingListEmail(context: Context, address: String, memos: List<ShoppingMemo>) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, parseEmailRecipients(address))
        putExtra(Intent.EXTRA_SUBJECT, shoppingListEmailSubject(context, memos))
        putExtra(Intent.EXTRA_TEXT, shoppingListEmailBody(context, memos))
    }
    val gmailIntent = Intent(emailIntent).setPackage(GmailPackageName)
    try {
        context.startActivity(gmailIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.email_app_chooser_title)))
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.email_app_not_found), Toast.LENGTH_SHORT).show()
        }
    }
}

private fun formatMemoOrderForTrace(memos: List<ShoppingMemo>): String {
    return memos.mapIndexed { index, memo ->
        val title = memo.title.ifBlank { "(blank)" }
        "${index + 1}:$title#${memo.id.takeLast(6)}"
    }.joinToString(" > ")
}

private fun formatEditMemoForTrace(memo: ShoppingMemo?): String {
    return memo?.let { "${it.title.ifBlank { "(blank)" }}#${it.id.takeLast(6)}" } ?: "none"
}

private fun formatEditEntryForTrace(entry: ShoppingEntry?): String {
    return entry?.let { "${it.name.ifBlank { "(blank)" }}#${it.id.takeLast(6)}" } ?: "none"
}

private fun formatOffsetForTrace(offset: Offset): String {
    return "(${offset.x.roundToInt()},${offset.y.roundToInt()})"
}

private fun formatRectForTrace(rect: Rect?): String {
    return rect?.let {
        "(${it.left.roundToInt()},${it.top.roundToInt()})-(${it.right.roundToInt()},${it.bottom.roundToInt()})"
    } ?: "none"
}

private fun applyLocalizedTemporaryTitle(context: Context, memo: ShoppingMemo) {
    memo.title = context.getString(R.string.temporary_memo_title)
}

@Composable
private fun rememberSparkleAlpha(active: Boolean): Float {
    val transition = rememberInfiniteTransition(label = "sparkle")
    val alpha by transition.animateFloat(
        initialValue = if (active) 0.25f else 0f,
        targetValue = if (active) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle-alpha"
    )
    return if (active) alpha else 0f
}

private fun Modifier.sparkleOverlay(alpha: Float): Modifier {
    if (alpha <= 0f) return this
    return drawWithContent {
        drawContent()
        val sparkleColor = Color(0xFFFFC107).copy(alpha = alpha)
        val glowColor = Color(0xFFFFF59D).copy(alpha = alpha * 0.55f)
        val r = 3.5f
        val points = listOf(
            Offset(size.width * 0.08f, size.height * 0.12f),
            Offset(size.width * 0.94f, size.height * 0.18f),
            Offset(size.width * 0.11f, size.height * 0.86f),
            Offset(size.width * 0.88f, size.height * 0.82f)
        )
        points.forEachIndexed { index, point ->
            drawCircle(glowColor, radius = r * 2.2f, center = point)
            drawCircle(sparkleColor, radius = r + index % 2, center = point)
        }
    }
}

private fun pruneBlankEntries(memo: ShoppingMemo) {
    memo.entries.removeAll { it.name.isBlank() }
    memo.deletedEntries.removeAll { it.name.isBlank() }
}

private fun ensureDisplayBlankEntry(memo: ShoppingMemo, keepCompletedItemsInPlace: Boolean = false): ShoppingEntry? {
    if (keepCompletedItemsInPlace) {
        val visible = memo.entries.filter { it.name.isNotBlank() }
        val blank = memo.entries.firstOrNull { it.name.isBlank() }
        memo.entries.clear()
        memo.entries.addAll(visible)
        if (visible.isEmpty()) {
            val displayBlank = blank ?: ShoppingEntry(name = "")
            memo.entries.add(displayBlank)
            return displayBlank
        }
        return null
    }
    val active = memo.entries.filter { it.name.isNotBlank() && !it.checked }
    val done = memo.entries.filter { it.name.isNotBlank() && it.checked }
    val blank = memo.entries.firstOrNull { it.name.isBlank() }
    memo.entries.clear()
    memo.entries.addAll(active)
    if (active.isEmpty()) {
        val displayBlank = blank ?: ShoppingEntry(name = "")
        memo.entries.add(displayBlank)
        memo.entries.addAll(done)
        return displayBlank
    }
    memo.entries.addAll(done)
    return null
}

private fun visibleEntryGroup(memo: ShoppingMemo, checked: Boolean): List<ShoppingEntry> {
    return memo.entries.filter { it.name.isNotBlank() && it.checked == checked }
}

private fun displayEntryGroup(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    keepCompletedItemsInPlace: Boolean
): List<ShoppingEntry> {
    return memo.entries.filter {
        it.name.isNotBlank() && (keepCompletedItemsInPlace || it.checked == entry.checked)
    }
}

private fun moveEntryWithinVisibleGroup(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    direction: Int,
    keepCompletedItemsInPlace: Boolean = false
): Boolean {
    if (entry.name.isBlank()) return false
    if (keepCompletedItemsInPlace) {
        val targetGroup = displayEntryGroup(memo, entry, keepCompletedItemsInPlace = true).toMutableList()
        val from = targetGroup.indexOfFirst { it.id == entry.id }
        if (from < 0) return false
        val to = (from + direction).coerceIn(0, targetGroup.lastIndex)
        if (from == to) return false
        val moving = targetGroup.removeAt(from)
        targetGroup.add(to, moving)
        memo.entries.clear()
        memo.entries.addAll(targetGroup)
        return true
    }
    val active = visibleEntryGroup(memo, checked = false).toMutableList()
    val done = visibleEntryGroup(memo, checked = true).toMutableList()
    val targetGroup = if (entry.checked) done else active
    val from = targetGroup.indexOfFirst { it.id == entry.id }
    if (from < 0) return false
    val to = (from + direction).coerceIn(0, targetGroup.lastIndex)
    if (from == to) return false
    val moving = targetGroup.removeAt(from)
    targetGroup.add(to, moving)

    memo.entries.clear()
    memo.entries.addAll(active)
    memo.entries.addAll(done)
    ensureDisplayBlankEntry(memo)
    return true
}

private fun requestBlankEntry(memo: ShoppingMemo, keepCompletedItemsInPlace: Boolean = false): ShoppingEntry {
    if (keepCompletedItemsInPlace) {
        val visible = memo.entries.filter { it.name.isNotBlank() }
        val blank = memo.entries.firstOrNull { it.name.isBlank() } ?: ShoppingEntry(name = "")
        memo.entries.clear()
        memo.entries.addAll(visible)
        memo.entries.add(blank)
        return blank
    }
    val active = memo.entries.filter { it.name.isNotBlank() && !it.checked }
    val done = memo.entries.filter { it.name.isNotBlank() && it.checked }
    val blank = memo.entries.firstOrNull { it.name.isBlank() } ?: ShoppingEntry(name = "")
    memo.entries.clear()
    memo.entries.addAll(active)
    memo.entries.add(blank)
    memo.entries.addAll(done)
    return blank
}

private fun restoreEntryToActiveBottom(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    keepCompletedItemsInPlace: Boolean = false
): ShoppingEntry {
    val restored = entry.copy(checked = false)
    if (keepCompletedItemsInPlace) {
        val visible = memo.entries
            .filter { it.id != entry.id && it.name.isNotBlank() }
            .toMutableList()
        visible.add(restored)
        memo.entries.clear()
        memo.entries.addAll(visible)
        return restored
    }
    val active = memo.entries
        .filter { it.id != entry.id && it.name.isNotBlank() && !it.checked }
        .toMutableList()
    val done = memo.entries
        .filter { it.id != entry.id && it.name.isNotBlank() && it.checked }
    active.add(restored)
    memo.entries.clear()
    memo.entries.addAll(active)
    memo.entries.addAll(done)
    return restored
}

@Composable
fun ShoppingMemoApp(
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    onShowPrivacyOptions: () -> Unit
) {
    val context = LocalContext.current
    val appLocale = context.appLocale()
    val initialSimpleModeEnabled = remember { loadSimpleModeEnabled(context) }
    val memos = remember {
        mutableStateListOf<ShoppingMemo>().also { state ->
            val loaded = loadMemos(context).toMutableList()
            val temporaryMemo = loaded.firstOrNull { it.id == TemporaryMemoId }
            val localizedTemporaryTitle = context.getString(R.string.temporary_memo_title)
            var temporaryTitleChanged = false
            if (temporaryMemo == null) {
                loaded.add(0, ShoppingMemo(id = TemporaryMemoId, title = localizedTemporaryTitle))
                temporaryTitleChanged = true
            } else if (temporaryMemo.title != localizedTemporaryTitle) {
                applyLocalizedTemporaryTitle(context, temporaryMemo)
                temporaryTitleChanged = true
            }
            if (temporaryTitleChanged) saveMemos(context, loaded)
            state.addAll(loaded)
        }
    }
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedMemoId by remember { mutableStateOf<String?>(null) }
    var homeCarouselSelectedMemoId by remember { mutableStateOf<String?>(null) }
    var imageEditingMemoId by remember { mutableStateOf<String?>(null) }
    var titlePatternTarget by remember { mutableStateOf<TitlePatternTarget?>(null) }
    var titlePatternReturnScreen by remember { mutableStateOf(Screen.Home) }
    var homeTitlePattern by remember { mutableStateOf(loadHomeTitlePattern(context)) }
    var temporaryTitlePattern by remember { mutableStateOf(loadTemporaryTitlePattern(context)) }
    var oneHandModeEnabled by remember { mutableStateOf(loadOneHandModeEnabled(context)) }
    var simpleModeEnabled by remember { mutableStateOf(initialSimpleModeEnabled) }
    var leftHandModeEnabled by remember { mutableStateOf(loadLeftHandModeEnabled(context)) }
    var microphoneSettings by remember { mutableStateOf(loadMicrophoneSettings(context)) }
    var microphoneSessionActive by remember { mutableStateOf(false) }
    var editHelpVisible by remember { mutableStateOf(loadEditHelpVisible(context)) }
    var largeFontEnabled by remember { mutableStateOf(loadLargeFontEnabled(context)) }
    var keepCompletedItemsInPlace by remember { mutableStateOf(loadKeepCompletedItemsInPlace(context)) }
    var modeSelectionCompleted by remember { mutableStateOf(loadModeSelectionCompleted(context)) }
    var homeTopScrollRequest by remember { mutableStateOf(0) }
    var supportAdVideoVisible by remember { mutableStateOf(false) }
    val recentlyMovedEntryIds = remember { mutableStateListOf<String>() }
    val selectedMemo = memos.firstOrNull { it.id == selectedMemoId }
    val imageEditingMemo = memos.firstOrNull { it.id == imageEditingMemoId }
    val temporaryMemo = memos.first { it.id == TemporaryMemoId }
    val activeMemos = memos.filter { !isTemporaryMemo(it) && !it.trashed }
    val trashedMemos = memos.filter { !isTemporaryMemo(it) && it.trashed }

    fun persist() = saveMemos(context, memos)

    fun applyMemoOrder(orderedActiveIds: List<String>) {
        val currentActive = memos.filter { !isTemporaryMemo(it) && !it.trashed }
        if (orderedActiveIds.size != currentActive.size || orderedActiveIds.toSet() != currentActive.map { it.id }.toSet()) return
        val orderedActive = orderedActiveIds.mapNotNull { id -> currentActive.firstOrNull { it.id == id } }
        if (orderedActive.size != currentActive.size) return
        Log.d(
            HomeCarouselTraceTag,
            "parent applyMemoOrder before active=${formatMemoOrderForTrace(currentActive)} requested=${orderedActiveIds.joinToString(" > ") { id -> currentActive.firstOrNull { it.id == id }?.let { memo -> "${memo.title.ifBlank { "(blank)" }}#${memo.id.takeLast(6)}" } ?: id.takeLast(6) }}"
        )
        Snapshot.withMutableSnapshot {
            var activeIndex = 0
            memos.indices.forEach { index ->
                val memo = memos[index]
                if (!isTemporaryMemo(memo) && !memo.trashed) {
                    memos[index] = orderedActive[activeIndex++]
                }
            }
        }
        Log.d(
            HomeCarouselTraceTag,
            "parent applyMemoOrder after active=${formatMemoOrderForTrace(memos.filter { !isTemporaryMemo(it) && !it.trashed })}"
        )
        persist()
    }

    fun updateOneHandMode(enabled: Boolean) {
        oneHandModeEnabled = enabled
        saveOneHandModeEnabled(context, enabled)
    }
    fun updateSimpleMode(enabled: Boolean) {
        simpleModeEnabled = enabled
        applyLocalizedTemporaryTitle(context, temporaryMemo)
        saveSimpleModeEnabled(context, enabled)
        saveModeSelectionCompleted(context, true)
        modeSelectionCompleted = true
        persist()
    }
    fun updateLeftHandMode(enabled: Boolean) {
        leftHandModeEnabled = enabled
        saveLeftHandModeEnabled(context, enabled)
    }
    fun updateLargeFont(enabled: Boolean) {
        largeFontEnabled = enabled
        saveLargeFontEnabled(context, enabled)
    }
    fun updateMicrophoneSettings(settings: MicrophoneSettings) {
        microphoneSettings = settings
        if (settings.disabled) {
            microphoneSessionActive = false
        }
        saveMicrophoneSettings(context, settings)
    }
    fun updateKeepCompletedItemsInPlace(enabled: Boolean) {
        keepCompletedItemsInPlace = enabled
        saveKeepCompletedItemsInPlace(context, enabled)
    }
    fun updateEditHelpVisible(visible: Boolean) {
        editHelpVisible = visible
        saveEditHelpVisible(context, visible)
    }
    fun openTitlePatternPicker(target: TitlePatternTarget) {
        titlePatternTarget = target
        titlePatternReturnScreen = currentScreen
        currentScreen = Screen.TitlePatternPicker
    }
    fun closeTitlePatternPicker() {
        currentScreen = titlePatternReturnScreen
        titlePatternTarget = null
    }
    fun updateTitlePattern(pattern: Int) {
        when (titlePatternTarget) {
            TitlePatternTarget.Home -> {
                homeTitlePattern = pattern
                saveHomeTitlePattern(context, pattern)
            }
            TitlePatternTarget.Temporary -> {
                temporaryTitlePattern = pattern
                saveTemporaryTitlePattern(context, pattern)
            }
            null -> Unit
        }
        closeTitlePatternPicker()
    }
    fun openMemo(memo: ShoppingMemo) {
        selectedMemoId = memo.id
        if (!isTemporaryMemo(memo)) {
            homeCarouselSelectedMemoId = memo.id
        }
        if (!microphoneSettings.disabled && microphoneSettings.startOnLaunch) {
            microphoneSessionActive = true
        }
        currentScreen = Screen.Detail
    }
    fun openFavorites() {
        if (!microphoneSettings.disabled && microphoneSettings.startOnLaunch) {
            microphoneSessionActive = true
        }
        currentScreen = Screen.Favorites
    }
    fun finishDetail() {
        selectedMemo?.let { memo ->
            pruneBlankEntries(memo)
            if (isTemporaryMemo(memo)) {
                applyLocalizedTemporaryTitle(context, memo)
            } else if (memo.title.isBlank() && memo.entries.none { it.name.isNotBlank() } && memo.deletedEntries.none { it.name.isNotBlank() }) {
                memos.remove(memo)
                selectedMemoId = null
            } else {
                assignDefaultTitleIfBlank(context, memo, memos)
            }
            recentlyMovedEntryIds.removeAll { id -> memo.entries.any { it.id == id } }
        }
        persist()
        currentScreen = Screen.Home
    }
    fun addMemo() {
        val memo = ShoppingMemo(
            imagePattern = defaultNewMemoImagePattern(appLocale),
            entries = listOf(ShoppingEntry(name = ""))
        )
        val insertIndex = (memos.indexOfFirst { isTemporaryMemo(it) } + 1)
            .coerceIn(0, memos.size)
        memos.add(insertIndex, memo)
        homeTopScrollRequest++
        openMemo(memo)
    }
    fun finishTemporaryHome() {
        pruneBlankEntries(temporaryMemo)
        applyLocalizedTemporaryTitle(context, temporaryMemo)
        persist()
        currentScreen = Screen.Home
    }

    BackHandler(enabled = currentScreen == Screen.Home) {
        // ホームでの戻るジェスチャーではアプリを終了しない。
    }
    BackHandler(enabled = currentScreen == Screen.Settings) {
        currentScreen = Screen.Home
    }
    BackHandler(enabled = currentScreen == Screen.Detail) {
        finishDetail()
    }
    BackHandler(enabled = currentScreen == Screen.PatternPicker) {
        currentScreen = Screen.Home
    }
    BackHandler(enabled = currentScreen == Screen.TitlePatternPicker) {
        closeTitlePatternPicker()
    }
    CompositionLocalProvider(
        LocalAppFontSizes provides appFontSizes(largeFontEnabled),
        LocalLayoutDirection provides appLayoutDirection(appLocale)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!supportAdVideoVisible) {
                    BottomIconBar(
                        screen = when (currentScreen) {
                            Screen.TitlePatternPicker -> titlePatternReturnScreen
                            else -> currentScreen
                        },
                        simpleModeEnabled = simpleModeEnabled,
                        onHome = {
                            if (currentScreen == Screen.Detail) finishDetail() else currentScreen = Screen.Home
                        },
                        onEdit = { currentScreen = Screen.Edit },
                        onSettings = { currentScreen = Screen.Settings },
                        onFavorite = ::openFavorites,
                        onAds = { currentScreen = Screen.Ads }
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                color = Color.White
            ) {
                when (currentScreen) {
                Screen.Home -> {
                    if (simpleModeEnabled) {
                        MemoDetailScreen(
                            memo = temporaryMemo,
                            oneHandModeEnabled = oneHandModeEnabled,
                            leftHandModeEnabled = leftHandModeEnabled,
                            microphoneEnabled = !microphoneSettings.disabled,
                            microphoneSettings = microphoneSettings,
                            microphoneSessionActive = microphoneSessionActive,
                            onMicrophoneSessionActiveChange = { microphoneSessionActive = it },
                            keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                            titlePattern = temporaryTitlePattern,
                            onTitlePatternClick = { openTitlePatternPicker(TitlePatternTarget.Temporary) },
                            recentlyMovedEntryIds = emptySet(),
                            onClearRecentlyMovedEntryHighlight = {},
                            onFinish = ::finishTemporaryHome,
                            onChanged = ::persist
                        )
                    } else {
                        AdvancedHomeScreen(
                            memos = activeMemos,
                            trashedMemos = trashedMemos,
                            temporaryMemo = temporaryMemo,
                            oneHandModeEnabled = oneHandModeEnabled,
                            leftHandModeEnabled = leftHandModeEnabled,
                            homeTopScrollRequest = homeTopScrollRequest,
                            titlePattern = homeTitlePattern,
                            homeCarouselSelectedMemoId = homeCarouselSelectedMemoId,
                            onHomeCarouselSelected = { homeCarouselSelectedMemoId = it },
                            onTitlePatternClick = { openTitlePatternPicker(TitlePatternTarget.Home) },
                            onAddMemo = ::addMemo,
                            onOpenMemo = ::openMemo,
                            onOpenTemporaryMemo = { openMemo(temporaryMemo) },
                            onTemporaryEntryMoved = { targetMemo, entry ->
                                recentlyMovedEntryIds.remove(entry.id)
                                recentlyMovedEntryIds.add(entry.id)
                                persist()
                            },
                            onApplyMemoOrder = ::applyMemoOrder,
                            onDeleteMemo = {
                                if (!isTemporaryMemo(it)) it.trashed = true
                                persist()
                            },
                            onRestoreMemo = {
                                it.trashed = false
                                persist()
                            },
                            onEraseMemo = {
                                if (!isTemporaryMemo(it)) memos.remove(it)
                                persist()
                            },
                            onSelectPatternMemo = {
                                imageEditingMemoId = it.id
                                currentScreen = Screen.PatternPicker
                                persist()
                            },
                            onCardImageChanged = ::persist,
                            onToggleFavorite = {
                                it.favorite = !it.favorite
                                persist()
                            }
                        )
                    }
                }
                Screen.Detail -> {
                    if (selectedMemo == null) {
                        currentScreen = Screen.Home
                    } else {
                        MemoDetailScreen(
                            memo = selectedMemo,
                            oneHandModeEnabled = oneHandModeEnabled,
                            leftHandModeEnabled = leftHandModeEnabled,
                            microphoneEnabled = !microphoneSettings.disabled,
                            microphoneSettings = microphoneSettings,
                            microphoneSessionActive = microphoneSessionActive,
                            onMicrophoneSessionActiveChange = { microphoneSessionActive = it },
                            keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                            titlePattern = if (isTemporaryMemo(selectedMemo)) temporaryTitlePattern else homeTitlePattern,
                            onTitlePatternClick = if (isTemporaryMemo(selectedMemo)) {
                                { openTitlePatternPicker(TitlePatternTarget.Temporary) }
                            } else {
                                null
                            },
                            recentlyMovedEntryIds = recentlyMovedEntryIds.toSet(),
                            onClearRecentlyMovedEntryHighlight = { recentlyMovedEntryIds.remove(it) },
                            onFinish = ::finishDetail,
                            onChanged = ::persist
                        )
                    }
                }
                Screen.Ads -> AdsSupportScreen(
                    canRequestAds = canRequestAds,
                    onVideoOverlayVisibleChange = { supportAdVideoVisible = it }
                )
                Screen.Edit -> MemoMoveEditScreen(
                    memos = listOf(temporaryMemo) + activeMemos,
                    oneHandModeEnabled = oneHandModeEnabled,
                    showInstruction = editHelpVisible,
                    onDismissInstruction = { updateEditHelpVisible(false) },
                    onChanged = ::persist
                )
                Screen.PatternPicker -> {
                    if (imageEditingMemo == null) {
                        currentScreen = Screen.Home
                    } else {
                        PatternPickerScreen(
                            memo = imageEditingMemo,
                            oneHandModeEnabled = oneHandModeEnabled,
                            onBack = { currentScreen = Screen.Home },
                            onSelect = { pattern ->
                                imageEditingMemo.imagePattern = pattern
                                imageEditingMemo.customImageUri = null
                                persist()
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }
                Screen.TitlePatternPicker -> {
                    val selectedPattern = when (titlePatternTarget) {
                        TitlePatternTarget.Home -> homeTitlePattern
                        TitlePatternTarget.Temporary -> temporaryTitlePattern
                        null -> 0
                    }
                    TitlePatternPickerScreen(
                        selectedPattern = selectedPattern,
                        oneHandModeEnabled = oneHandModeEnabled,
                        onSelect = ::updateTitlePattern
                    )
                }
                Screen.Settings -> SettingsScreen(
                    memos = listOf(temporaryMemo) + activeMemos,
                    memoCount = activeMemos.size,
                    oneHandModeEnabled = oneHandModeEnabled,
                    onOneHandModeChanged = ::updateOneHandMode,
                    simpleModeEnabled = simpleModeEnabled,
                    onSimpleModeChanged = ::updateSimpleMode,
                    leftHandModeEnabled = leftHandModeEnabled,
                    onLeftHandModeChanged = ::updateLeftHandMode,
                    largeFontEnabled = largeFontEnabled,
                    onLargeFontChanged = ::updateLargeFont,
                    keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                    onKeepCompletedItemsInPlaceChanged = ::updateKeepCompletedItemsInPlace,
                    microphoneSettings = microphoneSettings,
                    onMicrophoneSettingsChanged = ::updateMicrophoneSettings,
                    privacyOptionsRequired = privacyOptionsRequired,
                    onShowPrivacyOptions = onShowPrivacyOptions,
                    onResetOperationHelp = { updateEditHelpVisible(true) },
                    onHome = { currentScreen = Screen.Home }
                )
                Screen.Favorites -> FavoritesScreen(
                    memos = listOf(temporaryMemo) + activeMemos.filter { it.favorite },
                    oneHandModeEnabled = oneHandModeEnabled,
                    leftHandModeEnabled = leftHandModeEnabled,
                    microphoneEnabled = !microphoneSettings.disabled,
                    microphoneSessionActive = microphoneSessionActive,
                    onMicrophoneSessionActiveChange = { microphoneSessionActive = it },
                    keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                    onHome = { currentScreen = Screen.Home },
                    onChanged = ::persist
                )
            }
        }
        if (!modeSelectionCompleted) {
            InitialModeSelectionDialog(
                onSelect = { enabled -> updateSimpleMode(enabled) }
            )
        }
    }
}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdvancedHomeScreen(
    memos: List<ShoppingMemo>,
    trashedMemos: List<ShoppingMemo>,
    temporaryMemo: ShoppingMemo,
    oneHandModeEnabled: Boolean,
    leftHandModeEnabled: Boolean,
    homeTopScrollRequest: Int,
    titlePattern: Int,
    homeCarouselSelectedMemoId: String?,
    onHomeCarouselSelected: (String?) -> Unit,
    onTitlePatternClick: () -> Unit,
    onAddMemo: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onOpenTemporaryMemo: () -> Unit,
    onTemporaryEntryMoved: (ShoppingMemo, ShoppingEntry) -> Unit,
    onApplyMemoOrder: (List<String>) -> Unit,
    onDeleteMemo: (ShoppingMemo) -> Unit,
    onRestoreMemo: (ShoppingMemo) -> Unit,
    onEraseMemo: (ShoppingMemo) -> Unit,
    onSelectPatternMemo: (ShoppingMemo) -> Unit,
    onCardImageChanged: () -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tabs = listOf(stringResource(R.string.tab_items), stringResource(R.string.tab_trash))
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val activeGridState = rememberLazyGridState()
    val trashGridState = rememberLazyGridState()
    val memoCardBounds = remember { mutableStateMapOf<String, Rect>() }
    val homeMemoOrderIds = remember { mutableStateListOf<String>() }
    val sparklingMemoIds = remember { mutableStateListOf<String>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPoint by remember { mutableStateOf(Offset.Zero) }
    var draggedCardBounds by remember { mutableStateOf<Rect?>(null) }
    var homeMemoOrderChanged by remember { mutableStateOf(false) }
    var homeDragStartOrderIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var homeCarouselSettleTargetId by remember { mutableStateOf<String?>(null) }
    var homeCarouselSettleSerial by remember { mutableStateOf(0) }
    var homeDropTraceSerial by remember { mutableStateOf(0) }
    var homeOrderSourceSyncPaused by remember { mutableStateOf(false) }
    var homeExpectedOrderAfterDropIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var homeIgnoreNextDragCancel by remember { mutableStateOf(false) }
    var homeIgnoreNextDragCancelSerial by remember { mutableStateOf(0) }
    var imageChangeBounds by remember { mutableStateOf<Rect?>(null) }
    var cardTrashBounds by remember { mutableStateOf<Rect?>(null) }
    var pendingImageMemo by remember { mutableStateOf<ShoppingMemo?>(null) }
    var pendingCameraMemoId by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingFolderMemoId by remember { mutableStateOf<String?>(null) }
    var homeBounds by remember { mutableStateOf<Rect?>(null) }
    var homeControlBounds by remember { mutableStateOf<Rect?>(null) }
    var temporaryListBounds by remember { mutableStateOf<Rect?>(null) }
    var draggingTemporaryEntry by remember { mutableStateOf<ShoppingEntry?>(null) }
    var temporaryDragPoint by remember { mutableStateOf(Offset.Zero) }
    var homeHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember { mutableStateOf(0f) }
    var oneHandFlingGeneration by remember { mutableStateOf(0) }
    var controlScrollReachedTopInGesture by remember { mutableStateOf(false) }
    val homeOneHandModeEnabled = oneHandModeEnabled && pagerState.currentPage == 1
    val oneHandMaxOffsetPx = if (homeOneHandModeEnabled) {
        (homeHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    } else {
        0f
    }
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val oneHandOffsetDp = with(density) { oneHandOffsetPx.toDp() }
    val dragActive = draggingId != null || draggingTemporaryEntry != null
    val currentListAtTop = if (pagerState.currentPage == 0) {
        !activeGridState.canScrollBackward
    } else {
        !trashGridState.canScrollBackward
    }
    val latestCurrentListAtTop by rememberUpdatedState(currentListAtTop)
    val latestHomeBounds by rememberUpdatedState(homeBounds)
    val latestHomeControlBounds by rememberUpdatedState(homeControlBounds)
    val latestTemporaryListBounds by rememberUpdatedState(temporaryListBounds)
    val latestDraggingId by rememberUpdatedState(draggingId)
    val sourceMemoIds = memos.map { it.id }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val memo = memos.firstOrNull { it.id == pendingCameraMemoId }
        val uri = pendingCameraUri
        if (success && memo != null && uri != null) {
            memo.customImageUri = uri.toString()
            onCardImageChanged()
        }
        pendingCameraMemoId = null
        pendingCameraUri = null
        pendingImageMemo = null
    }
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val memo = memos.firstOrNull { it.id == pendingFolderMemoId }
        val importedUri = uri?.let { importCardImageUri(context, it) }
        if (importedUri != null && memo != null) {
            memo.customImageUri = importedUri.toString()
            onCardImageChanged()
        }
        pendingFolderMemoId = null
        pendingImageMemo = null
    }
    val orderedHomeMemos = run {
        val byId = memos.associateBy { it.id }
        val ordered = homeMemoOrderIds.mapNotNull { byId[it] }
        if (ordered.size == memos.size) ordered else memos
    }
    val homeHeaderMemo = if (pagerState.currentPage == 0) {
        homeCarouselSelectedMemoId
            ?.let { selectedId -> orderedHomeMemos.firstOrNull { it.id == selectedId } }
            ?: orderedHomeMemos.firstOrNull()
    } else {
        null
    }
    val homeHeaderTitle = homeHeaderMemo?.let { localizedMemoDisplayTitle(it) } ?: stringResource(R.string.shopping_memo_title)
    val latestOrderedHomeMemoIds by rememberUpdatedState(orderedHomeMemos.map { it.id })

    fun formatHomeMemoId(id: String?): String {
        if (id == null) return "null"
        val title = memos.firstOrNull { it.id == id }?.title?.ifBlank { "(blank)" } ?: "(missing)"
        return "$title#${id.takeLast(6)}"
    }

    fun formatHomeMemoOrder(orderIds: List<String>): String {
        return orderIds.mapIndexed { index, id ->
            "${index + 1}:${formatHomeMemoId(id)}"
        }.joinToString(" > ")
    }

    fun logHomeMemoOrder(label: String, orderIds: List<String>) {
        Log.d(
            HomeCarouselTraceTag,
            "$label count=${orderIds.size} selected=${formatHomeMemoId(homeCarouselSelectedMemoId)} order=${formatHomeMemoOrder(orderIds)}"
        )
    }

    fun hasSameHomeMemoSet(left: List<String>, right: List<String>): Boolean {
        return left.size == right.size && left.toSet() == right.toSet()
    }

    LaunchedEffect(sourceMemoIds, draggingId, homeOrderSourceSyncPaused) {
        if (homeOrderSourceSyncPaused) {
            val expectedOrder = homeExpectedOrderAfterDropIds
            val localOrder = homeMemoOrderIds.toList()
            Log.d(
                HomeCarouselTraceTag,
                "sourceSync paused dragging=${formatHomeMemoId(draggingId)} source=${formatHomeMemoOrder(sourceMemoIds)} local=${formatHomeMemoOrder(localOrder)} expected=${formatHomeMemoOrder(expectedOrder)}"
            )
            if (
                expectedOrder.isNotEmpty() &&
                sourceMemoIds == expectedOrder &&
                localOrder != expectedOrder &&
                hasSameHomeMemoSet(localOrder, expectedOrder)
            ) {
                Log.d(
                    HomeCarouselTraceTag,
                    "sourceSync restoreLocalToExpected reason=paused-source-confirmed before=${formatHomeMemoOrder(localOrder)} after=${formatHomeMemoOrder(expectedOrder)}"
                )
                homeMemoOrderIds.clear()
                homeMemoOrderIds.addAll(expectedOrder)
            }
            return@LaunchedEffect
        }
        if (draggingId == null) {
            val localOrder = homeMemoOrderIds.toList()
            val hasSameOrder = localOrder == sourceMemoIds
            if (!hasSameOrder) {
                Log.d(
                    HomeCarouselTraceTag,
                    "sourceSync applySource dragging=null source=${formatHomeMemoOrder(sourceMemoIds)} local=${formatHomeMemoOrder(localOrder)}"
                )
                homeMemoOrderIds.clear()
                homeMemoOrderIds.addAll(sourceMemoIds)
            }
        } else {
            val before = homeMemoOrderIds.toList()
            homeMemoOrderIds.removeAll { it !in sourceMemoIds }
            sourceMemoIds.forEach { id ->
                if (id !in homeMemoOrderIds) homeMemoOrderIds.add(id)
            }
            val after = homeMemoOrderIds.toList()
            if (before != after) {
                Log.d(
                    HomeCarouselTraceTag,
                    "sourceSync mergeDuringDrag dragging=${formatHomeMemoId(draggingId)} source=${formatHomeMemoOrder(sourceMemoIds)} before=${formatHomeMemoOrder(before)} after=${formatHomeMemoOrder(after)}"
                )
            }
        }
    }

    LaunchedEffect(homeOrderSourceSyncPaused) {
        if (homeOrderSourceSyncPaused) {
            withFrameNanos { }
            delay(180)
            Log.d(
                HomeCarouselTraceTag,
                "sourceSync pauseReleased expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)} local=${formatHomeMemoOrder(homeMemoOrderIds.toList())} source=${formatHomeMemoOrder(sourceMemoIds)}"
            )
            homeOrderSourceSyncPaused = false
            homeExpectedOrderAfterDropIds = emptyList()
        }
    }

    LaunchedEffect(homeIgnoreNextDragCancelSerial) {
        if (homeIgnoreNextDragCancelSerial <= 0) return@LaunchedEffect
        delay(350)
        if (homeIgnoreNextDragCancel) {
            Log.d(
                HomeCarouselTraceTag,
                "dragCancel ignoreWindowExpired local=${formatHomeMemoOrder(homeMemoOrderIds.toList())} expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)}"
            )
            homeIgnoreNextDragCancel = false
        }
    }

    LaunchedEffect(homeDropTraceSerial) {
        if (homeDropTraceSerial <= 0) return@LaunchedEffect
        withFrameNanos { }
        delay(80)
        logHomeMemoOrder("renderedOrder afterDrop serial=$homeDropTraceSerial", latestOrderedHomeMemoIds)
    }

    LaunchedEffect(homeTopScrollRequest) {
        if (homeTopScrollRequest > 0) {
            activeGridState.scrollToItem(0, 0)
            oneHandOffsetPx = 0f
        }
    }

    fun moveTemporaryEntryTo(targetMemo: ShoppingMemo, entry: ShoppingEntry) {
        temporaryMemo.entries.remove(entry)
        val insertIndex = if (entry.checked) {
            targetMemo.entries.size
        } else {
            targetMemo.entries.indexOfFirst { it.name.isBlank() || it.checked }.let { if (it >= 0) it else targetMemo.entries.size }
        }
        targetMemo.entries.add(insertIndex, entry)
        ensureDisplayBlankEntry(temporaryMemo)
        ensureDisplayBlankEntry(targetMemo)
        onTemporaryEntryMoved(targetMemo, entry)
        sparklingMemoIds.remove(targetMemo.id)
        sparklingMemoIds.add(targetMemo.id)
        scope.launch {
            delay(5000)
            sparklingMemoIds.remove(targetMemo.id)
        }
    }

    fun updateHomeMemoOrderDuringDrag() {
        val draggedMemoId = draggingId ?: return
        if (imageChangeBounds?.contains(dragPoint) == true || cardTrashBounds?.contains(dragPoint) == true) {
            return
        }
        if (homeMemoOrderIds.isEmpty()) homeMemoOrderIds.addAll(sourceMemoIds)
        val targets = orderedHomeMemos.mapNotNull { memo ->
            if (memo.id == draggedMemoId) {
                null
            } else {
                memoCardBounds[memo.id]?.let { bounds -> memo to bounds }
            }
        }.filter { (_, bounds) ->
            dragPoint.x >= bounds.left - 48f && dragPoint.x <= bounds.right + 48f
        }
        val targetPair = targets.firstOrNull { (_, bounds) ->
            bounds.contains(dragPoint)
        } ?: targets.minByOrNull { (_, bounds) ->
            kotlin.math.abs(bounds.center.y - dragPoint.y)
        }
        val target = targetPair?.first ?: return
        val targetBounds = targetPair.second
        val from = homeMemoOrderIds.indexOf(draggedMemoId)
        val targetIndex = homeMemoOrderIds.indexOf(target.id)
        if (from < 0 || targetIndex < 0) return

        val insertBefore = dragPoint.y < targetBounds.center.y
        var to = if (insertBefore) targetIndex else targetIndex + 1
        if (from < to) to--
        if (from == to) return

        val movedId = homeMemoOrderIds.removeAt(from)
        homeMemoOrderIds.add(to.coerceIn(0, homeMemoOrderIds.size), movedId)
        homeMemoOrderChanged = true
    }

    fun resetHomeMemoOrder() {
        Log.d(
            HomeCarouselTraceTag,
            "resetHomeMemoOrder before source=${formatHomeMemoOrder(sourceMemoIds)} local=${formatHomeMemoOrder(homeMemoOrderIds.toList())} expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)}"
        )
        homeMemoOrderIds.clear()
        homeMemoOrderIds.addAll(sourceMemoIds)
        homeMemoOrderChanged = false
        homeDragStartOrderIds = emptyList()
        homeExpectedOrderAfterDropIds = emptyList()
        homeOrderSourceSyncPaused = false
        Log.d(
            HomeCarouselTraceTag,
            "resetHomeMemoOrder after local=${formatHomeMemoOrder(homeMemoOrderIds.toList())}"
        )
    }

    fun finishHomeMemoDrag(swapCandidateId: String? = null) {
        val draggedMemoId = draggingId
        val memo = memos.firstOrNull { it.id == draggedMemoId }
        val savedOrder = homeDragStartOrderIds
        val sourceMemoIdSet = sourceMemoIds.toSet()
        fun validOrderOrNull(order: List<String>): List<String>? {
            return order.takeIf { it.size == sourceMemoIds.size && it.toSet() == sourceMemoIdSet }
        }
        Log.d(
            HomeCarouselTraceTag,
            "drop dragged=${formatHomeMemoId(draggedMemoId)} candidate=${formatHomeMemoId(swapCandidateId)}"
        )
        logHomeMemoOrder("drop savedOrder", savedOrder)
        when {
            memo != null && imageChangeBounds?.contains(dragPoint) == true -> pendingImageMemo = memo
            memo != null && cardTrashBounds?.contains(dragPoint) == true -> onDeleteMemo(memo)
            draggedMemoId != null && swapCandidateId != null && draggedMemoId != swapCandidateId -> {
                val baseOrder = validOrderOrNull(savedOrder)
                    ?: validOrderOrNull(homeMemoOrderIds.toList())
                    ?: sourceMemoIds
                val draggedIndex = baseOrder.indexOf(draggedMemoId)
                val candidateIndex = baseOrder.indexOf(swapCandidateId)
                if (draggedIndex >= 0 && candidateIndex >= 0) {
                    val nextOrder = baseOrder.toMutableList()
                    val dragged = nextOrder[draggedIndex]
                    nextOrder[draggedIndex] = nextOrder[candidateIndex]
                    nextOrder[candidateIndex] = dragged
                    homeMemoOrderIds.clear()
                    homeMemoOrderIds.addAll(nextOrder)
                    logHomeMemoOrder("drop appliedOrder", nextOrder)
                    homeExpectedOrderAfterDropIds = nextOrder
                    homeIgnoreNextDragCancel = true
                    homeIgnoreNextDragCancelSerial++
                    homeOrderSourceSyncPaused = true
                    homeCarouselSettleTargetId = swapCandidateId
                    homeCarouselSettleSerial++
                    Log.d(
                        HomeCarouselTraceTag,
                        "drop commit begin expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)} ignoreCancelSerial=$homeIgnoreNextDragCancelSerial"
                    )
                    onApplyMemoOrder(homeMemoOrderIds.toList())
                    onHomeCarouselSelected(swapCandidateId)
                }
            }
        }
        if (homeMemoOrderIds.isNotEmpty()) {
            logHomeMemoOrder("drop currentOrderBeforeRender", homeMemoOrderIds.toList())
        }
        homeDropTraceSerial++
        draggingId = null
        dragOffset = Offset.Zero
        draggedCardBounds = null
        homeMemoOrderChanged = false
        homeDragStartOrderIds = emptyList()
    }

    fun startOneHandFling(initialVelocityY: Float) {
        if (oneHandMaxOffsetPx <= 0f || kotlin.math.abs(initialVelocityY) < 120f) return
        val generation = ++oneHandFlingGeneration
        scope.launch {
            var velocityY = initialVelocityY.coerceIn(-3200f, 3200f)
            var lastFrameNanos = withFrameNanos { it }
            while (generation == oneHandFlingGeneration && kotlin.math.abs(velocityY) > 30f) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastFrameNanos = frameNanos
                val nextOffset = (oneHandOffsetPx + velocityY * deltaSeconds).coerceIn(0f, oneHandMaxOffsetPx)
                if (nextOffset == oneHandOffsetPx) break
                oneHandOffsetPx = nextOffset
                velocityY *= 0.90f
            }
        }
    }

    fun applyOneHandControlScroll(delta: Float): Float {
        if (!homeOneHandModeEnabled || oneHandMaxOffsetPx <= 0f) return 0f
        val before = oneHandOffsetPx
        val boostedDelta = delta * OneHandScrollSpeedMultiplier
        val nextOffset = (oneHandOffsetPx + boostedDelta).coerceIn(0f, oneHandMaxOffsetPx)
        if (nextOffset == before) return 0f
        oneHandFlingGeneration++
        oneHandOffsetPx = nextOffset
        return delta
    }

    fun scrollHomeControls(delta: Float): Float {
        if (delta < 0f && oneHandOffsetPx > 0f) {
            return applyOneHandControlScroll(delta)
        }
        if (delta > 0f && controlScrollReachedTopInGesture) {
            return delta
        }

        val gridAtTopBefore = activeGridState.firstVisibleItemIndex == 0 &&
            activeGridState.firstVisibleItemScrollOffset == 0
        val gridConsumed = -activeGridState.dispatchRawDelta(-delta)
        val remaining = delta - gridConsumed
        val gridAtTopAfter = activeGridState.firstVisibleItemIndex == 0 &&
            activeGridState.firstVisibleItemScrollOffset == 0
        if (delta > 0f && !gridAtTopBefore && gridAtTopAfter) {
            controlScrollReachedTopInGesture = true
            return if (remaining > 0f) delta else gridConsumed
        }
        if (remaining > 0f && gridAtTopBefore && gridAtTopAfter) {
            return gridConsumed + applyOneHandControlScroll(remaining)
        }
        return gridConsumed
    }

    LaunchedEffect(homeOneHandModeEnabled) {
        if (!homeOneHandModeEnabled) oneHandOffsetPx = 0f
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0) {
            oneHandOffsetPx = 0f
            oneHandFlingGeneration++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                homeBounds = it.boundsInWindow()
                homeHeightPx = it.size.height
            }
            .pointerInput(draggingId) {
                if (draggingId == null) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        if (event.changes.none { it.pressed }) {
                            scope.launch {
                                delay(80)
                                if (latestDraggingId != null) finishHomeMemoDrag()
                            }
                            break
                        }
                    }
                }
            }
            .pointerInput(pagerState.currentPage, dragActive, sourceMemoIds.size) {
                if (dragActive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalX = 0f
                    var totalY = 0f
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange()
                        totalX += delta.x
                        totalY += delta.y
                        if (kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f) {
                            if (
                                pagerState.currentPage == 0 &&
                                sourceMemoIds.isEmpty() &&
                                kotlin.math.abs(totalX) > DetailBackSwipeThresholdPx
                            ) {
                                change.consume()
                                scope.launch { pagerState.animateScrollToPage(1) }
                                break
                            } else if (pagerState.currentPage == 1 && totalX > DetailBackSwipeThresholdPx) {
                                change.consume()
                                scope.launch { pagerState.animateScrollToPage(0) }
                                break
                            }
                        }
                    }
                }
            }
    ) {
        if (homeOneHandModeEnabled && oneHandOffsetPx > 1f) {
            OneHandModeBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(oneHandBackdropHeight)
                    .graphicsLayer {
                        alpha = (0.42f + (oneHandOffsetPx / oneHandMaxOffsetPx.coerceAtLeast(1f)) * 0.46f)
                            .coerceIn(0.42f, 0.88f)
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = oneHandOffsetPx }
                    .pointerInput(homeOneHandModeEnabled, oneHandMaxOffsetPx, dragActive) {
                    if (!homeOneHandModeEnabled || dragActive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val home = latestHomeBounds
                        val controlBounds = latestHomeControlBounds
                        if (home != null && controlBounds != null) {
                            val windowDown = Offset(home.left + down.position.x, home.top + down.position.y)
                            val translatedWindowDown = windowDown.copy(y = windowDown.y + oneHandOffsetPx)
                            if (controlBounds.contains(windowDown) || controlBounds.contains(translatedWindowDown)) {
                                return@awaitEachGesture
                            }
                        }
                        oneHandFlingGeneration++
                        val rightSideGesture = pagerState.currentPage == 0 &&
                            home != null &&
                            down.position.x >= (home.right - home.left) / 2f
                        val listAtTopWhenGestureStarted = latestCurrentListAtTop || rightSideGesture
                        var totalX = 0f
                        var totalY = 0f
                        var lastVelocityY = 0f
                        var movedContentInGesture = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            val verticalGesture = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.15f
                            val canPullDown = delta.y > 0f && oneHandOffsetPx < oneHandMaxOffsetPx && listAtTopWhenGestureStarted
                            val canPushUp = delta.y < 0f && oneHandOffsetPx > 0f
                            if (verticalGesture && (canPullDown || canPushUp)) {
                                val boostedDeltaY = delta.y * OneHandScrollSpeedMultiplier
                                val nextOffset = (oneHandOffsetPx + boostedDeltaY).coerceIn(0f, oneHandMaxOffsetPx)
                                if (nextOffset != oneHandOffsetPx) {
                                    oneHandOffsetPx = nextOffset
                                    lastVelocityY = boostedDeltaY * 60f
                                    movedContentInGesture = true
                                    change.consume()
                                }
                            }
                        }
                        if (movedContentInGesture) startOneHandFling(lastVelocityY)
                    }
                }
        ) {
            TitlePatternHeader(
                title = homeHeaderTitle,
                pattern = titlePattern,
                onClick = onTitlePatternClick
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                if (page == 0) {
                    HomeItemsPage(
                        memos = orderedHomeMemos,
                        temporaryMemo = temporaryMemo,
                        gridState = activeGridState,
                        memoCardBounds = memoCardBounds,
                        sparklingMemoIds = sparklingMemoIds,
                        selectedMemoId = homeCarouselSelectedMemoId,
                        settleTargetId = homeCarouselSettleTargetId,
                        settleSerial = homeCarouselSettleSerial,
                        onSelectedMemoChanged = onHomeCarouselSelected,
                        draggingId = draggingId,
                        draggedCardBounds = draggedCardBounds,
                        dragPoint = dragPoint,
                        dragOffset = dragOffset,
                        onDragStart = { memo, start, bounds ->
                            val sourceMemoIdSet = sourceMemoIds.toSet()
                            val hasStableOrder = homeMemoOrderIds.size == sourceMemoIds.size &&
                                homeMemoOrderIds.toSet() == sourceMemoIdSet
                            if (!hasStableOrder) {
                                Log.d(
                                    HomeCarouselTraceTag,
                                    "dragStart initializeLocalFromSource reason=unstable source=${formatHomeMemoOrder(sourceMemoIds)} local=${formatHomeMemoOrder(homeMemoOrderIds.toList())}"
                                )
                                homeMemoOrderIds.clear()
                                homeMemoOrderIds.addAll(sourceMemoIds)
                            }
                            homeExpectedOrderAfterDropIds = emptyList()
                            homeIgnoreNextDragCancel = false
                            homeDragStartOrderIds = homeMemoOrderIds.toList()
                            Log.d(HomeCarouselTraceTag, "dragStart memo=${formatHomeMemoId(memo.id)}")
                            logHomeMemoOrder("dragStart savedOrder", homeDragStartOrderIds)
                            draggingId = memo.id
                            dragOffset = Offset.Zero
                            draggedCardBounds = bounds
                            homeMemoOrderChanged = false
                            dragPoint = if (bounds == null) start else Offset(bounds.left + start.x, bounds.top + start.y)
                        },
                        onDrag = { amount ->
                            dragOffset += amount
                            dragPoint += amount
                        },
                        onAutoScrollTick = {},
                        onDragEnd = { swapCandidateId -> finishHomeMemoDrag(swapCandidateId) },
                        onDragCancel = {
                            if (homeIgnoreNextDragCancel) {
                                Log.d(
                                    HomeCarouselTraceTag,
                                    "dragCancel ignoredAfterDrop local=${formatHomeMemoOrder(homeMemoOrderIds.toList())} source=${formatHomeMemoOrder(sourceMemoIds)} expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)}"
                                )
                                homeIgnoreNextDragCancel = false
                                draggingId = null
                                dragOffset = Offset.Zero
                                draggedCardBounds = null
                                homeDragStartOrderIds = emptyList()
                            } else {
                                Log.d(
                                    HomeCarouselTraceTag,
                                    "dragCancel reset local=${formatHomeMemoOrder(homeMemoOrderIds.toList())} source=${formatHomeMemoOrder(sourceMemoIds)} expected=${formatHomeMemoOrder(homeExpectedOrderAfterDropIds)}"
                                )
                                draggingId = null
                                dragOffset = Offset.Zero
                                draggedCardBounds = null
                                homeDragStartOrderIds = emptyList()
                                resetHomeMemoOrder()
                            }
                        },
                        onOpenMemo = onOpenMemo,
                        onToggleFavorite = onToggleFavorite,
                        onAddMemo = onAddMemo,
                        onShowTrash = {
                            scope.launch {
                                oneHandOffsetPx = 0f
                                oneHandFlingGeneration++
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        leftHandModeEnabled = leftHandModeEnabled,
                        oneHandOffset = oneHandOffsetDp,
                        showCardDropTargets = draggingId != null,
                        imageTargetActive = imageChangeBounds?.contains(dragPoint) == true,
                        trashTargetActive = cardTrashBounds?.contains(dragPoint) == true,
                        onImageTargetPositioned = { imageChangeBounds = it },
                        onTrashTargetPositioned = { cardTrashBounds = it },
                        draggingTemporaryEntryId = draggingTemporaryEntry?.id,
                        onOpenTemporaryMemo = onOpenTemporaryMemo,
                        onTemporaryListPositioned = { temporaryListBounds = it },
                        onControlPositioned = { homeControlBounds = it },
                        onControlScrollGestureStart = { controlScrollReachedTopInGesture = false },
                        onControlScroll = { delta -> scrollHomeControls(delta) },
                        onTemporaryDragStart = { entry, start, bounds ->
                            draggingTemporaryEntry = entry
                            temporaryDragPoint = if (bounds == null) start else Offset(bounds.left + start.x, bounds.top + start.y)
                        },
                        onTemporaryDrag = { delta -> temporaryDragPoint += delta },
                        onTemporaryDragEnd = {
                            val entry = draggingTemporaryEntry
                            val target = memos.firstOrNull { memo ->
                                memoCardBounds[memo.id]?.contains(temporaryDragPoint) == true
                            }
                            if (entry != null && target != null) {
                                moveTemporaryEntryTo(target, entry)
                            }
                            draggingTemporaryEntry = null
                        },
                        onTemporaryDragCancel = { draggingTemporaryEntry = null }
                    )
                } else {
                    HomeTrashPage(
                        memos = trashedMemos,
                        gridState = trashGridState,
                        onRestoreMemo = onRestoreMemo,
                        onEraseMemo = onEraseMemo
                    )
                }
            }
        }

        if (draggingId == null && pagerState.currentPage == 0) {
            val addControlGap = 10.dp * HomeOperationScale
            val addControlWidth = homeControlBounds?.let { bounds ->
                with(density) { bounds.width.toDp() }
            } ?: (120.dp * HomeOperationScale)
            val addSidePadding = addControlWidth + 12.dp + addControlGap
            AddMemoCard(
                compact = true,
                onClick = onAddMemo,
                contentScale = HomeOperationScale,
                modifier = Modifier
                    .align(physicalBottomSideAlignment(leftHandModeEnabled))
                    .absolutePadding(
                        left = if (leftHandModeEnabled) addSidePadding else 0.dp,
                        right = if (leftHandModeEnabled) 0.dp else addSidePadding,
                        bottom = 12.dp
                    )
                    .size(86.dp * HomeOperationScale)
                    .zIndex(40f)
            )
        }

        val draggedMemo = memos.firstOrNull { it.id == draggingId }
        val draggedBounds = draggedCardBounds
        if (draggedMemo != null && draggedBounds != null) {
            MemoCard(
                memo = draggedMemo,
                sizeScale = HomeOperationScale,
                modifier = Modifier
                    .align(PhysicalTopLeftAlignment)
                    .graphicsLayer {
                        translationX = draggedBounds.left - (homeBounds?.left ?: 0f) + dragOffset.x
                        translationY = draggedBounds.top - (homeBounds?.top ?: 0f) + dragOffset.y
                        scaleX = 1.05f
                        scaleY = 1.05f
                        shadowElevation = 26f
                    }
                    .width(with(density) { draggedBounds.width.toDp() })
                    .height(with(density) { draggedBounds.height.toDp() })
                    .zIndex(320f),
                onClick = {},
                onToggleFavorite = {}
            )
        }

        draggingTemporaryEntry?.let { entry ->
            TemporaryFloatingEntry(
                entry = entry,
                position = Offset(
                    x = temporaryDragPoint.x - (homeBounds?.left ?: 0f) - 84f,
                    y = temporaryDragPoint.y - (homeBounds?.top ?: 0f) - 28f
                ),
                modifier = Modifier
                    .align(PhysicalTopLeftAlignment)
                    .zIndex(10f)
            )
        }
    }

    pendingImageMemo?.let { memo ->
        ImageChangeChoiceDialog(
            memo = memo,
            onDismiss = { pendingImageMemo = null },
            onCamera = {
                val uri = createCardImageCaptureUri(context)
                if (uri != null) {
                    pendingCameraMemoId = memo.id
                    pendingCameraUri = uri
                    pendingImageMemo = null
                    cameraLauncher.launch(uri)
                }
            },
            onFolder = {
                pendingFolderMemoId = memo.id
                pendingImageMemo = null
                folderLauncher.launch("image/*")
            },
            onPattern = {
                pendingImageMemo = null
                onSelectPatternMemo(memo)
            }
        )
    }
}

@Composable
private fun HomeTabRow(
    tabs: List<String>,
    selectedPage: Int,
    onSelect: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedPage == index
            val selectedTrashTab = selected && index == 1
            Column(
                modifier = Modifier
                    .weight(if (index == 0) 2f else 1f)
                    .fillMaxHeight()
                    .background(if (selectedTrashTab) TrashTabSelectedColor else Color.White)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        selectedTrashTab -> Color.White
                        selected -> Color(0xFF1976D2)
                        else -> Color(0xFF777777)
                    }
                )
                Spacer(Modifier.height(9.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(
                            when {
                                selectedTrashTab -> Color.White
                                selected -> Color(0xFF1565C0)
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun HomeItemsPage(
    memos: List<ShoppingMemo>,
    temporaryMemo: ShoppingMemo,
    gridState: LazyGridState,
    memoCardBounds: MutableMap<String, Rect>,
    sparklingMemoIds: List<String>,
    selectedMemoId: String?,
    settleTargetId: String?,
    settleSerial: Int,
    onSelectedMemoChanged: (String?) -> Unit,
    draggingId: String?,
    draggedCardBounds: Rect?,
    dragPoint: Offset,
    dragOffset: Offset,
    onDragStart: (ShoppingMemo, Offset, Rect?) -> Unit,
    onDrag: (Offset) -> Unit,
    onAutoScrollTick: () -> Unit,
    onDragEnd: (String?) -> Unit,
    onDragCancel: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit,
    onAddMemo: () -> Unit,
    onShowTrash: () -> Unit,
    leftHandModeEnabled: Boolean,
    oneHandOffset: Dp,
    showCardDropTargets: Boolean,
    imageTargetActive: Boolean,
    trashTargetActive: Boolean,
    onImageTargetPositioned: (Rect) -> Unit,
    onTrashTargetPositioned: (Rect) -> Unit,
    draggingTemporaryEntryId: String?,
    onOpenTemporaryMemo: () -> Unit,
    onTemporaryDragStart: (ShoppingEntry, Offset, Rect?) -> Unit,
    onTemporaryDrag: (Offset) -> Unit,
    onTemporaryDragEnd: () -> Unit,
    onTemporaryDragCancel: () -> Unit,
    onTemporaryListPositioned: (Rect) -> Unit,
    onControlPositioned: (Rect) -> Unit,
    onControlScrollGestureStart: () -> Unit,
    onControlScroll: (Float) -> Float
) {
    HomeMemoCarouselPage(
        memos = memos,
        memoCardBounds = memoCardBounds,
        sparklingMemoIds = sparklingMemoIds,
        selectedMemoId = selectedMemoId,
        settleTargetId = settleTargetId,
        settleSerial = settleSerial,
        onSelectedMemoChanged = onSelectedMemoChanged,
        draggingId = draggingId,
        dragPoint = dragPoint,
        onDragStart = onDragStart,
        onDrag = onDrag,
        onDragEnd = onDragEnd,
        onDragCancel = onDragCancel,
        onOpenMemo = onOpenMemo,
        onToggleFavorite = onToggleFavorite,
        onAddMemo = onAddMemo,
        onShowTrash = onShowTrash,
        onOpenTemporaryMemo = onOpenTemporaryMemo,
        leftHandModeEnabled = leftHandModeEnabled,
        showCardDropTargets = showCardDropTargets,
        imageTargetActive = imageTargetActive,
        trashTargetActive = trashTargetActive,
        onImageTargetPositioned = onImageTargetPositioned,
        onTrashTargetPositioned = onTrashTargetPositioned,
        onControlPositioned = onControlPositioned
    )
}

@Composable
private fun HomeMemoCarouselPage(
    memos: List<ShoppingMemo>,
    memoCardBounds: MutableMap<String, Rect>,
    sparklingMemoIds: List<String>,
    selectedMemoId: String?,
    settleTargetId: String?,
    settleSerial: Int,
    onSelectedMemoChanged: (String?) -> Unit,
    draggingId: String?,
    dragPoint: Offset,
    onDragStart: (ShoppingMemo, Offset, Rect?) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (String?) -> Unit,
    onDragCancel: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit,
    onAddMemo: () -> Unit,
    onShowTrash: () -> Unit,
    onOpenTemporaryMemo: () -> Unit,
    leftHandModeEnabled: Boolean,
    showCardDropTargets: Boolean,
    imageTargetActive: Boolean,
    trashTargetActive: Boolean,
    onImageTargetPositioned: (Rect) -> Unit,
    onTrashTargetPositioned: (Rect) -> Unit,
    onControlPositioned: (Rect) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val count = memos.size
    val latestCount by rememberUpdatedState(count)
    val memoIdKey = memos.joinToString(separator = "|") { it.id }
    val memoIds = remember(memoIdKey) { memos.map { it.id } }
    val initialSelectedIndex = selectedMemoId?.let { memoIds.indexOf(it) } ?: -1
    val initialCarouselOffset = if (initialSelectedIndex >= 0) {
        -initialSelectedIndex.toFloat()
    } else {
        0f
    }
    val carouselOffset = remember(memoIdKey) {
        Animatable(initialCarouselOffset)
    }
    var handledSettleSerial by remember { mutableStateOf(0) }
    val settleTargetIndex = settleTargetId?.let { memoIds.indexOf(it) } ?: -1
    val hasPendingSettle = settleSerial > 0 &&
        settleSerial != handledSettleSerial &&
        settleTargetIndex >= 0 &&
        draggingId == null
    val effectiveCarouselOffset = if (hasPendingSettle) {
        -settleTargetIndex.toFloat()
    } else {
        carouselOffset.value
    }
    var carouselDragging by remember { mutableStateOf(false) }
    var confirmedDragSwapCandidateId by remember { mutableStateOf<String?>(null) }
    var dragAllowedSwapCandidateId by remember { mutableStateOf<String?>(null) }
    var dragStartWindowPoint by remember { mutableStateOf<Offset?>(null) }
    var twoCardSideSign by remember(memoIdKey) { mutableStateOf(1) }
    val swapPreviewProgress = remember { Animatable(0f) }
    val swapActivationPaddingPx = with(density) { 64.dp.toPx() }
    val swapActivationDistancePx = with(density) { 132.dp.toPx() }
    val swapActivationDragPx = with(density) { 58.dp.toPx() }
    val dragStartPoint = dragStartWindowPoint
    val dragVector = if (dragStartPoint == null) Offset.Zero else dragPoint - dragStartPoint
    val dragDistance = kotlin.math.sqrt(dragVector.x * dragVector.x + dragVector.y * dragVector.y)

    fun traceMemoId(id: String?): String {
        if (id == null) return "null"
        val title = memos.firstOrNull { it.id == id }?.title?.ifBlank { "(blank)" } ?: "(missing)"
        return "$title#${id.takeLast(6)}"
    }

    fun traceCarouselOrder(): String {
        return memos.mapIndexed { index, memo ->
            "${index + 1}:${memo.title.ifBlank { "(blank)" }}#${memo.id.takeLast(6)}"
        }.joinToString(" > ")
    }

    val dragSwapCandidateId = if (
        draggingId != null &&
        dragStartPoint != null &&
        dragDistance >= swapActivationDragPx &&
        count > 1 &&
        !imageTargetActive &&
        !trashTargetActive
    ) {
        val startPoint = dragStartPoint
        val allowedCandidateId = dragAllowedSwapCandidateId
        val directionX = dragVector.x / dragDistance.coerceAtLeast(1f)
        val directionY = dragVector.y / dragDistance.coerceAtLeast(1f)
        val targetBounds = allowedCandidateId?.let { memoCardBounds[it] }
        if (allowedCandidateId != null && targetBounds != null) {
            val targetVector = targetBounds.center - startPoint
            val targetDistance = kotlin.math.sqrt(targetVector.x * targetVector.x + targetVector.y * targetVector.y)
            val projection = targetVector.x * directionX + targetVector.y * directionY
            val sameDirection = targetDistance > 1f && projection / targetDistance > 0.48f
            val crossedTowardTarget = projection > targetDistance * 0.38f && dragDistance > targetDistance * 0.30f
            val isNearTarget = expandedRectContains(targetBounds, dragPoint, swapActivationPaddingPx) ||
                distanceToRect(targetBounds, dragPoint) <= swapActivationDistancePx
            if (sameDirection && crossedTowardTarget && isNearTarget) allowedCandidateId else null
        } else {
            null
        }
    } else {
        null
    }
    val latestDragSwapCandidateId by rememberUpdatedState(dragSwapCandidateId)
    val activeDragSwapCandidateId = confirmedDragSwapCandidateId
    val latestConfirmedDragSwapCandidateId by rememberUpdatedState(activeDragSwapCandidateId)

    fun selectedMemoIdForOffset(offset: Float): String? {
        if (count <= 0) return null
        return memos.getOrNull(floorModIndex(-offset.roundToInt(), count))?.id
    }

    LaunchedEffect(memoIdKey, selectedMemoId) {
        if (carouselDragging || hasPendingSettle) {
            Log.d(
                HomeCarouselTraceTag,
                "carousel selectedEffect skipped dragging=$carouselDragging pending=$hasPendingSettle selected=${traceMemoId(selectedMemoId)} offset=${carouselOffset.value} order=${traceCarouselOrder()}"
            )
            return@LaunchedEffect
        }
        Log.d(
            HomeCarouselTraceTag,
            "carousel selectedEffect apply selected=${traceMemoId(selectedMemoId)} offsetBefore=${carouselOffset.value} order=${traceCarouselOrder()}"
        )
        if (count == 0) {
            carouselOffset.snapTo(0f)
        } else {
            val selectedIndex = selectedMemoId?.let { memoIds.indexOf(it) } ?: -1
            if (selectedIndex >= 0) {
                carouselOffset.snapTo(-selectedIndex.toFloat())
            } else {
                carouselOffset.snapTo(carouselOffset.value.coerceIn(-count.toFloat(), count.toFloat()))
            }
        }
        Log.d(
            HomeCarouselTraceTag,
            "carousel selectedEffect after offset=${carouselOffset.value} front=${traceMemoId(selectedMemoIdForOffset(carouselOffset.value))}"
        )
    }

    LaunchedEffect(draggingId) {
        if (draggingId == null) {
            Log.d(
                HomeCarouselTraceTag,
                "carousel draggingEffect clear offsetBefore=${carouselOffset.value} selected=${traceMemoId(selectedMemoId)} pending=$hasPendingSettle order=${traceCarouselOrder()}"
            )
            confirmedDragSwapCandidateId = null
            dragAllowedSwapCandidateId = null
            dragStartWindowPoint = null
            swapPreviewProgress.snapTo(0f)
            if (!hasPendingSettle && count == 0) {
                carouselOffset.snapTo(0f)
            } else if (!hasPendingSettle) {
                val selectedIndex = selectedMemoId?.let { memoIds.indexOf(it) } ?: -1
                if (selectedIndex >= 0) {
                    carouselOffset.snapTo(-selectedIndex.toFloat())
                }
            }
            carouselDragging = false
            Log.d(
                HomeCarouselTraceTag,
                "carousel draggingEffect cleared offsetAfter=${carouselOffset.value} front=${traceMemoId(selectedMemoIdForOffset(carouselOffset.value))}"
            )
        } else {
            Log.d(
                HomeCarouselTraceTag,
                "carousel draggingEffect active dragging=${traceMemoId(draggingId)} selected=${traceMemoId(selectedMemoId)} offset=${carouselOffset.value} order=${traceCarouselOrder()}"
            )
        }
    }

    LaunchedEffect(settleSerial, settleTargetId, memoIdKey) {
        if (settleSerial > 0 && settleTargetIndex >= 0) {
            Log.d(
                HomeCarouselTraceTag,
                "carousel settle serial=$settleSerial target=${traceMemoId(settleTargetId)} index=$settleTargetIndex offsetBefore=${carouselOffset.value} order=${traceCarouselOrder()}"
            )
            carouselOffset.snapTo(-settleTargetIndex.toFloat())
            handledSettleSerial = settleSerial
            carouselDragging = false
            Log.d(
                HomeCarouselTraceTag,
                "carousel settle after serial=$settleSerial offset=${carouselOffset.value} front=${traceMemoId(selectedMemoIdForOffset(carouselOffset.value))}"
            )
        }
    }

    LaunchedEffect(dragSwapCandidateId) {
        val candidateId = dragSwapCandidateId
        Log.d(
            HomeCarouselTraceTag,
            "carousel candidate changed candidate=${traceMemoId(candidateId)} confirmed=${traceMemoId(confirmedDragSwapCandidateId)} allowed=${traceMemoId(dragAllowedSwapCandidateId)} dragging=${traceMemoId(draggingId)} dragDistance=$dragDistance"
        )
        if (draggingId != null && candidateId != null && confirmedDragSwapCandidateId == null) {
            confirmedDragSwapCandidateId = candidateId
        }
    }

    LaunchedEffect(activeDragSwapCandidateId, draggingId, memoIdKey) {
        Log.d(
            HomeCarouselTraceTag,
            "carousel previewEffect active=${traceMemoId(activeDragSwapCandidateId)} dragging=${traceMemoId(draggingId)} order=${traceCarouselOrder()}"
        )
        if (draggingId != null && activeDragSwapCandidateId != null) {
            swapPreviewProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else {
            swapPreviewProgress.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(count, draggingId, leftHandModeEnabled) {
                if (count <= 1 || draggingId != null) return@pointerInput
                var dragTotalX = 0f
                detectDragGestures(
                    onDragStart = {
                        dragTotalX = 0f
                        carouselDragging = true
                        scope.launch { carouselOffset.stop() }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val horizontalDelta = if (leftHandModeEnabled) -amount.x else amount.x
                        dragTotalX += horizontalDelta
                        if (count == 2 && kotlin.math.abs(amount.x) > 0.5f) {
                            twoCardSideSign = if (amount.x < 0f) -1 else 1
                        }
                        val dominantDelta = horizontalDelta + amount.y * 0.35f
                        val step = (dominantDelta / 150f).coerceIn(-0.9f, 0.9f)
                        scope.launch {
                            carouselOffset.snapTo(carouselOffset.value + step)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            val target = if (count == 2 && kotlin.math.abs(dragTotalX) > 48f) {
                                if (dragTotalX < 0f) {
                                    kotlin.math.floor(carouselOffset.value.toDouble()).toFloat()
                                } else {
                                    kotlin.math.ceil(carouselOffset.value.toDouble()).toFloat()
                                }
                            } else {
                                carouselOffset.value.roundToInt().toFloat()
                            }
                            carouselOffset.animateTo(
                                targetValue = target,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            carouselDragging = false
                            selectedMemoIdForOffset(target)?.let(onSelectedMemoChanged)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            carouselOffset.animateTo(
                                targetValue = carouselOffset.value.roundToInt().toFloat(),
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            )
                            carouselDragging = false
                        }
                    }
                )
            }
    ) {
        val cardWidth = ((maxWidth * 0.47f).coerceIn(112.dp, 172.dp)) * HomeOperationScale
        val cardHeight = (cardWidth * 1.18f).coerceIn(150.dp * HomeOperationScale, 230.dp * HomeOperationScale)
        val orbitCenterX = maxWidth * 0.94f
        val orbitCenterY = maxHeight * 0.92f
        val displayedOrbitCenterX = if (leftHandModeEnabled) maxWidth - orbitCenterX else orbitCenterX
        val radiusX = maxWidth * 0.68f * HomeOperationScale
        val radiusY = maxHeight * 0.42f * HomeOperationScale
        val frontAngle = -2.25f
        data class CarouselPlacement(
            val x: Dp,
            val y: Dp,
            val scale: Float,
            val alpha: Float
        )

        fun placementFor(relativeValue: Float): CarouselPlacement {
            val absRelativeValue = kotlin.math.abs(relativeValue)
            val stepAngle = 0.78f
            val angle = frontAngle + relativeValue * stepAngle
            val sin = kotlin.math.sin(angle.toDouble()).toFloat()
            val cos = kotlin.math.cos(angle.toDouble()).toFloat()
            return CarouselPlacement(
                x = orbitCenterX + radiusX * cos - cardWidth / 2f,
                y = orbitCenterY + radiusY * sin - cardHeight / 2f,
                scale = (1.08f - absRelativeValue * 0.11f).coerceIn(0.72f, 1.08f),
                alpha = (1f - (absRelativeValue - 2.5f).coerceAtLeast(0f) * 0.25f).coerceIn(0.35f, 1f)
            )
        }

        val snappedOffset = effectiveCarouselOffset.roundToInt()
        val frontIndex = if (count == 0) 0 else floorModIndex(-snappedOffset, count)
        val fractionalOffset = effectiveCarouselOffset - snappedOffset
        val isCarouselSettled = count > 0 && !carouselDragging && kotlin.math.abs(fractionalOffset) < 0.03f
        val settledSelectedMemo = if (isCarouselSettled) memos.getOrNull(frontIndex) else null

        settledSelectedMemo?.let { memo ->
            HomeSelectedMemoBackdrop(
                memo = memo,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
            )
        }

        if (count > 0) {
            HomeCarouselOrbitGuide(
                centerX = displayedOrbitCenterX,
                centerY = orbitCenterY,
                radiusX = radiusX,
                radiusY = radiusY,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f)
            )
        }

        if (count > 0) {
            memos.forEachIndexed { index, memo ->
                val rawRelative = circularRelativeIndex(
                    index = index,
                    frontIndex = frontIndex,
                    fractionalOffset = fractionalOffset,
                    count = count
                )
                val relative = if (count == 2 && kotlin.math.abs(rawRelative) >= 0.99f) {
                    twoCardSideSign.toFloat()
                } else {
                    rawRelative
                }
                val absRelative = kotlin.math.abs(relative)
                val isPreviewCandidate = memo.id == activeDragSwapCandidateId
                val visible = count <= 6 || absRelative <= 2.6f || isPreviewCandidate
                if (visible) {
                    val basePlacement = placementFor(relative)
                    val frontPlacement = placementFor(0f)
                    val previewProgress = if (isPreviewCandidate) swapPreviewProgress.value.coerceIn(0f, 1f) else 0f
                    val x = basePlacement.x + (frontPlacement.x - basePlacement.x) * previewProgress
                    val displayedX = if (leftHandModeEnabled) maxWidth - x - cardWidth else x
                    val y = basePlacement.y + (frontPlacement.y - basePlacement.y) * previewProgress
                    val scale = lerpFloat(basePlacement.scale, frontPlacement.scale, previewProgress)
                    val alpha = lerpFloat(basePlacement.alpha, 1f, previewProgress)
                    val isFront = absRelative < 0.45f
                    val isVisuallyFront = isFront || previewProgress > 0.55f
                    var cardBounds by remember(memo.id) { mutableStateOf<Rect?>(null) }
                    val isDraggingMemo = draggingId == memo.id

                    Box(
                        modifier = Modifier
                            .align(PhysicalTopLeftAlignment)
                            .absoluteOffset(x = displayedX, y = y)
                            .width(cardWidth)
                            .onGloballyPositioned {
                                val bounds = it.boundsInWindow()
                                cardBounds = bounds
                                memoCardBounds[memo.id] = bounds
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = if (isDraggingMemo) alpha * 0.22f else alpha
                                shadowElevation = if (isVisuallyFront) 24f else 8f
                            }
                            .zIndex(if (previewProgress > 0f) 180f + previewProgress else 100f - absRelative)
                            .then(
                                if (isFront || isDraggingMemo) {
                                    Modifier.pointerInput(memo.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { start ->
                                                carouselDragging = true
                                                dragAllowedSwapCandidateId = if (count > 1) {
                                                    memos.getOrNull(floorModIndex(index + 1, count))?.id
                                                } else {
                                                    null
                                                }
                                                dragStartWindowPoint = cardBounds?.let { bounds ->
                                                    Offset(bounds.left + start.x, bounds.top + start.y)
                                                }
                                                Log.d(
                                                    HomeCarouselTraceTag,
                                                    "carousel cardDragStart memo=${traceMemoId(memo.id)} index=$index allowed=${traceMemoId(dragAllowedSwapCandidateId)} start=$start windowStart=$dragStartWindowPoint offset=${carouselOffset.value} order=${traceCarouselOrder()}"
                                                )
                                                onSelectedMemoChanged(memo.id)
                                                onDragStart(memo, start, cardBounds)
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                onDrag(amount)
                                            },
                                            onDragEnd = {
                                                val swapCandidateId = latestConfirmedDragSwapCandidateId ?: latestDragSwapCandidateId
                                                Log.d(
                                                    HomeCarouselTraceTag,
                                                    "carousel cardDragEnd memo=${traceMemoId(memo.id)} confirmed=${traceMemoId(latestConfirmedDragSwapCandidateId)} latest=${traceMemoId(latestDragSwapCandidateId)} chosen=${traceMemoId(swapCandidateId)} offset=${carouselOffset.value} order=${traceCarouselOrder()}"
                                                )
                                                onDragEnd(swapCandidateId)
                                            },
                                            onDragCancel = {
                                                Log.d(
                                                    HomeCarouselTraceTag,
                                                    "carousel cardDragCancel memo=${traceMemoId(memo.id)} confirmed=${traceMemoId(latestConfirmedDragSwapCandidateId)} latest=${traceMemoId(latestDragSwapCandidateId)} offset=${carouselOffset.value} order=${traceCarouselOrder()}"
                                                )
                                                onDragCancel()
                                                carouselDragging = false
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .then(
                                if (isVisuallyFront) {
                                    Modifier
                                        .border(4.dp, Color(0xFFFFEA00), RoundedCornerShape(14.dp))
                                        .padding(4.dp)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        MemoCard(
                            memo = memo,
                            sparkling = sparklingMemoIds.contains(memo.id),
                            sizeScale = HomeOperationScale,
                            modifier = Modifier
                                .width(cardWidth)
                                ,
                            onClick = {
                                val selectedNow = latestCount == 0 ||
                                    floorModIndex(-effectiveCarouselOffset.roundToInt(), latestCount.coerceAtLeast(1)) == index
                                if (selectedNow) {
                                    onOpenMemo(memo)
                                } else {
                                    scope.launch {
                                        val current = effectiveCarouselOffset.roundToInt()
                                        val delta = shortestCarouselDelta(index, floorModIndex(-current, count), count)
                                        carouselOffset.animateTo(
                                            targetValue = (current - delta).toFloat(),
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                        onSelectedMemoChanged(memo.id)
                                    }
                                }
                            },
                            onToggleFavorite = { onToggleFavorite(memo) }
                        )
                    }
                }
            }
        }

        if (showCardDropTargets && count > 0) {
            val dropTargetHeight = (((maxWidth - 20.dp) / 2f).coerceAtLeast(120.dp)) * HomeOperationScale
            HomeCarouselDropTargets(
                imageTargetActive = imageTargetActive,
                trashTargetActive = trashTargetActive,
                onImageTargetPositioned = onImageTargetPositioned,
                onTrashTargetPositioned = onTrashTargetPositioned,
                leftHandModeEnabled = leftHandModeEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .height(dropTargetHeight)
                    .zIndex(260f)
            )
        }

        val controlWidth = ((maxWidth * 0.34f).coerceIn(104.dp, 150.dp)) * HomeOperationScale
        val memoButtonSize = (controlWidth * 0.68f).coerceIn(68.dp * HomeOperationScale, 92.dp * HomeOperationScale)
        val controlSideX = if (leftHandModeEnabled) {
            12.dp
        } else {
            maxWidth - controlWidth - 12.dp
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .align(PhysicalBottomLeftAlignment)
                    .absoluteOffset(x = controlSideX, y = (-12).dp)
                    .width(controlWidth)
                    .zIndex(200f)
                    .onGloballyPositioned { onControlPositioned(it.boundsInWindow()) },
                horizontalAlignment = if (leftHandModeEnabled) Alignment.Start else Alignment.End
            ) {
                HomeTrashDisplayButton(
                    onClick = onShowTrash,
                    sizeScale = HomeOperationScale,
                    modifier = Modifier.size(86.dp * HomeOperationScale)
                )
                Spacer(Modifier.height(10.dp * HomeOperationScale))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    HomeMemoButton(
                        onClick = onOpenTemporaryMemo,
                        sizeScale = HomeOperationScale,
                        modifier = Modifier
                            .size(memoButtonSize)
                            .align(if (leftHandModeEnabled) Alignment.TopEnd else Alignment.TopStart)
                            .offset(
                                x = if (leftHandModeEnabled) memoButtonSize * 0.62f else -(memoButtonSize * 0.62f),
                                y = -(memoButtonSize * 0.62f)
                            )
                            .zIndex(1f)
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .align(if (leftHandModeEnabled) Alignment.BottomStart else Alignment.BottomEnd)
                    )
                }
            }
        }
    }
    }
@Composable
private fun HomeCarouselOrbitGuide(
    centerX: Dp,
    centerY: Dp,
    radiusX: Dp,
    radiusY: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = centerX.toPx()
        val cy = centerY.toPx()
        val rx = radiusX.toPx()
        val ry = radiusY.toPx()

        fun drawRing(scale: Float, color: Color, strokeWidth: Dp) {
            val scaledRx = rx * scale
            val scaledRy = ry * scale
            drawOval(
                color = color,
                topLeft = Offset(cx - scaledRx, cy - scaledRy),
                size = Size(scaledRx * 2f, scaledRy * 2f),
                style = Stroke(width = strokeWidth.toPx())
            )
        }

        drawRing(
            scale = 1f,
            color = Color(0xFF6B6B6B).copy(alpha = 0.22f),
            strokeWidth = 5.dp
        )
    }
}

@Composable
private fun HomeSelectedMemoBackdrop(
    memo: ShoppingMemo,
    modifier: Modifier = Modifier
) {
    val sizes = LocalAppFontSizes.current
    val activeCount = memo.entries.count { !it.checked && it.name.isNotBlank() }
    val doneCount = memo.entries.count { it.checked && it.name.isNotBlank() }
    val totalCount = activeCount + doneCount
    val progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()
    val percent = (progress * 100f).roundToInt()
    val displayEntries = memo.entries.filter { it.name.isNotBlank() }.take(12)

    Column(
        modifier = modifier
            .background(Color.White)
            .padding(bottom = 88.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizedMemoDisplayTitle(memo, R.string.title_input),
                color = Color(0xFF212121),
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            MemoCardImage(
                memo = memo,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(0.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeProgressBarHeight)
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(Color(0xFF4B50D8))
            )
            Text(
                text = "$percent %",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (displayEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.items_empty),
                    color = Color(0xFF888888),
                    fontSize = sizes.listPlaceholder,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            displayEntries.forEachIndexed { index, entry ->
                HomeSelectedMemoBackdropRow(index = index, entry = entry)
            }
        }
    }
}

@Composable
private fun HomeSelectedMemoBackdropRow(
    index: Int,
    entry: ShoppingEntry
) {
    val sizes = LocalAppFontSizes.current
    val rowBackground = when {
        entry.checked -> CompletedEntryBackground
        else -> entryColorMarkBackground(entry.colorMark)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(rowBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE3F2FD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                color = Color(0xFF1976D2),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = entry.name,
            color = if (entry.checked) Color(0xFF777777) else Color.Black,
            fontSize = sizes.listText,
            lineHeight = sizes.listLineHeight,
            fontWeight = FontWeight.Normal,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        )
    }
    Divider(color = Color(0xFFE0E0E0))
}

private fun floorModIndex(value: Int, size: Int): Int {
    if (size <= 0) return 0
    val mod = value % size
    return if (mod < 0) mod + size else mod
}

private fun expandedRectContains(rect: Rect, point: Offset, padding: Float): Boolean {
    return point.x >= rect.left - padding &&
        point.x <= rect.right + padding &&
        point.y >= rect.top - padding &&
        point.y <= rect.bottom + padding
}

private fun distanceToRect(rect: Rect, point: Offset): Float {
    val dx = when {
        point.x < rect.left -> rect.left - point.x
        point.x > rect.right -> point.x - rect.right
        else -> 0f
    }
    val dy = when {
        point.y < rect.top -> rect.top - point.y
        point.y > rect.bottom -> point.y - rect.bottom
        else -> 0f
    }
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun smallerFittingFontSize(current: Float, minFontSize: TextUnit): Float {
    return maxOf(minFontSize.value, current * 0.88f)
}

@Composable
private fun FittingSingleLineText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeightRatio: Float = 1.15f
) {
    BoxWithConstraints(modifier = modifier) {
        var fontSizeValue by remember(text, maxWidth, maxFontSize, minFontSize) {
            mutableStateOf(maxFontSize.value)
        }
        Text(
            text = text,
            color = color,
            fontSize = fontSizeValue.sp,
            lineHeight = (fontSizeValue * lineHeightRatio).sp,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { result ->
                if (result.didOverflowWidth && fontSizeValue > minFontSize.value) {
                    fontSizeValue = smallerFittingFontSize(fontSizeValue, minFontSize)
                }
            }
        )
    }
}

private fun circularRelativeIndex(
    index: Int,
    frontIndex: Int,
    fractionalOffset: Float,
    count: Int
): Float {
    if (count <= 0) return 0f
    var value = index - frontIndex + fractionalOffset
    val half = count / 2f
    while (value > half) value -= count
    while (value < -half) value += count
    return value
}

private fun shortestCarouselDelta(targetIndex: Int, currentIndex: Int, count: Int): Int {
    if (count <= 0) return 0
    var delta = targetIndex - currentIndex
    val half = count / 2
    while (delta > half) delta -= count
    while (delta < -half) delta += count
    return delta
}

@Composable
private fun HomeCarouselDropTargets(
    imageTargetActive: Boolean,
    trashTargetActive: Boolean,
    onImageTargetPositioned: (Rect) -> Unit,
    onTrashTargetPositioned: (Rect) -> Unit,
    leftHandModeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    @Composable
    fun ImageTarget(modifier: Modifier = Modifier) {
        HomeCarouselDropTargetCard(
            icon = stringResource(R.string.action_image),
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF1565C0),
            active = imageTargetActive,
            modifier = modifier.onGloballyPositioned { onImageTargetPositioned(it.boundsInWindow()) }
        )
    }

    @Composable
    fun TrashTarget(modifier: Modifier = Modifier) {
        HomeCarouselDropTargetCard(
            icon = "\uD83D\uDDD1",
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFD32F2F),
            active = trashTargetActive,
            modifier = modifier.onGloballyPositioned { onTrashTargetPositioned(it.boundsInWindow()) }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val targetModifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            if (leftHandModeEnabled) {
                TrashTarget(targetModifier)
                ImageTarget(targetModifier)
            } else {
                ImageTarget(targetModifier)
                TrashTarget(targetModifier)
            }
        }
    }
}

@Composable
private fun HomeCarouselDropTargetCard(
    icon: String,
    containerColor: Color,
    contentColor: Color,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.sparkleOverlay(rememberSparkleAlpha(active)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (active) 4.dp else 2.dp, contentColor.copy(alpha = if (active) 0.95f else 0.38f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (active) 14.dp else 7.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = contentColor,
                fontSize = if (icon == "\uD83D\uDDD1") 42.sp else 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeCardDropTargets(
    modifier: Modifier = Modifier,
    imageTargetActive: Boolean,
    trashTargetActive: Boolean,
    bottomInset: Dp,
    onImageTargetPositioned: (Rect) -> Unit,
    onTrashTargetPositioned: (Rect) -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = bottomInset),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HomeDropTargetCard(
            title = stringResource(R.string.image_change_title_short),
            icon = stringResource(R.string.action_image),
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF1565C0),
            active = imageTargetActive,
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { onImageTargetPositioned(it.boundsInWindow()) }
        )
        HomeDropTargetCard(
            title = stringResource(R.string.tab_trash),
            icon = stringResource(R.string.action_delete),
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFD32F2F),
            active = trashTargetActive,
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { onTrashTargetPositioned(it.boundsInWindow()) }
        )
    }
}

@Composable
private fun HomeDropTargetCard(
    title: String,
    icon: String,
    containerColor: Color,
    contentColor: Color,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val sparkleAlpha = rememberSparkleAlpha(active)
    val isTrashTarget = contentColor == Color(0xFFD32F2F)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .sparkleOverlay(sparkleAlpha),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (active) 4.dp else 2.dp, contentColor.copy(alpha = if (active) 0.95f else 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (active) 14.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isTrashTarget) "🗑" else stringResource(R.string.action_image),
                color = contentColor,
                fontSize = if (isTrashTarget) 56.sp else 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeTrashPage(
    memos: List<ShoppingMemo>,
    gridState: LazyGridState,
    onRestoreMemo: (ShoppingMemo) -> Unit,
    onEraseMemo: (ShoppingMemo) -> Unit
) {
    if (memos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Text(stringResource(R.string.trash_empty), color = Color(0xFF888888), fontSize = 16.sp, modifier = Modifier.padding(top = 44.dp))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(memos, key = { it.id }) { memo ->
            DeletedMemoCard(
                memo = memo,
                onRestore = { onRestoreMemo(memo) },
                onErase = { onEraseMemo(memo) }
            )
        }
    }
}

@Composable
private fun DeletedMemoCard(
    memo: ShoppingMemo,
    onRestore: () -> Unit,
    onErase: () -> Unit
) {
    val activeCount = memo.entries.count { !it.checked && it.name.isNotBlank() }
    val doneCount = memo.entries.count { it.checked && it.name.isNotBlank() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                MemoCardImage(memo = memo, modifier = Modifier.size(56.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    localizedMemoDisplayTitle(memo),
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(stringResource(R.string.active_done_count_summary, activeCount, doneCount), color = Color(0xFF777777), fontSize = 13.sp)
            }
            TextButton(onClick = onRestore) {
                Text(stringResource(R.string.action_restore), color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onErase) {
                Text(stringResource(R.string.action_erase), color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ImageChangeChoiceDialog(
    memo: ShoppingMemo,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onFolder: () -> Unit,
    onPattern: () -> Unit
) {
    val appLocale = LocalContext.current.appLocale()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.image_change_title, localizedMemoDisplayTitle(memo, R.string.card))) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImageChoiceButton(stringResource(R.string.camera), stringResource(R.string.shoot), Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f), onCamera)
                ImageChoiceButton(stringResource(R.string.folder), stringResource(R.string.photo), Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f), onFolder)
                ImageChoiceButton(stringResource(R.string.pattern), stringResource(R.string.pattern_count, activeShoppingVisualPatterns(appLocale).size), Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f), onPattern)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun ImageChoiceButton(
    title: String,
    caption: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(caption, color = contentColor.copy(alpha = 0.78f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun HomeScreen(
    memos: List<ShoppingMemo>,
    temporaryMemo: ShoppingMemo,
    onAddMemo: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onOpenTemporaryMemo: () -> Unit,
    onTemporaryEntryMoved: (ShoppingMemo, ShoppingEntry) -> Unit,
    onDeleteMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit
) {
    val scope = rememberCoroutineScope()
    val memoCardBounds = remember { mutableStateMapOf<String, Rect>() }
    val sparklingMemoIds = remember { mutableStateListOf<String>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPoint by remember { mutableStateOf(Offset.Zero) }
    var draggedCardBounds by remember { mutableStateOf<Rect?>(null) }
    var trashBounds by remember { mutableStateOf<Rect?>(null) }
    var pendingDelete by remember { mutableStateOf<ShoppingMemo?>(null) }
    var homeBounds by remember { mutableStateOf<Rect?>(null) }
    var draggingTemporaryEntry by remember { mutableStateOf<ShoppingEntry?>(null) }
    var temporaryDragPoint by remember { mutableStateOf(Offset.Zero) }

    fun moveTemporaryEntryTo(targetMemo: ShoppingMemo, entry: ShoppingEntry) {
        temporaryMemo.entries.remove(entry)
        val insertIndex = if (entry.checked) {
            targetMemo.entries.size
        } else {
            targetMemo.entries.indexOfFirst { it.name.isBlank() || it.checked }.let { if (it >= 0) it else targetMemo.entries.size }
        }
        targetMemo.entries.add(insertIndex, entry)
        ensureDisplayBlankEntry(temporaryMemo)
        ensureDisplayBlankEntry(targetMemo)
        onTemporaryEntryMoved(targetMemo, entry)
        sparklingMemoIds.remove(targetMemo.id)
        sparklingMemoIds.add(targetMemo.id)
        scope.launch {
            delay(5000)
            sparklingMemoIds.remove(targetMemo.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { homeBounds = it.boundsInWindow() }
    ) {
    Column(Modifier.fillMaxSize()) {
        Header(
            title = stringResource(R.string.shopping_memo_title),
            trailing = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .onGloballyPositioned { trashBounds = it.boundsInWindow() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (draggingId != null) "🗑" else "↻",
                        color = if (draggingId != null) Color(0xFFD32F2F) else Color(0xFF03A9F4),
                        fontSize = 36.sp
                    )
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 6.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memos, key = { it.id }) { memo ->
                        val isDragging = draggingId == memo.id
                        MemoCard(
                            memo = memo,
                            sparkling = sparklingMemoIds.contains(memo.id),
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = if (isDragging) dragOffset.x else 0f
                                    translationY = if (isDragging) dragOffset.y else 0f
                                    scaleX = if (isDragging) 1.05f else 1f
                                    scaleY = if (isDragging) 1.05f else 1f
                                    shadowElevation = if (isDragging) 18f else 0f
                                }
                                .onGloballyPositioned {
                                    val bounds = it.boundsInWindow()
                                    memoCardBounds[memo.id] = bounds
                                    if (isDragging || draggingId == null) draggedCardBounds = bounds
                                }
                                .pointerInput(memo.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { start ->
                                            draggingId = memo.id
                                            dragOffset = Offset.Zero
                                            val bounds = draggedCardBounds
                                            dragPoint = if (bounds == null) start else Offset(bounds.left + start.x, bounds.top + start.y)
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset += amount
                                            dragPoint += amount
                                        },
                                        onDragEnd = {
                                            if (trashBounds?.contains(dragPoint) == true) pendingDelete = memo
                                            draggingId = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingId = null
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                },
                            onClick = { if (draggingId == null) onOpenMemo(memo) },
                            onToggleFavorite = { onToggleFavorite(memo) }
                        )
                    }
                }
                HomeFixedActionButton(
                    icon = "+",
                    label = stringResource(R.string.action_add),
                    containerColor = Color(0xFFEAF4FF),
                    contentColor = Color(0xFF1976D2),
                    onClick = onAddMemo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFBDBDBD))
            )
            TemporaryMemoPanel(
                memo = temporaryMemo,
                draggingEntryId = draggingTemporaryEntry?.id,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                onOpen = onOpenTemporaryMemo,
                onListPositioned = {},
                onDragStart = { entry, start, bounds ->
                    draggingTemporaryEntry = entry
                    temporaryDragPoint = if (bounds == null) start else Offset(bounds.left + start.x, bounds.top + start.y)
                },
                onDrag = { delta ->
                    temporaryDragPoint += delta
                },
                onDragEnd = {
                    val entry = draggingTemporaryEntry
                    val target = memos.firstOrNull { memo ->
                        memoCardBounds[memo.id]?.contains(temporaryDragPoint) == true
                    }
                    if (entry != null && target != null) {
                        moveTemporaryEntryTo(target, entry)
                    }
                    draggingTemporaryEntry = null
                },
                onDragCancel = {
                    draggingTemporaryEntry = null
                }
            )
        }
    }
        draggingTemporaryEntry?.let { entry ->
            TemporaryFloatingEntry(
                entry = entry,
                position = Offset(
                    x = temporaryDragPoint.x - (homeBounds?.left ?: 0f) - 84f,
                    y = temporaryDragPoint.y - (homeBounds?.top ?: 0f) - 28f
                ),
                modifier = Modifier.zIndex(10f)
            )
        }
    }

    pendingDelete?.let { memo ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_card_confirm_title)) },
            text = { Text(stringResource(R.string.delete_card_confirm_body, localizedMemoDisplayTitle(memo, R.string.this_card))) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMemo(memo)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_yes), color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun TemporaryMemoPanel(
    memo: ShoppingMemo,
    draggingEntryId: String?,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onListPositioned: (Rect) -> Unit,
    onDragStart: (ShoppingEntry, Offset, Rect?) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val activeEntries = memo.entries.filter { !it.checked && it.name.isNotBlank() }
    val doneEntries = memo.entries.filter { it.checked && it.name.isNotBlank() }
    val totalCount = activeEntries.size + doneEntries.size
    val listState = rememberLazyListState()
    Card(
        modifier = modifier.pointerInput(onOpen) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                var moved = false
                var upTime = down.uptimeMillis
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    upTime = change.uptimeMillis
                    val distance = change.position - down.position
                    if (kotlin.math.abs(distance.x) > viewConfiguration.touchSlop ||
                        kotlin.math.abs(distance.y) > viewConfiguration.touchSlop
                    ) {
                        moved = true
                    }
                    if (!change.pressed) break
                }
                val heldLongEnough = upTime - down.uptimeMillis >= viewConfiguration.longPressTimeoutMillis
                if (!moved && !heldLongEnough) onOpen()
            }
        },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.temporary_short_title),
                    color = Color(0xFFD32F2F),
                    fontSize = 20.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    completedCountSummary(doneEntries.size, totalCount),
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
            }
            Divider(color = Color(0xFFE0E0E0))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { onListPositioned(it.boundsInWindow()) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                if (activeEntries.isEmpty() && doneEntries.isEmpty()) {
                    item {
                        Text(stringResource(R.string.empty), color = Color(0xFF999999), fontSize = 13.sp)
                    }
                } else {
                    items(activeEntries, key = { "temporary-active-${it.id}" }) { entry ->
                        TemporaryPreviewEntry(
                            entry = entry,
                            done = false,
                            isDragging = draggingEntryId == entry.id,
                            onDragStart = onDragStart,
                            onDrag = onDrag,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    }
                    if (doneEntries.isNotEmpty()) {
                        item {
                            Text(stringResource(R.string.completed_items), color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(doneEntries, key = { "temporary-done-${it.id}" }) { entry ->
                        TemporaryPreviewEntry(
                            entry = entry,
                            done = true,
                            isDragging = draggingEntryId == entry.id,
                            onDragStart = onDragStart,
                            onDrag = onDrag,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeLeftControlBoundary(
    modifier: Modifier = Modifier,
    controlHeight: Dp
) {
    Canvas(modifier) {
        val stroke = 3.dp.toPx()
        val radius = 12.dp.toPx()
        val centerX = size.width / 2f
        val controlTop = (size.height - controlHeight.toPx()).coerceAtLeast(0f)
        val cornerStartY = (controlTop - radius).coerceAtLeast(0f)
        val path = Path().apply {
            moveTo(centerX, 0f)
            lineTo(centerX, cornerStartY)
            quadraticTo(centerX, controlTop, centerX + radius, controlTop)
            lineTo(size.width, controlTop)
        }
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun HomeLeftBottomControls(
    modifier: Modifier = Modifier,
    onAddMemo: () -> Unit,
    onPositioned: (Rect) -> Unit,
    onScrollGestureStart: () -> Unit,
    onScroll: (Float) -> Float
) {
    Box(
        modifier = modifier
            .onGloballyPositioned { onPositioned(it.boundsInWindow()) }
            .background(Color.White.copy(alpha = 0.98f))
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp, end = 6.dp)
    ) {
        HomeAddScrollButton(
            onScroll = onScroll,
            onScrollGestureStart = onScrollGestureStart,
            onClick = onAddMemo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}

@Composable
private fun HomeAddScrollButton(
    modifier: Modifier = Modifier,
    onScroll: (Float) -> Float,
    onScrollGestureStart: () -> Unit,
    onClick: () -> Unit
) {
    val scrollState = rememberScrollableState { delta ->
        onScroll(delta)
    }
    Card(
        modifier = modifier
            .pointerInput(onScrollGestureStart) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onScrollGestureStart()
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                    } while (event.changes.any { it.pressed })
                }
            }
            .scrollable(
                state = scrollState,
                orientation = Orientation.Vertical
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF4FF),
        ),
        border = BorderStroke(2.dp, Color(0xFF90CAF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = Color(0xFF1976D2), fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    Text(stringResource(R.string.action_add), color = Color(0xFF1976D2), fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text("↕", color = Color(0xFFAD1457), fontSize = 46.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)
            }
        }
    }
}

@Composable
private fun HomeMicrophoneButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF1E88E5),
        modifier = modifier.size(86.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fab_mic),
            contentDescription = stringResource(R.string.content_description_microphone),
            tint = Color.White,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
private fun HomeFixedActionButton(
    icon: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    iconFontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    labelFontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = iconFontSize, fontWeight = FontWeight.Bold, lineHeight = iconFontSize)
            Text(label, fontSize = labelFontSize, fontWeight = FontWeight.Bold, lineHeight = labelFontSize)
        }
    }
}

@Composable
private fun TemporaryFloatingEntry(
    entry: ShoppingEntry,
    position: Offset,
    modifier: Modifier = Modifier
) {
    val sparkleAlpha = rememberSparkleAlpha(true)
    Surface(
        modifier = modifier
            .graphicsLayer {
                translationX = position.x
                translationY = position.y
                shadowElevation = 18f
            }
            .width(180.dp)
            .sparkleOverlay(sparkleAlpha),
        shape = RoundedCornerShape(8.dp),
        color = if (entry.checked) CompletedEntryBackground else Color.White,
        border = BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("・", color = Color(0xFFD32F2F), fontSize = 18.sp)
            Text(
                text = entry.name,
                color = if (entry.checked) Color(0xFF777777) else Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TemporaryPreviewEntry(
    entry: ShoppingEntry,
    done: Boolean,
    isDragging: Boolean,
    onDragStart: (ShoppingEntry, Offset, Rect?) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var rowBounds by remember { mutableStateOf<Rect?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isDragging) 0.35f else 1f }
            .background(if (done) CompletedEntryBackground else entryColorMarkBackground(entry.colorMark))
            .onGloballyPositioned { rowBounds = it.boundsInWindow() }
            .pointerInput(entry.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { start -> onDragStart(entry, start, rowBounds) },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            },
        verticalAlignment = Alignment.Top
    ) {
        Text("・", color = Color(0xFF777777), fontSize = 13.sp)
        Text(
            text = entry.name,
            color = if (done) Color(0xFF777777) else Color.Black,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MemoCard(
    memo: ShoppingMemo,
    modifier: Modifier = Modifier,
    sparkling: Boolean = false,
    sizeScale: Float = 1f,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val activeCount = memo.entries.count { !it.checked && it.name.isNotBlank() }
    val doneCount = memo.entries.count { it.checked }
    val totalCount = activeCount + doneCount
    val sparkleAlpha = rememberSparkleAlpha(sparkling)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp * sizeScale)
            .clickable(onClick = onClick)
            .sparkleOverlay(sparkleAlpha),
        shape = RoundedCornerShape(12.dp * sizeScale),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp * sizeScale)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = (48f * sizeScale).sp)
                MemoCardImage(memo = memo, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp * sizeScale)
                        .size(34.dp * sizeScale)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Text(
                        text = if (memo.favorite) "★" else "☆",
                        color = if (memo.favorite) Color(0xFFFFA000) else Color(0xFF555555),
                        fontSize = (22f * sizeScale).sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp * sizeScale, vertical = 6.dp * sizeScale)
            ) {
                Text(
                    completedCountSummary(doneCount, totalCount),
                    color = Color(0xFF2E7D32),
                    fontSize = (16f * sizeScale).sp,
                    lineHeight = (18f * sizeScale).sp
                )
                Text(
                    text = localizedMemoDisplayTitle(memo, R.string.title_input),
                    color = Color(0xFF212121),
                    fontSize = (22f * sizeScale).sp,
                    lineHeight = (24f * sizeScale).sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AddMemoCard(
    compact: Boolean,
    onClick: () -> Unit,
    contentScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (compact) {
        modifier.aspectRatio(1f)
    } else {
        modifier
            .fillMaxWidth()
            .height(300.dp)
    }
    Card(
        modifier = cardModifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("+", color = Color(0xFF1976D2), fontSize = ((if (compact) 64f else 96f) * contentScale).sp)
        }
    }
}

@Composable
private fun HomeMemoButton(
    onClick: () -> Unit,
    sizeScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    MemoNoteIcon(
        sizeScale = sizeScale,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun MemoNoteIcon(
    sizeScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp * sizeScale),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, Color(0xFFF9A825)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEE88))
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.memo_short),
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF3A3124),
                fontSize = (24f * sizeScale).sp,
                lineHeight = (26f * sizeScale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp * sizeScale)
            ) {
                val foldPath = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = foldPath,
                    color = Color(0xFFFFD54F)
                )
                drawPath(
                    path = foldPath,
                    color = Color(0xFFEF9A9A).copy(alpha = 0.55f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun HomeTrashDisplayButton(
    onClick: () -> Unit,
    sizeScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp * sizeScale),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFF90CAF9)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.show_trash),
                color = Color(0xFF1976D2),
                fontSize = (19f * sizeScale).sp,
                lineHeight = (23f * sizeScale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class ShoppingVisualKind {
    Food,
    Cart,
    Appliance,
    Game,
    Restaurant,
    Clothing,
    Sports,
    Service,
    HundredYenShop,
    DiscountShop,
    DailyGoods
}

private data class ShoppingVisualPattern(
    val nameResId: Int,
    val kind: ShoppingVisualKind,
    val background: Color,
    val accent: Color
)

private enum class GroceryVisualRegion {
    Japan,
    Asia,
    Europe
}

private fun groceryVisualRegion(locale: Locale): GroceryVisualRegion {
    val language = locale.language.lowercase(Locale.ROOT)
    return when {
        language == Locale.JAPANESE.language -> GroceryVisualRegion.Japan
        language in setOf("ar", "hi", "id", "ko", "th", "vi", "zh") -> GroceryVisualRegion.Asia
        else -> GroceryVisualRegion.Europe
    }
}

private fun isEnglishLocale(locale: Locale): Boolean {
    return locale.language.lowercase(Locale.ROOT) == Locale.ENGLISH.language
}

private fun activeShoppingVisualPatterns(locale: Locale): List<ShoppingVisualPattern> {
    val region = groceryVisualRegion(locale)
    val isJapanese = region == GroceryVisualRegion.Japan
    val foodBackground = when (region) {
        GroceryVisualRegion.Japan -> Color(0xFFFFA7B4)
        GroceryVisualRegion.Asia -> Color(0xFFFFE9B8)
        GroceryVisualRegion.Europe -> Color(0xFF8FD8FF)
    }
    return buildList {
        add(ShoppingVisualPattern(R.string.pattern_food, ShoppingVisualKind.Food, foodBackground, Color(0xFF2E7D32)))
        add(ShoppingVisualPattern(R.string.pattern_cart, ShoppingVisualKind.Cart, Color(0xFFFFE166), Color(0xFFD32F2F)))
        add(ShoppingVisualPattern(R.string.pattern_appliance, ShoppingVisualKind.Appliance, Color(0xFFC994F5), Color(0xFF1565C0)))
        add(ShoppingVisualPattern(R.string.pattern_game, ShoppingVisualKind.Game, Color(0xFFB7EC80), Color(0xFF512DA8)))
        add(ShoppingVisualPattern(R.string.pattern_restaurant, ShoppingVisualKind.Restaurant, Color(0xFFFFB63E), Color(0xFFF57F17)))
        add(ShoppingVisualPattern(R.string.pattern_clothing, ShoppingVisualKind.Clothing, Color(0xFFFF9FBC), Color(0xFFC2185B)))
        add(ShoppingVisualPattern(R.string.pattern_sports, ShoppingVisualKind.Sports, Color(0xFF9FDDFF), Color(0xFF00796B)))
        add(ShoppingVisualPattern(R.string.pattern_service, ShoppingVisualKind.Service, Color(0xFFFFDC54), Color(0xFFF9A825)))
        if (isJapanese) {
            add(ShoppingVisualPattern(R.string.pattern_hundred_yen_shop, ShoppingVisualKind.HundredYenShop, Color(0xFFFF9FCA), Color(0xFFD32F2F)))
        }
        add(ShoppingVisualPattern(R.string.pattern_discount_shop, ShoppingVisualKind.DiscountShop, Color(0xFFB9EB55), Color(0xFF8BC34A)))
        add(ShoppingVisualPattern(R.string.pattern_daily_goods, ShoppingVisualKind.DailyGoods, Color(0xFF52D1D1), Color(0xFF00838F)))
    }
}

private fun defaultNewMemoImagePattern(locale: Locale): Int {
    val index = activeShoppingVisualPatterns(locale).indexOfFirst { it.kind == ShoppingVisualKind.Cart }
    return if (index >= 0) index else 0
}

private fun shoppingPatternImageResId(kind: ShoppingVisualKind, locale: Locale): Int {
    return when (kind) {
        ShoppingVisualKind.Food -> when (groceryVisualRegion(locale)) {
            GroceryVisualRegion.Japan -> R.drawable.pattern_food_jp
            GroceryVisualRegion.Asia -> R.drawable.pattern_food_asia
            GroceryVisualRegion.Europe -> R.drawable.pattern_food_europe
        }
        ShoppingVisualKind.Cart -> R.drawable.pattern_cart
        ShoppingVisualKind.Appliance -> R.drawable.pattern_appliance
        ShoppingVisualKind.Game -> R.drawable.pattern_game
        ShoppingVisualKind.Restaurant -> R.drawable.pattern_restaurant
        ShoppingVisualKind.Clothing -> R.drawable.pattern_fashion
        ShoppingVisualKind.Sports -> R.drawable.pattern_sports
        ShoppingVisualKind.Service -> R.drawable.pattern_service
        ShoppingVisualKind.HundredYenShop -> R.drawable.pattern_100_yen_shop
        ShoppingVisualKind.DiscountShop -> if (isEnglishLocale(locale)) {
            R.drawable.pattern_discount_shop
        } else {
            R.drawable.pattern_discount_shop_no_text
        }
        ShoppingVisualKind.DailyGoods -> R.drawable.pattern_daily_goods
    }
}

@Composable
private fun MemoCardImage(
    memo: ShoppingMemo,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val customImageUri = memo.customImageUri
    if (customImageUri.isNullOrBlank()) {
        ShoppingPatternImage(pattern = memo.imagePattern, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, context, customImageUri) {
        value = loadCardImageBitmap(context, customImageUri)
    }
    val loadedBitmap = bitmap
    if (loadedBitmap != null) {
        Image(
            bitmap = loadedBitmap,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        ShoppingPatternImage(pattern = memo.imagePattern, modifier = modifier)
    }
}

private fun createCardImageCaptureUri(context: Context): Uri? {
    val imageFile = createCardImageFile(context) ?: return null
    return cardImageUriForFile(context, imageFile)
}

private fun importCardImageUri(context: Context, sourceUri: Uri): Uri? {
    val imageFile = createCardImageFile(context) ?: return null
    val copied = runCatching {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            imageFile.outputStream().use { output -> input.copyTo(output) }
        } != null
    }.getOrDefault(false)
    if (!copied) return null
    return cardImageUriForFile(context, imageFile)
}

private fun createCardImageFile(context: Context): File? {
    val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
    val imageDir = File(picturesDir, "card_images")
    if (!imageDir.exists() && !imageDir.mkdirs()) return null
    return File(imageDir, "card_${System.currentTimeMillis()}.jpg")
}

private fun cardImageUriForFile(context: Context, imageFile: File): Uri? {
    return runCatching {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }.getOrNull()
}

private fun loadCardImageBitmap(context: Context, uriString: String): ImageBitmap? {
    return runCatching {
        val uri = Uri.parse(uriString)
        val orientation = readImageExifOrientation(context, uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        val sampleSize = calculateBitmapSampleSize(bounds.outWidth, bounds.outHeight, maxSize = 1024)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: return@runCatching null
        applyExifOrientation(bitmap, orientation).asImageBitmap()
    }.getOrNull()
}

private fun readImageExifOrientation(context: Context, uri: Uri): Int {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
}

private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f)
            matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun calculateBitmapSampleSize(width: Int, height: Int, maxSize: Int): Int {
    if (width <= 0 || height <= 0) return 1
    var sampleSize = 1
    while (width / sampleSize > maxSize || height / sampleSize > maxSize) {
        sampleSize *= 2
    }
    return sampleSize
}

@Composable
private fun ShoppingPatternImage(pattern: Int, modifier: Modifier = Modifier) {
    val appLocale = LocalContext.current.appLocale()
    val patterns = activeShoppingVisualPatterns(appLocale)
    val item = patterns[((pattern % patterns.size) + patterns.size) % patterns.size]
    Box(
        modifier = modifier
            .background(item.background, RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(shoppingPatternImageResId(item.kind, appLocale)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
    return
    Box(
        modifier = modifier
            .background(item.background, RoundedCornerShape(0.dp))
            .padding(4.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            fun sx(v: Float) = w * v
            fun sy(v: Float) = h * v
            fun stroke(width: Float = 0.045f) = Stroke(width = u * width)

            when (item.kind) {
                ShoppingVisualKind.Food -> {
                    drawRoundRect(
                        color = Color(0xFFD7CCC8),
                        topLeft = Offset(sx(0.20f), sy(0.50f)),
                        size = Size(sx(0.60f), sy(0.24f)),
                        cornerRadius = CornerRadius(u * 0.08f, u * 0.08f),
                        style = stroke(0.045f)
                    )
                    drawLine(Color(0xFF8D6E63), Offset(sx(0.27f), sy(0.58f)), Offset(sx(0.73f), sy(0.58f)), strokeWidth = u * 0.03f)
                    drawCircle(Color(0xFFE53935), radius = u * 0.12f, center = Offset(sx(0.36f), sy(0.42f)))
                    drawCircle(Color(0xFF43A047), radius = u * 0.045f, center = Offset(sx(0.43f), sy(0.32f)))
                    drawOval(Color(0xFF1E88E5), topLeft = Offset(sx(0.50f), sy(0.34f)), size = Size(sx(0.25f), sy(0.14f)))
                    val tail = Path().apply {
                        moveTo(sx(0.73f), sy(0.41f))
                        lineTo(sx(0.84f), sy(0.32f))
                        lineTo(sx(0.84f), sy(0.50f))
                        close()
                    }
                    drawPath(tail, Color(0xFF1976D2))
                    if (appLocale.language == Locale.JAPANESE.language) {
                        drawOval(Color.White, topLeft = Offset(sx(0.43f), sy(0.47f)), size = Size(sx(0.22f), sy(0.13f)))
                        drawRoundRect(Color(0xFF90A4AE), Offset(sx(0.42f), sy(0.56f)), Size(sx(0.24f), sy(0.07f)), CornerRadius(u * 0.03f, u * 0.03f))
                    } else {
                        drawRoundRect(Color(0xFFFFB74D), Offset(sx(0.42f), sy(0.47f)), Size(sx(0.24f), sy(0.13f)), CornerRadius(u * 0.04f, u * 0.04f))
                    }
                }
                ShoppingVisualKind.Cart -> {
                    drawLine(item.accent, Offset(sx(0.24f), sy(0.35f)), Offset(sx(0.74f), sy(0.35f)), strokeWidth = u * 0.045f)
                    drawLine(item.accent, Offset(sx(0.30f), sy(0.35f)), Offset(sx(0.36f), sy(0.62f)), strokeWidth = u * 0.04f)
                    drawLine(item.accent, Offset(sx(0.74f), sy(0.35f)), Offset(sx(0.65f), sy(0.62f)), strokeWidth = u * 0.04f)
                    drawLine(item.accent, Offset(sx(0.34f), sy(0.62f)), Offset(sx(0.68f), sy(0.62f)), strokeWidth = u * 0.04f)
                    drawLine(item.accent, Offset(sx(0.72f), sy(0.35f)), Offset(sx(0.85f), sy(0.24f)), strokeWidth = u * 0.04f)
                    listOf(0.43f, 0.54f, 0.65f).forEach { x ->
                        drawLine(item.accent.copy(alpha = 0.65f), Offset(sx(x), sy(0.38f)), Offset(sx(x - 0.03f), sy(0.59f)), strokeWidth = u * 0.02f)
                    }
                    drawCircle(Color(0xFF455A64), radius = u * 0.055f, center = Offset(sx(0.40f), sy(0.73f)))
                    drawCircle(Color(0xFF455A64), radius = u * 0.055f, center = Offset(sx(0.66f), sy(0.73f)))
                    drawCircle(Color.White, radius = u * 0.027f, center = Offset(sx(0.40f), sy(0.73f)))
                    drawCircle(Color.White, radius = u * 0.027f, center = Offset(sx(0.66f), sy(0.73f)))
                }
                ShoppingVisualKind.Appliance -> {
                    drawRoundRect(Color.White, Offset(sx(0.30f), sy(0.18f)), Size(sx(0.42f), sy(0.66f)), CornerRadius(u * 0.06f, u * 0.06f))
                    drawRoundRect(item.accent, Offset(sx(0.30f), sy(0.18f)), Size(sx(0.42f), sy(0.66f)), CornerRadius(u * 0.06f, u * 0.06f), style = stroke(0.04f))
                    drawLine(item.accent, Offset(sx(0.31f), sy(0.43f)), Offset(sx(0.71f), sy(0.43f)), strokeWidth = u * 0.035f)
                    drawLine(item.accent, Offset(sx(0.62f), sy(0.28f)), Offset(sx(0.62f), sy(0.38f)), strokeWidth = u * 0.025f)
                    drawLine(item.accent, Offset(sx(0.62f), sy(0.52f)), Offset(sx(0.62f), sy(0.70f)), strokeWidth = u * 0.025f)
                }
                ShoppingVisualKind.Game -> {
                    drawRoundRect(Color(0xFF673AB7), Offset(sx(0.20f), sy(0.38f)), Size(sx(0.60f), sy(0.27f)), CornerRadius(u * 0.13f, u * 0.13f))
                    drawCircle(Color(0xFF512DA8), radius = u * 0.13f, center = Offset(sx(0.30f), sy(0.52f)))
                    drawCircle(Color(0xFF512DA8), radius = u * 0.13f, center = Offset(sx(0.70f), sy(0.52f)))
                    drawLine(Color.White, Offset(sx(0.27f), sy(0.52f)), Offset(sx(0.39f), sy(0.52f)), strokeWidth = u * 0.025f)
                    drawLine(Color.White, Offset(sx(0.33f), sy(0.46f)), Offset(sx(0.33f), sy(0.58f)), strokeWidth = u * 0.025f)
                    drawCircle(Color(0xFFFFEB3B), radius = u * 0.035f, center = Offset(sx(0.67f), sy(0.48f)))
                    drawCircle(Color(0xFF4CAF50), radius = u * 0.035f, center = Offset(sx(0.73f), sy(0.56f)))
                }
                ShoppingVisualKind.Restaurant -> {
                    drawOval(Color.White, Offset(sx(0.15f), sy(0.50f)), Size(sx(0.70f), sy(0.20f)))
                    drawOval(Color(0xFFFFCA28), Offset(sx(0.22f), sy(0.34f)), Size(sx(0.56f), sy(0.30f)))
                    drawLine(Color(0xFFE53935), Offset(sx(0.36f), sy(0.48f)), Offset(sx(0.65f), sy(0.42f)), strokeWidth = u * 0.045f)
                    drawCircle(Color(0xFFEF5350), radius = u * 0.03f, center = Offset(sx(0.50f), sy(0.45f)))
                }
                ShoppingVisualKind.Clothing -> {
                    val shirt = Path().apply {
                        moveTo(sx(0.30f), sy(0.30f))
                        lineTo(sx(0.43f), sy(0.22f))
                        lineTo(sx(0.50f), sy(0.32f))
                        lineTo(sx(0.57f), sy(0.22f))
                        lineTo(sx(0.70f), sy(0.30f))
                        lineTo(sx(0.78f), sy(0.47f))
                        lineTo(sx(0.65f), sy(0.53f))
                        lineTo(sx(0.65f), sy(0.78f))
                        lineTo(sx(0.35f), sy(0.78f))
                        lineTo(sx(0.35f), sy(0.53f))
                        lineTo(sx(0.22f), sy(0.47f))
                        close()
                    }
                    drawPath(shirt, item.accent)
                    drawRoundRect(Color.White.copy(alpha = 0.32f), Offset(sx(0.39f), sy(0.40f)), Size(sx(0.22f), sy(0.25f)), CornerRadius(u * 0.03f, u * 0.03f))
                }
                ShoppingVisualKind.Sports -> {
                    drawOval(Color(0xFFB2DFDB), Offset(sx(0.26f), sy(0.18f)), Size(sx(0.35f), sy(0.42f)), style = stroke(0.045f))
                    drawLine(item.accent, Offset(sx(0.52f), sy(0.55f)), Offset(sx(0.75f), sy(0.78f)), strokeWidth = u * 0.05f)
                    drawLine(Color.White.copy(alpha = 0.75f), Offset(sx(0.34f), sy(0.24f)), Offset(sx(0.51f), sy(0.53f)), strokeWidth = u * 0.013f)
                    drawLine(Color.White.copy(alpha = 0.75f), Offset(sx(0.28f), sy(0.38f)), Offset(sx(0.58f), sy(0.38f)), strokeWidth = u * 0.013f)
                    drawCircle(Color(0xFFFFF176), radius = u * 0.08f, center = Offset(sx(0.70f), sy(0.28f)))
                }
                ShoppingVisualKind.Service -> {
                    drawCircle(Color(0xFFFFCC80), radius = u * 0.12f, center = Offset(sx(0.50f), sy(0.28f)))
                    drawRoundRect(item.accent, Offset(sx(0.34f), sy(0.45f)), Size(sx(0.32f), sy(0.28f)), CornerRadius(u * 0.10f, u * 0.10f))
                    drawLine(Color(0xFF6D4C41), Offset(sx(0.38f), sy(0.38f)), Offset(sx(0.62f), sy(0.38f)), strokeWidth = u * 0.035f)
                    drawLine(item.accent, Offset(sx(0.35f), sy(0.53f)), Offset(sx(0.20f), sy(0.66f)), strokeWidth = u * 0.035f)
                    drawLine(item.accent, Offset(sx(0.65f), sy(0.53f)), Offset(sx(0.80f), sy(0.66f)), strokeWidth = u * 0.035f)
                }
                ShoppingVisualKind.HundredYenShop -> {
                    drawRoundRect(Color.White, Offset(sx(0.22f), sy(0.40f)), Size(sx(0.56f), sy(0.40f)), CornerRadius(u * 0.02f, u * 0.02f))
                    drawRoundRect(item.accent, Offset(sx(0.18f), sy(0.20f)), Size(sx(0.64f), sy(0.18f)), CornerRadius(u * 0.04f, u * 0.04f))
                    val stripeWidth = sx(0.64f) / 5f
                    repeat(5) { index ->
                        val color = if (index % 2 == 0) item.accent else Color.White
                        drawRoundRect(color, Offset(sx(0.18f) + stripeWidth * index, sy(0.36f)), Size(stripeWidth, sy(0.16f)), CornerRadius(u * 0.025f, u * 0.025f))
                    }
                    drawLine(Color(0xFF90CAF9), Offset(sx(0.32f), sy(0.53f)), Offset(sx(0.32f), sy(0.78f)), strokeWidth = u * 0.02f)
                    drawLine(Color(0xFF90CAF9), Offset(sx(0.68f), sy(0.53f)), Offset(sx(0.68f), sy(0.78f)), strokeWidth = u * 0.02f)
                    drawRoundRect(Color(0xFFBBDEFB), Offset(sx(0.34f), sy(0.54f)), Size(sx(0.32f), sy(0.25f)), CornerRadius(u * 0.02f, u * 0.02f))
                }
                ShoppingVisualKind.DiscountShop,
                ShoppingVisualKind.DailyGoods -> {
                    drawRoundRect(Color(0xFF4DD0E1), Offset(sx(0.34f), sy(0.32f)), Size(sx(0.32f), sy(0.44f)), CornerRadius(u * 0.08f, u * 0.08f))
                    drawRoundRect(Color(0xFF0097A7), Offset(sx(0.42f), sy(0.22f)), Size(sx(0.16f), sy(0.10f)), CornerRadius(u * 0.025f, u * 0.025f))
                    drawLine(Color(0xFF006064), Offset(sx(0.48f), sy(0.20f)), Offset(sx(0.68f), sy(0.20f)), strokeWidth = u * 0.035f)
                    drawOval(Color.White, Offset(sx(0.43f), sy(0.47f)), Size(sx(0.14f), sy(0.20f)))
                }
            }
        }
        if (item.kind == ShoppingVisualKind.HundredYenShop && appLocale.language == Locale.JAPANESE.language) {
            Text(
                text = "100",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            )
        }
    }
}

private val TitlePatternImageResIds = listOf(
    R.drawable.title_pattern_00,
    R.drawable.title_pattern_01,
    R.drawable.title_pattern_02,
    R.drawable.title_pattern_03,
    R.drawable.title_pattern_04,
    R.drawable.title_pattern_05,
    R.drawable.title_pattern_06,
    R.drawable.title_pattern_07,
    R.drawable.title_pattern_08,
    R.drawable.title_pattern_09,
    R.drawable.title_pattern_10,
    R.drawable.title_pattern_11,
    R.drawable.title_pattern_12,
    R.drawable.title_pattern_13,
    R.drawable.title_pattern_14
)

private fun titlePatternResId(pattern: Int): Int {
    return TitlePatternImageResIds[pattern.coerceIn(0, TitlePatternImageResIds.lastIndex)]
}

@Composable
private fun TitlePatternImage(
    pattern: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    val image = ImageBitmap.imageResource(titlePatternResId(pattern))
    Image(
        bitmap = image,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
private fun TitlePatternHeader(
    title: String,
    pattern: Int,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable BoxScope.() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeTitleHeaderHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                TitlePatternImage(
                    pattern = pattern,
                    modifier = Modifier.matchParentSize()
                )
                FittingSingleLineText(
                    text = title,
                    color = Color(0xFF3E2D22),
                    maxFontSize = 25.sp,
                    minFontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp)
                )
            }
            trailingContent?.invoke(this)
        }
        Divider(color = Color(0xFFE0E0E0))
    }
}

@Composable
private fun TitlePatternPickerScreen(
    selectedPattern: Int,
    oneHandModeEnabled: Boolean,
    onSelect: (Int) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var screenHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember { mutableStateOf(0f) }
    var oneHandFlingGeneration by remember { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (screenHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val latestGridAtTop by rememberUpdatedState(!gridState.canScrollBackward)

    LaunchedEffect(oneHandModeEnabled) {
        if (!oneHandModeEnabled) oneHandOffsetPx = 0f
    }

    fun startOneHandFling(initialVelocityY: Float) {
        if (oneHandMaxOffsetPx <= 0f || kotlin.math.abs(initialVelocityY) < 120f) return
        val generation = ++oneHandFlingGeneration
        scope.launch {
            var velocityY = initialVelocityY.coerceIn(-3200f, 3200f)
            var lastFrameNanos = withFrameNanos { it }
            while (generation == oneHandFlingGeneration && kotlin.math.abs(velocityY) > 30f) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastFrameNanos = frameNanos
                val nextOffset = (oneHandOffsetPx + velocityY * deltaSeconds).coerceIn(0f, oneHandMaxOffsetPx)
                if (nextOffset == oneHandOffsetPx) break
                oneHandOffsetPx = nextOffset
                velocityY *= 0.90f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenHeightPx = it.size.height }
    ) {
        if (oneHandModeEnabled && oneHandOffsetPx > 1f) {
            OneHandModeBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(oneHandBackdropHeight)
                    .graphicsLayer {
                        alpha = (0.42f + (oneHandOffsetPx / oneHandMaxOffsetPx.coerceAtLeast(1f)) * 0.46f)
                            .coerceIn(0.42f, 0.88f)
                    }
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = oneHandOffsetPx }
                .pointerInput(oneHandModeEnabled, oneHandMaxOffsetPx) {
                    if (!oneHandModeEnabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        oneHandFlingGeneration++
                        val gridAtTopWhenGestureStarted = latestGridAtTop
                        var totalX = 0f
                        var totalY = 0f
                        var lastVelocityY = 0f
                        var movedContentInGesture = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            val verticalGesture = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.15f
                            val canPullDown = delta.y > 0f && oneHandOffsetPx < oneHandMaxOffsetPx && gridAtTopWhenGestureStarted
                            val canPushUp = delta.y < 0f && oneHandOffsetPx > 0f
                            if (verticalGesture && (canPullDown || canPushUp)) {
                                val boostedDeltaY = delta.y * OneHandScrollSpeedMultiplier
                                val nextOffset = (oneHandOffsetPx + boostedDeltaY).coerceIn(0f, oneHandMaxOffsetPx)
                                if (nextOffset != oneHandOffsetPx) {
                                    oneHandOffsetPx = nextOffset
                                    lastVelocityY = boostedDeltaY * 60f
                                    movedContentInGesture = true
                                    change.consume()
                                }
                            }
                        }
                        if (movedContentInGesture) startOneHandFling(lastVelocityY)
                    }
                },
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(TitlePatternImageResIds.indices.toList(), key = { it }) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp)
                        .clickable { onSelect(index) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (selectedPattern == index) 3.dp else 1.dp,
                        color = if (selectedPattern == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        TitlePatternImage(
                            pattern = index,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = stringResource(R.string.shopping_memo_title),
                            color = Color(0xFF3E2D22),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternPickerScreen(
    memo: ShoppingMemo,
    oneHandModeEnabled: Boolean,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val appLocale = LocalContext.current.appLocale()
    val patterns = activeShoppingVisualPatterns(appLocale)
    PatternPickerGridScreen(
        memo = memo,
        patterns = patterns,
        oneHandModeEnabled = oneHandModeEnabled,
        onSelect = onSelect
    )
    return
    Column(Modifier.fillMaxSize()) {
        Header(
            title = stringResource(R.string.image_pattern_title),
            trailing = {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.action_back), color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                }
            }
        )
        Text(
            text = localizedMemoDisplayTitle(memo, R.string.card),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = Color(0xFF555555),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(patterns.indices.toList(), key = { it }) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .clickable { onSelect(index) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (memo.imagePattern == index) 3.dp else 1.dp,
                        color = if (memo.imagePattern == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ShoppingPatternImage(pattern = index, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadableShoppingPatternImage(
    item: ShoppingVisualPattern,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(item.background, RoundedCornerShape(0.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        when (item.kind) {
            ShoppingVisualKind.Food -> GroceryBasketPatternIcon(item.accent, Modifier.fillMaxSize())
            ShoppingVisualKind.Cart -> Text("🛒", fontSize = 72.sp)
            ShoppingVisualKind.Appliance -> TvPatternIcon(item.accent, Modifier.fillMaxSize())
            ShoppingVisualKind.Game -> Text("🎮", fontSize = 72.sp)
            ShoppingVisualKind.Restaurant -> OmuricePatternIcon(item.accent, Modifier.fillMaxSize())
            ShoppingVisualKind.Clothing -> Text("👕", fontSize = 72.sp)
            ShoppingVisualKind.Sports -> Text("🎾", fontSize = 72.sp)
            ShoppingVisualKind.Service -> ServiceStaffPatternIcon(item.accent, Modifier.fillMaxSize())
            ShoppingVisualKind.HundredYenShop -> HundredYenShopStorePatternIcon(Modifier.fillMaxSize())
            ShoppingVisualKind.DiscountShop -> HundredYenShopStorePatternIcon(Modifier.fillMaxSize())
            ShoppingVisualKind.DailyGoods -> Text("🧴", fontSize = 72.sp)
        }
    }
}

@Composable
private fun GroceryBasketPatternIcon(accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            val basket = Color(0xFF8D6E63)
            drawRoundRect(
                color = basket,
                topLeft = Offset(w * 0.16f, h * 0.50f),
                size = Size(w * 0.68f, h * 0.28f),
                cornerRadius = CornerRadius(u * 0.08f, u * 0.08f),
                style = Stroke(width = u * 0.055f)
            )
            drawLine(basket, Offset(w * 0.25f, h * 0.49f), Offset(w * 0.39f, h * 0.28f), strokeWidth = u * 0.035f)
            drawLine(basket, Offset(w * 0.75f, h * 0.49f), Offset(w * 0.61f, h * 0.28f), strokeWidth = u * 0.035f)
            repeat(4) { index ->
                val x = w * (0.26f + index * 0.14f)
                drawLine(Color(0xFFA1887F), Offset(x, h * 0.53f), Offset(x + w * 0.08f, h * 0.75f), strokeWidth = u * 0.02f)
            }
            drawLine(Color(0xFFA1887F), Offset(w * 0.22f, h * 0.62f), Offset(w * 0.78f, h * 0.62f), strokeWidth = u * 0.02f)
            drawLine(accent.copy(alpha = 0.65f), Offset(w * 0.23f, h * 0.50f), Offset(w * 0.77f, h * 0.50f), strokeWidth = u * 0.035f)
        }
        Text("🍞", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 14.dp))
        Text("🍎", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 17.dp, top = 15.dp))
        Text("🎃", fontSize = 34.sp, modifier = Modifier.align(Alignment.Center).padding(top = 2.dp))
    }
}

@Composable
private fun TvPatternIcon(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val u = kotlin.math.min(w, h)
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(w * 0.16f, h * 0.22f),
            size = Size(w * 0.68f, h * 0.44f),
            cornerRadius = CornerRadius(u * 0.055f, u * 0.055f)
        )
        drawRoundRect(
            color = Color(0xFF90CAF9),
            topLeft = Offset(w * 0.21f, h * 0.27f),
            size = Size(w * 0.58f, h * 0.34f),
            cornerRadius = CornerRadius(u * 0.035f, u * 0.035f)
        )
        drawLine(accent, Offset(w * 0.50f, h * 0.66f), Offset(w * 0.50f, h * 0.76f), strokeWidth = u * 0.045f)
        drawRoundRect(
            color = accent,
            topLeft = Offset(w * 0.31f, h * 0.76f),
            size = Size(w * 0.38f, h * 0.07f),
            cornerRadius = CornerRadius(u * 0.025f, u * 0.025f)
        )
        drawLine(Color.White.copy(alpha = 0.65f), Offset(w * 0.28f, h * 0.32f), Offset(w * 0.48f, h * 0.52f), strokeWidth = u * 0.018f)
    }
}

@Composable
private fun OmuricePatternIcon(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val u = kotlin.math.min(w, h)
        drawOval(Color.White, topLeft = Offset(w * 0.14f, h * 0.50f), size = Size(w * 0.72f, h * 0.24f))
        drawOval(Color(0xFFFFCA28), topLeft = Offset(w * 0.22f, h * 0.31f), size = Size(w * 0.56f, h * 0.34f))
        drawLine(Color(0xFFE53935), Offset(w * 0.34f, h * 0.48f), Offset(w * 0.66f, h * 0.40f), strokeWidth = u * 0.055f)
        drawCircle(Color(0xFFE53935), radius = u * 0.025f, center = Offset(w * 0.50f, h * 0.44f))
        drawLine(accent, Offset(w * 0.22f, h * 0.77f), Offset(w * 0.48f, h * 0.58f), strokeWidth = u * 0.035f)
        drawCircle(Color(0xFFBDBDBD), radius = u * 0.055f, center = Offset(w * 0.20f, h * 0.79f))
    }
}

@Composable
private fun ServiceStaffPatternIcon(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val u = kotlin.math.min(w, h)
        val skin = Color(0xFFFFCC80)
        val hair = Color(0xFF6D4C41)
        drawCircle(skin, radius = u * 0.13f, center = Offset(w * 0.50f, h * 0.24f))
        drawOval(hair, topLeft = Offset(w * 0.37f, h * 0.12f), size = Size(w * 0.26f, h * 0.14f))
        drawCircle(Color.Black, radius = u * 0.012f, center = Offset(w * 0.46f, h * 0.24f))
        drawCircle(Color.Black, radius = u * 0.012f, center = Offset(w * 0.54f, h * 0.24f))
        drawLine(Color(0xFFD84315), Offset(w * 0.46f, h * 0.31f), Offset(w * 0.54f, h * 0.31f), strokeWidth = u * 0.018f)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.34f, h * 0.40f),
            size = Size(w * 0.32f, h * 0.38f),
            cornerRadius = CornerRadius(u * 0.09f, u * 0.09f)
        )
        drawRoundRect(
            color = accent,
            topLeft = Offset(w * 0.39f, h * 0.48f),
            size = Size(w * 0.22f, h * 0.28f),
            cornerRadius = CornerRadius(u * 0.04f, u * 0.04f)
        )
        drawLine(accent, Offset(w * 0.38f, h * 0.48f), Offset(w * 0.23f, h * 0.60f), strokeWidth = u * 0.04f)
        drawLine(accent, Offset(w * 0.62f, h * 0.48f), Offset(w * 0.77f, h * 0.60f), strokeWidth = u * 0.04f)
        drawLine(Color(0xFF455A64), Offset(w * 0.43f, h * 0.78f), Offset(w * 0.40f, h * 0.88f), strokeWidth = u * 0.04f)
        drawLine(Color(0xFF455A64), Offset(w * 0.57f, h * 0.78f), Offset(w * 0.60f, h * 0.88f), strokeWidth = u * 0.04f)
    }
}

@Composable
private fun HundredYenShopStorePatternIcon(modifier: Modifier = Modifier) {
    val appLocale = LocalContext.current.appLocale()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            val red = Color(0xFFE53935)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.14f, h * 0.36f),
                size = Size(w * 0.72f, h * 0.48f),
                cornerRadius = CornerRadius(u * 0.025f, u * 0.025f)
            )
            drawRoundRect(
                color = Color(0xFFBBDEFB),
                topLeft = Offset(w * 0.23f, h * 0.56f),
                size = Size(w * 0.18f, h * 0.22f),
                cornerRadius = CornerRadius(u * 0.02f, u * 0.02f)
            )
            drawRoundRect(
                color = Color(0xFFBBDEFB),
                topLeft = Offset(w * 0.59f, h * 0.56f),
                size = Size(w * 0.18f, h * 0.22f),
                cornerRadius = CornerRadius(u * 0.02f, u * 0.02f)
            )
            drawRoundRect(
                color = Color(0xFF90CAF9),
                topLeft = Offset(w * 0.43f, h * 0.55f),
                size = Size(w * 0.14f, h * 0.29f),
                cornerRadius = CornerRadius(u * 0.015f, u * 0.015f)
            )
            drawLine(Color.White, Offset(w * 0.50f, h * 0.55f), Offset(w * 0.50f, h * 0.84f), strokeWidth = u * 0.012f)
            drawRoundRect(
                color = red,
                topLeft = Offset(w * 0.10f, h * 0.13f),
                size = Size(w * 0.80f, h * 0.24f),
                cornerRadius = CornerRadius(u * 0.035f, u * 0.035f)
            )
            val stripeWidth = w * 0.80f / 6f
            repeat(6) { index ->
                val color = if (index % 2 == 0) red else Color.White
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.10f + stripeWidth * index, h * 0.36f),
                    size = Size(stripeWidth, h * 0.16f),
                    cornerRadius = CornerRadius(u * 0.025f, u * 0.025f)
                )
            }
            drawLine(Color(0xFFBDBDBD), Offset(w * 0.14f, h * 0.84f), Offset(w * 0.86f, h * 0.84f), strokeWidth = u * 0.025f)
        }
        if (appLocale.language == Locale.JAPANESE.language) {
            Text(
                text = stringResource(R.string.pattern_hundred_yen_shop),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 15.dp)
            )
        }
    }
}

@Composable
private fun FoodBasketPatternIcon(accent: Color, modifier: Modifier = Modifier) {
    val appLocale = LocalContext.current.appLocale()
    val riceOrBread = if (appLocale.language == Locale.JAPANESE.language) "🍚" else "🍞"
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(w * 0.18f, h * 0.48f),
                size = Size(w * 0.64f, h * 0.28f),
                cornerRadius = CornerRadius(u * 0.10f, u * 0.10f),
                style = Stroke(width = u * 0.045f)
            )
            repeat(4) { index ->
                val x = w * (0.28f + index * 0.14f)
                drawLine(Color(0xFFA1887F), Offset(x, h * 0.50f), Offset(x + w * 0.05f, h * 0.72f), strokeWidth = u * 0.018f)
            }
            drawLine(Color(0xFFA1887F), Offset(w * 0.24f, h * 0.60f), Offset(w * 0.76f, h * 0.60f), strokeWidth = u * 0.018f)
            drawLine(accent.copy(alpha = 0.65f), Offset(w * 0.30f, h * 0.42f), Offset(w * 0.70f, h * 0.42f), strokeWidth = u * 0.035f)
        }
        Text("🍎", fontSize = 30.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 16.dp))
        Text("🐟", fontSize = 30.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 18.dp))
        Text(riceOrBread, fontSize = 30.sp, modifier = Modifier.align(Alignment.Center).padding(top = 4.dp))
    }
}

@Composable
private fun FridgePatternIcon(accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.30f, h * 0.14f),
                size = Size(w * 0.40f, h * 0.72f),
                cornerRadius = CornerRadius(u * 0.06f, u * 0.06f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.30f, h * 0.14f),
                size = Size(w * 0.40f, h * 0.72f),
                cornerRadius = CornerRadius(u * 0.06f, u * 0.06f),
                style = Stroke(width = u * 0.045f)
            )
            drawLine(accent, Offset(w * 0.32f, h * 0.42f), Offset(w * 0.68f, h * 0.42f), strokeWidth = u * 0.035f)
            drawLine(Color(0xFF455A64), Offset(w * 0.61f, h * 0.24f), Offset(w * 0.61f, h * 0.36f), strokeWidth = u * 0.035f)
            drawLine(Color(0xFF455A64), Offset(w * 0.61f, h * 0.52f), Offset(w * 0.61f, h * 0.72f), strokeWidth = u * 0.035f)
        }
    }
}

@Composable
private fun HundredYenShopPatternIcon(modifier: Modifier = Modifier) {
    val appLocale = LocalContext.current.appLocale()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val u = kotlin.math.min(w, h)
            val red = Color(0xFFE53935)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.18f, h * 0.38f),
                size = Size(w * 0.64f, h * 0.44f),
                cornerRadius = CornerRadius(u * 0.025f, u * 0.025f)
            )
            drawRoundRect(
                color = Color(0xFF90CAF9),
                topLeft = Offset(w * 0.32f, h * 0.56f),
                size = Size(w * 0.36f, h * 0.24f),
                cornerRadius = CornerRadius(u * 0.02f, u * 0.02f)
            )
            drawLine(Color.White, Offset(w * 0.50f, h * 0.56f), Offset(w * 0.50f, h * 0.80f), strokeWidth = u * 0.015f)
            drawRoundRect(
                color = red,
                topLeft = Offset(w * 0.14f, h * 0.16f),
                size = Size(w * 0.72f, h * 0.23f),
                cornerRadius = CornerRadius(u * 0.04f, u * 0.04f)
            )
            val stripeWidth = w * 0.72f / 5f
            repeat(5) { index ->
                val color = if (index % 2 == 0) red else Color.White
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.14f + stripeWidth * index, h * 0.36f),
                    size = Size(stripeWidth, h * 0.16f),
                    cornerRadius = CornerRadius(u * 0.025f, u * 0.025f)
                )
            }
        }
        if (appLocale.language == Locale.JAPANESE.language) {
            Text(
                text = stringResource(R.string.pattern_hundred_yen_shop),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 17.dp)
            )
        }
    }
}

@Composable
private fun PatternPickerGridScreen(
    memo: ShoppingMemo,
    patterns: List<ShoppingVisualPattern>,
    oneHandModeEnabled: Boolean,
    onSelect: (Int) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var screenHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember { mutableStateOf(0f) }
    var oneHandFlingGeneration by remember { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (screenHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val latestGridAtTop by rememberUpdatedState(!gridState.canScrollBackward)

    LaunchedEffect(oneHandModeEnabled) {
        if (!oneHandModeEnabled) oneHandOffsetPx = 0f
    }

    fun startOneHandFling(initialVelocityY: Float) {
        if (oneHandMaxOffsetPx <= 0f || kotlin.math.abs(initialVelocityY) < 120f) return
        val generation = ++oneHandFlingGeneration
        scope.launch {
            var velocityY = initialVelocityY.coerceIn(-3200f, 3200f)
            var lastFrameNanos = withFrameNanos { it }
            while (generation == oneHandFlingGeneration && kotlin.math.abs(velocityY) > 30f) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastFrameNanos = frameNanos
                val nextOffset = (oneHandOffsetPx + velocityY * deltaSeconds).coerceIn(0f, oneHandMaxOffsetPx)
                if (nextOffset == oneHandOffsetPx) break
                oneHandOffsetPx = nextOffset
                velocityY *= 0.90f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenHeightPx = it.size.height }
    ) {
        if (oneHandModeEnabled && oneHandOffsetPx > 1f) {
            OneHandModeBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(oneHandBackdropHeight)
                    .graphicsLayer {
                        alpha = (0.42f + (oneHandOffsetPx / oneHandMaxOffsetPx.coerceAtLeast(1f)) * 0.46f)
                            .coerceIn(0.42f, 0.88f)
                    }
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = oneHandOffsetPx }
                .pointerInput(oneHandModeEnabled, oneHandMaxOffsetPx) {
                    if (!oneHandModeEnabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        oneHandFlingGeneration++
                        val gridAtTopWhenGestureStarted = latestGridAtTop
                        var totalX = 0f
                        var totalY = 0f
                        var lastVelocityY = 0f
                        var movedContentInGesture = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            val verticalGesture = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.15f
                            val canPullDown = delta.y > 0f && oneHandOffsetPx < oneHandMaxOffsetPx && gridAtTopWhenGestureStarted
                            val canPushUp = delta.y < 0f && oneHandOffsetPx > 0f
                            if (verticalGesture && (canPullDown || canPushUp)) {
                                val boostedDeltaY = delta.y * OneHandScrollSpeedMultiplier
                                val nextOffset = (oneHandOffsetPx + boostedDeltaY).coerceIn(0f, oneHandMaxOffsetPx)
                                if (nextOffset != oneHandOffsetPx) {
                                    oneHandOffsetPx = nextOffset
                                    lastVelocityY = boostedDeltaY * 60f
                                    movedContentInGesture = true
                                    change.consume()
                                }
                            }
                        }
                        if (movedContentInGesture) startOneHandFling(lastVelocityY)
                    }
                },
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(patterns.indices.toList(), key = { it }) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clickable { onSelect(index) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (memo.imagePattern == index) 3.dp else 1.dp,
                        color = if (memo.imagePattern == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ShoppingPatternImage(
                        pattern = index,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OneHandModeBackdrop(modifier: Modifier = Modifier) {
    val appLocale = LocalContext.current.appLocale()
    val backdropResId = when (groceryVisualRegion(appLocale)) {
        GroceryVisualRegion.Japan -> R.drawable.one_hand_food_pattern_a
        GroceryVisualRegion.Asia -> R.drawable.one_hand_food_pattern_asia
        GroceryVisualRegion.Europe -> R.drawable.one_hand_food_pattern_europe
    }
    Box(
        modifier = modifier
            .background(Color(0xFFFAFCFF))
    ) {
        Image(
            painter = painterResource(backdropResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MemoDetailScreen(
    memo: ShoppingMemo,
    oneHandModeEnabled: Boolean,
    leftHandModeEnabled: Boolean,
    microphoneEnabled: Boolean,
    microphoneSettings: MicrophoneSettings,
    microphoneSessionActive: Boolean,
    onMicrophoneSessionActiveChange: (Boolean) -> Unit,
    keepCompletedItemsInPlace: Boolean,
    titlePattern: Int? = null,
    onTitlePatternClick: (() -> Unit)? = null,
    recentlyMovedEntryIds: Set<String>,
    onClearRecentlyMovedEntryHighlight: (String) -> Unit,
    onFinish: () -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var detailContainerBounds by remember { mutableStateOf<Rect?>(null) }
    val imeBottom = with(density) { WindowInsets.ime.getBottom(this).toDp() }
    val imeVisible = imeBottom > 0.dp
    val fabBottomPadding = keyboardAnchoredFabBottomPadding(
        view = view,
        containerBounds = detailContainerBounds,
        imeVisible = imeVisible,
        density = density
    )
    val listBottomPadding = if (imeVisible) {
        (imeBottom - BottomBarHeight + DetailListExtraBottom).coerceAtLeast(DetailListExtraBottom)
    } else {
        DetailListExtraBottom
    }
    val tabs = listOf(stringResource(R.string.tab_items), stringResource(R.string.tab_trash))
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var voiceEditingEntryId by remember(memo.id) { mutableStateOf<String?>(null) }
    val recentlyRestoredEntryIds = remember(memo.id) { mutableStateListOf<String>() }
    val entryCursorRanges = remember(memo.id) { mutableStateMapOf<String, TextRange>() }
    var voiceTarget by remember { mutableStateOf(if (memo.title.isBlank()) VoiceTarget.Title else VoiceTarget.Item) }
    var titleFocused by remember(memo.id) { mutableStateOf(false) }
    var focusedItemRequestId by remember { mutableStateOf<String?>(null) }
    var restoredScrollTargetEntryId by remember(memo.id) { mutableStateOf<String?>(null) }
    var restoredScrollSerial by remember(memo.id) { mutableStateOf(0) }
    var addButtonScrollRequest by remember { mutableStateOf(0) }
    var itemDragActive by remember { mutableStateOf(false) }
    var activeListAtTop by remember(memo.id) { mutableStateOf(true) }
    var deletedListAtTop by remember(memo.id) { mutableStateOf(true) }
    var detailHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember(memo.id) { mutableStateOf(0f) }
    var oneHandMoveSerial by remember(memo.id) { mutableStateOf(0) }
    var oneHandFlingGeneration by remember(memo.id) { mutableStateOf(0) }
    var detailVoiceScrollDirection by remember(memo.id) { mutableStateOf(0) }
    var detailVoiceScrollSerial by remember(memo.id) { mutableStateOf(0) }
    var activeItemJumpDirection by remember(memo.id) { mutableStateOf(0) }
    var activeItemJumpSerial by remember(memo.id) { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (detailHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val currentListAtTop = if (pagerState.currentPage == 0) activeListAtTop else deletedListAtTop
    val latestCurrentListAtTop by rememberUpdatedState(currentListAtTop)
    val activeItemCount = memo.entries.count {
        it.name.isNotBlank() && (keepCompletedItemsInPlace || !it.checked)
    }
    val titleVoiceHighlighted = microphoneEnabled &&
        microphoneSessionActive &&
        (voiceTarget == VoiceTarget.Title || memo.title.isBlank())

    fun editableEntry(): ShoppingEntry {
        return requestBlankEntry(memo, keepCompletedItemsInPlace)
    }

    fun moveToItemInput() {
        val entry = editableEntry()
        selectedEntryId = entry.id
        focusedItemRequestId = entry.id
        voiceTarget = VoiceTarget.Item
    }

    fun stopDetailVoiceScroll() {
        detailVoiceScrollDirection = 0
        detailVoiceScrollSerial++
    }

    fun startDetailVoiceScroll(direction: Int) {
        detailVoiceScrollDirection = direction
        detailVoiceScrollSerial++
    }

    fun clearDetailItemEditing(hideKeyboard: Boolean = false) {
        focusManager.clearFocus(force = true)
        if (hideKeyboard) {
            keyboardController?.hide()
        }
        selectedEntryId = null
        voiceEditingEntryId = null
        focusedItemRequestId = null
    }

    fun clearDetailTransientHighlight(entryId: String) {
        recentlyRestoredEntryIds.remove(entryId)
        onClearRecentlyMovedEntryHighlight(entryId)
    }

    fun requestActiveItemJump(direction: Int) {
        clearDetailItemEditing(hideKeyboard = true)
        activeItemJumpDirection = direction
        activeItemJumpSerial++
    }

    fun focusItemByVoiceNumber(number: Int) {
        val entry = memo.entries
            .filter { it.name.isNotBlank() && (keepCompletedItemsInPlace || !it.checked) }
            .getOrNull(number - 1) ?: return
        stopDetailVoiceScroll()
        selectedEntryId = entry.id
        voiceEditingEntryId = entry.id
        entryCursorRanges[entry.id] = TextRange(entry.name.length)
        focusedItemRequestId = entry.id
        voiceTarget = VoiceTarget.Item
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    fun insertVoiceTextIntoEntry(entry: ShoppingEntry, text: String) {
        val range = coerceTextRange(entryCursorRanges[entry.id] ?: TextRange(entry.name.length), entry.name.length)
        val start = minOf(range.start, range.end)
        val end = maxOf(range.start, range.end)
        entry.name = entry.name.substring(0, start) + text + entry.name.substring(end)
        val cursor = start + text.length
        entryCursorRanges[entry.id] = TextRange(cursor)
        voiceTarget = VoiceTarget.Item
        ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
        clearDetailItemEditing(hideKeyboard = true)
    }

    fun handleVoiceText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isBlank()) return

        if (pagerState.currentPage == 1) {
            return
        }

        if (voiceTarget == VoiceTarget.Title || memo.title.isBlank()) {
            memo.title = cleaned
            editableEntry()
            clearDetailItemEditing(hideKeyboard = true)
            focusedItemRequestId = null
            voiceTarget = VoiceTarget.Item
        } else {
            val targetEntryId = selectedEntryId ?: voiceEditingEntryId
            val selectedEntry = memo.entries.firstOrNull {
                it.id == targetEntryId && (!it.checked || keepCompletedItemsInPlace)
            }
            if (selectedEntry != null) {
                insertVoiceTextIntoEntry(selectedEntry, cleaned)
            } else {
            val entry = memo.entries.firstOrNull { it.id == selectedEntryId && it.name.isBlank() }
                ?: memo.entries.firstOrNull { it.name.isBlank() }
                ?: ShoppingEntry(name = "").also { memo.entries.add(it) }
                insertVoiceTextIntoEntry(entry, cleaned)
                addButtonScrollRequest++
            }
            voiceTarget = VoiceTarget.Item
        }
        onChanged()
    }

    val latestDetailVoiceHandler by rememberUpdatedState(::handleVoiceText)
    val speechController = remember(memo.id) {
        ContinuousSpeechController(context) { latestDetailVoiceHandler(it) }
    }
    DisposableEffect(speechController) {
        onDispose { speechController.destroy() }
    }

    var microphonePermissionRequested by remember(memo.id) { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        microphonePermissionRequested = false
        if (granted) {
            keyboardController?.hide()
            speechController.start()
            onMicrophoneSessionActiveChange(true)
        } else {
            onMicrophoneSessionActiveChange(false)
        }
    }
    fun requestDetailMicrophonePermission() {
        if (microphonePermissionRequested) return
        microphonePermissionRequested = true
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(memo.id, titlePattern) {
        if (memo.title.isBlank()) {
            titleFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(microphoneEnabled, microphoneSessionActive, pagerState.currentPage, memo.id) {
        if (!microphoneEnabled || !microphoneSessionActive || pagerState.currentPage != 0) {
            speechController.stop()
            stopDetailVoiceScroll()
            if (microphoneSessionActive && pagerState.currentPage != 0) {
                onMicrophoneSessionActiveChange(false)
            }
            return@LaunchedEffect
        }
        if (!speechController.isRunning) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                keyboardController?.hide()
                speechController.start()
            } else if (!microphonePermissionRequested) {
                requestDetailMicrophonePermission()
            }
        }
    }

    LaunchedEffect(microphoneEnabled, microphoneSessionActive, selectedEntryId, voiceEditingEntryId) {
        if (microphoneSessionActive || !microphoneEnabled) return@LaunchedEffect
        val targetId = selectedEntryId ?: voiceEditingEntryId ?: return@LaunchedEffect
        selectedEntryId = targetId
        focusedItemRequestId = targetId
        withFrameNanos { }
        delay(80)
        keyboardController?.show()
    }

    LaunchedEffect(oneHandModeEnabled, memo.id) {
        if (!oneHandModeEnabled) {
            oneHandOffsetPx = 0f
        }
    }

    fun startOneHandFling(initialVelocityY: Float) {
        if (oneHandMaxOffsetPx <= 0f || kotlin.math.abs(initialVelocityY) < 120f) return
        val generation = ++oneHandFlingGeneration
        oneHandMoveSerial++
        scope.launch {
            var velocityY = initialVelocityY.coerceIn(-3200f, 3200f)
            var lastFrameNanos = withFrameNanos { it }
            while (generation == oneHandFlingGeneration && kotlin.math.abs(velocityY) > 30f) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastFrameNanos = frameNanos
                val nextOffset = (oneHandOffsetPx + velocityY * deltaSeconds).coerceIn(0f, oneHandMaxOffsetPx)
                if (nextOffset == oneHandOffsetPx) break
                oneHandOffsetPx = nextOffset
                velocityY *= 0.90f
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { detailContainerBounds = it.boundsInWindow() }
            .pointerInput(pagerState.currentPage, itemDragActive) {
                if (itemDragActive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalX = 0f
                    var totalY = 0f
                    var verticalScrollDetected = false
                    var activePointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == activePointerId } ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange()
                        totalX += delta.x
                        totalY += delta.y
                        if (
                            pagerState.currentPage == 0 &&
                            totalX > 0f &&
                            kotlin.math.abs(totalY) > viewConfiguration.touchSlop &&
                            kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 0.75f
                        ) {
                            verticalScrollDetected = true
                        }
                        if (kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f) {
                            if (pagerState.currentPage == 0 && totalX < -DetailBackSwipeThresholdPx) {
                                change.consume()
                                scope.launch { pagerState.animateScrollToPage(1) }
                                break
                            } else if (pagerState.currentPage == 0 && totalX > DetailBackSwipeThresholdPx) {
                                continue
                            } else if (pagerState.currentPage == 1 && totalX > DetailBackSwipeThresholdPx) {
                                change.consume()
                                scope.launch { pagerState.animateScrollToPage(0) }
                                break
                            }
                        }
                    }
                    if (
                        pagerState.currentPage == 0 &&
                        totalX > DetailBackSwipeThresholdPx &&
                        !verticalScrollDetected &&
                        kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f
                    ) {
                        onFinish()
                    }
                }
            }
    ) {
        if (oneHandModeEnabled && oneHandOffsetPx > 1f) {
            OneHandModeBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(oneHandBackdropHeight)
                    .graphicsLayer {
                        alpha = (0.42f + (oneHandOffsetPx / oneHandMaxOffsetPx.coerceAtLeast(1f)) * 0.46f)
                            .coerceIn(0.42f, 0.88f)
                    }
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { detailHeightPx = it.size.height }
                .graphicsLayer { translationY = oneHandOffsetPx }
                .pointerInput(oneHandModeEnabled, oneHandMaxOffsetPx, itemDragActive) {
                    if (!oneHandModeEnabled || itemDragActive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        oneHandFlingGeneration++
                        val listAtTopWhenGestureStarted = latestCurrentListAtTop
                        var totalX = 0f
                        var totalY = 0f
                        var lastVelocityY = 0f
                        var movedContentInGesture = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            val verticalGesture = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.15f
                            val canPullDown = delta.y > 0f && oneHandOffsetPx < oneHandMaxOffsetPx && listAtTopWhenGestureStarted
                            val canPushUp = delta.y < 0f && oneHandOffsetPx > 0f
                            if (verticalGesture && (canPullDown || canPushUp)) {
                                val boostedDeltaY = delta.y * OneHandScrollSpeedMultiplier
                                val nextOffset = (oneHandOffsetPx + boostedDeltaY).coerceIn(0f, oneHandMaxOffsetPx)
                                if (nextOffset != oneHandOffsetPx) {
                                    oneHandOffsetPx = nextOffset
                                    lastVelocityY = boostedDeltaY * 60f
                                    if (!movedContentInGesture) {
                                        oneHandMoveSerial++
                                        movedContentInGesture = true
                                    }
                                    change.consume()
                                }
                            }
                        }
                        if (movedContentInGesture) {
                            startOneHandFling(lastVelocityY)
                        }
                    }
                }
        ) {
            DetailTitleArea(
                memo = memo,
                titleFocusRequester = titleFocusRequester,
                titlePattern = titlePattern,
                onTitlePatternClick = onTitlePatternClick,
                showEraseButton = pagerState.currentPage == 1 && memo.deletedEntries.isNotEmpty(),
                onErase = {
                    memo.deletedEntries.clear()
                    onChanged()
                },
                microphoneHighlighted = titleVoiceHighlighted,
                editingHighlighted = titleFocused,
                onTitleFocused = {
                    selectedEntryId = null
                    voiceEditingEntryId = null
                    focusedItemRequestId = null
                    titleFocused = true
                    speechController.stop()
                    voiceTarget = VoiceTarget.Title
                },
                onTitleFocusCleared = {
                    titleFocused = false
                },
                onChanged = onChanged
            )
            Divider(color = Color(0xFFE0E0E0))
            Row(modifier = Modifier.fillMaxWidth().height(HomeProgressBarHeight)) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    val selectedTrashTab = selected && index == 1
                    Column(
                        modifier = Modifier
                            .weight(if (index == 0) 2f else 1f)
                            .fillMaxHeight()
                            .background(if (selectedTrashTab) TrashTabSelectedColor else Color.White)
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                selectedTrashTab -> Color.White
                                selected -> Color(0xFF1976D2)
                                else -> Color(0xFF777777)
                            }
                        )
                        Spacer(Modifier.height(9.dp))
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .fillMaxWidth()
                                .background(
                                    when {
                                        selectedTrashTab -> Color.White
                                        selected -> Color(0xFF1565C0)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                if (page == 0) {
                    ActiveItemsPage(
                        memo = memo,
                        selectedEntryId = selectedEntryId,
                        focusedItemRequestId = focusedItemRequestId,
                        addButtonScrollRequest = addButtonScrollRequest,
                        voiceScrollDirection = if (pagerState.currentPage == 0) detailVoiceScrollDirection else 0,
                        voiceScrollSerial = detailVoiceScrollSerial,
                        onVoiceScrollFinished = { stopDetailVoiceScroll() },
                        activeItemJumpDirection = activeItemJumpDirection,
                        activeItemJumpSerial = activeItemJumpSerial,
                        highlightedEntryId = if (microphoneEnabled && microphoneSessionActive && !titleVoiceHighlighted) {
                            selectedEntryId ?: voiceEditingEntryId
                        } else {
                            null
                        },
                        highlightAddListItem = microphoneEnabled &&
                            microphoneSessionActive &&
                            !titleVoiceHighlighted &&
                            (selectedEntryId ?: voiceEditingEntryId) == null,
                        keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                        onFocusedItemConsumed = { focusedItemRequestId = null },
                        onSelect = { selectedEntryId = it },
                        onRequestFocus = { focusedItemRequestId = it },
                        onEditingDismissed = { clearDetailItemEditing(hideKeyboard = true) },
                        entryCursorRanges = entryCursorRanges,
                        onEntryCursorChanged = { entryId, range -> entryCursorRanges[entryId] = range },
                        onEntryFocused = { entryId ->
                            selectedEntryId = entryId
                            voiceEditingEntryId = entryId
                            speechController.stop()
                            voiceTarget = VoiceTarget.Item
                        },
                        onEntryFocusCleared = { id ->
                            if (selectedEntryId == id && !microphoneSessionActive) selectedEntryId = null
                        },
                        bottomPadding = listBottomPadding,
                        onItemDragActiveChange = { itemDragActive = it },
                        onListAtTopChanged = { activeListAtTop = it },
                        editTapSuppressionSerial = oneHandMoveSerial,
                        recentlyMovedEntryIds = recentlyMovedEntryIds,
                        recentlyRestoredEntryIds = recentlyRestoredEntryIds.toSet(),
                        restoredScrollTargetEntryId = restoredScrollTargetEntryId,
                        restoredScrollSerial = restoredScrollSerial,
                        onClearTransientHighlight = ::clearDetailTransientHighlight,
                        onChanged = onChanged
                    )
                } else {
                    DeletedItemsPage(
                        memo = memo,
                        onItemDragActiveChange = { itemDragActive = it },
                        onListAtTopChanged = { deletedListAtTop = it },
                        voiceScrollDirection = 0,
                        voiceScrollSerial = detailVoiceScrollSerial,
                        onVoiceScrollFinished = { stopDetailVoiceScroll() },
                        onRestore = {
                            memo.deletedEntries.remove(it)
                            val restored = restoreEntryToActiveBottom(memo, it, keepCompletedItemsInPlace)
                            recentlyRestoredEntryIds.remove(restored.id)
                            recentlyRestoredEntryIds.add(restored.id)
                            restoredScrollTargetEntryId = restored.id
                            restoredScrollSerial++
                            scope.launch { pagerState.animateScrollToPage(0) }
                            onChanged()
                        },
                        onErase = {
                            memo.deletedEntries.remove(it)
                            onChanged()
                        }
                    )
                }
            }
        }

        val showActiveItemJumpButtons = pagerState.currentPage == 0 && activeItemCount >= 20
        val showDetailMicrophone = microphoneEnabled && pagerState.currentPage == 0
        if (showDetailMicrophone || showActiveItemJumpButtons) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .align(physicalBottomSideAlignment(leftHandModeEnabled))
                        .absolutePadding(
                            left = if (leftHandModeEnabled) 12.dp else 0.dp,
                            right = if (leftHandModeEnabled) 0.dp else 12.dp,
                            bottom = fabBottomPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (showActiveItemJumpButtons) {
                        JumpFab(
                            label = if (leftHandModeEnabled) "⇑" else "⇓",
                            sizeScale = HomeOperationScale,
                            onClick = { requestActiveItemJump(if (leftHandModeEnabled) -1 else 1) }
                        )
                    }
                    if (showDetailMicrophone) {
                        MicFab(
                            controller = speechController,
                            sizeScale = HomeOperationScale,
                            modifier = Modifier,
                            onClick = {
                                if (speechController.partialText.isNotBlank()) {
                                    speechController.commitPartial()
                                    return@MicFab
                                }
                                if (speechController.isRunning) {
                                    speechController.stop()
                                    stopDetailVoiceScroll()
                                    onMicrophoneSessionActiveChange(false)
                                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    if (memo.title.isBlank() || titleFocused) {
                                        selectedEntryId = null
                                        voiceEditingEntryId = null
                                        focusedItemRequestId = null
                                        voiceTarget = VoiceTarget.Title
                                    } else {
                                        voiceEditingEntryId = selectedEntryId ?: voiceEditingEntryId
                                    }
                                    keyboardController?.hide()
                                    speechController.start()
                                    onMicrophoneSessionActiveChange(true)
                                } else {
                                    if (memo.title.isBlank() || titleFocused) {
                                        selectedEntryId = null
                                        voiceEditingEntryId = null
                                        focusedItemRequestId = null
                                        voiceTarget = VoiceTarget.Title
                                    } else {
                                        voiceEditingEntryId = selectedEntryId ?: voiceEditingEntryId
                                    }
                                    requestDetailMicrophonePermission()
                                }
                            }
                        )
                    }
                    if (showActiveItemJumpButtons) {
                        JumpFab(
                            label = if (leftHandModeEnabled) "⇓" else "⇑",
                            sizeScale = HomeOperationScale,
                            onClick = { requestActiveItemJump(if (leftHandModeEnabled) 1 else -1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTitleArea(
    memo: ShoppingMemo,
    titleFocusRequester: FocusRequester,
    titlePattern: Int?,
    onTitlePatternClick: (() -> Unit)?,
    showEraseButton: Boolean,
    onErase: () -> Unit,
    microphoneHighlighted: Boolean,
    editingHighlighted: Boolean,
    onTitleFocused: () -> Unit,
    onTitleFocusCleared: () -> Unit,
    onChanged: () -> Unit
) {
    val titleSparkleAlpha = rememberSparkleAlpha(microphoneHighlighted)
    val titleHighlightModifier = when {
        microphoneHighlighted -> Modifier
            .goldAddItemBackground(true)
            .sparkleOverlay(titleSparkleAlpha * 0.45f)
        editingHighlighted -> Modifier.background(focusedEntryBackground())
        else -> Modifier
    }
    val titleInput: @Composable RowScope.() -> Unit = {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
        ) {
            var titleFontSizeValue by remember(memo.title, maxWidth) { mutableStateOf(22f) }
            BasicTextField(
                value = memo.title,
                onValueChange = {
                    memo.title = it
                    onChanged()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) onTitleFocused() else onTitleFocusCleared()
                    },
                textStyle = TextStyle(
                    fontSize = titleFontSizeValue.sp,
                    lineHeight = (titleFontSizeValue * 1.23f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2D22),
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(Color(0xFF1976D2)),
                onTextLayout = { result ->
                    if (result.didOverflowWidth && titleFontSizeValue > 8f) {
                        titleFontSizeValue = smallerFittingFontSize(titleFontSizeValue, 8.sp)
                    }
                },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (memo.title.isBlank()) {
                            Text(
                                stringResource(R.string.title_untitled),
                                color = Color(0xFFC0C4CC),
                                fontSize = titleFontSizeValue.sp,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                }
            )
        }
        if (showEraseButton) {
            Button(
                onClick = onErase,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFD32F2F)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
            ) {
                Text(stringResource(R.string.action_clear_all), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (titlePattern != null) {
        TitlePatternHeader(
            title = "",
            pattern = titlePattern,
            onClick = null
        ) {
            Box(modifier = Modifier.matchParentSize().then(titleHighlightModifier))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeTitleHeaderHeight)
                    .align(Alignment.Center)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = titleInput
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeTitleHeaderHeight)
                .then(titleHighlightModifier)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = titleInput
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveItemsPage(
    memo: ShoppingMemo,
    selectedEntryId: String?,
    focusedItemRequestId: String?,
    addButtonScrollRequest: Int,
    voiceScrollDirection: Int,
    voiceScrollSerial: Int,
    onVoiceScrollFinished: () -> Unit,
    activeItemJumpDirection: Int,
    activeItemJumpSerial: Int,
    highlightedEntryId: String?,
    highlightAddListItem: Boolean,
    keepCompletedItemsInPlace: Boolean,
    onFocusedItemConsumed: () -> Unit,
    onSelect: (String?) -> Unit,
    onRequestFocus: (String) -> Unit,
    onEditingDismissed: () -> Unit,
    entryCursorRanges: Map<String, TextRange>,
    onEntryCursorChanged: (String, TextRange) -> Unit,
    onEntryFocused: (String) -> Unit,
    onEntryFocusCleared: (String) -> Unit,
    bottomPadding: Dp,
    onItemDragActiveChange: (Boolean) -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    editTapSuppressionSerial: Int,
    recentlyMovedEntryIds: Set<String>,
    recentlyRestoredEntryIds: Set<String>,
    restoredScrollTargetEntryId: String?,
    restoredScrollSerial: Int,
    onClearTransientHighlight: (String) -> Unit,
    onChanged: () -> Unit
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val rowLiftPx = with(density) { 96.dp.roundToPx() }
    val addButtonLiftPx = with(density) { 72.dp.roundToPx() }
    val bottomPaddingPx = with(density) { bottomPadding.roundToPx() }
    val dragEdgePaddingPx = with(density) { 8.dp.toPx() }
    val fallbackRowHeightPx = with(density) { 56.dp.toPx() }
    val deleteSwipeThresholdPx = with(density) { 104.dp.toPx() }
    val listTopAnchorIndexOffset = 1
    var scrollAnchor by remember { mutableStateOf(ScrollAnchor.Item) }
    var draggingEntryId by remember { mutableStateOf<String?>(null) }
    var draggingOffsetY by remember { mutableStateOf(0f) }
    var bottomClampGapY by remember { mutableStateOf(0f) }
    var topClampGapY by remember { mutableStateOf(0f) }
    var dragDirectionY by remember { mutableStateOf(0) }
    var autoReorderDirection by remember { mutableStateOf(0) }
    var deleteSwipeEntryId by remember { mutableStateOf<String?>(null) }
    var deleteSwipeOffsetX by remember { mutableStateOf(0f) }
    var previousBottomPadding by remember { mutableStateOf(bottomPadding) }
    val listAtTop = !listState.canScrollBackward

    fun activeEntriesForDisplay(): List<ShoppingEntry> {
        return if (keepCompletedItemsInPlace) {
            memo.entries.toList()
        } else {
            memo.entries.filter { !it.checked }
        }
    }

    fun activeVisibleEntriesForJump(): List<ShoppingEntry> {
        return memo.entries.filter {
            it.name.isNotBlank() && (keepCompletedItemsInPlace || !it.checked)
        }
    }

    fun detailEntryRowKey(entry: ShoppingEntry): Any {
        return if (keepCompletedItemsInPlace || !entry.checked) entry.id else "done-${entry.id}"
    }

    fun reorderGroup(entry: ShoppingEntry): List<ShoppingEntry> {
        return displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
    }

    fun addListItemIndex(): Int {
        return activeEntriesForDisplay().size + listTopAnchorIndexOffset
    }

    fun removeUnusedBlankEntry(): Boolean {
        if (memo.entries.none { it.name.isNotBlank() && (keepCompletedItemsInPlace || !it.checked) }) return false
        val removed = memo.entries.removeAll { it.name.isBlank() }
        if (removed) {
            onSelect(null)
            onFocusedItemConsumed()
        }
        return removed
    }

    LaunchedEffect(listAtTop) {
        onListAtTopChanged(listAtTop)
    }

    LaunchedEffect(selectedEntryId, highlightedEntryId) {
        selectedEntryId?.let(onClearTransientHighlight)
        highlightedEntryId?.let(onClearTransientHighlight)
    }

    LaunchedEffect(voiceScrollSerial, voiceScrollDirection) {
        val direction = voiceScrollDirection
        if (direction == 0) return@LaunchedEffect
        while (true) {
            val canScroll = if (direction < 0) listState.canScrollBackward else listState.canScrollForward
            if (!canScroll) {
                onVoiceScrollFinished()
                break
            }
            val consumed = listState.scrollBy(direction * VoiceScrollStepPx)
            if (kotlin.math.abs(consumed) < 0.5f) {
                onVoiceScrollFinished()
                break
            }
            delay(VoiceScrollDelayMillis)
        }
    }

    LaunchedEffect(activeItemJumpSerial) {
        if (activeItemJumpSerial <= 0 || activeItemJumpDirection == 0) return@LaunchedEffect
        val visibleActiveEntries = activeVisibleEntriesForJump()
        val targetEntry = if (activeItemJumpDirection < 0) {
            visibleActiveEntries.firstOrNull()
        } else {
            visibleActiveEntries.lastOrNull()
        } ?: return@LaunchedEffect
        val visibleIndex = visibleActiveEntries.indexOfFirst { it.id == targetEntry.id }
        if (visibleIndex >= 0) {
            listState.scrollToItem(index = visibleIndex + listTopAnchorIndexOffset)
        }
    }

    fun bottomAlignedOffset(extraLiftPx: Int): Int {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val targetY = (viewportHeight - bottomPaddingPx - extraLiftPx).coerceAtLeast(0)
        return -targetY
    }

    fun editingAlignedOffset(): Int {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val visibleHeight = (viewportHeight - bottomPaddingPx).coerceAtLeast((viewportHeight * 0.35f).roundToInt())
        val targetY = (visibleHeight * 0.30f).roundToInt().coerceAtLeast(0)
        return -targetY
    }

    LaunchedEffect(restoredScrollSerial, restoredScrollTargetEntryId, bottomPadding, memo.entries.size) {
        if (restoredScrollSerial <= 0) return@LaunchedEffect
        val targetId = restoredScrollTargetEntryId ?: return@LaunchedEffect
        val visibleActiveEntries = activeEntriesForDisplay()
        val visibleIndex = visibleActiveEntries.indexOfFirst { it.id == targetId }
        if (visibleIndex >= 0) {
            listState.animateScrollToItem(
                index = visibleIndex + listTopAnchorIndexOffset,
                scrollOffset = bottomAlignedOffset(rowLiftPx)
            )
        }
    }

    LaunchedEffect(memo.entries.size, focusedItemRequestId, selectedEntryId) {
        val editingBlank = memo.entries.any {
            it.name.isBlank() && (it.id == focusedItemRequestId || it.id == selectedEntryId)
        }
        if (!editingBlank) {
            ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
        }
    }

    LaunchedEffect(focusedItemRequestId, selectedEntryId, bottomPadding) {
        if (scrollAnchor == ScrollAnchor.AddButton) return@LaunchedEffect
        val targetId = focusedItemRequestId ?: selectedEntryId ?: return@LaunchedEffect
        val index = memo.entries.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            withFrameNanos { }
            delay(120)
            listState.animateScrollToItem(index = index + listTopAnchorIndexOffset, scrollOffset = editingAlignedOffset())
        }
    }

    LaunchedEffect(scrollAnchor, bottomPadding, memo.entries.size) {
        if (scrollAnchor != ScrollAnchor.AddButton) return@LaunchedEffect
        listState.animateScrollToItem(index = addListItemIndex(), scrollOffset = bottomAlignedOffset(addButtonLiftPx))
    }

    LaunchedEffect(addButtonScrollRequest, bottomPadding, memo.entries.size) {
        if (addButtonScrollRequest <= 0) return@LaunchedEffect
        listState.animateScrollToItem(index = addListItemIndex(), scrollOffset = bottomAlignedOffset(addButtonLiftPx))
    }

    LaunchedEffect(bottomPadding) {
        if (bottomPadding < previousBottomPadding) {
            draggingEntryId = null
            draggingOffsetY = 0f
            bottomClampGapY = 0f
            topClampGapY = 0f
            dragDirectionY = 0
            autoReorderDirection = 0
            deleteSwipeEntryId = null
            deleteSwipeOffsetX = 0f
            onItemDragActiveChange(false)
        }
        previousBottomPadding = bottomPadding
    }

    fun selectEntry(entry: ShoppingEntry) {
        if (entry.checked) return
        val blanksToRemove = memo.entries.filter { it.id != entry.id && it.name.isBlank() }
        if (blanksToRemove.isNotEmpty() && memo.entries.size > blanksToRemove.size) {
            memo.entries.removeAll(blanksToRemove)
            onChanged()
        }
        scrollAnchor = ScrollAnchor.Item
        onSelect(entry.id)
    }

    fun moveEntry(entry: ShoppingEntry, direction: Int): Boolean {
        if (!moveEntryWithinVisibleGroup(memo, entry, direction, keepCompletedItemsInPlace)) return false
        onChanged()
        return true
    }

    fun visibleItemHeightPx(entry: ShoppingEntry): Float {
        val itemKey = detailEntryRowKey(entry)
        return listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == itemKey }
            ?.size
            ?.toFloat()
            ?: fallbackRowHeightPx
    }

    fun listIndexForEntry(entry: ShoppingEntry): Int {
        if (keepCompletedItemsInPlace) {
            val index = activeEntriesForDisplay().indexOfFirst { it.id == entry.id }
            return if (index < 0) -1 else index + listTopAnchorIndexOffset
        }
        return if (entry.checked) {
            val doneIndex = memo.entries.filter { it.checked && it.name.isNotBlank() }.indexOf(entry)
            val uncheckedCount = memo.entries.count { !it.checked }
            if (doneIndex < 0) -1 else uncheckedCount + 2 + doneIndex + listTopAnchorIndexOffset
        } else {
            val index = memo.entries.filter { !it.checked }.indexOf(entry)
            if (index < 0) -1 else index + listTopAnchorIndexOffset
        }
    }

    fun gentlyKeepDraggedEntryVisible(entry: ShoppingEntry, accumulateBottomGap: Boolean = true) {
        val itemKey = detailEntryRowKey(entry)
        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
            ?: return
        val viewportStart = listState.layoutInfo.viewportStartOffset
        val autoScrollStart = viewportStart + bottomPaddingPx
        val autoScrollEnd = listState.layoutInfo.viewportEndOffset - bottomPaddingPx
        val dragDisplayStart = viewportStart + dragEdgePaddingPx
        val dragDisplayEnd = listState.layoutInfo.viewportEndOffset - dragEdgePaddingPx
        var visualTop = itemInfo.offset + draggingOffsetY
        var visualBottom = visualTop + itemInfo.size
        val edgeBand = itemInfo.size * 0.35f
        val detectedAutoDirection = when {
            visualBottom >= autoScrollEnd - edgeBand -> 1
            visualTop <= autoScrollStart + edgeBand -> -1
            else -> 0
        }
        autoReorderDirection = when {
            dragDirectionY > 0 && detectedAutoDirection < 0 -> 0
            dragDirectionY < 0 && detectedAutoDirection > 0 -> 0
            else -> detectedAutoDirection
        }
        val lowerLimitOverflow = visualBottom - dragDisplayEnd
        if (lowerLimitOverflow > 0f && (bottomClampGapY > 0f || dragDirectionY >= 0)) {
            draggingOffsetY -= lowerLimitOverflow
            topClampGapY = 0f
            if (accumulateBottomGap && dragDirectionY >= 0) {
                bottomClampGapY += lowerLimitOverflow
            }
            visualTop -= lowerLimitOverflow
            visualBottom -= lowerLimitOverflow
        }
        val upperLimitOverflow = viewportStart - visualTop
        val upperDisplayOverflow = dragDisplayStart - visualTop
        if (upperDisplayOverflow > 0f && (topClampGapY > 0f || dragDirectionY <= 0)) {
            draggingOffsetY += upperDisplayOverflow
            bottomClampGapY = 0f
            if (accumulateBottomGap && dragDirectionY <= 0) {
                topClampGapY += upperDisplayOverflow
            }
            visualTop += upperDisplayOverflow
            visualBottom += upperDisplayOverflow
        } else if (upperLimitOverflow > 0f && dragDirectionY <= 0) {
            draggingOffsetY += upperLimitOverflow
            bottomClampGapY = 0f
            topClampGapY = 0f
            visualTop += upperLimitOverflow
            visualBottom += upperLimitOverflow
        }
        val overflowBottom = visualBottom - dragDisplayEnd
        val overflowTop = viewportStart - visualTop
        val scrollAmount = when {
            overflowBottom > 0 && (bottomClampGapY > 0f || dragDirectionY >= 0) -> overflowBottom.coerceAtMost(itemInfo.size / 2f)
            overflowTop > 0 && (topClampGapY > 0f || dragDirectionY <= 0) -> -overflowTop.coerceAtMost(itemInfo.size / 2f)
            else -> 0
        }.toFloat()
        if (scrollAmount != 0f) {
            draggingOffsetY += scrollAmount
            scope.launch { listState.scrollBy(scrollAmount) }
        }
    }

    fun keepDraggedEntryVisibleAfterLayout(entry: ShoppingEntry, accumulateBottomGap: Boolean = true) {
        scope.launch {
            withFrameNanos { }
            if (draggingEntryId == entry.id) {
                gentlyKeepDraggedEntryVisible(entry, accumulateBottomGap)
            }
        }
    }

    fun draggedVisualTop(entry: ShoppingEntry): Float? {
        val itemKey = detailEntryRowKey(entry)
        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
            ?: return null
        return itemInfo.offset + draggingOffsetY
    }

    fun keepDraggedEntryAtVisualTopAfterLayout(
        entry: ShoppingEntry,
        visualTop: Float,
        firstVisibleIndex: Int? = null,
        firstVisibleScrollOffset: Int = 0
    ) {
        scope.launch {
            withFrameNanos { }
            if (draggingEntryId != entry.id) return@launch
            if (firstVisibleIndex != null) {
                listState.scrollToItem(firstVisibleIndex, firstVisibleScrollOffset)
                withFrameNanos { }
                if (draggingEntryId != entry.id) return@launch
            }
            val itemKey = detailEntryRowKey(entry)
            val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
                ?: return@launch
            draggingOffsetY = visualTop - itemInfo.offset
            gentlyKeepDraggedEntryVisible(entry)
        }
    }

    fun isDraggedNearTopEdge(entry: ShoppingEntry): Boolean {
            val itemKey = detailEntryRowKey(entry)
        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
            ?: return false
        val viewportStart = listState.layoutInfo.viewportStartOffset
        val autoScrollStart = viewportStart + bottomPaddingPx
        val visualTop = itemInfo.offset + draggingOffsetY
        return visualTop <= autoScrollStart + itemInfo.size * 0.35f
    }

    fun pinDraggedEntryToTopAfterLayout(entry: ShoppingEntry) {
        scope.launch {
            withFrameNanos { }
            if (draggingEntryId != entry.id) return@launch
            val targetIndex = listIndexForEntry(entry)
            if (targetIndex < 0) return@launch
            draggingOffsetY = 0f
            bottomClampGapY = 0f
            topClampGapY = 0f
            listState.scrollToItem(targetIndex)
            withFrameNanos { }
            if (draggingEntryId == entry.id) {
                val group = reorderGroup(entry)
                val groupIndex = group.indexOf(entry)
                autoReorderDirection = if (dragDirectionY < 0 && groupIndex > 0) -1 else 0
            }
        }
    }

    fun startReorder(entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        draggingEntryId = entry.id
        draggingOffsetY = 0f
        bottomClampGapY = 0f
        topClampGapY = 0f
        dragDirectionY = 0
        autoReorderDirection = 0
        scrollAnchor = ScrollAnchor.Item
        onSelect(null)
        val entryListIndex = listIndexForEntry(entry)
        if (entryListIndex == listTopAnchorIndexOffset && listState.firstVisibleItemIndex == entryListIndex) {
            scope.launch { listState.scrollToItem(ListTopAnchorIndex) }
        }
    }

    fun dragReorder(entry: ShoppingEntry, deltaY: Float) {
        if (draggingEntryId != entry.id) return
        val firstVisibleIndexAtDragStart = listState.firstVisibleItemIndex
        val firstVisibleOffsetAtDragStart = listState.firstVisibleItemScrollOffset
        if (deltaY > 0f) {
            dragDirectionY = 1
        } else if (deltaY < 0f) {
            dragDirectionY = -1
        }
        val effectiveDeltaY = if (bottomClampGapY > 0f) {
            if (deltaY >= 0f) {
                bottomClampGapY += deltaY
                0f
            } else {
                val releasedGap = minOf(bottomClampGapY, -deltaY)
                bottomClampGapY -= releasedGap
                deltaY + releasedGap
            }
        } else if (topClampGapY > 0f) {
            if (deltaY <= 0f) {
                topClampGapY += -deltaY
                0f
            } else {
                val releasedGap = minOf(topClampGapY, deltaY)
                topClampGapY -= releasedGap
                deltaY - releasedGap
            }
        } else {
            deltaY
        }
        draggingOffsetY += effectiveDeltaY
        val targetVisualTop = draggedVisualTop(entry)
        var movedDuringDrag = false
        var restoreFirstVisibleAfterMove = false

        while (true) {
            val reorderGroup = reorderGroup(entry)
            val index = reorderGroup.indexOf(entry)
            if (index < 0) return

            if (draggingOffsetY > 0f) {
                val next = reorderGroup.getOrNull(index + 1)
                if (next == null) {
                    draggingOffsetY = 0f
                    break
                }
                val nextHeight = visibleItemHeightPx(next)
                if (draggingOffsetY <= nextHeight / 2f) break
                val entryListIndexBeforeMove = listIndexForEntry(entry)
                if (moveEntry(entry, 1)) {
                    movedDuringDrag = true
                    if (entryListIndexBeforeMove == firstVisibleIndexAtDragStart) {
                        restoreFirstVisibleAfterMove = true
                    }
                    draggingOffsetY -= nextHeight
                    if (keepCompletedItemsInPlace || !entry.checked) {
                        val newIndex = activeVisibleEntriesForJump().indexOf(entry)
                        val newListIndex = newIndex + listTopAnchorIndexOffset
                        if (newIndex >= 0 && newListIndex <= listState.firstVisibleItemIndex) {
                            scope.launch { listState.scrollToItem(newListIndex) }
                        }
                    }
                } else {
                    draggingOffsetY = 0f
                    break
                }
            } else if (draggingOffsetY < 0f) {
                val previous = reorderGroup.getOrNull(index - 1)
                if (previous == null) {
                    draggingOffsetY = 0f
                    break
                }
                val previousHeight = visibleItemHeightPx(previous)
                if (-draggingOffsetY <= previousHeight / 2f) break
                if (moveEntry(entry, -1)) {
                    movedDuringDrag = true
                    draggingOffsetY += previousHeight
                    if (keepCompletedItemsInPlace || !entry.checked) {
                        val newIndex = activeVisibleEntriesForJump().indexOf(entry)
                        val newListIndex = newIndex + listTopAnchorIndexOffset
                        if (newIndex >= 0 && newListIndex <= listState.firstVisibleItemIndex) {
                            scope.launch { listState.scrollToItem(newListIndex) }
                        }
                    }
                } else {
                    draggingOffsetY = 0f
                    break
                }
            } else {
                break
            }
        }

        val reorderGroup = reorderGroup(entry)
        val index = reorderGroup.indexOf(entry)
        if (index == 0 && draggingOffsetY < 0f) {
            draggingOffsetY = 0f
        } else if (index == reorderGroup.lastIndex && draggingOffsetY > 0f) {
            draggingOffsetY = 0f
        }
        if (movedDuringDrag) {
            if (dragDirectionY < 0 && isDraggedNearTopEdge(entry)) {
                pinDraggedEntryToTopAfterLayout(entry)
            } else if (restoreFirstVisibleAfterMove && targetVisualTop != null) {
                keepDraggedEntryAtVisualTopAfterLayout(
                    entry = entry,
                    visualTop = targetVisualTop,
                    firstVisibleIndex = firstVisibleIndexAtDragStart,
                    firstVisibleScrollOffset = firstVisibleOffsetAtDragStart
                )
            } else if (targetVisualTop != null) {
                keepDraggedEntryAtVisualTopAfterLayout(entry, targetVisualTop)
            } else {
                keepDraggedEntryVisibleAfterLayout(entry)
            }
        } else {
            gentlyKeepDraggedEntryVisible(entry)
        }
    }

    fun endReorder() {
        draggingEntryId = null
        draggingOffsetY = 0f
        bottomClampGapY = 0f
        topClampGapY = 0f
        dragDirectionY = 0
        autoReorderDirection = 0
    }

    LaunchedEffect(draggingEntryId, autoReorderDirection) {
        while (draggingEntryId != null && autoReorderDirection != 0) {
            delay(DragAutoReorderDelayMillis)
            val entry = memo.entries.firstOrNull { it.id == draggingEntryId } ?: break
            val group = reorderGroup(entry)
            val index = group.indexOf(entry)
            val direction = autoReorderDirection
            val neighbor = group.getOrNull(index + direction)
            if (index < 0 || neighbor == null) {
                autoReorderDirection = 0
                break
            }
            val neighborHeight = visibleItemHeightPx(neighbor)
            if (moveEntry(entry, direction)) {
                if (direction < 0) {
                    pinDraggedEntryToTopAfterLayout(entry)
                } else {
                    draggingOffsetY -= neighborHeight
                    draggingOffsetY += neighborHeight
                    scope.launch { listState.scrollBy(neighborHeight) }
                    keepDraggedEntryVisibleAfterLayout(entry, accumulateBottomGap = false)
                }
                val updatedGroup = reorderGroup(entry)
                val updatedIndex = updatedGroup.indexOf(entry)
                autoReorderDirection = if (updatedGroup.getOrNull(updatedIndex + direction) != null) {
                    direction
                } else {
                    0
                }
            } else {
                autoReorderDirection = 0
                break
            }
        }
    }

    fun startDeleteSwipe(entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        startReorder(entry)
        deleteSwipeEntryId = entry.id
        deleteSwipeOffsetX = 0f
        onItemDragActiveChange(true)
        onSelect(null)
    }

    fun dragDeleteSwipe(entry: ShoppingEntry, deltaX: Float) {
        if (deleteSwipeEntryId != entry.id) return
        deleteSwipeOffsetX = (deleteSwipeOffsetX + deltaX).coerceAtLeast(0f)
    }

    fun endDeleteSwipe(entry: ShoppingEntry) {
        if (deleteSwipeEntryId == entry.id && deleteSwipeOffsetX >= deleteSwipeThresholdPx) {
            memo.entries.remove(entry)
            memo.deletedEntries.add(entry.copy(checked = false))
            ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
            onChanged()
        }
        deleteSwipeEntryId = null
        deleteSwipeOffsetX = 0f
        endReorder()
        onItemDragActiveChange(false)
    }

    fun markDone(entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        if (keepCompletedItemsInPlace) {
            entry.checked = true
            onSelect(null)
            ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
            onChanged()
            return
        }
        memo.entries.remove(entry)
        entry.checked = true
        memo.entries.add(entry)
        onSelect(null)
        ensureDisplayBlankEntry(memo)
        onChanged()
    }

    fun restoreDone(entry: ShoppingEntry) {
        if (keepCompletedItemsInPlace) {
            entry.checked = false
            ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
            onChanged()
            return
        }
        memo.entries.remove(entry)
        entry.checked = false
        val insertIndex = memo.entries.indexOfFirst { it.name.isBlank() }.let { if (it >= 0) it else memo.entries.size }
        memo.entries.add(insertIndex, entry)
        ensureDisplayBlankEntry(memo)
        onChanged()
    }

    fun toggleEntryColor(entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        entry.colorMark = (entry.colorMark + 1) % 4
        onSelect(null)
        onChanged()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(memo.id, draggingEntryId, deleteSwipeEntryId) {
                if (draggingEntryId != null || deleteSwipeEntryId != null) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalMove = Offset.Zero
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        totalMove += change.positionChange()
                        if (
                            totalMove.getDistance() > viewConfiguration.touchSlop &&
                            kotlin.math.abs(totalMove.y) > kotlin.math.abs(totalMove.x)
                        ) {
                            focusManager.clearFocus(force = true)
                            onSelect(null)
                            onFocusedItemConsumed()
                            onEditingDismissed()
                            if (removeUnusedBlankEntry()) {
                                onChanged()
                            }
                            break
                        }
                    }
                }
            },
        state = listState,
        contentPadding = PaddingValues(bottom = bottomPadding)
    ) {
        item(key = ListTopAnchorKey) {
            Spacer(Modifier.height(ListTopAnchorHeight))
        }
        val displayEntries = activeEntriesForDisplay()
        itemsIndexed(displayEntries, key = { _, entry -> detailEntryRowKey(entry) }) { index, entry ->
            val transientHighlighted = (recentlyMovedEntryIds.contains(entry.id) || recentlyRestoredEntryIds.contains(entry.id)) &&
                selectedEntryId != entry.id &&
                highlightedEntryId != entry.id
            val displayNumber = if (entry.name.isNotBlank()) {
                displayEntries.take(index + 1).count { it.name.isNotBlank() }
            } else {
                null
            }
            val actionLabel = if (keepCompletedItemsInPlace && entry.checked) stringResource(R.string.action_restore) else stringResource(R.string.completed)
            ShoppingEntryRow(
                entry = entry,
                displayNumber = displayNumber,
                selected = selectedEntryId == entry.id,
                shouldRequestFocus = focusedItemRequestId == entry.id,
                cursorRange = entryCursorRanges[entry.id],
                microphoneHighlighted = highlightedEntryId == entry.id,
                isDragging = draggingEntryId == entry.id,
                dragOffsetY = if (draggingEntryId == entry.id) draggingOffsetY else 0f,
                isDeleteSwiping = deleteSwipeEntryId == entry.id,
                deleteSwipeOffsetX = if (deleteSwipeEntryId == entry.id) deleteSwipeOffsetX else 0f,
                editTapSuppressionSerial = editTapSuppressionSerial,
                recentlyMoved = transientHighlighted,
                actionLabel = actionLabel,
                modifier = if (draggingEntryId == entry.id) {
                    Modifier.zIndex(1f)
                } else if (deleteSwipeEntryId == entry.id) {
                    Modifier.zIndex(1f)
                } else {
                    Modifier.animateItem().zIndex(0f)
                },
                onSelect = { selectEntry(entry) },
                onPressStarted = {
                    onClearTransientHighlight(entry.id)
                    onSelect(null)
                    onFocusedItemConsumed()
                    onEditingDismissed()
                },
                onEditRequested = {
                    onClearTransientHighlight(entry.id)
                    selectEntry(entry)
                    onRequestFocus(entry.id)
                },
                onNumberClick = { toggleEntryColor(entry) },
                onFocusConsumed = onFocusedItemConsumed,
                onCursorChanged = { range -> onEntryCursorChanged(entry.id, range) },
                onFocused = {
                    onClearTransientHighlight(entry.id)
                    onSelect(entry.id)
                    if (entry.name.isNotBlank()) scrollAnchor = ScrollAnchor.Item
                    onEntryFocused(entry.id)
                },
                onFocusCleared = {
                    onEntryFocusCleared(entry.id)
                },
                onNameChanged = {
                    onClearTransientHighlight(entry.id)
                    entry.name = it
                    if (
                        it.isBlank() &&
                        selectedEntryId == entry.id &&
                        memo.entries.any {
                            other -> other.id != entry.id &&
                                other.name.isNotBlank() &&
                                (keepCompletedItemsInPlace || !other.checked)
                        }
                    ) {
                        memo.entries.removeAll { other -> other.id != entry.id && other.name.isBlank() }
                    } else {
                        ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
                    }
                    onChanged()
                },
                onComplete = {
                    onClearTransientHighlight(entry.id)
                    if (keepCompletedItemsInPlace && entry.checked) {
                        restoreDone(entry)
                    } else {
                        markDone(entry)
                    }
                },
                onDelete = {
                    onClearTransientHighlight(entry.id)
                    memo.entries.remove(entry)
                    memo.deletedEntries.add(entry.copy(checked = false))
                    ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
                    onChanged()
                },
                onReorderStart = {
                    onClearTransientHighlight(entry.id)
                    startReorder(entry)
                },
                onReorderDrag = { deltaY -> dragReorder(entry, deltaY) },
                onReorderEnd = { endReorder() },
                onDeleteSwipeStart = { startDeleteSwipe(entry) },
                onDeleteSwipeDrag = { deltaX, deltaY ->
                    dragDeleteSwipe(entry, deltaX)
                    dragReorder(entry, deltaY)
                },
                onDeleteSwipeEnd = { endDeleteSwipe(entry) }
            )
        }
        item(key = AddListItemKey) {
            AddListItemButton(
                highlighted = highlightAddListItem,
                onClick = {
                    val entry = requestBlankEntry(memo, keepCompletedItemsInPlace)
                    scrollAnchor = ScrollAnchor.AddButton
                    onSelect(entry.id)
                    onRequestFocus(entry.id)
                    onChanged()
                }
            )
        }
        val doneEntries = if (keepCompletedItemsInPlace) {
            emptyList()
        } else {
            memo.entries.filter { it.checked && it.name.isNotBlank() }
        }
        if (doneEntries.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.completed_items),
                    color = Color(0xFFD32F2F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
            items(doneEntries, key = { "done-${it.id}" }) { entry ->
                val transientHighlighted = recentlyMovedEntryIds.contains(entry.id) || recentlyRestoredEntryIds.contains(entry.id)
                DoneEntryRow(
                    entry = entry,
                    isDragging = draggingEntryId == entry.id,
                    dragOffsetY = if (draggingEntryId == entry.id) draggingOffsetY else 0f,
                    isDeleteSwiping = deleteSwipeEntryId == entry.id,
                    deleteSwipeOffsetX = if (deleteSwipeEntryId == entry.id) deleteSwipeOffsetX else 0f,
                    recentlyMoved = transientHighlighted,
                    modifier = if (draggingEntryId == entry.id || deleteSwipeEntryId == entry.id) {
                        Modifier.zIndex(1f)
                    } else {
                        Modifier.animateItem().zIndex(0f)
                    },
                    onRestore = {
                        onClearTransientHighlight(entry.id)
                        restoreDone(entry)
                    },
                    onDeleteSwipeStart = {
                        onClearTransientHighlight(entry.id)
                        startDeleteSwipe(entry)
                    },
                    onDeleteSwipeDrag = { deltaX, deltaY ->
                        dragDeleteSwipe(entry, deltaX)
                        dragReorder(entry, deltaY)
                    },
                    onDeleteSwipeEnd = { endDeleteSwipe(entry) }
                )
            }
        }
    }
}

@Composable
private fun swipeToTrashModifier(
    key: Any,
    enabled: Boolean,
    editTapSuppressionSerial: Int = 0,
    onPressStarted: () -> Unit = {},
    onTap: (Offset) -> Unit = {},
    onSwipeStart: () -> Unit,
    onSwipeDrag: (Float, Float) -> Unit,
    onSwipeEnd: () -> Unit
): Modifier {
    val focusManager = LocalFocusManager.current
    val latestEditTapSuppressionSerial by rememberUpdatedState(editTapSuppressionSerial)
    return Modifier.pointerInput(key, enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val editTapSuppressionSerialAtDown = latestEditTapSuppressionSerial
            down.consume()
            val preLongPressResult = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var totalMove = Offset.Zero
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull false
                    totalMove += change.positionChange()
                    if (totalMove.getDistance() > viewConfiguration.touchSlop) {
                        return@withTimeoutOrNull false
                    }
                    if (!change.pressed) return@withTimeoutOrNull true
                }
            }
            if (preLongPressResult == true) {
                if (latestEditTapSuppressionSerial == editTapSuppressionSerialAtDown) {
                    onTap(down.position)
                }
                return@awaitEachGesture
            } else if (preLongPressResult == false) {
                focusManager.clearFocus(force = true)
                onPressStarted()
                return@awaitEachGesture
            }

            focusManager.clearFocus(force = true)
            onPressStarted()
            onSwipeStart()
            try {
                var activePointerId = down.id
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == activePointerId } ?: break
                    if (!change.pressed) break
                    val delta = change.positionChange()
                    if (delta.x != 0f || delta.y != 0f) {
                        change.consume()
                        onSwipeDrag(delta.x, delta.y)
                    }
                }
            } finally {
                onSwipeEnd()
            }
        }
    }
}

@Composable
private fun horizontalLongPressSwipeModifier(
    key: Any,
    onSwipeStart: () -> Unit,
    onSwipeDrag: (Float) -> Unit,
    onSwipeEnd: () -> Unit
): Modifier {
    val focusManager = LocalFocusManager.current
    return Modifier.pointerInput(key) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            down.consume()
            focusManager.clearFocus(force = true)
            val preLongPressResult = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var totalMove = Offset.Zero
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull false
                    totalMove += change.positionChange()
                    if (totalMove.getDistance() > viewConfiguration.touchSlop) {
                        return@withTimeoutOrNull false
                    }
                    if (!change.pressed) return@withTimeoutOrNull true
                }
            }
            if (preLongPressResult != null) return@awaitEachGesture

            onSwipeStart()
            try {
                val activePointerId = down.id
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == activePointerId } ?: break
                    if (!change.pressed) break
                    val delta = change.positionChange()
                    if (delta.x != 0f || delta.y != 0f) {
                        change.consume()
                        onSwipeDrag(delta.x)
                    }
                }
            } finally {
                onSwipeEnd()
            }
        }
    }
}

@Composable
private fun horizontalPressSwipeModifier(
    key: Any,
    onSwipeStart: () -> Unit,
    onSwipeDrag: (Float) -> Unit,
    onSwipeEnd: () -> Unit
): Modifier {
    val focusManager = LocalFocusManager.current
    return Modifier.pointerInput(key) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            down.consume()
            focusManager.clearFocus(force = true)
            onSwipeStart()
            try {
                val activePointerId = down.id
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == activePointerId } ?: break
                    if (!change.pressed) break
                    val delta = change.positionChange()
                    if (delta.x != 0f || delta.y != 0f) {
                        change.consume()
                        onSwipeDrag(delta.x)
                    }
                }
            } finally {
                onSwipeEnd()
            }
        }
    }
}

@Composable
private fun ShoppingEntryRow(
    entry: ShoppingEntry,
    displayNumber: Int?,
    selected: Boolean,
    shouldRequestFocus: Boolean,
    cursorRange: TextRange?,
    microphoneHighlighted: Boolean,
    isDragging: Boolean,
    dragOffsetY: Float,
    isDeleteSwiping: Boolean,
    deleteSwipeOffsetX: Float,
    editTapSuppressionSerial: Int,
    recentlyMoved: Boolean,
    actionLabel: String,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onPressStarted: () -> Unit,
    onEditRequested: (Offset) -> Unit,
    onNumberClick: () -> Unit,
    onFocusConsumed: () -> Unit,
    onCursorChanged: (TextRange) -> Unit,
    onFocused: () -> Unit,
    onFocusCleared: () -> Unit,
    onNameChanged: (String) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onReorderStart: () -> Unit,
    onReorderDrag: (Float) -> Unit,
    onReorderEnd: () -> Unit,
    onDeleteSwipeStart: () -> Unit,
    onDeleteSwipeDrag: (Float, Float) -> Unit,
    onDeleteSwipeEnd: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val sizes = LocalAppFontSizes.current
    var fieldValue by remember(entry.id) { mutableStateOf(TextFieldValue(entry.name)) }
    var textLayoutResult by remember(entry.id) { mutableStateOf<TextLayoutResult?>(null) }
    var pendingTapPosition by remember(entry.id) { mutableStateOf<Offset?>(null) }
    var textFocused by remember(entry.id) { mutableStateOf(false) }
    val rowBackground = when {
        entry.checked -> CompletedEntryBackground
        selected -> focusedEntryBackground()
        else -> entryColorMarkBackground(entry.colorMark)
    }
    val microphoneSparkleAlpha = rememberSparkleAlpha(microphoneHighlighted)
    val rowSwipeModifier = swipeToTrashModifier(
        key = entry.id,
        enabled = true,
        editTapSuppressionSerial = editTapSuppressionSerial,
        onPressStarted = onPressStarted,
        onTap = {
            pendingTapPosition = it
            onEditRequested(it)
        },
        onSwipeStart = onDeleteSwipeStart,
        onSwipeDrag = onDeleteSwipeDrag,
        onSwipeEnd = onDeleteSwipeEnd
    )

    LaunchedEffect(entry.name, cursorRange) {
        val requestedSelection = cursorRange?.let { coerceTextRange(it, entry.name.length) }
        if (entry.name != fieldValue.text) {
            val selection = requestedSelection ?: coerceTextRange(fieldValue.selection, entry.name.length)
            fieldValue = fieldValue.copy(text = entry.name, selection = selection)
        } else if (requestedSelection != null && requestedSelection != fieldValue.selection) {
            fieldValue = fieldValue.copy(selection = requestedSelection)
        }
    }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            val tapOffset = pendingTapPosition
                ?.let { textLayoutResult?.getOffsetForPosition(it) }
                ?.coerceIn(0, fieldValue.text.length)
            val selection = tapOffset?.let { TextRange(it) }
                ?: cursorRange?.let { coerceTextRange(it, fieldValue.text.length) }
                ?: TextRange(fieldValue.selection.start.coerceIn(0, fieldValue.text.length))
            fieldValue = fieldValue.copy(selection = selection)
            onCursorChanged(selection)
            if (!textFocused) {
                focusRequester.requestFocus()
            }
            onFocusConsumed()
            pendingTapPosition = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = deleteSwipeOffsetX
                translationY = dragOffsetY
                scaleX = if (isDragging || isDeleteSwiping) 1.02f else 1f
                scaleY = if (isDragging || isDeleteSwiping) 1.02f else 1f
                shadowElevation = if (isDragging || isDeleteSwiping) 16f else 0f
            }
            .then(
                if (microphoneHighlighted) {
                    Modifier
                        .goldAddItemBackground(true)
                        .sparkleOverlay(microphoneSparkleAlpha * 0.45f)
                } else {
                    Modifier.recentMoveBackground(recentlyMoved, rowBackground)
                }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberHandle(displayNumber = displayNumber, onClick = onNumberClick)
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it
                        onCursorChanged(it.selection)
                        if (it.text != entry.name) {
                            onNameChanged(it.text)
                        }
                    },
                    enabled = selected && !isDeleteSwiping,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        textFocused = it.isFocused
                        if (it.isFocused) onFocused() else onFocusCleared()
                    }
                    .padding(vertical = 12.dp),
                    minLines = 1,
                    maxLines = 8,
                    onTextLayout = { textLayoutResult = it },
                    textStyle = TextStyle(
                        fontSize = sizes.listText,
                        lineHeight = sizes.listLineHeight,
                        color = Color.Black,
                        textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    cursorBrush = SolidColor(Color(0xFF1976D2)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorationBox = { innerTextField ->
                        if (entry.name.isBlank()) {
                            Text(stringResource(R.string.input_empty), color = Color(0xFF9E9E9E), fontSize = sizes.listText)
                        }
                        innerTextField()
                    }
                )
                Box(modifier = Modifier.matchParentSize().then(rowSwipeModifier))
            }
            TextButton(
                enabled = entry.name.isNotBlank(),
                onClick = onComplete
            ) {
                Text(
                    text = if (isDeleteSwiping) "→ 🗑" else actionLabel,
                    color = if (isDeleteSwiping) Color(0xFFD32F2F) else if (entry.name.isBlank()) Color(0xFFBBBBBB) else Color(0xFF1976D2),
                    fontSize = sizes.listAction,
                    fontWeight = if (isDeleteSwiping) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

private fun entryColorMarkBackground(colorMark: Int): Color {
    return when (colorMark) {
        1 -> Color(0xFFFFEEEE)
        2 -> Color(0xFFEAF4FF)
        3 -> Color(0xFFEAF8E8)
        else -> Color.White
    }
}

private fun focusedEntryBackground(): Color {
    return Color(0xFFFFF8D8)
}

private fun coerceTextRange(range: TextRange, length: Int): TextRange {
    return TextRange(
        start = range.start.coerceIn(0, length),
        end = range.end.coerceIn(0, length)
    )
}

private fun Modifier.recentMoveBackground(active: Boolean, normalColor: Color): Modifier {
    return if (active) {
        background(
            Brush.horizontalGradient(
                colors = PurpleHighlightColors
            )
        )
    } else {
        background(normalColor)
    }
}

private fun Modifier.goldAddItemBackground(active: Boolean): Modifier {
    return if (active) {
        background(Brush.horizontalGradient(GoldHighlightColors))
    } else {
        background(Color.White)
    }
}

@Composable
private fun NumberHandle(displayNumber: Int?, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .padding(start = 6.dp, end = 10.dp)
            .size(32.dp)
            .then(if (displayNumber == null) Modifier else Modifier.clickable(onClick = onClick))
            .background(
                color = if (displayNumber == null) Color.Transparent else Color(0xFFE3F2FD),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (displayNumber == null) {
            Text("・", color = Color(0xFF9E9E9E), fontSize = 24.sp)
        } else {
            Text(
                text = displayNumber.toString(),
                color = Color(0xFF1976D2),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DoneEntryRow(
    entry: ShoppingEntry,
    isDragging: Boolean,
    dragOffsetY: Float,
    isDeleteSwiping: Boolean,
    deleteSwipeOffsetX: Float,
    recentlyMoved: Boolean,
    modifier: Modifier = Modifier,
    onRestore: () -> Unit,
    onDeleteSwipeStart: () -> Unit,
    onDeleteSwipeDrag: (Float, Float) -> Unit,
    onDeleteSwipeEnd: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    val swipeModifier = swipeToTrashModifier(
        key = "done-${entry.id}",
        enabled = entry.name.isNotBlank(),
        onSwipeStart = onDeleteSwipeStart,
        onSwipeDrag = onDeleteSwipeDrag,
        onSwipeEnd = onDeleteSwipeEnd
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = deleteSwipeOffsetX
                translationY = dragOffsetY
                scaleX = if (isDragging || isDeleteSwiping) 1.02f else 1f
                scaleY = if (isDragging || isDeleteSwiping) 1.02f else 1f
                shadowElevation = if (isDragging || isDeleteSwiping) 16f else 0f
            }
            .recentMoveBackground(recentlyMoved, CompletedEntryBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "・",
            color = Color(0xFF888888),
            fontSize = 28.sp,
            modifier = Modifier.padding(start = 8.dp, end = 10.dp)
        )
        Text(
            text = entry.name,
            color = Color(0xFF777777),
            fontSize = sizes.listText,
            lineHeight = sizes.listLineHeight,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.LineThrough,
            modifier = Modifier
                .weight(1f)
                .then(swipeModifier)
                .padding(vertical = 12.dp)
        )
        if (!isDragging && !isDeleteSwiping) {
            RestoreIconButton(onClick = onRestore)
        }
        if (isDeleteSwiping) {
            Text(
                text = "→ 🗑",
                color = Color(0xFFD32F2F),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun RestoreIconButton(onClick: () -> Unit) {
    val sizes = LocalAppFontSizes.current
    TextButton(onClick = onClick) {
        Text(stringResource(R.string.action_restore), color = Color(0xFF1976D2), fontSize = sizes.listAction, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeletedItemsPage(
    memo: ShoppingMemo,
    onItemDragActiveChange: (Boolean) -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    voiceScrollDirection: Int,
    voiceScrollSerial: Int,
    onVoiceScrollFinished: () -> Unit,
    onRestore: (ShoppingEntry) -> Unit,
    onErase: (ShoppingEntry) -> Unit
) {
    if (memo.deletedEntries.isEmpty()) {
        LaunchedEffect(Unit) {
            onListAtTopChanged(true)
        }
        PlaceholderBody(stringResource(R.string.trash_empty))
        return
    }
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val actionThresholdPx = with(density) { 104.dp.toPx() }
    var draggingEntryId by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    val listAtTop = !listState.canScrollBackward

    LaunchedEffect(listAtTop) {
        onListAtTopChanged(listAtTop)
    }

    LaunchedEffect(voiceScrollSerial, voiceScrollDirection) {
        val direction = voiceScrollDirection
        if (direction == 0) return@LaunchedEffect
        while (true) {
            val canScroll = if (direction < 0) listState.canScrollBackward else listState.canScrollForward
            if (!canScroll) {
                onVoiceScrollFinished()
                break
            }
            val consumed = listState.scrollBy(direction * VoiceScrollStepPx)
            if (kotlin.math.abs(consumed) < 0.5f) {
                onVoiceScrollFinished()
                break
            }
            delay(VoiceScrollDelayMillis)
        }
    }

    fun startDrag(entry: ShoppingEntry) {
        draggingEntryId = entry.id
        dragOffsetX = 0f
        onItemDragActiveChange(true)
    }

    fun drag(entry: ShoppingEntry, deltaX: Float) {
        if (draggingEntryId != entry.id) return
        dragOffsetX = (dragOffsetX + deltaX).coerceIn(-actionThresholdPx * 1.4f, actionThresholdPx * 1.4f)
    }

    fun endDrag(entry: ShoppingEntry) {
        val offset = dragOffsetX
        draggingEntryId = null
        dragOffsetX = 0f
        onItemDragActiveChange(false)
        when {
            offset <= -actionThresholdPx -> onRestore(entry)
            offset >= actionThresholdPx -> onErase(entry)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        items(memo.deletedEntries, key = { it.id }) { entry ->
            DeletedEntryRow(
                entry = entry,
                isDragging = draggingEntryId == entry.id,
                dragOffsetX = if (draggingEntryId == entry.id) dragOffsetX else 0f,
                onRestore = { onRestore(entry) },
                onErase = { onErase(entry) },
                onDragStart = { startDrag(entry) },
                onDrag = { deltaX -> drag(entry, deltaX) },
                onDragEnd = { endDrag(entry) }
            )
            Divider(color = Color(0xFFE0E0E0))
        }
    }
}

@Composable
private fun DeletedEntryRow(
    entry: ShoppingEntry,
    isDragging: Boolean,
    dragOffsetX: Float,
    onRestore: () -> Unit,
    onErase: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    val swipeModifier = horizontalLongPressSwipeModifier(
        key = "deleted-${entry.id}",
        onSwipeStart = onDragStart,
        onSwipeDrag = onDrag,
        onSwipeEnd = onDragEnd
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = dragOffsetX
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 16f else 0f
            }
            .background(Color.White)
            .then(swipeModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDragging) {
            Text(
                text = stringResource(R.string.action_restore),
                color = Color(0xFF1976D2),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
        } else {
            Text(
                text = "・",
                color = Color(0xFF888888),
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
        }
        Text(
            text = entry.name.ifBlank { stringResource(R.string.untitled_item) },
            fontSize = sizes.listText,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        if (isDragging) {
            Text(
                text = stringResource(R.string.action_erase),
                color = Color(0xFFD32F2F),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun DeletedItemsPageLegacy(
    memo: ShoppingMemo,
    onRestore: (ShoppingEntry) -> Unit,
    onErase: (ShoppingEntry) -> Unit
) {
    if (memo.deletedEntries.isEmpty()) {
        PlaceholderBody(stringResource(R.string.deleted_items_empty))
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(memo.deletedEntries, key = { it.id }) { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "・",
                    color = Color(0xFF888888),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(entry.name.ifBlank { stringResource(R.string.untitled_item) }, fontSize = 19.sp, color = Color(0xFF666666), modifier = Modifier.weight(1f))
                RestoreIconButton(onClick = { onRestore(entry) })
                TextButton(onClick = { onErase(entry) }) { Text(stringResource(R.string.action_erase), color = Color(0xFFD32F2F)) }
            }
            Divider(color = Color(0xFFE0E0E0))
        }
    }
}

@Composable
private fun MicFab(
    controller: ContinuousSpeechController,
    modifier: Modifier,
    sizeScale: Float = 1f,
    onClick: () -> Unit
) {
    val hasPartial = controller.partialText.isNotBlank()
    val isHearingSpeech = controller.isHearingSpeech
    val recordingTransition = rememberInfiniteTransition(label = "recording-stop")
    val stopScale by recordingTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording-stop-scale"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (hasPartial) {
            Text(
                text = controller.partialText,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color(0xFF333333),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (controller.isRunning) Color(0xFFE11D48) else Color(0xFF1E88E5),
            modifier = Modifier.size(86.dp * sizeScale)
        ) {
            if (controller.isRunning && !isHearingSpeech) {
                Box(
                    modifier = Modifier
                        .size(34.dp * sizeScale)
                        .graphicsLayer {
                            scaleX = stopScale
                            scaleY = stopScale
                        }
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    painter = painterResource(
                        id = if (isHearingSpeech) R.drawable.ic_fab_ear else R.drawable.ic_fab_mic
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size((if (isHearingSpeech) 46.dp else 50.dp) * sizeScale)
                )
            }
        }
    }
}

@Composable
private fun JumpFab(
    label: String,
    sizeScale: Float = 1f,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.White,
        modifier = Modifier
            .size(86.dp * sizeScale)
            .border(BorderStroke(1.dp, Color(0xFF90CAF9)), RoundedCornerShape(18.dp))
    ) {
        Text(
            text = label,
            color = Color(0xFF1976D2),
            fontSize = (42 * sizeScale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FavoritesScreen(
    memos: List<ShoppingMemo>,
    oneHandModeEnabled: Boolean,
    leftHandModeEnabled: Boolean,
    microphoneEnabled: Boolean,
    microphoneSessionActive: Boolean,
    onMicrophoneSessionActiveChange: (Boolean) -> Unit,
    keepCompletedItemsInPlace: Boolean,
    onHome: () -> Unit,
    onChanged: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val favoriteItemsListState = rememberLazyListState()
    var listAtTop by remember { mutableStateOf(true) }
    var itemDragActive by remember { mutableStateOf(false) }
    val recentlyRestoredEntryIds = remember { mutableStateListOf<String>() }
    var restoredMemoId by remember { mutableStateOf<String?>(null) }
    var restoredEntryId by remember { mutableStateOf<String?>(null) }
    var restoredScrollSerial by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 72.dp.toPx() }
    val latestOnHome by rememberUpdatedState(onHome)

    OneHandSettingsFrame(
        oneHandModeEnabled = oneHandModeEnabled,
        listAtTop = listAtTop,
        gestureBlocked = itemDragActive
    ) { frameModifier ->
        Box(frameModifier) {
            Column(
                Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTab, swipeThresholdPx, itemDragActive) {
                        if (itemDragActive) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            val startTab = selectedTab
                            var totalX = 0f
                            var totalY = 0f
                            var tabChanged = false
                            var verticalScrollDetected = false
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                val delta = change.positionChange()
                                totalX += delta.x
                                totalY += delta.y
                                if (
                                    kotlin.math.abs(totalY) > viewConfiguration.touchSlop &&
                                    kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 0.75f
                                ) {
                                    verticalScrollDetected = true
                                }
                                if (!tabChanged && kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f) {
                                    when {
                                        startTab == 0 && totalX < -swipeThresholdPx -> {
                                            selectedTab = 1
                                            tabChanged = true
                                            change.consume()
                                            break
                                        }
                                        startTab == 1 && totalX > swipeThresholdPx -> {
                                            selectedTab = 0
                                            tabChanged = true
                                            change.consume()
                                            break
                                        }
                                    }
                                }
                            }
                            if (
                                !tabChanged &&
                                startTab == 0 &&
                                totalX > DetailBackSwipeThresholdPx &&
                                !verticalScrollDetected &&
                                kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f
                            ) {
                                latestOnHome()
                            }
                        }
                    }
            ) {
                CompactHeader(stringResource(R.string.nav_favorites))
                FavoriteScreenTabs(selectedTab = selectedTab, onSelectTab = { selectedTab = it })
                if (memos.isEmpty()) {
                    LaunchedEffect(selectedTab) {
                        listAtTop = true
                    }
                    PlaceholderBody(stringResource(R.string.favorites_empty))
                } else {
                    when (selectedTab) {
                        0 -> FavoriteItemsOverview(
                            memos = memos,
                            listState = favoriteItemsListState,
                            leftHandModeEnabled = leftHandModeEnabled,
                            microphoneEnabled = microphoneEnabled,
                            microphoneSessionActive = microphoneSessionActive,
                            onMicrophoneSessionActiveChange = onMicrophoneSessionActiveChange,
                            keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                            voiceScrollDirection = 0,
                            voiceScrollSerial = 0,
                            onVoiceScrollFinished = { },
                            onItemDragActiveChange = { itemDragActive = it },
                            onListAtTopChanged = { listAtTop = it },
                            recentlyRestoredEntryIds = recentlyRestoredEntryIds.toSet(),
                            restoredMemoId = restoredMemoId,
                            restoredEntryId = restoredEntryId,
                            restoredScrollSerial = restoredScrollSerial,
                            onClearRestoredEntryHighlight = { recentlyRestoredEntryIds.remove(it) },
                            onEntryRestored = { memo, entry ->
                                recentlyRestoredEntryIds.remove(entry.id)
                                recentlyRestoredEntryIds.add(entry.id)
                                restoredMemoId = memo.id
                                restoredEntryId = entry.id
                                restoredScrollSerial++
                            },
                            onChanged = onChanged
                        )
                        else -> FavoriteTrashOverview(
                            memos = memos,
                            voiceScrollDirection = 0,
                            voiceScrollSerial = 0,
                            onVoiceScrollFinished = { },
                            onListAtTopChanged = { listAtTop = it },
                            keepCompletedItemsInPlace = keepCompletedItemsInPlace,
                            onEntryRestored = { memo, entry ->
                                recentlyRestoredEntryIds.remove(entry.id)
                                recentlyRestoredEntryIds.add(entry.id)
                                restoredMemoId = memo.id
                                restoredEntryId = entry.id
                                restoredScrollSerial++
                                selectedTab = 0
                            },
                            onChanged = onChanged
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun FavoriteScreenTabs(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.White)
    ) {
        listOf(stringResource(R.string.tab_items), stringResource(R.string.tab_trash)).forEachIndexed { index, label ->
            val selected = selectedTab == index
            val isTrash = index == 1
            val selectedColor = if (isTrash) TrashTabSelectedColor else Color(0xFF1976D2)
            Box(
                modifier = Modifier
                    .weight(if (isTrash) 0.7f else 1f)
                    .fillMaxHeight()
                    .background(if (selected && isTrash) selectedColor else Color.White)
                    .clickable { onSelectTab(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = when {
                        selected && isTrash -> Color.White
                        selected -> selectedColor
                        else -> Color(0xFF777777)
                    },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(selectedColor)
                    )
                }
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteItemsOverview(
    memos: List<ShoppingMemo>,
    listState: LazyListState,
    leftHandModeEnabled: Boolean,
    microphoneEnabled: Boolean,
    microphoneSessionActive: Boolean,
    onMicrophoneSessionActiveChange: (Boolean) -> Unit,
    keepCompletedItemsInPlace: Boolean,
    voiceScrollDirection: Int,
    voiceScrollSerial: Int,
    onVoiceScrollFinished: () -> Unit,
    onItemDragActiveChange: (Boolean) -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    recentlyRestoredEntryIds: Set<String>,
    restoredMemoId: String?,
    restoredEntryId: String?,
    restoredScrollSerial: Int,
    onClearRestoredEntryHighlight: (String) -> Unit,
    onEntryRestored: (ShoppingMemo, ShoppingEntry) -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val rowBounds = remember { mutableStateMapOf<String, Rect>() }
    val addRowBounds = remember { mutableStateMapOf<String, Rect>() }
    val deleteSwipeThresholdPx = with(density) { 104.dp.toPx() }
    val dragEdgePaddingPx = with(density) { 8.dp.toPx() }
    val fallbackRowHeightPx = with(density) { 56.dp.toPx() }
    var draggingMemoId by remember { mutableStateOf<String?>(null) }
    var draggingEntryId by remember { mutableStateOf<String?>(null) }
    var draggingChecked by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var dragDirectionY by remember { mutableStateOf(0) }
    var autoReorderDirection by remember { mutableStateOf(0) }
    var topClampGapY by remember { mutableStateOf(0f) }
    var bottomClampGapY by remember { mutableStateOf(0f) }
    var layoutCorrectionSerial by remember { mutableStateOf(0) }
    var correctionMemoId by remember { mutableStateOf<String?>(null) }
    var correctionEntryId by remember { mutableStateOf<String?>(null) }
    var correctionVisualTop by remember { mutableStateOf<Float?>(null) }
    var correctionFirstVisibleIndex by remember { mutableStateOf<Int?>(null) }
    var correctionFirstVisibleScrollOffset by remember { mutableStateOf(0) }
    var correctionAccumulateBottomGap by remember { mutableStateOf(true) }
    var correctionPinToTop by remember { mutableStateOf(false) }
    var addingMemoId by remember { mutableStateOf<String?>(null) }
    var addingEntryId by remember { mutableStateOf<String?>(null) }
    var addingFocusSerial by remember { mutableStateOf(0) }
    var selectedFavoriteMemoId by remember { mutableStateOf<String?>(null) }
    var selectedFavoriteEntryId by remember { mutableStateOf<String?>(null) }
    var focusedFavoriteMemoId by remember { mutableStateOf<String?>(null) }
    var focusedFavoriteEntryId by remember { mutableStateOf<String?>(null) }
    val favoriteEntryCursorRanges = remember { mutableStateMapOf<String, TextRange>() }
    var voiceAddedMemoId by remember { mutableStateOf<String?>(null) }
    var voiceAddedEntryId by remember { mutableStateOf<String?>(null) }
    var voiceAddedScrollSerial by remember { mutableStateOf(0) }
    var listBounds by remember { mutableStateOf<Rect?>(null) }
    var favoriteContainerBounds by remember { mutableStateOf<Rect?>(null) }
    val listAtTop = !listState.canScrollBackward
    val imeBottom = with(density) { WindowInsets.ime.getBottom(this).toDp() }
    val imeVisible = imeBottom > 0.dp
    val listBottomPadding = if (imeVisible) {
        (imeBottom - BottomBarHeight + DetailListExtraBottom).coerceAtLeast(DetailListExtraBottom)
    } else {
        DetailListExtraBottom
    }
    val bottomPaddingPx = with(density) { listBottomPadding.roundToPx() }
    val fabBottomPadding = keyboardAnchoredFabBottomPadding(
        view = view,
        containerBounds = favoriteContainerBounds,
        imeVisible = imeVisible,
        density = density
    )
    val selectedFavoriteRowKey = selectedFavoriteMemoId?.let { memoId ->
        selectedFavoriteEntryId?.let { entryId ->
            memos.firstOrNull { it.id == memoId }
                ?.entries
                ?.firstOrNull { it.id == entryId }
                ?.let { entry -> favoriteEntryRowKey(memoId, entry) }
        }
    }
    val microphoneEditingFavoriteRowKey = if (microphoneEnabled && microphoneSessionActive) {
        selectedFavoriteRowKey
    } else {
        null
    }
    val microphoneAddHighlightActive = microphoneEnabled && microphoneSessionActive && microphoneEditingFavoriteRowKey == null

    LaunchedEffect(listAtTop) {
        onListAtTopChanged(listAtTop)
    }

    fun favoriteActiveEntriesForDisplay(memo: ShoppingMemo): List<ShoppingEntry> {
        val active = favoriteVisibleGroup(memo, checked = false).toMutableList()
        val editingEntry = if (memo.id == addingMemoId) {
            memo.entries.firstOrNull { it.id == addingEntryId && !it.checked }
        } else {
            null
        }
        if (editingEntry != null && editingEntry.name.isBlank() && active.none { it.id == editingEntry.id }) {
            active.add(editingEntry)
        }
        return active
    }

    fun favoriteEntriesForDisplay(memo: ShoppingMemo): List<ShoppingEntry> {
        return if (keepCompletedItemsInPlace) {
            memo.entries.toMutableList().also { entries ->
                val editingEntry = if (memo.id == addingMemoId) {
                    memo.entries.firstOrNull { it.id == addingEntryId }
                } else {
                    null
                }
                if (editingEntry != null && editingEntry.name.isBlank() && entries.none { it.id == editingEntry.id }) {
                    entries.add(editingEntry)
                }
            }
        } else {
            favoriteActiveEntriesForDisplay(memo)
        }
    }

    fun beginFavoriteAdd(memo: ShoppingMemo) {
        val entry = requestBlankEntry(memo, keepCompletedItemsInPlace)
        addingMemoId = memo.id
        addingEntryId = entry.id
        selectedFavoriteMemoId = memo.id
        selectedFavoriteEntryId = entry.id
        focusedFavoriteMemoId = memo.id
        focusedFavoriteEntryId = entry.id
        favoriteEntryCursorRanges[favoriteEntryRowKey(memo.id, entry)] = TextRange(entry.name.length)
        addingFocusSerial++
        onChanged()
    }

    fun updateFavoriteEntryName(memo: ShoppingMemo, entry: ShoppingEntry, name: String) {
        entry.name = name
        if (name.isNotBlank()) {
            ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
        }
        onChanged()
    }

    fun clearFavoriteItemEditing(hideKeyboard: Boolean = false) {
        focusManager.clearFocus(force = true)
        if (hideKeyboard) {
            keyboardController?.hide()
        }
        selectedFavoriteMemoId = null
        selectedFavoriteEntryId = null
        focusedFavoriteMemoId = null
        focusedFavoriteEntryId = null
    }

    fun removeUnusedFavoriteBlankEntry(): Boolean {
        val memo = memos.firstOrNull { it.id == addingMemoId } ?: return false
        if (memo.entries.none { it.name.isNotBlank() && (keepCompletedItemsInPlace || !it.checked) }) return false
        val removed = memo.entries.removeAll { it.name.isBlank() }
        if (removed) {
            addingMemoId = null
            addingEntryId = null
            clearFavoriteItemEditing()
        }
        return removed
    }

    fun selectFavoriteEntry(memo: ShoppingMemo, entry: ShoppingEntry) {
        selectedFavoriteMemoId = memo.id
        selectedFavoriteEntryId = entry.id
        focusedFavoriteMemoId = memo.id
        focusedFavoriteEntryId = entry.id
        if (entry.name.isBlank()) {
            addingMemoId = memo.id
            addingEntryId = entry.id
        } else if (addingEntryId != entry.id) {
            addingMemoId = null
            addingEntryId = null
        }
    }

    fun insertFavoriteVoiceTextIntoEntry(memo: ShoppingMemo, entry: ShoppingEntry, text: String) {
        val rowKey = favoriteEntryRowKey(memo.id, entry)
        val range = coerceTextRange(favoriteEntryCursorRanges[rowKey] ?: TextRange(entry.name.length), entry.name.length)
        val start = minOf(range.start, range.end)
        val end = maxOf(range.start, range.end)
        entry.name = entry.name.substring(0, start) + text + entry.name.substring(end)
        favoriteEntryCursorRanges[rowKey] = TextRange(start + text.length)
        ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
        addingMemoId = null
        addingEntryId = null
        clearFavoriteItemEditing(hideKeyboard = true)
    }

    fun visibleFavoriteAddMemoId(): String? {
        val bounds = listBounds ?: return null
        val centerY = bounds.center.y
        return addRowBounds
            .filter { (_, rect) -> rect.bottom > bounds.top && rect.top < bounds.bottom }
            .minByOrNull { (_, rect) -> kotlin.math.abs(rect.center.y - centerY) }
            ?.key
    }

    fun favoriteTopVisibleMemoId(): String? {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        var sectionStartIndex = 0
        memos.forEach { memo ->
            val active = favoriteEntriesForDisplay(memo)
            val done = favoriteVisibleGroup(memo, checked = true)
            val bodyCount = if (active.isEmpty() && (keepCompletedItemsInPlace || done.isEmpty())) {
                1
            } else {
                active.size + if (!keepCompletedItemsInPlace && done.isNotEmpty()) 1 + done.size else 0
            }
            val nextSectionStartIndex = sectionStartIndex + 1 + bodyCount + 1 + 1
            if (firstVisibleIndex < nextSectionStartIndex) {
                return memo.id
            }
            sectionStartIndex = nextSectionStartIndex
        }
        return memos.lastOrNull()?.id
    }

    val highlightedFavoriteAddMemoId = if (microphoneAddHighlightActive) {
        visibleFavoriteAddMemoId()
    } else {
        null
    }
    val favoriteVoiceTargetMemoId = if (microphoneAddHighlightActive) {
        highlightedFavoriteAddMemoId ?: favoriteTopVisibleMemoId()
    } else {
        null
    }
    val latestFavoriteVoiceTargetMemoId by rememberUpdatedState(favoriteVoiceTargetMemoId)

    fun handleFavoriteVoiceText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isBlank()) return
        val selectedMemo = memos.firstOrNull { it.id == selectedFavoriteMemoId }
        val selectedEntry = selectedMemo?.entries?.firstOrNull {
            it.id == selectedFavoriteEntryId && (keepCompletedItemsInPlace || !it.checked)
        }
        if (selectedMemo != null && selectedEntry != null) {
            onClearRestoredEntryHighlight(selectedEntry.id)
            insertFavoriteVoiceTextIntoEntry(selectedMemo, selectedEntry, cleaned)
            onChanged()
            return
        }
        val targetMemoId = latestFavoriteVoiceTargetMemoId ?: addingMemoId
        val memo = memos.firstOrNull { it.id == targetMemoId } ?: memos.firstOrNull() ?: return
        val entry = memo.entries.firstOrNull {
            it.id == addingEntryId && it.name.isBlank() && (keepCompletedItemsInPlace || !it.checked)
        } ?: memo.entries.firstOrNull {
            it.name.isBlank() && (keepCompletedItemsInPlace || !it.checked)
        } ?: requestBlankEntry(memo, keepCompletedItemsInPlace)
        onClearRestoredEntryHighlight(entry.id)
        entry.checked = false
        entry.name = cleaned
        ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
        addingMemoId = memo.id
        addingEntryId = null
        voiceAddedMemoId = memo.id
        voiceAddedEntryId = entry.id
        voiceAddedScrollSerial++
        focusManager.clearFocus()
        onChanged()
    }

    val latestFavoriteVoiceHandler by rememberUpdatedState(::handleFavoriteVoiceText)
    val favoriteSpeechController = remember {
        ContinuousSpeechController(context) { latestFavoriteVoiceHandler(it) }
    }
    DisposableEffect(favoriteSpeechController) {
        onDispose { favoriteSpeechController.destroy() }
    }
    var microphonePermissionRequested by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        microphonePermissionRequested = false
        if (granted) {
            keyboardController?.hide()
            favoriteSpeechController.start()
            onMicrophoneSessionActiveChange(true)
        } else {
            onMicrophoneSessionActiveChange(false)
        }
    }
    fun requestFavoriteMicrophonePermission() {
        if (microphonePermissionRequested) return
        microphonePermissionRequested = true
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(microphoneEnabled, microphoneSessionActive) {
        if (!microphoneEnabled || !microphoneSessionActive) {
            favoriteSpeechController.stop()
            return@LaunchedEffect
        }
        if (!favoriteSpeechController.isRunning) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                keyboardController?.hide()
                favoriteSpeechController.start()
            } else if (!microphonePermissionRequested) {
                requestFavoriteMicrophonePermission()
            }
        }
    }

    LaunchedEffect(voiceScrollSerial, voiceScrollDirection) {
        val direction = voiceScrollDirection
        if (direction == 0) return@LaunchedEffect
        while (true) {
            val canScroll = if (direction < 0) listState.canScrollBackward else listState.canScrollForward
            if (!canScroll) {
                onVoiceScrollFinished()
                break
            }
            val consumed = listState.scrollBy(direction * VoiceScrollStepPx)
            if (kotlin.math.abs(consumed) < 0.5f) {
                onVoiceScrollFinished()
                break
            }
            delay(VoiceScrollDelayMillis)
        }
    }

    DisposableEffect(Unit) {
        onDispose { onItemDragActiveChange(false) }
    }

    fun favoriteRowHeightPx(memo: ShoppingMemo, entry: ShoppingEntry): Float {
        return listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == favoriteEntryRowKey(memo.id, entry) }
            ?.size
            ?.toFloat()
            ?: fallbackRowHeightPx
    }

    fun canDragWithinCurrentFavoriteGroup(memo: ShoppingMemo, entry: ShoppingEntry, direction: Int): Boolean {
        val group = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
        val index = group.indexOfFirst { it.id == entry.id }
        return index >= 0 && group.getOrNull(index + direction) != null
    }

    fun favoriteSectionItemCount(memo: ShoppingMemo): Int {
        val active = favoriteEntriesForDisplay(memo)
        val done = favoriteVisibleGroup(memo, checked = true)
        val bodyCount = if (active.isEmpty() && (keepCompletedItemsInPlace || done.isEmpty())) {
            1
        } else {
            active.size + if (!keepCompletedItemsInPlace && done.isNotEmpty()) 1 + done.size else 0
        }
        return 1 + bodyCount + 1 + 1
    }

    fun favoriteHeaderListIndex(memoIndex: Int): Int {
        var index = 0
        memos.take(memoIndex).forEach { memo ->
            index += favoriteSectionItemCount(memo)
        }
        return index
    }

    fun currentFavoriteSectionIndex(): Int {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        var sectionStartIndex = 0
        memos.forEachIndexed { index, memo ->
            val nextSectionStartIndex = sectionStartIndex + favoriteSectionItemCount(memo)
            if (firstVisibleIndex < nextSectionStartIndex) {
                return index
            }
            sectionStartIndex = nextSectionStartIndex
        }
        return memos.lastIndex.coerceAtLeast(0)
    }

    fun requestFavoriteSectionJump(direction: Int) {
        if (memos.size < 2) return
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        val targetMemoIndex = (currentFavoriteSectionIndex() + direction).coerceIn(0, memos.lastIndex)
        scope.launch {
            listState.scrollToItem(index = favoriteHeaderListIndex(targetMemoIndex))
        }
    }

    fun favoriteListIndexForEntry(memo: ShoppingMemo, entry: ShoppingEntry): Int {
        var index = 0
        memos.forEach { currentMemo ->
            val active = favoriteEntriesForDisplay(currentMemo)
            val done = favoriteVisibleGroup(currentMemo, checked = true)
            if (currentMemo.id == memo.id) {
                return if (keepCompletedItemsInPlace) {
                    val entryIndex = active.indexOfFirst { it.id == entry.id }
                    if (entryIndex < 0) -1 else index + 1 + entryIndex
                } else if (entry.checked) {
                    val doneIndex = done.indexOfFirst { it.id == entry.id }
                    if (doneIndex < 0) -1 else index + 1 + active.size + 1 + 1 + doneIndex
                } else {
                    val activeIndex = active.indexOfFirst { it.id == entry.id }
                    if (activeIndex < 0) -1 else index + 1 + activeIndex
                }
            }
            index += favoriteSectionItemCount(currentMemo)
        }
        return -1
    }

    fun favoriteEditingAlignedOffset(): Int {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val visibleHeight = (viewportHeight - bottomPaddingPx).coerceAtLeast((viewportHeight * 0.35f).roundToInt())
        val targetY = (visibleHeight * 0.30f).roundToInt().coerceAtLeast(0)
        return -targetY
    }

    LaunchedEffect(microphoneEnabled, microphoneSessionActive, selectedFavoriteMemoId, selectedFavoriteEntryId) {
        if (microphoneSessionActive || !microphoneEnabled) return@LaunchedEffect
        val memoId = selectedFavoriteMemoId ?: return@LaunchedEffect
        val entryId = selectedFavoriteEntryId ?: return@LaunchedEffect
        focusedFavoriteMemoId = memoId
        focusedFavoriteEntryId = entryId
        withFrameNanos { }
        delay(80)
        keyboardController?.show()
    }

    LaunchedEffect(
        focusedFavoriteMemoId,
        focusedFavoriteEntryId,
        selectedFavoriteMemoId,
        selectedFavoriteEntryId,
        listBottomPadding,
        memos.size
    ) {
        val memoId = focusedFavoriteMemoId ?: selectedFavoriteMemoId ?: return@LaunchedEffect
        val entryId = focusedFavoriteEntryId ?: selectedFavoriteEntryId ?: return@LaunchedEffect
        val memo = memos.firstOrNull { it.id == memoId } ?: return@LaunchedEffect
        val entry = memo.entries.firstOrNull { it.id == entryId } ?: return@LaunchedEffect
        val targetIndex = favoriteListIndexForEntry(memo, entry)
        if (targetIndex >= 0) {
            withFrameNanos { }
            delay(120)
            listState.animateScrollToItem(targetIndex, scrollOffset = favoriteEditingAlignedOffset())
        }
    }

    LaunchedEffect(restoredScrollSerial, restoredMemoId, restoredEntryId, memos.size) {
        if (restoredScrollSerial <= 0) return@LaunchedEffect
        val targetMemo = memos.firstOrNull { it.id == restoredMemoId } ?: return@LaunchedEffect
        val targetEntry = targetMemo.entries.firstOrNull { it.id == restoredEntryId && !it.checked } ?: return@LaunchedEffect
        withFrameNanos { }
        val targetIndex = favoriteListIndexForEntry(targetMemo, targetEntry)
        if (targetIndex >= 0) {
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            val scrollOffset = -((viewportHeight - bottomPaddingPx - fallbackRowHeightPx.roundToInt()).coerceAtLeast(0))
            listState.animateScrollToItem(targetIndex, scrollOffset = scrollOffset)
        }
    }

    LaunchedEffect(voiceAddedScrollSerial, voiceAddedMemoId, voiceAddedEntryId, memos.size) {
        if (voiceAddedScrollSerial <= 0) return@LaunchedEffect
        val targetMemo = memos.firstOrNull { it.id == voiceAddedMemoId } ?: return@LaunchedEffect
        val targetEntry = targetMemo.entries.firstOrNull { it.id == voiceAddedEntryId } ?: return@LaunchedEffect
        withFrameNanos { }
        val targetIndex = favoriteListIndexForEntry(targetMemo, targetEntry)
        if (targetIndex >= 0) {
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            val scrollOffset = -((viewportHeight - bottomPaddingPx - fallbackRowHeightPx.roundToInt()).coerceAtLeast(0))
            listState.animateScrollToItem(targetIndex, scrollOffset = scrollOffset)
        }
    }

    fun keepFavoriteDraggedEntryVisible(memo: ShoppingMemo, entry: ShoppingEntry, accumulateBottomGap: Boolean = true) {
        val itemInfo = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == favoriteEntryRowKey(memo.id, entry) }
            ?: return
        val viewportStart = listState.layoutInfo.viewportStartOffset
        val autoScrollStart = viewportStart + bottomPaddingPx
        val autoScrollEnd = listState.layoutInfo.viewportEndOffset - bottomPaddingPx
        val dragDisplayStart = viewportStart + dragEdgePaddingPx
        val dragDisplayEnd = listState.layoutInfo.viewportEndOffset - dragEdgePaddingPx
        var visualTop = itemInfo.offset + dragOffsetY
        var visualBottom = visualTop + itemInfo.size
        val edgeBand = itemInfo.size * 0.35f
        val detectedAutoDirection = when {
            canDragWithinCurrentFavoriteGroup(memo, entry, 1) && visualBottom >= autoScrollEnd - edgeBand -> 1
            canDragWithinCurrentFavoriteGroup(memo, entry, -1) && visualTop <= autoScrollStart + edgeBand -> -1
            else -> 0
        }
        autoReorderDirection = when {
            dragDirectionY > 0 && detectedAutoDirection < 0 -> 0
            dragDirectionY < 0 && detectedAutoDirection > 0 -> 0
            else -> detectedAutoDirection
        }
        val lowerLimitOverflow = visualBottom - dragDisplayEnd
        if (lowerLimitOverflow > 0f && (bottomClampGapY > 0f || dragDirectionY >= 0)) {
            dragOffsetY -= lowerLimitOverflow
            topClampGapY = 0f
            if (accumulateBottomGap && dragDirectionY >= 0) {
                bottomClampGapY += lowerLimitOverflow
            }
            visualTop -= lowerLimitOverflow
            visualBottom -= lowerLimitOverflow
        }
        val upperLimitOverflow = viewportStart - visualTop
        val upperDisplayOverflow = dragDisplayStart - visualTop
        if (upperDisplayOverflow > 0f && (topClampGapY > 0f || dragDirectionY <= 0)) {
            dragOffsetY += upperDisplayOverflow
            bottomClampGapY = 0f
            if (accumulateBottomGap && dragDirectionY <= 0) {
                topClampGapY += upperDisplayOverflow
            }
            visualTop += upperDisplayOverflow
            visualBottom += upperDisplayOverflow
        } else if (upperLimitOverflow > 0f && dragDirectionY <= 0) {
            dragOffsetY += upperLimitOverflow
            bottomClampGapY = 0f
            topClampGapY = 0f
            visualTop += upperLimitOverflow
            visualBottom += upperLimitOverflow
        }
        val overflowBottom = visualBottom - dragDisplayEnd
        val overflowTop = viewportStart - visualTop
        val scrollAmount = when {
            canDragWithinCurrentFavoriteGroup(memo, entry, 1) &&
                overflowBottom > 0f &&
                (bottomClampGapY > 0f || dragDirectionY >= 0) -> overflowBottom.coerceAtMost(itemInfo.size / 2f)
            canDragWithinCurrentFavoriteGroup(memo, entry, -1) &&
                overflowTop > 0f &&
                (topClampGapY > 0f || dragDirectionY <= 0) -> -overflowTop.coerceAtMost(itemInfo.size / 2f)
            else -> 0f
        }
        if (scrollAmount != 0f) {
            dragOffsetY += scrollAmount
            scope.launch { listState.scrollBy(scrollAmount) }
        }
    }

    fun clearFavoriteLayoutCorrection() {
        correctionMemoId = null
        correctionEntryId = null
        correctionVisualTop = null
        correctionFirstVisibleIndex = null
        correctionFirstVisibleScrollOffset = 0
        correctionAccumulateBottomGap = true
        correctionPinToTop = false
    }

    fun cancelFavoriteLayoutCorrection() {
        if (correctionMemoId == null) return
        clearFavoriteLayoutCorrection()
        layoutCorrectionSerial += 1
    }

    fun requestFavoriteLayoutCorrection(
        memo: ShoppingMemo,
        entry: ShoppingEntry,
        visualTop: Float? = null,
        firstVisibleIndex: Int? = null,
        firstVisibleScrollOffset: Int = 0,
        accumulateBottomGap: Boolean = true,
        pinToTop: Boolean = false
    ) {
        correctionMemoId = memo.id
        correctionEntryId = entry.id
        correctionVisualTop = visualTop
        correctionFirstVisibleIndex = firstVisibleIndex
        correctionFirstVisibleScrollOffset = firstVisibleScrollOffset
        correctionAccumulateBottomGap = accumulateBottomGap
        correctionPinToTop = pinToTop
        layoutCorrectionSerial += 1
    }

    LaunchedEffect(layoutCorrectionSerial) {
        val expectedSerial = layoutCorrectionSerial
        val memoId = correctionMemoId ?: return@LaunchedEffect
        val entryId = correctionEntryId ?: return@LaunchedEffect
        val visualTop = correctionVisualTop
        val firstVisibleIndex = correctionFirstVisibleIndex
        val firstVisibleScrollOffset = correctionFirstVisibleScrollOffset
        val accumulateBottomGap = correctionAccumulateBottomGap
        val pinToTop = correctionPinToTop

        withFrameNanos { }
        if (
            expectedSerial != layoutCorrectionSerial ||
            draggingMemoId != memoId ||
            draggingEntryId != entryId
        ) {
            return@LaunchedEffect
        }

        val memo = memos.firstOrNull { it.id == memoId } ?: return@LaunchedEffect
        val entry = memo.entries.firstOrNull { it.id == entryId && it.checked == draggingChecked } ?: return@LaunchedEffect

        if (pinToTop) {
            val targetIndex = favoriteListIndexForEntry(memo, entry)
            if (targetIndex < 0) return@LaunchedEffect
            dragOffsetY = 0f
            bottomClampGapY = 0f
            topClampGapY = 0f
            listState.scrollToItem(targetIndex)
            withFrameNanos { }
            if (
                expectedSerial != layoutCorrectionSerial ||
                draggingMemoId != memoId ||
                draggingEntryId != entryId
            ) {
                return@LaunchedEffect
            }
            val group = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
            val groupIndex = group.indexOfFirst { it.id == entry.id }
            autoReorderDirection = if (dragDirectionY < 0 && groupIndex > 0) -1 else 0
            if (expectedSerial == layoutCorrectionSerial) {
                clearFavoriteLayoutCorrection()
            }
            return@LaunchedEffect
        }

        if (firstVisibleIndex != null) {
            listState.scrollToItem(firstVisibleIndex, firstVisibleScrollOffset)
            withFrameNanos { }
            if (
                expectedSerial != layoutCorrectionSerial ||
                draggingMemoId != memoId ||
                draggingEntryId != entryId
            ) {
                return@LaunchedEffect
            }
        }

        if (visualTop != null) {
            val itemInfo = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == favoriteEntryRowKey(memo.id, entry) }
                ?: return@LaunchedEffect
            dragOffsetY = visualTop - itemInfo.offset
        }
        keepFavoriteDraggedEntryVisible(memo, entry, accumulateBottomGap)
        if (expectedSerial == layoutCorrectionSerial) {
            clearFavoriteLayoutCorrection()
        }
    }

    fun keepFavoriteDraggedEntryVisibleAfterLayout(
        memo: ShoppingMemo,
        entry: ShoppingEntry,
        accumulateBottomGap: Boolean = true
    ) {
        requestFavoriteLayoutCorrection(memo = memo, entry = entry, accumulateBottomGap = accumulateBottomGap)
    }

    fun favoriteDraggedVisualTop(memo: ShoppingMemo, entry: ShoppingEntry): Float? {
        val itemInfo = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == favoriteEntryRowKey(memo.id, entry) }
            ?: return null
        return itemInfo.offset + dragOffsetY
    }

    fun keepFavoriteDraggedEntryAtVisualTopAfterLayout(
        memo: ShoppingMemo,
        entry: ShoppingEntry,
        visualTop: Float,
        firstVisibleIndex: Int? = null,
        firstVisibleScrollOffset: Int = 0
    ) {
        requestFavoriteLayoutCorrection(
            memo = memo,
            entry = entry,
            visualTop = visualTop,
            firstVisibleIndex = firstVisibleIndex,
            firstVisibleScrollOffset = firstVisibleScrollOffset
        )
    }

    fun isFavoriteDraggedNearTopEdge(memo: ShoppingMemo, entry: ShoppingEntry): Boolean {
        val itemInfo = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == favoriteEntryRowKey(memo.id, entry) }
            ?: return false
        val viewportStart = listState.layoutInfo.viewportStartOffset
        val autoScrollStart = viewportStart + bottomPaddingPx
        val visualTop = itemInfo.offset + dragOffsetY
        return visualTop <= autoScrollStart + itemInfo.size * 0.35f
    }

    fun pinFavoriteDraggedEntryToTopAfterLayout(memo: ShoppingMemo, entry: ShoppingEntry) {
        requestFavoriteLayoutCorrection(memo = memo, entry = entry, pinToTop = true)
    }

    fun startDrag(memo: ShoppingMemo, entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        cancelFavoriteLayoutCorrection()
        draggingMemoId = memo.id
        draggingEntryId = entry.id
        draggingChecked = entry.checked
        dragOffsetX = 0f
        dragOffsetY = 0f
        dragDirectionY = 0
        autoReorderDirection = 0
        topClampGapY = 0f
        bottomClampGapY = 0f
        onItemDragActiveChange(true)
    }

    fun drag(memo: ShoppingMemo, entry: ShoppingEntry, deltaX: Float, deltaY: Float) {
        if (draggingMemoId != memo.id || draggingEntryId != entry.id || draggingChecked != entry.checked) return
        cancelFavoriteLayoutCorrection()
        val firstVisibleIndexAtDragStart = listState.firstVisibleItemIndex
        val firstVisibleOffsetAtDragStart = listState.firstVisibleItemScrollOffset
        if (deltaY > 0f) {
            dragDirectionY = 1
        } else if (deltaY < 0f) {
            dragDirectionY = -1
        }
        dragOffsetX = (dragOffsetX + deltaX).coerceIn(0f, deleteSwipeThresholdPx * 1.4f)
        val effectiveDeltaY = if (bottomClampGapY > 0f) {
            if (deltaY >= 0f) {
                bottomClampGapY += deltaY
                0f
            } else {
                val releasedGap = minOf(bottomClampGapY, -deltaY)
                bottomClampGapY -= releasedGap
                deltaY + releasedGap
            }
        } else if (topClampGapY > 0f) {
            if (deltaY <= 0f) {
                topClampGapY += -deltaY
                0f
            } else {
                val releasedGap = minOf(topClampGapY, deltaY)
                topClampGapY -= releasedGap
                deltaY - releasedGap
            }
        } else {
            deltaY
        }
        dragOffsetY += effectiveDeltaY

        val targetVisualTop = favoriteDraggedVisualTop(memo, entry)
        var movedDuringDrag = false
        var restoreFirstVisibleAfterMove = false
        while (true) {
            val group = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
            val currentIndex = group.indexOfFirst { it.id == entry.id }
            if (currentIndex < 0) return
            when {
                dragOffsetY > 0f -> {
                    val next = group.getOrNull(currentIndex + 1) ?: break
                    val nextHeight = favoriteRowHeightPx(memo, next)
                    if (dragOffsetY <= nextHeight / 2f) break
                    val entryListIndexBeforeMove = favoriteListIndexForEntry(memo, entry)
                    if (!moveFavoriteEntry(memo, entry, 1, keepCompletedItemsInPlace)) break
                    movedDuringDrag = true
                    if (entryListIndexBeforeMove == firstVisibleIndexAtDragStart) {
                        restoreFirstVisibleAfterMove = true
                    }
                    dragOffsetY -= nextHeight
                    val newListIndex = favoriteListIndexForEntry(memo, entry)
                    if (newListIndex >= 0 && newListIndex <= listState.firstVisibleItemIndex) {
                        scope.launch { listState.scrollToItem(newListIndex) }
                    }
                }
                dragOffsetY < 0f -> {
                    val previous = group.getOrNull(currentIndex - 1) ?: break
                    val previousHeight = favoriteRowHeightPx(memo, previous)
                    if (-dragOffsetY <= previousHeight / 2f) break
                    if (!moveFavoriteEntry(memo, entry, -1, keepCompletedItemsInPlace)) break
                    movedDuringDrag = true
                    dragOffsetY += previousHeight
                    val newListIndex = favoriteListIndexForEntry(memo, entry)
                    if (newListIndex >= 0 && newListIndex <= listState.firstVisibleItemIndex) {
                        scope.launch { listState.scrollToItem(newListIndex) }
                    }
                }
                else -> break
            }
        }
        val group = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
        val currentIndex = group.indexOfFirst { it.id == entry.id }
        if (currentIndex == 0 && dragOffsetY < 0f) {
            dragOffsetY = 0f
        } else if (currentIndex == group.lastIndex && dragOffsetY > 0f) {
            dragOffsetY = 0f
        }
        if (movedDuringDrag) {
            onChanged()
            if (dragDirectionY < 0 && isFavoriteDraggedNearTopEdge(memo, entry)) {
                pinFavoriteDraggedEntryToTopAfterLayout(memo, entry)
            } else if (restoreFirstVisibleAfterMove && targetVisualTop != null) {
                keepFavoriteDraggedEntryAtVisualTopAfterLayout(
                    memo = memo,
                    entry = entry,
                    visualTop = targetVisualTop,
                    firstVisibleIndex = firstVisibleIndexAtDragStart,
                    firstVisibleScrollOffset = firstVisibleOffsetAtDragStart
                )
            } else if (targetVisualTop != null) {
                keepFavoriteDraggedEntryAtVisualTopAfterLayout(memo, entry, targetVisualTop)
            } else {
                keepFavoriteDraggedEntryVisibleAfterLayout(memo, entry)
            }
        } else {
            keepFavoriteDraggedEntryVisible(memo, entry)
        }
    }

    fun endDrag(memo: ShoppingMemo, entry: ShoppingEntry) {
        if (draggingMemoId == memo.id && draggingEntryId == entry.id && dragOffsetX >= deleteSwipeThresholdPx) {
            moveFavoriteEntryToTrash(memo, entry, keepCompletedItemsInPlace)
            onChanged()
        }
        draggingMemoId = null
        draggingEntryId = null
        dragOffsetX = 0f
        dragOffsetY = 0f
        dragDirectionY = 0
        autoReorderDirection = 0
        topClampGapY = 0f
        bottomClampGapY = 0f
        cancelFavoriteLayoutCorrection()
        onItemDragActiveChange(false)
    }

    LaunchedEffect(draggingMemoId, draggingEntryId, autoReorderDirection) {
        while (draggingMemoId != null && draggingEntryId != null && autoReorderDirection != 0) {
            delay(DragAutoReorderDelayMillis)
            val memo = memos.firstOrNull { it.id == draggingMemoId } ?: break
            val entry = memo.entries.firstOrNull { it.id == draggingEntryId && it.checked == draggingChecked } ?: break
            val group = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
            val index = group.indexOfFirst { it.id == entry.id }
            val direction = autoReorderDirection
            val neighbor = group.getOrNull(index + direction)
            if (index < 0 || neighbor == null) {
                autoReorderDirection = 0
                break
            }
            val neighborHeight = favoriteRowHeightPx(memo, neighbor)
            if (moveFavoriteEntry(memo, entry, direction, keepCompletedItemsInPlace)) {
                onChanged()
                if (direction < 0) {
                    pinFavoriteDraggedEntryToTopAfterLayout(memo, entry)
                } else {
                    dragOffsetY -= neighborHeight
                    dragOffsetY += neighborHeight
                    scope.launch { listState.scrollBy(neighborHeight) }
                    keepFavoriteDraggedEntryVisibleAfterLayout(entry = entry, memo = memo, accumulateBottomGap = false)
                }
                val updatedGroup = displayEntryGroup(memo, entry, keepCompletedItemsInPlace)
                val updatedIndex = updatedGroup.indexOfFirst { it.id == entry.id }
                autoReorderDirection = if (updatedGroup.getOrNull(updatedIndex + direction) != null) {
                    direction
                } else {
                    0
                }
            } else {
                autoReorderDirection = 0
                break
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { favoriteContainerBounds = it.boundsInWindow() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { listBounds = it.boundsInWindow() }
                .pointerInput(draggingMemoId, draggingEntryId) {
                    if (draggingEntryId != null) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var totalMove = Offset.Zero
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            totalMove += change.positionChange()
                            if (
                                totalMove.getDistance() > viewConfiguration.touchSlop &&
                                kotlin.math.abs(totalMove.y) > kotlin.math.abs(totalMove.x)
                            ) {
                                clearFavoriteItemEditing(hideKeyboard = true)
                                if (removeUnusedFavoriteBlankEntry()) {
                                    onChanged()
                                }
                                break
                            }
                        }
                    }
                },
            state = listState,
            contentPadding = PaddingValues(bottom = listBottomPadding)
        ) {
            memos.forEach { memo ->
                val activeEntries = favoriteEntriesForDisplay(memo)
                val visibleActiveCount = favoriteVisibleGroup(memo, checked = false).size
                val headerDoneCount = favoriteVisibleGroup(memo, checked = true).size
                val doneEntries = if (keepCompletedItemsInPlace) {
                    emptyList()
                } else {
                    favoriteVisibleGroup(memo, checked = true)
                }
                item(key = favoriteMemoHeaderKey(memo.id)) {
                    FavoriteMemoItemsHeader(memo = memo, activeCount = visibleActiveCount, doneCount = headerDoneCount)
                }
                if (activeEntries.isEmpty() && doneEntries.isEmpty()) {
                    item(key = favoriteEmptyRowKey(memo.id)) {
                        FavoriteEmptyEntryRow()
                    }
                } else {
                    itemsIndexed(
                        items = activeEntries,
                        key = { _, entry -> favoriteEntryRowKey(memo.id, entry) }
                    ) { index, entry ->
                        val rowKey = favoriteEntryRowKey(memo.id, entry)
                        val rowSelected = selectedFavoriteMemoId == memo.id && selectedFavoriteEntryId == entry.id
                        val rowFocused = focusedFavoriteMemoId == memo.id && focusedFavoriteEntryId == entry.id
                        val rowDragging = draggingMemoId == memo.id && draggingEntryId == entry.id
                        val rowDeleteSwiping = rowDragging && dragOffsetX > 0f
                        val displayNumber = if (entry.name.isNotBlank()) {
                            activeEntries.take(index + 1).count { it.name.isNotBlank() }
                        } else {
                            null
                        }
                        ShoppingEntryRow(
                            entry = entry,
                            displayNumber = displayNumber,
                            selected = rowSelected,
                            shouldRequestFocus = rowFocused,
                            cursorRange = favoriteEntryCursorRanges[rowKey],
                            microphoneHighlighted = microphoneEditingFavoriteRowKey == rowKey,
                            isDragging = rowDragging,
                            dragOffsetY = if (rowDragging) dragOffsetY else 0f,
                            isDeleteSwiping = rowDeleteSwiping,
                            deleteSwipeOffsetX = if (rowDragging) dragOffsetX else 0f,
                            editTapSuppressionSerial = 0,
                            recentlyMoved = recentlyRestoredEntryIds.contains(entry.id) &&
                                !rowSelected &&
                                microphoneEditingFavoriteRowKey != rowKey,
                            actionLabel = if (keepCompletedItemsInPlace && entry.checked) stringResource(R.string.action_restore) else stringResource(R.string.completed),
                            modifier = Modifier
                                .onGloballyPositioned { rowBounds[rowKey] = it.boundsInWindow() }
                                .then(
                                    if (rowDragging) {
                                        Modifier.zIndex(1f)
                                    } else {
                                        Modifier.animateItem().zIndex(0f)
                                    }
                                ),
                            onSelect = { selectFavoriteEntry(memo, entry) },
                            onPressStarted = {
                                onClearRestoredEntryHighlight(entry.id)
                                clearFavoriteItemEditing(hideKeyboard = true)
                                if (removeUnusedFavoriteBlankEntry()) {
                                    onChanged()
                                }
                            },
                            onEditRequested = {
                                onClearRestoredEntryHighlight(entry.id)
                                selectFavoriteEntry(memo, entry)
                                favoriteSpeechController.stop()
                            },
                            onNumberClick = {
                                if (entry.name.isNotBlank()) {
                                    entry.colorMark = (entry.colorMark + 1) % 4
                                    clearFavoriteItemEditing()
                                    onChanged()
                                }
                            },
                            onFocusConsumed = {
                                focusedFavoriteMemoId = null
                                focusedFavoriteEntryId = null
                            },
                            onCursorChanged = { range -> favoriteEntryCursorRanges[rowKey] = range },
                            onFocused = {
                                onClearRestoredEntryHighlight(entry.id)
                                selectedFavoriteMemoId = memo.id
                                selectedFavoriteEntryId = entry.id
                                favoriteSpeechController.stop()
                            },
                            onFocusCleared = {
                                if (selectedFavoriteMemoId == memo.id && selectedFavoriteEntryId == entry.id && !microphoneSessionActive) {
                                    selectedFavoriteMemoId = null
                                    selectedFavoriteEntryId = null
                                }
                            },
                            onNameChanged = {
                                onClearRestoredEntryHighlight(entry.id)
                                entry.name = it
                                if (it.isBlank()) {
                                    addingMemoId = memo.id
                                    addingEntryId = entry.id
                                } else if (addingMemoId == memo.id && addingEntryId == entry.id) {
                                    addingMemoId = null
                                    addingEntryId = null
                                }
                                if (
                                    it.isBlank() &&
                                    rowSelected &&
                                    memo.entries.any { other ->
                                        other.id != entry.id &&
                                            other.name.isNotBlank() &&
                                            (keepCompletedItemsInPlace || !other.checked)
                                    }
                                ) {
                                    memo.entries.removeAll { other -> other.id != entry.id && other.name.isBlank() }
                                } else {
                                    ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
                                }
                                onChanged()
                            },
                            onComplete = {
                                onClearRestoredEntryHighlight(entry.id)
                                entry.checked = if (keepCompletedItemsInPlace && entry.checked) false else true
                                addingMemoId = null
                                addingEntryId = null
                                clearFavoriteItemEditing()
                                ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
                                onChanged()
                            },
                            onDelete = {
                                onClearRestoredEntryHighlight(entry.id)
                                moveFavoriteEntryToTrash(memo, entry, keepCompletedItemsInPlace)
                                clearFavoriteItemEditing()
                                onChanged()
                            },
                            onReorderStart = { startDrag(memo, entry) },
                            onReorderDrag = { deltaY -> drag(memo, entry, 0f, deltaY) },
                            onReorderEnd = { endDrag(memo, entry) },
                            onDeleteSwipeStart = {
                                onClearRestoredEntryHighlight(entry.id)
                                startDrag(memo, entry)
                            },
                            onDeleteSwipeDrag = { deltaX, deltaY -> drag(memo, entry, deltaX, deltaY) },
                            onDeleteSwipeEnd = { endDrag(memo, entry) }
                        )
                    }
                }
                item(key = favoriteAddRowKey(memo.id)) {
                    DisposableEffect(memo.id) {
                        onDispose { addRowBounds.remove(memo.id) }
                    }
                    FavoriteAddListItemRow(
                        highlighted = microphoneAddHighlightActive && highlightedFavoriteAddMemoId == memo.id,
                        modifier = Modifier.onGloballyPositioned { addRowBounds[memo.id] = it.boundsInWindow() },
                        onClick = { beginFavoriteAdd(memo) }
                    )
                }
                if (doneEntries.isNotEmpty()) {
                    item(key = favoriteDoneLabelKey(memo.id)) {
                        FavoriteDoneEntryLabel()
                    }
                    items(
                        items = doneEntries,
                        key = { entry -> favoriteEntryRowKey(memo.id, entry) }
                    ) { entry ->
                        FavoriteDoneEntryRow(
                            memoId = memo.id,
                            entry = entry,
                            rowBounds = rowBounds,
                            isDragging = draggingMemoId == memo.id && draggingEntryId == entry.id,
                            dragOffsetX = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetX else 0f,
                            dragOffsetY = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetY else 0f,
                            onRestore = {
                                onClearRestoredEntryHighlight(entry.id)
                                val restored = restoreEntryToActiveBottom(memo, entry, keepCompletedItemsInPlace)
                                clearFavoriteItemEditing()
                                onEntryRestored(memo, restored)
                                onChanged()
                            },
                            onTrash = {
                                moveFavoriteEntryToTrash(memo, entry, keepCompletedItemsInPlace)
                                onChanged()
                            },
                            onDragStart = { startDrag(memo, entry) },
                            onDrag = { deltaX, deltaY -> drag(memo, entry, deltaX, deltaY) },
                            onDragEnd = { endDrag(memo, entry) }
                        )
                    }
                }
                item(key = favoriteSectionSpacerKey(memo.id)) {
                    FavoriteSectionSpacer()
                }
            }
        }

        val showFavoriteJumpButtons = memos.size >= 2
        if ((microphoneEnabled || showFavoriteJumpButtons) && draggingEntryId == null) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .align(physicalBottomSideAlignment(leftHandModeEnabled))
                        .absolutePadding(
                            left = if (leftHandModeEnabled) 12.dp else 0.dp,
                            right = if (leftHandModeEnabled) 0.dp else 12.dp,
                            bottom = fabBottomPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (showFavoriteJumpButtons) {
                        JumpFab(
                            label = if (leftHandModeEnabled) "⇑" else "⇓",
                            sizeScale = HomeOperationScale,
                            onClick = { requestFavoriteSectionJump(if (leftHandModeEnabled) -1 else 1) }
                        )
                    }
                    if (microphoneEnabled) {
                        MicFab(
                            controller = favoriteSpeechController,
                            sizeScale = HomeOperationScale,
                            modifier = Modifier,
                            onClick = {
                                if (favoriteSpeechController.partialText.isNotBlank()) {
                                    favoriteSpeechController.commitPartial()
                                    return@MicFab
                                }
                                if (favoriteSpeechController.isRunning) {
                                    favoriteSpeechController.stop()
                                    onMicrophoneSessionActiveChange(false)
                                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    keyboardController?.hide()
                                    favoriteSpeechController.start()
                                    onMicrophoneSessionActiveChange(true)
                                } else {
                                    requestFavoriteMicrophonePermission()
                                }
                            }
                        )
                    }
                    if (showFavoriteJumpButtons) {
                        JumpFab(
                            label = if (leftHandModeEnabled) "⇓" else "⇑",
                            sizeScale = HomeOperationScale,
                            onClick = { requestFavoriteSectionJump(if (leftHandModeEnabled) 1 else -1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteTrashOverview(
    memos: List<ShoppingMemo>,
    voiceScrollDirection: Int,
    voiceScrollSerial: Int,
    onVoiceScrollFinished: () -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    keepCompletedItemsInPlace: Boolean,
    onEntryRestored: (ShoppingMemo, ShoppingEntry) -> Unit,
    onChanged: () -> Unit
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val actionThresholdPx = with(density) { 104.dp.toPx() }
    var draggingEntryId by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    val listAtTop = !listState.canScrollBackward
    val memosWithTrash = memos.filter { memo -> memo.deletedEntries.any { it.name.isNotBlank() } }
    LaunchedEffect(listAtTop) {
        onListAtTopChanged(listAtTop)
    }

    LaunchedEffect(voiceScrollSerial, voiceScrollDirection) {
        val direction = voiceScrollDirection
        if (direction == 0) return@LaunchedEffect
        while (true) {
            val canScroll = if (direction < 0) listState.canScrollBackward else listState.canScrollForward
            if (!canScroll) {
                onVoiceScrollFinished()
                break
            }
            val consumed = listState.scrollBy(direction * VoiceScrollStepPx)
            if (kotlin.math.abs(consumed) < 0.5f) {
                onVoiceScrollFinished()
                break
            }
            delay(VoiceScrollDelayMillis)
        }
    }

    if (memosWithTrash.isEmpty()) {
        LaunchedEffect(Unit) {
            onListAtTopChanged(true)
        }
        PlaceholderBody(stringResource(R.string.trash_empty))
        return
    }

    fun startDrag(entry: ShoppingEntry) {
        draggingEntryId = entry.id
        dragOffsetX = 0f
    }

    fun drag(entry: ShoppingEntry, deltaX: Float) {
        if (draggingEntryId != entry.id) return
        dragOffsetX = (dragOffsetX + deltaX).coerceIn(-actionThresholdPx * 1.4f, actionThresholdPx * 1.4f)
    }

    fun endDrag(entry: ShoppingEntry, onRestore: () -> Unit, onErase: () -> Unit) {
        val offset = dragOffsetX
        draggingEntryId = null
        dragOffsetX = 0f
        when {
            offset <= -actionThresholdPx -> onRestore()
            offset >= actionThresholdPx -> onErase()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        items(memosWithTrash, key = { "trash-${it.id}" }) { memo ->
            FavoriteMemoTrashSection(
                memo = memo,
                draggingEntryId = draggingEntryId,
                dragOffsetX = dragOffsetX,
                onRestore = { entry ->
                    memo.deletedEntries.remove(entry)
                    val restored = restoreEntryToActiveBottom(memo, entry, keepCompletedItemsInPlace)
                    onEntryRestored(memo, restored)
                    onChanged()
                },
                onErase = { entry ->
                    memo.deletedEntries.remove(entry)
                    onChanged()
                },
                onDragStart = ::startDrag,
                onDrag = ::drag,
                onDragEnd = { entry, restore, erase -> endDrag(entry, restore, erase) }
            )
        }
    }
}

@Composable
private fun FavoriteMemoItemsHeader(
    memo: ShoppingMemo,
    activeCount: Int,
    doneCount: Int
) {
    val totalCount = activeCount + doneCount
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0277BD))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FavoriteMemoIcon(memo = memo)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    localizedMemoDisplayTitle(memo),
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    completedCountSummary(doneCount, totalCount),
                    color = Color(0xFFE3F2FD),
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Divider(color = Color(0xFFE0E0E0))
    }
}

@Composable
private fun FavoriteDoneEntryLabel() {
    Text(
        text = stringResource(R.string.completed_items),
        color = Color(0xFFD32F2F),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@Composable
private fun AddListItemButton(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val sparkleAlpha = rememberSparkleAlpha(highlighted)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .goldAddItemBackground(highlighted)
            .sparkleOverlay(if (highlighted) sparkleAlpha * 0.45f else 0f)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                stringResource(R.string.add_list_item),
                color = if (highlighted) Color(0xFF8A5A00) else Color(0xFF1976D2),
                fontSize = 18.sp,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteAddListItemRow(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AddListItemButton(
        highlighted = highlighted,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun FavoriteEditableEntryRow(
    entry: ShoppingEntry,
    displayNumber: Int?,
    focusRequestSerial: Int,
    onFocusRequestConsumed: () -> Unit,
    onNameChanged: (String) -> Unit,
    onComplete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val sizes = LocalAppFontSizes.current
    var fieldValue by remember(entry.id) {
        mutableStateOf(TextFieldValue(entry.name, TextRange(entry.name.length)))
    }

    LaunchedEffect(entry.name) {
        if (entry.name != fieldValue.text) {
            fieldValue = TextFieldValue(entry.name, TextRange(entry.name.length))
        }
    }

    LaunchedEffect(focusRequestSerial) {
        if (focusRequestSerial > 0) {
            fieldValue = fieldValue.copy(selection = TextRange(fieldValue.text.length))
            focusRequester.requestFocus()
            onFocusRequestConsumed()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(focusedEntryBackground())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberHandle(displayNumber = displayNumber)
        BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onNameChanged(it.text)
            },
            singleLine = false,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .padding(vertical = 8.dp),
            minLines = 1,
            maxLines = 8,
            textStyle = TextStyle(
                fontSize = sizes.listText,
                lineHeight = sizes.listLineHeight,
                color = Color.Black
            ),
            cursorBrush = SolidColor(Color(0xFF1976D2)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            decorationBox = { innerTextField ->
                if (entry.name.isBlank()) {
                    Text(stringResource(R.string.input_empty), color = Color(0xFF9E9E9E), fontSize = sizes.listText)
                }
                innerTextField()
            }
        )
        TextButton(
            enabled = entry.name.isNotBlank(),
            onClick = onComplete,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                stringResource(R.string.completed),
                color = if (entry.name.isBlank()) Color(0xFFBBBBBB) else Color(0xFF1976D2),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteSectionSpacer() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color(0xFFF5F5F5))
    )
}

@Composable
private fun FavoriteMemoItemsSection(
    memo: ShoppingMemo,
    rowBounds: MutableMap<String, Rect>,
    draggingMemoId: String?,
    draggingEntryId: String?,
    dragOffsetX: Float,
    dragOffsetY: Float,
    onComplete: (ShoppingEntry) -> Unit,
    onRestoreDone: (ShoppingEntry) -> Unit,
    onMoveToTrash: (ShoppingEntry) -> Unit,
    onDragStart: (ShoppingMemo, ShoppingEntry) -> Unit,
    onDrag: (ShoppingMemo, ShoppingEntry, Float, Float) -> Unit,
    onDragEnd: (ShoppingMemo, ShoppingEntry) -> Unit
) {
    val activeEntries = memo.entries.filter { it.name.isNotBlank() && !it.checked }
    val doneEntries = memo.entries.filter { it.name.isNotBlank() && it.checked }
    val totalCount = activeEntries.size + doneEntries.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0277BD))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FavoriteMemoIcon(memo = memo)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    localizedMemoDisplayTitle(memo),
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    completedCountSummary(doneEntries.size, totalCount),
                    color = Color(0xFFE3F2FD),
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Divider(color = Color(0xFFE0E0E0))

        if (activeEntries.isEmpty() && doneEntries.isEmpty()) {
            FavoriteEmptyEntryRow()
        } else {
            activeEntries.forEachIndexed { index, entry ->
                key(favoriteEntryRowKey(memo.id, entry)) {
                    FavoriteActiveEntryRow(
                        memoId = memo.id,
                        entry = entry,
                        number = index + 1,
                        rowBounds = rowBounds,
                        isDragging = draggingMemoId == memo.id && draggingEntryId == entry.id,
                        dragOffsetX = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetX else 0f,
                        dragOffsetY = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetY else 0f,
                        recentlyRestored = false,
                        onComplete = { onComplete(entry) },
                        onTrash = { onMoveToTrash(entry) },
                        onDragStart = { onDragStart(memo, entry) },
                        onDrag = { deltaX, deltaY -> onDrag(memo, entry, deltaX, deltaY) },
                        onDragEnd = { onDragEnd(memo, entry) }
                    )
                }
            }
            if (doneEntries.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.completed_items),
                    color = Color(0xFFD32F2F),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
                doneEntries.forEach { entry ->
                    key(favoriteEntryRowKey(memo.id, entry)) {
                        FavoriteDoneEntryRow(
                            memoId = memo.id,
                            entry = entry,
                            rowBounds = rowBounds,
                            isDragging = draggingMemoId == memo.id && draggingEntryId == entry.id,
                            dragOffsetX = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetX else 0f,
                            dragOffsetY = if (draggingMemoId == memo.id && draggingEntryId == entry.id) dragOffsetY else 0f,
                            onRestore = { onRestoreDone(entry) },
                            onTrash = { onMoveToTrash(entry) },
                            onDragStart = { onDragStart(memo, entry) },
                            onDrag = { deltaX, deltaY -> onDrag(memo, entry, deltaX, deltaY) },
                            onDragEnd = { onDragEnd(memo, entry) }
                        )
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFF5F5F5))
        )
    }
}

@Composable
private fun FavoriteMemoTrashSection(
    memo: ShoppingMemo,
    draggingEntryId: String?,
    dragOffsetX: Float,
    onRestore: (ShoppingEntry) -> Unit,
    onErase: (ShoppingEntry) -> Unit,
    onDragStart: (ShoppingEntry) -> Unit,
    onDrag: (ShoppingEntry, Float) -> Unit,
    onDragEnd: (ShoppingEntry, () -> Unit, () -> Unit) -> Unit
) {
    val deletedEntries = memo.deletedEntries.filter { it.name.isNotBlank() }
    if (deletedEntries.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0277BD))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FavoriteMemoIcon(memo = memo)
            Text(
                text = localizedMemoDisplayTitle(memo),
                color = Color.White,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
        }
        Divider(color = Color(0xFFE0E0E0))
        deletedEntries.forEach { entry ->
            DeletedEntryRow(
                entry = entry,
                isDragging = draggingEntryId == entry.id,
                dragOffsetX = if (draggingEntryId == entry.id) dragOffsetX else 0f,
                onRestore = { onRestore(entry) },
                onErase = { onErase(entry) },
                onDragStart = { onDragStart(entry) },
                onDrag = { deltaX -> onDrag(entry, deltaX) },
                onDragEnd = { onDragEnd(entry, { onRestore(entry) }, { onErase(entry) }) }
            )
            Divider(color = Color(0xFFE0E0E0))
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFF5F5F5))
        )
    }
}

@Composable
private fun FavoriteMemoIcon(memo: ShoppingMemo) {
    if (isTemporaryMemo(memo)) {
        MemoNoteIcon(
            sizeScale = 0.72f,
            modifier = Modifier.size(46.dp)
        )
        return
    }
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(Color.White.copy(alpha = 0.94f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        MemoCardImage(memo = memo, modifier = Modifier.fillMaxSize())
    }
}

private fun favoriteVisibleGroup(memo: ShoppingMemo, checked: Boolean): List<ShoppingEntry> {
    return visibleEntryGroup(memo, checked)
}

private fun favoriteEntryRowKey(memoId: String, entry: ShoppingEntry): String {
    return if (entry.checked) {
        "$memoId:done:${entry.id}"
    } else {
        "$memoId:entry:${entry.id}"
    }
}

private fun favoriteMemoHeaderKey(memoId: String): String {
    return "$memoId:header"
}

private fun favoriteEmptyRowKey(memoId: String): String {
    return "$memoId:empty"
}

private fun favoriteDoneLabelKey(memoId: String): String {
    return "$memoId:done-label"
}

private fun favoriteAddRowKey(memoId: String): String {
    return "$memoId:add-list-item"
}

private fun favoriteSectionSpacerKey(memoId: String): String {
    return "$memoId:spacer"
}

private fun moveFavoriteEntry(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    direction: Int,
    keepCompletedItemsInPlace: Boolean = false
): Boolean {
    return moveEntryWithinVisibleGroup(memo, entry, direction, keepCompletedItemsInPlace)
}

private fun moveFavoriteEntryToTrash(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    keepCompletedItemsInPlace: Boolean = false
) {
    if (entry.name.isBlank()) return
    memo.entries.remove(entry)
    memo.deletedEntries.add(entry.copy(checked = false))
    ensureDisplayBlankEntry(memo, keepCompletedItemsInPlace)
}

@Composable
private fun FavoriteActiveEntryRow(
    memoId: String,
    entry: ShoppingEntry,
    number: Int,
    rowBounds: MutableMap<String, Rect>,
    isDragging: Boolean,
    dragOffsetX: Float,
    dragOffsetY: Float,
    recentlyRestored: Boolean,
    actionLabel: String = "",
    onComplete: () -> Unit,
    onTrash: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    val rowKey = favoriteEntryRowKey(memoId, entry)
    val swipeModifier = swipeToTrashModifier(
        key = rowKey,
        enabled = entry.name.isNotBlank(),
        onSwipeStart = onDragStart,
        onSwipeDrag = onDrag,
        onSwipeEnd = onDragEnd
    )
    DisposableEffect(rowKey) {
        onDispose { rowBounds.remove(rowKey) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = dragOffsetX
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 16f else 0f
            }
            .zIndex(if (isDragging) 1f else 0f)
            .recentMoveBackground(
                active = recentlyRestored,
                normalColor = if (entry.checked) CompletedEntryBackground else Color.White
            )
            .onGloballyPositioned { rowBounds[rowKey] = it.boundsInWindow() }
            .then(swipeModifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberHandle(displayNumber = number)
        Text(
            text = entry.name,
            color = if (entry.checked) Color(0xFF777777) else Color.Black,
            fontSize = sizes.listText,
            lineHeight = sizes.listLineHeight,
            fontWeight = if (entry.checked) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        )
        if (isDragging) {
            Text(
                text = "→ 🗑",
                color = Color(0xFFD32F2F),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            TextButton(onClick = onComplete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(actionLabel, color = Color(0xFF1976D2), fontSize = sizes.listAction, fontWeight = FontWeight.Bold)
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteDoneEntryRow(
    memoId: String,
    entry: ShoppingEntry,
    rowBounds: MutableMap<String, Rect>,
    isDragging: Boolean,
    dragOffsetX: Float,
    dragOffsetY: Float,
    onRestore: () -> Unit,
    onTrash: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    val rowKey = favoriteEntryRowKey(memoId, entry)
    val swipeModifier = swipeToTrashModifier(
        key = rowKey,
        enabled = entry.name.isNotBlank(),
        onSwipeStart = onDragStart,
        onSwipeDrag = onDrag,
        onSwipeEnd = onDragEnd
    )
    DisposableEffect(rowKey) {
        onDispose { rowBounds.remove(rowKey) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = dragOffsetX
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 16f else 0f
            }
            .zIndex(if (isDragging) 1f else 0f)
            .background(CompletedEntryBackground)
            .onGloballyPositioned { rowBounds[rowKey] = it.boundsInWindow() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "・",
            color = Color(0xFF888888),
            fontSize = 28.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            text = entry.name,
            color = Color(0xFF777777),
            fontSize = sizes.listText,
            lineHeight = sizes.listLineHeight,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.LineThrough,
            modifier = Modifier
                .weight(1f)
                .then(swipeModifier)
                .padding(vertical = 8.dp)
        )
        if (isDragging) {
            Text(
                text = "→ 🗑",
                color = Color(0xFFD32F2F),
                fontSize = sizes.listAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            TextButton(onClick = onRestore, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(stringResource(R.string.action_restore), color = Color(0xFF1976D2), fontSize = sizes.listAction, fontWeight = FontWeight.Bold)
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteDeletedEntryRow(
    entry: ShoppingEntry,
    onRestore: () -> Unit,
    onErase: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "・",
            color = Color(0xFF888888),
            fontSize = 28.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            text = entry.name,
            color = Color(0xFF666666),
            fontSize = sizes.listText,
            lineHeight = sizes.listLineHeight,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        )
        TextButton(onClick = onRestore, contentPadding = PaddingValues(horizontal = 8.dp)) {
            Text(stringResource(R.string.action_restore), color = Color(0xFF1976D2), fontSize = sizes.listAction, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onErase, contentPadding = PaddingValues(horizontal = 6.dp)) {
            Text(stringResource(R.string.action_erase), color = Color(0xFFD32F2F), fontSize = sizes.listAction, fontWeight = FontWeight.Bold)
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FavoriteEmptyEntryRow() {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.input_empty), color = Color(0xFF9E9E9E), fontSize = sizes.listPlaceholder)
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingsScreen(
    memos: List<ShoppingMemo>,
    memoCount: Int,
    oneHandModeEnabled: Boolean,
    onOneHandModeChanged: (Boolean) -> Unit,
    simpleModeEnabled: Boolean,
    onSimpleModeChanged: (Boolean) -> Unit,
    leftHandModeEnabled: Boolean,
    onLeftHandModeChanged: (Boolean) -> Unit,
    largeFontEnabled: Boolean,
    onLargeFontChanged: (Boolean) -> Unit,
    keepCompletedItemsInPlace: Boolean,
    onKeepCompletedItemsInPlaceChanged: (Boolean) -> Unit,
    microphoneSettings: MicrophoneSettings,
    onMicrophoneSettingsChanged: (MicrophoneSettings) -> Unit,
    privacyOptionsRequired: Boolean,
    onShowPrivacyOptions: () -> Unit,
    onResetOperationHelp: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val latestOnHome by rememberUpdatedState(onHome)
    val microphoneControlsEnabled = !microphoneSettings.disabled
    val sendableMemos = sendableShoppingMemos(memos)
    val emailSelectedMemoIds = remember { mutableStateListOf<String>() }
    var emailSelectionDialogVisible by remember { mutableStateOf(false) }
    var emailAddressDialogVisible by remember { mutableStateOf(false) }
    var emailAddress by remember { mutableStateOf("") }
    var microphonePermissionRequesting by remember { mutableStateOf(false) }
    val microphonePermissionGrantedMessage = stringResource(R.string.mic_permission_granted_toast)
    val microphonePermissionDeniedMessage = stringResource(R.string.mic_permission_denied_toast)
    val microphonePermissionAlreadyGrantedMessage = stringResource(R.string.mic_permission_already_granted_toast)
    val microphonePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        microphonePermissionRequesting = false
        Toast.makeText(
            context,
            if (granted) microphonePermissionGrantedMessage else microphonePermissionDeniedMessage,
            Toast.LENGTH_SHORT
        ).show()
    }
    fun requestMicrophonePermissionFromSettings() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, microphonePermissionAlreadyGrantedMessage, Toast.LENGTH_SHORT).show()
            return
        }
        if (microphonePermissionRequesting) return
        microphonePermissionRequesting = true
        microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    OneHandSettingsFrame(
        oneHandModeEnabled = oneHandModeEnabled,
        listAtTop = !listState.canScrollBackward
    ) { contentModifier ->
        Column(
            contentModifier.homeReturnSwipeOnRelease {
                latestOnHome()
            }
        ) {
            CompactHeader(stringResource(R.string.nav_settings))
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SettingGroup(title = stringResource(R.string.settings_group_mode), backgroundColor = SettingsPaleYellow) {
                        SettingModeRow(
                            title = stringResource(R.string.settings_simple_mode),
                            description = stringResource(R.string.settings_simple_mode_desc),
                            selected = simpleModeEnabled,
                            onClick = { onSimpleModeChanged(true) }
                        )
                        SettingModeRow(
                            title = stringResource(R.string.settings_advanced_mode),
                            description = stringResource(R.string.settings_advanced_mode_desc),
                            selected = !simpleModeEnabled,
                            onClick = { onSimpleModeChanged(false) }
                        )
                    }
                }
                item {
                    SettingGroup(title = stringResource(R.string.settings_group_assist), backgroundColor = SettingsPaleGreen) {
                        SettingToggleRow(
                            title = stringResource(R.string.settings_one_hand),
                            description = stringResource(R.string.settings_one_hand_desc),
                            checked = oneHandModeEnabled,
                            onCheckedChange = onOneHandModeChanged
                        )
                        SettingToggleRow(
                            title = stringResource(R.string.settings_left_hand),
                            description = stringResource(R.string.settings_left_hand_desc),
                            checked = leftHandModeEnabled,
                            onCheckedChange = onLeftHandModeChanged
                        )
                        SettingFontSizeRow(
                            largeFontEnabled = largeFontEnabled,
                            onLargeFontChanged = onLargeFontChanged
                        )
                        SettingToggleRow(
                            title = stringResource(R.string.settings_keep_completed_in_place),
                            description = stringResource(R.string.settings_keep_completed_in_place_desc),
                            checked = keepCompletedItemsInPlace,
                            onCheckedChange = onKeepCompletedItemsInPlaceChanged
                        )
                    }
                }
                item {
                    SettingGroup(title = stringResource(R.string.settings_group_microphone), backgroundColor = SettingsPaleYellow) {
                        SettingToggleRow(
                            title = stringResource(R.string.mic_auto_start),
                            description = stringResource(R.string.mic_auto_start_desc),
                            checked = microphoneSettings.startOnLaunch,
                            enabled = microphoneControlsEnabled,
                            onCheckedChange = { onMicrophoneSettingsChanged(microphoneSettings.copy(startOnLaunch = it)) }
                        )
                        MicrophoneStopTimeoutDropdown(
                            selectedMinutes = microphoneSettings.stopTimeoutMinutes,
                            enabled = microphoneControlsEnabled,
                            onSelected = { onMicrophoneSettingsChanged(microphoneSettings.copy(stopTimeoutMinutes = it)) }
                        )
                        SettingActionRow(
                            title = stringResource(R.string.mic_permission_enable),
                            description = stringResource(R.string.mic_permission_desc),
                            buttonText = stringResource(R.string.mic_permission_button),
                            onClick = ::requestMicrophonePermissionFromSettings
                        )
                        SettingToggleRow(
                            title = stringResource(R.string.mic_disable),
                            description = stringResource(R.string.mic_disable_desc),
                            checked = microphoneSettings.disabled,
                            onCheckedChange = {
                                onMicrophoneSettingsChanged(
                                    microphoneSettings.copy(
                                        disabled = it,
                                        startOnLaunch = if (it) false else microphoneSettings.startOnLaunch
                                    )
                                )
                            }
                        )
                    }
                }
                item {
                    SettingGroup(title = stringResource(R.string.settings_group_email), backgroundColor = SettingsPaleGreen) {
                        SettingActionRow(
                            title = stringResource(R.string.email_send_shopping_list),
                            description = stringResource(R.string.email_send_shopping_list_desc),
                            buttonText = stringResource(R.string.action_select),
                            enabled = sendableMemos.isNotEmpty(),
                            onClick = {
                                emailSelectedMemoIds.clear()
                                emailSelectedMemoIds.addAll(sendableMemos.map { it.id })
                                emailAddress = ""
                                emailSelectionDialogVisible = true
                            }
                        )
                    }
                }
                if (privacyOptionsRequired) {
                    item {
                        SettingGroup(
                            title = stringResource(R.string.settings_group_privacy),
                            backgroundColor = SettingsPaleYellow
                        ) {
                            SettingActionRow(
                                title = stringResource(R.string.settings_privacy_options),
                                description = stringResource(R.string.settings_privacy_options_desc),
                                buttonText = stringResource(R.string.action_open),
                                onClick = onShowPrivacyOptions
                            )
                        }
                    }
                }
                item {
                    SettingGroup(title = stringResource(R.string.settings_group_help), backgroundColor = SettingsPaleGreen) {
                        SettingActionRow(
                            title = stringResource(R.string.help_reset_title),
                            description = stringResource(R.string.help_reset_desc),
                            buttonText = stringResource(R.string.action_reset),
                            onClick = onResetOperationHelp
                        )
                    }
                }
            }
        }
    }
    if (emailSelectionDialogVisible) {
        EmailMemoSelectionDialog(
            memos = sendableMemos,
            selectedMemoIds = emailSelectedMemoIds,
            onDismiss = { emailSelectionDialogVisible = false },
            onNext = {
                emailSelectionDialogVisible = false
                emailAddressDialogVisible = true
            }
        )
    }
    if (emailAddressDialogVisible) {
        val selectedMemos = sendableMemos.filter { it.id in emailSelectedMemoIds }
        EmailAddressDialog(
            address = emailAddress,
            onAddressChange = { emailAddress = it },
            onDismiss = { emailAddressDialogVisible = false },
            onSend = {
                launchShoppingListEmail(context, emailAddress.trim(), selectedMemos)
                emailAddressDialogVisible = false
            }
        )
    }
}

@Composable
private fun EmailMemoSelectionDialog(
    memos: List<ShoppingMemo>,
    selectedMemoIds: MutableList<String>,
    onDismiss: () -> Unit,
    onNext: () -> Unit
) {
    val memoIds = memos.map { it.id }
    val allSelected = memoIds.isNotEmpty() && memoIds.all { it in selectedMemoIds }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.email_select_cards_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                EmailMemoSelectionRow(
                    title = stringResource(R.string.email_all_items),
                    subtitle = stringResource(R.string.email_cards_to_send, memos.size),
                    checked = allSelected,
                    onCheckedChange = { checked ->
                        selectedMemoIds.clear()
                        if (checked) selectedMemoIds.addAll(memoIds)
                    }
                )
                Divider(color = Color(0xFFE0E0E0))
                memos.forEach { memo ->
                    val totalCount = memo.entries.count { it.name.isNotBlank() }
                    val completedCount = memo.entries.count { it.name.isNotBlank() && it.checked }
                    EmailMemoSelectionRow(
                        title = localizedMemoDisplayTitle(memo),
                        subtitle = stringResource(R.string.email_completed_of_total, totalCount, completedCount),
                        checked = memo.id in selectedMemoIds,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (memo.id !in selectedMemoIds) selectedMemoIds.add(memo.id)
                            } else {
                                selectedMemoIds.remove(memo.id)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedMemoIds.isNotEmpty(),
                onClick = onNext
            ) {
                Text(stringResource(R.string.action_next))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun EmailMemoSelectionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                title,
                fontSize = sizes.settingsValue,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = sizes.settingsBody,
                color = Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmailAddressDialog(
    address: String,
    onAddressChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    val context = LocalContext.current
    val chooseGmailAccountTitle = stringResource(R.string.email_choose_gmail_account_title)
    val gmailAccountUnavailableMessage = stringResource(R.string.email_gmail_account_unavailable)
    val accountLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedAddress = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!selectedAddress.isNullOrBlank()) {
                onAddressChange(selectedAddress)
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.email_address_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.email_address_dialog_body))
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            accountLauncher.launch(gmailAccountChooserIntent(chooseGmailAccountTitle))
                        } catch (_: Exception) {
                            Toast.makeText(context, gmailAccountUnavailableMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(stringResource(R.string.email_choose_gmail_account), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text(stringResource(R.string.email_address_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = address.isNotBlank(),
                onClick = onSend
            ) {
                Text(stringResource(R.string.email_open_gmail))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private val MicrophoneStopOptions = listOf(
    0 to R.string.mic_stop_never,
    1 to R.string.mic_stop_1_minute,
    3 to R.string.mic_stop_3_minutes,
    5 to R.string.mic_stop_5_minutes,
    10 to R.string.mic_stop_10_minutes
)

@Composable
private fun OneHandSettingsFrame(
    oneHandModeEnabled: Boolean,
    listAtTop: Boolean,
    gestureBlocked: Boolean = false,
    content: @Composable (Modifier) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var screenHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember { mutableStateOf(0f) }
    var oneHandFlingGeneration by remember { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (screenHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val latestListAtTop by rememberUpdatedState(listAtTop)

    LaunchedEffect(oneHandModeEnabled) {
        if (!oneHandModeEnabled) oneHandOffsetPx = 0f
    }

    LaunchedEffect(gestureBlocked) {
        if (gestureBlocked) oneHandFlingGeneration++
    }

    fun startOneHandFling(initialVelocityY: Float) {
        if (oneHandMaxOffsetPx <= 0f || kotlin.math.abs(initialVelocityY) < 120f) return
        val generation = ++oneHandFlingGeneration
        scope.launch {
            var velocityY = initialVelocityY.coerceIn(-3200f, 3200f)
            var lastFrameNanos = withFrameNanos { it }
            while (generation == oneHandFlingGeneration && kotlin.math.abs(velocityY) > 30f) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastFrameNanos = frameNanos
                val nextOffset = (oneHandOffsetPx + velocityY * deltaSeconds).coerceIn(0f, oneHandMaxOffsetPx)
                if (nextOffset == oneHandOffsetPx) break
                oneHandOffsetPx = nextOffset
                velocityY *= 0.90f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenHeightPx = it.size.height }
    ) {
        if (oneHandModeEnabled && oneHandOffsetPx > 1f) {
            OneHandModeBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(oneHandBackdropHeight)
                    .graphicsLayer {
                        alpha = (0.42f + (oneHandOffsetPx / oneHandMaxOffsetPx.coerceAtLeast(1f)) * 0.46f)
                            .coerceIn(0.42f, 0.88f)
                    }
            )
        }
        content(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = oneHandOffsetPx }
                .pointerInput(oneHandModeEnabled, oneHandMaxOffsetPx, gestureBlocked) {
                    if (!oneHandModeEnabled || gestureBlocked) return@pointerInput
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        oneHandFlingGeneration++
                        val listAtTopWhenGestureStarted = latestListAtTop
                        var totalX = 0f
                        var totalY = 0f
                        var lastVelocityY = 0f
                        var movedContentInGesture = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            val verticalGesture = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.15f
                            val canPullDown = delta.y > 0f && oneHandOffsetPx < oneHandMaxOffsetPx && listAtTopWhenGestureStarted
                            val canPushUp = delta.y < 0f && oneHandOffsetPx > 0f
                            if (verticalGesture && (canPullDown || canPushUp)) {
                                val boostedDeltaY = delta.y * OneHandScrollSpeedMultiplier
                                val nextOffset = (oneHandOffsetPx + boostedDeltaY).coerceIn(0f, oneHandMaxOffsetPx)
                                if (nextOffset != oneHandOffsetPx) {
                                    oneHandOffsetPx = nextOffset
                                    lastVelocityY = boostedDeltaY * 60f
                                    movedContentInGesture = true
                                    change.consume()
                                }
                            }
                        }
                        if (movedContentInGesture) startOneHandFling(lastVelocityY)
                    }
                }
        )
    }
}

@Composable
private fun MicrophoneStopTimeoutDropdown(
    selectedMinutes: Int,
    enabled: Boolean = true,
    onSelected: (Int) -> Unit
) {
    val sizes = LocalAppFontSizes.current
    var expanded by remember { mutableStateOf(false) }
    val selectedLabelResId = MicrophoneStopOptions.firstOrNull { it.first == selectedMinutes }?.second
        ?: MicrophoneStopOptions.first().second
    Column {
        Text(stringResource(R.string.mic_stop_time), fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = true },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFBBD7F6)),
                colors = CardDefaults.cardColors(containerColor = if (enabled) Color(0xFFF7FBFF) else Color(0xFFF2F2F2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(selectedLabelResId), fontSize = sizes.settingsValue, modifier = Modifier.weight(1f), color = Color.Black)
                    Text("▼", fontSize = 14.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                }
            }
            DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                MicrophoneStopOptions.forEach { (minutes, labelResId) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(labelResId), fontSize = sizes.settingsValue) },
                        onClick = {
                            expanded = false
                            onSelected(minutes)
                        }
                    )
                }
            }
        }
        Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun SettingGroup(
    title: String,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, backgroundColor.copy(alpha = 0.75f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(title, fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = Color(0xFF444444))
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingModeRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
        ) {
            Text(title, fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color(0xFF666666), fontSize = sizes.settingsBody, lineHeight = sizes.settingsBodyLineHeight)
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingFontSizeRow(
    largeFontEnabled: Boolean,
    onLargeFontChanged: (Boolean) -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.settings_font_adjust), fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingFontSizeChoice(
                label = stringResource(R.string.font_medium),
                selected = !largeFontEnabled,
                onClick = { onLargeFontChanged(false) },
                modifier = Modifier.weight(1f)
            )
            SettingFontSizeChoice(
                label = stringResource(R.string.font_large),
                selected = largeFontEnabled,
                onClick = { onLargeFontChanged(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun SettingFontSizeChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizes = LocalAppFontSizes.current
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFFE3F2FD) else Color.White,
        border = BorderStroke(1.dp, if (selected) Color(0xFF1976D2) else Color(0xFFD5D5D5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(label, fontSize = sizes.settingsValue, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(title, fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = if (enabled) Color.Black else Color(0xFFAAAAAA))
            Spacer(Modifier.height(4.dp))
            Text(description, color = if (enabled) Color(0xFF666666) else Color(0xFFBBBBBB), fontSize = sizes.settingsBody, lineHeight = sizes.settingsBodyLineHeight)
        }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingActionRow(
    title: String,
    description: String,
    buttonText: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(title, fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, color = if (enabled) Color.Black else Color(0xFFAAAAAA))
            Spacer(Modifier.height(4.dp))
            Text(description, color = if (enabled) Color(0xFF666666) else Color(0xFFBBBBBB), fontSize = sizes.settingsBody, lineHeight = sizes.settingsBodyLineHeight)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(buttonText, color = Color.White, fontSize = sizes.settingsBody, fontWeight = FontWeight.Bold)
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingRow(label: String, value: String) {
    val sizes = LocalAppFontSizes.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = sizes.settingsTitle, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = Color(0xFF666666), fontSize = sizes.settingsValue)
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun MemoMoveEditScreen(
    memos: List<ShoppingMemo>,
    oneHandModeEnabled: Boolean,
    showInstruction: Boolean,
    onDismissInstruction: () -> Unit,
    onChanged: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val memoIdKey = memos.joinToString("|") { it.id }
    var leftMemoId by remember { mutableStateOf<String?>(null) }
    var rightMemoId by remember { mutableStateOf<String?>(null) }
    var selectingSide by remember { mutableStateOf<EditPaneSide?>(null) }
    var rootBounds by remember { mutableStateOf<Rect?>(null) }
    var leftPaneBounds by remember { mutableStateOf<Rect?>(null) }
    var rightPaneBounds by remember { mutableStateOf<Rect?>(null) }
    var draggingEntry by remember { mutableStateOf<EditDraggingEntry?>(null) }
    var draggingBounds by remember { mutableStateOf<Rect?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPoint by remember { mutableStateOf(Offset.Zero) }
    var leftPaneAtTop by remember { mutableStateOf(true) }
    var rightPaneAtTop by remember { mutableStateOf(true) }
    var leftScrollTargetId by remember { mutableStateOf<String?>(null) }
    var rightScrollTargetId by remember { mutableStateOf<String?>(null) }
    var leftScrollSerial by remember { mutableStateOf(0) }
    var rightScrollSerial by remember { mutableStateOf(0) }
    var lastDragOverTarget by remember { mutableStateOf<String?>(null) }
    val editMovedEntryIds = remember { mutableStateListOf<String>() }
    val leftMemo = memos.firstOrNull { it.id == leftMemoId }
    val rightMemo = memos.firstOrNull { it.id == rightMemoId }
    val leftDropActive = draggingEntry?.side == EditPaneSide.Right && leftPaneBounds?.contains(dragPoint) == true
    val rightDropActive = draggingEntry?.side == EditPaneSide.Left && rightPaneBounds?.contains(dragPoint) == true

    LaunchedEffect(memoIdKey) {
        val ids = memos.map { it.id }
        val defaultLeftMemoId = memos.firstOrNull { isTemporaryMemo(it) }?.id ?: ids.firstOrNull()
        if (leftMemoId !in ids) leftMemoId = defaultLeftMemoId
        if (rightMemoId !in ids || rightMemoId == leftMemoId) {
            rightMemoId = ids.firstOrNull { it != leftMemoId } ?: ids.firstOrNull()
        }
    }

    fun selectMemo(side: EditPaneSide, memo: ShoppingMemo) {
        Log.d(
            EditMoveTraceTag,
            "selectMemo side=$side new=${formatEditMemoForTrace(memo)} oldLeft=${formatEditMemoForTrace(leftMemo)} oldRight=${formatEditMemoForTrace(rightMemo)}"
        )
        when (side) {
            EditPaneSide.Left -> {
                if (memo.id == rightMemoId) rightMemoId = leftMemoId
                leftMemoId = memo.id
            }
            EditPaneSide.Right -> {
                if (memo.id == leftMemoId) leftMemoId = rightMemoId
                rightMemoId = memo.id
            }
        }
        selectingSide = null
    }

    fun selectAdjacentMemo(side: EditPaneSide, step: Int) {
        if (memos.size <= 1) {
            Log.d(EditMoveTraceTag, "selectAdjacent ignored side=$side reason=singleMemo")
            return
        }
        val currentMemoId = if (side == EditPaneSide.Left) leftMemoId else rightMemoId
        val otherMemoId = if (side == EditPaneSide.Left) rightMemoId else leftMemoId
        val currentIndex = memos.indexOfFirst { it.id == currentMemoId }.takeIf { it >= 0 } ?: 0
        val skipOtherPane = memos.size > 2
        repeat(memos.size) { offset ->
            val nextIndex = Math.floorMod(currentIndex + step * (offset + 1), memos.size)
            val candidate = memos[nextIndex]
            if (!skipOtherPane || candidate.id != otherMemoId) {
                Log.d(
                    EditMoveTraceTag,
                    "selectAdjacent side=$side step=$step current=${formatEditMemoForTrace(memos.getOrNull(currentIndex))} candidate=${formatEditMemoForTrace(candidate)} other=${otherMemoId?.takeLast(6)}"
                )
                selectMemo(side, candidate)
                return
            }
        }
        Log.d(EditMoveTraceTag, "selectAdjacent ignored side=$side reason=noCandidate")
    }

    fun swapSelectedMemos() {
        Log.d(
            EditMoveTraceTag,
            "swapSelected left=${formatEditMemoForTrace(leftMemo)} right=${formatEditMemoForTrace(rightMemo)}"
        )
        val oldLeft = leftMemoId
        leftMemoId = rightMemoId
        rightMemoId = oldLeft
    }

    fun dragOverTarget(point: Offset): String {
        return when {
            leftPaneBounds?.contains(point) == true -> "Left"
            rightPaneBounds?.contains(point) == true -> "Right"
            else -> "None"
        }
    }

    fun startEntryDrag(side: EditPaneSide, memo: ShoppingMemo, entry: ShoppingEntry, start: Offset, bounds: Rect) {
        draggingEntry = EditDraggingEntry(side, memo, entry)
        draggingBounds = bounds
        dragOffset = Offset.Zero
        dragPoint = Offset(bounds.left + start.x, bounds.top + start.y)
        lastDragOverTarget = dragOverTarget(dragPoint)
        Log.d(
            EditMoveTraceTag,
            "dragStart side=$side memo=${formatEditMemoForTrace(memo)} entry=${formatEditEntryForTrace(entry)} start=${formatOffsetForTrace(start)} point=${formatOffsetForTrace(dragPoint)} rowBounds=${formatRectForTrace(bounds)} leftBounds=${formatRectForTrace(leftPaneBounds)} rightBounds=${formatRectForTrace(rightPaneBounds)} over=$lastDragOverTarget"
        )
    }

    fun dragEntryBy(amount: Offset) {
        dragOffset += amount
        dragPoint += amount
        val over = dragOverTarget(dragPoint)
        if (over != lastDragOverTarget) {
            Log.d(
                EditMoveTraceTag,
                "dragOverChanged entry=${formatEditEntryForTrace(draggingEntry?.entry)} point=${formatOffsetForTrace(dragPoint)} from=$lastDragOverTarget to=$over leftBounds=${formatRectForTrace(leftPaneBounds)} rightBounds=${formatRectForTrace(rightPaneBounds)}"
            )
            lastDragOverTarget = over
        }
    }

    fun cancelEntryDrag(reason: String) {
        Log.d(
            EditMoveTraceTag,
            "dragCancel reason=$reason entry=${formatEditEntryForTrace(draggingEntry?.entry)} point=${formatOffsetForTrace(dragPoint)} over=${dragOverTarget(dragPoint)}"
        )
        draggingEntry = null
        draggingBounds = null
        dragOffset = Offset.Zero
        dragPoint = Offset.Zero
        lastDragOverTarget = null
    }

    fun moveEntryTo(source: ShoppingMemo, target: ShoppingMemo, entry: ShoppingEntry, targetSide: EditPaneSide) {
        if (source.id == target.id) {
            Log.d(EditMoveTraceTag, "moveIgnored reason=sameMemo source=${formatEditMemoForTrace(source)} entry=${formatEditEntryForTrace(entry)}")
            return
        }
        if (entry.name.isBlank()) {
            Log.d(EditMoveTraceTag, "moveIgnored reason=blankEntry source=${formatEditMemoForTrace(source)} target=${formatEditMemoForTrace(target)}")
            return
        }
        if (!source.entries.remove(entry)) {
            Log.d(EditMoveTraceTag, "moveIgnored reason=sourceMissing source=${formatEditMemoForTrace(source)} target=${formatEditMemoForTrace(target)} entry=${formatEditEntryForTrace(entry)}")
            return
        }
        val insertIndex = if (entry.checked) {
            target.entries.size
        } else {
            val firstDoneOrBlank = target.entries.indexOfFirst { it.name.isBlank() || it.checked }
            if (firstDoneOrBlank >= 0) firstDoneOrBlank else target.entries.size
        }
        Log.d(
            EditMoveTraceTag,
            "moveCommit source=${formatEditMemoForTrace(source)} target=${formatEditMemoForTrace(target)} targetSide=$targetSide entry=${formatEditEntryForTrace(entry)} insertIndex=$insertIndex"
        )
        target.entries.add(insertIndex, entry)
        ensureDisplayBlankEntry(source)
        ensureDisplayBlankEntry(target)
        editMovedEntryIds.remove(entry.id)
        editMovedEntryIds.add(entry.id)
        when (targetSide) {
            EditPaneSide.Left -> {
                leftScrollTargetId = entry.id
                leftScrollSerial++
            }
            EditPaneSide.Right -> {
                rightScrollTargetId = entry.id
                rightScrollSerial++
            }
        }
        onChanged()
    }

    fun finishDrag() {
        val dragging = draggingEntry
        if (dragging != null) {
            val leftHit = leftPaneBounds?.contains(dragPoint) == true
            val rightHit = rightPaneBounds?.contains(dragPoint) == true
            val target = when {
                dragging.side == EditPaneSide.Left && rightHit -> rightMemo
                dragging.side == EditPaneSide.Right && leftHit -> leftMemo
                else -> null
            }
            Log.d(
                EditMoveTraceTag,
                "dragEnd side=${dragging.side} entry=${formatEditEntryForTrace(dragging.entry)} point=${formatOffsetForTrace(dragPoint)} leftHit=$leftHit rightHit=$rightHit target=${formatEditMemoForTrace(target)} leftBounds=${formatRectForTrace(leftPaneBounds)} rightBounds=${formatRectForTrace(rightPaneBounds)}"
            )
            if (target != null) {
                val targetSide = if (dragging.side == EditPaneSide.Left) EditPaneSide.Right else EditPaneSide.Left
                moveEntryTo(dragging.memo, target, dragging.entry, targetSide)
            }
        } else {
            Log.d(EditMoveTraceTag, "dragEnd ignored reason=noDragging")
        }
        draggingEntry = null
        draggingBounds = null
        dragOffset = Offset.Zero
        dragPoint = Offset.Zero
        lastDragOverTarget = null
    }

    OneHandSettingsFrame(
        oneHandModeEnabled = oneHandModeEnabled,
        listAtTop = leftPaneAtTop && rightPaneAtTop
    ) { contentModifier ->
        Box(
            modifier = contentModifier
            .background(Color(0xFFFAFCFF))
            .onGloballyPositioned { rootBounds = it.boundsInWindow() }
        ) {
        Column(Modifier.fillMaxSize()) {
            CompactHeader(stringResource(R.string.nav_card_change))
            if (memos.isEmpty()) {
                PlaceholderBody(stringResource(R.string.no_movable_cards))
            } else {
                if (showInstruction) {
                    EditInstructionPanel(onDismiss = onDismissInstruction)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    EditCenterSwapControl(
                        onClick = ::swapSelectedMemos,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(3f)
                    )
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditMemoPane(
                            label = stringResource(R.string.current_card),
                            labelColor = Color(0xFF43A047),
                            headerColor = Color(0xFFE8F5E9),
                            side = EditPaneSide.Left,
                            memo = leftMemo,
                            dropActive = leftDropActive,
                            draggingEntryId = draggingEntry?.entry?.id,
                            highlightedEntryIds = editMovedEntryIds.toSet(),
                            scrollTargetEntryId = leftScrollTargetId,
                            scrollRequestSerial = leftScrollSerial,
                            swipeEnabled = draggingEntry == null,
                            onHeaderClick = { selectingSide = EditPaneSide.Left },
                            onSwipeLeft = { selectAdjacentMemo(EditPaneSide.Left, 1) },
                            onSwipeRight = { selectAdjacentMemo(EditPaneSide.Left, -1) },
                            onListAtTopChanged = { leftPaneAtTop = it },
                            onPanePositioned = { leftPaneBounds = it },
                            onEntryDragStart = { memo, entry, start, bounds -> startEntryDrag(EditPaneSide.Left, memo, entry, start, bounds) },
                            onEntryDrag = ::dragEntryBy,
                            onEntryDragEnd = ::finishDrag,
                            onEntryDragCancel = { cancelEntryDrag("leftGestureCancel") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        EditMemoPane(
                            label = stringResource(R.string.destination_card),
                            labelColor = Color(0xFF7E57C2),
                            headerColor = Color(0xFFF1E3FF),
                            side = EditPaneSide.Right,
                            memo = rightMemo,
                            dropActive = rightDropActive,
                            draggingEntryId = draggingEntry?.entry?.id,
                            highlightedEntryIds = editMovedEntryIds.toSet(),
                            scrollTargetEntryId = rightScrollTargetId,
                            scrollRequestSerial = rightScrollSerial,
                            swipeEnabled = draggingEntry == null,
                            onHeaderClick = { selectingSide = EditPaneSide.Right },
                            onSwipeLeft = { selectAdjacentMemo(EditPaneSide.Right, 1) },
                            onSwipeRight = { selectAdjacentMemo(EditPaneSide.Right, -1) },
                            onListAtTopChanged = { rightPaneAtTop = it },
                            onPanePositioned = { rightPaneBounds = it },
                            onEntryDragStart = { memo, entry, start, bounds -> startEntryDrag(EditPaneSide.Right, memo, entry, start, bounds) },
                            onEntryDrag = ::dragEntryBy,
                            onEntryDragEnd = ::finishDrag,
                            onEntryDragCancel = { cancelEntryDrag("rightGestureCancel") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val dragging = draggingEntry
        val bounds = draggingBounds
        val root = rootBounds
        if (dragging != null && bounds != null && root != null) {
            EditFloatingEntryRow(
                entry = dragging.entry,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = bounds.left - root.left + dragOffset.x
                        translationY = bounds.top - root.top + dragOffset.y
                        shadowElevation = 22f
                    }
                    .width(with(density) { bounds.width.toDp() })
                    .zIndex(30f)
            )
        }
    }
    }

    selectingSide?.let { side ->
        MemoSelectDialog(
            side = side,
            memos = memos,
            currentMemoId = if (side == EditPaneSide.Left) leftMemoId else rightMemoId,
            onSelect = { selectMemo(side, it) },
            onDismiss = { selectingSide = null }
        )
    }
}

@Composable
private fun EditInstructionPanel(onDismiss: () -> Unit) {
    val sizes = LocalAppFontSizes.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF8E1),
        border = BorderStroke(1.dp, Color(0xFFFFECB3))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡", fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.edit_instruction_text),
                    color = Color(0xFF3E2D22),
                    fontSize = sizes.editHelpBody,
                    lineHeight = sizes.editHelpLineHeight,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.size(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("×", color = Color(0xFF795548), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditMemoPane(
    label: String,
    labelColor: Color,
    headerColor: Color,
    side: EditPaneSide,
    memo: ShoppingMemo?,
    dropActive: Boolean,
    draggingEntryId: String?,
    highlightedEntryIds: Set<String>,
    scrollTargetEntryId: String?,
    scrollRequestSerial: Int,
    swipeEnabled: Boolean,
    onHeaderClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    onPanePositioned: (Rect) -> Unit,
    onEntryDragStart: (ShoppingMemo, ShoppingEntry, Offset, Rect) -> Unit,
    onEntryDrag: (Offset) -> Unit,
    onEntryDragEnd: () -> Unit,
    onEntryDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sizes = LocalAppFontSizes.current
    val swipeThresholdPx = with(density) { 56.dp.toPx() }
    val latestSwipeEnabled by rememberUpdatedState(swipeEnabled)
    val latestOnSwipeLeft by rememberUpdatedState(onSwipeLeft)
    val latestOnSwipeRight by rememberUpdatedState(onSwipeRight)
    val listState = rememberLazyListState()
    val listAtTop = !listState.canScrollBackward

    LaunchedEffect(memo?.id, listAtTop) {
        onListAtTopChanged(listAtTop)
    }

    Column(modifier = modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = labelColor,
            modifier = Modifier.padding(bottom = 7.dp)
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(memo?.id, side, swipeThresholdPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var totalMove = Offset.Zero
                        var switched = false
                        var horizontalIntent = false
                        var verticalIntent = false
                        var blockedByLongPress = false
                        var loggedLongPressBlock = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            totalMove = change.position - down.position
                            val elapsedMillis = change.uptimeMillis - down.uptimeMillis
                            if (!horizontalIntent && elapsedMillis >= viewConfiguration.longPressTimeoutMillis) {
                                blockedByLongPress = true
                                if (!loggedLongPressBlock) {
                                    Log.d(
                                        EditMoveTraceTag,
                                        "swipeBlockedLongPress side=$side memo=${formatEditMemoForTrace(memo)} move=${formatOffsetForTrace(totalMove)} elapsed=$elapsedMillis"
                                    )
                                    loggedLongPressBlock = true
                                }
                            }
                            val absX = kotlin.math.abs(totalMove.x)
                            val absY = kotlin.math.abs(totalMove.y)
                            if (!horizontalIntent && !verticalIntent && !blockedByLongPress) {
                                when {
                                    absX > viewConfiguration.touchSlop && absX > absY * 0.8f -> {
                                        horizontalIntent = true
                                        change.consume()
                                        Log.d(
                                            EditMoveTraceTag,
                                            "swipeHorizontalIntent side=$side memo=${formatEditMemoForTrace(memo)} move=${formatOffsetForTrace(totalMove)} elapsed=$elapsedMillis"
                                        )
                                    }
                                    absY > viewConfiguration.touchSlop && absY > absX * 1.35f -> {
                                        verticalIntent = true
                                        Log.d(
                                            EditMoveTraceTag,
                                            "swipeVerticalIntent side=$side memo=${formatEditMemoForTrace(memo)} move=${formatOffsetForTrace(totalMove)} elapsed=$elapsedMillis"
                                        )
                                    }
                                }
                            }
                            if (horizontalIntent && latestSwipeEnabled) {
                                change.consume()
                            }
                            if (
                                !switched &&
                                latestSwipeEnabled &&
                                horizontalIntent &&
                                !verticalIntent &&
                                !blockedByLongPress &&
                                absX > swipeThresholdPx
                            ) {
                                switched = true
                                change.consume()
                                val direction = if (totalMove.x < 0f) "Left" else "Right"
                                Log.d(
                                    EditMoveTraceTag,
                                    "swipeSwitch side=$side direction=$direction memo=${formatEditMemoForTrace(memo)} move=${formatOffsetForTrace(totalMove)} elapsed=$elapsedMillis"
                                )
                                if (totalMove.x < 0f) {
                                    latestOnSwipeLeft()
                                } else {
                                    latestOnSwipeRight()
                                }
                            }
                            if (!change.pressed) break
                        }
                        if (!switched && totalMove.getDistance() > viewConfiguration.touchSlop) {
                            Log.d(
                                EditMoveTraceTag,
                                "swipeNoSwitch side=$side memo=${formatEditMemoForTrace(memo)} move=${formatOffsetForTrace(totalMove)} enabled=$latestSwipeEnabled horizontal=$horizontalIntent vertical=$verticalIntent longPress=$blockedByLongPress"
                            )
                        }
                    }
                }
                .onGloballyPositioned { onPanePositioned(it.boundsInWindow()) },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(if (dropActive) 3.dp else 1.dp, if (dropActive) labelColor else Color(0xFFD5D5D5)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (dropActive) 9.dp else 3.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (memo == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_card), color = Color(0xFF999999), fontSize = 14.sp)
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    EditMemoHeader(
                        memo = memo,
                        color = headerColor,
                        accent = labelColor,
                        onClick = onHeaderClick
                    )
                    Divider(color = Color(0xFFE0E0E0))
                    val entries = memo.entries.filter { it.name.isNotBlank() }
                    LaunchedEffect(scrollRequestSerial, scrollTargetEntryId, entries.size, memo.id) {
                        if (scrollRequestSerial <= 0) return@LaunchedEffect
                        val targetId = scrollTargetEntryId ?: return@LaunchedEffect
                        val index = entries.indexOfFirst { it.id == targetId }
                        if (index < 0) return@LaunchedEffect
                        listState.animateScrollToItem(index)
                        withFrameNanos { }
                        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == targetId }
                        if (itemInfo != null) {
                            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                            val bottomGap = with(density) { 8.dp.roundToPx() }
                            val scrollOffset = -((viewportHeight - itemInfo.size - bottomGap).coerceAtLeast(0))
                            listState.animateScrollToItem(index, scrollOffset = scrollOffset)
                        }
                    }
                    if (entries.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_items), color = Color(0xFF999999), fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(entries, key = { it.id }) { entry ->
                                EditMoveEntryRow(
                                    memo = memo,
                                    entry = entry,
                                    side = side,
                                    isDragging = draggingEntryId == entry.id,
                                    recentlyMoved = highlightedEntryIds.contains(entry.id),
                                    onDragStart = onEntryDragStart,
                                    onDrag = onEntryDrag,
                                    onDragEnd = onEntryDragEnd,
                                    onDragCancel = onEntryDragCancel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditMemoHeader(
    memo: ShoppingMemo,
    color: Color,
    accent: Color,
    onClick: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    val doneCount = memo.entries.count { it.checked && it.name.isNotBlank() }
    val totalCount = memo.entries.count { it.name.isNotBlank() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            MemoCardImage(
                memo = memo,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.86f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (memo.favorite) "★" else "☆",
                    color = if (memo.favorite) Color(0xFFFFA000) else Color(0xFF666666),
                    fontSize = 28.sp,
                    lineHeight = 28.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    localizedMemoDisplayTitle(memo),
                    color = accent,
                    fontSize = sizes.editEntry,
                    lineHeight = sizes.editEntryLineHeight,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    completedCountSummary(doneCount, totalCount),
                    color = accent,
                    fontSize = sizes.editHelpBody,
                    lineHeight = sizes.editHelpLineHeight,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("↔", color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EditMoveEntryRow(
    memo: ShoppingMemo,
    entry: ShoppingEntry,
    side: EditPaneSide,
    isDragging: Boolean,
    recentlyMoved: Boolean,
    onDragStart: (ShoppingMemo, ShoppingEntry, Offset, Rect) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val sizes = LocalAppFontSizes.current
    var rowBounds by remember { mutableStateOf<Rect?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isDragging) 0.18f else 1f }
            .recentMoveBackground(
                active = recentlyMoved,
                normalColor = if (entry.checked) CompletedEntryBackground else Color.White
            )
            .onGloballyPositioned { rowBounds = it.boundsInWindow() }
            .pointerInput(memo.id, entry.id, side) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { start ->
                        val bounds = rowBounds
                        Log.d(
                            EditMoveTraceTag,
                            "rowLongPressDragStart side=$side memo=${formatEditMemoForTrace(memo)} entry=${formatEditEntryForTrace(entry)} start=${formatOffsetForTrace(start)} rowBounds=${formatRectForTrace(bounds)}"
                        )
                        if (bounds != null) {
                            onDragStart(memo, entry, start, bounds)
                        } else {
                            Log.d(
                                EditMoveTraceTag,
                                "rowLongPressDragStartIgnored reason=noBounds side=$side entry=${formatEditEntryForTrace(entry)}"
                            )
                        }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = {
                        Log.d(EditMoveTraceTag, "rowLongPressDragEnd side=$side entry=${formatEditEntryForTrace(entry)}")
                        onDragEnd()
                    },
                    onDragCancel = {
                        Log.d(EditMoveTraceTag, "rowLongPressDragCancel side=$side entry=${formatEditEntryForTrace(entry)}")
                        onDragCancel()
                    }
                )
            }
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("・", color = Color(0xFF555555), fontSize = 16.sp)
        Spacer(Modifier.width(5.dp))
        Text(
            text = entry.name,
            color = if (entry.checked) Color(0xFF777777) else Color.Black,
            fontSize = sizes.editEntry,
            lineHeight = sizes.editEntryLineHeight,
            textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text("≡", color = Color(0xFFBDBDBD), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
    Divider(color = Color(0xFFE8E8E8))
}

@Composable
private fun EditFloatingEntryRow(
    entry: ShoppingEntry,
    modifier: Modifier = Modifier
) {
    val sizes = LocalAppFontSizes.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = if (entry.checked) CompletedEntryBackground else Color.White,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, Color(0xFFBBD7F6))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("・", color = Color(0xFF1976D2), fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                entry.name,
                color = if (entry.checked) Color(0xFF777777) else Color.Black,
                textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None,
                fontSize = sizes.editEntry,
                lineHeight = sizes.editEntryLineHeight,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text("↔", color = Color(0xFF43A047), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EditCenterSwapControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = Color(0xFF7CB342)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("↔", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MemoSelectDialog(
    side: EditPaneSide,
    memos: List<ShoppingMemo>,
    currentMemoId: String?,
    onSelect: (ShoppingMemo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        title = {
            Text(
                if (side == EditPaneSide.Left) {
                    stringResource(R.string.select_left_card)
                } else {
                    stringResource(R.string.select_right_card)
                },
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                items(memos, key = { it.id }) { memo ->
                    val selected = memo.id == currentMemoId
                    val doneCount = memo.entries.count { it.checked && it.name.isNotBlank() }
                    val totalCount = memo.entries.count { it.name.isNotBlank() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(memo) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onSelect(memo) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFFD32F2F),
                                unselectedColor = Color(0xFF666666)
                            )
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                localizedMemoDisplayTitle(memo, R.string.title_input),
                                color = if (selected) Color(0xFFD32F2F) else Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                completedCountSummary(doneCount, totalCount),
                                color = if (selected) Color(0xFFD32F2F) else Color(0xFF666666),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = Color(0xFF1976D2))
            }
        }
    )
}

private fun supportAdTodayKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

@Composable
private fun AdsSupportScreen(
    canRequestAds: Boolean,
    onVideoOverlayVisibleChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val todayKey = remember { supportAdTodayKey() }
    var watchedDate by remember { mutableStateOf(loadSupportAdWatchDate(context)) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var rewardedLoading by remember { mutableStateOf(false) }
    val watchedToday = watchedDate == todayKey

    fun completeRewardedAd() {
        saveSupportAdWatchDate(context, todayKey)
        watchedDate = todayKey
    }

    fun requestRewardedAdLoad() {
        if (!canRequestAds || rewardedLoading || watchedToday) return
        rewardedLoading = true
        loadSupportRewardedAd(
            context = context,
            onLoaded = { ad ->
                rewardedAd = ad
                rewardedLoading = false
            },
            onFailed = {
                rewardedAd = null
                rewardedLoading = false
            }
        )
    }

    fun showRewardedAd() {
        if (watchedToday) return
        val activity = context.findActivity()
        val ad = rewardedAd
        if (activity == null || ad == null) {
            requestRewardedAdLoad()
            return
        }
        rewardedAd = null
        onVideoOverlayVisibleChange(true)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onVideoOverlayVisibleChange(false)
                requestRewardedAdLoad()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onVideoOverlayVisibleChange(false)
                requestRewardedAdLoad()
            }
        }
        ad.show(activity) {
            completeRewardedAd()
        }
    }

    LaunchedEffect(watchedToday, canRequestAds) {
        if (canRequestAds && !watchedToday) requestRewardedAdLoad()
    }

    DisposableEffect(Unit) {
        onDispose { onVideoOverlayVisibleChange(false) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF8))
    ) {
        Column(Modifier.fillMaxSize()) {
            AdMobBannerAd(
                canRequestAds = canRequestAds,
                modifier = Modifier.fillMaxWidth()
            )
            SupportDeveloperWebView(
                watchedToday = watchedToday,
                onWatchAd = {
                    if (!watchedToday) showRewardedAd()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun SupportDeveloperWebView(
    watchedToday: Boolean,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestOnWatchAd by rememberUpdatedState(onWatchAd)
    val html = remember(context, watchedToday) {
        supportDeveloperHtml(context, watchedToday)
    }

    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        factory = { viewContext ->
            WebView(viewContext).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.allowFileAccess = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        return handleSupportDeveloperLink(url, latestOnWatchAd)
                    }

                    @Deprecated("Deprecated in Android")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return handleSupportDeveloperLink(url, latestOnWatchAd)
                    }
                }
                tag = html
                loadDataWithBaseURL(
                    "file:///android_asset/support_ads/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(
                    "file:///android_asset/support_ads/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}

private fun handleSupportDeveloperLink(
    url: String?,
    onWatchAd: () -> Unit
): Boolean {
    if (url == "kaimonomemo://watch-ad") {
        onWatchAd()
        return true
    }
    return false
}

private data class SupportDeveloperText(
    val htmlLang: String,
    val htmlDir: String,
    val title: String,
    val thanks: String,
    val messageBefore: String,
    val messageEmphasis: String,
    val messageAfter: String,
    val footerTitle: String,
    val footerBody: String,
    val watchButton: String,
    val watchButtonDone: String,
    val watchDuration: String,
    val watchLimit: String
)

private fun supportDeveloperText(context: Context): SupportDeveloperText {
    return SupportDeveloperText(
        htmlLang = context.getString(R.string.support_ad_html_lang),
        htmlDir = context.getString(R.string.support_ad_html_dir),
        title = context.getString(R.string.support_ad_title),
        thanks = context.getString(R.string.support_ad_thanks),
        messageBefore = context.getString(R.string.support_ad_message_before),
        messageEmphasis = context.getString(R.string.support_ad_message_emphasis),
        messageAfter = context.getString(R.string.support_ad_message_after),
        footerTitle = context.getString(R.string.support_ad_footer_title),
        footerBody = context.getString(R.string.support_ad_footer_body),
        watchButton = context.getString(R.string.support_ad_watch_button),
        watchButtonDone = context.getString(R.string.support_ad_watch_button_done),
        watchDuration = context.getString(R.string.support_ad_watch_duration),
        watchLimit = context.getString(R.string.support_ad_watch_limit)
    )
}

private fun supportDeveloperHtml(
    context: Context,
    watchedToday: Boolean
): String {
    val text = supportDeveloperText(context)
    val watchLabel = if (watchedToday) text.watchButtonDone else text.watchButton
    val watchTag = if (watchedToday) {
        """<div class="watch-button disabled">"""
    } else {
        """<a class="watch-button" href="kaimonomemo://watch-ad">"""
    }
    val watchCloseTag = if (watchedToday) "</div>" else "</a>"

    return """
        <!doctype html>
        <html lang="${text.htmlLang.htmlEscaped()}" dir="${text.htmlDir.htmlEscaped()}">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=4.0, user-scalable=yes">
          <style>
            html, body {
              margin: 0;
              min-height: 100%;
              background: #fffdf8;
              color: #3c2b1f;
              font-family: -apple-system, BlinkMacSystemFont, "Noto Sans JP", "Hiragino Sans", "Yu Gothic", sans-serif;
            }
            * { box-sizing: border-box; }
            .page {
              min-height: 100vh;
              display: flex;
              flex-direction: column;
              background: #fffdf8;
            }
            .title {
              padding: 10px 10px 8px;
              text-align: center;
              color: #193f1e;
              font-size: clamp(24px, 7.4vw, 36px);
              font-weight: 900;
              letter-spacing: 0.02em;
              background: linear-gradient(180deg, #f2fae8 0%, #eef8df 100%);
              border-bottom: 1px solid #e3ecd7;
            }
            .wave {
              height: 18px;
              background:
                radial-gradient(70% 24px at 25% -2px, transparent 98%, #fffdf8 100%),
                radial-gradient(70% 24px at 75% -2px, transparent 98%, #fffdf8 100%),
                #eef8df;
            }
            .content {
              flex: 1;
              display: flex;
              flex-direction: column;
              justify-content: space-evenly;
              gap: clamp(10px, 2.2vh, 20px);
              padding: 4px 10px 12px;
            }
            .thanks {
              margin-top: 2px;
              text-align: center;
              font-size: clamp(16px, 4.9vw, 25px);
              font-weight: 900;
              transform: rotate(-4deg);
            }
            .hero {
              display: flex;
              justify-content: center;
              align-items: center;
              min-height: 126px;
            }
            .hero-image {
              display: block;
              width: min(100%, 560px);
              height: auto;
            }
            .message {
              margin: 0 auto;
              max-width: 620px;
              text-align: center;
              font-size: clamp(16px, 4.4vw, 23px);
              line-height: 1.45;
              font-weight: 800;
            }
            .message .emphasis { color: #f36d76; }
            .thanks-card {
              display: grid;
              grid-template-columns: 74px 1fr;
              align-items: center;
              gap: 12px;
              width: 100%;
              max-width: 680px;
              margin: 0 auto;
              padding: 8px 12px;
              border: 2px solid #ececec;
              border-radius: 22px;
              background: rgba(255,255,255,0.92);
              box-shadow: 0 1px 4px rgba(85, 68, 44, 0.06);
            }
            .cat-image {
              width: 72px;
              height: 58px;
              object-fit: contain;
            }
            .thanks-title {
              font-size: clamp(15px, 4.1vw, 20px);
              font-weight: 900;
            }
            .thanks-body {
              margin-top: 2px;
              font-size: clamp(13px, 3.6vw, 17px);
              line-height: 1.35;
              font-weight: 650;
            }
            .watch-button {
              min-height: 66px;
              width: 100%;
              max-width: 700px;
              margin: 0 auto;
              display: grid;
              grid-template-columns: 64px 1fr;
              align-items: center;
              gap: 10px;
              padding: 9px 20px;
              border-radius: 28px;
              color: #fff;
              text-decoration: none;
              background: linear-gradient(180deg, #8ac174 0%, #63a852 70%, #568f47 100%);
              box-shadow: inset 0 -6px 0 rgba(34, 96, 35, 0.20), 0 3px 8px rgba(77, 117, 58, 0.24);
            }
            .watch-button.disabled {
              background: linear-gradient(180deg, #9eb593 0%, #78936f 70%, #6f8566 100%);
            }
            .play-icon {
              width: 52px;
              height: 40px;
              border: 6px solid rgba(255,255,255,0.95);
              border-radius: 4px;
              position: relative;
            }
            .play-icon:before,
            .play-icon:after {
              content: "";
              position: absolute;
              top: -6px;
              bottom: -6px;
              width: 6px;
              background: rgba(255,255,255,0.92);
            }
            .play-icon:before { left: 8px; }
            .play-icon:after { right: 8px; }
            .play-triangle {
              position: absolute;
              left: 17px;
              top: 6px;
              width: 0;
              height: 0;
              border-top: 9px solid transparent;
              border-bottom: 9px solid transparent;
              border-left: 14px solid rgba(255,255,255,0.95);
            }
            .watch-text {
              text-align: center;
              font-size: clamp(18px, 5.1vw, 27px);
              line-height: 1.2;
              font-weight: 900;
              text-shadow: 0 1px 1px rgba(43, 91, 38, 0.35);
            }
            .duration {
              display: inline-block;
              margin-top: 1px;
              padding: 1px 12px 2px;
              border-radius: 999px;
              background: rgba(78, 139, 65, 0.38);
              font-size: 0.72em;
            }
            .limit {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 5px;
              color: #5f6b5b;
              font-size: clamp(12px, 3.5vw, 16px);
              font-weight: 700;
            }
            .shield {
              display: inline-flex;
              align-items: center;
              justify-content: center;
              width: 18px;
              height: 18px;
              border-radius: 50%;
              background: #77aa61;
              color: #fff;
              font-size: 12px;
              font-weight: 900;
            }
            @media (max-height: 640px) {
              .content { gap: 8px; padding-bottom: 8px; }
              .hero { min-height: 92px; }
              .hero-image { width: min(100%, 430px); }
              .thanks-card { padding: 7px 10px; grid-template-columns: 62px 1fr; }
              .cat-image { width: 60px; height: 48px; }
              .watch-button { min-height: 56px; border-radius: 22px; }
              .play-icon { transform: scale(0.88); transform-origin: left center; }
            }
          </style>
        </head>
        <body>
          <div class="page">
            <header class="title">${text.title.htmlEscaped()}</header>
            <div class="wave"></div>
            <main class="content">
              <div class="thanks">${text.thanks.htmlEscaped()}</div>
              <section class="hero" aria-hidden="true">
                <img class="hero-image" src="support_heart_hands.png" alt="">
              </section>
              <p class="message">
                ${text.messageBefore.htmlEscaped()}<br>
                <span class="emphasis">${text.messageEmphasis.htmlEscaped()}</span>${text.messageAfter.htmlEscaped()}
              </p>
              <section class="thanks-card">
                <img class="cat-image" src="support_cat.png" alt="">
                <div>
                  <div class="thanks-title">${text.footerTitle.htmlEscaped()}</div>
                  <div class="thanks-body">${text.footerBody.htmlEscaped()}</div>
                </div>
              </section>
              $watchTag
                <span class="play-icon"><span class="play-triangle"></span></span>
                <span class="watch-text">
                  ${watchLabel.htmlEscaped()}
                  <span class="duration">${text.watchDuration.htmlEscaped()}</span>
                </span>
              $watchCloseTag
              <div class="limit"><span class="shield">&#x2713;</span>${text.watchLimit.htmlEscaped()}</div>
            </main>
          </div>
        </body>
        </html>
    """.trimIndent()
}

private fun String.htmlEscaped(): String {
    return buildString(length) {
        this@htmlEscaped.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(char)
            }
        }
    }
}

@Composable
private fun AdMobBannerAd(
    canRequestAds: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adView = remember(context, canRequestAds) {
        if (!canRequestAds) return@remember null
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdMobBannerAdUnitId
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) {
        onDispose { adView?.destroy() }
    }
    Surface(
        modifier = modifier.height(54.dp),
        color = Color(0xFFF6F7F2),
        border = BorderStroke(1.dp, Color(0xFFD7E6CC))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (adView != null) {
                AndroidView(
                    modifier = Modifier
                        .width(320.dp)
                        .height(50.dp),
                    factory = { adView }
                )
            }
        }
    }
}

@Composable
private fun AdMobDummyUnitCard(
    title: String,
    adUnitId: String,
    status: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFFDE7),
        border = BorderStroke(1.dp, Color(0xFFFFE082))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Ad unit: $adUnitId", color = Color(0xFF5D4037), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(status, color = Color(0xFF6D4C41), fontSize = 13.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun DummyBannerAd(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(54.dp),
        color = Color(0xFFF6F7F2),
        border = BorderStroke(1.dp, Color(0xFFD7E6CC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = LocalContext.current.getString(R.string.support_ad_banner_dummy),
                color = Color(0xFF6F7F66),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SupportDeveloperPanel(
    watchedToday: Boolean,
    onWatchAd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE7EAD9))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SupportHeader()
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.support_ad_thanks),
                color = Color(0xFF4A2E1F),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            SupportHeartIllustration()
            Spacer(Modifier.height(12.dp))
            SupportMessage()
            Spacer(Modifier.height(18.dp))
            SupportBenefitPanel()
            Spacer(Modifier.height(18.dp))
            SupportWatchButton(watchedToday = watchedToday, onClick = onWatchAd)
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (watchedToday) {
                    stringResource(R.string.support_ad_watch_button_done)
                } else {
                    stringResource(R.string.support_ad_watch_limit)
                },
                color = Color(0xFF666666),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            SupportThanksBox()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SupportHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFFEAF5DE))
            val wave = Path().apply {
                moveTo(0f, size.height * 0.72f)
                cubicTo(
                    size.width * 0.22f, size.height * 0.90f,
                    size.width * 0.32f, size.height * 0.52f,
                    size.width * 0.50f, size.height * 0.72f
                )
                cubicTo(
                    size.width * 0.68f, size.height * 0.92f,
                    size.width * 0.78f, size.height * 0.52f,
                    size.width, size.height * 0.72f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wave, Color.White)
        }
        Text(
            text = stringResource(R.string.support_ad_title),
            modifier = Modifier.align(Alignment.Center),
            color = Color(0xFF1B5E20),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SupportHeartIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.48f)
            drawCircle(Color(0xFFFFEBEE), radius = size.minDimension * 0.30f, center = center)
            drawLine(Color(0xFFF9A825), Offset(size.width * 0.18f, size.height * 0.25f), Offset(size.width * 0.26f, size.height * 0.34f), strokeWidth = 5f)
            drawLine(Color(0xFFF9A825), Offset(size.width * 0.82f, size.height * 0.25f), Offset(size.width * 0.74f, size.height * 0.34f), strokeWidth = 5f)
            drawLine(Color(0xFF7CB342), Offset(size.width * 0.30f, size.height * 0.60f), Offset(size.width * 0.38f, size.height * 0.56f), strokeWidth = 4f)
            drawLine(Color(0xFF7CB342), Offset(size.width * 0.70f, size.height * 0.60f), Offset(size.width * 0.62f, size.height * 0.56f), strokeWidth = 4f)
        }
        Text(
            text = "❤",
            color = Color(0xFFEF6C73),
            fontSize = 86.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(88.dp)
        ) {
            Text("╲", color = Color(0xFFF4B183), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text("╱", color = Color(0xFFF4B183), fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SupportMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.support_ad_cost_before),
            color = Color(0xFF3E2D22),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.support_ad_cost_emphasis), color = Color(0xFF558B2F), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.support_ad_cost_after), color = Color(0xFF3E2D22), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.support_ad_message_before),
            color = Color(0xFF3E2D22),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.support_ad_message_emphasis), color = Color(0xFFFF6F7F), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.support_ad_message_after), color = Color(0xFF3E2D22), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SupportBenefitPanel() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFDF5),
        border = BorderStroke(1.dp, Color(0xFFF1DFA4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF66A84F),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.support_ad_benefits_title),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SupportBenefitItem(
                    icon = "🌱",
                    title = stringResource(R.string.support_ad_benefit_continue_title),
                    body = stringResource(R.string.support_ad_benefit_continue_body),
                    titleColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                SupportBenefitItem(
                    icon = "❤",
                    title = stringResource(R.string.support_ad_benefit_better_title),
                    body = stringResource(R.string.support_ad_benefit_better_body),
                    titleColor = Color(0xFFEF6C73),
                    modifier = Modifier.weight(1f)
                )
                SupportBenefitItem(
                    icon = "🎁",
                    title = stringResource(R.string.support_ad_benefit_features_title),
                    body = stringResource(R.string.support_ad_benefit_features_body),
                    titleColor = Color(0xFFF57F17),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SupportBenefitItem(
    icon: String,
    title: String,
    body: String,
    titleColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = titleColor.copy(alpha = 0.13f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 30.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text(body, color = Color(0xFF333333), fontSize = 12.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SupportWatchButton(
    watchedToday: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !watchedToday,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(76.dp),
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF66A84F),
            disabledContainerColor = Color(0xFFB0B0B0),
            contentColor = Color.White,
            disabledContentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("▶", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(14.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (watchedToday) {
                        stringResource(R.string.support_ad_watch_button_done)
                    } else {
                        stringResource(R.string.support_ad_watch_button)
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                if (!watchedToday) {
                    Text(stringResource(R.string.support_ad_watch_duration), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SupportThanksBox() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE6E6E6))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = Color(0xFFFFF7E6)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("❤", color = Color(0xFFEF6C73), fontSize = 34.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.support_ad_footer_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2D22))
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.support_ad_footer_body),
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun DummyRewardedVideoOverlay(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(5) }
    val remainingText = stringResource(R.string.rewarded_remaining, remainingSeconds)

    BackHandler { onDismiss() }
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(50f)
            .background(Color(0xEE000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.rewarded_dummy_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B2A1B),
                border = BorderStroke(1.dp, Color(0xFF9CCC65))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▶", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.rewarded_playing), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.rewarded_placeholder), color = Color(0xFFDDEEDD), fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (remainingSeconds > 0) remainingText else stringResource(R.string.rewarded_ready),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onComplete,
                enabled = remainingSeconds == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF66A84F),
                    disabledContainerColor = Color(0xFF616161)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(stringResource(R.string.rewarded_complete), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = Color.White)
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, body: String) {
    Column(Modifier.fillMaxSize()) {
        Header(title = title)
        PlaceholderBody(body)
    }
}

@Composable
private fun PlaceholderBody(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(text, color = Color(0xFF888888), modifier = Modifier.padding(top = 48.dp))
    }
}

@Composable
private fun CompactHeader(
    title: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeTitleHeaderHeight)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FittingSingleLineText(
                text = title,
                color = Color(0xFF1976D2),
                maxFontSize = 24.sp,
                minFontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        Divider(color = Color(0xFFE0E0E0))
    }
}

@Composable
private fun Header(
    title: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FittingSingleLineText(
                text = title,
                color = Color(0xFF1976D2),
                maxFontSize = 32.sp,
                minFontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        Divider(color = Color(0xFFE0E0E0))
    }
}

@Composable
private fun BottomIconBar(
    screen: Screen,
    simpleModeEnabled: Boolean,
    onHome: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onFavorite: () -> Unit,
    onAds: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .navigationBarsPadding()
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomIcon(R.drawable.icon_ad, R.drawable.nav_selected_ad, stringResource(R.string.nav_ads), screen == Screen.Ads, onAds)
            if (!simpleModeEnabled) {
                BottomIcon(R.drawable.icon_edit, R.drawable.nav_selected_edit, stringResource(R.string.nav_card_change), screen == Screen.Edit, onEdit)
            }
            BottomIcon(R.drawable.icon_home_nav, R.drawable.nav_selected_home, stringResource(R.string.nav_home), screen == Screen.Home, onHome)
            if (!simpleModeEnabled) {
                BottomIcon(R.drawable.icon_favorite_nav, R.drawable.nav_selected_favorite, stringResource(R.string.nav_favorites), screen == Screen.Favorites, onFavorite)
            }
            BottomIcon(R.drawable.icon_settings_nav, R.drawable.nav_selected_settings, stringResource(R.string.nav_settings), screen == Screen.Settings, onSettings)
        }
    }
}

@Composable
private fun InitialModeSelectionDialog(
    onSelect: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.initial_mode_title),
                    color = Color.Black,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.initial_mode_question),
                    color = Color.Black,
                    fontSize = 23.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.initial_mode_note),
                    color = Color.Black,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onSelect(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(stringResource(R.string.settings_simple_mode), color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = { onSelect(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(stringResource(R.string.settings_advanced_mode), color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomIcon(
    drawableId: Int,
    selectedDrawableId: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val normalText = Color(0xFF444444)
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(if (selected) selectedDrawableId else drawableId),
            contentDescription = label,
            modifier = Modifier
                .size(if (selected) 58.dp else 30.dp)
                .graphicsLayer { alpha = if (selected) 1f else 0.72f },
            contentScale = ContentScale.Fit
        )
        if (!selected) {
            Spacer(Modifier.height(1.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                color = normalText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
