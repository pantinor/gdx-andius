package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class FreeTypeSkin extends Skin {

    public FreeTypeSkin() {
    }

    public FreeTypeSkin(FileHandle skinFile) {
        super(skinFile);
    }

    public FreeTypeSkin(FileHandle skinFile, TextureAtlas atlas) {
        super(skinFile, atlas);
    }

    public FreeTypeSkin(TextureAtlas atlas) {
        super(atlas);
    }

    @Override
    protected Json getJsonLoader(final FileHandle skinFile) {
        Json json = super.getJsonLoader(skinFile);

        json.setSerializer(BitmapFont.class, new Json.ReadOnlySerializer<BitmapFont>() {
            @Override
            public BitmapFont read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("file", String.class, jsonData);
                jsonData.remove("file");

                FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.valueOf(
                        json.readValue("hinting", String.class, "AutoMedium", jsonData));
                jsonData.remove("hinting");

                Texture.TextureFilter minFilter = Texture.TextureFilter.valueOf(
                        json.readValue("minFilter", String.class, "Nearest", jsonData));
                jsonData.remove("minFilter");

                Texture.TextureFilter magFilter = Texture.TextureFilter.valueOf(
                        json.readValue("magFilter", String.class, "Nearest", jsonData));
                jsonData.remove("magFilter");

                FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                        json.readValue(FreeTypeFontGenerator.FreeTypeFontParameter.class, jsonData);
                parameter.hinting = hinting;
                parameter.minFilter = minFilter;
                parameter.magFilter = magFilter;

                FileHandle fontFile = resolveFontFile(skinFile, path);
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                generator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);

                BitmapFont font = generator.generateFont(parameter);

                if (!parameter.incremental) {
                    generator.dispose();
                }

                return font;
            }
        });

        return json;
    }

    private static FileHandle resolveFontFile(FileHandle skinFile, String path) {
        FileHandle fontFile = skinFile.parent().child(path);
        if (fontFile.exists()) {
            return fontFile;
        }

        fontFile = Gdx.files.classpath(path);
        if (fontFile.exists()) {
            return fontFile;
        }

        return skinFile.parent().child(path);
    }
}
