package com.ftpdroid.app.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object ServerDashboard : Routes("server_dashboard")
    data object ServerSettings : Routes("server_settings")
    data object UserManager : Routes("user_manager")
    data object ConnectionLog : Routes("connection_log")
    data object ProfileList : Routes("profile_list")
    data object AddEditProfile : Routes("add_edit_profile?profileId={profileId}")
    data object FileBrowser : Routes("file_browser/{profileId}/{path}")
    data object TransferQueue : Routes("transfer_queue")
    data object TransferHistory : Routes("transfer_history")
    data object AppSettings : Routes("app_settings")

    companion object {
        object NavArgs {
            const val PROFILE_ID = "profileId"
            const val PATH = "path"
        }

        fun createAddEditProfileRoute(profileId: Long = -1L): String {
            return if (profileId == -1L) {
                "add_edit_profile?profileId=-1"
            } else {
                "add_edit_profile?profileId=$profileId"
            }
        }

        fun createFileBrowserRoute(profileId: Long, path: String): String {
            val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            return "file_browser/$profileId/$encodedPath"
        }
    }
}