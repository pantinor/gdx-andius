package andius.objects;

import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Locale;

public final class Portraits {
    private static TextureRegion[][][] regions;

    private Portraits() { }

    public static String key(Gender gender, Race race, ClassType characterClass) {
        return ((gender == null ? Gender.MALE : gender).name() + "-"
                + (race == null ? Race.HUMAN : race).name() + "-"
                + (characterClass == null ? ClassType.FIGHTER : characterClass).name()).toLowerCase(Locale.ROOT);
    }

    public static void init(TextureAtlas atlas) {
        TextureRegion[][][] loaded = new TextureRegion[Gender.values().length][Race.values().length][ClassType.values().length];
        for (Gender gender : Gender.values()) {
            for (Race race : Race.values()) {
                for (ClassType characterClass : ClassType.values()) {
                    String name = key(gender, race, characterClass);
                    TextureRegion region = atlas.findRegion(name);
                    if (region == null) throw new IllegalArgumentException("Missing character portrait: " + name);
                    loaded[gender.ordinal()][race.ordinal()][characterClass.ordinal()] = region;
                }
            }
        }
        regions = loaded;
    }

    public static TextureRegion get(CharacterRecord character) {
        return get(character.gender, character.race, character.classType);
    }

    public static TextureRegion get(Gender gender, Race race, ClassType characterClass) {
        if (regions == null) throw new IllegalStateException("Portrait atlas has not been loaded");
        return regions[(gender == null ? Gender.MALE : gender).ordinal()]
                [(race == null ? Race.HUMAN : race).ordinal()]
                [(characterClass == null ? ClassType.FIGHTER : characterClass).ordinal()];
    }
}
