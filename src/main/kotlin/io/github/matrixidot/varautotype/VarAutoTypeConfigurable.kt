package io.github.matrixidot.varautotype

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import javax.swing.JComponent
import com.intellij.ui.dsl.builder.panel

class VarAutoTypeConfigurable : SearchableConfigurable {
    private var modified = false
    private val settings get() = VarAutoTypeSettings.Companion.instance.state

    private var optimizeImports = settings.optimizeImportsAfterConvert
    private var triggerOnEnter   = settings.triggerOnEnter

    override fun getId(): String = "io.github.matrixidot.varautotype.settings"
    override fun getDisplayName(): String = "Var Auto-Type"

    override fun createComponent(): JComponent = panel {
        group("Behavior") {
            row {
                checkBox("Optimize imports after conversion")
                    .applyToComponent { toolTipText = "Run optimize imports after replacing 'var'." }
                    .bindSelected(::optimizeImports)
                    .onChanged { modified = true }
            }
            row {
                checkBox("Also trigger on Enter")
                    .applyToComponent { toolTipText = "Convert when you press Enter as well as ';'." }
                    .bindSelected(::triggerOnEnter)
                    .onChanged { modified = true }
            }
        }
    }

    override fun isModified(): Boolean = modified ||
            optimizeImports != settings.optimizeImportsAfterConvert ||
            triggerOnEnter != settings.triggerOnEnter

    override fun apply() {
        settings.optimizeImportsAfterConvert = optimizeImports
        settings.triggerOnEnter = triggerOnEnter
        modified = false
    }

    override fun reset() {
        optimizeImports = settings.optimizeImportsAfterConvert
        triggerOnEnter  = settings.triggerOnEnter
        modified = false
    }
}
