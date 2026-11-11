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


varying vec3 v_worldPos;

// DefaultShader uploads point/spot color already multiplied by intensity.
#if numDirectionalLights > 0
struct DirectionalLight { vec3 color; vec3 direction; };
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif
#if numPointLights > 0
struct PointLight { vec3 color; vec3 position; };
uniform PointLight u_pointLights[numPointLights];
#endif
#if numSpotLights > 0
struct SpotLight {
    vec3 color;
    vec3 position;
    vec3 direction;
    float cutoffAngle; // DefaultShader uploads degrees.
    float exponent;
};
uniform SpotLight u_spotLights[numSpotLights];
#endif
#ifdef specularFlag
uniform vec4 u_cameraPosition;
#endif
#if defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag



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
    vec3 surfaceDiffuse = vec3(0.0);
    vec3 spotDiffuse = vec3(0.0);
    #ifdef specularFlag
        vec3 surfaceSpecular = vec3(0.0);
        vec3 spotSpecular = vec3(0.0);
    #endif
    #ifdef normalFlag
        vec3 N = normal * inversesqrt(max(dot(normal, normal), 1e-8));
        #ifdef specularFlag
            vec3 view = u_cameraPosition.xyz - v_worldPos;
            vec3 V = view * inversesqrt(max(dot(view, view), 1e-8));
        #endif
        #if numDirectionalLights > 0
            for (int i = 0; i < numDirectionalLights; i++) {
                vec3 direction = -u_dirLights[i].direction;
                vec3 L = direction * inversesqrt(max(dot(direction, direction), 1e-8));
                float NdotL = max(dot(N, L), 0.0);
                vec3 value = u_dirLights[i].color * NdotL;
                surfaceDiffuse += value;
                #ifdef specularFlag
                    vec3 halfVector = L + V;
                    vec3 H = halfVector * inversesqrt(max(dot(halfVector, halfVector), 1e-8));
                    surfaceSpecular += value * pow(max(dot(N, H), 0.0), u_shininess);
                #endif
            }
        #endif
        #if numPointLights > 0
            for (int i = 0; i < numPointLights; i++) {
                vec3 direction = u_pointLights[i].position - v_worldPos;
                float dist2 = dot(direction, direction);
                vec3 L = direction * inversesqrt(max(dist2, 1e-8));
                float NdotL = max(dot(N, L), 0.0);
                vec3 value = u_pointLights[i].color * (NdotL / (1.0 + dist2));
                surfaceDiffuse += value;
                #ifdef specularFlag
                    vec3 halfVector = L + V;
                    vec3 H = halfVector * inversesqrt(max(dot(halfVector, halfVector), 1e-8));
                    surfaceSpecular += value * pow(max(dot(N, H), 0.0), u_shininess);
                #endif
            }
        #endif
        #if numSpotLights > 0
            for (int i = 0; i < numSpotLights; i++) {
                vec3 lightToFrag = v_worldPos - u_spotLights[i].position;
                float dist2 = dot(lightToFrag, lightToFrag);
                vec3 outward = lightToFrag * inversesqrt(max(dist2, 1e-8));
                vec3 direction = u_spotLights[i].direction;
                direction *= inversesqrt(max(dot(direction, direction), 1e-8));
                float cosTheta = dot(direction, outward);
                float cosCutoff = cos(radians(u_spotLights[i].cutoffAngle));
                if (cosTheta > cosCutoff) {
                    float edge = smoothstep(cosCutoff, min(1.0, cosCutoff + .12), cosTheta);
                    float cone = edge * pow(max(cosTheta, 0.0), max(u_spotLights[i].exponent, 0.0));
                    vec3 L = -outward;
                    float NdotL = max(dot(N, L), 0.0);
                    vec3 value = u_spotLights[i].color * (NdotL * cone / (1.0 + dist2));
                    spotDiffuse += value;
                    #ifdef specularFlag
                        vec3 halfVector = L + V;
                        vec3 H = halfVector * inversesqrt(max(dot(halfVector, halfVector), 1e-8));
                        spotSpecular += value * pow(max(dot(N, H), 0.0), u_shininess);
                    #endif
                }
            }
        #endif
    #endif // normalFlag
#endif // lightingFlag
    #if (!defined(lightingFlag))
        gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    #elif (!defined(specularFlag))
        #if defined(ambientFlag) && defined(separateAmbientFlag)
            #ifdef shadowMapFlag
                vec3 direct = diffuse.rgb * (getShadow() * surfaceDiffuse + spotDiffuse);
            #else
                vec3 direct = diffuse.rgb * (surfaceDiffuse + spotDiffuse);
            #endif //shadowMapFlag

            vec3 ambient = diffuse.rgb * v_ambientLight;
            gl_FragColor.rgb = (ambient + direct) + emissive.rgb;
        #else
            #ifdef shadowMapFlag
                gl_FragColor.rgb = getShadow() * (diffuse.rgb * surfaceDiffuse) + (diffuse.rgb * spotDiffuse) + emissive.rgb;
            #else
                gl_FragColor.rgb = (diffuse.rgb * (surfaceDiffuse + spotDiffuse)) + emissive.rgb;
            #endif //shadowMapFlag
        #endif
    #else
        #if defined(specularTextureFlag) && defined(specularColorFlag)
            vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * (surfaceSpecular + spotSpecular);
        #elif defined(specularTextureFlag)
            vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * (surfaceSpecular + spotSpecular);
        #elif defined(specularColorFlag)
            vec3 specular = u_specularColor.rgb * (surfaceSpecular + spotSpecular);
        #else
            vec3 specular = surfaceSpecular + spotSpecular;
        #endif

        #if defined(ambientFlag) && defined(separateAmbientFlag)
            #ifdef shadowMapFlag
                vec3 direct = diffuse.rgb * (getShadow() * surfaceDiffuse + spotDiffuse);
            #else
                vec3 direct = diffuse.rgb * (surfaceDiffuse + spotDiffuse);
            #endif //shadowMapFlag

            vec3 ambient = diffuse.rgb * v_ambientLight;
            gl_FragColor.rgb = (ambient + direct) + specular + emissive.rgb;
        #else
            #ifdef shadowMapFlag
                gl_FragColor.rgb = getShadow() * (diffuse.rgb * surfaceDiffuse + specular) + (diffuse.rgb * spotDiffuse) + emissive.rgb;
            #else
                gl_FragColor.rgb = (diffuse.rgb * (surfaceDiffuse + spotDiffuse)) + specular + emissive.rgb;
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
