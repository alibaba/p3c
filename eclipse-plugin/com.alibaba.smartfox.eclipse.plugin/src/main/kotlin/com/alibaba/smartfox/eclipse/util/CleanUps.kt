// =====================================================================
//
// Copyright (C) 2012 - 2016, Philip Graf
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// =====================================================================
package com.alibaba.smartfox.eclipse.util

import com.alibaba.smartfox.eclipse.SmartfoxActivator
import org.eclipse.core.filebuffers.FileBuffers
import org.eclipse.core.filebuffers.ITextFileBuffer
import org.eclipse.core.filebuffers.LocationKind
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.Assert
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring
import org.eclipse.jdt.internal.corext.fix.FixMessages
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jdt.internal.ui.actions.ActionUtil
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions
import org.eclipse.jdt.ui.SharedASTProvider
import org.eclipse.jdt.ui.cleanup.CleanUpContext
import org.eclipse.jdt.ui.cleanup.CleanUpOptions
import org.eclipse.jdt.ui.cleanup.ICleanUp
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentExtension4
import org.eclipse.jface.window.Window
import org.eclipse.ltk.core.refactoring.Change
import org.eclipse.ltk.core.refactoring.CompositeChange
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes
import org.eclipse.ltk.core.refactoring.NullChange
import org.eclipse.ltk.core.refactoring.PerformChangeOperation
import org.eclipse.ltk.core.refactoring.RefactoringCore
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.TextFileChange
import org.eclipse.ltk.ui.refactoring.RefactoringUI
import org.eclipse.text.edits.MalformedTreeException
import org.eclipse.text.edits.TextEdit
import org.eclipse.text.edits.UndoEdit
import org.eclipse.ui.PlatformUI
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList

/**
 *
 *
 * @author caikang
 * @date 2017/06/15
 */
object CleanUps {

    private val WARNING_VALUE = "warning"
    private val ERROR_VALUE = "error"

    val cleanUpSettings = mapOf(CleanUpConstants.ADD_MISSING_ANNOTATIONS to CleanUpOptions.TRUE,
            CleanUpConstants.CONTROL_STATMENTS_USE_BLOCKS_ALWAYS to CleanUpOptions.TRUE,
            CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE to CleanUpOptions.TRUE,
            CleanUpConstants.CONTROL_STATEMENTS_USE_BLOCKS to CleanUpOptions.TRUE,
            CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION to
                    CleanUpOptions.TRUE)

    fun fix(file: IFile, monitor: IProgressMonitor) {
        val compilationUnit = JavaCore.createCompilationUnitFrom(file) ?: return
        doCleanUp(compilationUnit, monitor)
    }

    @Throws(CoreException::class) fun doCleanUp(unit: ICompilationUnit, monitor: IProgressMonitor) {

        monitor.beginTask("Fix", IProgressMonitor.UNKNOWN)

        if (!ActionUtil.isOnBuildPath(unit)) return
        val result = CompositeChange(FixMessages.CleanUpPostSaveListener_SaveAction_ChangeName)
        val undoEdits = LinkedList<UndoEdit>()
        val oldFileValue = unit.resource.modificationStamp
        val oldDocValue = getDocumentStamp(unit.resource as IFile, monitor)
        val manager = RefactoringCore.getUndoManager()
        var success = false

        try {
            manager.aboutToPerformChange(result)
            success = doCleanUp(unit, monitor, result, undoEdits)
        } finally {
            manager.changePerformed(result, success)
        }

        if (undoEdits.size > 0) {
            val undoEditArray = undoEdits.toTypedArray()
            val undo = CleanUpSaveUndo(result.name, unit.resource as IFile, undoEditArray, oldDocValue, oldFileValue)
            undo.initializeValidationData(NullProgressMonitor())
            manager.addUndo(result.name, undo)
        }
    }

    @Throws(CoreException::class) private fun getDocumentStamp(file: IFile, monitor: IProgressMonitor): Long {
        val manager = FileBuffers.getTextFileBufferManager()
        val path = file.fullPath

        monitor.beginTask("", 2)

        var buffer: ITextFileBuffer? = null
        try {
            manager.connect(path, LocationKind.IFILE, monitor)
            buffer = manager.getTextFileBuffer(path, LocationKind.IFILE)
            val document = buffer!!.document

            if (document is IDocumentExtension4) {
                return document.modificationStamp
            } else {
                return file.modificationStamp
            }
        } finally {
            if (buffer != null) manager.disconnect(path, LocationKind.IFILE, monitor)
            monitor.done()
        }
    }

    private fun doCleanUp(unit: ICompilationUnit, monitor: IProgressMonitor, result: CompositeChange,
            undoEdits: LinkedList<UndoEdit>): Boolean {
        val cleanUps = JavaPlugin.getDefault().cleanUpRegistry.createCleanUps(
                setOf("org.eclipse.jdt.ui.cleanup.java50", "org.eclipse.jdt.ui.cleanup.control_statements"))
        val preCondition = RefactoringStatus()
        val postCondition = RefactoringStatus()
        cleanUps.forEach { cleanUp ->
            cleanUp.setOptions(MapCleanUpOptions(cleanUpSettings))

            preCondition.merge(cleanUp.checkPreConditions(unit.javaProject, arrayOf(unit), monitor))

            val options = HashMap<String, String>(cleanUp.requirements.compilerOptions ?: emptyMap())

            var ast = CleanUps.createAst(unit, options, monitor)
            if (cleanUp.requirements.requiresAST()) {
                ast = createAst(unit, options, monitor)
            }

            val context = CleanUpContext(unit, ast)

            val undoneCleanUps = ArrayList<ICleanUp>()
            val change = CleanUpRefactoring.calculateChange(context, arrayOf(cleanUp), undoneCleanUps, null)

            postCondition.merge(cleanUp.checkPostConditions(monitor))
            if (showStatus(postCondition) != Window.OK) {
                return@doCleanUp false
            }

            if (change == null) {
                return@forEach
            }
            result.add(change)

            change.initializeValidationData(NullProgressMonitor())

            val performChangeOperation = PerformChangeOperation(change)
            performChangeOperation.setSchedulingRule(unit.schedulingRule)
            performChangeOperation.run(monitor)
            performChangeOperation.undoChange
            undoEdits.addFirst(change.undoEdit)
        }

        return true
    }

    private fun showStatus(status: RefactoringStatus): Int {
        if (!status.hasError()) return Window.OK

        val shell = PlatformUI.getWorkbench().activeWorkbenchWindow.shell

        val dialog = RefactoringUI.createRefactoringStatusDialog(status, shell, "", false)
        return dialog.open()
    }

    private fun createAst(unit: ICompilationUnit, cleanUpOptions: Map<String, String>,
            monitor: IProgressMonitor): CompilationUnit {
        val project = unit.javaProject
        if (compatibleOptions(project, cleanUpOptions)) {
            val ast = SharedASTProvider.getAST(unit, SharedASTProvider.WAIT_NO, monitor)
            if (ast != null) return ast
        }

        val parser = CleanUpRefactoring.createCleanUpASTParser()
        parser.setSource(unit)

        val compilerOptions = RefactoringASTParser.getCompilerOptions(unit.javaProject)
        compilerOptions.putAll(cleanUpOptions)
        parser.setCompilerOptions(compilerOptions)

        return parser.createAST(monitor) as CompilationUnit
    }

    private fun compatibleOptions(project: IJavaProject, cleanUpOptions: Map<String, String>): Boolean {
        if (cleanUpOptions.isEmpty()) {
            return true
        }

        val projectOptions = project.getOptions(true)

        return !cleanUpOptions.keys.any {
            val projectOption = projectOptions[it]?.toString()
            val cleanUpOption = cleanUpOptions[it]?.toString()
            !strongerEquals(projectOption, cleanUpOption)
        }
    }

    private fun strongerEquals(projectOption: String?, cleanUpOption: String?): Boolean {
        if (projectOption == null) return false

        if (ERROR_VALUE == cleanUpOption) {
            return ERROR_VALUE == projectOption
        } else if (WARNING_VALUE == cleanUpOption) {
            return ERROR_VALUE == projectOption || WARNING_VALUE == projectOption
        }

        return false
    }

    private class CleanUpSaveUndo(name: String, private val fFile: IFile, private val fUndos: Array<UndoEdit>,
            private val fDocumentStamp: Long, private val fFileStamp: Long) : TextFileChange(name,
            fFile) {

        init {
            Assert.isNotNull(fUndos)
        }

        public override fun needsSaving(): Boolean {
            return true
        }

        @Throws(CoreException::class) override fun perform(monitor: IProgressMonitor?): Change {
            val pm = monitor ?: NullProgressMonitor()
            if (isValid(pm).hasFatalError()) return NullChange()

            val manager = FileBuffers.getTextFileBufferManager()
            pm.beginTask("", 2)
            var buffer: ITextFileBuffer? = null

            try {
                manager.connect(fFile.fullPath, LocationKind.IFILE, pm)
                buffer = manager.getTextFileBuffer(fFile.fullPath, LocationKind.IFILE)

                val document = buffer!!.document
                val oldFileValue = fFile.modificationStamp
                val undoEditCollector = LinkedList<UndoEdit>()
                val oldDocValue = LongArray(1)
                val setContentStampSuccess = booleanArrayOf(false)

                if (!buffer.isSynchronizationContextRequested) {
                    performEdit(document, oldFileValue, undoEditCollector, oldDocValue, setContentStampSuccess)

                } else {
                    val fileBufferManager = FileBuffers.getTextFileBufferManager()

                    class UIRunnable : Runnable {
                        var fDone: Boolean = false
                        var fException: Exception? = null

                        override fun run() {
                            synchronized(this) {
                                try {
                                    performEdit(document, oldFileValue, undoEditCollector, oldDocValue,
                                            setContentStampSuccess)
                                } catch (e: BadLocationException) {
                                    fException = e
                                } catch (e: MalformedTreeException) {
                                    fException = e
                                } catch (e: CoreException) {
                                    fException = e
                                } finally {
                                    fDone = true
                                    (this as Object).notifyAll()
                                }
                            }
                        }
                    }

                    val runnable = UIRunnable()

                    synchronized(runnable) {
                        fileBufferManager.execute(runnable)
                        while (!runnable.fDone) {
                            try {
                                (runnable as Object).wait(500)
                            } catch (x: InterruptedException) {
                            }

                        }
                    }

                    if (runnable.fException != null) {
                        if (runnable.fException is BadLocationException) {
                            throw runnable.fException as BadLocationException
                        } else if (runnable.fException is MalformedTreeException) {
                            throw runnable.fException as MalformedTreeException
                        } else if (runnable.fException is CoreException) {
                            throw runnable.fException as CoreException
                        }
                    }
                }

                buffer.commit(pm, false)
                if (!setContentStampSuccess[0]) {
                    fFile.revertModificationStamp(fFileStamp)
                }

                return CleanUpSaveUndo(name, fFile, undoEditCollector.toTypedArray(), oldDocValue[0], oldFileValue)
            } catch (e: BadLocationException) {
                throw wrapBadLocationException(e)
            } finally {
                if (buffer != null) manager.disconnect(fFile.fullPath, LocationKind.IFILE, pm)
            }
        }

        @Throws(MalformedTreeException::class, BadLocationException::class,
                CoreException::class) private fun performEdit(document: IDocument, oldFileValue: Long,
                editCollector: LinkedList<UndoEdit>,
                oldDocValue: LongArray,
                setContentStampSuccess: BooleanArray) {
            if (document is IDocumentExtension4) {
                oldDocValue[0] = document.modificationStamp
            } else {
                oldDocValue[0] = oldFileValue
            }

            // perform the changes
            fUndos.map { it.apply(document, TextEdit.CREATE_UNDO) }.forEach { editCollector.addFirst(it) }

            if (document is IDocumentExtension4 && fDocumentStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                try {
                    document.replace(0, 0, "", fDocumentStamp)
                    setContentStampSuccess[0] = true
                } catch (e: BadLocationException) {
                    throw wrapBadLocationException(e)
                }

            }
        }
    }

    private fun wrapBadLocationException(e: BadLocationException): CoreException {
        var message: String? = e.message
        if (message == null) message = "BadLocationException"
        return CoreException(
                Status(IStatus.ERROR, SmartfoxActivator.PLUGIN_ID, IRefactoringCoreStatusCodes.BAD_LOCATION, message,
                        e))
    }

}