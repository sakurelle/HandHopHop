package ru.handhophop

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.handhophop.core.design.Route

@Serializable
sealed interface AppRoute : NavKey {
    val tab: Route

    @Serializable
    data class Mash(
        val workId: Long? = null,
        val imageUrl: String? = null,
    ) : AppRoute {
        override val tab: Route = Route.MASH
    }

    @Serializable
    data object Feed : AppRoute {
        override val tab: Route = Route.FEED
    }

    @Serializable
    data object Bookmark : AppRoute {
        override val tab: Route = Route.BOOKMARK
    }

    @Serializable
    data object Settings : AppRoute {
        override val tab: Route = Route.SETTINGS
    }

    companion object {
        fun from(route: Route): AppRoute = when (route) {
            Route.MASH -> Mash()
            Route.FEED -> Feed
            Route.BOOKMARK -> Bookmark
            Route.SETTINGS -> Settings
        }
    }
}
