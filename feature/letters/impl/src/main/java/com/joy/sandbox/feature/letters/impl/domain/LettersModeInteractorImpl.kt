package com.joy.sandbox.feature.letters.impl.domain

import javax.inject.Inject

class LettersModeInteractorImpl @Inject constructor(
    private val repository: LettersModeRepository,
) : LettersModeInteractor {

    override fun loadLastMode(): LettersMode = repository.loadMode()

    override fun saveMode(mode: LettersMode) {
        repository.saveMode(mode)
    }
}
