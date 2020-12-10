package com.omegar.mvp.compiler.pipeline;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class JavaFileWriter extends Receiver<JavaFile> {

    private final Filer mFiler;

    public JavaFileWriter(Filer Filer) {
        mFiler = Filer;
    }

    @Override
    public void receive(JavaFile file) {
        try {
            file.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
