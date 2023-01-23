package com.omegar.mvp.compiler.pipeline

import com.squareup.kotlinpoet.FileSpec
import java.io.IOException
import javax.annotation.processing.Filer

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
class KotlinFileWriter(private val mFiler: Filer) : Receiver<KotlinFile>() {

    override fun receive(input: FileSpec) {
        try {
            input.writeTo(mFiler)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}