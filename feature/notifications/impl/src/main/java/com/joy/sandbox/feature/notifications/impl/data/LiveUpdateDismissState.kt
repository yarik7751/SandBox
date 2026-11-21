package com.joy.sandbox.feature.notifications.impl.data

/**
 * Факт отмены Live Update пользователем (свайп/открепление через deleteIntent).
 *
 * Kotlin object, а не Dagger-синглтон: receiver зарегистрирован в манифесте
 * и не проходит инъекцию, ему нужен доступ к тому же флагу напрямую.
 *
 * Контракт Android: если пользователь убрал Live Update, приложение обязано
 * прекратить его обновлять — иначе система отзывает у приложения право
 * на promotion (promoted notifications). Пользователь может отклонить
 * уведомление в любой момент, пока приложение спит, поэтому флаг живёт
 * во времени жизни процесса, а не во ViewModel.
 */
object LiveUpdateDismissState {

    @Volatile
    private var dismissedByUser: Boolean = false

    fun markDismissed() {
        dismissedByUser = true
    }

    /** Одноразовое чтение: вернёт true только первый раз после отмены. */
    fun consumeDismissal(): Boolean {
        val wasDismissed = dismissedByUser
        dismissedByUser = false
        return wasDismissed
    }
}
