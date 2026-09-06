package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.Student360DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CompanionViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationCrashTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: CompanionViewModel

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repository = Repository(database)
    viewModel = CompanionViewModel(repository)
  }

  @Test
  fun testMainAppScreenTabs() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppScreen(viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }
}
