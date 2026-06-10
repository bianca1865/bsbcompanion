package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import com.example.viewmodel.CompanionViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(this)
    val repository = Repository(database)

    val viewModel: CompanionViewModel by viewModels {
      CompanionViewModelFactory(repository)
    }

    setContent {
      val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
      val isDark = loggedInUser?.isDarkMode ?: true
      MyApplicationTheme(darkTheme = isDark) {
        MainAppScreen(viewModel)
      }
    }
  }
}

val CustomWalletIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "CustomWallet",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
  ).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.White)) {
      moveTo(19f, 5f)
      horizontalLineTo(5f)
      curveTo(3.9f, 5f, 3f, 5.9f, 3f, 7f)
      verticalLineTo(17f)
      curveTo(3f, 18.1f, 3.9f, 19f, 5f, 19f)
      horizontalLineTo(19f)
      curveTo(20.1f, 19f, 21f, 18.1f, 21f, 17f)
      verticalLineTo(14f)
      horizontalLineTo(16f)
      curveTo(14.9f, 14f, 14f, 13.1f, 14f, 12f)
      curveTo(14f, 10.9f, 14.9f, 10f, 16f, 10f)
      horizontalLineTo(21f)
      verticalLineTo(7f)
      curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
      close()
    }
    path(fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.White)) {
      moveTo(18f, 12f)
      curveTo(18f, 12.55f, 17.55f, 13f, 17f, 13f)
      horizontalLineTo(15.5f)
      curveTo(14.95f, 13f, 14.5f, 12.55f, 14.5f, 12f)
      curveTo(14.5f, 11.45f, 14.95f, 11f, 15.5f, 11f)
      horizontalLineTo(17f)
      curveTo(17.55f, 11f, 18f, 11.45f, 18f, 12f)
      close()
    }
  }.build()
}

val CustomBusIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "CustomBus",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
  ).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.White)) {
      moveTo(4f, 16f)
      curveTo(4f, 17.1f, 4.9f, 18f, 6f, 18f)
      horizontalLineTo(7f)
      verticalLineTo(20f)
      curveTo(7f, 20.6f, 7.4f, 21f, 8f, 21f)
      horizontalLineTo(9f)
      curveTo(9.6f, 21f, 10f, 20.6f, 10f, 20f)
      verticalLineTo(18f)
      horizontalLineTo(14f)
      verticalLineTo(20f)
      curveTo(14f, 20.6f, 14.4f, 21f, 15f, 21f)
      horizontalLineTo(16f)
      curveTo(16.6f, 21f, 17f, 20.6f, 17f, 20f)
      verticalLineTo(18f)
      horizontalLineTo(18f)
      curveTo(19.1f, 18f, 20f, 17.1f, 20f, 16f)
      verticalLineTo(6f)
      curveTo(20f, 3f, 17f, 3f, 12f, 3f)
      curveTo(7f, 3f, 4f, 3f, 4f, 6f)
      verticalLineTo(16f)
      close()
      moveTo(6f, 6f)
      horizontalLineTo(18f)
      verticalLineTo(11f)
      horizontalLineTo(6f)
      verticalLineTo(6f)
      close()
      moveTo(7.5f, 15f)
      curveTo(6.7f, 15f, 6f, 14.3f, 6f, 13.5f)
      curveTo(6f, 12.7f, 6.7f, 12f, 7.5f, 12f)
      curveTo(8.3f, 12f, 9f, 12.7f, 9f, 13.5f)
      curveTo(9f, 14.3f, 8.3f, 15f, 7.5f, 15f)
      close()
      moveTo(16.5f, 15f)
      curveTo(15.7f, 15f, 15f, 14.3f, 15f, 13.5f)
      curveTo(15f, 12.7f, 15.7f, 12f, 16.5f, 12f)
      curveTo(17.3f, 12f, 18f, 12.7f, 18f, 13.5f)
      curveTo(18f, 14.3f, 17.3f, 15f, 16.5f, 15f)
      close()
    }
  }.build()
}

val CustomSavingsIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "CustomSavings",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
  ).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.White)) {
      moveTo(12f, 2f)
      curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
      curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
      curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
      curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
      close()
      moveTo(12f, 19f)
      curveTo(8.1f, 19f, 5f, 15.9f, 5f, 12f)
      curveTo(5f, 8.1f, 8.1f, 5f, 12f, 5f)
      curveTo(15.9f, 5f, 19f, 8.1f, 19f, 12f)
      curveTo(19f, 15.9f, 15.9f, 19f, 12f, 19f)
      close()
      moveTo(12.5f, 7f)
      horizontalLineTo(11f)
      verticalLineTo(8.5f)
      horizontalLineTo(9.5f)
      verticalLineTo(10f)
      horizontalLineTo(11f)
      verticalLineTo(11.5f)
      horizontalLineTo(9.5f)
      verticalLineTo(13f)
      horizontalLineTo(11f)
      verticalLineTo(15.5f)
      curveTo(11f, 16.3f, 11.7f, 17f, 12.5f, 17f)
      horizontalLineTo(13f)
      verticalLineTo(15.5f)
      horizontalLineTo(14.5f)
      verticalLineTo(14f)
      horizontalLineTo(13f)
      verticalLineTo(12.5f)
      horizontalLineTo(14.5f)
      verticalLineTo(11f)
      horizontalLineTo(13f)
      verticalLineTo(8.5f)
      curveTo(13f, 7.7f, 12.8f, 7f, 12.5f, 7f)
      close()
    }
  }.build()
}

// Represent the 6 interactive screens of our BSB Savings Companion
enum class NavigationTab(val title: String, val icon: ImageVector) {
  OVERVIEW("Overview", Icons.Default.Home),
  CALENDAR("Calendar", Icons.Default.DateRange),
  AUTOPAY("Auto-Pay", Icons.Default.Refresh),
  ACCOUNTS("BSB Wallets", CustomWalletIcon),
  EXPENSES("Expenses", Icons.Default.Check),
  PROFILE("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: CompanionViewModel) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // States
  val accounts by viewModel.accounts.collectAsStateWithLifecycle()
  val cards by viewModel.cards.collectAsStateWithLifecycle()
  val payments by viewModel.payments.collectAsStateWithLifecycle()
  val expenses by viewModel.expenses.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val simulatedDay by viewModel.simulatedDay.collectAsStateWithLifecycle()
  val freeDataMode by viewModel.freeDataMode.collectAsStateWithLifecycle()
  val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
  val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
  var showNotificationsOverlay by remember { mutableStateOf(false) }
  var purchaseBiometricApproved by remember { mutableStateOf(false) }

  var selectedAccountId by remember(accounts) {
    mutableStateOf(accounts.firstOrNull()?.id ?: -1)
  }

  // Listen for simulated automated payment sweeps to notify the user immediately
  LaunchedEffect(key1 = true) {
    viewModel.paymentExecutionEvent.collectLatest { message ->
      scope.launch {
        snackbarHostState.showSnackbar(
          message = message,
          actionLabel = "Review",
          duration = SnackbarDuration.Long
        )
      }
    }
  }

  // Transaction Approval Dialog
  val pendingApproval by viewModel.pendingApproval.collectAsStateWithLifecycle()
  if (pendingApproval != null) {
    val purchase = pendingApproval!!
    val isFrozen = loggedInUser?.isCardFrozen ?: false
    val dailyLimit = loggedInUser?.dailyCardLimit ?: 5000.0
    val exceedsLimit = purchase.amount > dailyLimit
    
    AlertDialog(
      onDismissRequest = { viewModel.declinePurchase(purchase) },
      containerColor = NavySurface,
      shape = RoundedCornerShape(24.dp),
      title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = CoralOrange.copy(alpha = 0.15f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield SECURE",
                        tint = CoralOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "BSB VISA 3D-SECURE V2",
                color = CoralOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Transaction Authorization",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
      },
      text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "A payment authorization request is pending on your custom virtual BSB card.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Merchant and amount detail card (Glass / high contrast)
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = purchase.merchantName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Card: " + purchase.cardNumberMasked,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "BWP ${String.format("%,.2f", purchase.amount)}",
                        color = GoldOrange,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Interactive Tactile Biometric Scanner Sensor Placeholder
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (purchaseBiometricApproved) Color(0x2210B981) else NavySurface
                ),
                border = BorderStroke(
                    1.5.dp, 
                    if (purchaseBiometricApproved) Color(0xFF10B981) else CoralOrange.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isFrozen && !exceedsLimit) {
                            purchaseBiometricApproved = true
                            Toast.makeText(context, "Biometric matches BSB customer profile. Authorized!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Cannot authorize: Transaction is currently blocked.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("dialog_biometric_placeholder")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (purchaseBiometricApproved) Color(0x3310B981) else CoralOrange.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (purchaseBiometricApproved) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = "Scan Finger",
                            tint = if (purchaseBiometricApproved) Color(0xFF10B981) else CoralOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (purchaseBiometricApproved) "🔒 BIOMETRIC SIGNATURE VERIFIED" else "🛡️ BIOMETRIC ACCESS REQUIRED",
                            color = if (purchaseBiometricApproved) Color(0xFF10B981) else CoralOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (purchaseBiometricApproved) "Press authorize button below to close checkout." else "Tap the Lock sensor above to simulate secure fingerprint verification.",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            
            if (isFrozen) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text("Blocked: Your card is frozen. Please unfreeze in Profile.", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (exceedsLimit) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text("Blocked: Exceeds daily spend limit (P ${dailyLimit.toInt()})", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (!purchaseBiometricApproved && !isFrozen && !exceedsLimit) {
                Toast.makeText(context, "Verification Failed: Tap the secure fingerprint sensor placeholder first!", Toast.LENGTH_LONG).show()
            } else {
                viewModel.approvePurchase(purchase) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    if (success) {
                        selectedTab = NavigationTab.OVERVIEW
                    }
                }
            }
          },
          colors = ButtonDefaults.buttonColors(
              containerColor = if (purchaseBiometricApproved) CoralOrange else CoralOrange.copy(alpha = 0.5f)
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("SECURELY AUTHORIZE", color = NavyBackground, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            viewModel.declinePurchase(purchase)
            Toast.makeText(context, "Online checkout transaction declined.", Toast.LENGTH_SHORT).show()
          }
        ) {
          Text("DECLINE", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    )
  }

  if (!isLoggedIn) {
    AuthScreen(viewModel = viewModel) {
      // Login success callback triggers auto recompose of main view
    }
  } else {
    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      bottomBar = {
        NavigationBar(
          containerColor = NavySurface,
          tonalElevation = 8.dp,
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          NavigationTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
              selected = isSelected,
              onClick = { selectedTab = tab },
              icon = {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.title,
                  tint = if (isSelected) CoralOrange else TextMuted,
                  modifier = Modifier.size(26.dp)
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = NavyPrimary
              ),
              alwaysShowLabel = false,
              modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
          }
        }
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(NavyBackground)
          .padding(paddingValues)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // App Header Branding Box
          CompanionHeader(
            simulatedDay = simulatedDay,
            freeDataMode = freeDataMode,
            notifications = notifications,
            onToggleFreeData = { viewModel.toggleFreeDataMode() },
            onAdvanceDay = { viewModel.advanceSimulatedDay() },
            onNotificationClick = { showNotificationsOverlay = true },
            onLogOut = { viewModel.logOut() }
          )

          // Main Tab Switch Board
          Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
              NavigationTab.OVERVIEW -> OverviewScreen(
                viewModel = viewModel,
                accounts = accounts,
                cards = cards,
                payments = payments,
                expenses = expenses,
                selectedAccountId = selectedAccountId,
                onAccountSelect = { selectedAccountId = it }
              )
              NavigationTab.CALENDAR -> CalendarScreen(
                viewModel = viewModel,
                payments = payments,
                accounts = accounts,
                simulatedDay = simulatedDay
              )
              NavigationTab.AUTOPAY -> AutoPayScreen(
                viewModel = viewModel,
                accounts = accounts,
                cards = cards,
                payments = payments
              )
              NavigationTab.ACCOUNTS -> AccountsScreen(
                viewModel = viewModel,
                accounts = accounts,
                cards = cards
              )
              NavigationTab.EXPENSES -> ExpensesScreen(
                viewModel = viewModel,
                expenses = expenses
              )
              NavigationTab.PROFILE -> ProfileScreen(
                viewModel = viewModel,
                notifications = notifications
              )
            }
          }
        }
      }
    }
  }

  // Custom Notifications Dialog Overlay (Bell icon at the top)
  if (showNotificationsOverlay) {
    Dialog(onDismissRequest = { showNotificationsOverlay = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = NavySurface,
        border = BorderStroke(1.5.dp, CoralOrange.copy(alpha = 0.35f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(520.dp)
          .padding(8.dp)
          .testTag("notifications_overlay")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
        ) {
          // Header of notifications overlay
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = CoralOrange.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = CoralOrange,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "BOTSWANA SAVINGS BANK",
                  color = CoralOrange,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                )
                Text(
                  text = "Receipts & Alerts",
                  color = TextPrimary,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            
            // Close Button
            IconButton(
              onClick = { showNotificationsOverlay = false },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close notifications",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          
          Spacer(modifier = Modifier.height(14.dp))
          
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val unread = notifications.count { !it.isRead }
            Text(
              text = if (unread > 0) "$unread brand-new events" else "No unread events",
              color = if (unread > 0) CoralOrange else TextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            
            TextButton(
              onClick = { viewModel.clearAllNotifications() },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text(
                text = "Clear All Receipts",
                color = CoralOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
          
          Spacer(modifier = Modifier.height(10.dp))
          Spacer(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(NavyPrimary))
          Spacer(modifier = Modifier.height(12.dp))
          
          if (notifications.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Notifications,
                  contentDescription = "No alerts",
                  tint = TextMuted.copy(alpha = 0.3f),
                  modifier = Modifier.size(44.dp)
                )
                Text(
                  text = "No savings receipts or automated alerts present.",
                  color = TextMuted,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Center
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(notifications) { notif ->
                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = if (notif.isRead) NavyPrimary.copy(alpha = 0.5f) else NavyPrimary
                  ),
                  border = BorderStroke(
                    width = 1.dp,
                    color = if (notif.isRead) Color.Transparent else CoralOrange.copy(alpha = 0.15f)
                  ),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.markNotificationAsRead(notif.id) }
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.Top
                    ) {
                      Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        if (!notif.isRead) {
                          Box(
                            modifier = Modifier
                              .size(6.dp)
                              .clip(CircleShape)
                              .background(CoralOrange)
                          )
                        }
                        Text(
                          text = notif.title,
                          color = if (notif.isRead) TextPrimary.copy(alpha = 0.8f) else TextPrimary,
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                      
                      Text(
                        text = java.text.SimpleDateFormat("d MMM, hh:mm a")
                          .format(java.util.Date(notif.timestamp)),
                        color = TextMuted,
                        fontSize = 8.sp
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = notif.message,
                      color = if (notif.isRead) TextMuted else TextPrimary.copy(alpha = 0.9f),
                      fontSize = 11.sp,
                      lineHeight = 15.sp
                    )
                  }
                }
              }
            }
          }
          
          Spacer(modifier = Modifier.height(12.dp))
          
          Button(
            onClick = { showNotificationsOverlay = false },
            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
          ) {
            Text("ACKNOWLEDGE & CLOSE", color = NavyBackground, fontWeight = FontWeight.Black, fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
fun CompanionHeader(
  simulatedDay: Int,
  freeDataMode: Boolean,
  notifications: List<AppNotification>,
  onToggleFreeData: () -> Unit,
  onAdvanceDay: () -> Unit,
  onNotificationClick: () -> Unit,
  onLogOut: () -> Unit
) {
  val unreadCount = notifications.count { !it.isRead }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(NavyPrimary)
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = R.drawable.bsb_companion_icon_1780844988616),
          contentDescription = "BSB Companion Logo",
          modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "BSB COMPANION",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif
          )
          Text(
            text = "Savings Partner Utility",
            color = CoralOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Notification Badge Bell
        IconButton(
          onClick = onNotificationClick,
          modifier = Modifier.size(38.dp)
        ) {
          BadgedBox(
            badge = {
              if (unreadCount > 0) {
                Badge(
                  containerColor = CoralOrange,
                  contentColor = Color.White
                ) {
                  Text(unreadCount.toString(), fontSize = 9.sp)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = if (unreadCount > 0) CoralOrange else TextPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  viewModel: CompanionViewModel,
  onAuthSuccess: () -> Unit
) {
  val context = LocalContext.current
  val registeredUsers by viewModel.registeredUsers.collectAsStateWithLifecycle()

  var isRegisterTab by remember(registeredUsers) { mutableStateOf(registeredUsers.isEmpty()) }

  // Login Inputs state (Prefill email if user registered)
  val defaultEmail = registeredUsers.firstOrNull()?.email ?: ""
  var loginEmail by remember(defaultEmail) { mutableStateOf(defaultEmail) }
  var loginPassword by remember { mutableStateOf("") }
  var loginPasswordVisible by remember { mutableStateOf(false) }

  // Register Inputs state
  var regFullName by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regCellphone by remember { mutableStateOf("") }
  var regCardNumber by remember { mutableStateOf("") }
  var regCardExpiry by remember { mutableStateOf("") }
  var regCardCvvOrPin by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regConfirmPassword by remember { mutableStateOf("") }
  var regBiometricsEnabled by remember { mutableStateOf(true) }

  // Error / Dialog modes
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showBiometricDialog by remember { mutableStateOf(false) }
  var biometricScanSuccess by remember { mutableStateOf(false) }

  LaunchedEffect(biometricScanSuccess) {
    if (biometricScanSuccess) {
      kotlinx.coroutines.delay(1000)
      showBiometricDialog = false
      biometricScanSuccess = false
      viewModel.loginWithBiometrics { success, msg ->
        if (success) {
          onAuthSuccess()
        } else {
          errorMessage = msg
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(NavyBackground)
  ) {
    // Aesthetic Top Wave Curved Header with Botswana Savings Bank (BSB) Branding
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(NavyPrimary)
    ) {
      // Draw curves
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
          moveTo(0f, h * 0.7f)
          quadraticTo(w * 0.5f, h * 0.4f, w, h * 0.8f)
          lineTo(w, h)
          lineTo(0f, h)
          close()
        }
        drawPath(
          path,
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFF15A24).copy(alpha = 0.2f), Color(0xFFFF8C00).copy(alpha = 0.1f))
          )
        )
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.bsb_companion_icon_1780844988616),
              contentDescription = "BSB Companion Logo",
              modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "BOTSWANA SAVINGS BANK",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
              )
              Text(
                text = "BSB Savings Companion • Secure Bank Access",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Column {
          Text(
            text = if (isRegisterTab) "Dumelang • Join BSB App" else "Dumelang • Welcome Back",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = if (isRegisterTab)
              "Register your secure mobile banking client profile"
            else "Enter your details or authenticate using biometric fingerprints",
            color = CoralOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // Tab Selector FNB style: full width matching BSB navy/orange
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
         .clip(RoundedCornerShape(12.dp))
        .background(NavySurface)
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .background(if (!isRegisterTab) CoralOrange else Color.Transparent)
          .clickable { isRegisterTab = false }
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Log In",
            tint = if (!isRegisterTab) NavyBackground else TextMuted,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "SECURE LOG IN",
            color = if (!isRegisterTab) NavyBackground else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .background(if (isRegisterTab) CoralOrange else Color.Transparent)
          .clickable { isRegisterTab = true }
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Register",
            tint = if (isRegisterTab) NavyBackground else TextMuted,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "CREATE PROFILE",
            color = if (isRegisterTab) NavyBackground else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Error Display
    errorMessage?.let { msg ->
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error
          )
          Text(
            text = msg,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
              .size(18.dp)
              .clickable { errorMessage = null }
          )
        }
      }
    }

    Box(modifier = Modifier.weight(1f)) {
      if (!isRegisterTab) {
        // LOG IN SCREEN LAYOUT
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Spacer(modifier = Modifier.height(8.dp))

          if (registeredUsers.isNotEmpty()) {
            val primaryUser = registeredUsers.first()
            Card(
              colors = CardDefaults.cardColors(containerColor = NavySurface),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .background(CoralOrange, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = primaryUser.fullName.firstOrNull()?.toString()?.uppercase() ?: "U",
                    color = NavyBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "Logged Registered Profile:", color = TextMuted, fontSize = 10.sp)
                  Text(
                    text = primaryUser.fullName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "Botswana Mobile: +267 ${primaryUser.cellphone}",
                    color = TextMuted,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }

          // Username / Email input
          OutlinedTextField(
            value = loginEmail,
            onValueChange = { loginEmail = it },
            label = { Text("Registered Email Address") },
            placeholder = { Text("e.g. masego@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = TextMuted) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_email_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CoralOrange,
              unfocusedBorderColor = NavyDistant,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
          )

          // Password Input
          OutlinedTextField(
            value = loginPassword,
            onValueChange = { loginPassword = it },
            label = { Text("Authentication Password") },
            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = TextMuted) },
            trailingIcon = {
              IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                Icon(
                  imageVector = if (loginPasswordVisible) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                  contentDescription = "Toggle Visibility",
                  tint = TextMuted
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CoralOrange,
              unfocusedBorderColor = NavyDistant,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
          )

          // Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                viewModel.loginWithPassword(loginEmail, loginPassword) { success, msg ->
                  if (success) {
                    onAuthSuccess()
                  } else {
                    errorMessage = msg
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("login_btn_submit")
            ) {
              Text(
                text = "LOG IN Securely",
                color = NavyBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            val isBiometricEnabledOnProfile = registeredUsers.firstOrNull()?.biometricsEnabled ?: false
            val biometricTint = if (isBiometricEnabledOnProfile) CoralOrange else TextMuted

            IconButton(
              onClick = {
                if (registeredUsers.isEmpty()) {
                  errorMessage = "No registered client profiles found. Please register first."
                } else if (!isBiometricEnabledOnProfile) {
                  errorMessage = "Biometric access is disabled. Please log in with password and enable biometrics in registration details."
                } else {
                  showBiometricDialog = true
                  biometricScanSuccess = false
                }
              },
              modifier = Modifier
                .size(50.dp)
                .border(1.5.dp, biometricTint, RoundedCornerShape(10.dp))
                .background(NavySurface, RoundedCornerShape(10.dp))
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Biometrics Fingerprint Trigger",
                tint = biometricTint,
                modifier = Modifier.size(24.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Quick Demo Bypass
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                if (registeredUsers.isEmpty()) {
                  viewModel.registerCustomer(
                    email = "masego@gmail.com",
                    fullName = "Masego L. Kaelo",
                    cellphone = "71649231",
                    cardNumber = "4556102434529012",
                    cardExpiry = "10/29",
                    cardCvvOrPin = "123",
                    passwordHash = "1234",
                    biometricsEnabled = true
                  ) { s, m -> }
                }
                viewModel.loginWithPassword("masego@gmail.com", "1234") { s, m ->
                  if (s) onAuthSuccess()
                }
              }
              .background(NavySurface)
              .padding(14.dp),
            color = Color.Transparent
          ) {
            Column {
              Text(
                text = "💡 QUICK DEMO AUTO-LOGIN",
                color = CoralOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "Click here to auto-fill registration (Masego L. Kaelo) and log in straight to the simulated sandbox immediately.",
                color = TextMuted,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
        }
      } else {
        // REGISTER SCREEN LAYOUT
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Spacer(modifier = Modifier.height(8.dp))

          // 1. PROFILE INFO
          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                "1. Client Personal Info",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )

              OutlinedTextField(
                value = regFullName,
                onValueChange = { regFullName = it },
                label = { Text("Full Legal Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User icon", tint = TextMuted) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_fullName"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralOrange,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                )
              )

              OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Preferred Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email icon", tint = TextMuted) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_email"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralOrange,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
              )
            }
          }

          // 2. CELLPHONE SETUP
          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                "2. Mobile Cellphone (Botswana +267)",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .height(56.dp)
                    .width(75.dp)
                    .background(NavyPrimary, RoundedCornerShape(4.dp))
                    .border(1.dp, NavyDistant, RoundedCornerShape(4.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+267", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                }

                OutlinedTextField(
                  value = regCellphone,
                  onValueChange = { if (it.length <= 8) regCellphone = it },
                  label = { Text("Mobile Phone") },
                  placeholder = { Text("e.g. 71649231") },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("reg_cellphone"),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralOrange,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
              }
              Text(
                "FNB and BSB utilize mobile auth SMS verifications. Input an 8-digit Botswana mobile number.",
                color = TextMuted,
                fontSize = 9.sp
              )
            }
          }

          // 3. BSB CARD LINKING
          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                "3. BSB Debit/Credit Card Mapping",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )

              OutlinedTextField(
                value = regCardNumber,
                onValueChange = { if (it.length <= 16) regCardNumber = it },
                label = { Text("16-Digit Card Number") },
                leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = "Card icon", tint = TextMuted) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_cardNumber"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralOrange,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = regCardExpiry,
                  onValueChange = { regCardExpiry = it },
                  label = { Text("Expiry (MM/YY)") },
                  placeholder = { Text("10/29") },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("reg_cardExpiry"),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralOrange,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  )
                )
                OutlinedTextField(
                  value = regCardCvvOrPin,
                  onValueChange = { if (it.length <= 4) regCardCvvOrPin = it },
                  label = { Text("CVV/ATM PIN") },
                  visualTransformation = PasswordVisualTransformation(),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("reg_cardCvvOrPin"),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralOrange,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
              }
            }
          }

          // 4. SECURE PASSWORD CREDENTIALS
          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                "4. Access Security Credentials",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )

              OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Login Code/Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_password"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralOrange,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )

              OutlinedTextField(
                value = regConfirmPassword,
                onValueChange = { regConfirmPassword = it },
                label = { Text("Confirm Code/Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_confirmPassword"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralOrange,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Biometrics Enabled",
                    tint = CoralOrange
                  )
                  Column {
                    Text(
                      "Enable Fingerprint ID",
                      color = TextPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      "Fast, passwordless biometric access login.",
                      color = TextMuted,
                      fontSize = 9.sp
                    )
                  }
                }
                Switch(
                  checked = regBiometricsEnabled,
                  onCheckedChange = { regBiometricsEnabled = it },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = NavyBackground,
                    checkedTrackColor = CoralOrange,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = NavyPrimary
                  )
                )
              }
            }
          }

          // Register Submit Button
          Button(
            onClick = {
              if (regPassword != regConfirmPassword) {
                errorMessage = "Password confirmation does not match. Please verify."
              } else {
                viewModel.registerCustomer(
                  email = regEmail,
                  fullName = regFullName,
                  cellphone = regCellphone,
                  cardNumber = regCardNumber,
                  cardExpiry = regCardExpiry,
                  cardCvvOrPin = regCardCvvOrPin,
                  passwordHash = regPassword,
                  biometricsEnabled = regBiometricsEnabled
                ) { success, msg ->
                  if (success) {
                    loginEmail = regEmail
                    loginPassword = ""
                    isRegisterTab = false
                    errorMessage = null
                    Toast.makeText(context, "Profile Created! Enter password below to log in.", Toast.LENGTH_LONG).show()
                  } else {
                    errorMessage = msg
                  }
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("reg_btn_submit")
          ) {
            Text(
              text = "REGISTER ONLINE PROFILE",
              color = NavyBackground,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(28.dp))
        }
      }
    }
  }

  // Custom Biometric Scan Animation Dialog
  if (showBiometricDialog) {
    Dialog(onDismissRequest = { showBiometricDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = NavySurface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "BSB Biometric Match",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )

          Text(
            text = "Hold your finger on the touch sensor below to authenticate securely.",
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
          )

          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(CircleShape)
              .background(NavyPrimary)
              .clickable {
                biometricScanSuccess = true
              },
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.size(60.dp)) {
              val r = size.width / 2
              drawCircle(color = CoralOrange.copy(alpha = 0.3f), radius = r, style = Stroke(width = 2.dp.toPx()))
              drawCircle(color = CoralOrange.copy(alpha = 0.5f), radius = r * 0.75f, style = Stroke(width = 2.dp.toPx()))
              drawCircle(color = CoralOrange.copy(alpha = 0.8f), radius = r * 0.5f, style = Stroke(width = 2.dp.toPx()))
              drawCircle(color = CoralOrange, radius = r * 0.25f, style = Stroke(width = 2.dp.toPx()))
            }

            Icon(
              imageVector = if (biometricScanSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
              contentDescription = "Scan",
              tint = if (biometricScanSuccess) Color.Green else CoralOrange,
              modifier = Modifier.size(36.dp)
            )
          }

          Text(
            text = if (biometricScanSuccess) "Authentication Approved!" else "Tap fingerprint sensor to scan",
            color = if (biometricScanSuccess) Color.Green else CoralOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ---------------- OVERVIEW SCREEN ----------------
@Composable
fun OverviewScreen(
  viewModel: CompanionViewModel,
  accounts: List<BSBAccount>,
  cards: List<BSBCard>,
  payments: List<ScheduledPayment>,
  expenses: List<ExpenseItem>,
  selectedAccountId: Int,
  onAccountSelect: (Int) -> Unit
) {
  val scrollState = rememberScrollState()

  // Find active account
  val activeAccountId = if (selectedAccountId == -1 && accounts.isNotEmpty()) accounts.first().id else selectedAccountId
  val activeAccount = accounts.find { it.id == activeAccountId }
  val activeCard = cards.find { it.linkedAccountId == activeAccountId }

  // Track Budget & Spending
  var monthlyLimitText by remember { mutableStateOf("2200") }
  val budgetLimit = monthlyLimitText.toDoubleOrNull() ?: 2200.00
  val currentMonthTotal = expenses.sumOf { it.amount }
  val budgetProgress = if (budgetLimit > 0) (currentMonthTotal / budgetLimit).coerceIn(0.0, 1.0) else 0.0

  // Student Allowance Allocator state with Profile custom limits
  val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
  val foodLimit = loggedInUser?.foodMaxLimit?.toFloat() ?: 1500f
  val rentLimit = loggedInUser?.rentMaxLimit?.toFloat() ?: 3000f
  val transportLimit = loggedInUser?.transportMaxLimit?.toFloat() ?: 1000f
  val savingsLimit = loggedInUser?.savingsMaxLimit?.toFloat() ?: 2000f

  var foodAlloc by remember { mutableStateOf(1000f) }
  var rentAlloc by remember { mutableStateOf(700f) }
  var transportAlloc by remember { mutableStateOf(250f) }
  var savingsAlloc by remember { mutableStateOf(250f) }
  var totalAllowanceLimit by remember { mutableStateOf(2200f) }
  var isTotalPlannedLocked by remember { mutableStateOf(false) }

  var foodAllocInput by remember(foodAlloc) { mutableStateOf(foodAlloc.toInt().toString()) }
  var rentAllocInput by remember(rentAlloc) { mutableStateOf(rentAlloc.toInt().toString()) }
  var transportAllocInput by remember(transportAlloc) { mutableStateOf(transportAlloc.toInt().toString()) }
  var savingsAllocInput by remember(savingsAlloc) { mutableStateOf(savingsAlloc.toInt().toString()) }
  var totalAllowanceInput by remember(totalAllowanceLimit) { mutableStateOf(totalAllowanceLimit.toInt().toString()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    
    // 1. Tactile Account SWITCHER Button Row
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        accounts.forEach { acc ->
          val isSelected = acc.id == activeAccountId
          val cardColor = if (isSelected) CoralOrange else NavySurface
          val textColor = if (isSelected) NavyBackground else TextPrimary
          val borderStroke = if (isSelected) Color.Transparent else CoralOrange.copy(alpha = 0.3f)
          
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(cardColor)
              .clickable { onAccountSelect(acc.id) }
              .border(1.dp, borderStroke, RoundedCornerShape(10.dp))
              .padding(vertical = 10.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              val simplifiedName = acc.accountName
                .replace("BSB", "")
                .replace("Account", "")
                .replace("Smart", "")
                .replace("Plan", "")
                .trim()
              
              Text(
                text = simplifiedName,
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "P ${String.format("%.0f", acc.balance)}",
                color = if (isSelected) NavyBackground else GoldOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 2. Active Selected Account Wealth Card
    activeAccount?.let { acc ->
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = acc.accountName,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
              )
              Text(
                text = "No. ${acc.accountNumber}",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          
          Spacer(modifier = Modifier.height(6.dp))
          
          Text(
            text = "BWP ${String.format("%,.2f", acc.balance)}",
            color = GoldOrange,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp
          )
        }
      }
    }

    // 3. Realistic Card Image with beautiful overlays
    val cardTypeLabel = when {
      activeAccount?.accountName?.contains("Allowance", ignoreCase = true) == true -> "Student Card"
      else -> "Youth Card"
    }

    val displayCard = activeCard ?: BSBCard(
      cardHolder = "Masego L. Kaelo",
      cardNumberMasked = "**** **** **** " + (activeAccount?.accountNumber?.takeLast(4) ?: "8888"),
      cardExpiry = "10/29",
      linkedAccountId = activeAccountId,
      cardType = cardTypeLabel
    )

    BSBThemedCard(
      cardType = displayCard.cardType,
      cardNumberMasked = displayCard.cardNumberMasked,
      cardHolder = displayCard.cardHolder,
      cardExpiry = displayCard.cardExpiry,
      associatedAccountName = activeAccount?.accountName,
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
    )

    val foodAllocCoerced = foodAlloc.coerceIn(100f, foodLimit.coerceAtLeast(101f))
    val rentAllocCoerced = rentAlloc.coerceIn(100f, rentLimit.coerceAtLeast(101f))
    val transportAllocCoerced = transportAlloc.coerceIn(50f, transportLimit.coerceAtLeast(51f))
    val savingsAllocCoerced = savingsAlloc.coerceIn(0f, savingsLimit.coerceAtLeast(1f))

    // 4. Interactive Live Student Allowance Allocator (Budget Planner)
    val totalAllocated = foodAllocCoerced + rentAllocCoerced + transportAllocCoerced + savingsAllocCoerced
    val remainingAllowance = totalAllowanceLimit - totalAllocated
    val progressOfAllowance = (totalAllocated / totalAllowanceLimit.coerceAtLeast(1f)).coerceIn(0f, 1f)

    Card(
      colors = CardDefaults.cardColors(containerColor = NavySurface),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Allowance Allocator & Planner",
          color = CoralOrange,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp
        )

        HorizontalDivider(color = NavyPrimary, thickness = 1.dp)

        // Separated Total Planned component with distinct BlueAccent color and Lock/Unlock functionality
        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkThemeGlobal) Color(0x1200B4D8) else Color(0x0A00B4D8)
          ),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(
            1.dp,
            if (isTotalPlannedLocked) BlueAccent.copy(alpha = 0.25f) else BlueAccent.copy(alpha = 0.6f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = if (isTotalPlannedLocked) "Locked" else "Unlocked",
                  tint = if (isTotalPlannedLocked) Color.Red.copy(alpha = 0.8f) else BlueAccent.copy(alpha = 0.5f),
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "TOTAL PLANNED",
                  color = BlueAccent,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp
                )
                if (isTotalPlannedLocked) {
                  Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "LOCKED",
                      color = Color.Red,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "BWP ${totalAllowanceLimit.toInt()}",
                  color = BlueAccent,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black
                )
                IconButton(
                  onClick = { isTotalPlannedLocked = !isTotalPlannedLocked },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Toggle Lock",
                    tint = if (isTotalPlannedLocked) Color.Red.copy(alpha = 0.8f) else BlueAccent,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            // Amount input field above slider for Total Planned
            OutlinedTextField(
              value = totalAllowanceInput,
              onValueChange = { newVal ->
                if (!isTotalPlannedLocked) {
                  val clean = newVal.filter { it.isDigit() }
                  totalAllowanceInput = clean
                  clean.toFloatOrNull()?.let {
                    totalAllowanceLimit = it
                  }
                }
              },
              enabled = !isTotalPlannedLocked,
              placeholder = { Text("Enter Total Planned...", fontSize = 11.sp, color = TextMuted) },
              textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isTotalPlannedLocked) Color.Gray else BlueAccent),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BlueAccent,
                unfocusedTextColor = BlueAccent,
                disabledTextColor = Color.Gray,
                focusedBorderColor = BlueAccent,
                unfocusedBorderColor = if (isDarkThemeGlobal) BlueAccent.copy(alpha = 0.3f) else BlueAccent.copy(alpha = 0.2f),
                disabledBorderColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                focusedContainerColor = if (isDarkThemeGlobal) Color(0x0F00B4D8) else Color(0x0500B4D8),
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
              ),
              prefix = { Text("BWP ", fontSize = 11.sp, color = if (isTotalPlannedLocked) Color.Gray else BlueAccent, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(8.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Slider(
              value = totalAllowanceLimit,
              onValueChange = { if (!isTotalPlannedLocked) totalAllowanceLimit = it },
              valueRange = 1000f..10000f,
              enabled = !isTotalPlannedLocked,
              colors = SliderDefaults.colors(
                thumbColor = if (isTotalPlannedLocked) Color.Gray else BlueAccent,
                activeTrackColor = BlueAccent,
                inactiveTrackColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                disabledThumbColor = Color.Gray.copy(alpha = 0.6f),
                disabledActiveTrackColor = BlueAccent.copy(alpha = 0.2f),
                disabledInactiveTrackColor = BlueAccent.copy(alpha = 0.1f)
              ),
              modifier = Modifier.height(24.dp)
            )
          }
        }

        // Progress breakdown & status message (Moved below Total Planned)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            if (remainingAllowance >= 0) {
              Text(
                text = "Remaining: BWP ${remainingAllowance.toInt()}",
                color = if (remainingAllowance == 0f) Color.Green else TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
              )
            } else {
              Text(
                text = "⚠️ Deficit: BWP ${Math.abs(remainingAllowance).toInt()}",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (remainingAllowance >= 0) Color(0x1A4CAF50) else Color(0x1AF44336))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = if (remainingAllowance >= 0) "IN BUDGET" else "OVERDRAFT",
              color = if (remainingAllowance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
              fontWeight = FontWeight.Black,
              fontSize = 10.sp
            )
          }
        }

        LinearProgressIndicator(
          progress = { progressOfAllowance },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
          color = if (remainingAllowance >= 0) GoldOrange else Color(0xFFF44336),
          trackColor = NavyPrimary
        )

        HorizontalDivider(color = NavyPrimary, thickness = 1.dp)

        // Custom Allocations Sliders Customization
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

          // Category 1: Groceries
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(16.dp))
                Text("Groceries", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Text("Max Limit: BWP ${foodLimit.toInt()}", color = GoldOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
              value = foodAllocInput,
              onValueChange = { newVal ->
                val clean = newVal.filter { it.isDigit() }
                foodAllocInput = clean
                clean.toFloatOrNull()?.let {
                  foodAlloc = it
                }
              },
              placeholder = { Text("Enter amount...", fontSize = 11.sp, color = TextMuted) },
              textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CoralOrange,
                unfocusedBorderColor = if (isDarkThemeGlobal) Color(0xFF334155) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                unfocusedContainerColor = Color.Transparent
              ),
              prefix = { Text("BWP ", fontSize = 11.sp, color = GoldOrange, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(8.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Slider(
              value = foodAllocCoerced,
              onValueChange = { foodAlloc = it },
              valueRange = 100f..foodLimit.coerceAtLeast(101f),
              colors = SliderDefaults.colors(
                thumbColor = CoralOrange,
                activeTrackColor = CoralOrange,
                inactiveTrackColor = NavyPrimary
              ),
              modifier = Modifier.height(24.dp)
            )
          }

          // Category 2: Rent / Campus Residence
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(16.dp))
                Text("Residence Rent / Boarding", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Text("Max Limit: BWP ${rentLimit.toInt()}", color = GoldOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
              value = rentAllocInput,
              onValueChange = { newVal ->
                val clean = newVal.filter { it.isDigit() }
                rentAllocInput = clean
                clean.toFloatOrNull()?.let {
                  rentAlloc = it
                }
              },
              placeholder = { Text("Enter amount...", fontSize = 11.sp, color = TextMuted) },
              textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CoralOrange,
                unfocusedBorderColor = if (isDarkThemeGlobal) Color(0xFF334155) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                unfocusedContainerColor = Color.Transparent
              ),
              prefix = { Text("BWP ", fontSize = 11.sp, color = GoldOrange, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(8.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Slider(
              value = rentAllocCoerced,
              onValueChange = { rentAlloc = it },
              valueRange = 100f..rentLimit.coerceAtLeast(101f),
              colors = SliderDefaults.colors(
                thumbColor = CoralOrange,
                activeTrackColor = CoralOrange,
                inactiveTrackColor = NavyPrimary
              ),
              modifier = Modifier.height(24.dp)
            )
          }

          // Category 3: Combi Transport
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(CustomBusIcon, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(16.dp))
                Text("Combi & Taxi Transport", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Text("Max Limit: BWP ${transportLimit.toInt()}", color = GoldOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
              value = transportAllocInput,
              onValueChange = { newVal ->
                val clean = newVal.filter { it.isDigit() }
                transportAllocInput = clean
                clean.toFloatOrNull()?.let {
                  transportAlloc = it
                }
              },
              placeholder = { Text("Enter amount...", fontSize = 11.sp, color = TextMuted) },
              textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CoralOrange,
                unfocusedBorderColor = if (isDarkThemeGlobal) Color(0xFF334155) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                unfocusedContainerColor = Color.Transparent
              ),
              prefix = { Text("BWP ", fontSize = 11.sp, color = GoldOrange, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(8.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Slider(
              value = transportAllocCoerced,
              onValueChange = { transportAlloc = it },
              valueRange = 50f..transportLimit.coerceAtLeast(51f),
              colors = SliderDefaults.colors(
                thumbColor = CoralOrange,
                activeTrackColor = CoralOrange,
                inactiveTrackColor = NavyPrimary
              ),
              modifier = Modifier.height(24.dp)
            )
          }

          // Category 4: savings
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(CustomSavingsIcon, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(16.dp))
                Text("Smart Emergency Savings", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Text("Max Limit: BWP ${savingsLimit.toInt()}", color = GoldOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
              value = savingsAllocInput,
              onValueChange = { newVal ->
                val clean = newVal.filter { it.isDigit() }
                savingsAllocInput = clean
                clean.toFloatOrNull()?.let {
                  savingsAlloc = it
                }
              },
              placeholder = { Text("Enter amount...", fontSize = 11.sp, color = TextMuted) },
              textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CoralOrange,
                unfocusedBorderColor = if (isDarkThemeGlobal) Color(0xFF334155) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                unfocusedContainerColor = Color.Transparent
              ),
              prefix = { Text("BWP ", fontSize = 11.sp, color = GoldOrange, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(8.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Slider(
              value = savingsAllocCoerced,
              onValueChange = { savingsAlloc = it },
              valueRange = 0f..savingsLimit.coerceAtLeast(1f),
              colors = SliderDefaults.colors(
                thumbColor = CoralOrange,
                activeTrackColor = CoralOrange,
                inactiveTrackColor = NavyPrimary
              ),
              modifier = Modifier.height(24.dp)
            )
          }
        }


      }
    }


  }
}

// ---------------- AUTOPAY SCREEN ----------------
@Composable
fun AutoPayScreen(
  viewModel: CompanionViewModel,
  accounts: List<BSBAccount>,
  cards: List<BSBCard>,
  payments: List<ScheduledPayment>
) {
  val context = LocalContext.current
  var showAddDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Guide Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Scheduled Auto-Payments", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
      }

      FilledIconButton(
        onClick = { showAddDialog = true },
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CoralOrange),
        modifier = Modifier
          .size(48.dp)
          .testTag("add_autopay_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Payment rules list
    if (payments.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Empty list",
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Your Auto-Scheduler is Empty",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Connect BSB savings elements, then authorize mobile or savings subscriptions.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(payments) { payment ->
          val linkedAccount = accounts.find { it.id == payment.selectedAccountId }
          val linkedCard = cards.find { it.id == payment.selectedCardId }

          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(CoralOrange)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = payment.payeeName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Expense Category: ${payment.paymentType}",
                  color = TextMuted,
                  fontSize = 12.sp
                )
                Text(
                  text = "Triggers: Day ${payment.paymentDay} of Month",
                  color = GoldOrange,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )

                if (!payment.recipientAccount.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Recipient: ${payment.recipientName ?: payment.payeeName} (Acc: ${payment.recipientAccount})\nBranch: ${payment.recipientBranchNumber ?: "N/A"} - ${payment.recipientBranchName ?: "N/A"}",
                    color = CoralOrange.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Account Selection Details Display
                Surface(
                  color = NavyPrimary,
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.padding(top = 4.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (linkedCard != null) Icons.Default.Lock else Icons.Default.AccountBox,
                      contentDescription = "Source Type",
                      tint = TextMuted,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (linkedCard != null) {
                        "Card ${linkedCard.cardNumberMasked.takeLast(4)} via ${linkedAccount?.accountName}"
                      } else {
                        "Account: ${linkedAccount?.accountName ?: "No Wallet Linked"}"
                      },
                      color = TextMuted,
                      fontSize = 10.sp
                    )
                  }
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "P ${String.format("%.2f", payment.amount)}",
                  color = CoralOrange,
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp
                )
                IconButton(
                  onClick = { viewModel.deletePayment(payment) },
                  modifier = Modifier.testTag("delete_payment_${payment.id}")
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralOrange.copy(alpha = 0.7f))
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog to configure a new automated paying rule
  if (showAddDialog) {
    var feeType by remember { mutableStateOf("Wifi") }
    var payeeText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var calendarDayText by remember { mutableStateOf("15") }
    var selectedAccIndex by remember { mutableStateOf(-1) }
    var selectedCardIndex by remember { mutableStateOf(-1) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Recipient specific bank details
    var recNameText by remember { mutableStateOf("") }
    var recAccountText by remember { mutableStateOf("") }
    var recBranchNumberText by remember { mutableStateOf("") }
    var recBranchNameText by remember { mutableStateOf("") }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var cardDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Savings", "Wifi", "Mobile Subscription", "Rent", "Other")

    Dialog(onDismissRequest = { showAddDialog = false }) {
      Surface(
        color = NavySurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Create Auto-Pay Rule",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Text(
            text = "Automate bills natively using linked BSB wallets.",
            color = TextMuted,
            fontSize = 11.sp
          )

          // Highly Visual Category Selection Matrices
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Select Bill Category", color = TextMuted, fontSize = 11.sp)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val categoriesList = listOf(
                Triple("Savings", "Savings Pot", Icons.Default.Star),
                Triple("Wifi", "Broadband Wifi", Icons.Default.Refresh),
                Triple("Mobile Subscription", "Mobile Data", Icons.Default.Phone),
                Triple("Rent", "Rent Payment", Icons.Default.Home),
                Triple("Other", "Other Outlay", Icons.Default.List)
              )
              
              categoriesList.forEach { (catType, label, icon) ->
                val isSelected = feeType == catType
                val cardColor = if (isSelected) CoralOrange else NavyPrimary
                val textColor = if (isSelected) NavyBackground else TextPrimary
                val iconColor = if (isSelected) NavyBackground else GoldOrange
                
                Box(
                  modifier = Modifier
                    .weight(1.0f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardColor)
                    .clickable {
                      feeType = catType
                      // auto suggest name and bank details based on tapped category
                      if (payeeText.isBlank() || payeeText.contains("Mascom") || payeeText.contains("BTC") || payeeText.contains("Rent") || payeeText.contains("Water") || payeeText.contains("Emergency") || payeeText.contains("Store")) {
                        payeeText = when (catType) {
                          "Savings" -> "BSB Golden Pot Savings"
                          "Wifi" -> "BTC Broadband Fibers"
                          "Mobile Subscription" -> "Mascom Mobile Data"
                          "Rent" -> "Gaborone Village Landlord"
                          else -> "Choppies Supermarket"
                        }
                      }
                      
                      if (catType == "Rent") {
                        recNameText = "Gaborone Village Properties"
                        recAccountText = "9080012456"
                        recBranchNumberText = "120305"
                        recBranchNameText = "BSB Main Gaborone"
                      }
                    }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                      imageVector = icon,
                      contentDescription = label,
                      tint = iconColor,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = label,
                      color = textColor,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black,
                      textAlign = TextAlign.Center,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }

          // Payee Field
          OutlinedTextField(
            value = payeeText,
            onValueChange = { payeeText = it },
            label = { Text("Provider/Payee Name (e.g. BTC Wifi)") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("payee_name_input")
          )

          // Amount Field
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (Pula / BWP)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pay_amount_input")
          )

          // Date selection field - Click opens a custom Pop-up Calendar
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Date of Payment", color = TextMuted, fontSize = 11.sp)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { showDatePickerDialog = true }
            ) {
              OutlinedTextField(
                value = if (calendarDayText.isNotBlank()) "Day $calendarDayText of Month" else "Tap to choose a payment day",
                onValueChange = {},
                readOnly = true,
                enabled = false, // ensures all clicks bubble up to the parent Box
                label = null,
                colors = TextFieldDefaults.colors(
                  disabledContainerColor = NavyPrimary,
                  disabledTextColor = TextPrimary,
                  disabledLabelColor = CoralOrange,
                  disabledIndicatorColor = Color.White.copy(alpha = 0.12f),
                  disabledPlaceholderColor = TextMuted
                ),
                trailingIcon = {
                  Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = CoralOrange
                  )
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("pay_day_input")
              )
            }
          }

          // Custom Calendar Popup Dialog/Overlay
          if (showDatePickerDialog) {
            Dialog(onDismissRequest = { showDatePickerDialog = false }) {
              Surface(
                color = NavySurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.5f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  Text(
                    text = "Select Payment Day",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                  )
                  
                  Text(
                    text = "Choose a day (1 - 31) for this automated billing schedule.",
                    color = TextMuted,
                    fontSize = 11.sp
                  )

                  // 1 to 31 Days represented as a calendar grid
                  val daysList = (1..31).toList()
                  val columns = 7
                  val rows = daysList.chunked(columns)

                  // Day of week labels mimicking a real calendar grid starting Monday
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                  ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                      Text(
                        text = label,
                        color = CoralOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(32.dp)
                      )
                    }
                  }

                  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowDays ->
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                      ) {
                        rowDays.forEach { dayNum ->
                          val isSelected = calendarDayText == dayNum.toString()
                          Box(
                            modifier = Modifier
                              .size(34.dp)
                              .clip(RoundedCornerShape(8.dp))
                              .background(
                                if (isSelected) CoralOrange else NavyPrimary.copy(alpha = 0.4f)
                              )
                              .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else if (isDarkThemeGlobal) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                              )
                              .clickable {
                                calendarDayText = dayNum.toString()
                                showDatePickerDialog = false
                              },
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = "$dayNum",
                              color = if (isSelected) NavyBackground else TextPrimary,
                              fontWeight = FontWeight.Black,
                              fontSize = 11.sp
                            )
                          }
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                  ) {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                      Text("Cancel", color = CoralOrange, fontWeight = FontWeight.Black)
                    }
                  }
                }
              }
            }
          }

          // RECIPIENT BANK DETAILS SECTION - ONLY FOR RENT
          if (feeType == "Rent") {
            Card(
              colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.5f)),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = "Recipient Bank Details (Rent Transfer Details)",
                  color = CoralOrange,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black
                )

                OutlinedTextField(
                  value = recNameText,
                  onValueChange = { recNameText = it },
                  label = { Text("Recipient Name") },
                  singleLine = true,
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = CoralOrange,
                    unfocusedLabelColor = TextMuted
                  ),
                  modifier = Modifier.fillMaxWidth().testTag("rec_name_input")
                )

                OutlinedTextField(
                  value = recAccountText,
                  onValueChange = { recAccountText = it },
                  label = { Text("Account Number") },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = CoralOrange,
                    unfocusedLabelColor = TextMuted
                  ),
                  modifier = Modifier.fillMaxWidth().testTag("rec_account_input")
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = recBranchNumberText,
                    onValueChange = { recBranchNumberText = it },
                    label = { Text("Branch Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                      focusedContainerColor = NavySurface,
                      unfocusedContainerColor = NavySurface,
                      focusedTextColor = TextPrimary,
                      unfocusedTextColor = TextPrimary,
                      focusedLabelColor = CoralOrange,
                      unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.weight(1f).testTag("rec_branch_code_input")
                  )

                  OutlinedTextField(
                    value = recBranchNameText,
                    onValueChange = { recBranchNameText = it },
                    label = { Text("Branch Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                      focusedContainerColor = NavySurface,
                      unfocusedContainerColor = NavySurface,
                      focusedTextColor = TextPrimary,
                      unfocusedTextColor = TextPrimary,
                      focusedLabelColor = CoralOrange,
                      unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.weight(1.2f).testTag("rec_branch_name_input")
                  )
                }
              }
            }
          }

          // Account selection drop down list
          Column {
            Text("BSB Debit Source Account", color = TextMuted, fontSize = 11.sp)
            Box {
              Button(
                onClick = { accountDropdownExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("account_dropdown_trigger")
              ) {
                Text(
                  text = if (selectedAccIndex >= 0 && selectedAccIndex < accounts.size) {
                    "${accounts[selectedAccIndex].accountName} (Bal: P${accounts[selectedAccIndex].balance})"
                  } else {
                    "Select Account"
                  },
                  color = TextPrimary
                )
              }
              DropdownMenu(
                expanded = accountDropdownExpanded,
                onDismissRequest = { accountDropdownExpanded = false },
                modifier = Modifier.background(NavyPrimary)
              ) {
                accounts.forEachIndexed { idx, acc ->
                  DropdownMenuItem(
                    text = { Text("${acc.accountName} (P${acc.balance})", color = TextPrimary) },
                    onClick = {
                      selectedAccIndex = idx
                      accountDropdownExpanded = false
                      // reset selected card if account changed
                      selectedCardIndex = -1
                    }
                  )
                }
              }
            }
          }

          // Card Selection Dropdown List (filtered by selected account)
          Column {
            Text("Select Connected Card (Optional)", color = TextMuted, fontSize = 11.sp)
            Box {
              Button(
                onClick = { cardDropdownExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("card_dropdown_trigger")
              ) {
                val cardSelected = selectedCardIndex >= 0 && selectedCardIndex < cards.size
                Text(
                  text = if (cardSelected) {
                    "Card: ${cards[selectedCardIndex].cardNumberMasked}"
                  } else {
                    "Direct Account Pay (No Card)"
                  },
                  color = TextPrimary
                )
              }
              DropdownMenu(
                expanded = cardDropdownExpanded,
                onDismissRequest = { cardDropdownExpanded = false },
                modifier = Modifier.background(NavyPrimary)
              ) {
                DropdownMenuItem(
                  text = { Text("Direct Account Pay (No Card)", color = TextPrimary) },
                  onClick = {
                    selectedCardIndex = -1
                    cardDropdownExpanded = false
                  }
                )
                // Filter cards linked to the selected account if any account is chosen
                val activeAccountId = if (selectedAccIndex >= 0 && selectedAccIndex < accounts.size) {
                  accounts[selectedAccIndex].id
                } else null

                val filteredCards = if (activeAccountId != null) {
                  cards.filter { it.linkedAccountId == activeAccountId }
                } else cards

                filteredCards.forEach { card ->
                  DropdownMenuItem(
                    text = { Text("Card: ${card.cardNumberMasked} (${card.cardHolder})", color = TextPrimary) },
                    onClick = {
                      selectedCardIndex = cards.indexOf(card)
                      cardDropdownExpanded = false
                    }
                  )
                }
              }
            }
          }

          // Accept or cancel buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(onClick = { showAddDialog = false }) {
              Text("Cancel", color = TextMuted)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val amount = amountText.toDoubleOrNull()
                val day = calendarDayText.toIntOrNull()
                val accSelected = selectedAccIndex >= 0 && selectedAccIndex < accounts.size

                if (payeeText.isNotBlank() && amount != null && day != null && day in 1..31 && accSelected) {
                  val accountId = accounts[selectedAccIndex].id
                  val cardId = if (selectedCardIndex >= 0 && selectedCardIndex < cards.size) {
                    cards[selectedCardIndex].id
                  } else null

                  viewModel.addPayment(
                    type = feeType,
                    payee = payeeText,
                    amount = amount,
                    day = day,
                    accountId = accountId,
                    cardId = cardId,
                    recipientNum = if (feeType == "Rent") recAccountText.ifBlank { null } else null,
                    recipientBranchNo = if (feeType == "Rent") recBranchNumberText.ifBlank { null } else null,
                    recipientBranchNm = if (feeType == "Rent") recBranchNameText.ifBlank { null } else null,
                    recipientNm = if (feeType == "Rent") recNameText.ifBlank { null } else null
                  )
                  showAddDialog = false
                } else {
                  Toast.makeText(context, "Please complete fields. Day must be 1 - 31.", Toast.LENGTH_LONG).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
              modifier = Modifier.testTag("dialog_save_autopay_btn")
            ) {
              Text("Save Rule", color = NavyBackground, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

// ---------------- ACCOUNTS & CARTS SCREEN ----------------
@Composable
fun AccountsScreen(
  viewModel: CompanionViewModel,
  accounts: List<BSBAccount>,
  cards: List<BSBCard>
) {
  var showAccountDialog by remember { mutableStateOf(false) }
  var showCardDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("BSB Card & Wallet Vault", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Link savings accounts & authorization keys", color = TextMuted, fontSize = 12.sp)
      }
    }

    // Action Grid Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { showAccountDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = NavySurface),
        modifier = Modifier
          .weight(1f)
          .border(1.dp, CoralOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
          .testTag("add_account_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Account", tint = CoralOrange)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Link Account", color = TextPrimary, fontSize = 12.sp)
      }

      Button(
        onClick = { showCardDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = NavySurface),
        modifier = Modifier
          .weight(1f)
          .border(1.dp, GoldOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
          .testTag("add_card_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Card", tint = GoldOrange)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Link Card", color = TextPrimary, fontSize = 12.sp)
      }
    }

    // BSB BANK ACCOUNTS SECTION
    Text(
      text = "Linked Botswana Savings Bank Accounts",
      color = TextPrimary,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp
    )

    if (accounts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .border(1.dp, NavyDistant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text("No BSB accounts connected. Click Link Account.", color = TextMuted, fontSize = 12.sp)
      }
    } else {
      accounts.forEach { acc ->
        Card(
          colors = CardDefaults.cardColors(containerColor = NavySurface),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(acc.accountName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("Account Number: ${acc.accountNumber}", color = TextMuted, fontSize = 12.sp)
            }
            Text(
              text = "P ${String.format("%,.2f", acc.balance)}",
              color = CoralOrange,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )
          }
        }
      }
    }

    // CARDS LIST SECTION (Physical Card Display!)
    Text(
      text = "Registered Authorization Cards (${cards.size})",
      color = TextPrimary,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp
    )

    if (cards.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .border(1.dp, NavyDistant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text("No connected transaction cards registered.", color = TextMuted, fontSize = 12.sp)
      }
    } else {
      // Horizontal swipe or stacked physical cards
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        cards.forEach { card ->
          val associatedAccount = accounts.find { it.id == card.linkedAccountId }

          // Physical Bank Card Layout
          BSBThemedCard(
            cardType = card.cardType,
            cardNumberMasked = card.cardNumberMasked,
            cardHolder = card.cardHolder,
            cardExpiry = card.cardExpiry,
            associatedAccountName = associatedAccount?.accountName
          )
        }
      }
    }
  }

  // --- POPUP DIALOGS ---

  // 1. Account Link dialog
  if (showAccountDialog) {
    var nameText by remember { mutableStateOf("") }
    var numberText by remember { mutableStateOf("") }
    var initBalanceText by remember { mutableStateOf("2500") }

    Dialog(onDismissRequest = { showAccountDialog = false }) {
      Surface(
        color = NavySurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text("Connect BSB Account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

          OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            label = { Text("Account Name (e.g. My Savings)") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("account_name_input")
          )

          OutlinedTextField(
            value = numberText,
            onValueChange = { numberText = it },
            label = { Text("BSB Account Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("account_number_input")
          )

          OutlinedTextField(
            value = initBalanceText,
            onValueChange = { initBalanceText = it },
            label = { Text("Initial Balance (Current BWP)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("account_balance_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showAccountDialog = false }) { Text("Cancel", color = TextMuted) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val balance = initBalanceText.toDoubleOrNull() ?: 0.0
                if (nameText.isNotBlank() && numberText.isNotBlank()) {
                  viewModel.addAccount(nameText, numberText, balance)
                  showAccountDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
              modifier = Modifier.testTag("dialog_save_acc_btn")
            ) {
              Text("Link Account", color = NavyBackground, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 2. Card Link Dialog
  if (showCardDialog) {
    var holderText by remember { mutableStateOf("") }
    var numberText by remember { mutableStateOf("") }
    var expiryText by remember { mutableStateOf("09/29") }
    var selectedAccIndex by remember { mutableStateOf(-1) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedCardType by remember { mutableStateOf("Student Card") }

    Dialog(onDismissRequest = { showCardDialog = false }) {
      Surface(
        color = NavySurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text("Connect BSB Debit Card", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

          OutlinedTextField(
            value = holderText,
            onValueChange = { holderText = it },
            label = { Text("Cardholder Full Name") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_holder_input")
          )

          OutlinedTextField(
            value = numberText,
            onValueChange = { numberText = it },
            label = { Text("16-Digit Card Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_number_input")
          )

          OutlinedTextField(
            value = expiryText,
            onValueChange = { expiryText = it },
            label = { Text("Expiry Date (MM/YY)") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_expiry_input")
          )

          // Card Tier Selector
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Select Card Type", color = TextMuted, fontSize = 11.sp)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val cardTiers = listOf("Student Card", "Youth Card")
              cardTiers.forEach { tier ->
                val isSelected = selectedCardType == tier
                val btnColor = if (isSelected) CoralOrange else NavyPrimary
                val txtColor = if (isSelected) NavyBackground else TextPrimary
                
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(btnColor)
                    .clickable { selectedCardType = tier }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = tier.replace(" Debit Card", "").replace(" Black", ""),
                    color = txtColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }

          // Link to Account drop down
          Column {
            Text("Linked Funding Wallet Account", color = TextMuted, fontSize = 11.sp)
            Box {
              Button(
                onClick = { dropdownExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("card_acc_select_trigger")
              ) {
                Text(
                  text = if (selectedAccIndex >= 0 && selectedAccIndex < accounts.size) {
                    accounts[selectedAccIndex].accountName
                  } else "Select Account",
                  color = TextPrimary
                )
              }
              DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(NavyPrimary)
              ) {
                accounts.forEachIndexed { index, bsbAccount ->
                  DropdownMenuItem(
                    text = { Text(bsbAccount.accountName, color = TextPrimary) },
                    onClick = {
                      selectedAccIndex = index
                      dropdownExpanded = false
                    }
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showCardDialog = false }) { Text("Cancel", color = TextMuted) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val accIndexValid = selectedAccIndex >= 0 && selectedAccIndex < accounts.size
                if (holderText.isNotBlank() && numberText.isNotBlank() && accIndexValid) {
                  // Mask the middle digits for security
                  val lastDigits = numberText.takeLast(4)
                  val maskedCardNo = "**** **** **** $lastDigits"
                  
                  val linkedAccId = accounts[selectedAccIndex].id
                  viewModel.addCard(holderText, maskedCardNo, expiryText, linkedAccId, selectedCardType)
                  showCardDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
              modifier = Modifier.testTag("dialog_save_card_btn")
            ) {
              Text("Link Card", color = NavyBackground, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

// ---------------- TRACKED EXPENSES SCREEN ----------------
@Composable
fun ExpensesScreen(
  viewModel: CompanionViewModel,
  expenses: List<ExpenseItem>
) {
  var showAddExpenseDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Expense Log", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Track monthly outlays comprehensively", color = TextMuted, fontSize = 12.sp)
      }

      FilledIconButton(
        onClick = { showAddExpenseDialog = true },
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CoralOrange),
        modifier = Modifier
          .size(48.dp)
          .testTag("add_manual_expense_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White, modifier = Modifier.size(24.dp))
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Expenses List
    if (expenses.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text("No recorded transactions for this cycle.", color = TextMuted, fontSize = 12.sp)
      }
    } else {
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().weight(1f)
      ) {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          itemsIndexed(expenses) { index, exp ->
            TransactionItemRow(
              exp = exp,
              onDeleteClick = { viewModel.deleteExpense(exp) }
            )
            if (index < expenses.size - 1) {
              HorizontalDivider(color = if (isDarkThemeGlobal) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f), thickness = 1.dp)
            }
          }
        }
      }
    }
  }

  // Dialog for inserting manual expense
  if (showAddExpenseDialog) {
    var titleText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Groceries") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val expenseCategories = listOf("Groceries", "Rent & Lodging", "Combi & Taxi Transport", "Student Data/Wifi", "Smart Savings")

    Dialog(onDismissRequest = { showAddExpenseDialog = false }) {
      Surface(
        color = NavySurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text("Add Spending Entry", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

          OutlinedTextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Expense Description / Shop Name") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("expense_desc_input")
          )

          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount Cash (BWP / Pula)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = NavyPrimary,
              unfocusedContainerColor = NavyPrimary,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedLabelColor = CoralOrange,
              unfocusedLabelColor = TextMuted
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("expense_cash_input")
          )

          Column {
            Text("Select Category", color = TextMuted, fontSize = 11.sp)
            Box {
              Button(
                onClick = { categoryDropdownExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("ex_category_trigger")
              ) {
                Text(categoryText, color = TextPrimary)
              }
              DropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false },
                modifier = Modifier.background(NavyPrimary)
              ) {
                expenseCategories.forEach { cat ->
                  DropdownMenuItem(
                    text = { Text(cat, color = TextPrimary) },
                    onClick = {
                      categoryText = cat
                      categoryDropdownExpanded = false
                    }
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancel", color = TextMuted) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val amt = amountText.toDoubleOrNull()
                if (titleText.isNotBlank() && amt != null) {
                  viewModel.addManualExpense(titleText, amt, categoryText)
                  showAddExpenseDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
              modifier = Modifier.testTag("dialog_save_ex_btn")
            ) {
              Text("Log Expense", color = NavyBackground, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

// ---------------- NOTIFICATIONS SCREEN ----------------
@Composable
fun NotificationsScreen(
  viewModel: CompanionViewModel,
  notifications: List<AppNotification>
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Transaction History Logs", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Recent deposits, payments, and settlements", color = TextMuted, fontSize = 12.sp)
      }

      TextButton(
        onClick = { viewModel.clearAllNotifications() },
        modifier = Modifier.testTag("clear_notif_button")
      ) {
        Text("Clear All", color = CoralOrange)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // List
    if (notifications.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text("No notifications or transaction receipts present.", color = TextMuted, fontSize = 12.sp)
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(notifications) { notif ->
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (notif.isRead) NavySurface.copy(alpha = 0.7f) else NavySurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.markNotificationAsRead(notif.id) }
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (!notif.isRead) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CoralOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                  }
                  Text(
                    text = notif.title,
                    color = if (notif.isRead) TextPrimary.copy(alpha = 0.8f) else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                Text(
                  text = java.text.SimpleDateFormat("d MMM, hh:mm a")
                    .format(java.util.Date(notif.timestamp)),
                  color = TextMuted,
                  fontSize = 10.sp
                )
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = notif.message,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun TransactionItemRow(
  exp: ExpenseItem,
  onDeleteClick: (() -> Unit)? = null
) {
  val catColor: Color
  val catIcon: ImageVector
  val catLower = exp.category.lowercase()
  
  when {
    catLower.contains("data") || catLower.contains("wifi") || catLower.contains("net") || catLower.contains("mobile") || catLower.contains("phone") || catLower.contains("internet") -> {
      catIcon = Icons.Default.Phone
      catColor = Color(0xFF2196F3) // Soft Blue
    }
    catLower.contains("study") || catLower.contains("book") || catLower.contains("school") || catLower.contains("education") -> {
      catIcon = Icons.Default.Star
      catColor = Color(0xFF9C27B0) // Soft Purple
    }
    catLower.contains("transport") || catLower.contains("combi") || catLower.contains("taxi") || catLower.contains("ride") -> {
      catIcon = Icons.Default.Refresh
      catColor = Color(0xFFFF9800) // Soft Orange/Amber
    }
    catLower.contains("groceries") || catLower.contains("food") || catLower.contains("cafeteria") || catLower.contains("meal") || catLower.contains("dining") || catLower.contains("rest") -> {
      catIcon = Icons.Default.ShoppingCart
      catColor = Color(0xFF4CAF50) // Soft Green
    }
    catLower.contains("rent") || catLower.contains("lodging") || catLower.contains("room") || catLower.contains("hostel") || catLower.contains("house") -> {
      catIcon = Icons.Default.Home
      catColor = Color(0xFFE91E63) // Soft Pink/Red
    }
    catLower.contains("savings") || catLower.contains("invest") || catLower.contains("pot") -> {
      catIcon = Icons.Default.Lock
      catColor = Color(0xFF009688) // Soft Teal
    }
    else -> {
      catIcon = Icons.Default.List
      catColor = Color(0xFF9E9E9E) // Soft Gray
    }
  }

  val isAllowanceDeposit = exp.title.lowercase().contains("allowance") || exp.title.lowercase().contains("deposit") || exp.amount < 0 || exp.category.lowercase().contains("deposit")

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left: Circular category icon badge
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(catColor.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = catIcon,
        contentDescription = exp.category,
        tint = catColor,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Middle: Title & Date & mini-tag
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = exp.title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = java.text.SimpleDateFormat("d MMM yyyy, hh:mm a")
            .format(java.util.Date(exp.timestamp)),
          color = TextMuted,
          fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = exp.category.uppercase(),
            color = catColor.copy(alpha = 0.9f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Right: Amount and Delete if available
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {
      Column(horizontalAlignment = Alignment.End) {
        if (isAllowanceDeposit) {
          Text(
            text = "+BWP ${String.format("%,.2f", Math.abs(exp.amount))}",
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
          )
          Text(
            text = "Credit",
            color = Color(0xFF4CAF50).copy(alpha = 0.7f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
          )
        } else {
          Text(
            text = "-BWP ${String.format("%,.2f", exp.amount)}",
            color = if (isDarkThemeGlobal) Color.White else Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
          )
          Text(
            text = "Debit",
            color = TextMuted,
            fontSize = 8.sp
          )
        }
      }
      
      if (onDeleteClick != null) {
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = onDeleteClick,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Red.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun BSBThemedCard(
    cardType: String,
    cardNumberMasked: String,
    cardHolder: String,
    cardExpiry: String,
    associatedAccountName: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(NavySurface)) {
            // Theme-specific background draw block
            when (cardType) {
                "Youth Debit Card", "Youth Card" -> {
                    // Deep navy background with diagonal/vertical vibrant traditional patterns
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF071221)))
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val bandWidth = 90.dp.toPx()
                        val startX = w - bandWidth
                        
                        // Draw vertical strip background
                        drawRect(
                            color = Color(0xFF0E1A2F),
                            topLeft = Offset(startX, 0f),
                            size = Size(bandWidth, h)
                        )
                        
                        // Interlocking tribal geometric designs in vibrant green, yellow, pink, and turquoise
                        val pathYellow1 = Path().apply {
                            moveTo(startX, 0f)
                            lineTo(startX + bandWidth * 0.5f, h * 0.25f)
                            lineTo(startX, h * 0.5f)
                            close()
                        }
                        drawPath(pathYellow1, Color(0xFFFFD600)) // vibrant yellow
                        
                        val pathPink1 = Path().apply {
                            moveTo(startX + bandWidth, 0f)
                            lineTo(startX + bandWidth * 0.5f, h * 0.25f)
                            lineTo(startX + bandWidth, h * 0.5f)
                            close()
                        }
                        drawPath(pathPink1, Color(0xFFFF2D55)) // vibrant pink
                        
                        val pathTurquoise1 = Path().apply {
                            moveTo(startX, h * 0.5f)
                            lineTo(startX + bandWidth * 0.5f, h * 0.75f)
                            lineTo(startX, h)
                            close()
                        }
                        drawPath(pathTurquoise1, Color(0xFF00F5D4)) // vibrant turquoise
                        
                        val pathGreen1 = Path().apply {
                            moveTo(startX + bandWidth, h * 0.5f)
                            lineTo(startX + bandWidth * 0.5f, h * 0.75f)
                            lineTo(startX + bandWidth, h)
                            close()
                        }
                        drawPath(pathGreen1, Color(0xFF00C853)) // vibrant green
                        
                        // Central diamond overlap
                        val cDiamond = Path().apply {
                            moveTo(startX + bandWidth * 0.5f, h * 0.25f)
                            lineTo(startX + bandWidth, h * 0.5f)
                            lineTo(startX + bandWidth * 0.5f, h * 0.75f)
                            lineTo(startX, h * 0.5f)
                            close()
                        }
                        drawPath(cDiamond, Color(0xFF1B2E4C).copy(alpha = 0.85f))
                        
                        // Draw mini accent circles/dots inside the pattern
                        drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(startX + bandWidth * 0.5f, h * 0.5f))
                    }
                }
                "Platinum Black Card" -> {
                    // Luxurious Matte Dark Obsidian Background with silver mandala patterns
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF222428), Color(0xFF0E0F11))
                                )
                            )
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val centerX = h * 0.32f
                        val centerY = h * 0.5f
                        val baseRadius = h * 0.38f
                        val silverColor = Color(0xFF94A3B8).copy(alpha = 0.25f)
                        val silverBright = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        
                        // Concentric silver-mandala lace circles
                        drawCircle(color = silverColor, radius = baseRadius, center = Offset(centerX, centerY), style = Stroke(width = 1.dp.toPx()))
                        drawCircle(color = silverColor.copy(alpha = 0.15f), radius = baseRadius * 1.3f, center = Offset(centerX, centerY), style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)))
                        drawCircle(color = silverColor, radius = baseRadius * 0.65f, center = Offset(centerX, centerY), style = Stroke(width = 1.3f.dp.toPx()))
                        drawCircle(color = silverColor.copy(alpha = 0.2f), radius = baseRadius * 0.35f, center = Offset(centerX, centerY), style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)))
                        
                        // Radiating spokes
                        for (angle in 0 until 360 step 30) {
                            val rad = Math.toRadians(angle.toDouble())
                            val endX = centerX + (baseRadius * 1.15f * Math.cos(rad)).toFloat()
                            val endY = centerY + (baseRadius * 1.15f * Math.sin(rad)).toFloat()
                            drawLine(
                                color = silverColor.copy(alpha = 0.2f),
                                start = Offset(centerX, centerY),
                                end = Offset(endX, endY),
                                strokeWidth = 0.8.dp.toPx()
                            )
                        }
                        
                        // Gleaming diamond in the very center
                        val diamondSize = 6.dp.toPx()
                        val diamondPath = Path().apply {
                            moveTo(centerX, centerY - diamondSize)
                            lineTo(centerX + diamondSize * 0.8f, centerY)
                            lineTo(centerX, centerY + diamondSize)
                            lineTo(centerX - diamondSize * 0.8f, centerY)
                            close()
                        }
                        drawPath(diamondPath, Color.White)
                        
                        // Outer accent dots
                        for (angle in 0 until 360 step 45) {
                            val rad = Math.toRadians(angle.toDouble())
                            val dotX = centerX + (baseRadius * 0.5f * Math.cos(rad)).toFloat()
                            val dotY = centerY + (baseRadius * 0.5f * Math.sin(rad)).toFloat()
                            drawCircle(color = silverBright, radius = 1.5.dp.toPx(), center = Offset(dotX, dotY))
                        }
                    }
                }
                else -> {
                    // Visa Classic Debit Card: Top half Deep Navy Blue, Bottom half Vibrant Warm Orange graceful curve
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1E36)))
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Beautiful quadratic bezier curve splitting the card half way
                        val path = Path().apply {
                            moveTo(0f, h * 0.55f)
                            quadraticTo(
                                w * 0.45f, h * 0.42f,
                                w, h * 0.62f
                            )
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF15A24), Color(0xFFFF8C00))
                            )
                        )
                        
                        // Thin gold lining
                        val linePath = Path().apply {
                            moveTo(0f, h * 0.55f)
                            quadraticTo(
                                w * 0.45f, h * 0.42f,
                                w, h * 0.62f
                            )
                        }
                        drawPath(
                            path = linePath,
                            color = Color(0xFFFFD600).copy(alpha = 0.4f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
            
            // Text values, chip and branded texts on top layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = cardType.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = when {
                              cardType.contains("Youth", ignoreCase = true) -> "Youth Co-Savings"
                              cardType.contains("Student", ignoreCase = true) -> "Student Co-Savings"
                              else -> "Companion Co-Savings"
                            },
                            color = Color(0xFFFF8C00),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Styled gold microchip
                    Box(
                        modifier = Modifier
                            .size(34.dp, 24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE5A93B))
                            .border(0.5.dp, Color(0xFF8B5E00), RoundedCornerShape(4.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            drawLine(Color(0xFF8B5E00).copy(alpha = 0.7f), Offset(w * 0.32f, 0f), Offset(w * 0.32f, h), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color(0xFF8B5E00).copy(alpha = 0.7f), Offset(w * 0.68f, 0f), Offset(w * 0.68f, h), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color(0xFF8B5E00).copy(alpha = 0.7f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color(0xFF8B5E00).copy(alpha = 0.7f), Offset(w * 0.32f, h * 0.25f), Offset(w * 0.68f, h * 0.25f), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color(0xFF8B5E00).copy(alpha = 0.7f), Offset(w * 0.32f, h * 0.75f), Offset(w * 0.68f, h * 0.75f), strokeWidth = 0.5.dp.toPx())
                        }
                    }
                }
                
                Text(
                    text = cardNumberMasked,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CARDHOLDER",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = cardHolder.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXPIRES",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = cardExpiry,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        if (cardType == "Platinum Black Card") {
                            Text(
                                text = "B S B",
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "VISA",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                        }
                        
                        associatedAccountName?.let {
                            Text(
                                text = it.replace("Account", "").replace("BSB", "").trim(),
                                color = if (cardType == "Platinum Black Card") Color(0xFF94A3B8) else Color(0xFFFFD600),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CompanionViewModel,
    notifications: List<AppNotification>
) {
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val simulatedDay by viewModel.simulatedDay.collectAsStateWithLifecycle()
    val freeDataMode by viewModel.freeDataMode.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    
    // Draft states for editable fields, pre-filled from loggedInUser
    var isEditMode by remember { mutableStateOf(false) }
    var draftFullName by remember(loggedInUser) { mutableStateOf(loggedInUser?.fullName ?: "Masego L. Kaelo") }
    var draftCellphone by remember(loggedInUser) { mutableStateOf(loggedInUser?.cellphone ?: "71649231") }
    var draftDailyLimit by remember(loggedInUser) { mutableStateOf((loggedInUser?.dailyCardLimit ?: 5000.0).toFloat()) }
    var draftStatementFreq by remember(loggedInUser) { mutableStateOf(loggedInUser?.statementFrequency ?: "Monthly") }
    
    var draftFoodLimit by remember(loggedInUser) { mutableStateOf((loggedInUser?.foodMaxLimit ?: 1500.0).toInt().toString()) }
    var draftRentLimit by remember(loggedInUser) { mutableStateOf((loggedInUser?.rentMaxLimit ?: 3000.0).toInt().toString()) }
    var draftTransportLimit by remember(loggedInUser) { mutableStateOf((loggedInUser?.transportMaxLimit ?: 1000.0).toInt().toString()) }
    var draftSavingsLimit by remember(loggedInUser) { mutableStateOf((loggedInUser?.savingsMaxLimit ?: 2000.0).toInt().toString()) }
    
    // Toggles
    val smsAlerts = loggedInUser?.smsAlertsEnabled ?: true
    val isCardFrozen = loggedInUser?.isCardFrozen ?: false
    val contactlessEnabled = loggedInUser?.contactlessEnabled ?: true
    val biometricsEnabled = loggedInUser?.biometricsEnabled ?: true
    val isDark = loggedInUser?.isDarkMode ?: true
    
    var showFreqDropdown by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Profile Header Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                Brush.linearGradient(listOf(CoralOrange, GoldOrange)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = draftFullName.firstOrNull()?.toString()?.uppercase() ?: "U",
                            color = NavyBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = draftFullName,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Primary Cell: +267 $draftCellphone",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Email: ${loggedInUser?.email ?: "masego@gmail.com"}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isEditMode = !isEditMode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditMode) NavyBackground else NavyPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = if (isEditMode) CoralOrange else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) "Done" else "Edit Info",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.logOut()
                                Toast.makeText(context, "Session Terminated Securely.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = NavyBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Log Out",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NavyBackground
                            )
                        }
                    }
                }
            }
        }
        
        if (isEditMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Modify Profile & Student Bounds",
                            color = CoralOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        
                        OutlinedTextField(
                            value = draftFullName,
                            onValueChange = { draftFullName = it },
                            label = { Text("Display / Legal Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralOrange,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        
                        OutlinedTextField(
                            value = draftCellphone,
                            onValueChange = { draftCellphone = it },
                            label = { Text("Botswana Cellphone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralOrange,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Text(
                            text = "Custom Allocator Maximum Bounds (BWP)",
                            color = GoldOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftRentLimit,
                                onValueChange = { draftRentLimit = it.filter { c -> c.isDigit() } },
                                label = { Text("Rent Max Limit") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralOrange,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = draftFoodLimit,
                                onValueChange = { draftFoodLimit = it.filter { c -> c.isDigit() } },
                                label = { Text("Groceries Max Limit") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralOrange,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftTransportLimit,
                                onValueChange = { draftTransportLimit = it.filter { c -> c.isDigit() } },
                                label = { Text("Transport Max") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralOrange,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = draftSavingsLimit,
                                onValueChange = { draftSavingsLimit = it.filter { c -> c.isDigit() } },
                                label = { Text("Savings Max") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralOrange,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.updateUserSettings(
                                    dailyCardLimit = draftDailyLimit.toDouble(),
                                    smsAlertsEnabled = smsAlerts,
                                    isCardFrozen = isCardFrozen,
                                    contactlessEnabled = contactlessEnabled,
                                    statementFrequency = draftStatementFreq,
                                    biometricsEnabled = biometricsEnabled,
                                    fullName = draftFullName,
                                    cellphone = draftCellphone,
                                    isDarkMode = isDark,
                                    foodMaxLimit = draftFoodLimit.toDoubleOrNull() ?: 1500.0,
                                    rentMaxLimit = draftRentLimit.toDoubleOrNull() ?: 3000.0,
                                    transportMaxLimit = draftTransportLimit.toDoubleOrNull() ?: 1000.0,
                                    savingsMaxLimit = draftSavingsLimit.toDoubleOrNull() ?: 2000.0
                                ) {
                                    isEditMode = false
                                    Toast.makeText(context, "Legal details & student bounds updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE NEW DETAILS", color = NavyBackground, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (!isEditMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rent Bound Limit", color = TextPrimary, fontSize = 12.sp)
                            Text("P ${String.format("%,.0f", loggedInUser?.rentMaxLimit ?: 3000.0)}", color = GoldOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Groceries Limit", color = TextPrimary, fontSize = 12.sp)
                            Text("P ${String.format("%,.0f", loggedInUser?.foodMaxLimit ?: 1500.0)}", color = GoldOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Combi Transport Limit", color = TextPrimary, fontSize = 12.sp)
                            Text("P ${String.format("%,.0f", loggedInUser?.transportMaxLimit ?: 1000.0)}", color = GoldOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Emergency Savings Limit", color = TextPrimary, fontSize = 12.sp)
                            Text("P ${String.format("%,.0f", loggedInUser?.savingsMaxLimit ?: 2000.0)}", color = GoldOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        

                    }
                }
            }
        }
        
        // Settings Sections (Simplified)
        

        item {
            // Visual Theme Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDark) "Dark Mode" else "Light Mode",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { newVal ->
                                viewModel.updateUserSettings(
                                    dailyCardLimit = draftDailyLimit.toDouble(),
                                    smsAlertsEnabled = smsAlerts,
                                    isCardFrozen = isCardFrozen,
                                    contactlessEnabled = contactlessEnabled,
                                    statementFrequency = draftStatementFreq,
                                    biometricsEnabled = biometricsEnabled,
                                    fullName = draftFullName,
                                    cellphone = draftCellphone,
                                    isDarkMode = newVal,
                                    foodMaxLimit = loggedInUser?.foodMaxLimit ?: 1500.0,
                                    rentMaxLimit = loggedInUser?.rentMaxLimit ?: 3000.0,
                                    transportMaxLimit = loggedInUser?.transportMaxLimit ?: 1000.0,
                                    savingsMaxLimit = loggedInUser?.savingsMaxLimit ?: 2000.0
                                ) {}
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NavyBackground,
                                checkedTrackColor = CoralOrange,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NavyPrimary
                            )
                        )
                    }
                }
            }
        }
        

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Botswana Savings Bank (BSB) Savings Companion v4.2S • Licensed App Client",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CalendarScreen(
  viewModel: CompanionViewModel,
  payments: List<ScheduledPayment>,
  accounts: List<BSBAccount>,
  simulatedDay: Int
) {
  val months = listOf(
    "January", "February", "March", "April", "May", "June", 
    "July", "August", "September", "October", "November", "December"
  )
  var selectedMonthIndex by remember { mutableStateOf(5) } // Default to June (Active simulation month)
  var selectedCalendarDay by remember { mutableStateOf(simulatedDay) }
  
  val monthsListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(simulatedDay) {
    selectedCalendarDay = simulatedDay
  }

  LaunchedEffect(selectedMonthIndex) {
    coroutineScope.launch {
      monthsListState.animateScrollToItem(selectedMonthIndex)
    }
  }

  // Get days in the selected month for 2026
  val daysInMonth = when (selectedMonthIndex) {
    1 -> 28 // February
    3, 5, 8, 10 -> 30 // April, June, September, November
    else -> 31 // Jan, Mar, May, Jul, Aug, Oct, Dec
  }

  // Exact 2026 weekday starting index (0 = Mon, 1 = Tue, 2 = Wed, 3 = Thu, 4 = Fri, 5 = Sat, 6 = Sun)
  val monthStartDayOfWeek = when (selectedMonthIndex) {
    0 -> 3 // January starts on Thursday
    1 -> 6 // February starts on Sunday
    2 -> 6 // March starts on Sunday
    3 -> 2 // April starts on Wednesday
    4 -> 4 // May starts on Friday
    5 -> 0 // June starts on Monday
    6 -> 2 // July starts on Wednesday
    7 -> 5 // August starts on Saturday
    8 -> 1 // September starts on Tuesday
    9 -> 3 // October starts on Thursday
    10 -> 6 // November starts on Sunday
    11 -> 1 // December starts on Tuesday
    else -> 0
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Expanded Multi-Month Calendar Core Card
    Card(
      colors = CardDefaults.cardColors(containerColor = NavySurface),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // A. Header Month Navigator with Left/Right Arrows
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              selectedMonthIndex = if (selectedMonthIndex == 0) 11 else selectedMonthIndex - 1
              if (selectedCalendarDay > 28) selectedCalendarDay = 28
            }
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Previous Month",
              tint = CoralOrange
            )
          }

          Text(
            text = "${months[selectedMonthIndex]} 2026",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
          )

          IconButton(
            onClick = {
              selectedMonthIndex = if (selectedMonthIndex == 11) 0 else selectedMonthIndex + 1
              if (selectedCalendarDay > 28) selectedCalendarDay = 28
            }
          ) {
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "Next Month",
              tint = CoralOrange
            )
          }
        }

        // B. Quick Month Horizontal Selection Pills with LazyRow to auto-scroll
        LazyRow(
          state = monthsListState,
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(months) { idx, mName ->
            val isCurrentSel = idx == selectedMonthIndex
            Surface(
              onClick = { 
                selectedMonthIndex = idx 
                if (selectedCalendarDay > 28) selectedCalendarDay = 28
              },
              color = if (isCurrentSel) CoralOrange else NavyPrimary,
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = mName.take(3),
                  color = if (isCurrentSel) NavyBackground else TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // C. Mon - Sun Column Headers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLetter ->
            Box(
              modifier = Modifier.size(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = dayLetter,
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
              )
            }
          }
        }

        // D. Flexible Month Cells generator with accurate offset
        val totalDays = daysInMonth
        val startOffset = monthStartDayOfWeek
        val totalCells = totalDays + startOffset
        val dayCols = 7
        val dayRows = (totalCells + dayCols - 1) / dayCols
        
        for (r in 0 until dayRows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            for (c in 0 until dayCols) {
              val cellIndex = r * 7 + c
              val cellDay = cellIndex - startOffset + 1
              if (cellIndex >= startOffset && cellDay <= totalDays) {
                val isToday = (selectedMonthIndex == 5) && (cellDay == simulatedDay)
                val isSelected = cellDay == selectedCalendarDay
                val hasPayment = payments.any { it.paymentDay == cellDay && it.isActive }

                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      when {
                        isToday -> CoralOrange
                        isSelected -> CoralOrange.copy(alpha = 0.25f)
                        else -> NavyPrimary.copy(alpha = 0.4f)
                      }
                    )
                    .border(
                      width = when {
                        isToday -> 0.dp
                        isSelected -> 2.dp
                        else -> 1.dp
                      },
                      color = when {
                        isToday -> Color.Transparent
                        isSelected -> CoralOrange
                        else -> if (isDarkThemeGlobal) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                      },
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedCalendarDay = cellDay },
                  contentAlignment = Alignment.Center
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = "$cellDay",
                      color = when {
                        isToday -> NavyBackground
                        isSelected -> CoralOrange
                        else -> TextPrimary
                      },
                      fontWeight = if (isToday || isSelected) FontWeight.Black else FontWeight.Bold,
                      fontSize = 12.sp
                    )

                    // Little dynamic payment action indicator dots
                    if (hasPayment) {
                      Box(
                        modifier = Modifier
                          .padding(top = 1.dp)
                          .size(4.dp)
                          .clip(CircleShape)
                          .background(if (isToday) NavyBackground else GoldOrange)
                      )
                    }
                  }
                }
              } else {
                // Pad with empty cell block
                Box(modifier = Modifier.size(38.dp))
              }
            }
          }
        }
      }
    }

    // 3. Detailed payment info for selected day
    val paymentsOnDay = payments.filter { it.paymentDay == selectedCalendarDay }

    Card(
      colors = CardDefaults.cardColors(containerColor = NavySurface),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DUE DETAILS FOR ${months[selectedMonthIndex].uppercase()} $selectedCalendarDay",
            color = CoralOrange,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
          )
          
          if (selectedMonthIndex == 5 && selectedCalendarDay == simulatedDay) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CoralOrange.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "TODAY",
                color = CoralOrange,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
              )
            }
          }
        }

        if (paymentsOnDay.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "No dues",
                tint = Color.Green.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "No payments scheduled for this calendar index!",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            paymentsOnDay.forEach { payment ->
              val payAccount = accounts.find { it.id == payment.selectedAccountId }
              
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(NavyPrimary.copy(alpha = 0.4f))
                  .border(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    // Category Bullet
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CoralOrange.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      val icon = when {
                        payment.paymentType.contains("Wifi", ignoreCase = true) -> Icons.Default.Refresh
                        payment.paymentType.contains("Mobile", ignoreCase = true) || payment.paymentType.contains("Data", ignoreCase = true) -> Icons.Default.PlayArrow
                        payment.paymentType.contains("Rent", ignoreCase = true) -> Icons.Default.Home
                        else -> Icons.Default.Star
                      }
                      Icon(
                        imageVector = icon,
                        contentDescription = payment.paymentType,
                        tint = CoralOrange,
                        modifier = Modifier.size(16.dp)
                      )
                    }

                    Column {
                      Text(
                        text = payment.payeeName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                      Text(
                        text = "${payment.paymentType} • From ${payAccount?.accountName ?: "Wallet"}",
                        color = TextMuted,
                        fontSize = 10.sp
                      )
                    }
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "BWP ${String.format("%.2f", payment.amount)}",
                      color = GoldOrange,
                      fontWeight = FontWeight.Black,
                      fontSize = 12.sp
                    )
                    Box(
                      modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (payment.isActive) Color(0x1110B981) else Color(0x22EF4444))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = if (payment.isActive) "Auto-Active" else "Deactivated",
                        color = if (payment.isActive) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                      )
                    }
                  }
                }

                // Render expanded custom recipient bank accounts details in the calendar dues card if configured
                if (!payment.recipientAccount.isNullOrBlank()) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(6.dp))
                      .background(NavyBackground.copy(alpha = 0.5f))
                      .padding(8.dp)
                  ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                      Text(
                        text = "RECIPIENT BANK SETTLEMENT",
                        color = CoralOrange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                      )
                      Text(
                        text = "Name: ${payment.recipientName ?: payment.payeeName}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "Account: ${payment.recipientAccount}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                      )
                      Text(
                        text = "Bank Branch: ${payment.recipientBranchNumber ?: "N/A"} - ${payment.recipientBranchName ?: "N/A"}",
                        color = TextMuted,
                        fontSize = 10.sp
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 4. Monthly Transaction History (Below Calendar & Dues Details)
    val expenses by viewModel.expenses.collectAsStateWithLifecycle(initialValue = emptyList())

    Card(
      colors = CardDefaults.cardColors(containerColor = NavySurface),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "MONTHLY TRANSACTION HISTORY",
              color = CoralOrange,
              fontWeight = FontWeight.Black,
              fontSize = 11.sp,
              letterSpacing = 0.5.sp
            )
          }

          if (expenses.isNotEmpty()) {
            Button(
              onClick = { viewModel.clearAllExpenses() },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text("Renew Log", color = CoralOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        HorizontalDivider(color = NavyPrimary, thickness = 1.dp)

        if (expenses.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No recorded transactions for this cycle.",
              color = TextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
              expenses.forEachIndexed { index, exp ->
                TransactionItemRow(
                  exp = exp,
                  onDeleteClick = null // No edit/delete on the overview dashboard
                )
                if (index < expenses.size - 1) {
                  HorizontalDivider(color = if (isDarkThemeGlobal) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f), thickness = 1.dp)
                }
              }
            }
          }
        }
      }
    }
  }
}

