package com.omegar.mvp

interface MvpKeyStore<KeyStore: MvpKeyStore<KeyStore>> {

    fun getString(key: String): String?

    fun putString(key: String, value: String?)

    fun getKeyStore(key: String): KeyStore?

    fun putKeyStore(key: String, keyStore: KeyStore?)

    fun putKeyStore(key: String): KeyStore

    fun containsKey(key: String): Boolean

    fun putAll(keyStore: KeyStore)
}