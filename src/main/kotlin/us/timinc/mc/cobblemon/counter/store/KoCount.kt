package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class KoCount : PlayerDataExtension {
    companion object {
        const val NAME = "koCount"
    }

    private val koCounts = mutableMapOf<String, Int>()

    fun reset() {
        koCounts.clear()
    }

    fun add(speciesName: String) {
        koCounts[speciesName] =
            get(speciesName) + 1
    }

    fun get(speciesName: String): Int {
        return koCounts.getOrDefault(speciesName, 0)
    }

    override fun deserialize(json: JsonObject): KoCount {
        val defeatsData = json.getAsJsonObject("defeats")
        for (speciesName in defeatsData.keySet()) {
            koCounts[speciesName] = defeatsData.get(speciesName).asInt
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
        for (speciesName in koCounts.keys) {
            defeatsData.addProperty(speciesName, koCounts[speciesName])
        }
        json.add("defeats", defeatsData)

        return json
    }
}