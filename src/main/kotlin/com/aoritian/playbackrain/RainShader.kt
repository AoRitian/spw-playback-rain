package com.aoritian.playbackrain

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Shader

private const val RANDOM_SEED = 4.3315f

private val ORTHONORMAL_MAP = floatArrayOf(
    0.788675134594813f, -0.211324865405187f, -0.577350269189626f,
    -0.211324865405187f, 0.788675134594813f, -0.577350269189626f,
    0.577350269189626f, 0.577350269189626f, 0.577350269189626f
)

object RainShader {
    private val effect: RuntimeEffect? by lazy {
        try {
            RuntimeEffect.makeForShader(SKSQL)
        } catch (failure: Throwable) {
            PluginLog.log(
                "rain shader compile failed: ${failure.javaClass.name}: ${failure.message}"
            )
            null
        }
    }

    internal fun compileEffect(): RuntimeEffect? = effect

    fun draw(scope: DrawScope, settings: RainSettings, timeSeconds: Float) {
        val runtimeEffect = effect
        if (runtimeEffect == null) {
            drawFallback(scope, settings, timeSeconds)
            return
        }
        val width = scope.size.width.coerceAtLeast(1f)
        val height = scope.size.height.coerceAtLeast(1f)
        var shader: Shader? = null
        var paint: Paint? = null
        try {
            val builder = RuntimeShaderBuilder(runtimeEffect)
            builder.uniform("uTime", timeSeconds)
            builder.uniform("uResolution", width, height)
            builder.uniform("uSpeed", settings.speed)
            builder.uniform("uSize", settings.size)
            builder.uniform("uDensity", settings.density)
            builder.uniform("uLightAngle", Math.toRadians(settings.lightAngle.toDouble()).toFloat())
            builder.uniform("uShadow", settings.shadow)
            shader = builder.makeShader()
            paint = Paint().apply {
                this.shader = shader
                isAntiAlias = false
            }
            scope.drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRect(Rect.makeWH(width, height), paint)
            }
        } catch (failure: Throwable) {
            PluginLog.log(
                "rain shader draw failed: ${failure.javaClass.name}: ${failure.message}"
            )
            drawFallback(scope, settings, timeSeconds)
        } finally {
            paint?.close()
            shader?.close()
        }
    }

    private fun drawFallback(scope: DrawScope, settings: RainSettings, timeSeconds: Float) {
        val width = scope.size.width
        val height = scope.size.height
        val count = (36 + 64 * settings.density).toInt().coerceIn(12, 100)
        val color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.24f)
        for (i in 0 until count) {
            val seed = i * 0.6180339f
            val speed = 0.06f + (i % 7) * 0.012f
            val x = ((seed * 7.13f) % 1f) * width
            val y = ((timeSeconds * speed * settings.speed + seed * 3.17f) % 1f) * height
            val length = (6f + (i % 5) * 3f) * settings.size
            scope.drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(x, y),
                end = androidx.compose.ui.geometry.Offset(x - length * 0.12f, y + length),
                strokeWidth = 1.2f
            )
        }
    }
}

private val SKSQL = """
uniform float uTime;
uniform float2 uResolution;
uniform float uSpeed;
uniform float uSize;
uniform float uDensity;
uniform float uLightAngle;
uniform float uShadow;

const float PI = 3.141592653589793;

float4 permute(float4 t) {
    return t * (t * 34.0 + 133.0);
}

float3 grad(float hash) {
    float3 cube = mod(floor(hash / float3(1.0, 2.0, 4.0)), float3(2.0)) * 2.0 - 1.0;
    float3 cuboct = cube;
    int index = int(hash / 16.0);
    if (index == 0) cuboct.x = 0.0;
    else if (index == 1) cuboct.y = 0.0;
    else cuboct.z = 0.0;
    float dropType = mod(floor(hash / 8.0), 2.0);
    float3 rhomb = (1.0 - dropType) * cube + dropType * (cuboct + cross(cube, cuboct));
    float3 g = cuboct * 1.22474487139 + rhomb;
    g *= (1.0 - 0.042942436724648037 * dropType) * 3.5946317686139184;
    return g;
}

float4 os2NoiseWithDerivativesPart(float3 x) {
    float3 b = floor(x);
    float4 i4 = float4(x - b, 2.5);
    float3 v1 = b + floor(dot(i4, float4(0.25)));
    float3 v2 = b + float3(1.0, 0.0, 0.0) + float3(-1.0, 1.0, 1.0) *
        floor(dot(i4, float4(-0.25, 0.25, 0.25, 0.35)));
    float3 v3 = b + float3(0.0, 1.0, 0.0) + float3(1.0, -1.0, 1.0) *
        floor(dot(i4, float4(0.25, -0.25, 0.25, 0.35)));
    float3 v4 = b + float3(0.0, 0.0, 1.0) + float3(1.0, 1.0, -1.0) *
        floor(dot(i4, float4(0.25, 0.25, -0.25, 0.35)));
    float4 hashes = permute(mod(float4(v1.x, v2.x, v3.x, v4.x), 289.0));
    hashes = permute(mod(hashes + float4(v1.y, v2.y, v3.y, v4.y), 289.0));
    hashes = mod(permute(mod(hashes + float4(v1.z, v2.z, v3.z, v4.z), 289.0)), 48.0);
    float3 d1 = x - v1;
    float3 d2 = x - v2;
    float3 d3 = x - v3;
    float3 d4 = x - v4;
    float4 a = max(0.75 - float4(dot(d1, d1), dot(d2, d2), dot(d3, d3), dot(d4, d4)), 0.0);
    float4 aa = a * a;
    float4 aaaa = aa * aa;
    float3 g1 = grad(hashes.x);
    float3 g2 = grad(hashes.y);
    float3 g3 = grad(hashes.z);
    float3 g4 = grad(hashes.w);
    float4 extrapolations = float4(dot(d1, g1), dot(d2, g2), dot(d3, g3), dot(d4, g4));
    float4 weighted = aa * a * extrapolations;
    float3 m1 = float3(
        dot(float4(d1.x, d2.x, d3.x, d4.x), weighted),
        dot(float4(d1.y, d2.y, d3.y, d4.y), weighted),
        dot(float4(d1.z, d2.z, d3.z, d4.z), weighted)
    );
    float3 m2 = float3(
        dot(float4(g1.x, g2.x, g3.x, g4.x), aaaa),
        dot(float4(g1.y, g2.y, g3.y, g4.y), aaaa),
        dot(float4(g1.z, g2.z, g3.z, g4.z), aaaa)
    );
    float3 derivative = -8.0 * m1 + m2;
    return float4(derivative, dot(aaaa, extrapolations));
}

float4 os2NoiseWithDerivativesImproveXy(float3 x) {
    x = float3x3(
        0.788675134594813, -0.211324865405187, -0.577350269189626,
        -0.211324865405187, 0.788675134594813, -0.577350269189626,
        0.577350269189626, 0.577350269189626, 0.577350269189626
    ) * x;
    float4 result = os2NoiseWithDerivativesPart(x) + os2NoiseWithDerivativesPart(x + 144.5);
    result.xyz = float3x3(
        0.788675134594813, -0.211324865405187, -0.577350269189626,
        -0.211324865405187, 0.788675134594813, -0.577350269189626,
        0.577350269189626, 0.577350269189626, 0.577350269189626
    ) * result.xyz;
    return result;
}

float gradientWave(float b, float t) {
    return smoothstep(0.0, b, t) * smoothstep(1.0, b, t);
}

float random(float2 uv, float seed) {
    return fract(sin(dot(uv * 13.235, float2(12.9898, 78.233)) * 0.000001) * 43758.5453123 * seed);
}

float3 randomVec3(float2 uv, float seed) {
    return float3(random(uv, seed), random(uv * 2.0, seed), random(uv * 3.0, seed));
}

float mapToRange(float edge0, float edge1, float x) {
    return clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
}

float proportionalMapToRange(float edge0, float edge1, float x) {
    return edge0 + (edge1 - edge0) * x;
}

float3 raindropSurface(float2 xy, float distanceScale, float zScale) {
    float a = distanceScale;
    float x = xy.x;
    float y = xy.y;
    float n = 1.5;
    float m = 0.5;
    float s = zScale;
    float tempZ = 1.0 - pow(x / a, 2.0) - pow(y / a, 2.0);
    float z = pow(max(0.0, tempZ), a / 2.0);
    float zInMAndN = (z - m) / (n - m);
    float t = clamp(zInMAndN, 0.0, 1.0);
    float height = s * t * t * (3.0 - 2.0 * t);
    float part01 = s * (6.0 * t - 8.0 * t * t);
    float part02 = 1.0 / (n - m);
    float part03 = -1.0 / a * pow(max(0.0, tempZ), a / 2.0 - 1.0);
    float tempValue = (zInMAndN > 0.0 && zInMAndN < 1.0) ? part01 * part02 : 0.0;
    float2 partialDerivative = height > 0.0
        ? float2(tempValue * (x * part03), tempValue * (y * part03))
        : float2(0.0);
    return float3(height, partialDerivative);
}

float3 staticRaindrops(float2 uv, float t, float uvScale, float density) {
    float2 tempUv = uv * uvScale;
    float2 id = floor(tempUv);
    float3 randomValue = randomVec3(float2(id.x * 470.15, id.y * 653.58), 4.3315);
    tempUv = fract(tempUv) - 0.5;
    float2 randomPoint = (randomValue.xy - 0.5) * 0.25;
    float2 xy = randomPoint - tempUv;
    float distance = length(tempUv - randomPoint);
    float3 noiseInput = float3(float2(tempUv.x * 305.0 * 0.02, tempUv.y * 305.0 * 0.02), 1.8660254037844386);
    float4 noiseResult = os2NoiseWithDerivativesImproveXy(noiseInput);
    float edgeRandomCurveAdjust = noiseResult.w * mix(0.02, 0.175, fract(randomValue.x));
    distance = edgeRandomCurveAdjust * 0.5 + distance;
    distance = distance * clamp(mix(1.0, 55.0, randomPoint.x), 1.0, 3.0);
    float gradientFade = gradientWave(0.0005, fract(t * 0.02 + randomValue.z));
    float distanceMaxRange = 1.45 * gradientFade;
    float2 direction = tempUv - randomPoint;
    float theta = PI - acos(dot(normalize(direction), float2(0.0, 1.0)));
    theta = theta * randomValue.z;
    float distanceScale = 0.2 / (1.0 - 0.8 * cos(theta - PI / 2.0 - 1.6));
    float yDistance = length(float2(0.0, tempUv.y) - float2(0.0, randomPoint.y));
    float scale = 1.65 * (0.2 + distanceScale) * distanceMaxRange * mix(1.5, 0.5, randomValue.x);
    float2 tempXy = float2(xy.x, xy.y) * 4.0;
    float randomScale = proportionalMapToRange(0.85, 1.35, randomValue.z);
    tempXy.x = randomScale * mix(tempXy.x, tempXy.x / smoothstep(1.0, 0.4, yDistance * randomValue.z), smoothstep(1.0, 0.0, randomValue.x));
    tempXy = tempXy + edgeRandomCurveAdjust;
    float3 heightAndNormal = raindropSurface(tempXy, scale, 1.0);
    heightAndNormal.yz = -heightAndNormal.yz;
    float randomVisible = fract(randomValue.z * 10.0 * 4.3315) < density ? 1.0 : 0.0;
    heightAndNormal.yz = heightAndNormal.yz * randomVisible;
    heightAndNormal.x = smoothstep(0.0, 1.0, heightAndNormal.x) * randomVisible;
    return heightAndNormal;
}

float4 rollingRaindrops(float2 uv, float t, float uvScale, float density) {
    float2 localUv = uv * uvScale;
    float2 tempUv = localUv;
    float2 constantA = float2(6.0, 1.0);
    float2 gridNum = constantA * 2.0;
    float2 gridId = floor(localUv * gridNum);
    float randomFloat = random(float2(gridId.x * 131.26, gridId.x * 101.81), 4.3315);
    float timeMovingY = t * 0.85 * proportionalMapToRange(0.1, 0.25, randomFloat);
    localUv.y += timeMovingY + randomFloat;
    float2 scaledUv = localUv * gridNum;
    gridId = floor(scaledUv);
    float3 randomVec3Value = randomVec3(float2(gridId.x * 17.32, gridId.y * 2217.54), 4.3315);
    float2 gridUv = fract(scaledUv) - float2(0.5, 0.0);
    float swingX = randomVec3Value.x - 0.5;
    float swingY = tempUv.y * 20.0;
    float swingPosition = sin(swingY + sin(gridId.y * randomVec3Value.z + swingY) + gridId.y * randomVec3Value.z);
    swingX += swingPosition * (0.5 - abs(swingX)) * (randomVec3Value.z - 0.5);
    swingX *= 0.65;
    float randomNormalizedTime = fract(timeMovingY + randomVec3Value.z);
    swingY = clamp((gradientWave(0.87, randomNormalizedTime) - 0.5) * 0.9 + 0.5, 0.15, 0.85);
    float2 position = float2(swingX, swingY);
    float2 xy = position - gridUv;
    float2 direction = (gridUv - position) * constantA.yx;
    float distance = length(direction);
    float3 noiseInput = float3(float2(tempUv.x * 513.20 * 0.02, tempUv.y * 779.40 * 0.02), 2.1660251037743386);
    float4 noiseResult = os2NoiseWithDerivativesImproveXy(noiseInput);
    float edgeRandomCurveAdjust = noiseResult.w * mix(0.02, 0.175, fract(randomVec3Value.y));
    distance = edgeRandomCurveAdjust + distance;
    float theta = PI - acos(dot(normalize(direction), float2(0.0, 1.0)));
    theta = theta * randomVec3Value.z;
    float distanceScale = 0.2 / (1.0 - 0.8 * cos(theta - PI / 2.0 - 1.6));
    float scale = 1.65 * (0.2 + distanceScale) * 1.45 * mix(1.0, 0.25, randomVec3Value.x);
    float2 tempXy = float2(xy.x, xy.y) * 4.0;
    tempXy = tempXy * float2(1.0, 4.2) + edgeRandomCurveAdjust * 0.85;
    float3 heightAndNormal = raindropSurface(tempXy, scale, 1.0);
    float trailY = pow(smoothstep(1.0, swingY, gridUv.y), 0.5);
    float trailX = abs(gridUv.x - swingX) * mix(0.8, 4.0, smoothstep(0.0, 1.0, randomVec3Value.x));
    float trail = smoothstep(0.25 * trailY, 0.15 * trailY * trailY, trailX);
    float trailClamp = smoothstep(-0.02, 0.02, gridUv.y - swingY);
    trail *= trailClamp * trailY;
    float signOfTrailX = sign(gridUv.x - swingX);
    float3 trailNoiseInput = float3(float2(tempUv.x * 513.20 * 0.02 * signOfTrailX, tempUv.y * 779.40 * 0.02), 2.1660251037743386);
    float4 trailNoiseResult = os2NoiseWithDerivativesImproveXy(trailNoiseInput);
    float trailEdgeRandomCurveAdjust = trailNoiseResult.w * mix(0.002, 0.175, fract(randomVec3Value.y));
    float trailXDistance = mapToRange(0.0, 0.1, trailEdgeRandomCurveAdjust * 0.5 + trailX);
    float2 trailDirection = signOfTrailX * float2(1.0, 0.0) + float2(0.0, 1.0) * smoothstep(1.0, 0.0, trail) * 0.5;
    float2 trailXy = trailDirection * trailXDistance;
    float3 trailHeightAndNormal = raindropSurface(trailXy, 1.0, 1.0);
    trailHeightAndNormal *= pow(trail * randomVec3Value.y, 2.0);
    trailHeightAndNormal.x = smoothstep(0.0, 1.0, trailHeightAndNormal.x);
    swingY = tempUv.y;
    float remainTrail = smoothstep(0.2 * trailY, 0.0, trailX);
    float remainDroplet = max(0.0, sin(swingY * (1.0 - swingY) * 120.0) - gridUv.y) * remainTrail * trailClamp * randomVec3Value.z;
    swingY = fract(swingY * 10.0) + (gridUv.y - 0.5);
    float2 remainDropletXy = gridUv - float2(swingX, swingY);
    remainDropletXy = remainDropletXy * float2(1.2, 0.8) + edgeRandomCurveAdjust * 0.85;
    float3 remainDropletHeightAndNormal = raindropSurface(remainDropletXy, 2.0 * remainDroplet, 1.0);
    remainDropletHeightAndNormal.x = smoothstep(0.0, 1.0, remainDropletHeightAndNormal.x);
    remainDropletHeightAndNormal = trailHeightAndNormal.x > 0.0 ? float3(0.0) : remainDropletHeightAndNormal;
    float4 returnValue;
    returnValue.x = heightAndNormal.x + trailHeightAndNormal.x * trailY * trailClamp + remainDropletHeightAndNormal.x * trailY * trailClamp;
    returnValue.yz = heightAndNormal.yz + trailHeightAndNormal.yz + remainDropletHeightAndNormal.yz;
    returnValue.w = trail;
    float randomVisible = fract(randomVec3Value.z * 20.0 * 4.3315) < density ? 1.0 : 0.0;
    returnValue *= randomVisible;
    return returnValue;
}

float4 raindrops(float2 uv, float t, float staticScale, float rollingScale, float density) {
    float4 staticRaindrop = float4(staticRaindrops(uv, t, staticScale, density), 0.0);
    float4 rollingRaindrop = rollingRaindrops(uv, t, rollingScale, density);
    float height = staticRaindrop.x + rollingRaindrop.x;
    float2 normal = staticRaindrop.yz + rollingRaindrop.yz;
    float trail = rollingRaindrop.w;
    return float4(height, normal, trail);
}

half4 main(float2 fragCoord) {
    float scaledTime = uTime * uSpeed;
    float2 scenePos = float2(fragCoord.x, uResolution.y - fragCoord.y);
    float2 localUv = (scenePos - 0.5 * uResolution) / uResolution.y;
    float staticUvScale = 20.0 / max(0.1, uSize);
    float rollingUvScale = 2.25 / max(0.1, uSize);
    float4 raindrop = raindrops(localUv, scaledTime, staticUvScale, rollingUvScale, uDensity);
    float height = raindrop.x;
    float2 normal = raindrop.yz;
    float dropMask = smoothstep(0.02, 0.15, height);
    float lightX = cos(uLightAngle);
    float lightY = sin(uLightAngle);
    float3 lightDir = normalize(float3(lightX, lightY, 1.5));
    float3 surfaceNormal = normalize(float3(normal.x, normal.y, 1.0 - height));
    float specular = max(0.0, dot(surfaceNormal, lightDir));
    specular = pow(specular, 24.0);
    float edgeShadow = smoothstep(0.2, 1.0, length(normal)) * uShadow;
    float baseBrightness = clamp(0.15 * height + specular - edgeShadow, 0.0, 1.0);
    float alpha = dropMask * clamp(0.25 + specular, 0.0, 1.0);
    return half4(baseBrightness, baseBrightness, baseBrightness, alpha);
}
"""
