package andius.objects;

import static andius.Constants.TILE_DIM;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerCursor extends com.badlogic.gdx.scenes.scene2d.Actor {

    private static final Texture GREEN = getCursorTexture(Color.GREEN);
    private static final Texture BLUE = getCursorTexture(Color.BLUE);

    boolean visible = false;

    TextureRegion healthGreen;
    TextureRegion healthBlue;

    public PlayerCursor() {
        this.healthGreen = new TextureRegion(GREEN, 0, 0, TILE_DIM, TILE_DIM);
        this.healthBlue = new TextureRegion(BLUE, 0, 0, TILE_DIM, TILE_DIM);
    }

    public void adjust(int hp, int maxhp) {
        double percent = (double) hp / maxhp;
        double bar = percent * (double) TILE_DIM;
        if (hp < 0) {
            bar = 0;
        }
        if (bar > TILE_DIM) {
            bar = TILE_DIM;
        }
        healthGreen.setRegion(0, 0, TILE_DIM, (int) bar);
        healthBlue.setRegion(0, 0, TILE_DIM, (int) bar);
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
            batch.draw(healthGreen, getX(), getY());
        } else {
            batch.draw(healthBlue, getX(), getY());
        }
    }

    private static Texture getCursorTexture(Color c) {
        Pixmap pixmap = new Pixmap(TILE_DIM, TILE_DIM, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(c.r, c.g, c.b, 0.1f));
        pixmap.fillRectangle(0, 0, TILE_DIM, TILE_DIM);
        return new Texture(pixmap);
    }

}
