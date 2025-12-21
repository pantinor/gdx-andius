package andius.objects;

import static andius.objects.Item.ItemType.ARMOR;
import static andius.objects.Item.ItemType.GAUNTLET;
import static andius.objects.Item.ItemType.HELMET;
import static andius.objects.Item.ItemType.MISC;
import static andius.objects.Item.ItemType.SHIELD;
import static andius.objects.Item.ItemType.SPECIAL;
import static andius.objects.Item.ItemType.WEAPON;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Icons {

    private static final TextureRegion[] LOOKUP_TABLE = new TextureRegion[840];
    private static TextureRegion[][] REGIONS;
    public static final int QUESTION_MARK = 803;

    public static void init() {

        REGIONS = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/inventory.png")), 44, 44);
        Texture tx = new Texture(Gdx.files.classpath("assets/data/inventory.png"));
        for (int row = 0; row < tx.getHeight() / 44; row++) {
            for (int col = 0; col < tx.getWidth() / 44; col++) {
                LOOKUP_TABLE[row * tx.getWidth() / 44 + col] = REGIONS[row][col];
            }
        }
    }

    public static TextureRegion get(int idx) {
        return LOOKUP_TABLE[idx];
    }

    public static TextureRegion get(int row, int col) {
        return REGIONS[row][col];
    }

    public static TextureRegion get(Item it) {
        int iconId = getId(it);
        if (it != null) {
            it.iconID = iconId;
        }
        return LOOKUP_TABLE[iconId];
    }

    private static int getId(Item it) {
        if (it == null || it.name == null) {
            return QUESTION_MARK;
        }
        String n = it.name.toLowerCase();
        switch (it.type) {
            case WEAPON:
                if (n.contains("dagger")) {
                    return 103;
                }
                if (n.contains("long")) {
                    return 98;
                }
                if (n.contains("axe")) {
                    return 127;
                }
                if (n.contains("mace")) {
                    return 113;
                }
                if (n.contains("flail")) {
                    return 176;
                }
                if (n.contains("staff")) {
                    return 152;
                }
                return 96;
            case ARMOR:
                if (n.contains("leather")) {
                    return 311;
                }
                if (n.contains("chain")) {
                    return 300;
                }
                if (n.contains("plate")) {
                    return 304;
                }
                if (n.contains("breast")) {
                    return 198;
                }
                if (n.contains("robe")) {
                    return 354;
                }
                return 302;
            case SHIELD:
                return 271;
            case HELMET:
                if (n.contains("diadem")) {
                    return 226;
                }
                return 390;
            case GAUNTLET:
                if (n.contains("copper")) {
                    return 399;
                }
                return 402;
            case SPECIAL:
            case MISC:
                if (n.contains("scroll")) {
                    return 466;
                }
                if (n.contains("pot")) {
                    return 511;
                }
                if (n.contains("staff")) {
                    return 152;
                }
                if (n.contains("ring")) {
                    return 369;
                }
                if (n.contains("amulet")) {
                    return 377;
                }
                if (n.contains("key")) {
                    return 71;
                }
                if (n.contains("statue")) {
                    return 481;
                }
                if (n.contains("rod")) {
                    return 215;
                }
                if (n.contains("boot")) {
                    return 211;
                }
                return 420;
            default:
                return QUESTION_MARK;
        }
    }

}
