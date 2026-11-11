package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.dungeon.DungeonAction
import com.joy.sandbox.core.dungeon.DungeonEvent
import com.joy.sandbox.core.dungeon.model.CharacterRef
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonGameInteractor
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonSoundPlayer
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonVibrationController
import com.joy.sandbox.feature.dungeon.impl.presentation.mapper.DungeonUiMapper
import com.joy.sandbox.feature.dungeon.impl.presentation.mapper.GameUiParams
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.PosUi
import java.util.ArrayDeque
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Игровой экран «Подземелья». Держит текущее состояние ядра и историю
 * для отмены; ядро ничего не знает про UI — ViewModel только вызывает его
 * и маппит результат в UiModel.
 */
class DungeonGameViewModel @Inject constructor(
    private val gameInteractor: DungeonGameInteractor,
    private val progressRepository: DungeonProgressRepository,
    private val soundPlayer: DungeonSoundPlayer,
    private val vibrationController: DungeonVibrationController,
    private val uiMapper: DungeonUiMapper,
    private val winTracker: WinTracker,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow<DungeonGameUiModel?>(null)

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenNext(val levelId: Int) : Commands

        data object ExitToTitle : Commands
    }

    private var gameState: DungeonState? = null
    private val history = ArrayDeque<DungeonState>()
    private var moveSeq = 0L
    private var soundEnabled = true
    private var vibrationEnabled = true

    fun init(levelId: Int) {
        if (gameState != null) return
        viewModelScope.launch {
            val progress = progressRepository.load()
            soundEnabled = progress.soundEnabled
            vibrationEnabled = progress.vibrationEnabled
            soundPlayer.setEnabled(soundEnabled)
            vibrationController.setEnabled(vibrationEnabled)
            gameState = gameInteractor.createState(DungeonLevels.byId(levelId))
            emit()
        }
    }

    fun onRoomClick(room: PosUi) {
        attempt(DungeonAction.Move(Pos(room.x, room.y)))
    }

    fun onDoorClick(doorId: String) {
        val door = DoorId.parse(doorId) ?: return
        attempt(DungeonAction.Toggle(door))
    }

    /**
     * Переключение активного персонажа (герой — id == null) через панель
     * или тап по фигуре на доске. Ход не тратится и в историю не пишется —
     * это выбор, чем ходить следующим тапом по клетке.
     */
    fun onCharacterClick(id: Int?) {
        val current = gameState ?: return
        val target = if (id == null) CharacterRef.Hero else CharacterRef.Hostage(id)
        val result = gameInteractor.act(current, DungeonAction.Switch(target))
        if (!result.ok) {
            soundPlayer.playDeny()
            return
        }
        gameState = result.state
        moveSeq++
        soundPlayer.play(result.events)
        emit()
    }

    fun onUndoClick() {
        val previous = history.pollLast() ?: return
        gameState = previous
        moveSeq++
        emit()
    }

    fun onRestartClick() {
        val state = gameState ?: return
        gameState = gameInteractor.createState(state.level)
        history.clear()
        winTracker.reset()
        moveSeq++
        emit()
    }

    fun onFeedbackToggle(sound: Boolean) {
        if (sound) {
            soundEnabled = !soundEnabled
            soundPlayer.setEnabled(soundEnabled)
            viewModelScope.launch { progressRepository.setSoundEnabled(soundEnabled) }
        } else {
            vibrationEnabled = !vibrationEnabled
            vibrationController.setEnabled(vibrationEnabled)
            viewModelScope.launch { progressRepository.setVibrationEnabled(vibrationEnabled) }
        }
        emit()
    }

    fun onNextClick() {
        val levelId = gameState?.level?.id ?: return
        commandsFlow.tryEmit(Commands.OpenNext(levelId + 1))
    }

    fun onExitToTitle() {
        commandsFlow.tryEmit(Commands.ExitToTitle)
    }

    private fun attempt(action: DungeonAction) {
        val current = gameState ?: return
        val result = gameInteractor.act(current, action)
        if (!result.ok) {
            soundPlayer.playDeny()
            return
        }
        history.addLast(current)
        gameState = result.state
        moveSeq++
        soundPlayer.play(result.events)
        if (result.events.any { it is DungeonEvent.Death }) {
            vibrationController.pulseOnDeath()
        }
        winTracker.recordWinIfNeeded(viewModelScope, result.state) { emit() }
        emit()
    }

    private fun emit() {
        val state = gameState ?: return
        stateFlow.value = uiMapper.mapGame(
            state,
            GameUiParams(
                canUndo = history.isNotEmpty(),
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                moveSeq = moveSeq,
                newRecord = winTracker.newRecord,
            ),
        )
    }
}
