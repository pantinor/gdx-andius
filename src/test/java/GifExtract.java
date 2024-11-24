
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
                                File myFile = new File("src/main/resources/assets/gifs/mounts/" + names[7]);
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
    public void makeAtlases() throws Exception {
        readGifs("creatures");
        readGifs("bosses");
        readGifs("mounts");
        readGifs("characters");
    }

    private static void readGifs(String atlasName) throws Exception {

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

        File directory = new File("src/main/resources/assets/tibian/" + atlasName);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        List<BufferedImage> frames = readGif(new FileInputStream(file));
                        String name = file.getName().replace(".gif", "").replace("%27", "").replace("%28", "").replace("%29", "").replace(" ", "_");
                        //System.out.printf("%s - frames [%d] width [%d] height [%d]\n", name, frames.length, frames[0].width, frames[0].height);
                        packFrames(tp, name, frames);
                        if (frames.get(0).getWidth() < 100) {
                            indexes.put(name, frames.get(0));
                        }
                    } catch (Exception e) {
                        //System.out.println(file.getName());
                        //e.printStackTrace();
                    }
                }
            }
        }

        tp.pack(new File("src/main/resources/assets/tibian"), atlasName + ".atlas");
        tileSet(indexes, "src/main/resources/assets/tibian/tibian-" + atlasName + ".png", 48);
    }

    private static void packFrames(TexturePacker tp, String name, List<BufferedImage> frames) {
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage f = frames.get(i);
            if (f.getWidth() < 1 || f.getHeight() < 1) {
                System.out.println("Skipping empty file " + name);
                continue;
            }
            if (f.getWidth() > 100 || f.getHeight() > 100) {
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

    private static List<BufferedImage> readGif(InputStream is) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(is));

        int numFrames = reader.getNumImages(true);
        for (int i = 0; i < numFrames; i++) {
            BufferedImage frame = reader.read(i);
            frames.add(frame);
        }
        return frames;
    }

}
