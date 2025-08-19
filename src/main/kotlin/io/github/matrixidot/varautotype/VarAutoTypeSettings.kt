package io.github.matrixidot.varautotype

import com.intellij.openapi.components.*

@State(
    name = "VarAutoTypeSettings",
    storages = [Storage("varAutoTypeSettings.xml")]
)
class VarAutoTypeSettings : PersistentStateComponent<VarAutoTypeSettings.State> {
    data class State(
        var optimizeImportsAfterConvert: Boolean = true,
        var triggerOnEnter: Boolean = true
    )

    private var state = State()

    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }

    companion object {
        val instance: VarAutoTypeSettings
            get() = service()
    }
}
