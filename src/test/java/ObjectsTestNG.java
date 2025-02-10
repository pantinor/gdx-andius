
import andius.objects.Direction;
import andius.WizardryData;
import static andius.WizardryData.DUNGEON_DIM;
import andius.WizardryData.MazeLevel;
import static andius.WizardryData.WER_LEVEL_DESC;
import andius.objects.ClassType;
import andius.objects.Race;
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import andius.objects.Conversations.Topic;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.Reward;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import utils.SpreadFOV;
import utils.Utils;

public class ObjectsTestNG {

    //@Test
    public void testCompareArrays() throws Exception {

        
        
        byte[] array1 = IOUtils.readFully(new FileInputStream("D:\\applewin\\paul-formatted-2.dsk"), 143360);
        byte[] array2 = IOUtils.readFully(new FileInputStream("D:\\applewin\\paul-formatted.dsk"), 143360);

        boolean areEqual = Arrays.equals(array1, array2);
        System.out.println("Arrays are equal: " + areEqual);

        int length = Math.min(array1.length, array2.length);
        for (int i = 0; i < length; i++) {
            if (array1[i] != array2[i]) {
                System.out.printf("Difference at index %s: %s  %s\n", Integer.toHexString(i) ,String.format("%02X", array1[i]), String.format("%02X", array2[i]));
            }
        }

        if (array1.length != array2.length) {
            System.out.println("Arrays have different lengths.");
        }
    }

    @Test
    public void testMazeData() throws Exception {
        WizardryData.Scenario sc = WizardryData.Scenario.WER;

        for (MazeLevel l : sc.levels()) {
            System.out.println(WER_LEVEL_DESC[l.level - 1]);
            for (int x = 0; x < DUNGEON_DIM; x++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    WizardryData.MazeCell c = l.cells[x][y];
                    if (c.stairs || c.chute || c.teleport) {
                        String hx = String.format("%04x", (short) c.addressTo.column);
                    }
                }
            }
        }
    }

    //@Test
    public void testScript() throws Exception {
        Conversations convs = Conversations.init();
        for (Conversation c : convs.getConversations()) {
            System.out.println(c);
        }
    }

    //@Test
    public void testReadSaveGame() throws Exception {

        CharacterRecord avatar = new CharacterRecord();
        avatar.name = "Steve";
        avatar.race = Race.HUMAN;
        avatar.classType = ClassType.PRIEST;
        avatar.hp = avatar.getMoreHP();
        avatar.maxhp = avatar.hp;
        avatar.gold = Utils.getRandomBetween(100, 200);
        avatar.weapon = new Item();
        avatar.armor = new Item();
        avatar.inventory.add(new Item());
        avatar.inventory.add(new Item());
        avatar.intell = 12;
        avatar.piety = 12;
        avatar.level = 1;

        if (avatar.classType == ClassType.MAGE || avatar.classType == ClassType.BISHOP) {
            avatar.knownSpells.add(Spells.values()[1]);
            avatar.knownSpells.add(Spells.values()[3]);
            avatar.magePoints[0] = 2;
        }
        if (avatar.classType == ClassType.PRIEST) {
            avatar.knownSpells.add(Spells.values()[23]);
            avatar.knownSpells.add(Spells.values()[24]);
            avatar.clericPoints[0] = 2;
        }

        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
        //avatar.magePoints[0]--;
        avatar.clericPoints[0]--;
        avatar.clericPoints[0]--;
        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
        SaveGame.setSpellPoints(avatar);
        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
        avatar.exp = 1300;
        int ret = 0;
        while (ret >= 0) {
            ret = avatar.checkAndSetLevel();
        }
        System.out.printf("%d\t%d\t%d\n", avatar.level, avatar.exp, ret);
        for (int i = 1; i < 30; i++) {
            avatar.level = i;
            SaveGame.setSpellPoints(avatar);
            SaveGame.tryLearn(avatar);
            System.out.println("" + i + "\t" + Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
            System.out.println("" + i + "\t" + avatar.knownSpells);
        }
    }

    //@Test
    public void formatEnum() throws Exception {
        String s = "";

        String[] lines = s.split("\\r?\\n");
        for (String line : lines) {
            String[] items = line.split("\\t");
            //String camel = toCamelCase(items[1].trim());
            System.out.println(String.format("%s(\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\"),", items[0].trim().toUpperCase().replace(" ", "_"), items[1].trim(), items[2], items[3], items[4], items[5], items[6]));
        }

    }

    public static String toCamelCase(final String init) {
        if (init == null) {
            return null;
        }

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == init.length())) {
                ret.append(" ");
            }
        }

        return ret.toString();
    }

    //@Test
    public void readJson() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/assets/json/items.json");
        String json = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/rewards.json");
        String json2 = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/monsters.json");
        String json3 = IOUtils.toString(is);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Item> items = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        List<Reward> rewards = gson.fromJson(json2, new TypeToken<List<Reward>>() {
        }.getType());
        List<Monster> monsters = gson.fromJson(json3, new TypeToken<List<Monster>>() {
        }.getType());
        int x = 0;

        Collections.sort(items);

        java.util.Map<String, Item> itemMap = new HashMap<>();

        for (Item it : items) {
            System.out.println(it);
            itemMap.put(it.name, it);
        }

        for (Reward r : rewards) {

            int goldAmt = r.goldAmount();
            System.out.println(String.format("player found %d gold with %s", goldAmt, r.getId()));

            for (Reward.RewardDetails d : r.getRewardDetails()) {
                if (d.itemReward != null) {
                    Item min = items.get(d.itemReward.getMin());
                    Item mx = items.get(d.itemReward.getMax());
                    System.out.println(String.format("player finds a %s or a %s", min.name, mx.name));
                }

            }

        }

        Collections.sort(monsters);

        java.util.Map<String, Monster> MONSTER_MAP = new HashMap<>();
        java.util.Map<Integer, java.util.List<Monster>> MONSTER_LEVELS = new HashMap<>();

        for (int i = 0; i < 30; i++) {
            MONSTER_LEVELS.put(i, new ArrayList<>());
        }

        for (Monster m : monsters) {
            if (!MONSTER_MAP.containsKey(m.name)) {
                MONSTER_MAP.put(m.name, m);
            } else {
                // System.err.printf("DUPLICATE %s mid %d level %d\n", m.name, m.iconId, m.getLevel());
            }
            MONSTER_LEVELS.get(m.getLevel()).add(m);
        }

        for (Monster m : monsters) {
            System.out.printf("%s mid %d level %d mlvl %d  prlvl %s\n", m.name, m.monsterId, m.getLevel(), m.getMageSpellLevel(), m.getPriestSpellLevel());
        }
    }

    //@Test
    public void parseImage() throws Exception {
        BufferedImage input = ImageIO.read(new File("C:\\Users\\Paul\\Documents\\water\\Wizardry7-Mapd.png"));
        StringBuilder grass = new StringBuilder();
        StringBuilder water = new StringBuilder();
        Random ra = new Random();
        for (int y = 0; y < 173; y++) {
            for (int x = 0; x < 197; x++) {
                try {
                    int rgb = input.getRGB(x * 14, y * 14);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    if (x == 8 && y == 91) {
                        int u = 0;
                    }
                    if (r == 0 && g == 0 && b == 0) { //nothing
                        grass.append("0,");
                        water.append("0,");
                    } else if (r == 144 && g == 92 && b == 60) { //darker tile floor
                        int id = ra.nextInt(4) + 206;
                        grass.append("" + id).append(",");
                        water.append("0,");
                    } else if (r == 160 && g == 120 && b == 56) { //path
                        int id = ra.nextInt(3) + 203;
                        grass.append("" + id).append(",");
                        water.append("0,");
                    } else if (r == 0 && g == 0 && b >= 72) { //water
                        grass.append("0,");
                        water.append("176,");
                    } else { //ground
                        int id = ra.nextInt(4) + 122;
                        grass.append("" + id).append(",");
                        water.append("0,");
                    }

                } catch (Exception e) {
                    System.err.printf("wrong coord %d %d\n", x, y);
                }
            }
            grass.append("\n");
            water.append("\n");

        }
        System.out.println("<data encoding=\"csv\">\n");
        System.out.println(grass.toString().trim());
        System.out.println("</data>\n\n\n");

        System.out.println("<data encoding=\"csv\">\n");
        System.out.println(water.toString().trim());
        System.out.println("</data>\n");
    }

    //@Test
    public void parseU5TLK() throws Exception {

        Conversations convs = new Conversations();

        FileInputStream fstream = new FileInputStream("u5tr.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        //String temp = "";
        //BufferedReader br = new BufferedReader(new StringReader(temp));

        boolean end = false;
        while (!end) {
            Conversation c = new Conversation();
            convs.getConversations().add(c);

            boolean texts = false;
            boolean labels = false;
            Label label = null;
            String s;
            while ((s = br.readLine()) != null) {
                if (s.length() < 1) {
                    continue;
                }

                s = s.replace("<New Line>", "");
                s = s.replace("<PAUSE>", "");
                s = s.replace("<Key wait>", "");
                s = s.replace("<Nothing>", "");
                s = s.replace("\"", "");
                s = s.replace("    ", " ");
                s = s.replace("<AVATAR NAME>", "%NAME%");
                s = s.replace("<", "%");
                s = s.replace(">", "%");
                s = s.replace("Goto Label ", "");
                s = s.replace("%END CONVERSATION%", "");
                s = s.replace("%If/Else Knows Name%", "");
                s = s.replace("%Ask Name%", "");
                s = s.replace("%Rune%", "");

                if (s.contains(":")) {
                    String[] toks = s.split(":");
                    if (toks[0].startsWith("Name")) {
                        c.setName(toks[1].trim());
                        continue;
                    }
                    if (toks[0].startsWith("Description")) {
                        c.setDescription(toks[1].trim());
                        continue;
                    }
                    if (toks[0].startsWith("Greeting")) {
                        if (toks.length == 2) {
                            c.getTopics().add(new Topic("greeting", toks[1].trim()));
                        }
                        continue;
                    }
                    if (toks[0].startsWith("Job")) {
                        if (toks.length == 2) {
                            c.getTopics().add(new Topic("job", toks[1].trim()));
                        }
                        continue;
                    }
                    if (toks[0].startsWith("Goodbye")) {
                        if (toks.length == 2) {
                            c.getTopics().add(new Topic("bye", toks[1].trim()));
                        }
                        continue;
                    }
                    if (toks[0].startsWith("Text information")) {
                        texts = true;
                        labels = false;
                        continue;
                    }
                    if (toks[0].startsWith("Label information")) {
                        labels = true;
                        texts = false;
                        continue;
                    }
                    if (texts && toks.length == 2) {
                        c.getTopics().add(new Topic(toks[0].trim().toLowerCase(), toks[1].trim()));
                    }
                    if (labels && toks.length == 2) {
                        String t = toks[0].trim().toLowerCase();
                        label.getTopics().add(new Topic(t, toks[1].trim()));
                    }
                } else {

                    if (labels) {
                        if (s.contains("Label")) {
                            label = new Label();
                            label.setId(s.substring(s.indexOf("Label") + 6, s.indexOf("Label") + 7));
                            label.setQuery(s.substring(s.indexOf("Label") + 8).trim());
                            c.getLabels().add(label);
                            continue;
                        }
                    }
                    if (!s.contains(":") && c.getName() != null) {
                        break;
                    }
                }
            }

            if (br.readLine() == null) {
                end = true;
            }
        }
        br.close();

        String tmp = "Castle Britannia 	9 	#Alistair, #Chuckles, #Desiree, #Drudgeworth, #Margaret, #Stephen, #Saduj, #Stillwelt, #Treanna\n"
                + "Empath Abbey 	7 	#Barbra, #Cory, #Hardluck, #Julia, #Lord Michael, #Tim, #Toshi\n"
                + "The Lycaeum 	7 	#Lady Hayden, #Lady Janell, #Lord R'hien, #Lord Shalineth, #Mariah, #Rollo, #Sir Sean\n"
                + "Serpent's Hold 	6 	#Gardner, #Kristi, #Lord Malone, #Loubet, #Maxwell, #Toede\n"
                + "Palace of Blackthorn 	7 	#Foulwell, #Gallrot, #Gorn, #Hassad, #Kraw, #Blackthorn, #Weblock, #Unnamed prisoner\n"
                + "Britain 	7 	#Annon, #Eb, #Greyson, #Gwenno, #Justin, #Telila, #Terrance\n"
                + "Yew 	10 	#Aleyn, #Chamfort, #Felespar, #Greymarch, #Jaana, #Jeremy, #Jerone, #Judge Dryden, #Landon, #Mario\n"
                + "Trinsic 	4 	#Gruman, #Jimmy, #Sindar, #Woolfe\n"
                + "Minoc 	6 	#Delwyn, #Fenelon, #Fiona, #Lady Sahra, #Rew, #Tactus\n"
                + "Moonglow 	5 	#Donn Piatt, #Lord Stuart the Hungry, #Malifora, #Malik, #Zachariah\n"
                + "Skara Brae 	4 	#Flain, #Froed, #Kindor, #Saul\n"
                + "New Magincia 	8 	#Fumiko, #Kaiko, #Katrina, #Shirita, #Tetsuo, #Tomoka, #Wartow, #Yasuda\n"
                + "Jhelom 	4 	#Bullwier, #Goeth, #Thorne, #Trian\n"
                + "Greyhaven (Trinsic) 	5 	#Anthony, #Charlotte, #David, #Sir Arbuthnot, #Kenneth\n"
                + "Stormcrow (Minoc) 	2 	#Emilly, #Windmire\n"
                + "Fogsbane (Britain) 	2 	#Jennifer, #Jotham\n"
                + "Waveguide (Moonglow) 	2 	#Gregory, #Jacqueline\n"
                + "West Britanny 	3 	#Camile, #Christopher, #Phillip\n"
                + "North Britanny 	5 	#Joshua, #Kurt, #Leof, #Thentis, #Vigil\n"
                + "East Britanny 	3 	#Sir Adam, #Flint, #Squire Jimmy\n"
                + "Paws 	2 	#Bandaii, #Glinkie\n"
                + "Cove 	3 	#Ambrose, #Ava, #Leona\n"
                + "Buccaneer's Den 	7 	#Bidney, #Geoffrey, #Lord Dalgrin, #Scally, #Sven, #Thorkin, #Tierra\n"
                + "Bordermarch 	4 	#Dupre, #Lady Tessa, #Sentri, #Sir Simon\n"
                + "Farthing 	4 	#Dufus, #Quintin, #Seggallion, #Temme\n"
                + "Windemere 	2 	#Elistaria, #Thrud\n"
                + "Stonegate 	1 	#Balinor\n"
                + "Iolo's hut 	1 	#Smith the Horse\n"
                + "Sin'Vraal's hut 	1 	#Sin'Vraal\n"
                + "Grendel's hut 	1 	#Grendel\n"
                + "Sutek's hut 	1 	#Sutek\n"
                + "Ararat shipwreck 	1 	#Captain Johne";

        BufferedReader br2 = new BufferedReader(new StringReader(tmp));
        String s;
        java.util.Map<String, String[]> mapChars = new HashMap<>();

        while ((s = br2.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s, "\t");
            String map = st.nextToken().trim();
            st.nextToken();
            String c = st.nextToken().trim();
            c = c.replace("#", "");
            String[] names = c.split(",");
            mapChars.put(map, names);
        }

        Iterator<Conversation> it = convs.getConversations().iterator();
        while (it.hasNext()) {
            Conversation c = it.next();
            for (String map : mapChars.keySet()) {
                String[] names = mapChars.get(map);
                for (String n : names) {
                    if (n.contains(c.getName()) || c.getName().contains(n)) {
                        c.setMap(map);
                    }
                }

            }
        }

        Iterator<Conversation> it2 = convs.getConversations().iterator();
        while (it2.hasNext()) {
            Conversation c = it2.next();
            if (c.getMap() == null) {
                c.setMap("LLECHY");
            }
        }

        JAXBContext context = JAXBContext.newInstance(Conversations.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(convs, System.out);

    }

    //@Test
    public void testAttack() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/assets/json/items.json");
        String json = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/monsters.json");
        String json3 = IOUtils.toString(is);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Item> items = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());

        List<Monster> monsters = gson.fromJson(json3, new TypeToken<List<Monster>>() {
        }.getType());

        java.util.Map<String, Item> itemsMap = new HashMap<>();
        for (Item i : items) {
            itemsMap.put(i.name, i);
        }

        CharacterRecord avatar = new CharacterRecord();
        avatar.name = "Steve";
        avatar.race = Race.HUMAN;
        avatar.classType = ClassType.FIGHTER;
        avatar.hp = 100;
        avatar.maxhp = avatar.hp;

        avatar.armor = itemsMap.get("CHAIN MAIL").clone();
        avatar.weapon = itemsMap.get("MACE +1").clone();
        avatar.helm = itemsMap.get("HELM").clone();
        avatar.shield = itemsMap.get("LARGE SHIELD").clone();
        //avatar.glove = itemsMap.get("SILVER GLOVES").clone();
        //avatar.item1 = itemsMap.get("ROD OF FLAME").clone();
        //avatar.item2 = itemsMap.get("WERDNAS AMULET").clone();

        avatar.str = 16;
        avatar.agility = 16;
        avatar.luck = 12;

        avatar.level = 1;

        for (int x = 0; x < 20; x++) {
            MutableMonster mm = new MutableMonster(monsters.get(85));
            Utils.attackHit(mm, avatar);
            Utils.attackHit(avatar, mm);
        }

        for (Monster m : monsters) {
            MutableMonster mm = new MutableMonster(m);
            //Utils.attackHit(mm, avatar);
            //Utils.attackHit(avatar, mm);
        }
    }

    @Test
    public void testDirection() throws Exception {

        int mask = 0;
        mask = Direction.addToMask(Direction.NORTH, mask);

        assertTrue(Direction.isDirInMask(Direction.NORTH, mask));
        assertFalse(Direction.isDirInMask(Direction.NORTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.NORTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH, mask));

        mask = Direction.addToMask(Direction.NORTH_WEST, mask);
        assertTrue(Direction.isDirInMask(Direction.NORTH, mask));
        assertTrue(Direction.isDirInMask(Direction.NORTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.NORTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH, mask));

        mask = Direction.removeFromMask(mask, Direction.NORTH_WEST);
        assertTrue(Direction.isDirInMask(Direction.NORTH, mask));
        assertFalse(Direction.isDirInMask(Direction.NORTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.NORTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_WEST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH_EAST, mask));
        assertFalse(Direction.isDirInMask(Direction.SOUTH, mask));

    }

    @Test
    public void testFOVWrapping() throws Exception {

        float[][] shadowMap = new float[][]{
            {1, 1, 1, 1, 0, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 1},
            {0, 0, 1, 0, 0, 0, 0, 1, 0},
            {1, 0, 1, 0, 0, 0, 0, 1, 1},
            {1, 1, 1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 1, 0, 0, 0, 1},
            {1, 1, 1, 1, 0, 1, 1, 1, 1},};

        SpreadFOV fov = new SpreadFOV(shadowMap);

        fov.calculateFOV(4, 1, 5);

        //visible
        assertTrue(fov.light(1, 1) > 0);
        assertTrue(fov.light(2, 1) > 0);
        assertTrue(fov.light(3, 1) > 0);
        assertTrue(fov.light(4, 1) > 0);
        assertTrue(fov.light(5, 1) > 0);
        assertTrue(fov.light(4, 0) > 0);

        //not visible
        assertTrue(fov.light(7, 1) <= 0);
        assertTrue(fov.light(4, 3) <= 0);
        assertTrue(fov.light(8, 1) <= 0);

        float[][] m = fov.lightMap();
        assertTrue(m[4 + 9][8] > 0);//wrapped

    }

}
