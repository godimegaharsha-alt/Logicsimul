package com.example.logiclab.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.ComponentDefinition
import kotlin.math.max
import kotlin.math.min

/**
 * Clipping-aware frustum culling and Level-Of-Detail (LOD) system for the infinite canvas.
 * Drastically improves frame rendering performance on complex logic circuits
 * by discarding off-screen components and wires, and simplifying micro-details when zoomed out.
 */
object ViewportCullingSystem {

    /**
     * Metrics for active viewport culling performance.
     */
    data class CullingStats(
        val visibleComponents: Int,
        val totalComponents: Int,
        val visibleWires: Int,
        val totalWires: Int,
        val isLODSimplified: Boolean
    ) {
        val culledComponents: Int get() = totalComponents - visibleComponents
        val culledWires: Int get() = totalWires - visibleWires
        val cullingEfficiencyPercent: Float
            get() {
                val total = totalComponents + totalWires
                if (total == 0) return 0f
                val culled = culledComponents + culledWires
                return (culled.toFloat() / total) * 100f
            }
    }

    /**
     * Calculates the world coordinates viewport rectangle visible on screen,
     * including an extra safety margin to prevent pop-in artifacts during fast pans.
     */
    fun calculateVisibleWorldBounds(
        canvasWidth: Float,
        canvasHeight: Float,
        panOffset: Offset,
        zoomScale: Float,
        margin: Float = 60f
    ): Rect {
        val safeZoom = max(0.1f, zoomScale)
        val left = (-panOffset.x - margin) / safeZoom
        val top = (-panOffset.y - margin) / safeZoom
        val right = (canvasWidth - panOffset.x + margin) / safeZoom
        val bottom = (canvasHeight - panOffset.y + margin) / safeZoom
        return Rect(left, top, right, bottom)
    }

    /**
     * Checks if a component's bounding box intersects the visible world rectangle.
     */
    fun isComponentVisible(
        comp: CircuitComponent,
        def: ComponentDefinition,
        visibleBounds: Rect
    ): Boolean {
        val compLeft = comp.x
        val compTop = comp.y
        val compRight = comp.x + def.width
        val compBottom = comp.y + def.height

        return compRight >= visibleBounds.left &&
               compLeft <= visibleBounds.right &&
               compBottom >= visibleBounds.top &&
               compTop <= visibleBounds.bottom
    }

    /**
     * Checks if a wire's bounding box intersects the visible world rectangle.
     */
    fun isWireVisible(
        p1: Offset,
        p2: Offset,
        visibleBounds: Rect,
        wirePadding: Float = 20f
    ): Boolean {
        val wireLeft = min(p1.x, p2.x) - wirePadding
        val wireRight = max(p1.x, p2.x) + wirePadding
        val wireTop = min(p1.y, p2.y) - wirePadding
        val wireBottom = max(p1.y, p2.y) + wirePadding

        return wireRight >= visibleBounds.left &&
               wireLeft <= visibleBounds.right &&
               wireBottom >= visibleBounds.top &&
               wireTop <= visibleBounds.bottom
    }

    /**
     * Determines whether micro text labels (like individual port names and pin indices)
     * should be rendered or culled based on zoom level to reduce font rasterization load.
     */
    fun shouldRenderPortLabels(zoomScale: Float): Boolean {
        return zoomScale >= 0.55f
    }

    /**
     * Determines whether component title headers should be rendered.
     */
    fun shouldRenderComponentLabels(zoomScale: Float): Boolean {
        return zoomScale >= 0.35f
    }
}
