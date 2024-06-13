package com.example.volcanoes

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class Repository(private val context: Context) {

    val list = listOf(Erebus, Kilauea, laPalma, stHelens, stromboli)

    fun getDetails(url: String, callback: (String) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        queue.add(
            StringRequest(
                Request.Method.GET,
                url,
                { response ->
                    callback.invoke(parse(response))
                },
                {
                    it.printStackTrace()
                    callback.invoke("error")
                })
        )
    }

    fun downloadImage(fragment: Fragment, url: String, view: ImageView, callback: () -> Unit) {

        Glide.with(fragment)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    view.setImageResource(R.drawable.placeholder)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    view.setImageBitmap(resource?.toBitmap())
                    callback.invoke()
                    return true
                }
            })
            .into(view)
    }

    private fun parse(info: String): String {
        var parsedInfo = info.substringAfter("<p>").substringBefore("</p>")
        while (parsedInfo.contains("<br />")) {
            parsedInfo = parsedInfo.substringBefore("<br />") +
                    "\n     " +
                    parsedInfo.substringAfter("<br />")
        }
        parsedInfo = "     $parsedInfo"
        return parsedInfo
    }
}