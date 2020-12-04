package com.omegar.mvp.compiler.pipeline;

import com.squareup.javapoet.JavaFile;

import java.util.List;

/**
 * Date: 27-Jul-17
 * Time: 10:26
 *
 * @author Evgeny Kursakov
 */
public abstract class JavaFileProcessor<M> extends Processor<M, JavaFile> {

	@Override
	public abstract JavaFile process(M input);

}

