package andius.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 *
 * @author Paul
 */
public class Icons {
 
    private static final TextureRegion[] LOOKUP_TABLE = new TextureRegion[840];
    private static TextureRegion[][] REGIONS;

    public static void init() {

        REGIONS = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_heroes.png")), 48, 48);
        
        for (int row = 0; row < 21; row++) {
            for (int col = 0; col < 40; col++) {
                LOOKUP_TABLE[row * 40 + col] = REGIONS[row][col];
            }
        }
    }

    public static TextureRegion get(int idx) {
        return LOOKUP_TABLE[idx];
    }

    public static TextureRegion get(int row, int col) {
        return REGIONS[row][col];
    }

}
