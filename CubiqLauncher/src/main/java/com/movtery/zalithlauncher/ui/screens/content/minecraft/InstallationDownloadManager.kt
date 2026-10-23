package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.content.Context
import android.widget.Toast
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskStage
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.multirt.JreDownloadManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.keepalive.TaskKeepAlive
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

object InstallationDownloadManager {
    private const val TAG = "InstallationDownloadManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentJob: Job? = null

    private val _activeInstaller = MutableStateFlow<GameInstaller?>(null)
    val activeInstaller = _activeInstaller.asStateFlow()

    private val _isPreparing = MutableStateFlow(false)
    val isPreparing = _isPreparing.asStateFlow()

    private val _showForegroundDialog = MutableStateFlow(false)
    val showForegroundDialog = _showForegroundDialog.asStateFlow()

    private val _activeInstallationName = MutableStateFlow<String?>(null)
    val activeInstallationName = _activeInstallationName.asStateFlow()

    private val _preparingTasksFlow = MutableStateFlow<List<TitledTask>>(emptyList())
    val preparingTasksFlow = _preparingTasksFlow.asStateFlow()

    fun dismissToBackground() {
        _showForegroundDialog.value = false
    }

    fun showDialog() {
        if (_activeInstaller.value != null || _isPreparing.value) {
            _showForegroundDialog.value = true
        }
    }

    fun cancelActiveInstallation() {
        currentJob?.cancel()
        currentJob = null
        _activeInstaller.value?.cancelInstall()
        _preparingTasksFlow.value = emptyList()
        _isPreparing.value = false
        _activeInstaller.value = null
        _showForegroundDialog.value = false
        _activeInstallationName.value = null
        TaskKeepAlive.release()
    }

    fun startInstallation(
        context: Context,
        name: String,
        version: String,
        loader: String,
        iconRes: Int,
        onSuccess: (String) -> Unit = {}
    ) {
        val finalVersionName = if (name.isBlank()) "$version $loader" else name
        _activeInstallationName.value = finalVersionName

        val firstNum = version.split('.').firstOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        val targetMajor = when {
            firstNum >= 26 || version.contains("26.") || version.contains("27.") -> 25
            version.contains("1.20.5") || version.contains("1.20.6") || version.contains("1.21") -> 21
            version.contains("1.17") || version.contains("1.18") || version.contains("1.19") || version.contains("1.20") -> 17
            else -> 8
        }
        val targetRuntimeName = "Internal-$targetMajor"
        val isVanilla = loader.equals("Vanilla", ignoreCase = true)

        val jreTask = Task.runTask(id = "prep_jre", task = { })
        val loaderTask = Task.runTask(id = "prep_loader", task = { })

        val initialTasks = listOf(
            TitledTask(
                title = androidText("Java Runtime Environment (Java $targetMajor)"),
                runningIcon = R.drawable.ic_java,
                task = jreTask
            ),
            TitledTask(
                title = androidText(if (isVanilla) "Resolving Version Metadata" else "Resolving $loader Loader"),
                runningIcon = R.drawable.ic_extension_outlined,
                task = loaderTask
            )
        )

        _preparingTasksFlow.value = initialTasks
        _isPreparing.value = true
        _showForegroundDialog.value = true

        TaskKeepAlive.acquire()

        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.IO) {
            try {
                val jreDir = File(PathManager.DIR_MULTIRT, targetRuntimeName)
                val hasLib = listOf(
                    File(jreDir, "lib/jli/libjli.so"),
                    File(jreDir, "lib/libjli.so"),
                    File(jreDir, "jre/lib/jli/libjli.so"),
                    File(jreDir, "lib/arm/jli/libjli.so"),
                    File(jreDir, "lib/aarch64/jli/libjli.so")
                ).any { it.exists() }

                if (!jreDir.exists() || !hasLib) {
                    jreTask.updateStage(TaskStage.RUNNING)
                    var installed = false
                    val jre = com.movtery.zalithlauncher.components.jre.Jre.entries.firstOrNull { it.jreName == targetRuntimeName }
                    if (jre != null) {
                        runCatching {
                            jreTask.updateProgress(-1f)
                            jreTask.updateMessage(androidText("Unpacking Java $targetMajor from local assets..."))
                            val task = com.movtery.zalithlauncher.components.jre.UnpackJreTask(context, jre)
                            if (!task.isCheckFailed()) {
                                task.run()
                                installed = true
                            }
                        }
                    }
                    if (!installed) {
                        jreTask.updateProgress(0f)
                        jreTask.updateMessage(androidText("Connecting to Java $targetMajor download mirrors..."))
                        val success = JreDownloadManager.downloadAndInstallJre(
                            context = context,
                            runtimeName = targetRuntimeName
                        ) { progress, message, speed ->
                            jreTask.updateProgress(if (progress >= 0) progress / 100f else -1f)
                            jreTask.updateMessage(androidText(message))
                            if (speed > 0L) {
                                jreTask.updateSpeed(speed)
                            } else {
                                jreTask.clearSpeed()
                            }
                        }
                        if (!success) {
                            throw IllegalStateException("Failed to download Java $targetMajor. Please check your internet connection.")
                        }
                    }
                    jreTask.clearSpeed()
                    jreTask.updateProgress(1f)
                    jreTask.updateStage(TaskStage.COMPLETED)
                    jreTask.updateMessage(androidText("Java $targetMajor ready"))
                } else {
                    jreTask.updateStage(TaskStage.COMPLETED)
                    jreTask.updateProgress(1f)
                    jreTask.updateMessage(androidText("Java $targetMajor verified"))
                }

                loaderTask.updateStage(TaskStage.RUNNING)
                loaderTask.updateProgress(-1f)
                loaderTask.updateMessage(androidText(if (isVanilla) "Resolving $version metadata..." else "Resolving $loader for $version..."))

                var optifineVer = if (loader.equals("OptiFine", ignoreCase = true)) {
                    OptiFineVersions.fetchOptiFineList(gameVersion = version)?.firstOrNull()
                } else null

                var fabricVer = if (loader.equals("Fabric", ignoreCase = true)) {
                    FabricVersions.fetchFabricLoaderList(version)?.firstOrNull()
                } else null

                var forgeVer = if (loader.equals("Forge", ignoreCase = true)) {
                    ForgeVersions.fetchForgeList(version)?.firstOrNull()
                } else null

                var neoforgeVer = if (loader.equals("NeoForge", ignoreCase = true)) {
                    NeoForgeVersions.fetchNeoForgeList(gameVersion = version)?.firstOrNull()
                } else null

                var quiltVer = if (loader.equals("Quilt", ignoreCase = true)) {
                    QuiltVersions.fetchQuiltLoaderList(version)?.firstOrNull()
                } else null

                loaderTask.updateProgress(1f)
                loaderTask.updateStage(TaskStage.COMPLETED)
                loaderTask.updateMessage(androidText(if (isVanilla) "Metadata ready" else "$loader resolved"))

                val downloadInfo = GameDownloadInfo(
                    gameVersion = version,
                    customVersionName = finalVersionName,
                    overwrite = true,
                    optifine = optifineVer,
                    fabric = fabricVer,
                    forge = forgeVer,
                    neoforge = neoforgeVer,
                    quilt = quiltVer
                )

                val installer = GameInstaller(context, downloadInfo, scope)
                _isPreparing.value = false
                _activeInstaller.value = installer

                installer.installGame(
                    onInstalled = { installedVersion ->
                        saveIconToVersion(context, installedVersion, iconRes)
                        VersionsManager.refresh("NewInstallationCreated", trySetVersion = installedVersion)
                        _activeInstaller.value = null
                        _showForegroundDialog.value = false
                        _activeInstallationName.value = null
                        _preparingTasksFlow.value = emptyList()
                        TaskKeepAlive.release()
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Installation $installedVersion installed successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess(installedVersion)
                        }
                    },
                    onError = { th ->
                        _isPreparing.value = false
                        _activeInstaller.value = null
                        _showForegroundDialog.value = false
                        _activeInstallationName.value = null
                        _preparingTasksFlow.value = emptyList()
                        TaskKeepAlive.release()
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to install $finalVersionName: ${th.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onGameAlreadyInstalled = {
                        _isPreparing.value = false
                        _activeInstaller.value = null
                        _showForegroundDialog.value = false
                        _activeInstallationName.value = null
                        _preparingTasksFlow.value = emptyList()
                        TaskKeepAlive.release()
                    }
                )
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    Logger.info(TAG, "Installation of $finalVersionName was cancelled")
                    return@launch
                }
                _isPreparing.value = false
                _activeInstaller.value = null
                _showForegroundDialog.value = false
                _activeInstallationName.value = null
                _preparingTasksFlow.value = emptyList()
                TaskKeepAlive.release()
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Error setting up $loader: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
