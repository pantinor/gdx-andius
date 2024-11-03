package andius.voronoi.graph;

import andius.voronoi.nodes.Point;
import com.google.gson.JsonObject;
import java.util.ArrayList;

public class Center {

    public int index;
    public Point loc;
    public ArrayList<Corner> corners = new ArrayList();
    public ArrayList<Center> neighbors = new ArrayList();
    public ArrayList<Edge> borders = new ArrayList();
    public int pointIndex;
    public Object portal;
    public JsonObject object;

    public Center() {
    }

    public Center(Point loc) {
        this.loc = loc;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.index;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Center other = (Center) obj;
        return this.index == other.index;
    }

    @Override
    public String toString() {
        return "Center{" + "loc=" + loc + ", pointIndex=" + pointIndex + '}';
    }

    public Center getClosestNeighbor(float x, float y) {
        return getClosestNeighbor(x, y, null);
    }

    public Center getClosestNeighbor(float x, float y, Center exclude) {
        int sz = this.neighbors.size();

        float tmp = 0;
        Center closest = null;

        for (int i = 0; i < sz; i++) {
            float dist = Point.distance(this.neighbors.get(i).loc, x, y);
            if (dist > 0 && tmp == 0 || dist < tmp) {
                tmp = dist;
                if (exclude == null) {
                    closest = this.neighbors.get(i);
                } else {
                    if (exclude != this.neighbors.get(i)) {
                        closest = this.neighbors.get(i);
                    }
                }
            }
        }

        return closest;

    }

}
