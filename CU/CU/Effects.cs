using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.math;
using com.badlogic.gdx.utils;

namespace CU
{
    public enum HighlightType
    {
        Plain, Bright, Dim, Spectrum
    }
    public delegate void Nilly ();
    public class NilTask : Timer.Task
    {
        Nilly runner;
        public NilTask(Nilly runner)
        {
            this.runner = runner;
        }
        public override void run()
        {
            runner();
        }
    }
    public class Effects
    {

        public static void CenterCamera(float gridX, float gridY)
        {
            GameGDX.camera.position.set(new Vector3(64 * (gridX + gridY), 32 * (gridX - gridY)+32, 0));
            GameGDX.camera.update();
        }
        public static void CenterCamera(Position pos)
        {
            GameGDX.camera.position.set(new Vector3(64 * (pos.x + pos.y), 32 * (pos.x - pos.y) + 32, 0));
            GameGDX.camera.update();
        }
    }
}
