package com.noted.noted.view.customView

/*
 * The MIT License Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * The MIT License Copyright (c) 2013 MeetMe, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// @formatter:off
/*
 * This is based on HorizontalListView.java from: https://github.com/dinocore1/DevsmartLib-Android
 * It has been substantially rewritten and added to from the original version.
 */
// @formatter:on

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.widget.EdgeEffectCompat
import com.noted.noted.R
import java.util.*


// @formatter:off
// @formatter:off
/**
 * A view that shows items in a horizontally scrolling list. The items
 * come from the [ListAdapter] associated with this view. <br></br>
 * <br></br>
 * **Limitations:**
 *
 *  * Does not support keyboard navigation
 *  * Does not support scroll bars *
 *  * Does not support header or footer views *
 *  * Does not support disabled items *
 *
 * <br></br>
 * **Custom XML Parameters Supported:**<br></br>
 * <br></br>
 *
 *  * **divider** - The divider to use between items. This can be a color or a drawable. If a drawable is used
 * dividerWidth will automatically be set to the intrinsic width of the provided drawable, this can be overriden by providing a dividerWidth.
 *  * **dividerWidth** - The width of the divider to be drawn.
 *  * **android:requiresFadingEdge** - If horizontal fading edges are enabled this view will render them
 *  * **android:fadingEdgeLength** - The length of the horizontal fading edges
 *
 */
// @formatter:on
class HorizontalListView(
    context: Context,
    attrs: AttributeSet?
) :
    AdapterView<ListAdapter?>(context, attrs) {
    /** Tracks ongoing flings  */
    protected var mFlingTracker: Scroller? = Scroller(getContext())

    /** Used for detecting gestures within this view so they can be handled  */
    private val mGestureDetector: GestureDetector

    /** This tracks the starting layout position of the leftmost view  */
    private var mDisplayOffset = 0

    /** Holds a reference to the adapter bound to this view  */
    protected var mAdapter: ListAdapter? = null

    /** Holds a cache of recycled views to be reused as needed  */
    private val mRemovedViewsCache: MutableList<Queue<View>> =
        ArrayList()

    /** Flag used to mark when the adapters data has changed, so the view can be relaid out  */
    private var mDataChanged = false

    /** Temporary rectangle to be used for measurements  */
    private val mRect = Rect()

    /** Tracks the currently touched view, used to delegate touches to the view being touched  */
    private var mViewBeingTouched: View? = null

    /** The width of the divider that will be used between list items  */
    private var mDividerWidth = 0

    /** The drawable that will be used as the list divider  */
    private var mDivider: Drawable? = null

    /** The x position of the currently rendered view  */
    protected var mCurrentX = 0

    /** The x position of the next to be rendered view  */
    protected var mNextX = 0

    /** Used to hold the scroll position to restore to post rotate  */
    private var mRestoreX: Int? = null

    /** Tracks the maximum possible X position, stays at max value until last item is laid out and it can be determined  */
    private var mMaxX = Int.MAX_VALUE

    /** The adapter index of the leftmost view currently visible  */
    private var mLeftViewAdapterIndex = 0

    /** The adapter index of the rightmost view currently visible  */
    private var mRightViewAdapterIndex = 0

    /** This tracks the currently selected accessibility item  */
    private var mCurrentlySelectedAdapterIndex = 0

    /**
     * Callback interface to notify listener that the user has scrolled this view to the point that it is low on data.
     */
    private var mRunningOutOfDataListener: RunningOutOfDataListener? = null

    /**
     * This tracks the user value set of how many items from the end will be considered running out of data.
     */
    private var mRunningOutOfDataThreshold = 0

    /**
     * Tracks if we have told the listener that we are running low on data. We only want to tell them once.
     */
    private var mHasNotifiedRunningLowOnData = false

    /**
     * Callback interface to be invoked when the scroll state has changed.
     */
    private var mOnScrollStateChangedListener: OnScrollStateChangedListener? =
        null

    /**
     * Represents the current scroll state of this view. Needed so we can detect when the state changes so scroll listener can be notified.
     */
    private var mCurrentScrollState: OnScrollStateChangedListener.ScrollState = OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE

    /**
     * Tracks the state of the left edge glow.
     */
    private val mEdgeGlowLeft: EdgeEffectCompat?

    /**
     * Tracks the state of the right edge glow.
     */
    private val mEdgeGlowRight: EdgeEffectCompat?

    /** The height measure spec for this view, used to help size children views  */
    private var mHeightMeasureSpec = 0

    /** Used to track if a view touch should be blocked because it stopped a fling  */
    private var mBlockTouchAction = false

    /** Used to track if the parent vertically scrollable view has been told to DisallowInterceptTouchEvent  */
    private var mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent =
        false

    /**
     * The listener that receives notifications when this view is clicked.
     */
    private var mOnClickListener: OnClickListener? = null

    /** Registers the gesture detector to receive gesture notifications for this view  */
    private fun bindGestureDetector() {
        // Generic touch listener that can be applied to any view that needs to process gestures
        val gestureListenerHandler =
            OnTouchListener { v, event -> // Delegate the touch event to our gesture detector
                mGestureDetector.onTouchEvent(event)
            }
        setOnTouchListener(gestureListenerHandler)
    }

    /**
     * When this HorizontalListView is embedded within a vertical scrolling view it is important to disable the parent view from interacting with
     * any touch events while the user is scrolling within this HorizontalListView. This will start at this view and go up the view tree looking
     * for a vertical scrolling view. If one is found it will enable or disable parent touch interception.
     *
     * @param disallowIntercept If true the parent will be prevented from intercepting child touch events
     */
    private fun requestParentListViewToNotInterceptTouchEvents(disallowIntercept: Boolean) {
        // Prevent calling this more than once needlessly
        if (mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent != disallowIntercept) {
            var view: View = this
            while (view.parent is View) {
                // If the parent is a ListView or ScrollView then disallow intercepting of touch events
                if (view.parent is ListView || view.parent is ScrollView) {
                    view.parent.requestDisallowInterceptTouchEvent(disallowIntercept)
                    mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent =
                        disallowIntercept
                    return
                }
                view = view.parent as View
            }
        }
    }

    /**
     * Parse the XML configuration for this widget
     *
     * @param context Context used for extracting attributes
     * @param attrs The Attribute Set containing the ColumnView attributes
     */
    private fun retrieveXmlConfiguration(
        context: Context,
        attrs: AttributeSet?
    ) {
        if (attrs != null) {
            val a =
                context.obtainStyledAttributes(attrs, R.styleable.HorizontalListView)

            // Get the provided drawable from the XML
            val d =
                a.getDrawable(R.styleable.HorizontalListView_android_divider)
            d?.let { setDivider(it) }

            // If a width is explicitly specified then use that width
            val dividerWidth =
                a.getDimensionPixelSize(R.styleable.HorizontalListView_dividerWidth, 0)
            if (dividerWidth != 0) {
                setDividerWidth(dividerWidth)
            }
            a.recycle()
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()

        // Add the parent state to the bundle
        bundle.putParcelable(
            BUNDLE_ID_PARENT_STATE,
            super.onSaveInstanceState()
        )

        // Add our state to the bundle
        bundle.putInt(BUNDLE_ID_CURRENT_X, mCurrentX)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state

            // Restore our state from the bundle
            mRestoreX =
                Integer.valueOf(bundle.getInt(BUNDLE_ID_CURRENT_X))

            // Restore out parent's state from the bundle
            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_ID_PARENT_STATE))
        }
    }

    /**
     * Sets the drawable that will be drawn between each item in the list. If the drawable does
     * not have an intrinsic width, you should also call [.setDividerWidth]
     *
     * @param divider The drawable to use.
     */
    fun setDivider(divider: Drawable?) {
        mDivider = divider
        if (divider != null) {
            setDividerWidth(divider.intrinsicWidth)
        } else {
            setDividerWidth(0)
        }
    }

    /**
     * Sets the width of the divider that will be drawn between each item in the list. Calling
     * this will override the intrinsic width as set by [.setDivider]
     *
     * @param width The width of the divider in pixels.
     */
    fun setDividerWidth(width: Int) {
        mDividerWidth = width

        // Force the view to rerender itself
        requestLayout()
        invalidate()
    }

    private fun initView() {
        mLeftViewAdapterIndex = -1
        mRightViewAdapterIndex = -1
        mDisplayOffset = 0
        mCurrentX = 0
        mNextX = 0
        mMaxX = Int.MAX_VALUE
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
    }

    /** Will re-initialize the HorizontalListView to remove all child views rendered and reset to initial configuration.  */
    private fun reset() {
        initView()
        removeAllViewsInLayout()
        requestLayout()
    }

    /** DataSetObserver used to capture adapter data change events  */
    private val mAdapterDataObserver: DataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            mDataChanged = true

            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false
            unpressTouchedChild()

            // Invalidate and request layout to force this view to completely redraw itself
            invalidate()
            requestLayout()
        }

        override fun onInvalidated() {
            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false
            unpressTouchedChild()
            reset()

            // Invalidate and request layout to force this view to completely redraw itself
            invalidate()
            requestLayout()
        }
    }

    override fun setSelection(position: Int) {
        mCurrentlySelectedAdapterIndex = position
    }

    override fun getSelectedView(): View {
        return getChild(mCurrentlySelectedAdapterIndex)!!
    }

    override fun setAdapter(adapter: ListAdapter?) {
        if (mAdapter != null) {
            mAdapter!!.unregisterDataSetObserver(mAdapterDataObserver)
        }
        if (adapter != null) {
            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false
            mAdapter = adapter
            mAdapter!!.registerDataSetObserver(mAdapterDataObserver)
        }
        initializeRecycledViewCache(mAdapter!!.viewTypeCount)
        reset()
    }

    override fun getAdapter(): ListAdapter? {
        return mAdapter
    }

    /**
     * Will create and initialize a cache for the given number of different types of views.
     *
     * @param viewTypeCount - The total number of different views supported
     */
    private fun initializeRecycledViewCache(viewTypeCount: Int) {
        // The cache is created such that the response from mAdapter.getItemViewType is the array index to the correct cache for that item.
        mRemovedViewsCache.clear()
        for (i in 0 until viewTypeCount) {
            mRemovedViewsCache.add(LinkedList())
        }
    }

    /**
     * Returns a recycled view from the cache that can be reused, or null if one is not available.
     *
     * @param adapterIndex
     * @return
     */
    private fun getRecycledView(adapterIndex: Int): View? {
        val itemViewType = mAdapter!!.getItemViewType(adapterIndex)
        return if (isItemViewTypeValid(itemViewType)) {
            mRemovedViewsCache[itemViewType].poll()
        } else null
    }

    /**
     * Adds the provided view to a recycled views cache.
     *
     * @param adapterIndex
     * @param view
     */
    private fun recycleView(adapterIndex: Int, view: View) {
        // There is one Queue of views for each different type of view.
        // Just add the view to the pile of other views of the same type.
        // The order they are added and removed does not matter.
        val itemViewType = mAdapter!!.getItemViewType(adapterIndex)
        if (isItemViewTypeValid(itemViewType)) {
            mRemovedViewsCache[itemViewType].offer(view)
        }
    }

    private fun isItemViewTypeValid(itemViewType: Int): Boolean {
        return itemViewType < mRemovedViewsCache.size
    }

    /** Adds a child to this viewgroup and measures it so it renders the correct size  */
    private fun addAndMeasureChild(child: View, viewPos: Int) {
        val params = getLayoutParams(child)
        addViewInLayout(child, viewPos, params, true)
        measureChild(child)
    }

    /**
     * Measure the provided child.
     *
     * @param child The child.
     */
    private fun measureChild(child: View) {
        val childLayoutParams = getLayoutParams(child)
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            mHeightMeasureSpec,
            paddingTop + paddingBottom,
            childLayoutParams.height
        )
        val childWidthSpec: Int
        childWidthSpec = if (childLayoutParams.width > 0) {
            MeasureSpec.makeMeasureSpec(childLayoutParams.width, MeasureSpec.EXACTLY)
        } else {
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        }
        child.measure(childWidthSpec, childHeightSpec)
    }

    /** Gets a child's layout parameters, defaults if not available.  */
    private fun getLayoutParams(child: View): LayoutParams {
        var layoutParams = child.layoutParams
        if (layoutParams == null) {
            // Since this is a horizontal list view default to matching the parents height, and wrapping the width
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            )
        }
        return layoutParams
    }

    @SuppressLint("WrongCall")
    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (mAdapter == null) {
            return
        }

        // Force the OS to redraw this view
        invalidate()

        // If the data changed then reset everything and render from scratch at the same offset as last time
        if (mDataChanged) {
            val oldCurrentX = mCurrentX
            initView()
            removeAllViewsInLayout()
            mNextX = oldCurrentX
            mDataChanged = false
        }

        // If restoring from a rotation
        if (mRestoreX != null) {
            mNextX = mRestoreX as Int
            mRestoreX = null
        }

        // If in a fling
        if (mFlingTracker!!.computeScrollOffset()) {
            // Compute the next position
            mNextX = mFlingTracker!!.currX
        }

        // Prevent scrolling past 0 so you can't scroll past the end of the list to the left
        if (mNextX < 0) {
            mNextX = 0

            // Show an edge effect absorbing the current velocity
            if (mEdgeGlowLeft!!.isFinished) {
                mEdgeGlowLeft.onAbsorb(determineFlingAbsorbVelocity().toInt())
            }
            mFlingTracker!!.forceFinished(true)
            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
        } else if (mNextX > mMaxX) {
            // Clip the maximum scroll position at mMaxX so you can't scroll past the end of the list to the right
            mNextX = mMaxX

            // Show an edge effect absorbing the current velocity
            if (mEdgeGlowRight!!.isFinished) {
                mEdgeGlowRight.onAbsorb(determineFlingAbsorbVelocity().toInt())
            }
            mFlingTracker!!.forceFinished(true)
            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
        }

        // Calculate our delta from the last time the view was drawn
        val dx = mCurrentX - mNextX
        removeNonVisibleChildren(dx)
        fillList(dx)
        positionChildren(dx)

        // Since the view has now been drawn, update our current position
        mCurrentX = mNextX

        // If we have scrolled enough to lay out all views, then determine the maximum scroll position now
        if (determineMaxX()) {
            // Redo the layout pass since we now know the maximum scroll position
            onLayout(changed, left, top, right, bottom)
            return
        }

        // If the fling has finished
        if (mFlingTracker!!.isFinished) {
            // If the fling just ended
            if (mCurrentScrollState == OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING) {
                setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
            }
        } else {
            // Still in a fling so schedule the next frame
            ViewCompat.postOnAnimation(this, mDelayedLayout)
        }
    }

    override fun getLeftFadingEdgeStrength(): Float {
        val horizontalFadingEdgeLength = horizontalFadingEdgeLength

        // If completely at the edge then disable the fading edge
        return if (mCurrentX == 0) {
            0.toFloat()
        } else if (mCurrentX < horizontalFadingEdgeLength) {
            // We are very close to the edge, so enable the fading edge proportional to the distance from the edge, and the width of the edge effect
            mCurrentX.toFloat() / horizontalFadingEdgeLength
        } else {
            // The current x position is more then the width of the fading edge so enable it fully.
            1.toFloat()
        }
    }

    override fun getRightFadingEdgeStrength(): Float {
        val horizontalFadingEdgeLength = horizontalFadingEdgeLength

        // If completely at the edge then disable the fading edge
        return if (mCurrentX == mMaxX) {
            0.toFloat()
        } else if (mMaxX - mCurrentX < horizontalFadingEdgeLength) {
            // We are very close to the edge, so enable the fading edge proportional to the distance from the ednge, and the width of the edge effect
            (mMaxX - mCurrentX).toFloat() / horizontalFadingEdgeLength
        } else {
            // The distance from the maximum x position is more then the width of the fading edge so enable it fully.
            1.toFloat()
        }
    }

    /** Determines the current fling absorb velocity  */
    private fun determineFlingAbsorbVelocity(): Float {
        // If the OS version is high enough get the real velocity */
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            IceCreamSandwichPlus.getCurrVelocity(mFlingTracker)
        } else {
            // Unable to get the velocity so just return a default.
            // In actuality this is never used since EdgeEffectCompat does not draw anything unless the device is ICS+.
            // Less then ICS EdgeEffectCompat essentially performs a NOP.
            FLING_DEFAULT_ABSORB_VELOCITY
        }
    }

    /** Use to schedule a request layout via a runnable  */
    private val mDelayedLayout = Runnable { requestLayout() }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Cache off the measure spec
        mHeightMeasureSpec = heightMeasureSpec
    }

    /**
     * Determine the Max X position. This is the farthest that the user can scroll the screen. Until the last adapter item has been
     * laid out it is impossible to calculate; once that has occurred this will perform the calculation, and if necessary force a
     * redraw and relayout of this view.
     *
     * @return true if the maxx position was just determined
     */
    private fun determineMaxX(): Boolean {
        // If the last view has been laid out, then we can determine the maximum x position
        if (isLastItemInAdapter(mRightViewAdapterIndex)) {
            val rightView = rightmostChild
            if (rightView != null) {
                val oldMaxX = mMaxX

                // Determine the maximum x position
                mMaxX = mCurrentX + (rightView.right - paddingLeft) - renderWidth

                // Handle the case where the views do not fill at least 1 screen
                if (mMaxX < 0) {
                    mMaxX = 0
                }
                if (mMaxX != oldMaxX) {
                    return true
                }
            }
        }
        return false
    }

    /** Adds children views to the left and right of the current views until the screen is full  */
    private fun fillList(dx: Int) {
        // Get the rightmost child and determine its right edge
        var edge = 0
        var child = rightmostChild
        if (child != null) {
            edge = child.right
        }

        // Add new children views to the right, until past the edge of the screen
        fillListRight(edge, dx)

        // Get the leftmost child and determine its left edge
        edge = 0
        child = leftmostChild
        if (child != null) {
            edge = child.left
        }

        // Add new children views to the left, until past the edge of the screen
        fillListLeft(edge, dx)
    }

    private fun removeNonVisibleChildren(dx: Int) {
        var child = leftmostChild

        // Loop removing the leftmost child, until that child is on the screen
        while (child != null && child.right + dx <= 0) {
            // The child is being completely removed so remove its width from the display offset and its divider if it has one.
            // To remove add the size of the child and its divider (if it has one) to the offset.
            // You need to add since its being removed from the left side, i.e. shifting the offset to the right.
            mDisplayOffset += if (isLastItemInAdapter(mLeftViewAdapterIndex)) child.measuredWidth else mDividerWidth + child.measuredWidth

            // Add the removed view to the cache
            recycleView(mLeftViewAdapterIndex, child)

            // Actually remove the view
            removeViewInLayout(child)

            // Keep track of the adapter index of the left most child
            mLeftViewAdapterIndex++

            // Get the new leftmost child
            child = leftmostChild
        }
        child = rightmostChild

        // Loop removing the rightmost child, until that child is on the screen
        while (child != null && child.left + dx >= width) {
            recycleView(mRightViewAdapterIndex, child)
            removeViewInLayout(child)
            mRightViewAdapterIndex--
            child = rightmostChild
        }
    }

    private fun fillListRight(rightEdge: Int, dx: Int) {
        // Loop adding views to the right until the screen is filled
        var rightEdge = rightEdge
        while (rightEdge + dx + mDividerWidth < width && mRightViewAdapterIndex + 1 < mAdapter!!.count) {
            mRightViewAdapterIndex++

            // If mLeftViewAdapterIndex < 0 then this is the first time a view is being added, and left == right
            if (mLeftViewAdapterIndex < 0) {
                mLeftViewAdapterIndex = mRightViewAdapterIndex
            }

            // Get the view from the adapter, utilizing a cached view if one is available
            val child = mAdapter!!.getView(
                mRightViewAdapterIndex,
                getRecycledView(mRightViewAdapterIndex),
                this
            )
            addAndMeasureChild(child, INSERT_AT_END_OF_LIST)

            // If first view, then no divider to the left of it, otherwise add the space for the divider width
            rightEdge += (if (mRightViewAdapterIndex == 0) 0 else mDividerWidth) + child.measuredWidth

            // Check if we are running low on data so we can tell listeners to go get more
            determineIfLowOnData()
        }
    }

    private fun fillListLeft(leftEdge: Int, dx: Int) {
        // Loop adding views to the left until the screen is filled
        var leftEdge = leftEdge
        while (leftEdge + dx - mDividerWidth > 0 && mLeftViewAdapterIndex >= 1) {
            mLeftViewAdapterIndex--
            val child = mAdapter!!.getView(
                mLeftViewAdapterIndex,
                getRecycledView(mLeftViewAdapterIndex),
                this
            )
            addAndMeasureChild(child, INSERT_AT_START_OF_LIST)

            // If first view, then no divider to the left of it
            leftEdge -= if (mLeftViewAdapterIndex == 0) child.measuredWidth else mDividerWidth + child.measuredWidth

            // If on a clean edge then just remove the child, otherwise remove the divider as well
            mDisplayOffset -= if (leftEdge + dx == 0) child.measuredWidth else mDividerWidth + child.measuredWidth
        }
    }

    /** Loops through each child and positions them onto the screen  */
    private fun positionChildren(dx: Int) {
        val childCount = childCount
        if (childCount > 0) {
            mDisplayOffset += dx
            var leftOffset = mDisplayOffset

            // Loop each child view
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val left = leftOffset + paddingLeft
                val top = paddingTop
                val right = left + child.measuredWidth
                val bottom = top + child.measuredHeight

                // Layout the child
                child.layout(left, top, right, bottom)

                // Increment our offset by added child's size and divider width
                leftOffset += child.measuredWidth + mDividerWidth
            }
        }
    }

    /** Gets the current child that is leftmost on the screen.  */
    private val leftmostChild: View
        private get() = getChildAt(0)

    /** Gets the current child that is rightmost on the screen.  */
    private val rightmostChild: View
        private get() = getChildAt(childCount - 1)

    /**
     * Finds a child view that is contained within this view, given the adapter index.
     * @return View The child view, or or null if not found.
     */
    private fun getChild(adapterIndex: Int): View? {
        return if (adapterIndex >= mLeftViewAdapterIndex && adapterIndex <= mRightViewAdapterIndex) {
            getChildAt(adapterIndex - mLeftViewAdapterIndex)
        } else null
    }

    /**
     * Returns the index of the child that contains the coordinates given.
     * This is useful to determine which child has been touched.
     * This can be used for a call to [.getChildAt]
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child is found then returns -1
     */
    private fun getChildIndex(x: Int, y: Int): Int {
        val childCount = childCount
        for (index in 0 until childCount) {
            getChildAt(index).getHitRect(mRect)
            if (mRect.contains(x, y)) {
                return index
            }
        }
        return -1
    }

    /** Simple convenience method for determining if this index is the last index in the adapter  */
    private fun isLastItemInAdapter(index: Int): Boolean {
        return index == mAdapter!!.count - 1
    }

    /** Gets the height in px this view will be rendered. (padding removed)  */
    private val renderHeight: Int
        private get() = height - paddingTop - paddingBottom

    /** Gets the width in px this view will be rendered. (padding removed)  */
    private val renderWidth: Int
        private get() = width - paddingLeft - paddingRight

    /** Scroll to the provided offset  */
    fun scrollTo(x: Int) {
        mFlingTracker!!.startScroll(mNextX, 0, x - mNextX, 0)
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING)
        requestLayout()
    }

    override fun getFirstVisiblePosition(): Int {
        return mLeftViewAdapterIndex
    }

    override fun getLastVisiblePosition(): Int {
        return mRightViewAdapterIndex
    }

    /** Draws the overscroll edge glow effect on the left and right sides of the horizontal list  */
    private fun drawEdgeGlow(canvas: Canvas) {
        if (mEdgeGlowLeft != null && !mEdgeGlowLeft.isFinished && isEdgeGlowEnabled) {
            // The Edge glow is meant to come from the top of the screen, so rotate it to draw on the left side.
            val restoreCount = canvas.save()
            val height = height
            canvas.rotate(-90f, 0f, 0f)
            canvas.translate(-height + paddingBottom.toFloat(), 0f)
            mEdgeGlowLeft.setSize(renderHeight, renderWidth)
            if (mEdgeGlowLeft.draw(canvas)) {
                invalidate()
            }
            canvas.restoreToCount(restoreCount)
        } else if (mEdgeGlowRight != null && !mEdgeGlowRight.isFinished && isEdgeGlowEnabled) {
            // The Edge glow is meant to come from the top of the screen, so rotate it to draw on the right side.
            val restoreCount = canvas.save()
            val width = width
            canvas.rotate(90f, 0f, 0f)
            canvas.translate(paddingTop.toFloat(), -width.toFloat())
            mEdgeGlowRight.setSize(renderHeight, renderWidth)
            if (mEdgeGlowRight.draw(canvas)) {
                invalidate()
            }
            canvas.restoreToCount(restoreCount)
        }
    }

    /** Draws the dividers that go in between the horizontal list view items  */
    private fun drawDividers(canvas: Canvas) {
        val count = childCount

        // Only modify the left and right in the loop, we set the top and bottom here since they are always the same
        val bounds = mRect
        mRect.top = paddingTop
        mRect.bottom = mRect.top + renderHeight

        // Draw the list dividers
        for (i in 0 until count) {
            // Don't draw a divider to the right of the last item in the adapter
            if (!(i == count - 1 && isLastItemInAdapter(mRightViewAdapterIndex))) {
                val child = getChildAt(i)
                bounds.left = child.right
                bounds.right = child.right + mDividerWidth

                // Clip at the left edge of the screen
                if (bounds.left < paddingLeft) {
                    bounds.left = paddingLeft
                }

                // Clip at the right edge of the screen
                if (bounds.right > width - paddingRight) {
                    bounds.right = width - paddingRight
                }

                // Draw a divider to the right of the child
                drawDivider(canvas, bounds)

                // If the first view, determine if a divider should be shown to the left of it.
                // A divider should be shown if the left side of this view does not fill to the left edge of the screen.
                if (i == 0 && child.left > paddingLeft) {
                    bounds.left = paddingLeft
                    bounds.right = child.left
                    drawDivider(canvas, bounds)
                }
            }
        }
    }

    /**
     * Draws a divider in the given bounds.
     *
     * @param canvas The canvas to draw to.
     * @param bounds The bounds of the divider.
     */
    private fun drawDivider(
        canvas: Canvas,
        bounds: Rect
    ) {
        if (mDivider != null) {
            mDivider!!.bounds = bounds
            mDivider!!.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDividers(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawEdgeGlow(canvas)
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        // Don't dispatch setPressed to our children. We call setPressed on ourselves to
        // get the selector in the right state, but we don't want to press each child.
    }

    protected fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        mFlingTracker!!.fling(mNextX, 0, (-velocityX).toInt(), 0, 0, mMaxX, 0, 0)
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING)
        requestLayout()
        return true
    }

    protected fun onDown(e: MotionEvent): Boolean {
        // If the user just caught a fling, then disable all touch actions until they release their finger
        mBlockTouchAction = !mFlingTracker!!.isFinished

        // Allow a finger down event to catch a fling
        mFlingTracker!!.forceFinished(true)
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
        unpressTouchedChild()
        if (!mBlockTouchAction) {
            // Find the child that was pressed
            val index = getChildIndex(e.x.toInt(), e.y.toInt())
            if (index >= 0) {
                // Save off view being touched so it can later be released
                mViewBeingTouched = getChildAt(index)
                if (mViewBeingTouched != null) {
                    // Set the view as pressed
                    mViewBeingTouched!!.isPressed = true
                    refreshDrawableState()
                }
            }
        }
        return true
    }

    /** If a view is currently pressed then unpress it  */
    private fun unpressTouchedChild() {
        if (mViewBeingTouched != null) {
            // Set the view as not pressed
            mViewBeingTouched!!.isPressed = false
            refreshDrawableState()

            // Null out the view so we don't leak it
            mViewBeingTouched = null
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return this@HorizontalListView.onDown(e)
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return this@HorizontalListView.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Lock the user into interacting just with this view
            requestParentListViewToNotInterceptTouchEvents(true)
            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_TOUCH_SCROLL)
            unpressTouchedChild()
            mNextX += distanceX.toInt()
            updateOverscrollAnimation(Math.round(distanceX))
            requestLayout()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            unpressTouchedChild()
            val onItemClickListener = onItemClickListener
            val index = getChildIndex(e.x.toInt(), e.y.toInt())

            // If the tap is inside one of the child views, and we are not blocking touches
            if (index >= 0 && !mBlockTouchAction) {
                val child = getChildAt(index)
                val adapterIndex = mLeftViewAdapterIndex + index
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(
                        this@HorizontalListView,
                        child,
                        adapterIndex,
                        mAdapter!!.getItemId(adapterIndex)
                    )
                    return true
                }
            }
            if (mOnClickListener != null && !mBlockTouchAction) {
                mOnClickListener!!.onClick(this@HorizontalListView)
            }
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            unpressTouchedChild()
            val index = getChildIndex(e.x.toInt(), e.y.toInt())
            if (index >= 0 && !mBlockTouchAction) {
                val child = getChildAt(index)
                val onItemLongClickListener = onItemLongClickListener
                if (onItemLongClickListener != null) {
                    val adapterIndex = mLeftViewAdapterIndex + index
                    val handled = onItemLongClickListener.onItemLongClick(
                        this@HorizontalListView, child, adapterIndex, mAdapter
                            !!.getItemId(adapterIndex)
                    )
                    if (handled) {
                        // BZZZTT!!1!
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Detect when the user lifts their finger off the screen after a touch
        if (event.action == MotionEvent.ACTION_UP) {
            // If not flinging then we are idle now. The user just finished a finger scroll.
            if (mFlingTracker == null || mFlingTracker!!.isFinished) {
                setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE)
            }

            // Allow the user to interact with parent views
            requestParentListViewToNotInterceptTouchEvents(false)
            releaseEdgeGlow()
        } else if (event.action == MotionEvent.ACTION_CANCEL) {
            unpressTouchedChild()
            releaseEdgeGlow()

            // Allow the user to interact with parent views
            requestParentListViewToNotInterceptTouchEvents(false)
        }
        return super.onTouchEvent(event)
    }

    /** Release the EdgeGlow so it animates  */
    private fun releaseEdgeGlow() {
        mEdgeGlowLeft?.onRelease()
        mEdgeGlowRight?.onRelease()
    }

    /**
     * Sets a listener to be called when the HorizontalListView has been scrolled to a point where it is
     * running low on data. An example use case is wanting to auto download more data when the user
     * has scrolled to the point where only 10 items are left to be rendered off the right of the
     * screen. To get called back at that point just register with this function with a
     * numberOfItemsLeftConsideredLow value of 10. <br></br>
     * <br></br>
     * This will only be called once to notify that the HorizontalListView is running low on data.
     * Calling notifyDataSetChanged on the adapter will allow this to be called again once low on data.
     *
     * @param listener The listener to be notified when the number of array adapters items left to
     * be shown is running low.
     *
     * @param numberOfItemsLeftConsideredLow The number of array adapter items that have not yet
     * been displayed that is considered too low.
     */
    fun setRunningOutOfDataListener(
        listener: RunningOutOfDataListener?,
        numberOfItemsLeftConsideredLow: Int
    ) {
        mRunningOutOfDataListener = listener
        mRunningOutOfDataThreshold = numberOfItemsLeftConsideredLow
    }

    /**
     * This listener is used to allow notification when the HorizontalListView is running low on data to display.
     */
    interface RunningOutOfDataListener {
        /** Called when the HorizontalListView is running out of data and has reached at least the provided threshold.  */
        fun onRunningOutOfData()
    }

    /**
     * Determines if we are low on data and if so will call to notify the listener, if there is one,
     * that we are running low on data.
     */
    private fun determineIfLowOnData() {
        // Check if the threshold has been reached and a listener is registered
        if (mRunningOutOfDataListener != null && mAdapter != null && mAdapter!!.count - (mRightViewAdapterIndex + 1) < mRunningOutOfDataThreshold
        ) {

            // Prevent notification more than once
            if (!mHasNotifiedRunningLowOnData) {
                mHasNotifiedRunningLowOnData = true
                mRunningOutOfDataListener!!.onRunningOutOfData()
            }
        }
    }

    /**
     * Register a callback to be invoked when the HorizontalListView has been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    override fun setOnClickListener(listener: OnClickListener?) {
        mOnClickListener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the view scroll state has changed.
     */
    interface OnScrollStateChangedListener {
        enum class ScrollState {
            /**
             * The view is not scrolling. Note navigating the list using the trackball counts as being
             * in the idle state since these transitions are not animated.
             */
            SCROLL_STATE_IDLE,

            /**
             * The user is scrolling using touch, and their finger is still on the screen
             */
            SCROLL_STATE_TOUCH_SCROLL,

            /**
             * The user had previously been scrolling using touch and had performed a fling. The
             * animation is now coasting to a stop
             */
            SCROLL_STATE_FLING
        }

        /**
         * Callback method to be invoked when the scroll state changes.
         *
         * @param scrollState The current scroll state.
         */
        fun onScrollStateChanged(scrollState: ScrollState?)
    }

    /**
     * Sets a listener to be invoked when the scroll state has changed.
     *
     * @param listener The listener to be invoked.
     */
    fun setOnScrollStateChangedListener(listener: OnScrollStateChangedListener?) {
        mOnScrollStateChangedListener = listener
    }

    /**
     * Call to set the new scroll state.
     * If it has changed and a listener is registered then it will be notified.
     */
    private fun setCurrentScrollState(newScrollState: OnScrollStateChangedListener.ScrollState) {
        // If the state actually changed then notify listener if there is one
        if (mCurrentScrollState != newScrollState && mOnScrollStateChangedListener != null) {
            mOnScrollStateChangedListener!!.onScrollStateChanged(newScrollState)
        }
        mCurrentScrollState = newScrollState
    }

    /**
     * Updates the over scroll animation based on the scrolled offset.
     *
     * @param scrolledOffset The scroll offset
     */
    private fun updateOverscrollAnimation(scrolledOffset: Int) {
        if (mEdgeGlowLeft == null || mEdgeGlowRight == null) return

        // Calculate where the next scroll position would be
        val nextScrollPosition = mCurrentX + scrolledOffset

        // If not currently in a fling (Don't want to allow fling offset updates to cause over scroll animation)
        if (mFlingTracker == null || mFlingTracker!!.isFinished) {
            // If currently scrolled off the left side of the list and the adapter is not empty
            if (nextScrollPosition < 0) {

                // Calculate the amount we have scrolled since last frame
                val overscroll = Math.abs(scrolledOffset)

                // Tell the edge glow to redraw itself at the new offset
                mEdgeGlowLeft.onPull(overscroll.toFloat() / renderWidth)

                // Cancel animating right glow
                if (!mEdgeGlowRight.isFinished) {
                    mEdgeGlowRight.onRelease()
                }
            } else if (nextScrollPosition > mMaxX) {
                // Scrolled off the right of the list

                // Calculate the amount we have scrolled since last frame
                val overscroll = Math.abs(scrolledOffset)

                // Tell the edge glow to redraw itself at the new offset
                mEdgeGlowRight.onPull(overscroll.toFloat() / renderWidth)

                // Cancel animating left glow
                if (!mEdgeGlowLeft.isFinished) {
                    mEdgeGlowLeft.onRelease()
                }
            }
        }
    }// If the maxx is more then zero then the user can scroll, so the edge effects should be shown

    /**
     * Checks if the edge glow should be used enabled.
     * The glow is not enabled unless there are more views than can fit on the screen at one time.
     */
    private val isEdgeGlowEnabled: Boolean
        private get() = if (mAdapter == null || mAdapter!!.isEmpty) false else mMaxX > 0

    // If the maxx is more then zero then the user can scroll, so the edge effects should be shown

    /** Wrapper class to protect access to API version 11 and above features  */
    private object HoneycombPlus {
        /** Sets the friction for the provided scroller  */
        fun setFriction(scroller: Scroller?, friction: Float) {
            scroller?.setFriction(friction)
        }

        init {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                throw RuntimeException("Should not get to HoneycombPlus class unless sdk is >= 11!")
            }
        }
    }

    /** Wrapper class to protect access to API version 14 and above features  */
    private object IceCreamSandwichPlus {
        /** Gets the velocity for the provided scroller  */
        fun getCurrVelocity(scroller: Scroller?): Float {
            return scroller!!.currVelocity
        }

        init {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                throw RuntimeException("Should not get to IceCreamSandwichPlus class unless sdk is >= 14!")
            }
        }
    }

    companion object {
        /** Defines where to insert items into the ViewGroup, as defined in `ViewGroup #addViewInLayout(View, int, LayoutParams, boolean)`  */
        private const val INSERT_AT_END_OF_LIST = -1
        private const val INSERT_AT_START_OF_LIST = 0

        /** The velocity to use for overscroll absorption  */
        private const val FLING_DEFAULT_ABSORB_VELOCITY = 30f

        /** The friction amount to use for the fling tracker  */
        private const val FLING_FRICTION = 0.009f

        /** Used for tracking the state data necessary to restore the HorizontalListView to its previous state after a rotation occurs  */
        private const val BUNDLE_ID_CURRENT_X = "BUNDLE_ID_CURRENT_X"

        /** The bundle id of the parents state. Used to restore the parent's state after a rotation occurs  */
        private const val BUNDLE_ID_PARENT_STATE = "BUNDLE_ID_PARENT_STATE"
    }

    init {
        mEdgeGlowLeft = EdgeEffectCompat(context)
        mEdgeGlowRight = EdgeEffectCompat(context)
        /** Gesture listener to receive callbacks when gestures are detected  */
        val mGestureListener = GestureListener()
        mGestureDetector = GestureDetector(context, mGestureListener)
        bindGestureDetector()
        initView()
        retrieveXmlConfiguration(context, attrs)
        setWillNotDraw(false)

        // If the OS version is high enough then set the friction on the fling tracker */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            HoneycombPlus.setFriction(
                mFlingTracker,
                FLING_FRICTION
            )
        }
    }
}