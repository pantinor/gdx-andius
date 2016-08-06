
import andius.objects.Monster;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.io.IOUtils;

public class MonsterMapEditor extends InputAdapter implements ApplicationListener {

    static int screenWidth = 1024;
    static int screenHeight = 768;

    Batch batch;
    BitmapFont font;
    Stage stage;
    Skin skin;

    java.util.List<Monster> monsters;
    java.util.List<Object> mapMonsters;

    Monster selectedMonster;
    Map map;

    @Override
    public void create() {

        font = new BitmapFont();
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));
        stage = new Stage();

        try {

            InputStream is = this.getClass().getResourceAsStream("/assets/data/cave.tmx");
            JAXBContext jaxbContext = JAXBContext.newInstance(Map.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            map = (Map) jaxbUnmarshaller.unmarshal(is);
            for (ObjectGroup og : map.getObjectgroups()) {
                if (og.name.equals("people")) {
                    mapMonsters = og.getObjects();
                }
            }
            InputStream is2 = this.getClass().getResourceAsStream("/assets/json/monsters-json.txt");
            String json = IOUtils.toString(is2);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
            }.getType());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<Label> monsterMapTable = new List(skin);
        Array<Label> tmp = new Array<>();
        for (Object obj : mapMonsters) {
            String creature = null;
            for (Property p : obj.getProperties().get(0).getProperties()) {
                if (p.getName().equals("creature")) {
                    creature = p.getValue();
                }
            }
            Label l = new Label(obj.name + " " + (creature == null ? "" : creature), skin);
            l.setUserObject(obj);
            tmp.add(l);
        }
        monsterMapTable.setItems(tmp);

        List<Label> monsterListTable = new List(skin);
        tmp = new Array<>();
        Collections.sort(monsters);
        for (Monster m : monsters) {
            Label l = new Label(m.toString(), skin);
            l.setUserObject(m);
            tmp.add(l);
        }
        monsterListTable.setItems(tmp);

        monsterListTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    selectedMonster = (Monster) monsterListTable.getSelected().getUserObject();
                }
                return false;
            }
        }
        );

        monsterMapTable.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    Label l = (Label) monsterMapTable.getSelected();
                    Object obj = (Object) monsterMapTable.getSelected().getUserObject();
                    if (selectedMonster != null) {
                        boolean found = false;
                        for (Property p : obj.getProperties().get(0).getProperties()) {
                            if (p.getName().equals("creature")) {
                                p.setValue(selectedMonster.name);
                                found = true;
                                l.setText(obj.getName() + " " + selectedMonster.name);
                            }
                        }
                        if (!found) {
                            Property newP = new Property();
                            newP.setName("creature");
                            newP.setValue(selectedMonster.name);
                            obj.getProperties().get(0).getProperties().add(newP);
                            l.setText(obj.getName() + " " + selectedMonster.name);
                        }
                    }
                }
                return false;
            }
        }
        );

        ScrollPane sp1 = new ScrollPane(monsterMapTable, skin);
        ScrollPane sp2 = new ScrollPane(monsterListTable, skin);

        TextButton makeButton = new TextButton("Make XML", skin, "default");
        makeButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                try {
                    JAXBContext context = JAXBContext.newInstance(Map.class);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    marshaller.marshal(map, System.out);

//                    GsonBuilder builder = new GsonBuilder();
//                    Gson gson = builder.setPrettyPrinting().create();
//                    String json = gson.toJson(monsters);
//                    FileOutputStream fos = new FileOutputStream(new File("monsters.json"));
//                    fos.write(json.getBytes("UTF-8"));
//                    fos.close();
                } catch (Exception e) {

                }
                return false;
            }
        }
        );

        sp1.setBounds(50, 50, 300, 700);
        sp2.setBounds(400, 50, 400, 700);

        stage.addActor(sp1);
        stage.addActor(sp2);
        stage.addActor(makeButton);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "" + selectedMonster, 600, 10);
        batch.end();

        stage.act();
        stage.draw();
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Monster Editor";
        cfg.width = screenWidth;
        cfg.height = screenHeight;
        new LwjglApplication(new MonsterMapEditor(), cfg);

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

}

@XmlRootElement(name = "map")
class Map {

    java.util.List<ObjectGroup> objectgroups;

    @XmlElement(name = "objectgroup")
    public java.util.List<ObjectGroup> getObjectgroups() {
        return objectgroups;
    }

    public void setObjectgroups(java.util.List<ObjectGroup> objectgroups) {
        this.objectgroups = objectgroups;
    }

}

@XmlRootElement(name = "objectgroup")
class ObjectGroup {

    String name;
    int visible;
    java.util.List<Object> objects;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    @XmlElement(name = "object")
    public java.util.List<Object> getObjects() {
        return objects;
    }

    public void setObjects(java.util.List<Object> objects) {
        this.objects = objects;
    }

}

@XmlRootElement(name = "object")
class Object {

    int id;
    String name;
    String type;
    int x;
    int y;
    int width;
    int height;
    java.util.List<Properties> properties;

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @XmlAttribute
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @XmlAttribute
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @XmlAttribute
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @XmlElement(name = "properties")
    public java.util.List<Properties> getProperties() {
        return properties;
    }

    public void setProperties(java.util.List<Properties> properties) {
        this.properties = properties;
    }

}

@XmlRootElement(name = "properties")
class Properties {

    java.util.List<Property> properties;

    @XmlElement(name = "property")
    public java.util.List<Property> getProperties() {
        return properties;
    }

    public void setProperties(java.util.List<Property> properties) {
        this.properties = properties;
    }

}

@XmlRootElement(name = "property")
class Property {

    String name;
    String value;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
