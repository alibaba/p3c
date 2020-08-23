package com.alibaba.p3c.idea.action

import com.alibaba.p3c.idea.component.AliProjectComponent
import com.alibaba.p3c.idea.ep.InspectionActionExtensionPoint
import com.alibaba.p3c.idea.inspection.AliLocalInspectionToolProvider
import com.alibaba.p3c.idea.inspection.PmdRuleInspectionIdentify
import com.alibaba.p3c.idea.pmd.AliPmdProcessor
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInsight.daemon.ProblemHighlightFilter
import com.intellij.codeInsight.daemon.impl.DaemonProgressIndicator
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ui.InspectionResultsView
import com.intellij.concurrency.JobLauncher
import com.intellij.concurrency.JobLauncherImpl
import com.intellij.concurrency.SensitiveProgressWrapper
import com.intellij.diagnostic.ThreadDumper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.displayUrlRelativeToProject
import com.intellij.openapi.project.isProjectOrWorkspaceFile
import com.intellij.openapi.roots.FileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.content.ContentManager
import com.intellij.util.ExceptionUtil
import com.intellij.util.IncorrectOperationException
import com.intellij.util.Processor
import com.intellij.util.ReflectionUtil
import com.intellij.util.containers.ContainerUtil
import gnu.trove.THashSet
import net.sourceforge.pmd.RuleViolation
import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit.SECONDS

/**
 * @date 2020/06/19
 * @author caikang
 */
class PmdGlobalInspectionContextImpl(
    project: Project,
    contentManager: NotNullLazyValue<ContentManager>,
    private val projectScopeSelected: Boolean
) :
    GlobalInspectionContextImpl(project, contentManager) {

    private val logger = Logger.getInstance(PmdGlobalInspectionContextImpl::class.java)

    override fun runTools(scope: AnalysisScope, runGlobalToolsOnly: Boolean, isOfflineInspections: Boolean) {
        val usedTools = usedTools
        val hasPmdTools = usedTools.any {
            it.tool.tool is PmdRuleInspectionIdentify
        }
        if (hasPmdTools) {
            val progressIndicator =
                ProgressIndicatorProvider.getGlobalProgressIndicator()
                    ?: throw IncorrectOperationException("Must be run under progress")
            pmdNodeWarmUp(scope, progressIndicator, isOfflineInspections)
        }
        super.runTools(scope, runGlobalToolsOnly, isOfflineInspections)
        if (myProgressIndicator.isCanceled) {
            return
        }
        InspectionActionExtensionPoint.extension.extensions.forEach {
            try {
                it.doOnInspectionFinished(this, projectScopeSelected)
            } catch (e: Exception) {
                logger.warn(e)
            }
        }
    }

    private fun pmdNodeWarmUp(
        scope: AnalysisScope,
        progressIndicator: ProgressIndicator,
        isOfflineInspections: Boolean
    ) {
        val aliProjectComponent = project.getComponent(AliProjectComponent::class.java)
        // run pmd inspection
        val processor = Processor { file: PsiFile ->
            ProgressManager.checkCanceled()
            val readActionSuccess =
                DumbService.getInstance(project).tryRunReadActionInSmartMode(
                    {
                        if (!file.isValid) {
                            return@tryRunReadActionInSmartMode true
                        }
                        val virtualFile = file.virtualFile
                        if (!scope.contains(virtualFile)) {
                            logger.info(file.name + "; scope: " + scope + "; " + virtualFile)
                            return@tryRunReadActionInSmartMode true
                        }
                        val path = virtualFile.canonicalPath?.toLowerCase() ?: ""
                        if (!path.endsWith(".java") && !path.endsWith(".vm")) {
                            return@tryRunReadActionInSmartMode true
                        }
                        doPmdProcess(file, aliProjectComponent, virtualFile)
                        true
                    }, "Inspect code is not available until indices are ready"
                )
            if (readActionSuccess == null || !readActionSuccess) {
                throw ProcessCanceledException()
            }
            true
        }
        val headlessEnvironment = ApplicationManager.getApplication().isHeadlessEnvironment
        val searchScope =
            ApplicationManager.getApplication().runReadAction<SearchScope, RuntimeException> { scope.toSearchScope() }
        val localScopeFiles: MutableSet<VirtualFile>? = if (searchScope is LocalSearchScope) THashSet() else null
        val filesToInspect: BlockingQueue<PsiFile> = ArrayBlockingQueue(1000)
        val iteratingIndicator: ProgressIndicator = SensitiveProgressWrapper(progressIndicator)
        val future: Future<*> = startIterateScopeInBackground(
            scope,
            localScopeFiles,
            headlessEnvironment,
            filesToInspect,
            iteratingIndicator
        ) as Future<*>
        val dependentIndicators =
            ReflectionUtil.getField(javaClass, this, List::class.java, "dependentIndicators")?.map {
                it as ProgressIndicator
            }?.toMutableList()
        try {
            val filesFailedToInspect: Queue<PsiFile> = LinkedBlockingQueue()
            while (true) {
                val disposable = Disposer.newDisposable()
                val wrapper: ProgressIndicator = DaemonProgressIndicator()
                dependentIndicators?.let {
                    it.add(wrapper)
                }
                try {
                    // avoid "attach listener"/"write action" race
                    ApplicationManager.getApplication().runReadAction {
                        wrapper.start()
                        ProgressIndicatorUtils.forceWriteActionPriority(wrapper, disposable)
                        // there is a chance we are racing with write action, in which case just registered listener might not be called, retry.
                        if (ApplicationManagerEx.getApplicationEx().isWriteActionPending) {
                            throw ProcessCanceledException()
                        }
                    }
                    // use wrapper here to cancel early when write action start but do not affect the original indicator
                    (JobLauncher.getInstance() as JobLauncherImpl).processQueue(
                        filesToInspect,
                        filesFailedToInspect,
                        wrapper,
                        PsiUtilCore.NULL_PSI_FILE,
                        processor
                    )
                    break
                } catch (e: ProcessCanceledException) {
                    progressIndicator.checkCanceled()
                    assert(
                        isOfflineInspections || !ApplicationManager.getApplication().isReadAccessAllowed
                    ) {
                        """
                                    Must be outside read action. PCE=
                                    ${ExceptionUtil.getThrowableText(e)}
                                    """.trimIndent()
                    }
                    assert(
                        isOfflineInspections || !ApplicationManager.getApplication().isDispatchThread
                    ) {
                        """
                                    Must be outside EDT. PCE=
                                    ${ExceptionUtil.getThrowableText(e)}
                                    """.trimIndent()
                    }

                    // wait for write action to complete
                    ApplicationManager.getApplication().runReadAction(EmptyRunnable.getInstance())
                } finally {
                    dependentIndicators?.let {
                        it.remove(wrapper)
                    }
                    Disposer.dispose(disposable)
                }
            }
        } finally {
            iteratingIndicator.cancel() // tell file scanning thread to stop
            filesToInspect.clear() // let file scanning thread a chance to put TOMBSTONE and complete
            try {
                future[30, SECONDS]
            } catch (e: java.lang.Exception) {
                logger.error(
                    """
                                Thread dump: 
                                ${ThreadDumper.dumpThreadsToString()}
                                """.trimIndent(), e
                )
            }
        }
        ProgressManager.checkCanceled()
    }

    private fun startIterateScopeInBackground(
        scope: AnalysisScope,
        localScopeFiles: MutableCollection<VirtualFile>?,
        headlessEnvironment: Boolean,
        outFilesToInspect: BlockingQueue<in PsiFile>,
        progressIndicator: ProgressIndicator
    ): Future<*>? {
        val task: Backgroundable = object : Backgroundable(project, "Scanning Files to Inspect") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val fileIndex: FileIndex = ProjectRootManager.getInstance(project).fileIndex
                    scope.accept { file: VirtualFile? ->
                        ProgressManager.checkCanceled()
                        if (isProjectOrWorkspaceFile(file!!) || !fileIndex.isInContent(file)) return@accept true
                        val psiFile =
                            ReadAction.compute<PsiFile?, RuntimeException> {
                                if (project.isDisposed) throw ProcessCanceledException()
                                val psi = PsiManager.getInstance(project).findFile(file)
                                val document =
                                    psi?.let { shouldProcess(it, headlessEnvironment, localScopeFiles) }
                                if (document != null) {
                                    return@compute psi
                                }
                                null
                            }
                        // do not inspect binary files
                        if (psiFile != null) {
                            try {
                                check(!ApplicationManager.getApplication().isReadAccessAllowed) { "Must not have read action" }
                                outFilesToInspect.put(psiFile)
                            } catch (e: InterruptedException) {
                                logger.error(e)
                            }
                        }
                        ProgressManager.checkCanceled()
                        true
                    }
                } catch (e: ProcessCanceledException) {
                    // ignore, but put tombstone
                } finally {
                    try {
                        outFilesToInspect.put(PsiUtilCore.NULL_PSI_FILE)
                    } catch (e: InterruptedException) {
                        logger.error(e)
                    }
                }
            }
        }
        return (ProgressManager.getInstance() as CoreProgressManager).runProcessWithProgressAsynchronously(
            task,
            progressIndicator,
            null
        )
    }

    private fun shouldProcess(
        file: PsiFile,
        headlessEnvironment: Boolean,
        localScopeFiles: MutableCollection<VirtualFile>?
    ): Document? {
        val virtualFile = file.virtualFile ?: return null
        if (isBinary(file)) return null //do not inspect binary files
        if (isViewClosed && !headlessEnvironment) {
            throw ProcessCanceledException()
        }
        if (logger.isDebugEnabled) {
            logger.debug("Running local inspections on " + virtualFile.path)
        }
        if (SingleRootFileViewProvider.isTooLargeForIntelligence(virtualFile)) return null
        if (localScopeFiles != null && !localScopeFiles.add(virtualFile)) return null
        return if (!ProblemHighlightFilter.shouldProcessFileInBatch(file)) null else PsiDocumentManager.getInstance(
            project
        ).getDocument(file)
    }

    private fun isBinary(file: PsiFile): Boolean {
        return file is PsiBinaryFile || file.fileType.isBinary
    }

    private fun doPmdProcess(
        file: PsiFile,
        aliProjectComponent: AliProjectComponent,
        virtualFile: VirtualFile
    ) {
        val url: String = displayUrlRelativeToProject(
            virtualFile,
            virtualFile.presentableUrl,
            project,
            true,
            false
        )
        myProgressIndicator.text = "PMD Process in $url"
        val violations =
            AliPmdProcessor(AliLocalInspectionToolProvider.getRuleSets()).processFile(
                file,
                false
            )
        val fileContext = aliProjectComponent.getFileContext(virtualFile)
        fileContext?.let { fc ->
            val ruleViolations = ContainerUtil.createConcurrentSoftValueMap<String, List<RuleViolation>>()
            for (entry in violations.groupBy {
                it.rule.name
            }) {
                ruleViolations[entry.key] = entry.value
            }
            fc.ruleViolations = ruleViolations
        }
    }

    override fun close(noSuspiciousCodeFound: Boolean) {
        super.close(noSuspiciousCodeFound)
        InspectionActionExtensionPoint.extension.extensions.forEach {
            try {
                it.doOnClose(noSuspiciousCodeFound, project)
            } catch (e: Exception) {
                logger.warn(e)
            }
        }
    }

    override fun addView(view: InspectionResultsView) {
        super.addView(view)
        InspectionActionExtensionPoint.extension.extensions.forEach {
            try {
                it.doOnView(view)
            } catch (e: Exception) {
                logger.warn(e)
            }
        }
    }
}