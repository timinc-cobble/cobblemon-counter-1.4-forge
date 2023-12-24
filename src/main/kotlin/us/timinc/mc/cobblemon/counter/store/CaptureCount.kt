package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class CaptureCount : PlayerDataExtension {
    companion object {
        const val NAME = "captureCount"
    }

    private val captureCounts = mutableMapOf<String, Int>()

    fun reset() {
        captureCounts.clear()
    }

    fun add(speciesName: String) {
        captureCounts[speciesName] =
            get(speciesName) + 1
    }

    fun get(speciesName: String): Int {
        return captureCounts.getOrDefault(speciesName, 0)
    }

    fun total(): Int {
        return captureCounts.values.reduceOrNull { acc, i -> acc + i } ?: 0
    }

    override fun deserialize(json: JsonObject): CaptureCount {
        val defeatsData = json.getAsJsonObject("defeats")
        for (speciesName in defeatsData.keySet()) {
            captureCounts[speciesName] = defeatsData.get(speciesName).asInt
        }

        return this
    }

    override fun name(): String {
        return NAME
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", NAME)

        val defeatsData = JsonObject()
        for (speciesName in captureCounts.keys) {
            defeatsData.addProperty(speciesName, captureCounts[speciesName])
        }
        json.add("defeats", defeatsData)

        return json
    }
}