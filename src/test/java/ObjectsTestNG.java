
import andius.objects.ArmorType;
import andius.objects.ClassType;
import andius.Constants.Map;
import andius.objects.Race;
import andius.objects.WeaponType;
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
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
            sg = SaveGame.read("test.sav");
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
        avatar.weapon = WeaponType.DAGGER;
        avatar.armor = ArmorType.BREAST_PLATE;
        avatar.weapons.put(WeaponType.DAGGER, 1);
        avatar.armors.put(ArmorType.ROBES, 1);

        sg.players[0] = avatar;

        sg.write("test.sav");

        int level = avatar.calculateLevel();
        int mxhp = avatar.calculateMaxHP();
        int x = 0;
    }

    @Test
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

}
