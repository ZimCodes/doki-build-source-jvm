package io.unthrottled.doki.build.jvm.tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy

object CommonConstructionFunctions {
  val gson: Gson = GsonBuilder()
    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .setPrettyPrinting().create()
}
