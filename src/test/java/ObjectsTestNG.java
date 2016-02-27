
import andius.Constants.ArmorType;
import andius.Constants.Map;
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

    //@Test
    public void testReadSaveGame() throws Exception {

        SaveGame sg = new SaveGame();
        try {
            sg.read("test.sav");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        assertEquals(Map.WORLD.ordinal(), sg.map);
        assertEquals(157, sg.wx, 157);
        assertEquals(56, sg.wy, 56);
        assertEquals(199, sg.players[0].health);
        assertEquals(2, sg.players[0].mana);
        assertEquals(10456, sg.players[0].gold);
        assertEquals(2502, sg.players[0].exp);
        assertEquals(WeaponType.AXE, sg.players[0].weapon);
        assertEquals(ArmorType.CHAIN, sg.players[0].armor);


        sg.map = Map.WORLD.ordinal();
        sg.wx = 157;
        sg.wy = 56;
        
        CharacterRecord avatar = new CharacterRecord();
        avatar.name = "Steve" ;
        avatar.health = 199;
        avatar.exp = 2502;
        avatar.gold = 10456;
        avatar.mana = 2;
        avatar.weapon = WeaponType.AXE;
        avatar.armor = ArmorType.CHAIN;
        avatar.weapons[WeaponType.NONE.ordinal()] = 0xFF;
        avatar.armors[ArmorType.NONE.ordinal()] = 0xFE;
        avatar.weapons[WeaponType.EXOTIC.ordinal()] = 0xBE;
        avatar.armors[ArmorType.EXOTIC.ordinal()] = 0xAE;

        sg.players[0] = avatar;

        sg.write("test.sav");
    }

}
