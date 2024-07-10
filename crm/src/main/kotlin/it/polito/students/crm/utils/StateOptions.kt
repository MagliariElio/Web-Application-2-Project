package it.polito.students.crm.utils

enum class StateOptions {
    RECEIVED,
    READ,
    DISCARDED,
    PROCESSING,
    DONE,
    FAILED
}

fun canTransition(currentState: StateOptions, newState: StateOptions): Boolean {
    return when (currentState) {
        StateOptions.RECEIVED -> newState == StateOptions.READ
        StateOptions.READ -> newState == StateOptions.DISCARDED
                || newState == StateOptions.PROCESSING
                || newState == StateOptions.DONE
                || newState == StateOptions.FAILED

        StateOptions.DISCARDED -> false
        StateOptions.PROCESSING -> newState == StateOptions.DONE
                || newState == StateOptions.FAILED

        StateOptions.DONE -> false
        StateOptions.FAILED -> false
    }
}