using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.backends.lwjgl;
using com.badlogic.gdx.utils;
using com.badlogic.gdx.math;
namespace CU
{
    class Launcher
    {
        public static void Main(string[] args)
        {
            //GdxNativesLoader.disableNativesLoading = true;
            LwjglApplication app = new LwjglApplication(new GameGDX(), "Commanders Unite", 800, 800);
            //Console.In.ReadLine();
        }
    }
    class GameGDX : ApplicationListener
    {
        OrthographicCamera camera;
        SpriteBatch batch;
        TextureAtlas atlas;
        int width, height;
        Logic brain;
        public void create()
        {
            brain = new Logic(20,20);
            width = brain.FieldMap.Width;
            height = brain.FieldMap.Height;
            brain.PlaceUnits();
            atlas = new TextureAtlas(Gdx.files.@internal("Assets/pack.json"));

            camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 800);
            camera.update();
            batch = new SpriteBatch();
            //atl.findRegion("color0/Estate_face0", 0);
        }
        public void render()
        {
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);
            camera.position.set(new Vector3(1280, 0, 0));
            camera.update();
            
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            //            for (int h = 0; h < height; h++)

            for (int h = 0; h < height; h++)
            {
                //for (int w = 0; w < width; w++)
            
                for (int w = width - 1; w > 0; w--) 
                {
                    batch.draw(atlas.findRegion("Terrain/" + LocalMap.Terrains[brain.FieldMap.Land[w, h]]), w * 64 + h * 64, w * 32 - h * 32);
                    if (brain.UnitGrid[w, h] != null)
                        batch.draw(atlas.findRegion("color" + brain.UnitGrid[w, h].color + "/" + brain.UnitGrid[w, h].name + "_face" + brain.UnitGrid[w, h].facingNumber)
                                        ,20 +  w * 64 + h * 64, 14 +  w * 32 - h * 32);
                }
            }
/*            for (int h = 0; h < height; h++)
            {
                for (int w = 0; w < height - h && w < width; w++)
                {
                    batch.draw(atlas.findRegion("Terrain/" + LocalMap.Terrains[brain.FieldMap.Land[w, h]]), w * 64 + h * 64, 0 - (h * 32 - w * 32) );
                    if (brain.UnitGrid[w, h] != null)
                        batch.draw(atlas.findRegion("color" + brain.UnitGrid[w, h].color + "/" + brain.UnitGrid[w, h].name + "_face" + brain.UnitGrid[w, h].facingNumber)
                                        , w * 64 + h * 64, w * 32 - h * 32);
                }
            }*/
            batch.end();
        }
        public void resume()
        {

        }
        public void pause()
        {

        }
        public void dispose()
        {
            batch.dispose();
            atlas.dispose();
        }
        public void resize(int wide, int high)
        {
            camera.setToOrtho(false, wide, high);
            camera.update();
        }
    }
}
