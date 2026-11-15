package utils;

import andius.Andius;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import static utils.SkinUtils.GOLD;
import static utils.SkinUtils.INK;
import static utils.SkinUtils.MUTED;
import static utils.SkinUtils.createSkin;

public final class ManagementUi implements Disposable {
   
    public final Skin skin = createSkin(Andius.skin);
    private final Texture pixel = Utils.fillRectangle(1, 1, Color.WHITE, 1);

    public ManagementUi() {
        skin.add("emphasis", new Label.LabelStyle(skin.getFont("emphasis"), Color.WHITE));
        
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        button.up = surface("223443");
        button.over = surface("344c5d");
        button.down = surface("66583f");
        button.disabled = surface("101d28");
        button.disabledFontColor = MUTED;
        skin.add("default", button);
        
        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        BaseDrawable track = (BaseDrawable) surface("0c1822");
        track.setMinWidth(6);
        
        BaseDrawable knob = (BaseDrawable) surface("7f755f");
        knob.setMinWidth(6);
        knob.setMinHeight(24);
        scroll.vScroll = track;
        scroll.vScrollKnob = knob;
        skin.add("default", scroll);
        
        List.ListStyle list = new List.ListStyle(skin.get("default-12-padded-clear", List.ListStyle.class));
        BaseDrawable selection = (BaseDrawable) surface("344b59");
        selection.setTopHeight(7);
        selection.setBottomHeight(7);
        selection.setLeftWidth(8);
        selection.setRightWidth(8);
        list.selection = selection;
        list.fontColorSelected = GOLD;
        skin.add("default", list);
        
        SelectBox.SelectBoxStyle select = new SelectBox.SelectBoxStyle(Andius.skin.get("default-16-dark-background", SelectBox.SelectBoxStyle.class));
        select.font = skin.getFont("emphasis");
        select.listStyle = new List.ListStyle(list);
        select.listStyle.background = surface("15232f");
        select.scrollStyle = new ScrollPane.ScrollPaneStyle(scroll);
        select.scrollStyle.background = surface("15232f");
        select.background = surface("223443");
        skin.add("default", select);
        
        Window.WindowStyle window = new Window.WindowStyle(skin.get(Window.WindowStyle.class));
        window.background = surface("15232f");
        window.stageBackground = surface("00000099");
        window.titleFontColor = GOLD;
        skin.add("default", window);
    }

    public Drawable surface(String color) {
        return new TextureRegionDrawable(new TextureRegion(pixel)).tint(Color.valueOf(color));
    }

    public Image selection(float width, float height) {
        Image selection = new Image(surface("344b59"));
        selection.setColor(1, 1, 1, .65f);
        selection.setSize(width, height);
        selection.setTouchable(Touchable.disabled);
        return selection;
    }

    public Label label(Stage stage, String value, String style, Color color, float x, float y, float width, float height) {
        Label label = new Label(value, skin, style);
        label.setColor(color);
        label.setEllipsis(true);
        label.setBounds(x, y, width, height);
        label.setTouchable(Touchable.disabled);
        stage.addActor(label);
        return label;
    }

    public Label description(Stage stage, float x, float y, float width, float height) {
        Label label = label(stage, "", "default", MUTED, x, y, width, height);
        label.setWrap(true);
        label.setEllipsis(false);
        label.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
        AutoFocusScrollPane pane = new AutoFocusScrollPane(label, skin);
        pane.setScrollingDisabled(true, false);
        pane.setBounds(x, y, width, height);
        stage.addActor(pane);
        return label;
    }

    public void panel(Stage stage, String title, String subtitle, float x, float width) {
        panel(stage, title, subtitle, x, 96, width, 600, "15232f");
    }

    public void panel(Stage stage, String title, String subtitle, float x, float y, float width, float height, String color) {
        Image background = new Image(surface(color));
        background.setBounds(x, y, width, height);
        background.setTouchable(Touchable.disabled);
        stage.addActor(background);
        label(stage, title, "default-16", GOLD, x + 12, y + height - 36, width - 24, 24);
        label(stage, subtitle, "default", MUTED, x + 12, y + height - 58, width - 24, 20);
        Image rule = new Image(surface("66583f"));
        rule.setBounds(x + 12, y + height - 64, width - 24, 1);
        rule.setTouchable(Touchable.disabled);
        stage.addActor(rule);
    }

    public void primary(TextButton button) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        style.up = surface("b59b67");
        style.over = surface("dbc28c");
        style.down = surface("947c50");
        style.fontColor = INK;
        button.setStyle(style);
    }

    public Table equipment() {
        Table table = new Table();
        table.setBounds(334, 554, 316, 64);
        return table;
    }

    @Override public void dispose() {
        skin.dispose();
        pixel.dispose();
    }
}
