package com.prime.gallery.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import com.prime.gallery.BuildConfig
import com.prime.gallery.Gallery
import com.prime.gallery.LocalNavController
import com.prime.gallery.LocalsProvider
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.billing.Banner
import com.prime.gallery.core.billing.Placement
import com.prime.gallery.core.billing.Product
import com.prime.gallery.core.billing.purchased
import com.prime.gallery.core.compose.Button
import com.prime.gallery.core.compose.DropDownPreference
import com.prime.gallery.core.compose.IconButton
import com.prime.gallery.core.compose.Preference
import com.prime.gallery.core.compose.SwitchPreference
import com.prime.gallery.core.compose.stringResource
import com.prime.gallery.core.compose.withStyle
import com.prime.gallery.purchase
import com.primex.core.stringHtmlResource
import com.primex.core.stringResource

private const val TAG = "Settings"

private val TopCurvedShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
private val Rectangular = RectangleShape
private val BottomCurved = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
private val CurvedShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonRestartableComposable
private fun Toolbar(
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior
) {
    val controller = LocalNavController.current
    LargeTopAppBar(
        title = { Text(text = "Settings") },
        scrollBehavior = behavior,
        modifier = modifier,
        navigationIcon = {
            IconButton(icon = Icons.Default.ArrowBack,
                contentDescription = null,
                onClick = { controller.navigateUp() })
        },
        actions = {
            val provider = LocalsProvider.current
            val purchased by purchase(id = Product.DISABLE_ADS)
            if (purchased.purchased)
                IconButton(
                    icon = Icons.Outlined.ShoppingCart,
                    contentDescription = "buy full version",
                    onClick = { provider.launchBillingFlow(Product.DISABLE_ADS) }
                )
        },
    )
}

@Composable
private fun Card(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Material.colorScheme.surfaceColorAtElevation(2.dp),
        shape = CurvedShape
    ) {
        Column(Modifier.padding(ContentPadding.normal)) {
            // Title + Developer + App version
            // App name etc.
            Text(
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    val appName = stringResource(id = R.string.app_name)
                    withStyle(Material.typography.headlineSmall) {
                        append(appName)
                    }
                    // The app version and check for updates.
                    val version = BuildConfig.VERSION_NAME
                    withStyle(Material.typography.labelSmall) {
                        append("v$version")
                    }
                    withStyle(Material.typography.labelMedium) {
                        append("\nby Zakir Sheikh")
                    }
                },
            )
            // Donate + Source Code Row
            Row(modifier = Modifier.padding(top = ContentPadding.large)) {
                // Donate
                Button(
                    label = "Donate",
                    icon = rememberVectorPainter(image = Icons.Default.Euro),
                    onClick = { /*TODO*/ },
                    shape = Material.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = Material.colorScheme.primary),
                    modifier = Modifier
                        .padding(end = ContentPadding.medium)
                        .weight(1f)
                        .heightIn(52.dp),
                )

                // Source code
                Button(
                    label = "Github",
                    icon = rememberVectorPainter(image = Icons.Default.DataObject),
                    onClick = { /*TODO*/ },
                    shape = Material.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = Material.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(52.dp),
                )
            }
        }
    }
}

@Composable
private inline fun Header(
    text: CharSequence, modifier: Modifier = Modifier
) {
    com.prime.gallery.core.compose.Text(
        text = text,
        modifier = Modifier
            .padding(ContentPadding.normal)
            .padding(ContentPadding.normal)
            .then(modifier),
        color = Material.colorScheme.primary,
        style = Material.typography.titleSmall
    )
}

private fun Context.shareApp() {
    ShareCompat
        .IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(getString(R.string.app_name))
        .setText("Let me recommend you this application ${Gallery.GOOGLE_STORE}").startChooser()
}

@Composable
private fun Content(
    resolver: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // TODO Replace this with lazy column
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {

        val provider = LocalsProvider.current
        Card(modifier = Modifier.padding(ContentPadding.normal))

        // Banner
        val purchase by purchase(id = Product.DISABLE_ADS)
        if (!purchase.purchased)
            Banner(
                placementID = Placement.BANNER_SETTINGS,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

        // Appearance Prefs
        Header(text = stringResource(id = R.string.appearance))

        //dark mode
        val darkTheme by resolver.nightMode
        DropDownPreference(
            title = stringResource(value = darkTheme.title),
            defaultValue = darkTheme.value,
            icon = darkTheme.vector,
            entries = listOf(
                "Dark" to NightMode.YES,
                "Light" to NightMode.NO,
                "Sync with System" to NightMode.FOLLOW_SYSTEM
            ),
            onRequestChange = {
                resolver.set(Gallery.NIGHT_MODE, it)
                provider.showAd(force = true)
            },
            shape = TopCurvedShape,
            modifier = Modifier.padding(horizontal = ContentPadding.normal)
        )


        //Color status bar
        val colorStatusBar by resolver.colorSystemBars
        SwitchPreference(
            checked = colorStatusBar.value,
            title = stringResource(value = colorStatusBar.title),
            summery = stringResource(value = colorStatusBar.summery),
            onCheckedChange = { should: Boolean ->
                resolver.set(Gallery.COLOR_STATUS_BAR, should)
                provider.showAd(force = true)
            },
            shape = Rectangular,
            modifier = Modifier.padding(horizontal = ContentPadding.normal)
        )

        // Enable/Disable dynamic colors
        val dynamicColors by resolver.dynamicColors
        SwitchPreference(
            checked = dynamicColors.value,
            title = stringResource(value = dynamicColors.title),
            summery = stringResource(value = dynamicColors.summery),
            onCheckedChange = { should: Boolean ->
                resolver.set(Gallery.DYNAMIC_COLORS, should)
            },
            shape = RectangleShape,
            modifier = Modifier.padding(horizontal = ContentPadding.normal)
        )

        //Hide StatusBar
        val hideStatusBar by resolver.hideStatusBar
        SwitchPreference(
            checked = hideStatusBar.value,
            title = stringResource(value = hideStatusBar.title),
            summery = stringResource(value = hideStatusBar.summery),
            onCheckedChange = { should: Boolean ->
                resolver.set(Gallery.HIDE_STATUS_BAR, should)
            },
            shape = BottomCurved,
            modifier = Modifier.padding(horizontal = ContentPadding.normal)
        )

        //General Prefs
        Header(text = "General")

        val useTrashCan by resolver.useTrashCan
        SwitchPreference(
            checked = useTrashCan.value,
            title = stringResource(value = useTrashCan.title),
            summery = stringResource(value = useTrashCan.summery),
            onCheckedChange = { should: Boolean ->
                resolver.set(Gallery.KEY_USE_TRASH_CAN, should)
            },
            shape = CurvedShape,
            modifier = Modifier.padding(horizontal = ContentPadding.normal)
        )


        // Feedback Prefs
        Header(text = "Feedback")

        val context = LocalContext.current
        Preference(
            title = stringResource(id = R.string.feedback),
            summery = stringResource(id = R.string.feedback_desc),
            icon = Icons.Outlined.Feedback,
            shape = TopCurvedShape,
            modifier = Modifier
                .padding(horizontal = ContentPadding.normal)
                .clickable(onClick = { provider.launchAppStore() })
        )

        Preference(
            title = stringResource(R.string.rate_us),
            summery = stringResource(id = R.string.rate_us_msg),
            icon = Icons.Outlined.Star,
            shape = Rectangular,
            modifier = Modifier
                .padding(horizontal = ContentPadding.normal)
                .clickable(onClick = { provider.launchAppStore() })
        )

        Preference(
            title = stringResource(R.string.spread_the_word),
            summery = stringResource(R.string.spread_the_word_summery),
            icon = Icons.Outlined.Share,
            shape = BottomCurved,
            modifier = Modifier
                .padding(horizontal = ContentPadding.normal)
                .clickable(onClick = { context.shareApp() })
        )


        // Feedback Prefs
        Header(text = "About Us")

        Preference(
            title = stringResource(R.string.about_us),
            summery = stringHtmlResource(R.string.about_us_desc),
            shape = CurvedShape,
            modifier = Modifier.padding(horizontal = ContentPadding.normal),
            icon = Icons.Outlined.Info
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(viewModel: SettingsViewModel) {
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = { Toolbar(behavior = behavior) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.nestedScroll(behavior.nestedScrollConnection)
    ) {
        Content(
            resolver = viewModel,
            modifier = Modifier
                .padding(it)
                .navigationBarsPadding()
        )
    }
}

