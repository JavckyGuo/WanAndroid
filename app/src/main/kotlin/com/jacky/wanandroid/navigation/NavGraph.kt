package com.jacky.wanandroid.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.jacky.wanandroid.domain.model.Article
import com.jacky.wanandroid.feature.auth.AuthScreen
import com.jacky.wanandroid.feature.collection.CollectionScreen
import com.jacky.wanandroid.feature.home.HomeScreen
import com.jacky.wanandroid.feature.knowledge.KnowledgeScreen
import com.jacky.wanandroid.feature.navigation.NavigationScreen
import com.jacky.wanandroid.feature.profile.ProfileScreen
import com.jacky.wanandroid.feature.project.ProjectScreen
import com.jacky.wanandroid.feature.search.SearchScreen
import com.jacky.wanandroid.feature.web.WebScreen

private data class TopLevel(val label: String, val marker: String, val destination: Any, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val topLevels = listOf(
    TopLevel("首页", "Home", Home, Icons.Rounded.Home), TopLevel("体系", "Knowledge", Knowledge, Icons.Rounded.AccountTree),
    TopLevel("导航", "Navigation", Navigation, Icons.Rounded.Explore), TopLevel("项目", "Projects", Projects, Icons.Rounded.Widgets),
    TopLevel("我的", "Profile", Profile, Icons.Rounded.Person)
)

@Composable fun WanAndroidApp(navController: NavHostController = rememberNavController()) {
    val entry by navController.currentBackStackEntryAsState(); val route = entry?.destination?.route.orEmpty()
    val showBottom = topLevels.any { route.endsWith(".${it.marker}") || route == it.marker }
    val openArticle: (Article) -> Unit = { navController.navigate(WebArticle(it.title, it.link, it.id)) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { if (showBottom) NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) { topLevels.forEach { item ->
        val selected = route.endsWith(".${item.marker}") || route == item.marker
        NavigationBarItem(selected = selected, onClick = { navController.navigate(item.destination) { popUpTo(Home) { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Icon(item.icon, contentDescription = null) }, label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer, selectedTextColor = MaterialTheme.colorScheme.onBackground, indicatorColor = MaterialTheme.colorScheme.primaryContainer, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant))
    } } }) { padding -> NavHost(navController, startDestination = Home, modifier = Modifier.padding(padding)) {
        composable<Home> { HomeScreen(onSearch = { navController.navigate(Search) }, onLogin = { navController.navigate(Auth) }, onOpenArticle = openArticle) }
        composable<Knowledge> { KnowledgeScreen(openArticle) }
        composable<Navigation> { NavigationScreen(openArticle) }
        composable<Projects> { ProjectScreen(openArticle) }
        composable<Profile> { ProfileScreen(onLogin = { navController.navigate(Auth) }, onCollections = { navController.navigate(Collections) }) }
        composable<Search> { SearchScreen(navController::popBackStack, openArticle) }
        composable<Auth> { AuthScreen(onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() }) }
        composable<Collections> { CollectionScreen(navController::popBackStack, { navController.navigate(Auth) }, openArticle) }
        composable<WebArticle> { backStack -> val args = backStack.toRoute<WebArticle>(); WebScreen(args.title, args.url, navController::popBackStack) }
    } }
}
