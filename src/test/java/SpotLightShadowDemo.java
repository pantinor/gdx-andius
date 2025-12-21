
import static andius.Andius.SCREEN_HEIGHT;
import static andius.Andius.SCREEN_WIDTH;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

/**
 * 5 spot lights, 5 blue cubes, red walls around each cube, custom shaders for
 * shadow map and main pass. Y is up.
 */
public class SpotLightShadowDemo extends ApplicationAdapter {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Test";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        new LwjglApplication(new SpotLightShadowDemo(), cfg);
    }
    private PerspectiveCamera cam;
    private CameraInputController camController;

    // Geometry
    private Model boxModel;
    private Model wallModel;
    private Model groundModel;
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<Vector3> cubeCenters = new Array<>();

    // Lighting
    private final Array<SpotLight> spotLights = new Array<>();

    // Shadow mapping (only first spotlight)
    private static final int SHADOW_MAP_SIZE = 1024;
    private FrameBuffer shadowFbo;
    private Texture shadowMapTexture;
    private PerspectiveCamera shadowCamera;

    // Rendering
    private ModelBatch mainBatch;
    private ModelBatch shadowBatch;

    @Override
    public void create() {
        // Camera: Y is up
        cam = new PerspectiveCamera(67f,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        cam.position.set(0f, 0.5f, 0f);
        cam.lookAt(1f, 0.5f, 1f);
        cam.near = 0.1f;
        cam.far = 50f;
        cam.update();

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        // Models & materials
        ModelBuilder mb = new ModelBuilder();
        Material blue = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material red = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material gray = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY));

        boxModel = mb.createBox(1f, 1f, 1f, blue, Usage.Position | Usage.Normal);
        wallModel = mb.createBox(3f, 4f, 0.1f, red, Usage.Position | Usage.Normal);
        groundModel = mb.createBox(20f, 0.1f, 20f, gray, Usage.Position | Usage.Normal);

        // Ground
        ModelInstance ground = new ModelInstance(groundModel);
        ground.transform.setToTranslation(0f, -0.05f, 0f);
        instances.add(ground);

        // Cubes + red walls
        createCubesAndWalls();

        // 5 spot lights, one over each cube (positive x,y,z)
        createSpotLights();

        // Shadow map FBO (color buffer stores depth)
        shadowFbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);
        shadowMapTexture = shadowFbo.getColorBufferTexture();
        shadowMapTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shadowMapTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        // Shadow camera: from main spotlight’s point of view
        shadowCamera = new PerspectiveCamera(90f,
                SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        shadowCamera.near = 0.1f;
        shadowCamera.far = 30f;

        // Shader providers
        mainBatch = new ModelBatch(
                new SpotLightShaderProvider(spotLights, shadowCamera, shadowMapTexture));
        shadowBatch = new ModelBatch(
                new DepthShaderProvider());
    }

    private void createCubesAndWalls() {
        // 5 cubes in positive X/Z, at y = 0.5 (Y up)
        // One directly in front of camera look-at
        Vector3[] centers = new Vector3[]{
            new Vector3(1f, 0.5f, 1f),
            new Vector3(1f, 0.5f, 6f),
            new Vector3(5f, 0.5f, 1f),
            new Vector3(5f, 0.5f, 6f),
            new Vector3(9f, 0.5f, 1f)
        };

        float wallHalfHeight = 2f; // total 4 high
        float fenceRadius = 1.5f;  // distance from cube center to wall center

        for (Vector3 c : centers) {
            cubeCenters.add(new Vector3(c));

            // Blue cube
            ModelInstance cube = new ModelInstance(boxModel);
            cube.transform.setToTranslation(c);
            instances.add(cube);

            // 4 red walls around cube, height 4
            // North / South (no rotation)
            ModelInstance wallN = new ModelInstance(wallModel);
            wallN.transform.setToTranslation(c.x, wallHalfHeight, c.z + fenceRadius);
            instances.add(wallN);

            ModelInstance wallS = new ModelInstance(wallModel);
            wallS.transform.setToTranslation(c.x, wallHalfHeight, c.z - fenceRadius);
            instances.add(wallS);

            // East / West (rotated 90 degrees around Y)
            ModelInstance wallE = new ModelInstance(wallModel);
            wallE.transform.idt()
                    .setToRotation(Vector3.Y, 90f)
                    .trn(c.x + fenceRadius, wallHalfHeight, c.z);
            instances.add(wallE);

            ModelInstance wallW = new ModelInstance(wallModel);
            wallW.transform.idt()
                    .setToRotation(Vector3.Y, 90f)
                    .trn(c.x - fenceRadius, wallHalfHeight, c.z);
            instances.add(wallW);
        }
    }

    private void createSpotLights() {
        // 5 spot lights above each cube, pointing straight down (-Y)
        for (Vector3 center : cubeCenters) {
            SpotLight sl = new SpotLight();

            Vector3 pos = new Vector3(center.x, center.y + 3f, center.z);
            Vector3 dir = new Vector3(0f, -1f, 0f);

            // Color, position, direction, intensity, cutoffAngle(deg), exponent
            sl.set(Color.WHITE, pos, dir, 2.5f, 45f, 10f);

            spotLights.add(sl);
        }
    }

    private void updateShadowCameraFromMainLight() {
        if (spotLights.size == 0) {
            return;
        }

        SpotLight main = spotLights.first(); // only first casts shadows here

        // Set position at the light
        shadowCamera.position.set(main.position);

        // Use the spotlight direction directly
        Vector3 dir = new Vector3(main.direction).nor();
        shadowCamera.direction.set(dir);

        // Choose an "up" that is NOT parallel to the direction
        if (Math.abs(dir.y) > 0.9f) {
            // Light mostly vertical -> use Z as up
            shadowCamera.up.set(0f, 0f, 1f);
        } else {
            shadowCamera.up.set(0f, 1f, 0f);
        }

        // Keep the frustum tight for better precision
        shadowCamera.near = 0.1f;
        shadowCamera.far = 20f;  // enough for your scene; smaller = better depth precision

        shadowCamera.update();
    }

    @Override
    public void render() {
        camController.update();

        // 1) Shadow pass from first spotlight
        updateShadowCameraFromMainLight();
        Gdx.gl.glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        shadowFbo.begin();
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shadowBatch.begin(shadowCamera);
        for (ModelInstance inst : instances) {
            shadowBatch.render(inst);
        }
        shadowBatch.end();
        shadowFbo.end();

        // 2) Main pass from camera
        Gdx.gl.glViewport(0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        mainBatch.begin(cam);
        for (ModelInstance inst : instances) {
            mainBatch.render(inst);
        }
        mainBatch.end();
    }

    @Override
    public void dispose() {
        mainBatch.dispose();
        shadowBatch.dispose();
        boxModel.dispose();
        wallModel.dispose();
        groundModel.dispose();
        shadowFbo.dispose();
    }

    // ------------------------------------------------------------------------
    // Shader providers
    // ------------------------------------------------------------------------
    private static class SpotLightShaderProvider extends BaseShaderProvider {

        private final Array<SpotLight> lights;
        private final Camera lightCamera;
        private final Texture shadowMapTexture;

        SpotLightShaderProvider(Array<SpotLight> lights,
                Camera lightCamera,
                Texture shadowMapTexture) {
            this.lights = lights;
            this.lightCamera = lightCamera;
            this.shadowMapTexture = shadowMapTexture;
        }

        @Override
        protected Shader createShader(Renderable renderable) {
            return new SpotLightShader(renderable, lights, lightCamera, shadowMapTexture);
        }
    }

    private static class DepthShaderProvider extends BaseShaderProvider {

        @Override
        protected Shader createShader(Renderable renderable) {
            return new DepthShader(renderable);
        }
    }

    // ------------------------------------------------------------------------
    // Depth-only shader (shadow map)
    // ------------------------------------------------------------------------
    private static class DepthShader implements Shader {

        private final ShaderProgram program;
        private int uWorldTrans;
        private int uLightProjViewTrans;

        DepthShader(Renderable renderable) {
            program = new ShaderProgram(DEPTH_VERT, DEPTH_FRAG);
            if (!program.isCompiled()) {
                throw new GdxRuntimeException("Depth shader compile error: " + program.getLog());
            }
            uWorldTrans = program.getUniformLocation("u_worldTrans");
            uLightProjViewTrans = program.getUniformLocation("u_lightProjViewTrans");
        }

        @Override
        public void init() {
        }

        @Override
        public void begin(Camera camera, RenderContext context) {
            program.begin();
            program.setUniformMatrix(uLightProjViewTrans, camera.combined);
            context.setDepthTest(GL20.GL_LEQUAL);
            context.setCullFace(GL20.GL_NONE);
            context.setBlending(false, 0, 0);
        }

        @Override
        public void render(Renderable renderable) {
            program.setUniformMatrix(uWorldTrans, renderable.worldTransform);
            renderable.meshPart.mesh.render(
                    program,
                    renderable.meshPart.primitiveType,
                    renderable.meshPart.offset,
                    renderable.meshPart.size
            );
        }

        @Override
        public void end() {
            program.end();
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public int compareTo(Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(Renderable renderable) {
            return true;
        }
    }

    // ------------------------------------------------------------------------
    // Main shader: 5 spot lights + shadow map from first light
    // ------------------------------------------------------------------------
    private static class SpotLightShader implements Shader {

        private final ShaderProgram program;
        private final Array<SpotLight> lights;
        private final Camera lightCamera;
        private final Texture shadowMap;

        private RenderContext context;
        private Camera camera;

        private int uProjViewTrans;
        private int uWorldTrans;
        private int uLightProjViewTrans;
        private int uCameraPos;
        private int uAmbientColor;
        private int uBaseColor;
        private int uShadowMap;
        private int uShadowBias;
        private int uNumLights;

        SpotLightShader(Renderable renderable,
                Array<SpotLight> lights,
                Camera lightCamera,
                Texture shadowMap) {
            this.lights = lights;
            this.lightCamera = lightCamera;
            this.shadowMap = shadowMap;

            program = new ShaderProgram(MAIN_VERT, MAIN_FRAG);
            if (!program.isCompiled()) {
                throw new GdxRuntimeException("SpotLight shader compile error: " + program.getLog());
            }

            uProjViewTrans = program.getUniformLocation("u_projViewTrans");
            uWorldTrans = program.getUniformLocation("u_worldTrans");
            uLightProjViewTrans = program.getUniformLocation("u_lightProjViewTrans");
            uCameraPos = program.getUniformLocation("u_cameraPosition");
            uAmbientColor = program.getUniformLocation("u_ambientColor");
            uBaseColor = program.getUniformLocation("u_baseColor");
            uShadowMap = program.getUniformLocation("u_shadowMap");
            uShadowBias = program.getUniformLocation("u_shadowBias");
            uNumLights = program.getUniformLocation("u_numSpotLights");
        }

        @Override
        public void init() {
        }

        @Override
        public void begin(Camera camera, RenderContext context) {
            this.camera = camera;
            this.context = context;

            program.begin();
            program.setUniformMatrix(uProjViewTrans, camera.combined);
            program.setUniformMatrix(uLightProjViewTrans, lightCamera.combined);
            program.setUniformf(uCameraPos,
                    camera.position.x, camera.position.y, camera.position.z);
            program.setUniformf(uAmbientColor, 0.05f, 0.05f, 0.05f);
            program.setUniformf(uShadowBias, 0.001f);

            // Lights
            int numLights = Math.min(lights.size, 5);
            program.setUniformi(uNumLights, numLights);
            for (int i = 0; i < numLights; i++) {
                SpotLight l = lights.get(i);
                String base = "u_spotLights[" + i + "].";

                program.setUniformf(base + "position", l.position);
                program.setUniformf(base + "direction", l.direction);
                program.setUniformf(base + "color",
                        l.color.r, l.color.g, l.color.b);

                float cutoffRad = (float) Math.toRadians(l.cutoffAngle);
                float outerCutoffRad = cutoffRad * 1.2f; // slightly softer edge
                float cutoffCos = (float) Math.cos(cutoffRad);
                float outerCutoffCos = (float) Math.cos(outerCutoffRad);

                program.setUniformf(base + "cutoffCos", cutoffCos);
                program.setUniformf(base + "outerCutoffCos", outerCutoffCos);
                program.setUniformf(base + "intensity", l.intensity);
            }

            // Shadow map on texture unit 0
            context.setDepthTest(GL20.GL_LEQUAL);
            context.setCullFace(GL20.GL_BACK);
            context.setBlending(false, 0, 0);

            shadowMap.bind(0);
            program.setUniformi(uShadowMap, 0);
        }

        @Override
        public void render(Renderable renderable) {
            program.setUniformMatrix(uWorldTrans, renderable.worldTransform);

            ColorAttribute diffuse = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
            if (diffuse != null) {
                program.setUniformf(uBaseColor,
                        diffuse.color.r, diffuse.color.g, diffuse.color.b);
            } else {
                program.setUniformf(uBaseColor, 1f, 1f, 1f);
            }

            renderable.meshPart.mesh.render(
                    program,
                    renderable.meshPart.primitiveType,
                    renderable.meshPart.offset,
                    renderable.meshPart.size
            );
        }

        @Override
        public void end() {
            program.end();
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public int compareTo(Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(Renderable renderable) {
            return true;
        }
    }

    // ------------------------------------------------------------------------
    // GLSL shader sources
    // ------------------------------------------------------------------------
    // Depth pass vertex shader
    private static final String DEPTH_VERT
            = "attribute vec3 a_position;\n"
            + "uniform mat4 u_worldTrans;\n"
            + "uniform mat4 u_lightProjViewTrans;\n"
            + "void main() {\n"
            + "    gl_Position = u_lightProjViewTrans * u_worldTrans * vec4(a_position, 1.0);\n"
            + "}\n";

    // Depth pass fragment shader (store depth in color)
    private static final String DEPTH_FRAG
            = "#ifdef GL_ES\n"
            + "precision mediump float;\n"
            + "#endif\n"
            + "vec4 packDepth(const float depth) {\n"
            + "    const vec4 bitShift = vec4(1.0, 256.0, 65536.0, 16777216.0);\n"
            + "    const vec4 bitMask  = vec4(1.0/256.0, 1.0/256.0, 1.0/256.0, 0.0);\n"
            + "    vec4 rgba = fract(depth * bitShift);\n"
            + "    rgba -= rgba.yzww * bitMask;\n"
            + "    return rgba;\n"
            + "}\n"
            + "void main() {\n"
            + "    float depth = gl_FragCoord.z;\n"
            + "    gl_FragColor = packDepth(depth);\n"
            + "}\n";

    // Main pass vertex shader
    private static final String MAIN_VERT
            = "attribute vec3 a_position;\n"
            + "attribute vec3 a_normal;\n"
            + "uniform mat4 u_projViewTrans;\n"
            + "uniform mat4 u_worldTrans;\n"
            + "uniform mat4 u_lightProjViewTrans;\n"
            + "varying vec3 v_worldPos;\n"
            + "varying vec3 v_normal;\n"
            + "varying vec4 v_lightSpacePos;\n"
            + "void main() {\n"
            + "    vec4 worldPos = u_worldTrans * vec4(a_position, 1.0);\n"
            + "    v_worldPos = worldPos.xyz;\n"
            + "    v_normal = normalize((u_worldTrans * vec4(a_normal, 0.0)).xyz);\n"
            + "    v_lightSpacePos = u_lightProjViewTrans * worldPos;\n"
            + "    gl_Position = u_projViewTrans * worldPos;\n"
            + "}\n";

    // Main pass fragment shader (5 spot lights, shadow from first)
    private static final String MAIN_FRAG
            = "#ifdef GL_ES\n"
            + "precision mediump float;\n"
            + "#endif\n"
            + "#define MAX_SPOT_LIGHTS 5\n"
            + "struct SpotLight {\n"
            + "    vec3 position;\n"
            + "    vec3 direction;\n"
            + "    vec3 color;\n"
            + "    float cutoffCos;\n"
            + "    float outerCutoffCos;\n"
            + "    float intensity;\n"
            + "};\n"
            + "uniform SpotLight u_spotLights[MAX_SPOT_LIGHTS];\n"
            + "uniform int u_numSpotLights;\n"
            + "uniform vec3 u_ambientColor;\n"
            + "uniform vec3 u_cameraPosition;\n"
            + "uniform vec3 u_baseColor;\n"
            + "uniform sampler2D u_shadowMap;\n"
            + "uniform float u_shadowBias;\n"
            + "varying vec3 v_worldPos;\n"
            + "varying vec3 v_normal;\n"
            + "varying vec4 v_lightSpacePos;\n"
            + "float unpackDepth(vec4 rgbaDepth) {\n"
            + "    const vec4 bitShift = vec4(1.0,\n"
            + "                               1.0/256.0,\n"
            + "                               1.0/65536.0,\n"
            + "                               1.0/16777216.0);\n"
            + "    return dot(rgbaDepth, bitShift);\n"
            + "}\n"
            + "float calculateShadow() {\n"
            + "    vec3 projCoords = v_lightSpacePos.xyz / v_lightSpacePos.w;\n"
            + "    projCoords = projCoords * 0.5 + 0.5;\n"
            + "    if (projCoords.x < 0.0 || projCoords.x > 1.0 ||\n"
            + "        projCoords.y < 0.0 || projCoords.y > 1.0 ||\n"
            + "        projCoords.z < 0.0 || projCoords.z > 1.0) {\n"
            + "        return 0.0;\n"
            + "    }\n"
            + "    float currentDepth = projCoords.z;\n"
            + "    float shadow = 0.0;\n"
            + "    float texel = 1.0 / 1024.0; // SHADOW_MAP_SIZE\n"
            + "    for (int x = -1; x <= 1; x++) {\n"
            + "        for (int y = -1; y <= 1; y++) {\n"
            + "            vec2 offset = projCoords.xy + vec2(float(x), float(y)) * texel;\n"
            + "            offset = clamp(offset, 0.0, 1.0);\n"
            + "            float closestDepth = unpackDepth(texture2D(u_shadowMap, offset));\n"
            + "            shadow += (currentDepth - u_shadowBias > closestDepth) ? 1.0 : 0.0;\n"
            + "        }\n"
            + "    }\n"
            + "    shadow /= 9.0;\n"
            + "    return shadow;\n"
            + "}\n"
            + "void main() {\n"
            + "    vec3 N = normalize(v_normal);\n"
            + "    vec3 viewDir = normalize(u_cameraPosition - v_worldPos);\n"
            + "    vec3 color = u_ambientColor * u_baseColor;\n"
            + "    float shadow = calculateShadow();\n"
            + "    for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {\n"
            + "        if (i >= u_numSpotLights) break;\n"
            + "        SpotLight light = u_spotLights[i];\n"
            + "        vec3 L = normalize(light.position - v_worldPos);\n"
            + "        float diff = max(dot(N, L), 0.0);\n"
            + "        float distance = length(light.position - v_worldPos);\n"
            + "        float attenuation = 1.0 / (1.0 + 0.09 * distance + 0.032 * distance * distance);\n"
            + "        float theta = dot(normalize(-light.direction), L);\n"
            + "        float epsilon = light.cutoffCos - light.outerCutoffCos;\n"
            + "        float spotIntensity = clamp((theta - light.outerCutoffCos) / epsilon, 0.0, 1.0);\n"
            + "        vec3 diffuse = diff * u_baseColor * light.color;\n"
            + "        vec3 halfwayDir = normalize(L + viewDir);\n"
            + "        float spec = pow(max(dot(N, halfwayDir), 0.0), 32.0);\n"
            + "        vec3 specular = spec * light.color;\n"
            + "        float shadowFactor = (i == 0) ? (1.0 - shadow) : 1.0;\n"
            + "        color += (diffuse + specular) * attenuation * spotIntensity * light.intensity * shadowFactor;\n"
            + "    }\n"
            + "    gl_FragColor = vec4(color, 1.0);\n"
            + "}\n";
}
