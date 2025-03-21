
import andius.WizardryData;
import static andius.WizardryData.DUNGEON_DIM;
import andius.WizardryData.MazeCell;
import andius.WizardryData.Scenario;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import utils.FrameMaker;

public class WizardryMapEditor extends InputAdapter implements ApplicationListener {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "WizardryMapEditor";
        cfg.width = 1224;
        cfg.height = 916;
        new LwjglApplication(new WizardryMapEditor(), cfg);
    }

    private static final int UNIT = 4;
    private static final int FACTOR = 9;

    private enum Face {
        NORTH, SOUTH, EAST, WEST
    }

    private Batch batch;
    private BitmapFont font;
    private Stage stage;
    private Skin skin;
    private Texture background;

    private Texture northWall;
    private Texture southWall;
    private Texture eastWall;
    private Texture westWall;

    private Texture northDoor;
    private Texture southDoor;
    private Texture eastDoor;
    private Texture westDoor;

    private Texture northHiddenDoor;
    private Texture southHiddenDoor;
    private Texture eastHiddenDoor;
    private Texture westHiddenDoor;

    private Texture northNone;
    private Texture southNone;
    private Texture eastNone;
    private Texture westNone;

    private Texture center;

    private int currentNorth;
    private int currentEast;

    private final MazeCellActor[][] cells = new MazeCellActor[DUNGEON_DIM][DUNGEON_DIM];

    private MazeCellActor selectedCell;

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();
        //stage.setDebugAll(true);

        northWall = fillRectangle(UNIT * FACTOR, UNIT, Color.DARK_GRAY, 1);
        southWall = fillRectangle(UNIT * FACTOR, UNIT, Color.DARK_GRAY, 1);
        eastWall = fillRectangle(UNIT, UNIT * FACTOR, Color.DARK_GRAY, 1);
        westWall = fillRectangle(UNIT, UNIT * FACTOR, Color.DARK_GRAY, 1);

        northDoor = fillRectangle(UNIT * FACTOR, UNIT, Color.BROWN, 1);
        southDoor = fillRectangle(UNIT * FACTOR, UNIT, Color.BROWN, 1);
        eastDoor = fillRectangle(UNIT, UNIT * FACTOR, Color.BROWN, 1);
        westDoor = fillRectangle(UNIT, UNIT * FACTOR, Color.BROWN, 1);

        northHiddenDoor = fillRectangle(UNIT * FACTOR, UNIT, Color.PINK, 1);
        southHiddenDoor = fillRectangle(UNIT * FACTOR, UNIT, Color.PINK, 1);
        eastHiddenDoor = fillRectangle(UNIT, UNIT * FACTOR, Color.PINK, 1);
        westHiddenDoor = fillRectangle(UNIT, UNIT * FACTOR, Color.PINK, 1);

        northNone = fillRectangle(UNIT * FACTOR, UNIT, Color.WHITE, 1);
        southNone = fillRectangle(UNIT * FACTOR, UNIT, Color.WHITE, 1);
        eastNone = fillRectangle(UNIT, UNIT * FACTOR, Color.WHITE, 1);
        westNone = fillRectangle(UNIT, UNIT * FACTOR, Color.WHITE, 1);

        center = fillRectangle(UNIT * FACTOR, UNIT * FACTOR, Color.WHITE, 1);

        for (int e = 0; e < DUNGEON_DIM; e++) {
            for (int n = 0; n < DUNGEON_DIM; n++) {
                cells[n][e] = new MazeCellActor(n, e);
                stage.addActor(cells[n][e]);
            }
        }

        WizardryData.Scenario sc = WizardryData.Scenario.PMO;

        for (int e = 0; e < DUNGEON_DIM; e++) {
            for (int n = 0; n < DUNGEON_DIM; n++) {
                WizardryData.MazeCell c = sc.levels()[0].cells[n][e];
                cells[n][e].set(c);
            }
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

        FrameMaker fm = new FrameMaker(1224, 916);
        MazeSettings ms = new MazeSettings();
        fm.setBounds(ms, 950, 400, 200, 400);
        this.stage.addActor(ms);
        this.background = fm.build();

        this.selectedCell = cells[0][0];

    }

    private class MazeCellActor extends WidgetGroup {

        private Side north = new Side(Face.NORTH, northNone);
        private Side south = new Side(Face.SOUTH, southNone);
        private Side east = new Side(Face.EAST, eastNone);
        private Side west = new Side(Face.WEST, westNone);
        private MazeLabel label;
        private Image ctr = new Image(center);

        private final int n;
        private final int e;
        private MazeCell cell;

        public MazeCellActor(int n, int e) {

            this.n = n;
            this.e = e;

            north.setPosition(UNIT, UNIT * (FACTOR + 1));
            south.setPosition(UNIT, 0);
            west.setPosition(0, UNIT);
            east.setPosition(UNIT * (FACTOR + 1), UNIT);
            ctr.setPosition(UNIT, UNIT);

            this.addActor(north);
            this.addActor(south);
            this.addActor(east);
            this.addActor(west);
            this.addActor(ctr);

            setPosition(10 + e * UNIT * (FACTOR + 2), 10 + n * UNIT * (FACTOR + 2));

            ctr.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event.toString().equals("touchDown")) {
                        currentNorth = MazeCellActor.this.n;
                        currentEast = MazeCellActor.this.e;
                        selectedCell = MazeCellActor.this;
                    }
                    return false;
                }
            }
            );

        }

        public void set(MazeCell cell) {

            this.cell = cell;

            this.label = new MazeLabel(cell);
            this.addActor(this.label);
            this.label.setPosition(UNIT + 6, UNIT + 16);

            if (!cell.northWall) {
                this.north.idx = 0;
                north.setDrawable(new TextureRegionDrawable(new TextureRegion(northNone)));
            }
            if (!cell.southWall) {
                this.south.idx = 0;
                south.setDrawable(new TextureRegionDrawable(new TextureRegion(southNone)));
            }
            if (!cell.eastWall) {
                this.east.idx = 0;
                east.setDrawable(new TextureRegionDrawable(new TextureRegion(eastNone)));
            }
            if (!cell.westWall) {
                this.west.idx = 0;
                west.setDrawable(new TextureRegionDrawable(new TextureRegion(westNone)));
            }

            if (cell.northWall) {
                this.north.idx = 1;
                north.setDrawable(new TextureRegionDrawable(new TextureRegion(northWall)));
            }
            if (cell.southWall) {
                this.south.idx = 1;
                south.setDrawable(new TextureRegionDrawable(new TextureRegion(southWall)));
            }
            if (cell.eastWall) {
                this.east.idx = 1;
                east.setDrawable(new TextureRegionDrawable(new TextureRegion(eastWall)));
            }
            if (cell.westWall) {
                this.west.idx = 1;
                west.setDrawable(new TextureRegionDrawable(new TextureRegion(westWall)));
            }

            if (cell.eastDoor) {
                this.east.idx = 2;
                east.setDrawable(new TextureRegionDrawable(new TextureRegion(eastDoor)));
            }
            if (cell.westDoor) {
                this.west.idx = 2;
                west.setDrawable(new TextureRegionDrawable(new TextureRegion(westDoor)));
            }
            if (cell.northDoor) {
                this.north.idx = 2;
                north.setDrawable(new TextureRegionDrawable(new TextureRegion(northDoor)));
            }
            if (cell.southDoor) {
                this.south.idx = 2;
                south.setDrawable(new TextureRegionDrawable(new TextureRegion(southDoor)));
            }

            if (cell.eastWall && cell.eastDoor) {
                this.east.idx = 3;
                east.setDrawable(new TextureRegionDrawable(new TextureRegion(eastHiddenDoor)));
            }
            if (cell.westWall && cell.westDoor) {
                this.west.idx = 3;
                west.setDrawable(new TextureRegionDrawable(new TextureRegion(westHiddenDoor)));
            }
            if (cell.northWall && cell.northDoor) {
                this.north.idx = 3;
                north.setDrawable(new TextureRegionDrawable(new TextureRegion(northHiddenDoor)));
            }
            if (cell.southWall && cell.southDoor) {
                this.south.idx = 3;
                south.setDrawable(new TextureRegionDrawable(new TextureRegion(southHiddenDoor)));
            }
        }

    }

    private class MazeLabel extends Label {

        private final MazeCell cell;
        private Color col = Color.BLUE;
        private final StringBuilder sb = new StringBuilder();

        public MazeLabel(MazeCell cell) {
            super("", skin, "default-12");
            this.cell = cell;
        }

        @Override
        public StringBuilder getText() {
            sb.clear();
            if (cell.darkness) {
                sb.append("DRK");
            }
            if (cell.rock) {
                sb.append("RCK");
            }
            if (cell.chute) {
                sb.append("CHU");
            }
            if (cell.encounterID >= 0) {
                sb.append("MID");
            }
            if (cell.message != null) {
                sb.append("MSG");
            }
            if (cell.itemRequired > 0) {
                sb.append("ITREQ");
            }
            if (cell.itemObtained > 0) {
                sb.append("ITOBT");
            }
            if (cell.pit) {
                sb.append("PIT");
            }
            if (cell.damage != null) {
                sb.append("DMG");
            }
            if (cell.cage) {
                sb.append("CAG");
            }
            if (cell.spinner) {
                sb.append("SPN");
            }
            if (cell.teleport) {
                sb.append("TEP");
            }
            if (cell.stairs) {
                sb.append("STA");
            }
            if (cell.elevator) {
                sb.append("ELV");
            }
            if (cell.summoningCircle != null) {
                sb.append("SUM");
            }
            return sb;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

        @Override
        public Color getColor() {
            return col;
        }

    }

    private class MazeSettings extends WidgetGroup {

        public MazeSettings() {
            List<Actor> temp = new ArrayList<>();
            {
                CheckBox cb = new CheckBox("darkness", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.darkness = !selectedCell.cell.darkness;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                CheckBox cb = new CheckBox("rock", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.rock = !selectedCell.cell.rock;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                CheckBox cb = new CheckBox("chute", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.chute = !selectedCell.cell.chute;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }

            {
                CheckBox cb = new CheckBox("spinner", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.spinner = !selectedCell.cell.spinner;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                CheckBox cb = new CheckBox("teleport", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.teleport = !selectedCell.cell.teleport;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                CheckBox cb = new CheckBox("stairs", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.stairs = !selectedCell.cell.stairs;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                CheckBox cb = new CheckBox("elevator", skin, "default-16");
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        selectedCell.cell.elevator = !selectedCell.cell.elevator;
                    }
                });
                this.addActor(cb);
                temp.add(cb);
            }
            {
                TextField cb = new TextField("", skin, "default-16");
                cb.setTextFieldListener(new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField tf, char key) {
                        if (key == '\r') {
                            try {
                                selectedCell.cell.encounterID = Integer.parseInt(tf.getText().trim());
                            } catch (Throwable t) {
                            }
                        }
                    }
                });
                this.addActor(cb);
                temp.add(cb);
                Label l = new Label("encounterID", skin, "default-16");
                this.addActor(l);
                temp.add(l);
            }
            {
                TextField cb = new TextField("", skin, "default-16");
                cb.setTextFieldListener(new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField tf, char key) {
                        if (key == '\r') {
                            try {
                                selectedCell.cell.message = WizardryData.getMessage(Scenario.PMO.messages(), Integer.parseInt(tf.getText().trim()));
                            } catch (Throwable t) {
                            }
                        }
                    }
                });
                this.addActor(cb);
                temp.add(cb);
                Label l = new Label("message", skin, "default-16");
                this.addActor(l);
                temp.add(l);
            }
            {
                TextField cb = new TextField("", skin, "default-16");
                cb.setTextFieldListener(new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField tf, char key) {
                        if (key == '\r') {
                            try {
                                selectedCell.cell.itemRequired = Integer.parseInt(tf.getText().trim());
                            } catch (Throwable t) {
                            }
                        }
                    }
                });
                this.addActor(cb);
                temp.add(cb);
                Label l = new Label("itemRequired", skin, "default-16");
                this.addActor(l);
                temp.add(l);
            }
            {
                TextField cb = new TextField("", skin, "default-16");
                cb.setTextFieldListener(new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField tf, char key) {
                        if (key == '\r') {
                            try {
                                selectedCell.cell.itemObtained = Integer.parseInt(tf.getText().trim());
                            } catch (Throwable t) {
                            }
                        }
                    }
                });
                this.addActor(cb);
                temp.add(cb);
                Label l = new Label("itemObtained", skin, "default-16");
                this.addActor(l);
                temp.add(l);
            }
            for (int i = 0; i < temp.size(); i++) {
                Actor a = temp.get(i);
                a.setPosition(0, i * 25);
            }

        }

    }

    private class Side extends Image {

        private int idx = 0;
        private Face face;

        public Side(Face face, Texture texture) {
            super(texture);
            this.face = face;

            addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event.toString().equals("touchDown")) {
                        idx++;
                        if (idx > 3) {
                            idx = 0;
                        }
                        if (idx == 0) {
                            Texture t = null;
                            switch (face) {
                                case NORTH:
                                    t = northNone;
                                    break;
                                case SOUTH:
                                    t = southNone;
                                    break;
                                case EAST:
                                    t = eastNone;
                                    break;
                                case WEST:
                                    t = westNone;
                                    break;
                            }
                            Side.this.setDrawable(new TextureRegionDrawable(new TextureRegion(t)));
                        }
                        if (idx == 1) {
                            Texture t = null;
                            switch (face) {
                                case NORTH:
                                    t = northWall;
                                    break;
                                case SOUTH:
                                    t = southWall;
                                    break;
                                case EAST:
                                    t = eastWall;
                                    break;
                                case WEST:
                                    t = westWall;
                                    break;
                            }
                            Side.this.setDrawable(new TextureRegionDrawable(new TextureRegion(t)));
                        }
                        if (idx == 2) {
                            Texture t = null;
                            switch (face) {
                                case NORTH:
                                    t = northDoor;
                                    break;
                                case SOUTH:
                                    t = southDoor;
                                    break;
                                case EAST:
                                    t = eastDoor;
                                    break;
                                case WEST:
                                    t = westDoor;
                                    break;
                            }
                            Side.this.setDrawable(new TextureRegionDrawable(new TextureRegion(t)));
                        }
                        if (idx == 3) {
                            Texture t = null;
                            switch (face) {
                                case NORTH:
                                    t = northHiddenDoor;
                                    break;
                                case SOUTH:
                                    t = southHiddenDoor;
                                    break;
                                case EAST:
                                    t = eastHiddenDoor;
                                    break;
                                case WEST:
                                    t = westHiddenDoor;
                                    break;
                            }
                            Side.this.setDrawable(new TextureRegionDrawable(new TextureRegion(t)));
                        }
                    }
                    return false;
                }
            }
            );
        }

    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        stage.act();
        stage.draw();

        batch.begin();
        font.draw(batch, "" + currentNorth + "," + currentEast, 400, 907);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void resume() {

    }

    private static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

}
