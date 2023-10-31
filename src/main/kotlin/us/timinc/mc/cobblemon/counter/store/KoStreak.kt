package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class KoStreak : PlayerDataExtension {
    companion object {
        const val NAME = "koStreak"
    }

    var species = ""
    var count = 0

    fun reset() {
        species = ""
        count = 0
    }

    fun add(speciesName: String) {
        if (speciesName == species) {
            count++
        } else {
            species = speciesName
            count = 1
        }
    }

    fun get(speciesName: String): Int {
        if (speciesName == species) {
            return count
        }
        return 0
    }

    override fun deserialize(json: JsonObject): KoStreak {
        species = json.get("species").asString
        count = json.get("count").asInt

        return this
    }

    override fun name(): String {
        return NAME
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()

        json.addProperty("name", NAME)
        json.addProperty("species", species)
        json.addProperty("count", count)

        return json
    }
}