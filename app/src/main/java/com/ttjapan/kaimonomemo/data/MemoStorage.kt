package com.ttjapan.kaimonomemo.data

import android.content.Context
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val PREF_NAME = "shopping_memo"
private const val PREF_MEMOS = "memos"
private const val PREF_ONE_HAND_MODE = "one_hand_mode_enabled"
private const val PREF_SIMPLE_MODE = "simple_mode_enabled"
private const val PREF_LEFT_HAND_MODE = "left_hand_mode_enabled"
private const val PREF_MIC_START_ON_LAUNCH = "mic_start_on_launch"
private const val PREF_MIC_STOP_TIMEOUT_MINUTES = "mic_stop_timeout_minutes"
private const val PREF_MIC_DISABLED = "mic_disabled"
private const val PREF_MIC_OPERATION_ENABLED = "mic_operation_enabled"
private const val PREF_MIC_COMMAND_PREFIX = "mic_command_"
private const val PREF_HOME_TITLE_PATTERN = "home_title_pattern"
private const val PREF_TEMPORARY_TITLE_PATTERN = "temporary_title_pattern"
private const val PREF_SUPPORT_AD_WATCH_DATE = "support_ad_watch_date"
private const val PREF_EDIT_HELP_VISIBLE = "edit_help_visible"
private const val TITLE_PATTERN_COUNT = 15

data class MicrophoneSettings(
    val disabled: Boolean = false,
    val startOnLaunch: Boolean = false,
    val stopTimeoutMinutes: Int = 0,
    val operationEnabled: Boolean = true,
    val commands: Map<String, String> = defaultMicrophoneCommands()
)

fun defaultMicrophoneCommands(): Map<String, String> {
    return linkedMapOf(
        "home" to "ホーム",
        "showTrash" to "ゴミ箱",
        "showItems" to "アイテム",
        "scrollUp" to "上スク",
        "scrollDown" to "下スク",
        "stop" to "ストップ",
        "focusNumber" to "（数字）番",
        "add" to "追加",
        "temporary" to "一時的",
        "complete" to "（数字番）完了",
        "delete" to "（数字番）削除",
        "restore" to "（数字番）戻す",
        "readAloud" to "（数字番）読み上げ",
        "finishMic" to "マイク停止",
        "exitApp" to "アプリ終了"
    )
}

fun loadMemos(context: Context): List<ShoppingMemo> {
    val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(PREF_MEMOS, null)
        ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            ShoppingMemo(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", ""),
                favorite = obj.optBoolean("favorite", false),
                trashed = obj.optBoolean("trashed", false),
                imagePattern = obj.optInt("imagePattern", 0),
                entries = readEntries(obj.optJSONArray("entries")),
                deletedEntries = readEntries(obj.optJSONArray("deletedEntries"))
            )
        }
    }.getOrDefault(emptyList())
}

fun saveMemos(context: Context, memos: List<ShoppingMemo>) {
    val array = JSONArray()
    memos.filter { it.trashed || it.title.isNotBlank() || it.entries.any { entry -> entry.name.isNotBlank() } || it.deletedEntries.any { entry -> entry.name.isNotBlank() } }
        .forEach { memo ->
        array.put(JSONObject().apply {
            put("id", memo.id)
            put("title", memo.title)
            put("favorite", memo.favorite)
            put("trashed", memo.trashed)
            put("imagePattern", memo.imagePattern)
            put("entries", JSONArray().also { entries ->
                memo.entries.filter { it.name.isNotBlank() }.forEach { entry ->
                    entries.put(JSONObject().apply {
                        put("id", entry.id)
                        put("name", entry.name)
                        put("checked", entry.checked)
                        put("colorMark", entry.colorMark)
                    })
                }
            })
            put("deletedEntries", JSONArray().also { entries ->
                memo.deletedEntries.filter { it.name.isNotBlank() }.forEach { entry ->
                    entries.put(JSONObject().apply {
                        put("id", entry.id)
                        put("name", entry.name)
                        put("checked", entry.checked)
                        put("colorMark", entry.colorMark)
                    })
                }
            })
        })
    }
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_MEMOS, array.toString())
        .apply()
}

fun loadOneHandModeEnabled(context: Context): Boolean {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_ONE_HAND_MODE, true)
}

fun saveOneHandModeEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_ONE_HAND_MODE, enabled)
        .apply()
}

fun loadSimpleModeEnabled(context: Context): Boolean {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_SIMPLE_MODE, true)
}

fun saveSimpleModeEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_SIMPLE_MODE, enabled)
        .apply()
}

fun loadLeftHandModeEnabled(context: Context): Boolean {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_LEFT_HAND_MODE, false)
}

fun saveLeftHandModeEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_LEFT_HAND_MODE, enabled)
        .apply()
}

fun loadMicrophoneSettings(context: Context): MicrophoneSettings {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val defaults = defaultMicrophoneCommands()
    return MicrophoneSettings(
        disabled = prefs.getBoolean(PREF_MIC_DISABLED, false),
        startOnLaunch = prefs.getBoolean(PREF_MIC_START_ON_LAUNCH, false),
        stopTimeoutMinutes = prefs.getInt(PREF_MIC_STOP_TIMEOUT_MINUTES, 0),
        operationEnabled = prefs.getBoolean(PREF_MIC_OPERATION_ENABLED, true),
        commands = defaults.mapValues { (key, defaultValue) ->
            val savedValue = prefs.getString(PREF_MIC_COMMAND_PREFIX + key, defaultValue) ?: defaultValue
            if (key == "finishMic" && savedValue == "マイク終了") defaultValue else savedValue
        }
    )
}

fun saveMicrophoneSettings(context: Context, settings: MicrophoneSettings) {
    val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        .putBoolean(PREF_MIC_DISABLED, settings.disabled)
        .putBoolean(PREF_MIC_START_ON_LAUNCH, settings.startOnLaunch)
        .putInt(PREF_MIC_STOP_TIMEOUT_MINUTES, settings.stopTimeoutMinutes)
        .putBoolean(PREF_MIC_OPERATION_ENABLED, settings.operationEnabled)
    defaultMicrophoneCommands().keys.forEach { key ->
        editor.putString(PREF_MIC_COMMAND_PREFIX + key, settings.commands[key].orEmpty())
    }
    editor.apply()
}

fun loadHomeTitlePattern(context: Context): Int {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getInt(PREF_HOME_TITLE_PATTERN, 0)
        .coerceIn(0, TITLE_PATTERN_COUNT - 1)
}

fun saveHomeTitlePattern(context: Context, pattern: Int) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(PREF_HOME_TITLE_PATTERN, pattern.coerceIn(0, TITLE_PATTERN_COUNT - 1))
        .apply()
}

fun loadTemporaryTitlePattern(context: Context): Int {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getInt(PREF_TEMPORARY_TITLE_PATTERN, 0)
        .coerceIn(0, TITLE_PATTERN_COUNT - 1)
}

fun saveTemporaryTitlePattern(context: Context, pattern: Int) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(PREF_TEMPORARY_TITLE_PATTERN, pattern.coerceIn(0, TITLE_PATTERN_COUNT - 1))
        .apply()
}

fun loadSupportAdWatchDate(context: Context): String {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString(PREF_SUPPORT_AD_WATCH_DATE, "")
        .orEmpty()
}

fun saveSupportAdWatchDate(context: Context, date: String) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_SUPPORT_AD_WATCH_DATE, date)
        .apply()
}

fun loadEditHelpVisible(context: Context): Boolean {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_EDIT_HELP_VISIBLE, true)
}

fun saveEditHelpVisible(context: Context, visible: Boolean) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_EDIT_HELP_VISIBLE, visible)
        .apply()
}

fun assignDefaultTitleIfBlank(memo: ShoppingMemo, memos: List<ShoppingMemo>) {
    if (memo.title.isNotBlank()) return
    val usedTitles = memos.asSequence()
        .filter { it.id != memo.id }
        .map { it.title }
        .toSet()
    var number = 1
    while ("タイトル$number" in usedTitles) number++
    memo.title = "タイトル$number"
}

private fun readEntries(array: JSONArray?): List<ShoppingEntry> {
    if (array == null) return emptyList()
    return List(array.length()) { index ->
        val obj = array.getJSONObject(index)
        ShoppingEntry(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            checked = obj.optBoolean("checked", false),
            colorMark = obj.optInt("colorMark", 0)
        )
    }
}
