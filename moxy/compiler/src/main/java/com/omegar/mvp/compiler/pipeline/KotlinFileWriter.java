package com.omegar.mvp.compiler.pipeline;


import com.squareup.kotlinpoet.FileSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
@SuppressWarnings("NewApi")
public class KotlinFileWriter extends Receiver<FileSpec> {

    private final Filer mFiler;

    public KotlinFileWriter(Filer Filer) {
        mFiler = Filer;
    }

    @Override
    public void receive(FileSpec file) {
        run(file);
    }

    private void run(FileSpec file) {
        try {
            file.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
