package andius;

import andius.objects.ClassType;
import andius.objects.Gender;
import andius.objects.Portraits;
import andius.objects.Race;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

public class CharacterPortraitSmokeMain extends Game {
    private int frames;
    private ManageScreen manage;
    private SaveGame save;
    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new CharacterPortraitSmokeMain(), cfg);
    }
    private Object field(String name) throws Exception {
        var field = ManageScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(manage);
    }
    private SaveGame.CharacterRecord character(Object slot) throws Exception {
        var field = slot.getClass().getDeclaredField("character");
        field.setAccessible(true);
        return (SaveGame.CharacterRecord) field.get(slot);
    }
    private void expect(String image, Gender gender, Race race, ClassType type) throws Exception {
        TextureRegion actual = ((TextureRegionDrawable) ((Image) field(image)).getDrawable()).getRegion();
        if (actual != Portraits.get(gender, race, type)) throw new AssertionError(image + " used the wrong portrait");
    }
    @Override public void create() {
        try {
            new Andius().create();
            for (Gender gender : Gender.values()) for (Race race : Race.values()) for (ClassType type : ClassType.values()) {
                TextureRegion region = Portraits.get(gender, race, type);
                if (region == null || region.getRegionWidth() != 152 || region.getRegionHeight() != 152)
                    throw new AssertionError("Invalid atlas portrait " + Portraits.key(gender, race, type));
            }
            save = new SaveGame();
            var player = SaveGame.generatePlayer(10, ClassType.MAGE, "Elaine");
            player.gender = Gender.FEMALE; player.race = Race.ELF;
            player.str = player.intell = player.piety = player.vitality = player.agility = player.luck = 18;
            save.players = new SaveGame.CharacterRecord[] {player};
            manage = new ManageScreen(null, Andius.skin, save);
            setScreen(manage);
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
    @Override @SuppressWarnings("unchecked") public void render() {
        try {
            super.render(); frames++;
            if (frames == 5) {
                expect("partyPortrait", Gender.FEMALE, Race.ELF, ClassType.MAGE);
                manage.changeClassBox.setSelected(ClassType.NINJA);
                manage.changeClassBtn.fire(new ChangeEvent());
            }
            if (frames == 6) {
                expect("partyPortrait", Gender.FEMALE, Race.ELF, ClassType.NINJA);
                ((SelectBox<Gender>) field("partyGender")).setSelected(Gender.MALE);
                ((SelectBox<Gender>) field("genderSelection")).setSelected(Gender.FEMALE);
                manage.cbRace[Race.DWARF.ordinal()].setChecked(true);
                manage.strPlus.fire(new ChangeEvent());
                manage.classTypeSelection.setSelected(ClassType.FIGHTER);
                manage.nameField.setText("Bryn");
                manage.registry.setSelectedIndex(manage.registry.getItems().size - 1);
            }
            if (frames == 7) {
                expect("partyPortrait", Gender.MALE, Race.ELF, ClassType.NINJA);
                expect("newPortrait", Gender.FEMALE, Race.DWARF, ClassType.FIGHTER);
                manage.apply.fire(new ChangeEvent());
                SaveGame.CharacterRecord created = character(manage.registry.getSelected());
                if (created.gender != Gender.FEMALE || created.race != Race.DWARF || created.classType != ClassType.FIGHTER)
                    throw new AssertionError("Creation did not preserve portrait attributes");
                var method = ManageScreen.class.getDeclaredMethod("buildRosterForSave");
                method.setAccessible(true);
                String json = new com.google.gson.Gson().toJson(method.invoke(manage));
                if (json.contains("portaitIndex") || !json.contains("FEMALE")) throw new AssertionError("Roster save schema is wrong");
            }
            if (frames == 8) {
                expect("rosterPortrait", Gender.FEMALE, Race.DWARF, ClassType.FIGHTER);
                ((SelectBox<Gender>) field("rosterGender")).setSelected(Gender.MALE);
            }
            if (frames == 9) {
                expect("rosterPortrait", Gender.MALE, Race.DWARF, ClassType.FIGHTER);
                Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, 1024, 768);
                PixmapIO.writePNG(Gdx.files.local("target/manage-attribute-portraits.png"), image, -1, true);
                image.dispose();
                System.out.println("PASS: all 80 atlas portraits, creation preview, gender changes, class changes, and roster serialization");
                Gdx.app.exit();
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
}
