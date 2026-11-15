import andius.objects.ClassType;
import andius.objects.Gender;
import andius.objects.Portraits;
import andius.objects.Race;
import andius.objects.SaveGame;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PortraitsTest {
    private TextureAtlas atlas() throws Exception {
        TextureAtlas atlas = new TextureAtlas();
        com.badlogic.gdx.graphics.Texture texture = org.mockito.Mockito.mock(com.badlogic.gdx.graphics.Texture.class);
        org.mockito.Mockito.when(texture.getWidth()).thenReturn(152);
        org.mockito.Mockito.when(texture.getHeight()).thenReturn(152);
        for (String line : Files.readAllLines(Path.of("src/main/resources/assets/data/portraits.atlas"))) {
            if (!line.matches("(?:male|female)-[a-z]+-[a-z]+")) continue;
            TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(new TextureRegion(texture));
            region.name = line;
            atlas.getRegions().add(region);
        }
        return atlas;
    }

    @Test public void everyCharacterCombinationHasTheCorrectNamedPortrait() throws Exception {
        TextureAtlas atlas = atlas();
        Portraits.init(atlas);
        Set<String> names = new HashSet<>();
        for (Gender gender : Gender.values()) for (Race race : Race.values()) for (ClassType type : ClassType.values()) {
            String key = Portraits.key(gender, race, type);
            assertTrue(names.add(key), "Duplicate portrait key " + key);
            SaveGame.CharacterRecord character = new SaveGame.CharacterRecord();
            character.gender = gender; character.race = race; character.classType = type;
            assertSame(Portraits.get(character), atlas.findRegion(key), key);
            character.classType = ClassType.values()[(type.ordinal() + 1) % ClassType.values().length];
            assertSame(Portraits.get(character), atlas.findRegion(Portraits.key(gender, race, character.classType)));
        }
        assertEquals(names.size(), 80);
        assertEquals(atlas.getRegions().size, names.size());
        assertEquals(Portraits.key(null, null, null), "male-human-fighter");
    }

    @Test public void incompleteAtlasFailsWithTheMissingKey() throws Exception {
        TextureAtlas atlas = atlas();
        String removed = atlas.getRegions().pop().name;
        try {
            Portraits.init(atlas);
            fail("Missing portrait must fail initialization");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains(removed));
        }
    }

    @Test public void oldSavesLoadAndNewSavesStoreAttributesWithoutPortraitIndices() throws Exception {
        Path file = Files.createTempFile("andius-portrait-migration-", ".json");
        try {
            Files.writeString(file, """
                {"players":[{"name":"Legacy","portaitIndex":207,"race":"ELF","classType":"MAGE"},
                {"name":"New","gender":"FEMALE","race":"DWARF","classType":"PRIEST"},
                {"name":"Null gender","gender":null}]}
                """);
            SaveGame save = SaveGame.read(file.toString());
            assertEquals(save.players[0].gender, Gender.MALE);
            assertEquals(save.players[1].gender, Gender.FEMALE);
            assertEquals(save.players[2].gender, Gender.MALE);
            String json = new Gson().toJson(save);
            assertFalse(json.contains("portaitIndex"));
            assertFalse(json.contains("portraitIndex"));
            SaveGame copy = new Gson().fromJson(json, SaveGame.class);
            assertEquals(Portraits.key(copy.players[0].gender, copy.players[0].race, copy.players[0].classType), "male-elf-mage");
            assertEquals(Portraits.key(copy.players[1].gender, copy.players[1].race, copy.players[1].classType), "female-dwarf-priest");
        } finally { Files.deleteIfExists(file); }
    }
}
