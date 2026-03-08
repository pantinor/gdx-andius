package andius.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import static utils.Utils.CLASSPTH_RSLVR;

public class UltimaSprite {

    private static TiledMapTileSets TILESETS;

    public static void init() {
        if (TILESETS == null) {
            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            TILESETS = loader.load("assets/data/YEW.tmx").getTileSets();
        }
    }

    public static Animation<TextureRegion> anim(int id) {
        if (TILESETS == null) {
            throw new RuntimeException("TILESETS not initialized");
        }

        TiledMapTile tile = TILESETS.getTile(id + 1);

        if (tile instanceof AnimatedTiledMapTile) {
            AnimatedTiledMapTile animTile = (AnimatedTiledMapTile) tile;
            StaticTiledMapTile[] frameTiles = animTile.getFrameTiles();
            if (frameTiles == null) {
                return null;
            }
            Array<TextureRegion> frames = new Array<>(frameTiles.length);
            for (StaticTiledMapTile ft : frameTiles) {
                frames.add(ft.getTextureRegion());
            }
            return new Animation<>(.3f, frames, Animation.PlayMode.LOOP);
        } else {
            Array<TextureRegion> frames = new Array<>(1);
            frames.add(tile.getTextureRegion());
            return new Animation<>(.3f, frames, Animation.PlayMode.LOOP);
        }

    }

    public static TextureRegion icon(int id) {
        if (TILESETS == null) {
            throw new RuntimeException("TILESETS not initialized");
        }

        TiledMapTile tile = TILESETS.getTile(id + 1);
        if (tile == null) {
            return null;
        }

        return tile.getTextureRegion();
    }

}
