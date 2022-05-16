package com.omegar.mvp.compiler.pipeline;

import com.squareup.kotlinpoet.FileSpec;

/**
 * Date: 27-Jul-17
 * Time: 10:26
 *
 * @author Evgeny Kursakov
 */
public abstract class KotlinFileProcessor<M> extends Processor<M, FileSpec> {

	@Override
	public abstract FileSpec process(M input);

}

