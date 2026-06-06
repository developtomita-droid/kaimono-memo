package com.ttjapan.kaimonomemo.ui.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.ttjapan.kaimonomemo.data.saveMemos
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import com.ttjapan.kaimonomemo.voice.ContinuousSpeechController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private enum class Screen {
    Home,
    Detail,
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
private val TrashTabSelectedColor = Color(0xFFE91E63)

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
    if (blank != null) {
        memo.entries.add(blank)
        memo.entries.addAll(done)
        return blank
    }
    if (active.isEmpty()) {
        val newBlank = ShoppingEntry(name = "")
        memo.entries.add(newBlank)
        memo.entries.addAll(done)
        return newBlank
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
    val memos = remember { mutableStateListOf<ShoppingMemo>().also { it.addAll(loadMemos(context)) } }
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedMemoId by remember { mutableStateOf<String?>(null) }
    val selectedMemo = memos.firstOrNull { it.id == selectedMemoId }

    fun persist() = saveMemos(context, memos)
    fun openMemo(memo: ShoppingMemo) {
        selectedMemoId = memo.id
        currentScreen = Screen.Detail
    }
    fun finishDetail() {
        selectedMemo?.let { memo ->
            pruneBlankEntries(memo)
            if (memo.title.isBlank() && memo.entries.none { it.name.isNotBlank() } && memo.deletedEntries.none { it.name.isNotBlank() }) {
                memos.remove(memo)
                selectedMemoId = null
            } else {
                assignDefaultTitleIfBlank(memo, memos)
            }
        }
        persist()
        currentScreen = Screen.Home
    }
    fun addMemo() {
        val memo = ShoppingMemo(entries = listOf(ShoppingEntry(name = "")))
        memos.add(memo)
        openMemo(memo)
    }

    BackHandler(enabled = currentScreen == Screen.Home) {
        // ホームでの戻るジェスチャーではアプリを終了しない。
    }
    BackHandler(enabled = currentScreen == Screen.Detail) {
        finishDetail()
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
                Screen.Home -> HomeScreen(
                    memos = memos,
                    onAddMemo = ::addMemo,
                    onOpenMemo = ::openMemo,
                    onDeleteMemo = {
                        memos.remove(it)
                        persist()
                    },
                    onToggleFavorite = {
                        it.favorite = !it.favorite
                        persist()
                    }
                )
                Screen.Detail -> {
                    if (selectedMemo == null) {
                        currentScreen = Screen.Home
                    } else {
                        MemoDetailScreen(
                            memo = selectedMemo,
                            onFinish = ::finishDetail,
                            onChanged = ::persist
                        )
                    }
                }
                Screen.Map -> PlaceholderScreen("地図", "地図機能はありません")
                Screen.Settings -> SettingsScreen(memoCount = memos.size)
                Screen.Favorites -> FavoritesScreen(
                    memos = memos.filter { it.favorite },
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

@Composable
private fun HomeScreen(
    memos: List<ShoppingMemo>,
    onAddMemo: () -> Unit,
    onOpenMemo: (ShoppingMemo) -> Unit,
    onDeleteMemo: (ShoppingMemo) -> Unit,
    onToggleFavorite: (ShoppingMemo) -> Unit
) {
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPoint by remember { mutableStateOf(Offset.Zero) }
    var draggedCardBounds by remember { mutableStateOf<Rect?>(null) }
    var trashBounds by remember { mutableStateOf<Rect?>(null) }
    var pendingDelete by remember { mutableStateOf<ShoppingMemo?>(null) }

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(memos, key = { it.id }) { memo ->
                val isDragging = draggingId == memo.id
                MemoCard(
                    memo = memo,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = if (isDragging) dragOffset.x else 0f
                            translationY = if (isDragging) dragOffset.y else 0f
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                            shadowElevation = if (isDragging) 18f else 0f
                        }
                        .onGloballyPositioned {
                            if (isDragging || draggingId == null) draggedCardBounds = it.boundsInWindow()
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
            item {
                AddMemoCard(compact = memos.isNotEmpty(), onClick = onAddMemo)
            }
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
private fun MemoCard(
    memo: ShoppingMemo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val activeCount = memo.entries.count { !it.checked && it.name.isNotBlank() }
    val doneCount = memo.entries.count { it.checked }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = 76.sp)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Text(
                        text = if (memo.favorite) "★" else "☆",
                        color = if (memo.favorite) Color(0xFFFFA000) else Color(0xFF555555),
                        fontSize = 36.sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text("未購入 $activeCount 件", color = Color(0xFF2E7D32), fontSize = 22.sp)
                Text(
                    text = memo.title.ifBlank { "タイトル入力" },
                    color = Color(0xFF212121),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text("完了 $doneCount 件", color = Color(0xFF212121), fontSize = 24.sp, modifier = Modifier.weight(1f))
                    Text("編集中", color = Color(0xFF16A34A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
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

@Composable
private fun MemoDetailScreen(
    memo: ShoppingMemo,
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
        Column(Modifier.fillMaxSize()) {
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
                    textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                    singleLine = true,
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
                    TextButton(
                        onClick = {
                            memo.deletedEntries.clear()
                            onChanged()
                        }
                    ) {
                        Text("全消去", color = Color(0xFFD32F2F))
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
                        onChanged = onChanged
                    )
                } else {
                    DeletedItemsPage(
                        memo = memo,
                        onItemDragActiveChange = { itemDragActive = it },
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
    onChanged: () -> Unit
) {
    val density = LocalDensity.current
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

    fun bottomAlignedOffset(extraLiftPx: Int): Int {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val targetY = (viewportHeight - bottomPaddingPx - extraLiftPx).coerceAtLeast(0)
        return -targetY
    }

    LaunchedEffect(memo.entries.size) {
        ensureDisplayBlankEntry(memo)
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                onFocusConsumed = onFocusedItemConsumed,
                onFocused = {
                    onSelect(entry.id)
                    if (entry.name.isNotBlank()) scrollAnchor = ScrollAnchor.Item
                    onEntryFocused()
                },
                onFocusCleared = { onEntryFocusCleared(entry.id) },
                onNameChanged = {
                    entry.name = it
                    ensureDisplayBlankEntry(memo)
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
    onPressStarted: () -> Unit = {},
    onTap: () -> Unit = {},
    onSwipeStart: () -> Unit,
    onSwipeDrag: (Float, Float) -> Unit,
    onSwipeEnd: () -> Unit
): Modifier {
    val focusManager = LocalFocusManager.current
    return Modifier.pointerInput(key, enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            down.consume()
            focusManager.clearFocus(force = true)
            onPressStarted()
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
                onTap()
                return@awaitEachGesture
            } else if (preLongPressResult == false) {
                return@awaitEachGesture
            }

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
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onPressStarted: () -> Unit,
    onEditRequested: () -> Unit,
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
    val rowSwipeModifier = swipeToTrashModifier(
        key = entry.id,
        enabled = canDrag,
        onPressStarted = onPressStarted,
        onTap = onEditRequested,
        onSwipeStart = onDeleteSwipeStart,
        onSwipeDrag = onDeleteSwipeDrag,
        onSwipeEnd = onDeleteSwipeEnd
    )

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            onFocusConsumed()
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
            .background(if (selected) Color(0xFFE3F2FD) else Color.White)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberHandle(displayNumber = displayNumber)
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                BasicTextField(
                    value = entry.name,
                    onValueChange = onNameChanged,
                    enabled = selected && !isDeleteSwiping,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) onFocused() else onFocusCleared()
                    }
                    .padding(vertical = 12.dp),
                    minLines = 1,
                    maxLines = 8,
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

@Composable
private fun NumberHandle(displayNumber: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 6.dp, end = 10.dp)
            .size(32.dp)
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
    modifier: Modifier = Modifier,
    onRestore: () -> Unit,
    onDeleteSwipeStart: () -> Unit,
    onDeleteSwipeDrag: (Float, Float) -> Unit,
    onDeleteSwipeEnd: () -> Unit
) {
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
            .background(Color.White)
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
    onRestore: (ShoppingEntry) -> Unit,
    onErase: (ShoppingEntry) -> Unit
) {
    if (memo.deletedEntries.isEmpty()) {
        PlaceholderBody("蜑企勁貂医い繧､繝・Β縺ｯ縺ゅｊ縺ｾ縺帙ｓ")
        return
    }
    val density = LocalDensity.current
    val actionThresholdPx = with(density) { 104.dp.toPx() }
    var draggingEntryId by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }

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

    LazyColumn(Modifier.fillMaxSize()) {
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
    val swipeModifier = horizontalPressSwipeModifier(
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
            Icon(
                painter = painterResource(
                    id = when {
                        hasPartial -> R.drawable.ic_fab_check
                        controller.isRunning -> R.drawable.ic_fab_stop
                        else -> R.drawable.ic_fab_mic
                    }
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(
                    if (hasPartial || controller.isRunning) 44.dp else 50.dp
                )
            )
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
private fun SettingsScreen(memoCount: Int) {
    Column(Modifier.fillMaxSize()) {
        Header("設定")
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingRow("保存方式", "端末内保存")
            SettingRow("登録リスト数", "${memoCount}件")
            SettingRow("通信", "サーバーアクセスなし")
        }
    }
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
