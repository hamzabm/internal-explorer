package com.tools.hamzabm.internaldataexplorer

import android.app.Activity
import android.content.Intent

 class InternalDataExplorer(val path:String,val activity: Activity) {
    init {

    }

   fun launch(){

     val intent =  Intent(activity,DataExplorerActivity::class.java)

       intent.putExtra("path",path)
        activity.startActivity(intent)
   }
}