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

    private final ExecutorService mAsyncExecutors = Executors.newCachedThreadPool();
    private final Filer mFiler;

    public JavaFileWriter(Filer Filer) {
        mFiler = Filer;
    }

    @Override
    public void receive(JavaFile file) {
        if (false && isAsyncRun(file)) {
            mAsyncExecutors.submit(() -> run(file));
        } else {
            run(file);
        }
    }

    private boolean isAsyncRun(JavaFile file) {
        return !mAsyncExecutors.isShutdown() && file.typeSpec.methodSpecs.size() >= 1;
    }

    private void run(JavaFile file) {
        try {
            file.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        mAsyncExecutors.shutdown();

        try {
            mAsyncExecutors.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
