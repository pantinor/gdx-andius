package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SkinUtils {

    public static Skin createCombatSkin(Skin shared) {
        return createAdventureSkin(shared);
    }

    /** An independently owned font palette; drawables are borrowed from the application skin. */
    public static Skin createAdventureSkin(Skin shared) {
        Skin skin = new Skin();
        BitmapFont body = font("AlegreyaSans-Regular.ttf", 15);
        BitmapFont emphasis = font("AlegreyaSans-Medium.ttf", 16);
        BitmapFont heading = font("Cinzel.ttf", 16);
        BitmapFont title = font("Cinzel.ttf", 25);
        skin.add("body", body);
        skin.add("emphasis", emphasis);
        skin.add("heading", heading);
        skin.add("title", title);
        skin.add("default-12", new Label.LabelStyle(body, Color.WHITE));
        skin.add("default-16", new Label.LabelStyle(heading, Color.WHITE));
        skin.add("default-24", new Label.LabelStyle(title, Color.WHITE));
        skin.add("default", new Label.LabelStyle(body, Color.WHITE));
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle(shared.get(TextButton.TextButtonStyle.class));
        button.font = emphasis;
        skin.add("default", button);
        Window.WindowStyle window = new Window.WindowStyle(shared.get(Window.WindowStyle.class));
        window.titleFont = heading;
        skin.add("default", window);
        for (String name : new String[] {"default-12", "default-16"}) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(shared.get(name, TextButton.TextButtonStyle.class));
            style.font = emphasis;
            skin.add(name, style);
        }
        List.ListStyle list = new List.ListStyle(shared.get("default-12-padded-clear", List.ListStyle.class));
        list.font = body;
        skin.add("default-12-padded-clear", list);
        skin.add("default", new ScrollPane.ScrollPaneStyle(shared.get(ScrollPane.ScrollPaneStyle.class)));
        skin.add("dimmer", shared.getDrawable("dimmer"), Drawable.class);
        return skin;
    }

    private static BitmapFont font(String filename, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("assets/fonts/combat/" + filename));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameters.size = size;
            parameters.hinting = FreeTypeFontGenerator.Hinting.AutoSlight;
            parameters.minFilter = TextureFilter.Linear;
            parameters.magFilter = TextureFilter.Linear;
            if (filename.equals("Cinzel.ttf")) {
                parameters.borderWidth = .2f;
                parameters.borderColor = Color.WHITE;
            }
            BitmapFont font = generator.generateFont(parameters);
            font.setUseIntegerPositions(false);
            return font;
        } finally {
            generator.dispose();
        }
    }
}
