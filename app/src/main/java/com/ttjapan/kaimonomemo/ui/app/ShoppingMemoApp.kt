package com.ttjapan.kaimonomemo.ui.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ttjapan.kaimonomemo.R
import com.ttjapan.kaimonomemo.data.assignDefaultTitleIfBlank
import com.ttjapan.kaimonomemo.data.loadMemos
import com.ttjapan.kaimonomemo.data.saveMemos
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import com.ttjapan.kaimonomemo.voice.ContinuousSpeechController
import kotlinx.coroutines.launch

private enum class Screen {
    Home,
    Detail,
    Map,
    Settings,
    Favorites
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
        selectedMemo?.let { assignDefaultTitleIfBlank(it, memos) }
        persist()
        currentScreen = Screen.Home
    }
    fun addMemo() {
        val memo = ShoppingMemo(entries = listOf(ShoppingEntry(name = "")))
        memos.add(memo)
        persist()
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
    val tabs = listOf("アイテム", "削除済")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var showTitleRequiredDialog by remember { mutableStateOf(false) }

    val speechController = remember(memo.id) {
        ContinuousSpeechController(context) { text ->
            memo.entries.add(ShoppingEntry(name = text))
            onChanged()
        }
    }
    DisposableEffect(speechController) {
        onDispose { speechController.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) speechController.start()
    }

    LaunchedEffect(memo.id) {
        if (memo.title.isBlank()) titleFocusRequester.requestFocus()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = memo.title,
                    onValueChange = {
                        memo.title = it
                        onChanged()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(titleFocusRequester),
                    textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    placeholder = { Text("タイトル入力") }
                )
                if (pagerState.currentPage == 1 && memo.deletedEntries.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            memo.deletedEntries.clear()
                            onChanged()
                        }
                    ) {
                        Text("完全削除", color = Color(0xFFD32F2F))
                    }
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color(0xFF1976D2) else Color(0xFF777777)
                        )
                        Spacer(Modifier.height(9.dp))
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .fillMaxWidth()
                                .background(if (selected) Color(0xFF1565C0) else Color.Transparent)
                        )
                    }
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page == 0) {
                    ActiveItemsPage(memo, selectedEntryId, { selectedEntryId = it }, onChanged)
                } else {
                    DeletedItemsPage(
                        memo = memo,
                        onRestore = {
                            memo.deletedEntries.remove(it)
                            memo.entries.add(it.copy(checked = false))
                            onChanged()
                        }
                    )
                }
            }
        }

        MicFab(
            controller = speechController,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp),
            onClick = {
                if (memo.title.isBlank()) {
                    showTitleRequiredDialog = true
                    titleFocusRequester.requestFocus()
                    return@MicFab
                }
                if (speechController.partialText.isNotBlank()) {
                    speechController.commitPartial()
                    return@MicFab
                }
                if (speechController.isRunning) {
                    speechController.stop()
                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    speechController.start()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        )
    }

    if (showTitleRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showTitleRequiredDialog = false },
            title = { Text("タイトルを入力してください") },
            confirmButton = {
                TextButton(onClick = { showTitleRequiredDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun ActiveItemsPage(
    memo: ShoppingMemo,
    selectedEntryId: String?,
    onSelect: (String) -> Unit,
    onChanged: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        items(memo.entries, key = { it.id }) { entry ->
            ShoppingEntryRow(
                entry = entry,
                selected = selectedEntryId == entry.id,
                canMoveUp = memo.entries.indexOf(entry) > 0,
                canMoveDown = memo.entries.indexOf(entry) < memo.entries.lastIndex,
                onSelect = { onSelect(entry.id) },
                onNameChanged = {
                    entry.name = it
                    onChanged()
                },
                onChecked = {
                    entry.checked = it
                    if (it) {
                        memo.entries.remove(entry)
                        memo.entries.add(entry)
                    }
                    onChanged()
                },
                onDelete = {
                    memo.entries.remove(entry)
                    memo.deletedEntries.add(entry.copy(checked = false))
                    onChanged()
                },
                onMove = { direction ->
                    val index = memo.entries.indexOf(entry)
                    val target = index + direction
                    if (target in 0 until memo.entries.size) {
                        memo.entries.removeAt(index)
                        memo.entries.add(target, entry)
                        onChanged()
                    }
                }
            )
        }
        item {
            TextButton(
                onClick = {
                    memo.entries.add(ShoppingEntry(name = ""))
                    onChanged()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("+ リストアイテム", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ShoppingEntryRow(
    entry: ShoppingEntry,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSelect: () -> Unit,
    onNameChanged: (String) -> Unit,
    onChecked: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onMove: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFFE3F2FD) else Color.White)
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = entry.checked, onCheckedChange = onChecked)
            OutlinedTextField(
                value = entry.name,
                onValueChange = onNameChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    textDecoration = if (entry.checked) TextDecoration.LineThrough else TextDecoration.None
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                placeholder = { Text("買うもの") }
            )
            IconButton(onClick = onDelete) {
                Text("🗑", fontSize = 24.sp)
            }
        }
        if (selected) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(enabled = canMoveUp, onClick = { onMove(-1) }) { Text("上へ") }
                Button(enabled = canMoveDown, onClick = { onMove(1) }) { Text("下へ") }
            }
        }
    }
    Divider(color = Color(0xFFE0E0E0))
}

@Composable
private fun DeletedItemsPage(
    memo: ShoppingMemo,
    onRestore: (ShoppingEntry) -> Unit
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
                Text(entry.name.ifBlank { "無題のアイテム" }, fontSize = 19.sp, color = Color(0xFF666666), modifier = Modifier.weight(1f))
                TextButton(onClick = { onRestore(entry) }) { Text("戻す") }
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
            containerColor = if (hasPartial) Color(0xFF16A34A) else if (controller.isRunning) Color(0xFFD32F2F) else Color(0xFF1976D2),
            modifier = Modifier.size(78.dp)
        ) {
            Text(
                text = if (hasPartial) "登録" else if (controller.isRunning) "■" else "🎙",
                color = Color.White,
                fontSize = if (hasPartial) 18.sp else 34.sp,
                fontWeight = FontWeight.Bold
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
