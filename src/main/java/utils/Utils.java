package utils;

import andius.Constants;
import static andius.Constants.Ability.AUTOKILL;
import static andius.Constants.Ability.PARALYZE;
import static andius.Constants.Ability.POISON;
import static andius.Constants.Ability.SLEEP;
import static andius.Constants.Ability.STONE;
import andius.Constants.Status;
import andius.objects.Direction;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.Mutable;
import andius.objects.MutableCharacter;
import andius.objects.MutableMonster;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.Random;

public class Utils {

    public static FileHandleResolver CLASSPTH_RSLVR = (String fileName) -> Gdx.files.classpath(fileName);

    public static final Random RANDOM = new Random();

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        if (low == high) {
            return low;
        }
        return RANDOM.nextInt(high - low) + low;
    }

    public static boolean randomBoolean() {
        return RANDOM.nextInt(100) < 50;
    }

    public static boolean percentChance(int percent) {
        return RANDOM.nextInt(100 + 1) <= percent;
    }

    public static int intValue(byte b1) {
        return b1 & 0xFF;
    }

    public static int intValue(byte b1, byte b2) {
        return intValue(b1) + intValue(b2) * 256;
    }

    public static int adjustValueMax(int v, int amt, int max) {
        v += amt;
        if (v > max) {
            v = max;
        }
        return v;
    }

    public static int adjustValueMin(int v, int amt, int min) {
        v += amt;
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int adjustValue(int v, long amt, int max, int min) {
        v += amt;
        if (v > max) {
            v = max;
        }
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static boolean distanceLessThan(float val, float x, float y) {
        return Math.abs(x - y) <= val;
    }

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

    public static Direction getPath(int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        // find the directions that lead [to/away from] our target 
        int directionsToObject = towards ? getRelativeDirection(toX, toY, fromX, fromY) : ~getRelativeDirection(toX, toY, fromX, fromY);

        // make sure we eliminate impossible options 
        directionsToObject &= validMovesMask;

        // get the new direction to move 
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } else {
            // there are no valid directions that lead to our target            
            return Direction.getRandomValidDirection(validMovesMask);
        }
    }

    public static int movementDistance(int fromX, int fromY, int toX, int toY) {
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(toX, toY, fromX, fromY);

        while (fromX != toX || fromY != toY) {

            if (fromX != toX) {
                if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                    fromX--;
                } else {
                    fromX++;
                }
                dist++;
            }
            if (fromY != toY) {
                if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                    fromY--;
                } else {
                    fromY++;
                }
                dist++;
            }

        }

        return dist;
    }

    public static int getRelativeDirection(int toX, int toY, int fromX, int fromY) {
        int dx = 0, dy = 0;
        int dirmask = 0;

        /* adjust our coordinates to find the closest path */
        dx = fromX - toX;
        dy = fromY - toY;

        /* add x directions that lead towards to_x to the mask */
        if (dx < 0) {
            dirmask |= Direction.EAST.mask();
        } else if (dx > 0) {
            dirmask |= Direction.WEST.mask();
        }

        /* add y directions that lead towards to_y to the mask */
        if (dy < 0) {
            dirmask |= Direction.SOUTH.mask();
        } else if (dy > 0) {
            dirmask |= Direction.NORTH.mask();
        }

        /* return the result */
        return dirmask;
    }

    public static boolean attackHit(Mutable attacker, Mutable defender) {
        int roll = RANDOM.nextInt(20) + 1;
        int chanceToHit = 20 - defender.getArmourClass() + defender.getACModifier() - attacker.hitModifier();
        chanceToHit = Math.max(1, Math.min(chanceToHit, 19));
        return roll >= chanceToHit;
    }

    public static boolean attackHit(CharacterRecord attacker, Mutable defender) {
        int roll = RANDOM.nextInt(20) + 1;
        int chanceToHit = 20 - defender.getArmourClass() + defender.getACModifier() - attacker.attackHitModifier();
        chanceToHit = Math.max(1, Math.min(chanceToHit, 19));
        return roll >= chanceToHit;
    }

    public static boolean attackHit(Mutable attacker, CharacterRecord defender) {
        int roll = RANDOM.nextInt(20) + 1;
        int chanceToHit = 20 - defender.calculateAC() - defender.defenseHitModifier() - attacker.hitModifier();
        chanceToHit = Math.max(1, Math.min(chanceToHit, 19));
        return roll >= chanceToHit;
    }

    public static int dealDamage(CharacterRecord attacker, Item weapon, Mutable defender) {

        if (weapon.autokill) {
            int critChance = Math.min(attacker.level * 2, 50);
            if (RANDOM.nextInt(100) < critChance && RANDOM.nextInt(35) > defender.getLevel() + 10) {
                defender.adjustHitPoints(defender.getMaxHitPoints());
                defender.adjustHealthCursor();
                return defender.getMaxHitPoints();
            }
        }

        int damage = weapon.damage.roll();

        if (defender.status().has(Status.ASLEEP)) {
            damage = damage * 2;
        }

        if (weapon.purposed(defender.getMonsterType())) {
            damage = damage * 2;
        }

        defender.adjustHitPoints(-damage);
        defender.adjustHealthCursor();

        return damage;
    }

    public static boolean inflict(MutableMonster attacker, MutableCharacter defender, Loggable logs) {

        if (defender == null) {
            return false;
        }

        for (Constants.Ability ability : attacker.monster().ability) {
            switch (ability) {
                case AUTOKILL:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        DoGooder dg = (DoGooder) defender.baseType();
                        if (dg.savingThrowDeath()) {
                            logs.add(String.format("%s made a saving throwing throw against %s", defender.name(), "DEATH"), Color.YELLOW);
                        } else {
                            defender.adjustHitPoints(defender.getCurrentHitPoints());
                            defender.getHealthCursor().adjust(defender.getCurrentHitPoints(), defender.getMaxHitPoints());
                            logs.add(String.format("%s was instantly killed by %s!", defender.name(), attacker.name()), Color.SCARLET);
                            return true;
                        }
                    }
                    break;
                case STONE:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        DoGooder dg = (DoGooder) defender.baseType();
                        if (dg.savingThrowPetrify()) {
                            logs.add(String.format("%s made a saving throwing throw against %s", defender.name(), "PETRIFICATION"), Status.STONED.getColor());
                        } else {
                            defender.status().set(Status.STONED, 100);
                            logs.add(String.format("%s was petrified by %s!", defender.name(), attacker.name()), Status.STONED.getColor());
                            return true;
                        }
                    }
                    break;
                case POISON:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        defender.status().set(Status.POISONED, 5);
                        logs.add(String.format("%s was poisoned by %s!", defender.name(), attacker.name()), Status.POISONED.getColor());
                        return true;
                    }
                    break;
                case PARALYZE:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        defender.status().set(Status.PARALYZED, 3);
                        logs.add(String.format("%s was paralyzed by %s!", defender.name(), attacker.name()), Status.PARALYZED.getColor());
                        return true;
                    }
                    break;
                case SLEEP:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.RANDOM.nextInt(100) < 15) {
                        defender.status().set(Status.ASLEEP, 3);
                        logs.add(String.format("%s was slept by %s!", defender.name(), attacker.name()), Status.ASLEEP.getColor());
                        return true;
                    }
                    break;
            }

        }
        return false;
    }

    public static boolean inflict(MutableMonster attacker, CharacterRecord defender, Loggable logs) {

        if (defender == null) {
            return false;
        }

        for (Constants.Ability ability : attacker.monster().ability) {
            switch (ability) {
                case AUTOKILL:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        if (defender.savingThrowDeath()) {
                            logs.add(String.format("%s made a saving throwing throw against %s", defender.name, "DEATH"), Color.YELLOW);
                        } else {
                            defender.adjustHP(-defender.hp);
                            logs.add(String.format("%s was instantly killed by %s!", defender.name, attacker.name()), Color.SCARLET);
                            return true;
                        }
                    }
                    break;
                case STONE:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        if (defender.savingThrowPetrify()) {
                            logs.add(String.format("%s made a saving throwing throw against %s", defender.name, "PETRIFICATION"), Status.STONED.getColor());
                        } else {
                            defender.status.set(Status.STONED, 100);
                            logs.add(String.format("%s was petrified by %s!", defender.name, attacker.name()), Status.STONED.getColor());
                            return true;
                        }
                    }
                    break;
                case POISON:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        defender.status.set(Status.POISONED, 5);
                        logs.add(String.format("%s was poisoned by %s!", defender.name, attacker.name()), Status.POISONED.getColor());
                        return true;
                    }
                    break;
                case PARALYZE:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.percentChance(attacker.getLevel() * 5)) {
                        defender.status.set(Status.PARALYZED, 3);
                        logs.add(String.format("%s was paralyzed by %s!", defender.name, attacker.name()), Status.PARALYZED.getColor());
                        return true;
                    }
                    break;
                case SLEEP:
                    if (attacker.getPercentDamaged() < 0.50 && Utils.RANDOM.nextInt(100) < 15) {
                        defender.status.set(Status.ASLEEP, 3);
                        logs.add(String.format("%s was slept by %s!", defender.name, attacker.name()), Status.ASLEEP.getColor());
                        return true;
                    }
                    break;
            }

        }
        return false;
    }

    public static Vector2 centerOfMass(TextureRegion tr) {

        int sx = tr.getRegionX();
        int sy = tr.getRegionY();
        int w = tr.getRegionWidth();
        int h = tr.getRegionHeight();

        if (!tr.getTexture().getTextureData().isPrepared()) {
            tr.getTexture().getTextureData().prepare();
        }
        Pixmap p = tr.getTexture().getTextureData().consumePixmap();

        int x1 = 0, x2 = 0, y1 = 0, y2 = 0;

        for (int x = sx; x < sx + h; x++) {
            for (int y = sy; y < sy + h; y++) {
                if (p.getPixel(x, y) != 0) {
                    x1 = x;
                    break;
                }
            }
        }
        for (int x = sx + w - 1; x >= sx; x--) {
            for (int y = sy; y < sy + h; y++) {
                if (p.getPixel(x, y) != 0) {
                    x2 = x;
                    break;
                }
            }
        }
        for (int y = sy; y < sy + h; y++) {
            for (int x = sx; x < sx + w; x++) {
                if (p.getPixel(x, y) != 0) {
                    y1 = y;
                    break;
                }
            }
        }
        for (int y = sy + h - 1; y >= sy; y--) {
            for (int x = sx; x < sx + w; x++) {
                if (p.getPixel(x, y) != 0) {
                    y2 = y;
                    break;
                }
            }
        }

        p.dispose();

        int cx = x1 + (x2 - x1) / 2 - sx;
        int cy = y1 + (y2 - y1) / 2 - sy;

        return new Vector2(cx, h - cy);
    }

    public static Texture rotate90(Texture t) {
        if (!t.getTextureData().isPrepared()) {
            t.getTextureData().prepare();
        }
        Pixmap p = t.getTextureData().consumePixmap();
        int width = p.getWidth();
        int height = p.getHeight();
        Pixmap rotatedPix = new Pixmap(height, width, p.getFormat());

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                rotatedPix.drawPixel(x, y, p.getPixel(y, x));
            }
        }

        Texture rotated = new Texture(rotatedPix);

        p.dispose();
        return rotated;

    }

    public static Texture reverse(Texture t) {
        if (!t.getTextureData().isPrepared()) {
            t.getTextureData().prepare();
        }
        Pixmap p = t.getTextureData().consumePixmap();
        Pixmap newp = new Pixmap(p.getWidth(), p.getHeight(), p.getFormat());

        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                newp.drawPixel(x, y, p.getPixel(p.getWidth() - 1 - x, p.getHeight() - 1 - y));
            }
        }

        Texture newt = new Texture(newp);

        p.dispose();
        return newt;
    }

    public static void animateText(Stage stage, Skin skin, String style, String text, Color color, float sx, float sy, float dx, float dy) {
        Label label = new Label(text, skin, style);
        label.setPosition(sx, sy);
        label.setColor(color);
        stage.addActor(label);
        label.addAction(sequence(Actions.moveTo(dx, dy, 3f), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    public static Model createSky(ModelBuilder builder) {
        Material skyMaterial = new Material(ColorAttribute.createDiffuse(Color.SKY), IntAttribute.createCullFace(GL20.GL_NONE));
        long attrs = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        return builder.createSphere(50f, 50f, 50f, 32, 32, skyMaterial, attrs);
    }

    public static Model createWall(ModelBuilder builder, Material wallMaterial, Material edgeMaterial) {

        final float width = 1f;
        final float height = 1f;
        final float depth = 0.05f;

        final float hw = width * 0.5f;  // half-width  (X)
        final float hh = height * 0.5f;  // half-height (Y)
        final float hd = depth * 0.5f;  // half-depth  (Z)

        long attrs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        builder.begin();

        MeshPartBuilder wallPart = builder.part("wallFaces", GL20.GL_TRIANGLES, attrs, wallMaterial);

        wallPart.setUVRange(0f, 0f, 1f, 1f);
        // Front face (z = +hd)
        wallPart.rect(
                -hw, -hh, +hd, // bottom-left
                +hw, -hh, +hd, // bottom-right
                +hw, +hh, +hd, // top-right
                -hw, +hh, +hd, // top-left
                0f, 0f, 1f
        );

        wallPart.setUVRange(0f, 0f, 1f, 1f);
        // Back face (z = -hd)
        wallPart.rect(
                -hw, -hh, -hd, // bottom-left
                +hw, -hh, -hd, // bottom-right
                +hw, +hh, -hd, // top-right
                -hw, +hh, -hd, // top-left
                0f, 0f, -1f
        );

        final float cornerSize = 0.040f;
        float postWidth = cornerSize;
        float postHeight = height;
        float postDepth = cornerSize;

        float cxLeft = -hw - postWidth * 0.5f;
        float cxRight = +hw + postWidth * 0.5f;

        float cy = 0f;
        float cz = 0f;

        builder.part("cornerLeft", GL20.GL_TRIANGLES, attrs, edgeMaterial)
                .box(cxLeft, cy, cz, postWidth, postHeight, postDepth);

        builder.part("cornerRight", GL20.GL_TRIANGLES, attrs, edgeMaterial)
                .box(cxRight, cy, cz, postWidth, postHeight, postDepth);

        MeshPartBuilder edgePart = builder.part("wallEdges", GL20.GL_TRIANGLES, attrs, edgeMaterial);

        // Top
        edgePart.setUVRange(0f, 0f, 1f, 1f);
        edgePart.rect(
                -hw, +hh, +hd,
                +hw, +hh, +hd,
                +hw, +hh, -hd,
                -hw, +hh, -hd,
                0f, 1f, 0f
        );

        // Bottom
        edgePart.setUVRange(0f, 0f, 1f, 1f);
        edgePart.rect(
                -hw, -hh, -hd,
                +hw, -hh, -hd,
                +hw, -hh, +hd,
                -hw, -hh, +hd,
                0f, -1f, 0f
        );

        // Left
        edgePart.setUVRange(0f, 0f, 1f, 1f);
        edgePart.rect(
                -hw, -hh, -hd,
                -hw, -hh, +hd,
                -hw, +hh, +hd,
                -hw, +hh, -hd,
                -1f, 0f, 0f
        );

        // Right
        edgePart.setUVRange(0f, 0f, 1f, 1f);
        edgePart.rect(
                +hw, -hh, +hd,
                +hw, -hh, -hd,
                +hw, +hh, -hd,
                +hw, +hh, +hd,
                1f, 0f, 0f
        );

        return builder.end();
    }

    public static Model createThinBox(ModelBuilder builder, Material... material) {
        float size = 1f;
        float thickness = 0.1f;

        float hw = size * 0.5f;
        float hh = thickness * 0.5f;
        float hd = size * 0.5f;

        long attrs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

        builder.begin();

        MeshPartBuilder top = builder.part("top", GL20.GL_TRIANGLES, attrs, material[0]);
        top.rect(
                -hw, +hh, -hd,
                hw, +hh, -hd,
                hw, +hh, hd,
                -hw, +hh, hd,
                0f, 1f, 0f
        );

        MeshPartBuilder bottom = builder.part("bottom", GL20.GL_TRIANGLES, attrs, material[material.length - 2]);
        bottom.rect(
                -hw, -hh, hd,
                hw, -hh, hd,
                hw, -hh, -hd,
                -hw, -hh, -hd,
                0f, -1f, 0f
        );

        MeshPartBuilder sides = builder.part("sides", GL20.GL_TRIANGLES, attrs, material[material.length - 1]);

        // front (z = -hd)
        sides.rect(
                -hw, -hh, -hd,
                hw, -hh, -hd,
                hw, +hh, -hd,
                -hw, +hh, -hd,
                0f, 0f, -1f
        );

        // back (z = +hd)
        sides.rect(
                -hw, -hh, hd,
                hw, -hh, hd,
                hw, +hh, hd,
                -hw, +hh, hd,
                0f, 0f, 1f
        );

        // left (x = -hw)
        sides.rect(
                -hw, -hh, hd,
                -hw, -hh, -hd,
                -hw, +hh, -hd,
                -hw, +hh, hd,
                -1f, 0f, 0f
        );

        // right (x = +hw)
        sides.rect(
                hw, -hh, -hd,
                hw, -hh, hd,
                hw, +hh, hd,
                hw, +hh, -hd,
                1f, 0f, 0f
        );

        return builder.end();
    }

    public static Model createPlaneModel(ModelBuilder builder, int dim, Material material, boolean faceDown) {
        builder.begin();

        long attrs = VertexAttributes.Usage.Position
                | VertexAttributes.Usage.Normal
                | VertexAttributes.Usage.TextureCoordinates;

        MeshPartBuilder mpb = builder.part(
                "plane",
                GL20.GL_TRIANGLES,
                attrs,
                material
        );

        int tiles = dim * 4;
        float halfSize = tiles * 0.5f;
        float y = 0f;

        float x1 = -halfSize;
        float x2 = halfSize;
        float z1 = -halfSize;
        float z2 = halfSize;

        mpb.setUVRange(0f, 0f, tiles, tiles);

        if (!faceDown) {
            mpb.rect(
                    x1, y, z1, // bottom-left
                    x2, y, z1, // bottom-right
                    x2, y, z2, // top-right
                    x1, y, z2, // top-left
                    0f, 1f, 0f // normal up
            );
        } else {
            mpb.rect(
                    x1, y, z1, // bottom-left
                    x2, y, z1, // bottom-right
                    x2, y, z2, // top-right
                    x1, y, z2, // top-left
                    0f, -1f, 0f // normal up
            );
        }

        return builder.end();
    }

    public static Model getDoor(ModelBuilder builder, Model wall) {
        Model door = ObjLoader.loadModel("assets/graphics/door.obj", "door", 0.1f);
        door.nodes.get(0).translation.set(0f, -.5f, 0f);
        door.calculateTransforms();

        builder.begin();
        builder.node("door-wall", wall);
        builder.node("door-main", door);
        return builder.end();
    }

    public static Model getMark(ModelBuilder builder, Model mark) {
        Model pedestal = ObjLoader.loadModel("assets/graphics/pedestal.obj", "pedestal", 0.1f);

        builder.begin();
        builder.node("mark", mark);
        builder.node("pedestal", pedestal);
        return builder.end();
    }

}
