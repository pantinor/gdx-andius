package andius.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TibianSprite {

    private static TextureAtlas atlas;

    private static final List<String> names = new ArrayList<>();

    public static void init() {
        atlas = new TextureAtlas(Gdx.files.classpath("assets/tibian/tibian.atlas"));

        for (AtlasRegion r : atlas.getRegions()) {
            if (!names.contains(r.name)) {
                names.add(r.name);
            }
        }

    }

    public static List<String> names() {
        return Collections.unmodifiableList(names);
    }

    public static TextureRegion icon(String n) {
        if (atlas != null) {
            TextureRegion cr = atlas.findRegion(n);
            if (cr != null) {
                return cr;
            }
        }
        return null;
    }

}
