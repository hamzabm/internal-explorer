package com.tools.hamzabm.internaldataexplorer.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.*
import java.nio.channels.FileChannel

class CopyBuilder(context: Context) {
    private val TAG = Copy::class.qualifiedName
    private lateinit var fileName: String
    private  var fileNameWithExtension: String?=null
    private lateinit var sourcePath: String
    private lateinit var destinationPath: Uri
    private lateinit var sourceChannel: FileChannel
    private lateinit var destinationChannel: FileChannel
    private var destinationChannel2: OutputStream?=null

    /**
     * File name with no extension
     * @param fileName name of the file without its extension
     * @return CopyBuilder
     */
    fun filename(fileName: String): CopyBuilder {
        Log.d(TAG, "filename: $fileName")
        this.fileName = fileName
        return this
    }

    /**
     * File name with its extension
     * @param fileNameWithExtension
     * @return CopyBuilder
     */
    fun fileNameWithExtension(fileNameWithExtension: String): CopyBuilder {
        Log.d(TAG, "fileNameWithExtension: $fileNameWithExtension")
        this.fileNameWithExtension = fileNameWithExtension
        return this
    }

    /**
     * Path where file will be moved from
     * @param sourcePath the path to where file will be copied from
     * @return CopyBuilder
     */
    fun sourcePath(sourcePath: String): CopyBuilder {
        Log.d(TAG, "sourcePath: $sourcePath")
        this.sourcePath = sourcePath
        return this
    }

    /**
     * Path where file will be moved to
     * @param destinationPath the path to where file will be copied to
     * @return CopyBuilder
     */
    fun destinationPath(destinationPath: Uri): CopyBuilder {
        Log.d(TAG, "destinationPath: $destinationPath")
        this.destinationPath = destinationPath
        return this
    }

    /**
     * Create directory if it does not exist.
     * @return CopyBuilder
     */
    fun createDestinationDirIfNotExist(): CopyBuilder {
        val fileDestPath =
            File(destinationPath.toString()) //set file destination. ie. the destination path where the file will end up in
        if (!fileDestPath.exists()) {
            var filecreated = fileDestPath.mkdirs()
            Log.d(
                TAG,
                "createDestinationDirIfNotExist: Directory not found. Creating directory at $fileDestPath"
            )
            Log.d(
                TAG,
                "Filecreated : $filecreated"
            )

        } //if the destination path does not exist, create it.
        return this
    }

    /**
     * Start copy
     */
    fun copytoExternel(context: Context,onCopy: (finished: Boolean) -> Any) {
        createDestinationDirIfNotExist()
        val source = File(sourcePath)

        Log.d(TAG, "build: successful")

        this.destinationChannel2 = context.contentResolver.openOutputStream(destinationPath)

        //do transfer
        this.destinationChannel2?.write(FileInputStream(source).readBytes())


        this.destinationChannel2?.close()
        Log.d(TAG, "build: successful")
        onCopy(true)
    }
    /**
     * Start copy
     */
    fun copy(onCopy: (finished: Boolean) -> Any) {
        createDestinationDirIfNotExist()
        val source = File(sourcePath)
        val destination:File
        if(fileNameWithExtension!=null){
             destination = File(destinationPath.toString(), fileNameWithExtension)
        }else {
             destination = File(destinationPath.toString())
        }

        Log.d(TAG, "build: successful")

        this.sourceChannel = FileInputStream(source).channel
        this.destinationChannel = FileOutputStream(destination).channel

        //do transfer
        this.destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())

        //close channels
        this.sourceChannel.close()
        this.destinationChannel.close()
        Log.d(TAG, "build: successful")
        onCopy(true)
    }

    fun savefile(sourceuri: Uri, destinationFilename: String) {
        val sourceFilename: String? = sourceuri.path

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(sourceFilename))
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) !== -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (bis != null) bis.close()
                if (bos != null) bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

class Copy {

    companion object {
        /**
         * @param context context
         * @return CopyBuilder
         */
        fun with(context: Context): CopyBuilder {
            return CopyBuilder(context)
        }
    }
}