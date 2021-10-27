/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.smartfox.idea.common.util

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import org.apache.http.MethodNotSupportedException
import java.awt.Component
import java.net.UnknownHostException

/**
 *
 *
 * @author caikang
 * @date 2017/05/08
 */
object BalloonNotifications {
    const val displayId = "SmartFox Intellij IDEA Balloon Notification"
    val balloonGroup = buildNotificationGroup(displayId, NotificationDisplayType.BALLOON, true)

    const val stickyBalloonDisplayId = "SmartFox Intellij IDEA Notification"
    val stickyBalloonGroup = buildNotificationGroup(stickyBalloonDisplayId, NotificationDisplayType.STICKY_BALLOON, true)
    const val TITLE = "SmartFox Intellij IDEA Plugin"

    fun showInfoDialog(component: Component, title: String, message: String) {
        Messages.showInfoMessage(component, message, title)
    }

    fun showErrorDialog(component: Component, title: String, errorMessage: String) {
        Messages.showErrorDialog(component, errorMessage, title)
    }

    fun showErrorDialog(component: Component, title: String, e: Exception) {
        if (isOperationCanceled(e)) {
            return
        }
        Messages.showErrorDialog(component, getErrorTextFromException(e), title)
    }

    fun showSuccessNotification(message: String, project: Project? = ProjectManager.getInstance().defaultProject,
                                title: String = TITLE, sticky: Boolean = false) {
        showNotification(message, project, title, NotificationType.INFORMATION, null, sticky)
    }

    fun showWarnNotification(message: String, project: Project? = ProjectManager.getInstance().defaultProject,
                             title: String = TITLE, sticky: Boolean = false) {
        showNotification(message, project, title, NotificationType.WARNING, null, sticky)
    }

    fun showErrorNotification(message: String, project: Project? = ProjectManager.getInstance().defaultProject,
                              title: String = TITLE, sticky: Boolean = false) {
        showNotification(message, project, title, NotificationType.ERROR, null, sticky)
    }

    fun showSuccessNotification(message: String, project: Project?,
                                notificationListener: NotificationListener, title: String = TITLE, sticky: Boolean = false) {
        showNotification(message, project, title, NotificationType.INFORMATION, notificationListener, sticky)
    }

    fun showNotification(message: String, project: Project? = ProjectManager.getInstance().defaultProject,
                         title: String = TITLE,
                         notificationType: NotificationType = NotificationType.INFORMATION,
                         notificationListener: NotificationListener? = null, sticky: Boolean = false) {
        val group = if (sticky) {
            stickyBalloonGroup
        } else {
            balloonGroup
        }
        group.createNotification(title, message, notificationType, notificationListener).notify(project)
    }

    private fun isOperationCanceled(e: Exception): Boolean {
        return e is ProcessCanceledException
    }

    fun getErrorTextFromException(e: Exception): String {
        if (e is UnknownHostException) {
            return "Unknown host: " + e.message
        }
        return e.message ?: ""
    }
}

object LogNotifications {

    val group = buildNotificationGroup(displayId = BalloonNotifications.displayId, displayType = NotificationDisplayType.NONE, isLogByDefault = true)

    fun log(message: String, project: Project? = ProjectManager.getInstance().defaultProject,
            title: String = BalloonNotifications.TITLE,
            notificationType: NotificationType = NotificationType.INFORMATION,
            notificationListener: NotificationListener? = null) {
        group.createNotification(title, message, notificationType, notificationListener).notify(project)
    }
}


fun buildNotificationGroup(displayId: String, displayType: NotificationDisplayType, isLogByDefault: Boolean): NotificationGroup {
    val notificationGroupClass = Class.forName("com.intellij.notification.NotificationGroup")
    notificationGroupClass.constructors.forEach {
        if (it.parameters.size == 3) {
            try {
                return it.newInstance(displayId, displayType, isLogByDefault)
                        as NotificationGroup
            } catch (e: Exception) {
                System.err.println(e)
            }
        }
    }
    throw MethodNotSupportedException("cannot find a suitable constructor for NotificationGroup who accepts [String,NotificationDisplayType,Boolean]")
}
