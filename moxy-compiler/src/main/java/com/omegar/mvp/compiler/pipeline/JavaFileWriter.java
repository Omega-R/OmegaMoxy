package com.omegar.mvp.compiler.pipeline;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class JavaFileWriter extends Receiver<JavaFile> {

    private final ProcessingEnvironment mProcessingEnv;

    public JavaFileWriter(ProcessingEnvironment processingEnvironment) {
        mProcessingEnv = processingEnvironment;
    }

    @Override
    public void receive(JavaFile file) {
        try {
            file.writeTo(mProcessingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
