package com.tools.hamzabm.internaldataexplorer.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.toyyyyMMddHHmmFromat(): String {
    try {
        var df = SimpleDateFormat("ddMMMyyyy HH:mm")
        return df.format(this)
    }catch (e:Exception){
        return  ""
    }

}