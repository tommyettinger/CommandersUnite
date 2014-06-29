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
        TextureRegion[] terrains;
        TextureRegion[][][][] units;
        Animation[][][] animations;
        float stateTime;
        TextureRegion currentFrame;
        public void create()
        {
            brain = new Logic(15,15);
            width = brain.FieldMap.Width;
            height = brain.FieldMap.Height;
            brain.PlaceUnits();
            atlas = new TextureAtlas(Gdx.files.@internal("Assets/pack.json"));
            terrains = new TextureRegion[10];
            for (int i = 0; i < 10; i++ )
            {
                terrains[i] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i]);
            }
            units = new TextureRegion[4][][][];
            animations = new Animation[4][][];
            for (int i = 0; i < 4; i++ )
            {
                units[i] = new TextureRegion[Unit.CurrentUnits.Length][][];
                animations[i] = new Animation[Unit.CurrentUnits.Length][];

                foreach (string name in Unit.CurrentUnits)
                {
                    units[i][Unit.UnitLookup[name]] = new TextureRegion[4][];
                    animations[i][Unit.UnitLookup[name]] = new Animation[4];

                    for (int j = 0; j < 4; j++)
                    {
                        units[i][Unit.UnitLookup[name]][j] = new TextureRegion[((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4)];
                        for (int k = 0; k < ((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4); k++)
                        {
                            units[i][Unit.UnitLookup[name]][j][k] = atlas.findRegion("color" + brain.Colors[i] + "/" + name + "_face" + j, k);
                        }
                        animations[i][Unit.UnitLookup[name]][j] = new Animation(((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 0.25F : 0.1F),
                            units[i][Unit.UnitLookup[name]][j]);
                    }
                }
            }
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 800);
            
            batch = new SpriteBatch();
            stateTime = 0;
            //atl.findRegion("color0/Estate_face0", 0);
        }
        public void render()
        {
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);
            camera.position.set(new Vector3(1024 - 64, 32, 0));
            camera.update();
            stateTime += Gdx.graphics.getDeltaTime();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            //            for (int h = 0; h < height; h++)

            for (int h = 0; h < height; h++)
            {
                //for (int w = 0; w < width; w++)
            
                for (int w = width - 1; w >= 0; w--) 
                {
                    batch.draw(terrains[brain.FieldMap.Land[w, h]], w * 64 + h * 64, w * 32 - h * 32);
                    if (brain.UnitGrid[w, h] != null)
                    {
                        currentFrame = animations[brain.ReverseColors[brain.UnitGrid[w, h].color]][brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber].getKeyFrame(stateTime, true);
                        batch.draw(currentFrame,20 +  w * 64 + h * 64, 14 +  w * 32 - h * 32);
                    }
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
