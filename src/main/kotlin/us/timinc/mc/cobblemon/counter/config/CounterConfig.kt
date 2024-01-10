package us.timinc.mc.cobblemon.counter.config

import com.google.gson.GsonBuilder
import us.timinc.mc.cobblemon.counter.Counter
import java.io.File
import java.io.FileReader
import java.io.PrintWriter

class CounterConfig {
    val debug = false;
    val broadcastKosToPlayer = true
    val broadcastCapturesToPlayer = true

    class Builder {
        companion object {
            fun load() : CounterConfig {
                val gson = GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create()

                var config = CounterConfig()
                val configFile = File("config/${Counter.MOD_ID}.json")
                configFile.parentFile.mkdirs()

                if (configFile.exists()) {
                    try {
                        val fileReader = FileReader(configFile)
                        config = gson.fromJson(fileReader, CounterConfig::class.java)
                        fileReader.close()
                    } catch (e: Exception) {
                        println("Error reading config file")
                    }
                }

                val pw = PrintWriter(configFile)
                gson.toJson(config, pw)
                pw.close()

                return config
            }
        }
    }
}