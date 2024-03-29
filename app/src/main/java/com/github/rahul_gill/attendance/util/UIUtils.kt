package com.github.rahul_gill.attendance.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.github.rahul_gill.attendance.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import kotlin.math.roundToInt


fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}


fun Context.spToPx(sp: Float): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics)
}

@ColorInt
fun Context.getThemeColor(@AttrRes themeAttribute: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(themeAttribute, typedValue, true)
    return typedValue.data
}


fun View.enableSystemBarsInsetsCallback(
    originalLeftMarginDp: Int = 0,
    originalRightMarginDp: Int = 0,
    originalBottomMarginDp: Int = 0,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = insets.left + view.context.dpToPx(originalLeftMarginDp)
            bottomMargin = insets.bottom + view.context.dpToPx(originalBottomMarginDp)
            marginEnd = insets.right + view.context.dpToPx(originalRightMarginDp)
        }
        WindowInsetsCompat.CONSUMED
    }
}

fun View.enableSystemGestureInsetsCallback() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
        view.updatePadding(insets.left, 0, insets.right, insets.bottom)
        WindowInsetsCompat.CONSUMED
    }
}

fun showSnackBarWithDismissButton(view: View, text: String) {
    val snackBar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
    snackBar.setAction(view.context.getString(R.string.ok)) {
        snackBar.dismiss()
    }
    snackBar.show()
}

fun enableViewAboveKeyboardWithAnimationCallback(
    view: View,
    getStartBottom: ((Int) -> Unit)? = null
) {
    ViewCompat.setWindowInsetsAnimationCallback(view,
        object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
            var startBottom = 0f
            var endBottom = 0f

            override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                super.onPrepare(animation)
                startBottom = view.bottom.toFloat()
                if (getStartBottom != null) {
                    getStartBottom(view.bottom)
                }
            }

            override fun onStart(
                animation: WindowInsetsAnimationCompat,
                bounds: WindowInsetsAnimationCompat.BoundsCompat
            ): WindowInsetsAnimationCompat.BoundsCompat {
                endBottom = view.bottom.toFloat()
                return super.onStart(animation, bounds)
            }

            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: MutableList<WindowInsetsAnimationCompat>
            ): WindowInsetsCompat {
                // Find an IME animation.
                val imeAnimation = runningAnimations.find {
                    (it.typeMask and WindowInsetsCompat.Type.ime() != 0)
                } ?: return insets
                // Offset the view based on the interpolated fraction of the IME animation.
                view.translationY =
                    (startBottom - endBottom) * (1 - imeAnimation.interpolatedFraction)

                return insets
            }
        }
    )
}

fun Context.showSoftKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}


fun softKeyboardVisible(view: View): Boolean {
    return ViewCompat.getRootWindowInsets(view)
        ?.isVisible(WindowInsetsCompat.Type.ime())
        ?: return false
}

fun Fragment.enableSharedZAxisTransition() {
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
        duration = 300L
    }
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
        duration = 300L
    }
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
        duration = 300L
    }
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
        duration = 300L
    }
}

fun TextView.textChangeWithBetterIndication() {
    val initialTextSize = textSize
    val animator = ValueAnimator.ofFloat(initialTextSize, initialTextSize * 1.5f, initialTextSize)
    animator.duration = 400
    animator.addUpdateListener {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, it.animatedValue as Float)
    }
    animator.start()
}

fun View.animateDropdown(activity: Activity, show: Boolean) {
    if (show) {
        val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            metrics.widthPixels
        }
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val widthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY)
        measure(widthMeasureSpec, heightMeasureSpec)
        val heightAnimator = ValueAnimator.ofInt(0, measuredHeight)
        isVisible = true
        heightAnimator.duration = 300
        heightAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            updateLayoutParams<LinearLayout.LayoutParams> {
                height = animatedValue
            }
        }
        heightAnimator.start()
    } else {
        val heightAnimator = ValueAnimator.ofInt(height, 0)
        heightAnimator.duration = 300
        heightAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            updateLayoutParams<LinearLayout.LayoutParams> {
                height = animatedValue
            }
            if (animatedValue == 0)
                isVisible = false
        }
        heightAnimator.start()
    }
}

fun View.animateRotation(toDegree: Float) {
    animate()
        .rotation(toDegree)
        .setDuration(300)
        .setListener(null)
}