package com.omegar.mvp.compiler.pipeline;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.processing.Filer;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
@SuppressWarnings("NewApi")
public class JavaFileWriter extends Receiver<JavaFile> {

    private final ExecutorService mExecutors = Executors.newCachedThreadPool();
    private final Filer mFiler;

    public JavaFileWriter(Filer Filer) {
        mFiler = Filer;
    }

    @Override
    public void receive(JavaFile file) {
        if (!mExecutors.isShutdown() && file.typeSpec.methodSpecs.size() >= 1) {
            mExecutors.submit(() -> run(file));
        } else {
            run(file);
        }
    }

    private void run(JavaFile file) {
        try {
            file.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        List<Runnable> runnables = mExecutors.shutdownNow();
        for (Runnable runnable : runnables) {
            runnable.run();
        }

        try {
            mExecutors.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
