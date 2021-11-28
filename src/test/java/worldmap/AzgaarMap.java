package worldmap;

import andius.voronoi.nodes.Voronoi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.testng.annotations.Test;

public class AzgaarMap {

    //@Test
    public void readMap() throws Exception {

        FileInputStream fstream = new FileInputStream("src/main/resources/assets/azgaarMaps/Horsto 2021-11-27-19-57.map");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String sparams = br.readLine();
        String ssettings = br.readLine();
        String smapCoordinates = br.readLine();
        String sbiomes = br.readLine();
        String snotes = br.readLine();

        while (!br.readLine().contains("</svg>")) {
            //next
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        String sgrid = br.readLine();
        Grid grid = gson.fromJson(sgrid, new TypeToken<Grid>() {
        }.getType());

        grid.cellsH = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.cellsPrec = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.cellsF = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.cellsT = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.cellsTemp = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();

        JsonElement features = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
        }.getType());
        JsonElement cultures = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
        }.getType());
        JsonElement states = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
        }.getType());
        JsonElement burgs = gson.fromJson(br.readLine(), new TypeToken<JsonArray>() {
        }.getType());

        grid.biome = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.burg = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.conf = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.culture = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.fl = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.pop = Arrays.stream(br.readLine().split(",")).mapToDouble(Double::parseDouble).toArray();
        grid.r = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.road = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.s = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.state = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.religion = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.province = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
        grid.crossroad = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();

        Voronoi v = new Voronoi(grid.points, 1);

        JsonArray e = burgs.getAsJsonArray();

        for (int i = 1; i < e.size(); i++) {
            JsonObject burg = e.get(i).getAsJsonObject();
            String name = burg.getAsJsonPrimitive("name").getAsString();
            int cell = burg.getAsJsonPrimitive("cell").getAsInt();

            System.out.printf("burg %s cell %d\n", name, cell);
        }

        int x = 0;
    }

    public class Grid {

        Float spacing;
        Integer cellsX;
        Integer cellsY;
        List<Integer[]> boundary;
        List<Float[]> points;
        JsonArray features;

        int[] cellsH;
        int[] cellsPrec;
        int[] cellsF;
        int[] cellsT;
        int[] cellsTemp;

        int[] biome;
        int[] burg;
        int[] conf;
        int[] culture;
        int[] fl;
        double[] pop;
        int[] r;
        int[] road;
        int[] s;
        int[] state;
        int[] religion;
        int[] province;
        int[] crossroad;

    }

}
