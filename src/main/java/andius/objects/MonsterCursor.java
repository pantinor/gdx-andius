package andius.objects;

import static andius.Constants.TILE_DIM;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class MonsterCursor extends com.badlogic.gdx.scenes.scene2d.Actor {

    Texture texture;

    boolean visible = true;

    public MonsterCursor() {
        this.texture = getCursorTexture(Color.RED);
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
            batch.draw(texture, getX(), getY());
        }
    }

    private Texture getCursorTexture(Color c) {
        Pixmap pixmap = new Pixmap(TILE_DIM, TILE_DIM, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(c.r, c.g, c.b, 0.1f));
        pixmap.fillRectangle(0, 0, TILE_DIM, TILE_DIM);
        return new Texture(pixmap);
    }

}
