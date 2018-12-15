package andius.voronoi.graph;

import andius.voronoi.nodes.Point;
import java.util.ArrayList;

public class Corner {

    public ArrayList<Center> touches = new ArrayList();
    public ArrayList<Corner> adjacent = new ArrayList();
    public ArrayList<Edge> protrudes = new ArrayList();
    public Point loc;
    public int index;
    public boolean border;
}
