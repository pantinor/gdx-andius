package andius.voronoi.graph;

import andius.Constants;
import andius.Constants.Moongate;
import andius.objects.Portal;
import andius.voronoi.nodes.Point;
import java.util.ArrayList;

public class Corner {

    public ArrayList<Center> touches = new ArrayList(); //good
    public ArrayList<Corner> adjacent = new ArrayList(); //good
    public ArrayList<Edge> protrudes = new ArrayList();
    public Point loc;
    public int index;
    public boolean border;
    public double elevation;
    public boolean water, ocean, coast;
    public Corner downslope;
    public int river;
    public double moisture;
    public Portal portal;

    public Corner getClosestNeighbor(double x, double y) {
        int sz = this.adjacent.size();

        double tmp = 0;
        Corner closest = null;

        for (int i = 0; i < sz; i++) {
            double dist = Point.distance(this.adjacent.get(i).loc, x, y);
            if (dist > 0 && tmp == 0 || dist < tmp) {
                tmp = dist;
                closest = this.adjacent.get(i);
            }
        }

        return closest;

    }
    
}
