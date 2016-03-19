package andius.objects;

import static andius.Constants.TILE_DIM;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.HashMap;
import java.util.Map;

public class ProjectileActor extends Actor {
    
    private static final Map<Color, Texture> BALLS = new HashMap<>();

    static {
        BALLS.put(Color.RED,getBall(Color.RED));
        BALLS.put(Color.GREEN,getBall(Color.GREEN));
        BALLS.put(Color.PURPLE,getBall(Color.PURPLE));
        BALLS.put(Color.CYAN,getBall(Color.CYAN));
        BALLS.put(Color.VIOLET,getBall(Color.VIOLET));
        BALLS.put(Color.BLUE,getBall(Color.BLUE));
        BALLS.put(Color.YELLOW,getBall(Color.YELLOW));
        BALLS.put(Color.WHITE,getBall(Color.WHITE));
        BALLS.put(Color.BROWN,getBall(Color.BROWN));
    }

    private static Texture getBall(Color color) {
        Pixmap px = new Pixmap(TILE_DIM, TILE_DIM, Format.RGBA8888);
        px.setColor(color);
        px.fillCircle(24, 24, 4);
        Texture t = new Texture(px);
        px.dispose();
        return t;
    }

    private final Texture texture;

    public ProjectileActor(Color color, float x, float y) {
        this.texture = BALLS.get(color);
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY());
    }

}
