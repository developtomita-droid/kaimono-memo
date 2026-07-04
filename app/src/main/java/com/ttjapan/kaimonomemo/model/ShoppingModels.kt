package com.ttjapan.kaimonomemo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class ShoppingEntry(
    val id: String = UUID.randomUUID().toString(),
    name: String,
    checked: Boolean = false,
    colorMark: Int = 0
) {
    var name by mutableStateOf(name)
    var checked by mutableStateOf(checked)
    var colorMark by mutableStateOf(colorMark)

    fun copy(
        id: String = this.id,
        name: String = this.name,
        checked: Boolean = this.checked,
        colorMark: Int = this.colorMark
    ) = ShoppingEntry(id = id, name = name, checked = checked, colorMark = colorMark)
}

class ShoppingMemo(
    val id: String = UUID.randomUUID().toString(),
    title: String = "",
    favorite: Boolean = false,
    trashed: Boolean = false,
    imagePattern: Int = 0,
    customImageUri: String? = null,
    entries: List<ShoppingEntry> = emptyList(),
    deletedEntries: List<ShoppingEntry> = emptyList()
) {
    var title by mutableStateOf(title)
    var favorite by mutableStateOf(favorite)
    var trashed by mutableStateOf(trashed)
    var imagePattern by mutableStateOf(imagePattern)
    var customImageUri by mutableStateOf(customImageUri)
    val entries = mutableStateListOf<ShoppingEntry>().also { it.addAll(entries) }
    val deletedEntries = mutableStateListOf<ShoppingEntry>().also { it.addAll(deletedEntries) }
}
