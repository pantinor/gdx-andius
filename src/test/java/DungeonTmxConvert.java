
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import java.io.File;
import java.util.Random;
import org.apache.commons.io.FileUtils;

public class DungeonTmxConvert implements ApplicationListener {
    
    public static final int DIM = 32;

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new DungeonTmxConvert());
    }

    @Override
    public void create() {

        try {
            Dungeon d = new Dungeon();
            d.createDungeon(DIM, DIM, 30);

            Formatter c = new Formatter(getFloor(d), getWall(d), getDoor(d), getProp(d));

            FileUtils.writeStringToFile(new File("src/main/resources/assets/data/temp_map_dungeon.tmx"), c.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    private String getFloor(Dungeon d) {
        StringBuilder dungeonMap = new StringBuilder();
        for (int y = 0; y < d.getYsize(); y++) {
            for (int x = 0; x < d.getXsize(); x++) {
                switch (d.getCell(x, y)) {
                    case Dungeon.tileDirtFloor:
                        dungeonMap.append(new Random().nextInt(3) + 182);
                        break;
                    case Dungeon.tileCorridor:
                    case Dungeon.tileDoor:
                    case Dungeon.tileUpStairs:
                    case Dungeon.tileDownStairs:
                        dungeonMap.append(new Random().nextInt(3) + 182);
                        break;
                    default:
                        dungeonMap.append(0);
                }
                dungeonMap.append(",");
            }
            dungeonMap.append("\n");
        }
        dungeonMap.deleteCharAt(dungeonMap.length() - 2);
        return dungeonMap.toString();
    }

    private String getWall(Dungeon d) {
        StringBuilder dungeonMap = new StringBuilder();
        for (int y = 0; y < d.getYsize(); y++) {
            for (int x = 0; x < d.getXsize(); x++) {
                switch (d.getCell(x, y)) {
                    case Dungeon.tileStoneWall:
                    case Dungeon.tileDirtWall:
                        dungeonMap.append(522);
                        break;
                    default:
                        dungeonMap.append(0);
                }
                dungeonMap.append(",");
            }
            dungeonMap.append("\n");
        }
        dungeonMap.deleteCharAt(dungeonMap.length() - 2);
        return dungeonMap.toString();
    }

    private String getDoor(Dungeon d) {
        StringBuilder dungeonMap = new StringBuilder();
        for (int y = 0; y < d.getYsize(); y++) {
            for (int x = 0; x < d.getXsize(); x++) {
                switch (d.getCell(x, y)) {
                    case Dungeon.tileDoor:
                        dungeonMap.append(273);
                        break;
                    default:
                        dungeonMap.append(0);
                }
                dungeonMap.append(",");
            }
            dungeonMap.append("\n");
        }
        dungeonMap.deleteCharAt(dungeonMap.length() - 2);
        return dungeonMap.toString();
    }

    private String getProp(Dungeon d) {
        StringBuilder dungeonMap = new StringBuilder();
        for (int y = 0; y < d.getYsize(); y++) {
            for (int x = 0; x < d.getXsize(); x++) {
                switch (d.getCell(x, y)) {
                    case Dungeon.tileUpStairs:
                    case Dungeon.tileDownStairs:
                        dungeonMap.append(49);
                        break;
                    case Dungeon.tileChest:
                        dungeonMap.append(625);
                        break;
                    default:
                        dungeonMap.append(0);
                }
                dungeonMap.append(",");
            }
            dungeonMap.append("\n");
        }
        dungeonMap.deleteCharAt(dungeonMap.length() - 2);
        return dungeonMap.toString();
    }

    private static class Formatter {
        private String wall;
        private String floor;
        private String doors;
        private String props;

        public Formatter(String floor, String wall, String doors, String props) {
            this.props = props;
            this.floor = floor;
            this.doors = doors;
            this.wall = wall;
        }

        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            for (int y = 0; y < DIM; y++) {
                for (int x = 0; x < DIM; x++) {
                    sb.append("0,");
                }
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 2);

            String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<map version=\"1.0\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\""+DIM+"\" height=\""+DIM+"\" tilewidth=\"48\" tileheight=\"48\" backgroundcolor=\"#000000\" nextobjectid=\"21\">\n"
                    + " <properties>\n"
                    + "  <property name=\"startX\" value=\"1\"/>\n"
                    + "  <property name=\"startY\" value=\"15\"/>\n"
                    + " </properties>\n"
                    + " <tileset firstgid=\"1\" name=\"uf_terrain\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"760\" columns=\"20\">\n"
                    + "  <image source=\"uf_terrain.png\" width=\"960\" height=\"1824\"/>\n"
                    + "  <tile id=\"115\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"115\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"116\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"117\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"118\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"135\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"135\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"136\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"137\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"138\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"155\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"155\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"156\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"157\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"158\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"175\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"175\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"176\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"177\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"178\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"195\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"195\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"196\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"197\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"198\" duration=\"300\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"215\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"215\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"216\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"217\" duration=\"500\"/>\n"
                    + "    <frame tileid=\"218\" duration=\"500\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"621\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"621\" duration=\"300\"/>\n"
                    + "    <frame tileid=\"622\" duration=\"300\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"644\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"644\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"645\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"664\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"664\" duration=\"200\"/>\n"
                    + "    <frame tileid=\"665\" duration=\"200\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"672\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"672\" duration=\"750\"/>\n"
                    + "    <frame tileid=\"673\" duration=\"750\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + "  <tile id=\"674\">\n"
                    + "   <animation>\n"
                    + "    <frame tileid=\"674\" duration=\"750\"/>\n"
                    + "    <frame tileid=\"675\" duration=\"750\"/>\n"
                    + "   </animation>\n"
                    + "  </tile>\n"
                    + " </tileset>\n"
                    + " <tileset firstgid=\"761\" name=\"uf_heroes\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"520\" columns=\"40\">\n"
                    + "  <image source=\"uf_heroes.png\" width=\"1920\" height=\"624\"/>\n"
                    + " </tileset>\n"
                    + " <layer name=\"floor\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"floor 2\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"props\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"creature\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"water_edges\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"shadows\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"walls\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"door\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   %s"
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"torches\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <layer name=\"webs\" width=\""+DIM+"\" height=\""+DIM+"\">\n"
                    + "  <data encoding=\"csv\">\n"
                    + "   " + sb.toString()
                    + "  </data>\n"
                    + " </layer>\n"
                    + " <objectgroup name=\"people\">\n"
                    + "  <object id=\"1\" name=\"Jason\" type=\"MERCHANT\" x=\"864\" y=\"96\" width=\"48\" height=\"48\">\n"
                    + "   <properties>\n"
                    + "    <property name=\"movement\" value=\"FOLLOW_AVATAR\"/>\n"
                    + "    <property name=\"startX\" value=\"18\"/>\n"
                    + "    <property name=\"startY\" value=\"2\"/>\n"
                    + "   </properties>\n"
                    + "  </object>\n"
                    + " </objectgroup>\n"
                    + "</map>";

            return String.format(template, floor, props, wall, doors);

        }
    }
}
