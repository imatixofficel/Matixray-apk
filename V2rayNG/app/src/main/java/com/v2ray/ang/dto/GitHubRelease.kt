package com.v2ray.ang.dto

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("assets")
    val assets: List<Asset> = emptyList(),
    @SerializedName("prerelease")
    val prerelease: Boolean = false
) {
    data class Asset(
        @SerializedName("name")
        val name: String,
        @SerializedName("browser_download_url")
        val browserDownloadUrl: String,
        @SerializedName("size")
        val size: Long = 0L
    )
}
