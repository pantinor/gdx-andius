/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul
 */
public class CombatHud {

    private final Map<CharacterRecord, PlayerListing> map = new HashMap<>();
    private PlayerListing current;

    public CombatHud(List<andius.objects.Actor> players) {
        for (andius.objects.Actor a : players) {
            map.put(a.getPlayer(), new PlayerListing(a.getPlayer()));
        }
    }

    public void set(CharacterRecord rec, Stage stage) {
        if (current != null) {
            current.remove();
        }
        PlayerListing pl = map.get(rec);
        current = pl;
        current.set();
        stage.addActor(current);
    }

    private class PlayerListing extends Group {

        final Image icon;
        final ImageButton[] slotButtons = new ImageButton[10];
        final Label l1;
        final Label l2;
        final Label l3;
        final Label l4;
        final CharacterRecord rec;

        PlayerListing(CharacterRecord rec) {

            this.rec = rec;
            this.icon = new Image(Andius.faceTiles[rec.portaitIndex]);
            this.l1 = new Label("", Andius.skin, "hudSmallFont");
            this.l2 = new Label("", Andius.skin, "hudSmallFont");
            this.l3 = new Label("", Andius.skin, "hudSmallFont");
            this.l4 = new Label("", Andius.skin, "hudSmallFont");

            for (int i = 0; i < 10; i++) {
                if (rec.spellPresets[i] != null) {
                    TextureRegionDrawable t1 = new TextureRegionDrawable(Andius.invIcons[rec.spellPresets[i].getIcon()]);
                    slotButtons[i] = new ImageButton(t1, t1.tint(Color.LIGHT_GRAY));
                    addActor(slotButtons[i]);
                }
            }
            addActor(this.icon);
            addActor(this.l1);
            addActor(this.l2);
            addActor(this.l3);
            addActor(this.l4);

            float x = getX();
            this.icon.setPosition(x, 124);

            this.l1.setPosition(x + 60, 124 + 48);
            this.l2.setPosition(x + 60, 124 + 36);
            this.l3.setPosition(x + 60, 124 + 24);
            this.l4.setPosition(x + 60, 124 + 12);

            x = getX() + 28;
            for (int i = 0; i < 5; i++) {
                if (this.slotButtons[i] != null) {
                    this.slotButtons[i].setPosition(x, 44 + 3);
                }
                x = x + 44 + 3;
            }
            x = getX() + 28;
            for (int i = 0; i < 5; i++) {
                if (this.slotButtons[i + 5] != null) {
                    this.slotButtons[i + 5].setPosition(x, 0);
                }
                x = x + 44 + 3;
            }

            this.setBounds(734, 534, 265, 177);
        }

        public void set() {

            String d1 = String.format("%s  LVL %d  %s  %s", rec.name.toUpperCase(), rec.level, rec.race.toString(), rec.classType.toString());
            String d2 = String.format("HP: %d /%d AC: %d ST: %s", rec.hp, rec.maxhp, rec.calculateAC(), rec.status);
            String d3 = String.format("GOLD: %d EXP: %d", rec.gold, rec.exp);
            int[] ms = rec.magePoints;
            int[] cs = rec.clericPoints;
            String d4 = String.format("M: %d %d %d %d %d %d %d P: %d %d %d %d %d %d %d",
                    ms[0], ms[1], ms[2], ms[3], ms[4], ms[5], ms[6], cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6]);

            this.l1.setText(d1);
            this.l2.setText(d2);
            this.l3.setText(d3);
            this.l4.setText(d4);
        }

    }

}
