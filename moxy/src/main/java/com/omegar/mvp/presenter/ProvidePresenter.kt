package com.omegar.mvp.presenter


/**
 * Called when Moxy can't find right presenter instance in [PresenterStore].
 *
 * Attention! **Don't use manually method marked with this annotation!** Use presenter field, which you want. If you override this method in inherited classes, make them return same type(not requirements but recommendation).
 *
 * Requirements:
 *
 *  * Method should return full equals class as presenter field type
 *  * Presenter Types should be same
 *  * Tags should be equals
 *  * Presenter IDs should be equals
 *
 *
 * Note: if this method stay unused after build, then Moxy never use this method and you should check annotation parameters. These parameters should be equals to @InjectPresenter parameters
 * <br></br>
 * Date: 14.10.2016
 * Time: 00:09
 *
 * @author Yuri Shmakov
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProvidePresenter(
    val tag: String = "",
    val type: PresenterType = PresenterType.LOCAL,
    val presenterId: String = ""
)