package com.lurenjia534.httprequest

import RequestViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lurenjia534.httprequest.NavItem.navItems
import com.lurenjia534.httprequest.ui.theme.HttpRequestTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 如果需要全屏体验
        setContent {
            HttpRequestTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Http Request") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        Row(modifier = Modifier.padding(innerPadding)) {
                            NavigationRailComponent(navController = navController)
                            NavHost(
                                navController = navController,
                                startDestination = "send",
                                modifier = Modifier.weight(1f)
                            ) {
                                composable("send") { Request(navController = navController) }
                                composable("records") { result(navController = navController) }
                            }
                        }
                    }
                )
            }
        }
    }
}


// NavigationRailComponent.kt
@Composable
fun NavigationRailComponent(navController: NavHostController) {
    NavigationRail {
        navItems.forEach { item ->
            NavigationRailItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = navController.currentBackStackEntry?.destination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 防止多次导航到同一目的地
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Request(navController: NavHostController) {
    val viewModel: RequestViewModel = viewModel()
    val url by viewModel.url.collectAsState()
    val selectedMethod by viewModel.selectedMethod.collectAsState()
    val headers by viewModel.headers.collectAsState()
    val body by viewModel.body.collectAsState()
    val response by viewModel.response.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HTTP Request") },
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.surface
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // 输入URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { viewModel.updateUrl(it) },
                    label = { Text("URL") },
                    placeholder = { Text("Enter request URL here") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = url.isBlank(),
                    trailingIcon = {
                        if (url.isBlank()) {
                            Icon(Icons.Filled.Close, contentDescription = "Error")
                        }
                    }
                )

                if (url.isBlank()) {
                    Text("URL cannot be empty", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 选择HTTP方法
                val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedMethod)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) }, // 修改这里，传递text参数
                                onClick = {
                                    viewModel.updateSelectedMethod(method)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 输入请求头
                OutlinedTextField(
                    value = headers,
                    onValueChange = { viewModel.updateHeaders(it) },
                    label = { Text("Headers (JSON format)") },
                    placeholder = { Text("Enter headers in JSON format") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 输入请求体
                OutlinedTextField(
                    value = body,
                    onValueChange = { viewModel.updateBody(it) },
                    label = { Text("Body") },
                    placeholder = { Text("Enter request body here") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 发送请求按钮
                Button(
                    onClick = {
                        if (url.isBlank()) {
                            Toast.makeText(context, "URL cannot be empty", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.sendRequest()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Request")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 显示响应
                if (response.isNotBlank()) {
                    LazyColumn {
                       item {
                           Text("Response: $response")
                       }
                    }
                }
            }
        }
    )
}


// DetailScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun result(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.surface

                )
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Text("Welcome to the Detail Screen!")
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back to Home")
                }
            }
        }
    )
}