/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class LogScrollPane extends AutoFocusScrollPane {

    private final Table internalTable;
    private final int width;
    private final Skin skin;
    private final LabelStyle ls;

    public LogScrollPane(Skin skin, Table table, int width) {

        super(table, skin);
        this.skin = skin;
        this.internalTable = table;
        this.width = width;

        this.ls = new LabelStyle(this.skin.get("font14", BitmapFont.class), Color.WHITE);

        setScrollingDisabled(true, false);

        internalTable.bottom().left();
    }

    public void add(String text) {
        add(text, Color.WHITE);
    }

    public void add(String text, Color color) {

        if (text == null) {
            return;
        }

        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setAlignment(Align.topLeft, Align.left);
        label.setColor(color);

        internalTable.add(label).pad(1).width(width - 3);
        internalTable.row();

        pack();

        scrollTo(0, 0, 0, 0);
    }

    @Override
    public void clear() {
        internalTable.clear();
        pack();
    }

    @Override
    public float getPrefWidth() {
        return this.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeight();
    }

    @Override
    public float getMaxWidth() {
        return this.getWidth();
    }

    @Override
    public float getMaxHeight() {
        return this.getHeight();
    }
}
