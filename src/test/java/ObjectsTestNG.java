
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

public class ObjectsTestNG {

    @Test
    public void testScript() throws Exception {
        Conversations convs = Conversations.init();
        for (Conversation c : convs.getConversations()) {
            System.out.println(c);
        }
    }

    //@Test
    public void testReadSaveGame() throws Exception {

        SaveGame sg = null;
        try {
            sg = SaveGame.read("party.sav");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        assertEquals(Map.WORLD.ordinal(), sg.map);
//        assertEquals(157, sg.wx, 157);
//        assertEquals(56, sg.wy, 56);
//        assertEquals(5, sg.players[0].health);
//        assertEquals(10456, sg.players[0].gold);
//        assertEquals(2502, sg.players[0].exp);
//        assertEquals(WeaponType.ANOINT_FLAIL, sg.players[0].weapon);
//        assertEquals(ArmorType.BREAST_PLATE, sg.players[0].armor);
        sg.map = Map.WORLD.ordinal();
        sg.wx = 157;
        sg.wy = 56;

        CharacterRecord avatar = new CharacterRecord();
        avatar.name = "Steve";
        avatar.race = Race.HUMAN;
        avatar.classType = ClassType.FIGHTER;
        avatar.hp = 5;
        avatar.exp = 50;
        avatar.gold = 10456;
        avatar.weapon = new Item();
        avatar.armor = new Item();
        avatar.inventory.add(new Item());
        avatar.inventory.add(new Item());

        sg.players[0] = avatar;

        sg.write("test.sav");

        int level = avatar.calculateLevel();
        int mxhp = avatar.getMoreHP();
        int x = 0;
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

    @Test
    public void readJson() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/assets/json/items-json.txt");
        String json = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/rewards-json.txt");
        String json2 = IOUtils.toString(is);

        is = this.getClass().getResourceAsStream("/assets/json/monsters-json.txt");
        String json3 = IOUtils.toString(is);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Item> items = gson.fromJson(json, new TypeToken<List<Item>>() {}.getType());
        List<Reward> rewards = gson.fromJson(json2, new TypeToken<List<Reward>>() {}.getType());
        List<Monster> monsters = gson.fromJson(json3, new TypeToken<List<Monster>>() {}.getType());
        int x = 0;
    }

}
