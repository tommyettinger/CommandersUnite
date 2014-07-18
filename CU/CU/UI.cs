using com.badlogic.gdx.scenes.scene2d;
using com.badlogic.gdx.scenes.scene2d.ui;
using com.badlogic.gdx.scenes.scene2d.utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CU
{
    class UI
    {
        public static Skin skin;
        public static Stage stage;


        public static ScrollPane makeMenu(List<MenuEntry> entries, int color)
        {
            VerticalGroup vg = new VerticalGroup();
            ScrollPane sp = new ScrollPane(vg);
            foreach (MenuEntry ent in entries)
            {
                TextButton btn = new TextButton(ent.text, skin, "color" + color);
                btn.addListener(new Changer(() => { btn.setDisabled(true); sp.setVisible(false); ent.action(); }));
                vg.addActor(btn);
            }
            TextButton end = new TextButton("End", skin, "color" + color);
            end.addListener(new Changer(() => { sp.setVisible(false); GameGDX.brain.advanceTurn(); sp.remove(); }));
            vg.addActor(end);
            return sp;
        }
        public static void postActor(Actor a)
        {
            a.setX(700);
            a.setY(360);
            stage.addActor(a);
        }
        public static void postActor(Actor a, float x, float y)
        {
            a.setX(x);
            a.setY(y);
            stage.addActor(a);
        }
        public static void draw()
        {
            stage.draw();
        }
    }

    struct MenuEntry
    {
        public string text;
        public NilFn action;
        
        public MenuEntry(string text, NilFn action)
        {
            this.text = text;
            this.action = action;
        }
    }
    class Changer : ChangeListener
    {
        NilFn n;
        public Changer()
        {
            n = () => { };
        }
        public Changer(NilFn fn)
        {
            n = fn;
        }
        public override void changed(ChangeEvent evt, Actor actor)
        {
            n();
            evt.stop();
        }
    }
}
