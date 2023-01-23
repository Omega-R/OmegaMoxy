package com.omegar.mvp.compiler.pipeline

import javax.lang.model.element.Element

/**
 * Date: 27-Jul-17
 * Time: 10:31
 *
 * @author Evgeny Kursakov
 */
abstract class ElementProcessor<E : Element, M> : Processor<E, M>()