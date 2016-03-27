/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import andius.Andius;
import andius.Constants;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

/**
 *
 * @author Paul
 */
public enum LabelStyles {

    WHITE(new LabelStyle(Andius.largeFont, Color.WHITE)),
    YELLOW(new LabelStyle(Andius.largeFont, Color.YELLOW)),
    GREEN(new LabelStyle(Andius.largeFont, Color.GREEN)),
    ORANGE(new LabelStyle(Andius.largeFont, Color.ORANGE)),
    PINK(new LabelStyle(Andius.largeFont, Color.PINK)),
    LIGHT_GRAY(new LabelStyle(Andius.largeFont, Color.LIGHT_GRAY)),
    SCARLET(new LabelStyle(Andius.largeFont, Color.SCARLET)),
    RED(new LabelStyle(Andius.largeFont, Color.RED)),;

    private final LabelStyle style;

    private LabelStyles(LabelStyle style) {
        this.style = style;
    }

    public LabelStyle getStyle() {
        return style;
    }

    public static LabelStyles get(CharacterRecord c) {
        if (c.status == Constants.Status.POISONED) {
            return GREEN;
        }
        if (c.status == Constants.Status.AFRAID) {
            return ORANGE;
        }
        if (c.status == Constants.Status.ASLEEP) {
            return PINK;
        }
        if (c.status == Constants.Status.ASHES) {
            return LIGHT_GRAY;
        }
        if (c.status == Constants.Status.PARALYZED) {
            return YELLOW;
        }
        if (c.status == Constants.Status.STONED) {
            return LIGHT_GRAY;
        }
        if (c.status == Constants.Status.DEAD) {
            return RED;
        }
        if (c.hp > 0 && c.hp < 2) {
            return SCARLET;
        }
        return WHITE;

    }

}
