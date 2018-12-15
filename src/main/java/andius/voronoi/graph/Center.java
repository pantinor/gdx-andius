package andius.voronoi.graph;

import andius.VoronoiAzgaarMapScreen.Cell;
import andius.voronoi.nodes.Point;
import java.util.ArrayList;

public class Center {

    public int index;
    public Point loc;
    public ArrayList<Corner> corners = new ArrayList();
    public ArrayList<Center> neighbors = new ArrayList();
    public ArrayList<Edge> borders = new ArrayList();
    public Cell cell;

    public Center() {
    }

    public Center(Point loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "Center{" + "index=" + index + ", cell=" + cell + '}';
    }



    public Center getClosestNeighbor(float x, float y) {
        int sz = this.neighbors.size();

        float tmp = 0;
        Center closest = null;

        for (int i = 0; i < sz; i++) {
            float dist = Point.distance(this.neighbors.get(i).loc, x, y);
            if (dist > 0 && tmp == 0 || dist < tmp) {
                tmp = dist;
                closest = this.neighbors.get(i);
            }
        }

        return closest;

    }

}
