package com.prime.gallery

import android.app.Application
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.google.firebase.FirebaseApp
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.api.MediaProvider
import com.primex.preferences.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val TAG = "Gallery"

@Module
@InstallIn(SingletonComponent::class)
object Singleton {
    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context) =
        Preferences(context, "shared_preferences.db")

    @Provides
    @Singleton
    fun mediaProvider(@ApplicationContext context: Context) = MediaProvider(context.contentResolver)
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object Activity {
    @ActivityRetainedScoped
    @Provides
    fun channel() = SnackbarHostState()
}

@HiltAndroidApp
class Gallery : Application() {

    companion object {

        /**
         * Retrieves/Sets The [NightMode] Strategy
         */
        val NIGHT_MODE = stringPreferenceKey(
            "${TAG}_night_mode",
            NightMode.FOLLOW_SYSTEM,
            object : StringSaver<NightMode> {
                override fun save(value: NightMode): String = value.name
                override fun restore(value: String): NightMode = NightMode.valueOf(value)
            }
        )

        val COLOR_STATUS_BAR = booleanPreferenceKey(TAG + "_color_status_bar", false)
        val HIDE_STATUS_BAR = booleanPreferenceKey(TAG + "_hide_status_bar", false)
        val DYNAMIC_COLORS = booleanPreferenceKey(TAG + "_dynamic_colors", true)

        /**
         * The counter counts the number of times this app was launched.
         */
        val KEY_LAUNCH_COUNTER = intPreferenceKey(TAG + "_launch_counter")

        /**
         * The preference key for controlling the usage of the trash can feature.This key represents
         * a boolean preference that determines whether the trash can is enabled or not.
         */
        val KEY_USE_TRASH_CAN = booleanPreferenceKey(TAG + "_use_trash_can", true)

        /**
         * The link to PlayStore Market.
         */
        const val GOOGLE_STORE = "market://details?id=" + BuildConfig.APPLICATION_ID

        /**
         * If PlayStore is not available in Users Phone. This will be used to redirect to the
         * WebPage of the app.
         */
        const val FALLBACK_GOOGLE_STORE =
            "http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID

        /**
         * Package name for the Google Play Store. Value can be verified here:
         * https://developers.google.com/android/reference/com/google/android/gms/common/GooglePlayServicesUtil.html#GOOGLE_PLAY_STORE_PACKAGE
         */
        const val PKG_GOOGLE_PLAY_STORE = "com.android.vending"
    }

    override fun onCreate() {
        super.onCreate()
        // initialize firebase
        FirebaseApp.initializeApp(this)
    }
}