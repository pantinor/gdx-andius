package andius.objects;

import andius.Constants.Map;
import com.badlogic.gdx.math.Vector3;
import java.util.List;
import java.util.Random;

public class Portal {

    private final Map map;
    private final int sx;
    private final int sy;
    private final Vector3 dest;
    private final List<Vector3> randoms;

    public Portal(Map map, int sx, int sy, int dx, int dy, List<Vector3> randoms) {
        this.map = map;
        this.sx = sx;
        this.sy = sy;
        this.dest = new Vector3(dx,dy,0);
        this.randoms = randoms;
    }

    public Map getMap() {
        return this.map;
    }

    public int getSx() {
        return sx;
    }

    public int getSy() {
        return sy;
    }

    public Vector3 getDest() {
        if (randoms != null) {
            return randoms.get(new Random().nextInt(randoms.size()));
        }
        return dest;
    }

}
