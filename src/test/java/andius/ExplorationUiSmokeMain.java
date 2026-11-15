package andius;

import andius.objects.ClassType;
import andius.objects.SaveGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.ScreenUtils;

public class ExplorationUiSmokeMain extends Game {
    private int frames;
    private SaveGame save;
    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new ExplorationUiSmokeMain(), cfg);
    }
    @Override public void create() {
        try {
            new Andius().create();
            Andius.CTX = new Context();
            save = new SaveGame();
            save.wx = 16; save.wy = 30;
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                save.players[i] = SaveGame.generatePlayer(30, ClassType.BISHOP, "Alexandria " + (i + 1));

                save.players[i].gold = 999999;
                save.players[i].exp = 9999999;
                java.util.Arrays.fill(save.players[i].magePoints, 9);
                java.util.Arrays.fill(save.players[i].clericPoints, 9);
            }
            Andius.CTX.setSaveGame(save);
            activate(Constants.Map.BRITAIN);
            save.x = 5;
            save.y = 4;
            screen().load(save);
            Andius.HUD.log("The party arrives in town. A longer journal message wraps within the panel without crossing into the map.");
        } catch (Throwable t) { t.printStackTrace(); System.exit(1); }
    }
    private BaseScreen screen() { return (BaseScreen) getScreen(); }
    private void activate(Constants.Map map) {
        var next = map.getScreen();
        next.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setScreen(next);
    }
    private void snapshot(String name) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local("target/" + name + ".png"), image, -1, true);
        image.dispose();
    }
    private void verify2d() throws Exception {
        BaseScreen view = screen();
        if (view.camera.viewportWidth != 576 || view.camera.viewportHeight != 608)
            throw new AssertionError("Original visible 2D map dimensions changed");
        Vector3 position = new Vector3();
        view.getCurrentMapCoords(position);
        float tileX = view.newMapPixelCoords.x, tileY = view.newMapPixelCoords.y;
        if (Math.abs(view.camera.position.x - view.camera.viewportWidth / 2 - (tileX - 256)) > .01f
                || Math.abs(view.camera.position.y - view.camera.viewportHeight / 2 - (tileY - 256)) > .01f)
            throw new AssertionError("Legacy map projection / avatar offset changed");
        var rendererField = view.getClass().getDeclaredField("renderer");
        rendererField.setAccessible(true);
        var renderer = (com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer) rendererField.get(view);
        if (renderer.getViewBounds().width != 544 || renderer.getViewBounds().height != 544)
            throw new AssertionError("Legacy 17 x 17 tile render bounds changed");
        if (!view.keyUp(Keys.ESCAPE)) throw new AssertionError("Esc must open help");
        Gdx.input.getInputProcessor().keyUp(Keys.RIGHT);
        Vector3 after = new Vector3(); view.getCurrentMapCoords(after);
        if (!after.equals(position)) throw new AssertionError("Moving underneath help dialog");
        for (var actor : view.stage.getActors()) {
            if (actor instanceof Dialog dialog) { dialog.hide(null); break; }
        }
        view.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        view.getCurrentMapCoords(after);
        if (!after.equals(position)) throw new AssertionError("Resizing changed map coordinates");
    }
    @Override public void render() {
        try {
            super.render(); frames++;
            if (frames == 10) {
                Vector3 corner = screen().viewport.project(new Vector3(utils.Hud.TILE_MAP_X + 1,
                        utils.Hud.TILE_MAP_Y + utils.Hud.TILE_MAP_HEIGHT - 1, 0));
                Pixmap pixel = ScreenUtils.getFrameBufferPixmap(Math.round(corner.x), Math.round(corner.y), 1, 1);
                try {
                    if (pixel.getPixel(0, 0) != 0x000000ff)
                        throw new AssertionError("Unrendered area at the map edge must be black");
                } finally { pixel.dispose(); }
                verify2d(); snapshot("exploration-town"); activate(Constants.Map.WORLD);
            }
            if (frames == 20) {
                verify2d(); snapshot("exploration-overworld");
                Gdx.graphics.setWindowedMode(1280, 800);
            }
            if (frames == 30) {
                verify2d(); snapshot("exploration-overworld-resized");
                Gdx.graphics.setWindowedMode(1024, 768);
                activate(Constants.Map.WIZARDRY1);
            }
            if (frames == 40) {
                snapshot("exploration-dungeon"); screen().keyUp(Keys.P);
            }
            if (frames == 50) {
                snapshot("exploration-dungeon-map"); screen().keyUp(Keys.P);
                activate(Constants.Map.SLAVERS_PIT);
            }
            if (frames == 60) {
                snapshot("exploration-tmx-dungeon"); screen().keyUp(Keys.P);
            }
            if (frames == 70) {
                snapshot("exploration-tmx-dungeon-map");
                save.players = new SaveGame.CharacterRecord[] {SaveGame.generatePlayer(1, ClassType.MAGE, "Werdna")};
                for (int i = 0; i < 25; i++) save.players[0].summonedMonsters.add(new andius.objects.MutableMonster(WizardryData.WER_MONSTERS.get(i)));
                activate(Constants.Map.WIZARDRY4);
            }
            if (frames == 80) {
                snapshot("exploration-werdna");
                var field = utils.Hud.class.getDeclaredField("summonedScroll"); field.setAccessible(true);
                var scroll = (utils.AutoFocusScrollPane) field.get(Andius.HUD);
                if (scroll.getMaxY() <= 0) throw new AssertionError("Allies must scroll");
                scroll.setScrollPercentY(1); scroll.updateVisualScroll();
            }
            if (frames == 90) {
                snapshot("exploration-allies-scrolled");
                System.out.println("PASS: all four exploration views, preserved 2D dimensions / tile bounds / projection, Esc help, modal input, resize, both minimaps, and 25 scrollable allies");
                Gdx.app.exit();
            }
        } catch (Throwable t) { t.printStackTrace(); System.exit(1); }
    }
}
