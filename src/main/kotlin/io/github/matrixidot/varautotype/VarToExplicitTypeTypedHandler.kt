package io.github.matrixidot.varautotype

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil

class VarToExplicitTypeTypedHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is PsiJavaFile) return Result.CONTINUE
        if (c == ';') processAtCarets(project, editor, file)
        return Result.CONTINUE
    }

    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        if (file !is PsiJavaFile) return Result.CONTINUE
        if (c == '\n' && VarAutoTypeSettings.instance.state.triggerOnEnter) {
            processAtCarets(project, editor, file)
        }
        return Result.CONTINUE
    }

    private fun processAtCarets(project: Project, editor: Editor, file: PsiJavaFile) {
        // Avoid heavy resolve during indexing
        if (DumbService.isDumb(project)) return

        val level: LanguageLevel = PsiUtil.getLanguageLevel(file)
        if (!level.isAtLeast(LanguageLevel.JDK_10)) return

        val doc = editor.document
        PsiDocumentManager.getInstance(project).commitDocument(doc)

        // Collect unique declaration statements around all carets
        val decls: Set<PsiDeclarationStatement> =
            editor.caretModel.allCarets.asSequence()
                .map { (it.offset - 1).coerceAtLeast(0) }
                .mapNotNull { file.findElementAt(it) }
                .mapNotNull { PsiTreeUtil.getParentOfType(it, PsiDeclarationStatement::class.java) }
                .toSet()

        if (decls.isEmpty()) return

        val codeStyle = JavaCodeStyleManager.getInstance(project)
        val factory = JavaPsiFacade.getElementFactory(project)

        WriteCommandAction
            .writeCommandAction(project)
            .withName("Replace Var With Explicit Type")
            .run<RuntimeException> {
                for (decl in decls) {
                    val locals = decl.declaredElements.filterIsInstance<PsiLocalVariable>()
                    for (localVar in locals) {
                        val typeElement = localVar.typeElement ?: continue
                        // Keep robust but safe check: only act on 'var' with initializer
                        if (typeElement.text != "var") continue
                        if (localVar.initializer == null) continue

                        val inferredType = runCatching { localVar.type }.getOrNull() ?: continue
                        val explicitTypeElement = factory.createTypeElement(inferredType)

                        typeElement.replace(explicitTypeElement)
                    }
                    // Add imports & shorten within the modified statement subtree
                    codeStyle.shortenClassReferences(decl)
                }

                if (VarAutoTypeSettings.instance.state.optimizeImportsAfterConvert) {
                    codeStyle.optimizeImports(file)
                }
            }

        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(doc)
    }
}
