/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import static andius.Constants.TILE_DIM;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 *
 * @author Paul
 */
public class PlayerCursor extends com.badlogic.gdx.scenes.scene2d.Actor {

    TextureRegion textureRed;
    TextureRegion textureYellow;
    TextureRegion textureBlue;
    TextureRegion texturePurple;

    boolean visible = false;

    public PlayerCursor() {
        TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/cursor.png")), 48, 48);
        this.textureYellow = trs[0][0];
        this.textureBlue = trs[0][1];
        this.textureRed = trs[1][0];
        this.texturePurple = trs[1][1];
    }

    public void set(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setVisible(boolean v) {
        this.visible = v;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (visible) {
            batch.draw(textureYellow, getX(), getY());
        } else {
            batch.draw(textureBlue, getX(), getY());
        }
    }

    private Texture getCursorTexture() {
        Pixmap pixmap = new Pixmap(TILE_DIM, TILE_DIM, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        int w = 4;
        pixmap.fillRectangle(0, 0, w, TILE_DIM);
        pixmap.fillRectangle(TILE_DIM - w, 0, w, TILE_DIM);
        pixmap.fillRectangle(w, 0, TILE_DIM - 2 * w, w);
        pixmap.fillRectangle(w, TILE_DIM - w, TILE_DIM - 2 * w, w);
        return new Texture(pixmap);
    }

}
