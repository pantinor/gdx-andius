
import andius.objects.ClassType;
import andius.Constants.Map;
import andius.objects.Race;
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.Reward;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import andius.objects.Spells;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import utils.Utils;

public class ObjectsTestNG {

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
        avatar.classType = ClassType.MAGE;
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

        if (avatar.classType == ClassType.MAGE || avatar.classType == ClassType.WIZARD) {
            avatar.knownSpells.add(Spells.values()[1]);
            avatar.knownSpells.add(Spells.values()[3]);
            avatar.magePoints[0] = 2;
        }
        if (avatar.classType == ClassType.CLERIC) {
            avatar.knownSpells.add(Spells.values()[23]);
            avatar.knownSpells.add(Spells.values()[24]);
            avatar.clericPoints[0] = 2;
        }
        
//        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
//        avatar.magePoints[0] --;
//        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
//        SaveGame.setSpellPoints(avatar);
//        System.out.println(Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));

//            avatar.exp = 1300;
//            int ret = 0;
//            while( ret >= 0) {
//                ret = avatar.checkAndSetLevel();
//            }
//            System.out.printf("%d\t%d\t%d\n",avatar.level, avatar.exp, ret);
        

//        for (int i = 1; i < 30; i++) {
//            avatar.level = i;
//            SaveGame.setSpellPoints(avatar);
//            SaveGame.tryLearn(avatar);
//            System.out.println("" + i + "\t" + Arrays.toString(avatar.magePoints) + "\t" + Arrays.toString(avatar.clericPoints));
//            System.out.println("" + i + "\t" + avatar.knownSpells);
//        }

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

        InputStream is = this.getClass().getResourceAsStream("/assets/json/items-json.txt");
        String json = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/rewards-json.txt");
        String json2 = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/monsters-json.txt");
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

}
