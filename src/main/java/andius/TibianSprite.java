package andius;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utils.Utils;

public class TibianSprite {

    private static TextureAtlas bossesAtlas;
    private static TextureAtlas creaturesAtlas;
    private static TextureAtlas mountsAtlas;
    private static TextureAtlas charactersAtlas;
    private static final List<String> boNames = new ArrayList<>();
    private static final List<String> crNames = new ArrayList<>();
    private static final List<String> moNames = new ArrayList<>();
    private static final List<String> chNames = new ArrayList<>();
    private static final List<String> allNames = new ArrayList<>();

    public static void init() {
        bossesAtlas = new TextureAtlas(Gdx.files.classpath("assets/tibian/bosses.atlas"));
        creaturesAtlas = new TextureAtlas(Gdx.files.classpath("assets/tibian/creatures.atlas"));
        mountsAtlas = new TextureAtlas(Gdx.files.classpath("assets/tibian/mounts.atlas"));
        charactersAtlas = new TextureAtlas(Gdx.files.classpath("assets/tibian/characters.atlas"));

        for (AtlasRegion r : bossesAtlas.getRegions()) {
            if (!boNames.contains(r.name)) {
                boNames.add(r.name);
            }
        }

        for (AtlasRegion r : creaturesAtlas.getRegions()) {
            if (!crNames.contains(r.name)) {
                crNames.add(r.name);
            }
        }

        for (AtlasRegion r : mountsAtlas.getRegions()) {
            if (!moNames.contains(r.name)) {
                moNames.add(r.name);
            }
        }

        for (AtlasRegion r : charactersAtlas.getRegions()) {
            if (!chNames.contains(r.name)) {
                chNames.add(r.name);
            }
        }

        boNames.stream().forEach(n -> {
            if (!allNames.contains(n)) {
                allNames.add(n);
            }
        });
        crNames.stream().forEach(n -> {
            if (!allNames.contains(n)) {
                allNames.add(n);
            }
        });
        moNames.stream().forEach(n -> {
            if (!allNames.contains(n)) {
                allNames.add(n);
            }
        });
        chNames.stream().forEach(n -> {
            if (!allNames.contains(n)) {
                allNames.add(n);
            }
        });

//        allNames.stream().forEach(n -> {
//            Animation a = animation(n);
//            System.out.printf("%s [%d]\n", n, a.getKeyFrames().length);
//        });
        //System.out.println(boNames.size());
        //System.out.println(crNames.size());
        //System.out.println(moNames.size());
        //System.out.println(chNames.size());
        //System.out.println(allNames.size());
        //allNames.stream().forEach(item -> System.out.println(item));
    }

    public static List<String> names() {
        return Collections.unmodifiableList(allNames);
    }

    public static TextureRegion icon(String n) {
        TextureRegion cr = creaturesAtlas.findRegion(n);
        if (cr != null) {
            return cr;
        }
        TextureRegion br = bossesAtlas.findRegion(n);
        if (br != null) {
            return br;
        }
        TextureRegion chr = charactersAtlas.findRegion(n);
        if (chr != null) {
            return chr;
        }
        TextureRegion mr = mountsAtlas.findRegion(n);
        if (mr != null) {
            return mr;
        }
        return null;
    }

    public static TibianAnimation animation(String n) {
        if (creaturesAtlas == null) {
            return null;
        }

        Array<AtlasRegion> ca = creaturesAtlas.findRegions(n);
        if (ca != null && ca.size != 0) {
            return new TibianAnimation(.2f, ca, Animation.PlayMode.LOOP);
        }
        Array<AtlasRegion> ba = bossesAtlas.findRegions(n);
        if (ba != null && ba.size != 0) {
            return new TibianAnimation(.2f, ba, Animation.PlayMode.LOOP);
        }
        Array<AtlasRegion> ch = charactersAtlas.findRegions(n);
        if (ch != null && ch.size != 0) {
            return new TibianAnimation(.2f, ch, Animation.PlayMode.LOOP);
        }
        Array<AtlasRegion> ma = mountsAtlas.findRegions(n);
        if (ma != null && ma.size != 0) {
            return new TibianAnimation(.2f, ma, Animation.PlayMode.LOOP);
        }
        return null;
    }

    public static class TibianAnimation extends Animation {
        
        //the center point of the bounding box of the pixels, not the center of the texture
        public Vector2 center;

        public TibianAnimation(float frameDuration, Array<AtlasRegion> keyFrames, PlayMode playMode) {
            super(frameDuration, keyFrames, playMode);
            this.center = Utils.centerOfMass(keyFrames.first());
        }

    }

}
