package com.tools.hamzabm.internaldataexplorer.utils

import java.io.File
import java.io.InputStream
import java.io.OutputStream


fun File.copyInputStreamToFile(
    inputStream: InputStream,
    onCopy: (totalBytesCopied: Long, bytesJustCopied: Int, finished: Boolean) -> Any
) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut, onCopy)
    }
}


fun File.size():String{

 var size  = (this.length() / 1024/1024)

    if(size==0L){
        size  = (this.length() / 1024)
        return "$size Kb"
    }
    return "$size Mb"

}

fun InputStream.copyTo(
    out: OutputStream,
    onCopy: (totalBytesCopied: Long, bytesJustCopied: Int, finished: Boolean) -> Any
) {
    var bytesCopied: Long = 0
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        onCopy(bytesCopied, bytes, false)
        bytes = read(buffer)
    }
    onCopy(bytesCopied, bytes, true)

}