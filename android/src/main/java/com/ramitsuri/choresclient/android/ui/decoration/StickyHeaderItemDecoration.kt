package com.ramitsuri.choresclient.android.ui.decoration

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class StickyHeaderItemDecoration(private val listener: StickyHeaderInterface): ItemDecoration() {
    private var stickyHeaderHeight = 0

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }
        val headerPosition: Int = listener.getHeaderPositionForItem(topChildPosition)
        val currentHeader = getHeaderViewForItem(parent, headerPosition)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPosition)
        if (childInContact != null &&
            listener.isHeader(parent.getChildAdapterPosition(childInContact))
        ) {
            moveHeader(c, currentHeader, childInContact)
            return
        }
        drawHeader(c, currentHeader)
    }

    private fun getHeaderViewForItem(parent: RecyclerView, itemPosition: Int): View {
        val headerPosition: Int = listener.getHeaderPositionForItem(itemPosition)
        val layoutResId: Int = listener.headerLayout
        val header = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        listener.bindHeaderData(header, headerPosition)
        return header
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0F, 0F)
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0F, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(
        parent: RecyclerView, contactPoint: Int,
        currentHeaderPosition: Int
    ): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(i)

            // measure height tolerance with child, if child is another header
            if (currentHeaderPosition != i) {
                val isChildHeader: Boolean =
                    listener.isHeader(parent.getChildAdapterPosition(child))
                if (isChildHeader) {
                    heightTolerance = stickyHeaderHeight - child.height
                }
            }

            // add heightTolerance if child is on top in display area
            val childBottomPosition: Int = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }
            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    // this child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    // Properly measures and layouts the top sticky header
    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup
            .getChildMeasureSpec(
                widthSpec, parent.paddingLeft + parent.paddingRight,
                view.layoutParams.width
            )
        val childHeightSpec = ViewGroup
            .getChildMeasureSpec(
                heightSpec, parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height
            )
        view.measure(childWidthSpec, childHeightSpec)
        stickyHeaderHeight = view.measuredHeight
        view.layout(0, 0, view.measuredWidth, stickyHeaderHeight)
    }

    interface StickyHeaderInterface {
        /**
         * @return Position of header that represents the item at position itemPosition
         */
        fun getHeaderPositionForItem(itemPosition: Int): Int

        /**
         * @return Layout resource id for header items
         */
        @get:LayoutRes
        val headerLayout: Int

        /**
         * Used to bind data to header view at position headerPosition
         */
        fun bindHeaderData(header: View, headerPosition: Int)

        /**
         * @return True if item at position itemPosition is a header item
         */
        fun isHeader(itemPosition: Int): Boolean
    }
}
