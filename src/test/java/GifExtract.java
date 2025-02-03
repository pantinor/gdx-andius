
import andius.WizardryData;
import andius.objects.DoGooder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import org.testng.annotations.Test;

public class GifExtract {

    //@Test
    public void extractGifsFromHtml() throws Exception {

        List<String> lines = IOUtils.readLines(new FileInputStream("C:\\Users\\panti\\OneDrive\\Desktop\\gifs.html"));

        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] xcs, String string) throws java.security.cert.CertificateException {
                return true;
            }

        };

        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, trustStrategy).build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();

        for (String line : lines) {
            if (line.contains("data-image-name=\"")) {
                String[] tokens = line.split("\"");
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].startsWith("https")) {

                        String url = tokens[i].replace("amp;", "");
                        System.out.printf("%s\n", url);

                        String[] names = url.split("/");

                        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                File myFile = new File("src/main/resources/assets/gifs/creatures/" + names[7]);
                                try (FileOutputStream outstream = new FileOutputStream(myFile)) {
                                    entity.writeTo(outstream);
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    //@Test
    public void extractGifsFromHtmlNPCs() throws Exception {

        List<String> lines = IOUtils.readLines(new FileInputStream("C:\\Users\\panti\\OneDrive\\Desktop\\gifs.html"));

        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] xcs, String string) throws java.security.cert.CertificateException {
                return true;
            }

        };

        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, trustStrategy).build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();

        for (String line : lines) {
            if (line.contains("Addon_3.gif")) {
                String[] tokens = line.split("\"");

                String name = tokens[7].replace("Outfit", "Sorcerer").replace(" Addon 3", "");
                String url = "https://www.tibiawiki.com.br" + tokens[9];

                try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        File myFile = new File("src/main/resources/assets/tibian/characters/" + name);
                        try (FileOutputStream outstream = new FileOutputStream(myFile)) {
                            entity.writeTo(outstream);
                        }
                    }
                }

            }
        }
    }

    //@Test
    public void extractIBMWiz4Gifs() throws Exception {

        WizardryData.Scenario sc = WizardryData.Scenario.WER;

        java.util.Map<String, String> map = new HashMap<>();
        List<String> urls = new ArrayList<>();
        for (int i = 1; i < 14; i++) {
            List<String> lines = IOUtils.readLines(new FileInputStream("C:\\Users\\panti\\Downloads\\gif" + i + ".txt"));
            for (int x = 0; x < lines.size(); x++) {
                String line1 = lines.get(x);
                if (line1.contains("4app")) {
                    String[] tokens = line1.split("\"");
                    String gif = tokens[3].replace(".gif", "").replace("4app", "").replace("images", "").replace("/", "");
                    String nameLine = lines.get(x - 1);
                    String[] nameTokens = nameLine.split("[<>]");
                    String name = nameTokens[2];
                    map.put(name, gif);
                }
            }
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        for (DoGooder dg : sc.characters()) {
            dg.iconID = map.get(dg.name);
            String json = gson.toJson(dg);
            System.out.println(json + ",");
        }

    }

    //@Test
    public void makeAtlases() throws Exception {
        //readGifs("creatures", "arachnids", "bears", "creatures", "fighters", "outlaws", "sorcerers");
        readPngs("bards-tale");
    }

    //@Test
    public void wiz4Images() throws Exception {

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.minWidth = 8;
        settings.minHeight = 8;
        settings.maxWidth = 256;
        settings.maxHeight = 4000;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;
        settings.edgePadding = false;
        settings.bleed = false;
        settings.debug = false;
        settings.alias = false;
        settings.useIndexes = true;

        TexturePacker tp = new TexturePacker(settings);

        File directory = new File("C:\\Users\\panti\\Downloads\\gifs");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        List<BufferedImage> frames = readGif(new FileInputStream(file));
                        String name = file.getName().replace(".gif", "").replace("%27", "").replace("%28", "").replace("%29", "").replace(" ", "_");
                        System.out.printf("%s - frames [%d]\n", name, frames.size());
                        tp.addImage(frames.get(0), name);
                    } catch (Exception e) {
                        System.out.println(file.getName());
                    }
                }
            }
        }

        tp.pack(new File("src/main/resources/assets/tibian"), "wiz4ibm.atlas");
    }

    private static void readGifs(String atlasName, String... dirs) throws Exception {

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.minWidth = 8;
        settings.minHeight = 8;
        settings.maxWidth = 2000;
        settings.maxHeight = 4000;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;
        settings.edgePadding = false;
        settings.bleed = false;
        settings.debug = false;
        settings.alias = false;
        settings.useIndexes = true;

        TexturePacker tp = new TexturePacker(settings);

        java.util.Map<String, BufferedImage> indexes = new HashMap<>();

        for (String d : dirs) {

            File directory = new File("src/main/resources/assets/gifs/" + d);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            List<BufferedImage> frames = readGif(new FileInputStream(file));
                            String name = file.getName().replace(".gif", "").replace("%27", "").replace("%28", "").replace("%29", "").replace(" ", "_");
                            System.out.printf("[%s] %s - frames [%d]\n", d, name, frames.size());
                            packFrames(tp, name, frames);
                            if (frames.get(0).getWidth() < 100) {
                                indexes.put(name, frames.get(0));
                            }
                        } catch (Exception e) {
                            System.out.println(file.getName());
                        }
                    }
                }
            }

        }

        tp.pack(new File("src/main/resources/assets/tibian"), atlasName + ".atlas");
        tileSet(indexes, "src/main/resources/assets/tibian/tibian-" + atlasName + ".png", 48);
    }

    private static void readPngs(String atlasName) throws Exception {

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.minWidth = 8;
        settings.minHeight = 8;
        settings.maxWidth = 2000;
        settings.maxHeight = 4000;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;
        settings.edgePadding = false;
        settings.bleed = false;
        settings.debug = false;
        settings.alias = false;
        settings.useIndexes = true;

        TexturePacker tp = new TexturePacker(settings);

        java.util.Map<String, BufferedImage> indexes = new HashMap<>();

        File directory = new File("src/main/resources/assets/bt");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        List<BufferedImage> frames = readPNG(new FileInputStream(file));
                        String name = file.getName().replace(".png", "").replace("%27", "").replace("%28", "").replace("%29", "").replace(" ", "_");
                        System.out.printf("[%s] %s - frames [%d]\n", "bt", name, frames.size());
                        packFrames(tp, name, frames);
                        indexes.put(name, frames.get(0));
                    } catch (Exception e) {
                        System.out.println(file.getName());
                    }
                }
            }
        }

        tp.pack(new File("src/main/resources/assets/bt"), atlasName + ".atlas");
        tileSet(indexes, "src/main/resources/assets/bt/bt-" + atlasName + ".png", 146, 176);
    }

    private static void packFrames(TexturePacker tp, String name, List<BufferedImage> frames) {
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage f = frames.get(i);
            if (f.getWidth() < 1 || f.getHeight() < 1) {
                System.out.println("Skipping empty file " + name);
                continue;
            }
            if (f.getWidth() > 200 || f.getHeight() > 200) {
                System.out.println("Skipping large file " + name);
                continue;
            }
            tp.addImage(f, name + "_" + i); //the atlas index is derived
        }
    }

    private static void tileSet(java.util.Map<String, BufferedImage> map, String outfile, int dim) throws IOException {

        int height = (map.size() * 48) / 20 + 48;

        BufferedImage output = new BufferedImage(960, height, BufferedImage.TYPE_INT_ARGB);

        int x = 0;
        int y = 0;
        int count = 0;

        for (String k : map.keySet()) {
            BufferedImage tile = map.get(k);
            output.getGraphics().drawImage(tile, x, y, dim, dim, null);

            x += dim;
            count++;
            if (count >= 20) {
                count = 0;
                x = 0;
                y += dim;
            }

            System.out.printf("%s,\n", k);
        }

        ImageIO.write(output, "PNG", new File(outfile));

        System.out.println("----");

    }

    private static void tileSet(java.util.Map<String, BufferedImage> map, String outfile, int w, int h) throws IOException {

        int dim = 10;
        int height = (map.size() * h) / dim + h;

        BufferedImage output = new BufferedImage(w * dim, height, BufferedImage.TYPE_INT_ARGB);

        int x = 0;
        int y = 0;
        int count = 0;

        for (String k : map.keySet()) {
            BufferedImage tile = map.get(k);
            output.getGraphics().drawImage(tile, x, y, w, h, null);

            x += w;
            count++;
            if (count >= dim) {
                count = 0;
                x = 0;
                y += h;
            }

            System.out.printf("%s,\n", k);
        }

        ImageIO.write(output, "PNG", new File(outfile));

        System.out.println("----");

    }

    private static List<BufferedImage> readGif(InputStream is) throws IOException {
        return readImage(is, "gif");
    }

    private static List<BufferedImage> readPNG(InputStream is) throws IOException {
        return readImage(is, "png");
    }

    private static List<BufferedImage> readImage(InputStream is, String format) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
        reader.setInput(ImageIO.createImageInputStream(is));

        int numFrames = reader.getNumImages(true);
        for (int i = 0; i < numFrames; i++) {
            BufferedImage frame = reader.read(i);
            frames.add(frame);
        }
        return frames;
    }

}
