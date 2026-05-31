package com.ttjapan.kaimonomemo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class ShoppingEntry(
    val id: String = UUID.randomUUID().toString(),
    name: String,
    checked: Boolean = false
) {
    var name by mutableStateOf(name)
    var checked by mutableStateOf(checked)

    fun copy(
        id: String = this.id,
        name: String = this.name,
        checked: Boolean = this.checked
    ) = ShoppingEntry(id = id, name = name, checked = checked)
}

class ShoppingMemo(
    val id: String = UUID.randomUUID().toString(),
    title: String = "",
    favorite: Boolean = false,
    entries: List<ShoppingEntry> = emptyList(),
    deletedEntries: List<ShoppingEntry> = emptyList()
) {
    var title by mutableStateOf(title)
    var favorite by mutableStateOf(favorite)
    val entries = mutableStateListOf<ShoppingEntry>().also { it.addAll(entries) }
    val deletedEntries = mutableStateListOf<ShoppingEntry>().also { it.addAll(deletedEntries) }
}
