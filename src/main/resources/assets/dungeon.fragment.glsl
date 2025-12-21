#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef specularFlag
#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif
#endif // specularFlag

#ifdef normalFlag
varying vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
varying MED vec2 v_emissiveUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if numSpotLights > 0
varying vec3 v_worldPos;

struct SpotLight {
    vec3  color;
    vec3  position;
    vec3  direction;    // direction the cone points (world space)
    float intensity;    // distance attenuation scale
    float cutoffAngle;  // degrees
    float exponent;     // focus
};
uniform SpotLight u_spotLights[numSpotLights];

#ifdef specularFlag
uniform vec4 u_cameraPosition;
#endif // specularFlag
#endif // numSpotLights

#if defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif //specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));
}

float getShadow()
{
    return (
        getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
        getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
        getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
        getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))
    ) * 0.25;
}
#endif //shadowMapFlag

// always separate ambient when we have ambient.
#if defined(ambientFlag) && !defined(separateAmbientFlag)
#define separateAmbientFlag
#endif

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif // fogFlag

void main() {
    #if defined(normalFlag)
        vec3 normal = v_normal;
    #endif // normalFlag

    #if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
        vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
    #elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
        vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
    #elif defined(diffuseTextureFlag) && defined(colorFlag)
        vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
    #elif defined(diffuseTextureFlag)
        vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
    #elif defined(diffuseColorFlag) && defined(colorFlag)
        vec4 diffuse = u_diffuseColor * v_color;
    #elif defined(diffuseColorFlag)
        vec4 diffuse = u_diffuseColor;
    #elif defined(colorFlag)
        vec4 diffuse = v_color;
    #else
        vec4 diffuse = vec4(1.0);
    #endif

    #if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
        vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV) * u_emissiveColor;
    #elif defined(emissiveTextureFlag)
        vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV);
    #elif defined(emissiveColorFlag)
        vec4 emissive = u_emissiveColor;
    #else
        vec4 emissive = vec4(0.0);
    #endif

#ifdef lightingFlag
    vec3 spotDiffuse = vec3(0.0);
    #ifdef specularFlag
        vec3 spotSpecular = vec3(0.0);
    #endif // specularFlag

    #if (numSpotLights > 0) && defined(normalFlag)
        vec3 N = normalize(normal);
        #ifdef specularFlag
            vec3 V = normalize(u_cameraPosition.xyz - v_worldPos);
        #endif // specularFlag

        for (int i = 0; i < numSpotLights; i++) {
            vec3 lightToFrag = v_worldPos - u_spotLights[i].position;
            float dist2 = dot(lightToFrag, lightToFrag);
            vec3 L = lightToFrag * inversesqrt(max(dist2, 1e-6)); // light -> frag

            float cosTheta  = dot(normalize(u_spotLights[i].direction), L);
            float cosCutoff = cos(radians(u_spotLights[i].cutoffAngle));

            if (cosTheta > cosCutoff) {
                float spotFactor = pow(cosTheta, u_spotLights[i].exponent);
                float att = u_spotLights[i].intensity / (1.0 + dist2);

                vec3 fragToLight = -L;
                float NdotL = max(dot(N, fragToLight), 0.0);

                if (NdotL > 0.0) {
                    vec3 value = u_spotLights[i].color * (NdotL * att * spotFactor);
                    spotDiffuse += value;

                    #ifdef specularFlag
                        vec3 H = normalize(fragToLight + V);
                        float halfDotView = max(0.0, dot(N, H));
                        spotSpecular += value * pow(halfDotView, u_shininess);
                    #endif // specularFlag
                }
            }
        }
    #endif // (numSpotLights > 0) && defined(normalFlag)
#endif // lightingFlag

    #if (!defined(lightingFlag))
        gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    #elif (!defined(specularFlag))
        #if defined(ambientFlag) && defined(separateAmbientFlag)
            #ifdef shadowMapFlag
                vec3 direct = diffuse.rgb * (getShadow() * v_lightDiffuse + spotDiffuse);
            #else
                vec3 direct = diffuse.rgb * (v_lightDiffuse + spotDiffuse);
            #endif //shadowMapFlag

            vec3 ambient = diffuse.rgb * v_ambientLight;
            gl_FragColor.rgb = max(ambient, direct) + emissive.rgb;
        #else
            #ifdef shadowMapFlag
                gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse) + (diffuse.rgb * spotDiffuse) + emissive.rgb;
            #else
                gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + spotDiffuse)) + emissive.rgb;
            #endif //shadowMapFlag
        #endif
    #else
        #if defined(specularTextureFlag) && defined(specularColorFlag)
            vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * (v_lightSpecular + spotSpecular);
        #elif defined(specularTextureFlag)
            vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * (v_lightSpecular + spotSpecular);
        #elif defined(specularColorFlag)
            vec3 specular = u_specularColor.rgb * (v_lightSpecular + spotSpecular);
        #else
            vec3 specular = v_lightSpecular + spotSpecular;
        #endif

        #if defined(ambientFlag) && defined(separateAmbientFlag)
            #ifdef shadowMapFlag
                vec3 direct = diffuse.rgb * (getShadow() * v_lightDiffuse + spotDiffuse);
            #else
                vec3 direct = diffuse.rgb * (v_lightDiffuse + spotDiffuse);
            #endif //shadowMapFlag

            vec3 ambient = diffuse.rgb * v_ambientLight;
            gl_FragColor.rgb = max(ambient, direct) + specular + emissive.rgb;
        #else
            #ifdef shadowMapFlag
                gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse + specular) + (diffuse.rgb * spotDiffuse) + emissive.rgb;
            #else
                gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + spotDiffuse)) + specular + emissive.rgb;
            #endif //shadowMapFlag
        #endif
    #endif //lightingFlag

    #ifdef fogFlag
        gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
    #endif // end fogFlag

    #ifdef blendedFlag
        gl_FragColor.a = diffuse.a * v_opacity;
        #ifdef alphaTestFlag
            if (gl_FragColor.a <= v_alphaTest)
                discard;
        #endif
    #else
        gl_FragColor.a = 1.0;
    #endif
}
