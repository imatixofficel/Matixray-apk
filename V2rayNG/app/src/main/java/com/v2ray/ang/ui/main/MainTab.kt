package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.v2ray.ang.R

/** The three pages of the main screen, shown in the bottom bar. */
enum class MainTab(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    Home(R.drawable.ic_home_24dp, R.string.tab_home),
    Configs(R.drawable.ic_configs_24dp, R.string.tab_configs),
    Settings(R.drawable.ic_settings_24dp, R.string.tab_settings)
}
