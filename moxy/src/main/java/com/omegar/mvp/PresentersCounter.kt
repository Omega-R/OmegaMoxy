package com.omegar.mvp

/**
 * Date: 14-Nov-16
 * Time: 04:39
 *
 * @author Yuri Shmakov
 */
object PresentersCounter {
    private val connections = HashMap<MvpPresenter<*>, MutableSet<String>>()
    private val tags = HashMap<String, MutableSet<MvpPresenter<*>>>()

    /**
     * Save delegate tag when it inject presenter to delegate's object
     *
     * @param presenter     Injected presenter
     * @param delegateTag   Delegate tag
     */
    fun injectPresenter(presenter: MvpPresenter<*>, delegateTag: String) {
        connections.getOrPut(presenter).add(delegateTag)
        tags.getOrPut(delegateTag).add(presenter)
    }

    private fun <K, V> MutableMap<K, MutableSet<V>>.getOrPut(key: K) = getOrPut(key) { HashSet() }

    /**
     * Remove tag when delegate's object was fully destroyed
     *
     * @param presenter     Rejected presenter
     * @param delegateTag   Delegate tag
     * @return              True if there are no links to this presenter and presenter be able to destroy. False otherwise
     */
    fun rejectPresenter(presenter: MvpPresenter<*>, delegateTag: String): Boolean {
        tags[delegateTag]?.let { presenters ->
            presenters.remove(presenter)

            if (presenters.isEmpty()) {
                tags.remove(delegateTag)
            }
        }

        val delegateTags = connections[presenter]
            ?.takeIf { it.isNotEmpty() }
            ?: let {
                connections.remove(presenter)
                return true
            }

        if (delegateTags.removeAll { tag -> tag.startsWith(delegateTag) }) {
            if (delegateTags.isEmpty()) {
                connections.remove(presenter)
                return true
            }
        }

        return false
    }

    fun getAll(delegateTag: String): Set<MvpPresenter<*>> {
        return tags.flatMapTo(HashSet()) { (key, value) ->
            if (key.startsWith(delegateTag)) value else emptyList()
        }
    }

    fun isInjected(presenter: MvpPresenter<*>): Boolean = connections[presenter]?.isNotEmpty() == true

}