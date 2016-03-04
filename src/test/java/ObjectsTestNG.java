
import andius.Constants;
import andius.Constants.ArmorType;
import andius.Constants.ClassType;
import andius.Constants.Map;
import andius.Constants.Race;
import andius.Constants.WeaponType;
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.SaveGame;
import andius.objects.SaveGame.CharacterRecord;
import static junit.framework.Assert.assertEquals;
import org.testng.annotations.Test;

public class ObjectsTestNG {

    @Test
    public void testScript() throws Exception {
        Conversations convs = Conversations.init();
        for (Conversation c : convs.getConversations()) {
            System.out.println(c);
        }
    }

    @Test
    public void testReadSaveGame() throws Exception {

        SaveGame sg = new SaveGame();
        try {
            sg.read("test.sav");
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
        avatar.name = "Steve" ;
        avatar.race = Race.HUMAN;
        avatar.classType = ClassType.FIGHTER;
        avatar.health = 5;
        avatar.exp = 50;
        avatar.gold = 10456;
        avatar.weapon = WeaponType.ANOINT_FLAIL;
        avatar.armor = ArmorType.BREAST_PLATE;
        avatar.weapons[WeaponType.NONE.ordinal()] = 0xFF;
        avatar.armors[ArmorType.NONE.ordinal()] = 0xFE;
        avatar.weapons[WeaponType.values().length - 1] = 0xBE;
        avatar.armors[ArmorType.values().length - 1] = 0xAE;

        sg.players[0] = avatar;

        sg.write("test.sav");
        
        int level = avatar.checkLevel();
        int mxhp = avatar.getMaxHP();
        int x = 0;
    }

}
