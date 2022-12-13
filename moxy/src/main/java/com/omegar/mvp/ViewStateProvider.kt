package com.omegar.mvp

import com.omegar.mvp.viewstate.MvpViewState

/**
 * Date: 18.12.2015
 * Time: 13:15
 *
 * @author Yuri Shmakov
 */
abstract class ViewStateProvider {
    /**
     *
     * Presenter creates view state object by calling this method.
     *
     * @return view state class name
     */
    abstract val viewState: MvpViewState<*>
}