package com.aoritian.playbackrain

import kotlin.test.Test
import kotlin.test.assertNotNull

class ShaderCompileTest {
    @Test
    fun shaderCompiles() {
        assertNotNull(RainShader.compileEffect())
    }
}
