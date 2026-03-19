package com.smarttrip.app.ui.screens.inspiration

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.smarttrip.app.data.remote.models.InspirationDestinationDto
import org.json.JSONArray
import org.json.JSONObject

// ─── Controller (hold a ref to the WebView, expose public API) ───────────────
class GlobeController {
    internal var webView: WebView? = null

    fun zoomToCity(city: String) {
        val safe = city.replace("'", "\\'")
        webView?.evaluateJavascript("window.zoomToCity('$safe')", null)
    }

    fun highlightDestinations(destinations: List<InspirationDestinationDto>) {
        val arr = JSONArray()
        destinations.forEachIndexed { i, d ->
            val obj = JSONObject()
            obj.put("city", d.city ?: "")
            obj.put("isTop", i == 0)
            arr.put(obj)
        }
        webView?.evaluateJavascript("window.highlightDestinations($arr)", null)
    }

    fun resetView() {
        webView?.evaluateJavascript("window.resetView()", null)
    }

    fun setFavoriteCodes(codes: Set<String>) {
        val arr = JSONArray()
        codes.forEach { arr.put(it) }
        webView?.evaluateJavascript("window.setFavoriteCodes($arr)", null)
    }

    fun setRecentCodes(codes: Set<String>) {
        val arr = JSONArray()
        codes.forEach { arr.put(it) }
        webView?.evaluateJavascript("window.setRecentCodes($arr)", null)
    }

    fun startSurpriseSpin() {
        webView?.evaluateJavascript("window.startSurpriseSpin()", null)
    }
}

@Composable
fun rememberGlobeController(): GlobeController {
    return remember { GlobeController() }
}

// ─── WebView composable ───────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GlobeWebView(
    modifier: Modifier = Modifier,
    controller: GlobeController,
    onMarkerClick: (String) -> Unit = {},
    onGlobeReady: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // globe.html is a local asset that fetches Three.js over HTTPS;
                // file-to-file cross-origin access is not needed.
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.mediaPlaybackRequiresUserGesture = false
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                // Transparent background so the CSS gradient shows through
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                // Hardware acceleration for smooth WebGL rendering
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                // Only enable remote debugging in debug builds
                if (com.smarttrip.app.BuildConfig.DEBUG) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false
                }

                // JavaScript → Android bridge
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onMarkerClick(city: String) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onMarkerClick(city)
                            }
                        }

                        @JavascriptInterface
                        fun onGlobeReady() {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onGlobeReady()
                            }
                        }
                    },
                    "SmartTripBridge"
                )

                loadUrl("file:///android_asset/globe.html")
                controller.webView = this
            }
        },
        update = { webView ->
            controller.webView = webView
        }
    )
}
