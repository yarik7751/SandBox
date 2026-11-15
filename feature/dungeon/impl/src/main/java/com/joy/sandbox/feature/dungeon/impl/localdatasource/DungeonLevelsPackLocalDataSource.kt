package com.joy.sandbox.feature.dungeon.impl.localdatasource

import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Чтение JSON-пака уровней из assets (docs/dungeon-generation.md, вариант А):
 * пак зашит в сборку, приложение грузит его при старте. Формат версионируемый;
 * новые паки добавляются без изменения кода чтения. Битый/отсутствующий пак
 * не роняет игру — работают рукотворные уровни из [com.joy.sandbox.core.dungeon.model.DungeonLevels].
 */
@Singleton
class DungeonLevelsPackLocalDataSource @Inject constructor(
    private val context: Context,
) {

    /** Пак из assets; при любой ошибке чтения — пустой (рукотворные уровни остаются). */
    suspend fun loadPack(): DungeonLevelsPackDto = withContext(Dispatchers.IO) {
        runCatching { readPack() }
            .onFailure { error -> Log.w(TAG, "пак уровней не прочитан: ${error.message}") }
            .getOrElse { EMPTY_PACK }
    }

    private fun readPack(): DungeonLevelsPackDto {
        val text = context.assets.open(PACK_ASSET).bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val version = root.getInt(KEY_VERSION)
        require(version == SUPPORTED_VERSION) { "неподдерживаемая версия пака: $version" }

        val levelsJson = root.getJSONArray(KEY_LEVELS)
        val levels = (0 until levelsJson.length()).map { index -> parseLevel(levelsJson.getJSONObject(index)) }
        return DungeonLevelsPackDto(version = version, levels = levels)
    }

    private fun parseLevel(json: JSONObject): DungeonLevelEntryDto = DungeonLevelEntryDto(
        id = json.getInt(KEY_ID),
        name = json.getString(KEY_NAME),
        par = json.getInt(KEY_PAR),
        w = json.getInt(KEY_W),
        h = json.getInt(KEY_H),
        player = json.getPosition(KEY_PLAYER),
        enemy = json.getPosition(KEY_ENEMY),
        hostages = json.getPositionList(KEY_HOSTAGES),
        closed = json.getStringList(KEY_CLOSED),
        blocked = json.getPositionList(KEY_BLOCKED),
        concealed = json.getPositionList(KEY_CONCEALED),
        jammed = json.getStringList(KEY_JAMMED),
        intro = json.optString(KEY_INTRO),
        togglesBudget = json.optInt(KEY_TOGGLES_BUDGET, DEFAULT_TOGGLES_BUDGET),
        enemyDoorCharge = json.optInt(KEY_ENEMY_DOOR_CHARGE, DEFAULT_ENEMY_DOOR_CHARGE),
    )

    private fun JSONObject.getPosition(key: String): Pair<Int, Int> =
        getJSONObject(key).let { it.getInt(KEY_X) to it.getInt(KEY_Y) }

    private fun JSONObject.getPositionList(key: String): List<Pair<Int, Int>> {
        val array = getJSONArray(key)
        return (0 until array.length()).map { index ->
            array.getJSONObject(index).let { it.getInt(KEY_X) to it.getInt(KEY_Y) }
        }
    }

    private fun JSONObject.getStringList(key: String): List<String> {
        val array = getJSONArray(key)
        return (0 until array.length()).map { index -> array.getString(index) }
    }

    private companion object {

        const val PACK_ASSET = "dungeon_levels_pack.json"
        const val SUPPORTED_VERSION = 1

        const val TAG = "DungeonLevelsPack"

        const val KEY_VERSION = "version"
        const val KEY_LEVELS = "levels"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_PAR = "par"
        const val KEY_W = "w"
        const val KEY_H = "h"
        const val KEY_PLAYER = "player"
        const val KEY_ENEMY = "enemy"
        const val KEY_HOSTAGES = "hostages"
        const val KEY_CLOSED = "closed"
        const val KEY_BLOCKED = "blocked"
        const val KEY_CONCEALED = "concealed"
        const val KEY_JAMMED = "jammed"
        const val KEY_INTRO = "intro"
        const val KEY_TOGGLES_BUDGET = "togglesBudget"
        const val KEY_ENEMY_DOOR_CHARGE = "enemyDoorCharge"
        const val KEY_X = "x"
        const val KEY_Y = "y"

        const val DEFAULT_TOGGLES_BUDGET = Int.MAX_VALUE
        const val DEFAULT_ENEMY_DOOR_CHARGE = 0

        val EMPTY_PACK = DungeonLevelsPackDto(version = SUPPORTED_VERSION, levels = emptyList())
    }
}
