/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import andius.objects.Item;
import andius.objects.Monster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

public class MonsterTest {

    //@Test
    public void monsters() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/monsters.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Monster> monsters = gson.fromJson(json, new TypeToken<java.util.List<Monster>>() {
        }.getType());

        Collections.sort(monsters);
        System.out.println("ID\tNAME          \tTYPE      \tLVL\tEXP\tHPMX\tAC\tDAMG                      \tMAGE\tPRST\tSPED\tGOLD\tREWD\tLVLDR\tHEAL\tBRTH\tPARTID\tGRPSZ");
        for (Monster m : monsters) {
            System.out.println(m);
        }

    }

    @Test
    public void items() throws Exception {

        InputStream is2 = this.getClass().getResourceAsStream("/assets/json/items.json");
        String json = IOUtils.toString(is2);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Item> items = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
        }.getType());

        Collections.sort(items);

        System.out.println("NAME               \tTYPE     \tCOST     \tDAMG\tAC\tSWINGS\tSPELL     \tHITMOD\tREGN\tVENDOR\tUSABLE");
        for (Item m : items) {
            System.out.println(m);
        }

    }

}
