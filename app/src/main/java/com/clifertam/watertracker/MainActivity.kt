package com.clifertam.watertracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clifertam.watertracker.model.MenuItem
import com.clifertam.watertracker.presentation.AppBarCustom
import com.clifertam.watertracker.ui.screen.MainScreen
import com.clifertam.watertracker.presentation.NavigationBody
import com.clifertam.watertracker.presentation.NavigationHeader
import com.clifertam.watertracker.ui.screen.HistoryScreen
import com.clifertam.watertracker.ui.screen.PersonaInformationScreen
import com.clifertam.watertracker.ui.screen.ReminderScreen
import com.clifertam.watertracker.ui.theme.WaterTrackerTheme
import com.clifertam.watertracker.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.solovyev.android.checkout.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mCheckout = Checkout.forActivity(this, WaterApp.get()?.getBilling()!!)
    var mInventorySubs: Inventory? = null
    lateinit var request: BillingRequests
    private var isBought = false

    private inner class PurchaseListener(val context: Activity) : EmptyRequestListener<Purchase>() {

        override fun onSuccess(purchase: Purchase) {
            context.startActivity(Intent(context, MainActivity::class.java))
            context.finish()
        }

        override fun onError(response: Int, e: Exception) {
            Log.d("ERROR", e.message.toString())
        }
    }

    private inner class InventoryCallback : Inventory.Callback {
        override fun onLoaded(products: Inventory.Products) {
            val productSub = products[ProductTypes.SUBSCRIPTION]

            if (productSub.isPurchased(BUY_GOOGLE)) {
                isBought = true
            }
        }
    }

    private fun buy(sku: String) {
        if (!::request.isInitialized ) {
            Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (sku == BUY_GOOGLE)
            request.purchase(
                ProductTypes.SUBSCRIPTION,
                sku,
                null,
                mCheckout.purchaseFlow
            )
        request
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mCheckout.start()

        mCheckout.createPurchaseFlow(PurchaseListener(this))
        mCheckout.whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                this@MainActivity.request = requests
                request.getPurchases(
                    ProductTypes.IN_APP,
                    null,
                    object : RequestListener<Purchases> {
                        override fun onSuccess(p0: Purchases) {
                            if (p0.hasPurchase(BUY_GOOGLE)) {
                                isBought = true
                            } else {
                                Log.d("PURCHASE", "No")
                            }
                        }

                        override fun onError(p0: Int, p1: Exception) {
                            Log.d("PURCHASE", "$p0 ${p1.localizedMessage}")
                        }

                    })
            }

        })
        mInventorySubs = mCheckout.makeInventory()
        mInventorySubs?.load(
            Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.SUBSCRIPTION, BUY_GOOGLE), InventoryCallback()
        )
        setContent {
            WaterTrackerTheme {
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                Scaffold(
                    scaffoldState = scaffoldState,
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppBarCustom(
                            navController = navController,
                            onNavigationIconClick = {
                                scope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }
                        )
                    },
                    drawerGesturesEnabled = false,
                    drawerContent = {
                        NavigationHeader {
                            closeDrawer(scaffoldState, scope)
                        }
                        NavigationBody(
                            items = listOf(
                                MenuItem(
                                    id = HOME_ID,
                                    title = "Home",
                                    icon = R.drawable.home,
                                    contentDescription = "Go to Home Screen"
                                ),
                                MenuItem(
                                    id = STATISTICS_ID,
                                    title = "History/ Statistics",
                                    icon = R.drawable.statistic,
                                    contentDescription = "Go to History/ Statistics Screen"
                                ),
                                MenuItem(
                                    id = NOTICE_ID,
                                    title = "Notice",
                                    icon = R.drawable.notification,
                                    contentDescription = "Go to Notice Screen"
                                ),
                                MenuItem(
                                    id = SETTINGS_ID,
                                    title = "Personal Information",
                                    icon = R.drawable.settings,
                                    contentDescription = "Go to Personal Information Screen"
                                ),
                                MenuItem(
                                    id = LOG_OUT_ID,
                                    title = "Log out",
                                    icon = R.drawable.log_out,
                                    contentDescription = "Log out"
                                )
                            ),
                            onItemClick = {
                                when(it.id) {
                                    HOME_ID -> {
                                        navController.navigate(Screen.MainScreen.route)
                                    }
                                    STATISTICS_ID -> {
                                        navController.navigate(Screen.HistoryScreen.route)
                                    }
                                    SETTINGS_ID -> {
                                        navController.navigate(Screen.PersonalInformationScreen.route)
                                    }
                                    NOTICE_ID -> {
                                        navController.navigate(Screen.ReminderScreen.route)
                                    }
                                    LOG_OUT_ID -> {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        )
                    }
                ) { contentPadding ->
                    Box(modifier = Modifier.padding(contentPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.MainScreen.route
                        ) {
                            composable(
                                route = Screen.MainScreen.route
                            ) {
                                MainScreen(navController)
                                closeDrawer(scaffoldState, scope)
                            }
                            composable(
                                route = Screen.HistoryScreen.route
                            ) {
                                HistoryScreen(navController)
                                closeDrawer(scaffoldState, scope)
                            }
                            composable(
                                route = Screen.PersonalInformationScreen.route
                            ) {
                                PersonaInformationScreen(navController)
                                closeDrawer(scaffoldState, scope)
                            }
                            composable(
                                route = Screen.ReminderScreen.route
                            ) {
                                ReminderScreen(navController,
                                    isBought = isBought,
                                    purchaseClick = {
                                    buy(BUY_GOOGLE)
                                })
                                closeDrawer(scaffoldState, scope)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun closeDrawer(scaffoldState: ScaffoldState, scope: CoroutineScope) {
        scope.launch {
            scaffoldState.drawerState.close()
        }
    }
}

