package com.example.logiclab.gl

import android.opengl.GLES20
import android.util.Log

/**
 * OpenGL ES Shader utility functions for compiling and linking GLSL programs.
 */
object GLShaderUtils {
    private const val TAG = "GLShaderUtils"

    const val VERTEX_SHADER_PHONG = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        uniform mat3 uNormalMatrix;
        uniform vec4 uColor;
        
        attribute vec4 aPosition;
        attribute vec3 aNormal;
        
        varying vec3 vPosition;
        varying vec3 vNormal;
        varying vec4 vColor;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vPosition = vec3(uModelMatrix * aPosition);
            vNormal = normalize(uNormalMatrix * aNormal);
            vColor = uColor;
        }
    """

    const val FRAGMENT_SHADER_PHONG = """
        precision mediump float;
        
        varying vec3 vPosition;
        varying vec3 vNormal;
        varying vec4 vColor;
        
        uniform vec3 uLightPos;
        uniform vec3 uViewPos;
        uniform float uEmission;
        uniform float uAmbientStrength;
        uniform float uSpecularStrength;
        
        void main() {
            vec3 normal = normalize(vNormal);
            vec3 lightDir = normalize(uLightPos - vPosition);
            
            // Ambient component
            vec3 ambient = uAmbientStrength * vColor.rgb;
            
            // Diffuse component
            float diff = max(dot(normal, lightDir), 0.0);
            vec3 diffuse = diff * vColor.rgb * 0.75;
            
            // Specular component
            vec3 viewDir = normalize(uViewPos - vPosition);
            vec3 reflectDir = reflect(-lightDir, normal);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16.0);
            vec3 specular = uSpecularStrength * spec * vec3(1.0, 1.0, 1.0);
            
            // Emission (for active LEDs, glowing wires, and signals)
            vec3 emissive = uEmission * vColor.rgb;
            
            vec3 result = ambient + diffuse + specular + emissive;
            gl_FragColor = vec4(result, vColor.a);
        }
    """

    const val VERTEX_SHADER_UNLIT = """
        uniform mat4 uMVPMatrix;
        uniform vec4 uColor;
        attribute vec4 aPosition;
        varying vec4 vColor;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vColor = uColor;
        }
    """

    const val FRAGMENT_SHADER_UNLIT = """
        precision mediump float;
        varying vec4 vColor;
        uniform float uAlphaMultiplier;
        
        void main() {
            gl_FragColor = vec4(vColor.rgb, vColor.a * uAlphaMultiplier);
        }
    """

    const val VERTEX_SHADER_POINT = """
        uniform mat4 uMVPMatrix;
        uniform float uPointSize;
        uniform vec4 uColor;
        attribute vec4 aPosition;
        varying vec4 vColor;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            gl_PointSize = uPointSize;
            vColor = uColor;
        }
    """

    const val FRAGMENT_SHADER_POINT = """
        precision mediump float;
        varying vec4 vColor;
        
        void main() {
            // Render smooth antialiased circular dots
            vec2 coord = gl_PointCoord - vec2(0.5);
            float dist = length(coord);
            if (dist > 0.5) {
                discard;
            }
            float alpha = smoothstep(0.5, 0.35, dist) * vColor.a;
            gl_FragColor = vec4(vColor.rgb, alpha);
        }
    """

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "Error compiling shader (type $type): $error")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) return 0

        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            GLES20.glDeleteShader(vertexShader)
            return 0
        }

        val program = GLES20.glCreateProgram()
        if (program == 0) return 0

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(program)
            Log.e(TAG, "Error linking program: $error")
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }
}
