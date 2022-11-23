package com.omegar.mvp

interface MvpSaveStore<SaveStore: MvpSaveStore<SaveStore>> {

    fun getString(key: String): String?

    fun putString(key: String, value: String?)

    fun getKeyStore(key: String): SaveStore?

    fun putKeyStore(key: String, keyStore: SaveStore?)

    fun putKeyStore(key: String): SaveStore

    fun containsKey(key: String): Boolean

    fun putAll(keyStore: SaveStore)
}