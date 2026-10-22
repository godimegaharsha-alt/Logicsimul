package com.example.logiclab.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native buffers and 3D geometry builders for the OpenGL circuit visualization module.
 */
object GLGeometry {

    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(coords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(coords)
                position(0)
            }
        }
    }

    fun createShortBuffer(indices: ShortArray): ShortBuffer {
        return ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }
    }

    /**
     * Standard 3D Unit Box centered at (0,0,0) from -0.5 to +0.5 on each axis.
     * Contains 24 vertices (4 per face) with normals and default vertex colors.
     */
    class UnitBoxMesh {
        val vertexBuffer: FloatBuffer
        val normalBuffer: FloatBuffer
        val indexBuffer: ShortBuffer
        val indexCount: Int = 36

        init {
            val positions = floatArrayOf(
                // Front face (+Z)
                -0.5f, -0.5f,  0.5f,   0.5f, -0.5f,  0.5f,   0.5f,  0.5f,  0.5f,  -0.5f,  0.5f,  0.5f,
                // Back face (-Z)
                 0.5f, -0.5f, -0.5f,  -0.5f, -0.5f, -0.5f,  -0.5f,  0.5f, -0.5f,   0.5f,  0.5f, -0.5f,
                // Top face (+Y)
                -0.5f,  0.5f,  0.5f,   0.5f,  0.5f,  0.5f,   0.5f,  0.5f, -0.5f,  -0.5f,  0.5f, -0.5f,
                // Bottom face (-Y)
                -0.5f, -0.5f, -0.5f,   0.5f, -0.5f, -0.5f,   0.5f, -0.5f,  0.5f,  -0.5f, -0.5f,  0.5f,
                // Right face (+X)
                 0.5f, -0.5f,  0.5f,   0.5f, -0.5f, -0.5f,   0.5f,  0.5f, -0.5f,   0.5f,  0.5f,  0.5f,
                // Left face (-X)
                -0.5f, -0.5f, -0.5f,  -0.5f, -0.5f,  0.5f,  -0.5f,  0.5f,  0.5f,  -0.5f,  0.5f, -0.5f
            )

            val normals = floatArrayOf(
                // Front
                 0f,  0f,  1f,   0f,  0f,  1f,   0f,  0f,  1f,   0f,  0f,  1f,
                // Back
                 0f,  0f, -1f,   0f,  0f, -1f,   0f,  0f, -1f,   0f,  0f, -1f,
                // Top
                 0f,  1f,  0f,   0f,  1f,  0f,   0f,  1f,  0f,   0f,  1f,  0f,
                // Bottom
                 0f, -1f,  0f,   0f, -1f,  0f,   0f, -1f,  0f,   0f, -1f,  0f,
                // Right
                 1f,  0f,  0f,   1f,  0f,  0f,   1f,  0f,  0f,   1f,  0f,  0f,
                // Left
                -1f,  0f,  0f,  -1f,  0f,  0f,  -1f,  0f,  0f,  -1f,  0f,  0f
            )

            val indices = shortArrayOf(
                 0,  1,  2,      0,  2,  3,    // front
                 4,  5,  6,      4,  6,  7,    // back
                 8,  9, 10,      8, 10, 11,    // top
                12, 13, 14,     12, 14, 15,    // bottom
                16, 17, 18,     16, 18, 19,    // right
                20, 21, 22,     20, 22, 23     // left
            )

            vertexBuffer = createFloatBuffer(positions)
            normalBuffer = createFloatBuffer(normals)
            indexBuffer = createShortBuffer(indices)
        }
    }

    /**
     * 3D Dome / Hemisphere mesh for LED lights.
     */
    class DomeMesh(val segments: Int = 16, val rings: Int = 8) {
        val vertexBuffer: FloatBuffer
        val normalBuffer: FloatBuffer
        val indexBuffer: ShortBuffer
        val indexCount: Int

        init {
            val vertexList = mutableListOf<Float>()
            val normalList = mutableListOf<Float>()
            val indexList = mutableListOf<Short>()

            for (r in 0..rings) {
                val phi = (PI / 2.0) * (r.toDouble() / rings)
                val y = sin(phi).toFloat()
                val radiusRing = cos(phi).toFloat()

                for (s in 0..segments) {
                    val theta = (2.0 * PI) * (s.toDouble() / segments)
                    val x = (radiusRing * cos(theta)).toFloat()
                    val z = (radiusRing * sin(theta)).toFloat()

                    vertexList.add(x)
                    vertexList.add(y)
                    vertexList.add(z)

                    // For unit sphere/dome, normal is position vector
                    normalList.add(x)
                    normalList.add(y)
                    normalList.add(z)
                }
            }

            val ringStride = segments + 1
            for (r in 0 until rings) {
                for (s in 0 until segments) {
                    val current = (r * ringStride + s).toShort()
                    val next = (current + 1).toShort()
                    val bottom = ((r + 1) * ringStride + s).toShort()
                    val bottomNext = (bottom + 1).toShort()

                    indexList.add(current)
                    indexList.add(bottom)
                    indexList.add(next)

                    indexList.add(next)
                    indexList.add(bottom)
                    indexList.add(bottomNext)
                }
            }

            indexCount = indexList.size
            vertexBuffer = createFloatBuffer(vertexList.toFloatArray())
            normalBuffer = createFloatBuffer(normalList.toFloatArray())
            indexBuffer = createShortBuffer(indexList.toShortArray())
        }
    }

    /**
     * 3D Cylinder mesh centered at origin along the Y-axis.
     * Radius 0.5f, height 1.0f (-0.5 to +0.5).
     */
    class CylinderMesh(val segments: Int = 16) {
        val vertexBuffer: FloatBuffer
        val normalBuffer: FloatBuffer
        val indexBuffer: ShortBuffer
        val indexCount: Int

        init {
            val vertexList = mutableListOf<Float>()
            val normalList = mutableListOf<Float>()
            val indexList = mutableListOf<Short>()

            // Top and bottom rings for the side surface
            for (i in 0..segments) {
                val theta = (2.0 * PI) * (i.toDouble() / segments)
                val cosT = cos(theta).toFloat() * 0.5f
                val sinT = sin(theta).toFloat() * 0.5f
                val nx = cos(theta).toFloat()
                val nz = sin(theta).toFloat()

                // Top vertex (+Y)
                vertexList.add(cosT); vertexList.add(0.5f); vertexList.add(sinT)
                normalList.add(nx); normalList.add(0f); normalList.add(nz)

                // Bottom vertex (-Y)
                vertexList.add(cosT); vertexList.add(-0.5f); vertexList.add(sinT)
                normalList.add(nx); normalList.add(0f); normalList.add(nz)
            }

            // Side indices
            for (i in 0 until segments) {
                val topLeft = (i * 2).toShort()
                val botLeft = (i * 2 + 1).toShort()
                val topRight = (i * 2 + 2).toShort()
                val botRight = (i * 2 + 3).toShort()

                indexList.add(topLeft); indexList.add(botLeft); indexList.add(topRight)
                indexList.add(topRight); indexList.add(botLeft); indexList.add(botRight)
            }

            // Top cap center vertex
            val topCenterIndex = (vertexList.size / 3).toShort()
            vertexList.add(0f); vertexList.add(0.5f); vertexList.add(0f)
            normalList.add(0f); normalList.add(1f); normalList.add(0f)

            // Top cap perimeter vertices
            val topPerimeterStart = (vertexList.size / 3).toShort()
            for (i in 0..segments) {
                val theta = (2.0 * PI) * (i.toDouble() / segments)
                vertexList.add(cos(theta).toFloat() * 0.5f)
                vertexList.add(0.5f)
                vertexList.add(sin(theta).toFloat() * 0.5f)
                normalList.add(0f); normalList.add(1f); normalList.add(0f)
            }
            for (i in 0 until segments) {
                indexList.add(topCenterIndex)
                indexList.add((topPerimeterStart + i).toShort())
                indexList.add((topPerimeterStart + i + 1).toShort())
            }

            // Bottom cap center vertex
            val botCenterIndex = (vertexList.size / 3).toShort()
            vertexList.add(0f); vertexList.add(-0.5f); vertexList.add(0f)
            normalList.add(0f); normalList.add(-1f); normalList.add(0f)

            // Bottom cap perimeter vertices
            val botPerimeterStart = (vertexList.size / 3).toShort()
            for (i in 0..segments) {
                val theta = (2.0 * PI) * (i.toDouble() / segments)
                vertexList.add(cos(theta).toFloat() * 0.5f)
                vertexList.add(-0.5f)
                vertexList.add(sin(theta).toFloat() * 0.5f)
                normalList.add(0f); normalList.add(-1f); normalList.add(0f)
            }
            for (i in 0 until segments) {
                indexList.add(botCenterIndex)
                indexList.add((botPerimeterStart + i + 1).toShort())
                indexList.add((botPerimeterStart + i).toShort())
            }

            indexCount = indexList.size
            vertexBuffer = createFloatBuffer(vertexList.toFloatArray())
            normalBuffer = createFloatBuffer(normalList.toFloatArray())
            indexBuffer = createShortBuffer(indexList.toShortArray())
        }
    }

    /**
     * Calculates 3x3 normal matrix from 4x4 model matrix.
     */
    fun computeNormalMatrix(modelMatrix: FloatArray, outNormalMatrix: FloatArray) {
        outNormalMatrix[0] = modelMatrix[0]
        outNormalMatrix[1] = modelMatrix[1]
        outNormalMatrix[2] = modelMatrix[2]
        outNormalMatrix[3] = modelMatrix[4]
        outNormalMatrix[4] = modelMatrix[5]
        outNormalMatrix[5] = modelMatrix[6]
        outNormalMatrix[6] = modelMatrix[8]
        outNormalMatrix[7] = modelMatrix[9]
        outNormalMatrix[8] = modelMatrix[10]
    }
}
