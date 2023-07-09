package com.aldarius.horoscop

import android.content.Context
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CopiaAssets(private var pattern: String, private var ct: Context) {
    internal fun copy() {
        val assetManager = ct.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }

        var outdir = ct.filesDir.toString() + File.separator + "/ephe"
        File(outdir).mkdirs()
        outdir += File.separator

        for (filename in files!!) {
            if (File(outdir + filename).exists() || !filename.matches(pattern.toRegex())) {
                continue
            }

            var input: InputStream?
            var out: OutputStream?
            try {
                input = assetManager.open(filename)

                val outFile = File(outdir, filename)

                out = FileOutputStream(outFile)
                //copyFile(input!!, out)
                input.copyTo(out, 1024)
                input.close()
                //input = null
                out.flush()
                out.close()
                //out = null
            } catch (e: IOException) {
                Log.e("tag", "Failed to copy asset file: $filename", e)
            }

        }
    }

    /*
    @Throws(IOException::class)
    private fun copyFile(input: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read)
        }
    }
    */
}
