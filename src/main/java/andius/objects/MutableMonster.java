/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import static andius.Constants.DEATHMSGS;
import andius.Constants.Status;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import utils.Utils;

/**
 *
 * @author Paul
 */
public class MutableMonster extends Monster {

    private static TextureRegion HLTH_BAR = null;

    static {
        try {
            HLTH_BAR = new TextureRegion(new Texture(Gdx.files.classpath("assets/skin/imgBtn.png")), 381, 82, 82, 6);
        } catch (Throwable t) {
        }
    }

    private int acmodifier;
    private int currentHitPoints;
    private final State status = new State();
    private final int maxHitPoints;
    private TextureRegion healthBar;

    public List<Spells> knownSpells = new ArrayList<>();
    public int[] magePoints = new int[7];
    public int[] clericPoints = new int[7];

    public MutableMonster(Monster m) {
        clone(m);
        this.maxHitPoints = this.hitPoints.roll();
        this.currentHitPoints = this.maxHitPoints;
    }

    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
    }

    public State status() {
        return status;
    }

    public void decrementStatusEffectCount() {
        for (Status s : Status.values()) {
            this.status.decrement(s);
        }
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public TextureRegion getHealthBar() {
        if (healthBar == null) {
            healthBar = new TextureRegion(HLTH_BAR);
        }
        return this.healthBar;
    }

    public void adjustHealthBar() {
        double percent = (double) currentHitPoints / maxHitPoints;
        double bar = percent * (double) 82;
        if (currentHitPoints < 0) {
            bar = 0;
        }
        if (bar > 82) {
            bar = 82;
        }
        getHealthBar().setRegion(381, 82, (int) bar, 6);
    }

    public String getDamageTag() {
        double percent = (double) currentHitPoints / maxHitPoints;
        if (percent > 0.99) {
            if (this.type > 4) {
                return ", unharmed, growls ominously";
            } else {
                return "chortles merrily as the armor takes the full blow";
            }
        } else if (percent > 0.75) {
            return "still has lots of fight left";
        } else if (percent > 0.50) {
            if (this.type > 4) {
                return "tough hide softens the blow";
            } else {
                return "armor takes some of the impact";
            }
        } else if (percent < 0.00) {
            return DEATHMSGS[Utils.RANDOM.nextInt(DEATHMSGS.length)];
        } else {
            return "is feeling rather weak";
        }
    }

    public void setACModifier(int acmodifier) {
        this.acmodifier = acmodifier;
    }

    public int getACModifier() {
        return acmodifier;
    }

    public boolean canCast(Spells spell) {
        if (!knownSpells.contains(spell)) {
            return false;
        }
        if (spell.getType() == ClassType.MAGE) {
            return magePoints[spell.getLevel() - 1] > 0;
        } else {
            return clericPoints[spell.getLevel() - 1] > 0;
        }
    }

    public void decrMagicPts(Spells spell) {
        if (spell.getType() == ClassType.MAGE) {
            magePoints[spell.getLevel() - 1]--;
        } else {
            clericPoints[spell.getLevel() - 1]--;
        }
    }

}
