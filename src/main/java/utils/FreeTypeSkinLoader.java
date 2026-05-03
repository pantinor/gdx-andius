package utils;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FreeTypeSkinLoader extends SkinLoader {

    public FreeTypeSkinLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    protected Skin newSkin(TextureAtlas atlas) {
        return new FreeTypeSkin(atlas);
    }

    public static class FreeTypeSkinLoaderParameter extends SkinParameter {
        public FreeTypeSkinLoaderParameter(String textureAtlasPath) {
            super(textureAtlasPath);
        }
    }
}
