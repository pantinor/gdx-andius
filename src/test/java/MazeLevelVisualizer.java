
import andius.Andius;
import andius.TmxWizardryLevel;
import andius.WizardryData;
import andius.WizardryData.CellType;
import static andius.WizardryData.CellType.ROCK;
import andius.WizardryData.MazeCell;
import andius.WizardryData.MazeLevel;
import andius.WizardryData.MazeLevelV1;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.CharArray;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.EndianUtils;

public class MazeLevelVisualizer extends InputAdapter implements ApplicationListener {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "WizardryMapEditor";

        Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

        cfg.width = displayMode.width - 20;
        cfg.height = displayMode.height - 100;

        cfg.x = 0;
        cfg.y = 0;

        new LwjglApplication(new MazeLevelVisualizer(), cfg);
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
    private final GlyphLayout glyphLayout = new GlyphLayout();

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

    private MazeCellActor[][] cells;
    private MazeCellActor selectedCell;
    private Special[] specials;
    private byte[][] cellInfoLocations;

    @Override
    public void create() {

        Andius a = new Andius();
        a.create();

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

        WizardryData.Scenario sc = WizardryData.Scenario.PMO;
        int level = 0;
        MazeLevel lvl = sc.levels()[level];

        if (lvl instanceof TmxWizardryLevel) {
            TmxWizardryLevel ml = (TmxWizardryLevel) lvl;
            int dim = ml.dimension;
            cells = new MazeCellActor[dim][dim];
            for (int e = 0; e < dim; e++) {
                for (int n = 0; n < dim; n++) {
                    cells[n][e] = new MazeCellActor(n, e);
                    stage.addActor(cells[n][e]);
                }
            }
            for (int e = 0; e < dim; e++) {
                for (int n = 0; n < dim; n++) {
                    WizardryData.MazeCell c = ml.cells[n][e];
                    cells[n][e].set(c);
                }
            }
        } else {
            MazeLevelV1 ml = (MazeLevelV1) sc.levels()[level];
            int dim = ml.dimension;

            cellInfoLocations = ml.cellInfoLocations;
            specials = getSpecials(ml.buffer, 0x2F8, sc);
            cells = new MazeCellActor[dim][dim];

            for (int e = 0; e < dim; e++) {
                for (int n = 0; n < dim; n++) {
                    cells[n][e] = new MazeCellActor(n, e);
                    stage.addActor(cells[n][e]);
                }
            }

            for (int e = 0; e < dim; e++) {
                for (int n = 0; n < dim; n++) {
                    WizardryData.MazeCell c = ml.cells[n][e];
                    cells[n][e].set(c);

                    int index = ml.cellInfoLocations[e][n];
                    specials[index].addLocation(new Location(level, n, e));
                }
            }
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

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

            if (cell.hiddenEastDoor) {
                this.east.idx = 3;
                east.setDrawable(new TextureRegionDrawable(new TextureRegion(eastHiddenDoor)));
            }
            if (cell.hiddenWestDoor) {
                this.west.idx = 3;
                west.setDrawable(new TextureRegionDrawable(new TextureRegion(westHiddenDoor)));
            }
            if (cell.hiddenNorthDoor) {
                this.north.idx = 3;
                north.setDrawable(new TextureRegionDrawable(new TextureRegion(northHiddenDoor)));
            }
            if (cell.hiddenSouthDoor) {
                this.south.idx = 3;
                south.setDrawable(new TextureRegionDrawable(new TextureRegion(southHiddenDoor)));
            }
        }

    }

    private class MazeLabel extends Label {

        private final MazeCell cell;
        private Color col = Color.BLUE;
        private final CharArray sb = new CharArray();

        public MazeLabel(MazeCell cell) {
            super("", skin, "default-12");
            this.cell = cell;
        }

        @Override
        public CharArray getText() {
            sb.clear();
            if (cell.darkness) {
                sb.append("DRK");
                this.col = Color.PURPLE;
            }
            if (cell.rock) {
                sb.append("RCK");
                this.col = Color.BROWN;
            }
            if (cell.chute) {
                sb.append("CHU");
                this.col = Color.BROWN;

            }
            if (cell.lair) {
                sb.append("LAIR");
            }
            if (cell.encounterID >= 0) {
                sb.append("MON");
                this.col = Color.RED;
            }
            if (cell.message != null) {
                sb.append("MSG");
                this.col = Color.GREEN;
            }
            if (cell.itemRequired > 0) {
                sb.append("ITREQ");
                this.col = Color.GOLD;
            }
            if (cell.itemObtained > 0) {
                sb.append("ITOBT");
                this.col = Color.GOLD;
            }
            if (cell.fountainType > 0) {
                sb.append("FNT");
                this.col = Color.BLUE;
            }
            if (cell.markType > 0) {
                sb.append("MARK");
                this.col = Color.CYAN;

            }
            if (cell.chestType > 0) {
                sb.append("CHST");
                this.col = Color.GOLDENROD;

            }
            if (cell.pit) {
                sb.append("PIT");
                this.col = Color.FOREST;
            }
            if (cell.damage != null) {
                sb.append("DMG");
                this.col = Color.CORAL;
            }
            if (cell.cage) {
                sb.append("CAG");
                this.col = Color.BROWN;
            }
            if (cell.spinner) {
                sb.append("SPIN");
                this.col = Color.PINK;
            }
            if (cell.teleport) {
                sb.append("TLP");
                this.col = Color.NAVY;
            }
            if (cell.stairs) {
                this.col = Color.VIOLET;
                if (cell.address.level > cell.addressTo.level) {//up
                    sb.append("UP");
                } else {//down           
                    sb.append("DWN");
                }
            }
            if (cell.elevator) {
                sb.append("ELV");
                this.col = Color.SKY;
            }
            if (cell.summoningCircle != null) {
                sb.append("SUM");
                this.col = Color.SALMON;
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

        stage.act();
        stage.draw();

        float pad = 10f;
        float rightX = Gdx.graphics.getWidth() - pad;
        float y = Gdx.graphics.getHeight() - pad;

        batch.begin();

        String status = String.format("(%d,%d)  %s", currentNorth, currentEast, selectedSpecialString());
        y = drawRightAligned(status, rightX, y);

        if (specials != null) {
            for (Special s : specials) {
                if (y < pad) {
                    break;
                }
                y = drawRightAligned(s.toString(), rightX, y);
            }
        }
        batch.end();
    }

    private String selectedSpecialString() {
        if (selectedCell == null || selectedCell.cell == null) {
            return "Special: (none)";
        }

        if (specials == null || cellInfoLocations == null) {
            return "Special: (none)";
        }

        int idx = cellInfoLocations[selectedCell.e][selectedCell.n];

        if (idx < 0 || idx >= specials.length || specials[idx] == null) {
            return String.format("Special[%d]: (missing)", idx);
        }

        if (specials[idx].square == CellType.NORMAL) {
            return "Special: (none)";
        }

        return String.format("Special[%d]: %s", idx, specials[idx].toString());
    }

    private float drawRightAligned(String text, float rightX, float yTop) {
        glyphLayout.setText(font, text);
        font.draw(batch, glyphLayout, rightX - glyphLayout.width, yTop);
        return yTop - font.getLineHeight();
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

    private Special[] getSpecials(byte[] buffer, int ptr, WizardryData.Scenario sc) {
        Special[] sp = new Special[16];

        int pos = 0;
        for (int i = 0; i < 8; i++) {
            sp[pos] = new Special(pos++, buffer, ptr, sc);
            sp[pos] = new Special(pos++, buffer, ptr, sc);
        }

        return sp;
    }

    private class Special {

        private final WizardryData.Scenario sc;
        private final CellType square;
        private final int[] aux = new int[3];
        private final List<Location> locations = new ArrayList<>();

        public Special(int index, byte[] buffer, int offset, WizardryData.Scenario sc) {

            byte b = buffer[offset + (index) / 2];
            int val = index % 2 == 0 ? b & 0x0F : (b & 0xF0) >>> 4;

            square = CellType.values()[val];

            aux[0] = EndianUtils.readSwappedShort(buffer, offset + 8 + index * 2);
            aux[1] = EndianUtils.readSwappedShort(buffer, offset + 40 + index * 2);
            aux[2] = EndianUtils.readSwappedShort(buffer, offset + 72 + index * 2);

            this.sc = sc;
        }

        private void addLocation(Location location) {
            locations.add(location);
        }

        private String description() {
            java.lang.StringBuilder description = new java.lang.StringBuilder();

            switch (square) {
                case ENCOUNTER:
                    description.append("Encounter : " + this.sc.monster(aux[2]).name);
                    break;
                case MESSAGE:
                    switch (aux[2]) {
                        case 1:
                            break;
                        case 2:
                            description.append("Obtain : " + this.sc.item(aux[0]).name);
                            break;
                        case 3:
                            if (aux[0] > 0) {
                                description.append("Wade : " + aux[0]);
                            }
                            break;
                        case 4:
                            if (aux[0] >= 0) {
                                description.append("Encounter : " + this.sc.monster(aux[0]).name);
                            } else if (aux[0] > -1200) {
                                description.append("Obtain : " + this.sc.item(aux[0] * -1 - 1000).name);
                            } else {
                                description.append("Trade : " + this.sc.item(aux[0]).name + " for " + this.sc.item(aux[1]).name);
                            }
                            break;
                        case 5:
                            description.append("Access requires : " + this.sc.item(aux[0]).name);
                            break;
                        case 6:
                            description.append("Check alignment");
                            break;
                        case 7:
                            break;
                        case 8:
                            description.append("Return to castle");
                            break;
                        case 9:
                            description.append(String.format("Look out : surrounded by fights"));
                            break;
                        case 10:
                            description.append("Answer : " + aux[0]);
                            break;
                        case 11:
                            description.append("Pay : " + aux[0]);
                            break;
                        case 12:
                            break;
                        case 13:
                            break;
                        case 14:
                            int east = aux[0] / 100;
                            int north = aux[0] % 100;
                            String item = "" + aux[1];
                            description.append(String.format("Required : %s else teleport N%d E%d", item, north, east));
                            break;
                        case 15:
                            break;
                        case 16:
                            break;
                        case 200:
                        case 201:
                            description.append("Summoning circle");
                            break;
                    }
                    break;
                case STAIRS:
                    Location location = new Location(aux);
                    description.append(String.format("Stairs to : %s", location));
                    break;
                case PIT:
                    break;
                case CHUTE:
                    location = new Location(aux);
                    description.append(String.format("Chute to : %s", location));
                    break;
                case SPINNER:
                    break;
                case DARK:
                    break;
                case TELEPORT:
                    location = new Location(aux);
                    description.append(String.format("Teleport to : %s", location));
                    break;
                case DAMAGE:
                    break;
                case ELEVATOR:
                    description.append(String.format("Elevator levels : %d to %d", aux[2], aux[1]));
                    break;
                case ROCK:
                    break;
                case MARK:
                    break;
                case FOUNTAIN:
                    break;
                case CHEST:
                    break;
                case NOSPELL:
                    break;
                case NORMAL:
                    break;
            }
            return description.toString();
        }

        @Override
        public String toString() {
            String extra = "(" + locations.size() + ")";
            return String.format("%-8s  %5d  %4d  %4d  %s %s", square, aux[0], aux[1], aux[2], extra, description());
        }
    }

    private class Location {

        private int level;
        private int row;
        private int column;

        public Location(int level, int row, int column) {
            this.level = level;
            this.row = row;
            this.column = column;
        }

        public Location(int[] aux) {
            this.level = aux[0];
            this.row = aux[1];
            this.column = aux[2];
        }

        @Override
        public String toString() {
            return String.format("[%d,%d,%d]", level, row, column);
        }

    }

}
