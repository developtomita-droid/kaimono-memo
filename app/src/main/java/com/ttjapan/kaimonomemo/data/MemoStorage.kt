package com.ttjapan.kaimonomemo.data

import android.content.Context
import com.ttjapan.kaimonomemo.model.ShoppingEntry
import com.ttjapan.kaimonomemo.model.ShoppingMemo
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val PREF_NAME = "shopping_memo"
private const val PREF_MEMOS = "memos"

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
                entries = readEntries(obj.optJSONArray("entries")),
                deletedEntries = readEntries(obj.optJSONArray("deletedEntries"))
            )
        }
    }.getOrDefault(emptyList())
}

fun saveMemos(context: Context, memos: List<ShoppingMemo>) {
    val array = JSONArray()
    memos.filter { it.title.isNotBlank() || it.entries.any { entry -> entry.name.isNotBlank() } || it.deletedEntries.any { entry -> entry.name.isNotBlank() } }
        .forEach { memo ->
        array.put(JSONObject().apply {
            put("id", memo.id)
            put("title", memo.title)
            put("favorite", memo.favorite)
            put("entries", JSONArray().also { entries ->
                memo.entries.filter { it.name.isNotBlank() }.forEach { entry ->
                    entries.put(JSONObject().apply {
                        put("id", entry.id)
                        put("name", entry.name)
                        put("checked", entry.checked)
                    })
                }
            })
            put("deletedEntries", JSONArray().also { entries ->
                memo.deletedEntries.filter { it.name.isNotBlank() }.forEach { entry ->
                    entries.put(JSONObject().apply {
                        put("id", entry.id)
                        put("name", entry.name)
                        put("checked", entry.checked)
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
            checked = obj.optBoolean("checked", false)
        )
    }
}
