package com.omegar.mvp

interface MvpDelegateHolder<D: Any> {

    val mvpDelegate: MvpDelegate<D>

}