
import andius.Constants;
import andius.Context;
import andius.objects.ClassType;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.SaveGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import andius.WizardryData;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.io.File;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MonsterTest {

    static {

        GdxNativesLoader.load();
        Gdx.gl = mock(GL20.class);
        Gdx.graphics = new MockGraphics();
        Gdx.files = mock(Files.class);

        doAnswer(inv -> {
            String filename = inv.getArgument(0, String.class);

            File file = new File("src/main/resources", filename);
            if (!file.exists()) {
                file = new File("target/classes", filename);
            }

            return new FileHandle(file);
        }).when(Gdx.files).classpath(anyString());

        Constants.class.getClass();
        WizardryData.class.getClass();
    }

    @Test
    public void monsters() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/ultima-monsters.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Monster> monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
        }.getType());

        Collections.sort(monsters);

        // Header aligned to Monster.toString()’s column widths
        String header = String.format(
                "%s\t%-55s\t%-15s\t%-4s\t%-4s\t%-5s\t%-3s\t%-25s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                "ID",
                "NAME",
                "TYPE",
                "LVL",
                "EXP",
                "HPMX",
                "AC",
                "DAMG",
                "MAGE",
                "PRST",
                "SPED",
                "GOLD",
                "REWD",
                "LVLDR",
                "HEAL",
                "BRTH",
                "PARTID",
                "GRPSZ",
                "ICON"
        );
        //System.out.println(header);

        for (Monster m : monsters) {
            System.out.println(m);
        }
    }

    @Test
    public void items() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/kod-items.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Item> items = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
        }.getType());

        Collections.sort(items);

        System.out.println("NAME               \tTYPE     \tCOST     \tDAMG\tAC\tSWINGS\tSPELL     \tHITMOD\tREGN\tVENDOR\tUSABLE");
        for (Item m : items) {
            System.out.println(m);
        }

    }

    @Test
    public void testGenerateSaveGameTeamAtLevel() throws Exception {
        int lvl = 1;
        Context ctx = new Context();
        ctx.setSaveGame(new SaveGame());
        ctx.saveGame.players = new CharacterRecord[6];
        ctx.saveGame.players[0] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "FRED");
        ctx.saveGame.players[1] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "SAM");
        ctx.saveGame.players[2] = SaveGame.generatePlayer(lvl, ClassType.FIGHTER, "JACK");
        ctx.saveGame.players[3] = SaveGame.generatePlayer(lvl, ClassType.PRIEST, "JOE");
        ctx.saveGame.players[4] = SaveGame.generatePlayer(lvl, ClassType.MAGE, "JANE");
        ctx.saveGame.players[5] = SaveGame.generatePlayer(lvl, ClassType.THIEF, "FRANK");

        ctx.saveGame.saveGameFileName = "party-team.json";
        ctx.saveGame.write();
    }

    //@Test
    public void testGenerateSaveGameMageOnlyAtLevel() throws Exception {
        Context ctx = new Context();
        ctx.setSaveGame(new SaveGame());
        ctx.saveGame.players = new CharacterRecord[1];

        int level = 6;
        ctx.saveGame.players[0] = SaveGame.generatePlayer(level, ClassType.MAGE, "jane");

        assertEquals(ctx.saveGame.players[0].level, level);

        ctx.saveGame.map = Constants.Map.WORLD;
        ctx.saveGame.wx = 10;
        ctx.saveGame.wy = 54;

        ctx.saveGame.saveGameFileName = "party-mage.json";
        ctx.saveGame.write();
    }

}
