package worldmap;

import andius.voronoi.nodes.Voronoi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.testng.annotations.Test;

public class AzgaarMap {

    @Test
    public void readMap() throws Exception {

        FileInputStream fstream = new FileInputStream("fantasy_map_1542492422343.map");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        // data convention: 0 - params; 1 - all points; 2 - cells; 3 - manors; 4 - states;
        // 5 - svg; 6 - options; 7 - cultures; 8 - none; 9 - none; 10 - heights; 11 - notes;
        String sparams = br.readLine();
        String spoints = br.readLine();
        String scells = br.readLine();
        String smanors = br.readLine();
        String sstates = br.readLine();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Float[]> points = gson.fromJson(spoints, new TypeToken<java.util.List<Float[]>>() {
        }.getType());

        List<Cell> cells = gson.fromJson(scells, new TypeToken<java.util.List<Cell>>() {
        }.getType());

        List<Manor> manors = gson.fromJson(smanors, new TypeToken<java.util.List<Manor>>() {
        }.getType());

        List<State> states = gson.fromJson(sstates, new TypeToken<java.util.List<State>>() {
        }.getType());

        Voronoi v = new Voronoi(points, 1);

        Map<Object, Integer> map = new HashMap<>();
        for (Cell c : cells) {
            if (c.harbor != null) {
                count(map, "harbor " + c.harbor);
            }
            if (c.path != null) {
                count(map, "path " + c.path);
            }
//            if (c.ctype == null) {
//                count(map, "ctype null");
//            } else {
//                count(map, "ctype " + c.ctype);
//            }
//            if (c.height == null) {
//                count(map, "height null");
//            } else {
//                count(map, "height " + c.height);
//            }

            if (c.region == null) {
                count(map, "region null");
            } else {
                count(map, "region " + c.region);
            }

            if (c.pop != null) {
                //System.out.println(c);
            }
        }

        SortedSet<Object> keys = new TreeSet<Object>(map.keySet());

        for (Object obj : keys) {
            System.out.printf("%s [%d]\n", obj, map.get(obj));
        }

        for (Manor m : manors) {
            System.out.println(m);
        }
        for (State m : states) {
            System.out.println(m);
        }

    }

    public static void count(Map<Object, Integer> map, Object category) {
        if (category != null) {
            Integer count = map.get(category);
            if (count == null) {
                map.put(category, 1);
            } else {
                map.put(category, count + 1);
            }
        }
    }

    public class Cell {

        Integer index;
        Float[] data;
        Integer height;
        Integer ctype;
        Integer fn;
        Integer[] neighbors;
        Integer harbor;
        Float area;
        Float flux;
        Integer haven;
        Float score;
        String region;
        Integer culture;
        Float pop;
        Integer path;

        @Override
        public String toString() {
            return "Cell{" + "height=" + height + ", ctype=" + ctype + ", height=" + height + ", score=" + score + ", region=" + region + ", culture=" + culture + ", pop=" + pop + '}';
        }

    }

    public class Manor {

        Integer i;
        Integer cell;
        Float x;
        Float y;
        Integer region;
        Integer culture;
        String name;
        Float population;

        @Override
        public String toString() {
            return "Manor{" + "i=" + i + ", cell=" + cell + ", x=" + x + ", y=" + y + ", region=" + region + ", culture=" + culture + ", name=" + name + ", population=" + population + '}';
        }

    }

    public class State {

        Integer i;
        String color;
        Float power;
        Object capital;
        String name;
        Integer burgs;
        Float urbanPopulation;
        Integer cells;
        Float ruralPopulation;
        Float area;

        @Override
        public String toString() {
            return "State{" + "i=" + i + ", color=" + color + ", power=" + power + ", capital=" + capital + ", name=" + name + ", burgs=" + burgs + ", urbanPopulation=" + urbanPopulation + ", cells=" + cells + ", ruralPopulation=" + ruralPopulation + ", area=" + area + '}';
        }

    }

}
