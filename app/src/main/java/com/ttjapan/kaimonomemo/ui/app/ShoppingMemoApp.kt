package com.ttjapan.kaimonomemo.ui.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.ttjapan.kaimonomemo.R
import com.ttjapan.kaimonomemo.data.assignDefaultTitleIfBlank
import com.ttjapan.kaimonomemo.data.loadMemos
import com.ttjapan.kaimonomemo.data.loadOneHandModeEnabled
import com.ttjapan.kaimonomemo.data.loadSimpleModeEnabled
import com.ttjapan.kaimonomemo.data.saveMemos
import com.ttjapan.kaimonomemo.data.saveOneHandModeEnabled
import com.ttjapan.kaimonomemo.data.saveSimpleModeEnabled
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import com.ttjapan.kaimonomemo.voice.ContinuousSpeechController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

private enum class Screen {
    Home,
    Detail,
    PatternPicker,
    Map,
    Settings,
    Favorites
}

private enum class VoiceTarget {
    Title,
    Item
}

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
private const val OneHandMaxOffsetRatio = 0.46f
private const val OneHandScrollSpeedMultiplier = 1.5f
private const val TemporaryMemoId = "temporary-shopping-memo"
private const val TemporaryMemoTitle = "テンポラリ"
private const val SimpleTemporaryMemoTitle = "お買い物リスト"
private val TrashTabSelectedColor = Color(0xFFE91E63)

private fun isTemporaryMemo(memo: ShoppingMemo): Boolean = memo.id == TemporaryMemoId

private fun temporaryTitleForMode(simpleModeEnabled: Boolean): String {
    return if (simpleModeEnabled) SimpleTemporaryMemoTitle else TemporaryMemoTitle
}

private fun isTemporaryDefaultTitle(title: String): Boolean {
    return title.isBlank() || title == TemporaryMemoTitle || title == SimpleTemporaryMemoTitle
}

private fun applyTemporaryTitleForMode(memo: ShoppingMemo, simpleModeEnabled: Boolean) {
    if (isTemporaryDefaultTitle(memo.title)) {
        memo.title = temporaryTitleForMode(simpleModeEnabled)
    }
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

private fun ensureDisplayBlankEntry(memo: ShoppingMemo): ShoppingEntry? {
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

private fun requestBlankEntry(memo: ShoppingMemo): ShoppingEntry {
    val active = memo.entries.filter { it.name.isNotBlank() && !it.checked }
    val done = memo.entries.filter { it.name.isNotBlank() && it.checked }
    val blank = memo.entries.firstOrNull { it.name.isBlank() } ?: ShoppingEntry(name = "")
    memo.entries.clear()
    memo.entries.addAll(active)
    memo.entries.add(blank)
    memo.entries.addAll(done)
    return blank
}

@Composable
fun ShoppingMemoApp() {
    val context = LocalContext.current
    val initialSimpleModeEnabled = remember { loadSimpleModeEnabled(context) }
    val memos = remember {
        mutableStateListOf<ShoppingMemo>().also { state ->
            val loaded = loadMemos(context).toMutableList()
            val temporaryMemo = loaded.firstOrNull { it.id == TemporaryMemoId }
            if (temporaryMemo == null) {
                loaded.add(0, ShoppingMemo(id = TemporaryMemoId, title = temporaryTitleForMode(initialSimpleModeEnabled)))
            } else {
                applyTemporaryTitleForMode(temporaryMemo, initialSimpleModeEnabled)
            }
            state.addAll(loaded)
        }
    }
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedMemoId by remember { mutableStateOf<String?>(null) }
    var imageEditingMemoId by remember { mutableStateOf<String?>(null) }
    var oneHandModeEnabled by remember { mutableStateOf(loadOneHandModeEnabled(context)) }
    var simpleModeEnabled by remember { mutableStateOf(initialSimpleModeEnabled) }
    var homeTopScrollRequest by remember { mutableStateOf(0) }
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
        var activeIndex = 0
        memos.indices.forEach { index ->
            val memo = memos[index]
            if (!isTemporaryMemo(memo) && !memo.trashed) {
                memos[index] = orderedActive[activeIndex]
                activeIndex++
            }
        }
        persist()
    }

    fun updateOneHandMode(enabled: Boolean) {
        oneHandModeEnabled = enabled
        saveOneHandModeEnabled(context, enabled)
    }
    fun updateSimpleMode(enabled: Boolean) {
        simpleModeEnabled = enabled
        applyTemporaryTitleForMode(temporaryMemo, enabled)
        saveSimpleModeEnabled(context, enabled)
        persist()
    }
    fun openMemo(memo: ShoppingMemo) {
        selectedMemoId = memo.id
        currentScreen = Screen.Detail
    }
    fun finishDetail() {
        selectedMemo?.let { memo ->
            pruneBlankEntries(memo)
            if (isTemporaryMemo(memo)) {
                applyTemporaryTitleForMode(memo, simpleModeEnabled)
            } else if (memo.title.isBlank() && memo.entries.none { it.name.isNotBlank() } && memo.deletedEntries.none { it.name.isNotBlank() }) {
                memos.remove(memo)
                selectedMemoId = null
            } else {
                assignDefaultTitleIfBlank(memo, memos)
            }
            recentlyMovedEntryIds.removeAll { id -> memo.entries.any { it.id == id } }
        }
        persist()
        currentScreen = Screen.Home
    }
    fun addMemo() {
        val memo = ShoppingMemo(entries = listOf(ShoppingEntry(name = "")))
        val insertIndex = (memos.indexOfFirst { isTemporaryMemo(it) } + 1)
            .coerceIn(0, memos.size)
        memos.add(insertIndex, memo)
        homeTopScrollRequest++
        openMemo(memo)
    }
    fun finishTemporaryHome() {
        pruneBlankEntries(temporaryMemo)
        applyTemporaryTitleForMode(temporaryMemo, simpleModeEnabled)
        persist()
        currentScreen = Screen.Home
    }

    BackHandler(enabled = currentScreen == Screen.Home) {
        // ホームでの戻るジェスチャーではアプリを終了しない。
    }
    BackHandler(enabled = currentScreen == Screen.Detail) {
        finishDetail()
    }
    BackHandler(enabled = currentScreen == Screen.PatternPicker) {
        currentScreen = Screen.Home
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomIconBar(
                screen = currentScreen,
                onHome = {
                    if (currentScreen == Screen.Detail) finishDetail() else currentScreen = Screen.Home
                },
                onMap = { currentScreen = Screen.Map },
                onSettings = { currentScreen = Screen.Settings },
                onFavorite = { currentScreen = Screen.Favorites },
                onOwner = {}
            )
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
                            recentlyMovedEntryIds = emptySet(),
                            onFinish = ::finishTemporaryHome,
                            onChanged = ::persist
                        )
                    } else {
                        AdvancedHomeScreen(
                            memos = activeMemos,
                            trashedMemos = trashedMemos,
                            temporaryMemo = temporaryMemo,
                            oneHandModeEnabled = oneHandModeEnabled,
                            homeTopScrollRequest = homeTopScrollRequest,
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
                            recentlyMovedEntryIds = recentlyMovedEntryIds.toSet(),
                            onFinish = ::finishDetail,
                            onChanged = ::persist
                        )
                    }
                }
                Screen.Map -> PlaceholderScreen("地図", "地図機能はありません")
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
                                persist()
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }
                Screen.Settings -> SettingsScreen(
                    memoCount = activeMemos.size,
                    oneHandModeEnabled = oneHandModeEnabled,
                    onOneHandModeChanged = ::updateOneHandMode,
                    simpleModeEnabled = simpleModeEnabled,
                    onSimpleModeChanged = ::updateSimpleMode
                )
                Screen.Favorites -> FavoritesScreen(
                    memos = activeMemos.filter { it.favorite },
                    onOpenMemo = ::openMemo,
                    onToggleFavorite = {
                        it.favorite = !it.favorite
                        persist()
                    }
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
    homeTopScrollRequest: Int,
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
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tabs = listOf("アイテム", "ゴミ箱")
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
    var imageChangeBounds by remember { mutableStateOf<Rect?>(null) }
    var cardTrashBounds by remember { mutableStateOf<Rect?>(null) }
    var pendingImageMemo by remember { mutableStateOf<ShoppingMemo?>(null) }
    var homeBounds by remember { mutableStateOf<Rect?>(null) }
    var homeControlBounds by remember { mutableStateOf<Rect?>(null) }
    var temporaryListBounds by remember { mutableStateOf<Rect?>(null) }
    var draggingTemporaryEntry by remember { mutableStateOf<ShoppingEntry?>(null) }
    var temporaryDragPoint by remember { mutableStateOf(Offset.Zero) }
    var homeHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember { mutableStateOf(0f) }
    var oneHandFlingGeneration by remember { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (homeHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
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
    val sourceMemoIds = memos.map { it.id }
    val orderedHomeMemos = run {
        val byId = memos.associateBy { it.id }
        val ordered = homeMemoOrderIds.mapNotNull { byId[it] }
        if (ordered.size == memos.size) ordered else memos
    }

    LaunchedEffect(sourceMemoIds, draggingId) {
        if (draggingId == null) {
            homeMemoOrderIds.clear()
            homeMemoOrderIds.addAll(sourceMemoIds)
        } else {
            homeMemoOrderIds.removeAll { it !in sourceMemoIds }
            sourceMemoIds.forEach { id ->
                if (id !in homeMemoOrderIds) homeMemoOrderIds.add(id)
            }
        }
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
        homeMemoOrderIds.clear()
        homeMemoOrderIds.addAll(sourceMemoIds)
        homeMemoOrderChanged = false
    }

    fun finishHomeMemoDrag() {
        val memo = memos.firstOrNull { it.id == draggingId }
        val revealTopAfterDrop = memo != null && homeMemoOrderChanged && homeMemoOrderIds.firstOrNull() == memo.id
        when {
            memo != null && imageChangeBounds?.contains(dragPoint) == true -> pendingImageMemo = memo
            memo != null && cardTrashBounds?.contains(dragPoint) == true -> onDeleteMemo(memo)
            memo != null && homeMemoOrderChanged -> onApplyMemoOrder(homeMemoOrderIds.toList())
        }
        draggingId = null
        dragOffset = Offset.Zero
        draggedCardBounds = null
        homeMemoOrderChanged = false
        if (revealTopAfterDrop) {
            scope.launch { activeGridState.scrollToItem(0, 0) }
        }
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
        if (!oneHandModeEnabled || oneHandMaxOffsetPx <= 0f) return 0f
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

        val gridAtTopBefore = activeGridState.firstVisibleItemIndex == 0 &&
            activeGridState.firstVisibleItemScrollOffset == 0
        val gridConsumed = -activeGridState.dispatchRawDelta(-delta)
        val remaining = delta - gridConsumed
        val gridAtTopAfter = activeGridState.firstVisibleItemIndex == 0 &&
            activeGridState.firstVisibleItemScrollOffset == 0
        if (remaining > 0f && gridAtTopBefore && gridAtTopAfter) {
            return gridConsumed + applyOneHandControlScroll(remaining)
        }
        return gridConsumed
    }

    LaunchedEffect(oneHandModeEnabled) {
        if (!oneHandModeEnabled) oneHandOffsetPx = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                homeBounds = it.boundsInWindow()
                homeHeightPx = it.size.height
            }
            .pointerInput(pagerState.currentPage, dragActive) {
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
                            if (pagerState.currentPage == 0 && totalX < -DetailBackSwipeThresholdPx) {
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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = oneHandOffsetPx }
                .pointerInput(oneHandModeEnabled, oneHandMaxOffsetPx, dragActive) {
                    if (!oneHandModeEnabled || dragActive) return@pointerInput
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
            Header(title = "買い物メモ")
            HomeTabRow(
                tabs = tabs,
                selectedPage = pagerState.currentPage,
                onSelect = { page -> scope.launch { pagerState.animateScrollToPage(page) } }
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
                        draggingId = draggingId,
                        draggedCardBounds = draggedCardBounds,
                        dragPoint = dragPoint,
                        dragOffset = dragOffset,
                        onDragStart = { memo, start, bounds ->
                            draggingId = memo.id
                            dragOffset = Offset.Zero
                            draggedCardBounds = bounds
                            homeMemoOrderChanged = false
                            dragPoint = if (bounds == null) start else Offset(bounds.left + start.x, bounds.top + start.y)
                        },
                        onDrag = { amount ->
                            dragOffset += amount
                            dragPoint += amount
                            updateHomeMemoOrderDuringDrag()
                        },
                        onAutoScrollTick = { updateHomeMemoOrderDuringDrag() },
                        onDragEnd = { finishHomeMemoDrag() },
                        onDragCancel = {
                            draggingId = null
                            dragOffset = Offset.Zero
                            draggedCardBounds = null
                            resetHomeMemoOrder()
                        },
                        onOpenMemo = onOpenMemo,
                        onToggleFavorite = onToggleFavorite,
                        onAddMemo = onAddMemo,
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

        val draggedMemo = memos.firstOrNull { it.id == draggingId }
        val draggedBounds = draggedCardBounds
        if (draggedMemo != null && draggedBounds != null) {
            MemoCard(
                memo = draggedMemo,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = draggedBounds.left - (homeBounds?.left ?: 0f) + dragOffset.x
                        translationY = draggedBounds.top - (homeBounds?.top ?: 0f) + dragOffset.y
                        scaleX = 1.05f
                        scaleY = 1.05f
                        shadowElevation = 26f
                    }
                    .width(with(density) { draggedBounds.width.toDp() })
                    .height(with(density) { draggedBounds.height.toDp() })
                    .zIndex(60f),
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
                modifier = Modifier.zIndex(10f)
            )
        }
    }

    pendingImageMemo?.let { memo ->
        ImageChangeChoiceDialog(
            memo = memo,
            onDismiss = { pendingImageMemo = null },
            onCamera = {
                memo.imagePattern = 8
                onCardImageChanged()
                pendingImageMemo = null
            },
            onFolder = {
                memo.imagePattern = 9
                onCardImageChanged()
                pendingImageMemo = null
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
    draggingId: String?,
    draggedCardBounds: Rect?,
    dragPoint: Offset,
    dragOffset: Offset,
    onDragStart: (ShoppingMemo, Offset, Rect?) -> Unit,
    onDrag: (Offset) -> Unit,
    onAutoScrollTick: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit,
    onAddMemo: () -> Unit,
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
    onControlScroll: (Float) -> Float
) {
    val controlHeight = 190.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var pageBounds by remember { mutableStateOf<Rect?>(null) }
    var gridBounds by remember { mutableStateOf<Rect?>(null) }
    val controlHeightPx = with(density) { controlHeight.toPx() }
    val autoScrollEdgePx = with(density) { 8.dp.toPx() }
    val autoScrollDragThresholdPx = with(density) { 32.dp.toPx() }
    val autoScrollStepPx = with(density) { 38.dp.toPx() }
    val latestMemos by rememberUpdatedState(memos)
    val latestDraggingId by rememberUpdatedState(draggingId)
    val latestDraggedCardBounds by rememberUpdatedState(draggedCardBounds)
    val latestDragOffset by rememberUpdatedState(dragOffset)
    val latestPageBounds by rememberUpdatedState(pageBounds)
    val latestGridBounds by rememberUpdatedState(gridBounds)
    val latestOnDragStart by rememberUpdatedState(onDragStart)
    val latestOnDrag by rememberUpdatedState(onDrag)
    val latestOnAutoScrollTick by rememberUpdatedState(onAutoScrollTick)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
    val latestOnDragCancel by rememberUpdatedState(onDragCancel)

    LaunchedEffect(draggingId) {
        if (draggingId == null) return@LaunchedEffect
        while (latestDraggingId != null) {
            val bounds = latestGridBounds
            val draggedBounds = latestDraggedCardBounds
            if (bounds != null) {
                val effectiveBottom = (bounds.bottom - controlHeightPx - 8f).coerceAtLeast(bounds.top)
                val visualTop = (draggedBounds?.top ?: Float.POSITIVE_INFINITY) + latestDragOffset.y
                val visualBottom = (draggedBounds?.bottom ?: Float.NEGATIVE_INFINITY) + latestDragOffset.y
                val visualCenterX = (draggedBounds?.center?.x ?: Float.NEGATIVE_INFINITY) + latestDragOffset.x
                val inLeftColumn = visualCenterX >= bounds.left - 48f && visualCenterX <= bounds.right + 48f
                val movingUp = latestDragOffset.y <= -autoScrollDragThresholdPx
                val movingDown = latestDragOffset.y >= autoScrollDragThresholdPx
                val scrollAmount = if (draggedBounds != null && inLeftColumn) {
                    when {
                        movingUp && visualTop <= bounds.top - autoScrollEdgePx && gridState.canScrollBackward -> -autoScrollStepPx
                        movingDown && visualBottom >= effectiveBottom + autoScrollEdgePx && gridState.canScrollForward -> autoScrollStepPx
                        else -> 0f
                    }
                } else {
                    0f
                }
                if (scrollAmount != 0f) {
                    gridState.scrollBy(scrollAmount)
                    withFrameNanos { }
                    latestOnAutoScrollTick()
                }
            }
            delay(45)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .onGloballyPositioned { pageBounds = it.boundsInWindow() }
            .pointerInput(Unit) {
                var activeCardDrag = false
                detectDragGesturesAfterLongPress(
                    onDragStart = { start ->
                        activeCardDrag = false
                        if (latestDraggingId != null) return@detectDragGesturesAfterLongPress
                        val hostBounds = latestPageBounds ?: return@detectDragGesturesAfterLongPress
                        val windowPoint = Offset(
                            x = hostBounds.left + start.x,
                            y = hostBounds.top + start.y
                        )
                        val memo = latestMemos.firstOrNull { memo ->
                            memoCardBounds[memo.id]?.contains(windowPoint) == true
                        } ?: return@detectDragGesturesAfterLongPress
                        val cardBounds = memoCardBounds[memo.id] ?: return@detectDragGesturesAfterLongPress
                        latestOnDragStart(
                            memo,
                            Offset(windowPoint.x - cardBounds.left, windowPoint.y - cardBounds.top),
                            cardBounds
                        )
                        activeCardDrag = true
                    },
                    onDrag = { change, amount ->
                        if (activeCardDrag) {
                            change.consume()
                            latestOnDrag(amount)
                        }
                    },
                    onDragEnd = {
                        if (activeCardDrag) {
                            latestOnDragEnd()
                            activeCardDrag = false
                        }
                    },
                    onDragCancel = {
                        if (activeCardDrag) {
                            latestOnDragCancel()
                            activeCardDrag = false
                        }
                    }
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .zIndex(if (draggingId != null) 10f else 0f)
                .fillMaxHeight()
                .padding(end = 6.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                state = gridState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { gridBounds = it.boundsInWindow() },
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = draggingId == null
            ) {
                items(memos, key = { it.id }) { memo ->
                    var cardBounds by remember { mutableStateOf<Rect?>(null) }
                    val isDragging = draggingId == memo.id
                    MemoCard(
                        memo = memo,
                        sparkling = sparklingMemoIds.contains(memo.id),
                        modifier = (if (isDragging) Modifier else Modifier.animateItem())
                            .graphicsLayer {
                                translationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                                shadowElevation = 0f
                                alpha = if (isDragging) 0f else 1f
                            }
                            .zIndex(0f)
                            .onGloballyPositioned {
                                val bounds = it.boundsInWindow()
                                cardBounds = bounds
                                memoCardBounds[memo.id] = bounds
                            },
                        onClick = { if (draggingId == null) onOpenMemo(memo) },
                        onToggleFavorite = { onToggleFavorite(memo) }
                    )
                }
            }
        }
        if (showCardDropTargets) {
            HomeCardDropTargets(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                imageTargetActive = imageTargetActive,
                trashTargetActive = trashTargetActive,
                bottomInset = if (oneHandOffset > 0.dp) oneHandOffset else controlHeight,
                onImageTargetPositioned = onImageTargetPositioned,
                onTrashTargetPositioned = onTrashTargetPositioned
            )
        } else {
            TemporaryMemoPanel(
                memo = temporaryMemo,
                draggingEntryId = draggingTemporaryEntryId,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                onOpen = onOpenTemporaryMemo,
                onListPositioned = onTemporaryListPositioned,
                onDragStart = onTemporaryDragStart,
                onDrag = onTemporaryDrag,
                onDragEnd = onTemporaryDragEnd,
                onDragCancel = onTemporaryDragCancel
            )
        }
        }
        HomeLeftControlBoundary(
            modifier = Modifier
                .matchParentSize()
                .zIndex(30f),
            controlHeight = controlHeight
        )
        HomeLeftBottomControls(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.5f)
                .height(controlHeight)
                .zIndex(25f),
            onAddMemo = onAddMemo,
            onPositioned = onControlPositioned,
            onScroll = onControlScroll
        )
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
            title = "Image 変更",
            icon = "画像",
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF1565C0),
            active = imageTargetActive,
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { onImageTargetPositioned(it.boundsInWindow()) }
        )
        HomeDropTargetCard(
            title = "ゴミ箱",
            icon = "削除",
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
                text = if (isTrashTarget) "🗑" else "画像",
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
            Text("ゴミ箱は空です。", color = Color(0xFF888888), fontSize = 16.sp, modifier = Modifier.padding(top = 44.dp))
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
                ShoppingPatternImage(pattern = memo.imagePattern, modifier = Modifier.size(56.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    memo.title.ifBlank { "タイトル未入力" },
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("未購入 $activeCount 件 / 完了 $doneCount 件", color = Color(0xFF777777), fontSize = 13.sp)
            }
            TextButton(onClick = onRestore) {
                Text("戻す", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onErase) {
                Text("消去", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${memo.title.ifBlank { "カード" }} の画像変更") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImageChoiceButton("カメラ", "撮影", Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f), onCamera)
                ImageChoiceButton("フォルダ", "写真", Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f), onFolder)
                ImageChoiceButton("パターン", "10種", Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f), onPattern)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
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
            title = "買い物メモ",
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
                    label = "追加",
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
            title = { Text("削除しますか？") },
            text = { Text("${memo.title.ifBlank { "このカード" }} を削除します。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMemo(memo)
                    pendingDelete = null
                }) { Text("はい", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("キャンセル") }
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
                    .height(112.dp)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_temp_cart),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    "${doneEntries.size}/$totalCount 件 完了",
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
                Text(
                    text = memo.title.ifBlank { TemporaryMemoTitle },
                    color = Color(0xFFD32F2F),
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                        Text("空っぽ", color = Color(0xFF999999), fontSize = 13.sp)
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
                            Text("完了アイテム", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    onClick: () -> Unit
) {
    val scrollState = rememberScrollableState { delta ->
        onScroll(delta)
    }
    Card(
        modifier = modifier
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
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = Color(0xFF1976D2), fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    Text("追加", color = Color(0xFF1976D2), fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text("↕", color = Color(0xFFAD1457), fontSize = 46.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)
            }
        }
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
        color = Color.White,
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
            .heightIn(min = 150.dp)
            .clickable(onClick = onClick)
            .sparkleOverlay(sparkleAlpha),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = 48.sp)
                ShoppingPatternImage(pattern = memo.imagePattern, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(3.dp)
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Text(
                        text = if (memo.favorite) "★" else "☆",
                        color = if (memo.favorite) Color(0xFFFFA000) else Color(0xFF555555),
                        fontSize = 22.sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    "$doneCount/$totalCount 件 完了",
                    color = Color(0xFF2E7D32),
                    fontSize = 16.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = memo.title.ifBlank { "タイトル入力" },
                    color = Color(0xFF212121),
                    fontSize = 22.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AddMemoCard(compact: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 150.dp else 300.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("+", color = Color(0xFF1976D2), fontSize = if (compact) 64.sp else 96.sp)
        }
    }
}

private data class ShoppingImagePattern(
    val name: String,
    val symbol: String,
    val accent: String,
    val background: Color,
    val foreground: Color
)

private val ShoppingImagePatterns = listOf(
    ShoppingImagePattern("赤いカート", "🛒", "＋", Color(0xFFFFEBEE), Color(0xFFD32F2F)),
    ShoppingImagePattern("野菜かご", "🥬", "🥕", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    ShoppingImagePattern("朝食", "🍞", "🥛", Color(0xFFFFF8E1), Color(0xFFF57F17)),
    ShoppingImagePattern("魚", "🐟", "氷", Color(0xFFE1F5FE), Color(0xFF0277BD)),
    ShoppingImagePattern("フルーツ", "🍎", "🍌", Color(0xFFFFF3E0), Color(0xFFE65100)),
    ShoppingImagePattern("米袋", "🍚", "米", Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
    ShoppingImagePattern("日用品", "🧴", "✓", Color(0xFFE0F2F1), Color(0xFF00796B)),
    ShoppingImagePattern("冷凍", "❄", "箱", Color(0xFFE3F2FD), Color(0xFF1565C0)),
    ShoppingImagePattern("カメラ", "📷", "撮", Color(0xFFFCE4EC), Color(0xFFC2185B)),
    ShoppingImagePattern("写真", "🖼", "選", Color(0xFFEDE7F6), Color(0xFF512DA8))
)

private fun activeShoppingImagePatterns(): List<ShoppingImagePattern> {
    val shopPattern = if (Locale.getDefault().language == Locale.JAPANESE.language) {
        ShoppingImagePattern("100円ショップ", "100\n🛍 🏬", "", Color(0xFFFFF3E0), Color(0xFFE65100))
    } else {
        ShoppingImagePattern("ショップ", "🛍 🏬\n🛒", "", Color(0xFFFFF3E0), Color(0xFFE65100))
    }
    return listOf(
        ShoppingImagePattern("食料品", "🥬 🍞\n🍚 🐟", "", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ShoppingImagePattern("カート", "🛒", "", Color(0xFFFFEBEE), Color(0xFFD32F2F)),
        ShoppingImagePattern("家電", "🔌 💡\n📺", "", Color(0xFFE3F2FD), Color(0xFF1565C0)),
        ShoppingImagePattern("ゲーム", "🎮 ⭐\n🎲", "", Color(0xFFEDE7F6), Color(0xFF512DA8)),
        ShoppingImagePattern("レストラン", "🍽 ☕\n🍰", "", Color(0xFFFFF8E1), Color(0xFFF57F17)),
        ShoppingImagePattern("服と靴", "👕 👟\n🧢", "", Color(0xFFFCE4EC), Color(0xFFC2185B)),
        ShoppingImagePattern("スポーツ用品", "⚽ 🏀\n🎾", "", Color(0xFFE0F2F1), Color(0xFF00796B)),
        ShoppingImagePattern("サービス業", "😊 🤝\n✨", "", Color(0xFFFFFDE7), Color(0xFFF9A825)),
        shopPattern,
        ShoppingImagePattern("雑貨", "🧺 🧴\n🧻", "", Color(0xFFF5F5F5), Color(0xFF616161))
    )
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
    DailyGoods
}

private data class ShoppingVisualPattern(
    val name: String,
    val kind: ShoppingVisualKind,
    val background: Color,
    val accent: Color
)

private fun activeShoppingVisualPatterns(): List<ShoppingVisualPattern> {
    return listOf(
        ShoppingVisualPattern("食料品", ShoppingVisualKind.Food, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ShoppingVisualPattern("カート", ShoppingVisualKind.Cart, Color(0xFFFFEBEE), Color(0xFFD32F2F)),
        ShoppingVisualPattern("家電", ShoppingVisualKind.Appliance, Color(0xFFE3F2FD), Color(0xFF1565C0)),
        ShoppingVisualPattern("ゲーム", ShoppingVisualKind.Game, Color(0xFFEDE7F6), Color(0xFF512DA8)),
        ShoppingVisualPattern("レストラン", ShoppingVisualKind.Restaurant, Color(0xFFFFF8E1), Color(0xFFF57F17)),
        ShoppingVisualPattern("衣類", ShoppingVisualKind.Clothing, Color(0xFFFCE4EC), Color(0xFFC2185B)),
        ShoppingVisualPattern("スポーツ用品", ShoppingVisualKind.Sports, Color(0xFFE0F2F1), Color(0xFF00796B)),
        ShoppingVisualPattern("サービス業", ShoppingVisualKind.Service, Color(0xFFFFFDE7), Color(0xFFF9A825)),
        ShoppingVisualPattern("100均一", ShoppingVisualKind.HundredYenShop, Color(0xFFFFF3E0), Color(0xFFD32F2F)),
        ShoppingVisualPattern("日用品", ShoppingVisualKind.DailyGoods, Color(0xFFE0F7FA), Color(0xFF00838F))
    )
}

@Composable
private fun ShoppingPatternImage(pattern: Int, modifier: Modifier = Modifier) {
    val patterns = activeShoppingVisualPatterns()
    val item = patterns[((pattern % patterns.size) + patterns.size) % patterns.size]
    ReadableShoppingPatternImage(item = item, modifier = modifier)
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
                    if (Locale.getDefault().language == Locale.JAPANESE.language) {
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
                ShoppingVisualKind.DailyGoods -> {
                    drawRoundRect(Color(0xFF4DD0E1), Offset(sx(0.34f), sy(0.32f)), Size(sx(0.32f), sy(0.44f)), CornerRadius(u * 0.08f, u * 0.08f))
                    drawRoundRect(Color(0xFF0097A7), Offset(sx(0.42f), sy(0.22f)), Size(sx(0.16f), sy(0.10f)), CornerRadius(u * 0.025f, u * 0.025f))
                    drawLine(Color(0xFF006064), Offset(sx(0.48f), sy(0.20f)), Offset(sx(0.68f), sy(0.20f)), strokeWidth = u * 0.035f)
                    drawOval(Color.White, Offset(sx(0.43f), sy(0.47f)), Size(sx(0.14f), sy(0.20f)))
                }
            }
        }
        if (item.kind == ShoppingVisualKind.HundredYenShop && Locale.getDefault().language == Locale.JAPANESE.language) {
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

@Composable
private fun PatternPickerScreen(
    memo: ShoppingMemo,
    oneHandModeEnabled: Boolean,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val patterns = activeShoppingVisualPatterns()
    PatternPickerGridScreen(
        memo = memo,
        patterns = patterns,
        oneHandModeEnabled = oneHandModeEnabled,
        onSelect = onSelect
    )
    return
    Column(Modifier.fillMaxSize()) {
        Header(
            title = "画像パターン",
            trailing = {
                TextButton(onClick = onBack) {
                    Text("戻る", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                }
            }
        )
        Text(
            text = memo.title.ifBlank { "カード" },
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
        if (Locale.getDefault().language == Locale.JAPANESE.language) {
            Text(
                text = "￥100ショップ",
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
    val riceOrBread = if (Locale.getDefault().language == Locale.JAPANESE.language) "🍚" else "🍞"
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
        if (Locale.getDefault().language == Locale.JAPANESE.language) {
            Text(
                text = "￥100ショップ",
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
    val backdropResId = if (Locale.getDefault().language == Locale.JAPANESE.language) {
        R.drawable.one_hand_food_pattern_a
    } else {
        R.drawable.one_hand_food_pattern_global
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
    recentlyMovedEntryIds: Set<String>,
    onFinish: () -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeBottom = with(density) { WindowInsets.ime.getBottom(this).toDp() }
    val imeVisible = imeBottom > 0.dp
    val fabBottomPadding = if (imeVisible) {
        (imeBottom - BottomBarHeight + FabKeyboardGap).coerceAtLeast(FabKeyboardGap)
    } else {
        22.dp
    }
    val listBottomPadding = if (imeVisible) {
        (imeBottom - BottomBarHeight + DetailListExtraBottom).coerceAtLeast(DetailListExtraBottom)
    } else {
        DetailListExtraBottom
    }
    val tabs = listOf("アイテム", "ゴミ箱")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var voiceTarget by remember { mutableStateOf(if (memo.title.isBlank()) VoiceTarget.Title else VoiceTarget.Item) }
    var focusedItemRequestId by remember { mutableStateOf<String?>(null) }
    var addButtonScrollRequest by remember { mutableStateOf(0) }
    var itemDragActive by remember { mutableStateOf(false) }
    var activeListAtTop by remember(memo.id) { mutableStateOf(true) }
    var deletedListAtTop by remember(memo.id) { mutableStateOf(true) }
    var detailHeightPx by remember { mutableStateOf(0) }
    var oneHandOffsetPx by remember(memo.id) { mutableStateOf(0f) }
    var oneHandMoveSerial by remember(memo.id) { mutableStateOf(0) }
    var oneHandFlingGeneration by remember(memo.id) { mutableStateOf(0) }
    val oneHandMaxOffsetPx = (detailHeightPx * OneHandMaxOffsetRatio).coerceAtLeast(0f)
    val oneHandBackdropHeight = with(density) { oneHandOffsetPx.toDp() }
    val currentListAtTop = if (pagerState.currentPage == 0) activeListAtTop else deletedListAtTop
    val latestCurrentListAtTop by rememberUpdatedState(currentListAtTop)

    fun editableEntry(): ShoppingEntry {
        return requestBlankEntry(memo)
    }

    fun moveToItemInput() {
        val entry = editableEntry()
        selectedEntryId = entry.id
        focusedItemRequestId = entry.id
        voiceTarget = VoiceTarget.Item
    }

    fun handleVoiceText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isBlank()) return

        if (voiceTarget == VoiceTarget.Title || memo.title.isBlank()) {
            memo.title = cleaned
            val entry = editableEntry()
            selectedEntryId = entry.id
            focusedItemRequestId = null
            voiceTarget = VoiceTarget.Item
        } else {
            val entry = memo.entries.firstOrNull { it.id == selectedEntryId && it.name.isBlank() }
                ?: memo.entries.firstOrNull { it.name.isBlank() }
                ?: ShoppingEntry(name = "").also { memo.entries.add(it) }
            entry.name = cleaned
            ensureDisplayBlankEntry(memo)
            selectedEntryId = null
            focusedItemRequestId = null
            voiceTarget = VoiceTarget.Item
            addButtonScrollRequest++
        }
        onChanged()
    }

    val speechController = remember(memo.id) {
        ContinuousSpeechController(context, ::handleVoiceText)
    }
    DisposableEffect(speechController) {
        onDispose { speechController.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            keyboardController?.hide()
            speechController.start()
        }
    }

    LaunchedEffect(memo.id) {
        if (memo.title.isBlank()) titleFocusRequester.requestFocus()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
            speechController.stop()
        }
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
            .pointerInput(pagerState.currentPage, itemDragActive) {
                if (itemDragActive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalX = 0f
                    var totalY = 0f
                    var activePointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == activePointerId } ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange()
                        totalX += delta.x
                        totalY += delta.y
                        if (kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.25f) {
                            if (pagerState.currentPage == 0 && totalX < -DetailBackSwipeThresholdPx) {
                                change.consume()
                                scope.launch { pagerState.animateScrollToPage(1) }
                                break
                            } else if (pagerState.currentPage == 0 && totalX > DetailBackSwipeThresholdPx) {
                                change.consume()
                                onFinish()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = memo.title,
                    onValueChange = {
                        memo.title = it
                        onChanged()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(titleFocusRequester)
                        .onFocusChanged {
                            if (it.isFocused) {
                                speechController.stop()
                                voiceTarget = VoiceTarget.Title
                            }
                        },
                    textStyle = TextStyle(fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                    singleLine = false,
                    maxLines = 2,
                    cursorBrush = SolidColor(Color(0xFF1976D2)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 20.dp)
                        ) {
                            if (memo.title.isBlank()) {
                                Text("タイトルを入力してください", color = Color(0xFFC0C4CC), fontSize = 24.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                if (pagerState.currentPage == 1 && memo.deletedEntries.isNotEmpty()) {
                    Button(
                        onClick = {
                            memo.deletedEntries.clear()
                            onChanged()
                        },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Text("全消去", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
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
                        onFocusedItemConsumed = { focusedItemRequestId = null },
                        onSelect = { selectedEntryId = it },
                        onRequestFocus = { focusedItemRequestId = it },
                        onEntryFocused = {
                            speechController.stop()
                            voiceTarget = VoiceTarget.Item
                        },
                        onEntryFocusCleared = { id ->
                            if (selectedEntryId == id) selectedEntryId = null
                        },
                        bottomPadding = listBottomPadding,
                        onItemDragActiveChange = { itemDragActive = it },
                        onListAtTopChanged = { activeListAtTop = it },
                        editTapSuppressionSerial = oneHandMoveSerial,
                        recentlyMovedEntryIds = recentlyMovedEntryIds,
                        onChanged = onChanged
                    )
                } else {
                    DeletedItemsPage(
                        memo = memo,
                        onItemDragActiveChange = { itemDragActive = it },
                        onListAtTopChanged = { deletedListAtTop = it },
                        onRestore = {
                            memo.deletedEntries.remove(it)
                            val restored = it.copy(checked = false)
                            val blankIndex = memo.entries.indexOfFirst { entry -> entry.name.isBlank() }
                            val insertIndex = if (blankIndex >= 0) blankIndex else memo.entries.indexOfFirst { entry -> entry.checked }.let { doneIndex ->
                                if (doneIndex >= 0) doneIndex else memo.entries.size
                            }
                            memo.entries.add(insertIndex, restored)
                            ensureDisplayBlankEntry(memo)
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

        if (pagerState.currentPage == 0) {
            MicFab(
                controller = speechController,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = fabBottomPadding),
                onClick = {
                            if (speechController.partialText.isNotBlank()) {
                                speechController.commitPartial()
                                return@MicFab
                            }
                            if (speechController.isRunning) {
                                speechController.stop()
                            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                keyboardController?.hide()
                                speechController.start()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveItemsPage(
    memo: ShoppingMemo,
    selectedEntryId: String?,
    focusedItemRequestId: String?,
    addButtonScrollRequest: Int,
    onFocusedItemConsumed: () -> Unit,
    onSelect: (String?) -> Unit,
    onRequestFocus: (String) -> Unit,
    onEntryFocused: () -> Unit,
    onEntryFocusCleared: (String) -> Unit,
    bottomPadding: Dp,
    onItemDragActiveChange: (Boolean) -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    editTapSuppressionSerial: Int,
    recentlyMovedEntryIds: Set<String>,
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

    fun removeUnusedBlankEntry(): Boolean {
        if (memo.entries.none { it.name.isNotBlank() && !it.checked }) return false
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

    fun bottomAlignedOffset(extraLiftPx: Int): Int {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val targetY = (viewportHeight - bottomPaddingPx - extraLiftPx).coerceAtLeast(0)
        return -targetY
    }

    LaunchedEffect(memo.entries.size, focusedItemRequestId, selectedEntryId) {
        val editingBlank = memo.entries.any {
            it.name.isBlank() && (it.id == focusedItemRequestId || it.id == selectedEntryId)
        }
        if (!editingBlank) {
            ensureDisplayBlankEntry(memo)
        }
    }

    LaunchedEffect(focusedItemRequestId, selectedEntryId, bottomPadding) {
        if (scrollAnchor == ScrollAnchor.AddButton) return@LaunchedEffect
        val targetId = focusedItemRequestId ?: selectedEntryId ?: return@LaunchedEffect
        val index = memo.entries.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            listState.animateScrollToItem(index = index + listTopAnchorIndexOffset, scrollOffset = bottomAlignedOffset(rowLiftPx))
        }
    }

    LaunchedEffect(scrollAnchor, bottomPadding, memo.entries.size) {
        if (scrollAnchor != ScrollAnchor.AddButton) return@LaunchedEffect
        val addIndex = memo.entries.count { !it.checked } + listTopAnchorIndexOffset
        listState.animateScrollToItem(index = addIndex, scrollOffset = bottomAlignedOffset(addButtonLiftPx))
    }

    LaunchedEffect(addButtonScrollRequest, bottomPadding, memo.entries.size) {
        if (addButtonScrollRequest <= 0) return@LaunchedEffect
        val addIndex = memo.entries.count { !it.checked } + listTopAnchorIndexOffset
        listState.animateScrollToItem(index = addIndex, scrollOffset = bottomAlignedOffset(addButtonLiftPx))
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
        if (entry.name.isBlank()) return false
        val active = memo.entries.filter { it.name.isNotBlank() && !it.checked }.toMutableList()
        val done = memo.entries.filter { it.checked && it.name.isNotBlank() }.toMutableList()
        val targetGroup = if (entry.checked) done else active
        val from = targetGroup.indexOf(entry)
        if (from < 0) return false
        val to = (from + direction).coerceIn(0, targetGroup.lastIndex)
        if (from == to) return false
        targetGroup.removeAt(from)
        targetGroup.add(to, entry)
        val blank = memo.entries.firstOrNull { it.name.isBlank() }
        memo.entries.clear()
        memo.entries.addAll(active)
        if (blank != null) memo.entries.add(blank)
        memo.entries.addAll(done)
        ensureDisplayBlankEntry(memo)
        onChanged()
        return true
    }

    fun visibleItemHeightPx(entry: ShoppingEntry): Float {
        val itemKey = if (entry.checked) "done-${entry.id}" else entry.id
        return listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == itemKey }
            ?.size
            ?.toFloat()
            ?: fallbackRowHeightPx
    }

    fun listIndexForEntry(entry: ShoppingEntry): Int {
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
        val itemKey = if (entry.checked) "done-${entry.id}" else entry.id
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
        val itemKey = if (entry.checked) "done-${entry.id}" else entry.id
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
            val itemKey = if (entry.checked) "done-${entry.id}" else entry.id
            val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
                ?: return@launch
            draggingOffsetY = visualTop - itemInfo.offset
            gentlyKeepDraggedEntryVisible(entry)
        }
    }

    fun isDraggedNearTopEdge(entry: ShoppingEntry): Boolean {
        val itemKey = if (entry.checked) "done-${entry.id}" else entry.id
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
                val group = memo.entries.filter { it.name.isNotBlank() && it.checked == entry.checked }
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
            val reorderGroup = memo.entries.filter { it.name.isNotBlank() && it.checked == entry.checked }
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
                    if (!entry.checked) {
                        val newIndex = memo.entries.filter { it.name.isNotBlank() && !it.checked }.indexOf(entry)
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
                    if (!entry.checked) {
                        val newIndex = memo.entries.filter { it.name.isNotBlank() && !it.checked }.indexOf(entry)
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

        val reorderGroup = memo.entries.filter { it.name.isNotBlank() && it.checked == entry.checked }
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
            val group = memo.entries.filter { it.name.isNotBlank() && it.checked == entry.checked }
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
                val updatedGroup = memo.entries.filter { it.name.isNotBlank() && it.checked == entry.checked }
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
            ensureDisplayBlankEntry(memo)
            onChanged()
        }
        deleteSwipeEntryId = null
        deleteSwipeOffsetX = 0f
        endReorder()
        onItemDragActiveChange(false)
    }

    fun markDone(entry: ShoppingEntry) {
        if (entry.name.isBlank()) return
        memo.entries.remove(entry)
        entry.checked = true
        memo.entries.add(entry)
        onSelect(null)
        ensureDisplayBlankEntry(memo)
        onChanged()
    }

    fun restoreDone(entry: ShoppingEntry) {
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
        itemsIndexed(memo.entries.filter { !it.checked }, key = { _, entry -> entry.id }) { index, entry ->
            ShoppingEntryRow(
                entry = entry,
                displayNumber = if (entry.name.isNotBlank()) index + 1 else null,
                selected = selectedEntryId == entry.id,
                shouldRequestFocus = focusedItemRequestId == entry.id,
                isDragging = draggingEntryId == entry.id,
                dragOffsetY = if (draggingEntryId == entry.id) draggingOffsetY else 0f,
                isDeleteSwiping = deleteSwipeEntryId == entry.id,
                deleteSwipeOffsetX = if (deleteSwipeEntryId == entry.id) deleteSwipeOffsetX else 0f,
                editTapSuppressionSerial = editTapSuppressionSerial,
                recentlyMoved = recentlyMovedEntryIds.contains(entry.id),
                modifier = if (draggingEntryId == entry.id) {
                    Modifier.zIndex(1f)
                } else if (deleteSwipeEntryId == entry.id) {
                    Modifier.zIndex(1f)
                } else {
                    Modifier.animateItem().zIndex(0f)
                },
                onSelect = { selectEntry(entry) },
                onPressStarted = {
                    onSelect(null)
                    onFocusedItemConsumed()
                },
                onEditRequested = {
                    selectEntry(entry)
                    onRequestFocus(entry.id)
                },
                onNumberClick = { toggleEntryColor(entry) },
                onFocusConsumed = onFocusedItemConsumed,
                onFocused = {
                    onSelect(entry.id)
                    if (entry.name.isNotBlank()) scrollAnchor = ScrollAnchor.Item
                    onEntryFocused()
                },
                onFocusCleared = {
                    onEntryFocusCleared(entry.id)
                },
                onNameChanged = {
                    entry.name = it
                    if (it.isBlank() && selectedEntryId == entry.id && memo.entries.any { other -> other.id != entry.id && other.name.isNotBlank() && !other.checked }) {
                        memo.entries.removeAll { other -> other.id != entry.id && other.name.isBlank() }
                    } else {
                        ensureDisplayBlankEntry(memo)
                    }
                    onChanged()
                },
                onComplete = { markDone(entry) },
                onDelete = {
                    memo.entries.remove(entry)
                    memo.deletedEntries.add(entry.copy(checked = false))
                    onChanged()
                },
                onReorderStart = { startReorder(entry) },
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
            TextButton(
                onClick = {
                    val entry = requestBlankEntry(memo)
                    scrollAnchor = ScrollAnchor.AddButton
                    onSelect(entry.id)
                    onRequestFocus(entry.id)
                    onChanged()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("+ リストアイテム", fontSize = 18.sp)
            }
        }
        val doneEntries = memo.entries.filter { it.checked && it.name.isNotBlank() }
        if (doneEntries.isNotEmpty()) {
            item {
                Text(
                    text = "完了アイテム",
                    color = Color(0xFFD32F2F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
            items(doneEntries, key = { "done-${it.id}" }) { entry ->
                DoneEntryRow(
                    entry = entry,
                    isDragging = draggingEntryId == entry.id,
                    dragOffsetY = if (draggingEntryId == entry.id) draggingOffsetY else 0f,
                    isDeleteSwiping = deleteSwipeEntryId == entry.id,
                    deleteSwipeOffsetX = if (deleteSwipeEntryId == entry.id) deleteSwipeOffsetX else 0f,
                    recentlyMoved = recentlyMovedEntryIds.contains(entry.id),
                    modifier = if (draggingEntryId == entry.id || deleteSwipeEntryId == entry.id) {
                        Modifier.zIndex(1f)
                    } else {
                        Modifier.animateItem().zIndex(0f)
                    },
                    onRestore = { restoreDone(entry) },
                    onDeleteSwipeStart = { startDeleteSwipe(entry) },
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
    isDragging: Boolean,
    dragOffsetY: Float,
    isDeleteSwiping: Boolean,
    deleteSwipeOffsetX: Float,
    editTapSuppressionSerial: Int,
    recentlyMoved: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onPressStarted: () -> Unit,
    onEditRequested: (Offset) -> Unit,
    onNumberClick: () -> Unit,
    onFocusConsumed: () -> Unit,
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
    val canDrag = entry.name.isNotBlank()
    var fieldValue by remember(entry.id) { mutableStateOf(TextFieldValue(entry.name)) }
    var textLayoutResult by remember(entry.id) { mutableStateOf<TextLayoutResult?>(null) }
    var pendingTapPosition by remember(entry.id) { mutableStateOf<Offset?>(null) }
    var textFocused by remember(entry.id) { mutableStateOf(false) }
    val rowBackground = if (selected) focusedEntryBackground() else entryColorMarkBackground(entry.colorMark)
    val moveSparkleAlpha = rememberSparkleAlpha(recentlyMoved)
    val rowSwipeModifier = swipeToTrashModifier(
        key = entry.id,
        enabled = canDrag,
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

    LaunchedEffect(entry.name) {
        if (entry.name != fieldValue.text) {
            val selection = fieldValue.selection.start.coerceIn(0, entry.name.length)
            fieldValue = fieldValue.copy(text = entry.name, selection = TextRange(selection))
        }
    }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            val tapOffset = pendingTapPosition
                ?.let { textLayoutResult?.getOffsetForPosition(it) }
                ?.coerceIn(0, fieldValue.text.length)
                ?: fieldValue.selection.start.coerceIn(0, fieldValue.text.length)
            fieldValue = fieldValue.copy(selection = TextRange(tapOffset))
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
            .recentMoveBackground(recentlyMoved, rowBackground, moveSparkleAlpha)
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
                        onNameChanged(it.text)
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
                        fontSize = 20.sp,
                        color = Color.Black,
                        textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    cursorBrush = SolidColor(Color(0xFF1976D2)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorationBox = { innerTextField ->
                        if (entry.name.isBlank()) {
                            Text("未入力", color = Color(0xFF9E9E9E), fontSize = 20.sp)
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
                    text = if (isDeleteSwiping) "→ 🗑" else "完了",
                    color = if (isDeleteSwiping) Color(0xFFD32F2F) else if (entry.name.isBlank()) Color(0xFFBBBBBB) else Color(0xFF1976D2),
                    fontSize = 16.sp,
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

private fun Modifier.recentMoveBackground(active: Boolean, normalColor: Color, sparkleAlpha: Float): Modifier {
    return if (active) {
        background(
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFFFDE7),
                    Color(0xFFFFF59D),
                    Color(0xFFFFFDE7)
                )
            )
        ).sparkleOverlay(sparkleAlpha)
    } else {
        background(normalColor)
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
    val moveSparkleAlpha = rememberSparkleAlpha(recentlyMoved)
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
            .recentMoveBackground(recentlyMoved, Color.White, moveSparkleAlpha)
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
            fontSize = 20.sp,
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun RestoreIconButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text("戻す", color = Color(0xFF1976D2), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeletedItemsPage(
    memo: ShoppingMemo,
    onItemDragActiveChange: (Boolean) -> Unit,
    onListAtTopChanged: (Boolean) -> Unit,
    onRestore: (ShoppingEntry) -> Unit,
    onErase: (ShoppingEntry) -> Unit
) {
    if (memo.deletedEntries.isEmpty()) {
        LaunchedEffect(Unit) {
            onListAtTopChanged(true)
        }
        PlaceholderBody("ゴミ箱は空です。")
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
                text = "戻す",
                color = Color(0xFF1976D2),
                fontSize = 16.sp,
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
            text = entry.name.ifBlank { "辟｡鬘後・繧｢繧､繝・Β" },
            fontSize = 19.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        if (isDragging) {
            Text(
                text = "消去",
                color = Color(0xFFD32F2F),
                fontSize = 16.sp,
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
        PlaceholderBody("削除済アイテムはありません")
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
                Text(entry.name.ifBlank { "無題のアイテム" }, fontSize = 19.sp, color = Color(0xFF666666), modifier = Modifier.weight(1f))
                RestoreIconButton(onClick = { onRestore(entry) })
                TextButton(onClick = { onErase(entry) }) { Text("消去", color = Color(0xFFD32F2F)) }
            }
            Divider(color = Color(0xFFE0E0E0))
        }
    }
}

@Composable
private fun MicFab(
    controller: ContinuousSpeechController,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val hasPartial = controller.partialText.isNotBlank()
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
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
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
            containerColor = if (hasPartial) Color(0xFF16A34A) else if (controller.isRunning) Color(0xFFE11D48) else Color(0xFF1E88E5),
            modifier = Modifier.size(90.dp)
        ) {
            if (controller.isRunning && !hasPartial) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            scaleX = stopScale
                            scaleY = stopScale
                        }
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    painter = painterResource(
                        id = if (hasPartial) R.drawable.ic_fab_check else R.drawable.ic_fab_mic
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (hasPartial) 44.dp else 50.dp)
                )
            }
        }
    }
}
@Composable
private fun FavoritesScreen(
    memos: List<ShoppingMemo>,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Header("お気に入り")
        if (memos.isEmpty()) {
            PlaceholderBody("お気に入りはありません")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memos, key = { it.id }) { memo ->
                    MemoCard(memo, onClick = { onOpenMemo(memo) }, onToggleFavorite = { onToggleFavorite(memo) })
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    memoCount: Int,
    oneHandModeEnabled: Boolean,
    onOneHandModeChanged: (Boolean) -> Unit,
    simpleModeEnabled: Boolean,
    onSimpleModeChanged: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Header("設定")
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("モード", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444))
            SettingModeRow(
                title = "かんたんモード",
                description = "基本操作を中心にしたモードです。",
                selected = simpleModeEnabled,
                onClick = { onSimpleModeChanged(true) }
            )
            SettingModeRow(
                title = "高機能モード",
                description = "より細かい操作を使うためのモードです。",
                selected = !simpleModeEnabled,
                onClick = { onSimpleModeChanged(false) }
            )
            SettingToggleRow(
                title = "片手だけで操作可能モード",
                description = "一覧を下半分までスクロールできるようにし、片手で操作しやすくします。",
                checked = oneHandModeEnabled,
                onCheckedChange = onOneHandModeChanged
            )
            SettingRow("保存方式", "端末内保存")
            SettingRow("登録リスト数", "${memoCount}件")
            SettingRow("通信", "サーバーアクセスなし")
        }
    }
}

@Composable
private fun SettingModeRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
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
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color(0xFF666666), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color(0xFF666666), fontSize = 13.sp, lineHeight = 18.sp)
        }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = Color(0xFF666666), fontSize = 16.sp)
    }
    Divider(color = Color(0xFFE0E0E0))
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
            Text(
                text = title,
                color = Color(0xFF1976D2),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
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
    onHome: () -> Unit,
    onMap: () -> Unit,
    onSettings: () -> Unit,
    onFavorite: () -> Unit,
    onOwner: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .navigationBarsPadding()
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomIcon(R.drawable.icon_carrot, "ホーム", screen == Screen.Home, onHome)
        BottomIcon(R.drawable.icon_cabbage, "地図", screen == Screen.Map, onMap)
        BottomIcon(R.drawable.icon_tomato, "設定", screen == Screen.Settings, onSettings)
        BottomIcon(R.drawable.icon_favorite, "お気に入り", screen == Screen.Favorites, onFavorite)
        BottomIcon(R.drawable.icon_potato, "オーナー", false, onOwner)
    }
}

@Composable
private fun RowScope.BottomIcon(drawableId: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = label,
            modifier = Modifier.size(if (selected) 76.dp else 70.dp),
            contentScale = ContentScale.Fit
        )
    }
}
