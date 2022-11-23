package com.omegar.mvp

import android.os.Bundle

@JvmInline
internal value class BundleMvpKeyStore(private val bundle: Bundle) : MvpSaveStore<BundleMvpKeyStore> {

    override fun getString(key: String): String? = bundle.getString(key)

    override fun putString(key: String, value: String?) = bundle.putString(key, value)

    override fun getKeyStore(key: String): BundleMvpKeyStore? = bundle.getBundle(key)?.toKeyStore()

    override fun putKeyStore(key: String, keyStore: BundleMvpKeyStore?) {
        bundle.putBundle(key, keyStore?.bundle)
    }

    override fun putKeyStore(key: String): BundleMvpKeyStore {
        val keyStore = Bundle().toKeyStore()
        putKeyStore(key, keyStore)
        return keyStore
    }

    override fun containsKey(key: String): Boolean {
        return bundle.containsKey(key)
    }

    override fun putAll(keyStore: BundleMvpKeyStore) {
        bundle.putAll(keyStore.bundle)
    }
}

internal fun Bundle.toKeyStore() = BundleMvpKeyStore(this)