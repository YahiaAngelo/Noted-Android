package com.noted.noted.view.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.transition.TransitionManager
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.play.core.review.ReviewManagerFactory
import com.noted.noted.BuildConfig
import com.noted.noted.R
import io.noties.markwon.Markwon

class AboutActivity : MaterialAboutActivity() {

    var popupWindow: PopupWindow? = null
    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.about)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {

        val notedCard = MaterialAboutCard.Builder()
            .addItem(MaterialAboutTitleItem.Builder()
                .text("Noted")
                .icon(R.mipmap.ic_launcher_round)
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.version))
                .subText(BuildConfig.VERSION_NAME)
                .icon(R.drawable.ic_info_black)
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.changelog))
                .icon(R.drawable.ic_restore)
                .setOnClickAction {
                    showChangelogWindow()
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.license))
                .icon(R.drawable.ic_class)
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/YahiaAngelo/Noted-Android/blob/master/LICENSE")))
                }
                .build())
            .build()

        val authorCard = MaterialAboutCard.Builder()
            .title(getString(R.string.author))
            .addItem(MaterialAboutActionItem.Builder()
                .text("Yahia Mostafa")
                .subText("@YahiaAngelo")
                .icon(R.drawable.ic_person)
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/YahiaAngelo")))
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text("Follow on Twitter")
                .icon(R.drawable.ic_logo_twitter)
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://twitter.com/YahiaAngelo_")))
                }
                .build())
            .build()

        val appInfoCard = MaterialAboutCard.Builder()
            .title(getString(R.string.about))
            .addItem(MaterialAboutActionItem.Builder()
                .text("Version")
                .subText(BuildConfig.VERSION_NAME)
                .icon(R.drawable.ic_info_black)
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text("Check source code")
                .icon(R.drawable.ic_logo_github)
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/YahiaAngelo/Noted-Android")))
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.rate_app))
                .icon(R.drawable.ic_star_rate)
                .setOnClickAction {
                    val uri: Uri = Uri.parse("market://details?id=$packageName")
                    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    try {
                        startActivity(goToMarket)
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
                    }
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.support_project))
                .icon(R.drawable.ic_logo_paypal)
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://paypal.me/YahiaMostafa")))
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.contact_us))
                .icon(R.drawable.ic_email)
                .setOnClickAction {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:") // only email apps should handle this
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("yahia.mostafa.elsayed@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Noted")
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                }
                .build())
            .build()

        return MaterialAboutList.Builder()
            .addCard(notedCard)
            .addCard(authorCard)
            .addCard(appInfoCard)
            .build()

    }

    private fun showChangelogWindow(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.changelog_view,null)
        popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        ).apply {
            enterTransition = android.transition.Fade()
            exitTransition = android.transition.Fade()
            setBackgroundDrawable(ColorDrawable())
            isOutsideTouchable = true
            elevation = 10.0F
        }

        val markwon = Markwon.create(this)
        val changelogTextView = view.findViewById<TextView>(R.id.changelog_text)
        markwon.setMarkdown(changelogTextView, resources.getString(R.string.app_changelog))
        TransitionManager.beginDelayedTransition(this.recyclerView)
        popupWindow!!.showAtLocation(
            this.recyclerView, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

    override fun onBackPressed() {
        if (popupWindow != null){
            if (popupWindow!!.isShowing){
                popupWindow!!.dismiss()
            }else{
                super.onBackPressed()
            }
        }else{
            super.onBackPressed()
        }
    }
}