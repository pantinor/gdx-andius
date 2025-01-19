package andius.objects;

import static andius.Constants.TILE_DIM;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MonsterCursor extends HealthCursor {

    private static final Texture TEXTURE = getCursorTexture(Color.RED);

    TextureRegion health;

    public MonsterCursor() {
        this.health = new TextureRegion(TEXTURE, 0, 0, TILE_DIM, TILE_DIM);
    }

    @Override
    public void adjust(int hp, int maxhp) {
        double percent = (double) hp / maxhp;
        double bar = percent * (double) TILE_DIM;
        if (hp < 0) {
            bar = 0;
        }
        if (bar > TILE_DIM) {
            bar = TILE_DIM;
        }
        health.setRegion(0, 0, TILE_DIM, (int) bar);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(health, getX(), getY());
    }

    private static Texture getCursorTexture(Color c) {
        Pixmap pixmap = new Pixmap(TILE_DIM, TILE_DIM, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(c.r, c.g, c.b, 0.1f));
        pixmap.fillRectangle(0, 0, TILE_DIM, TILE_DIM);
        return new Texture(pixmap);
    }

}
