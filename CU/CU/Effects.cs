/* This file is licensed under
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Thomas Ettinger
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.math;

namespace CU
{
    public enum HighlightType
    {
        Plain, Bright, Dim, Spectrum
    }
    /*
    public class ShaderTools
    {
        public static ShaderProgram AlterChannels(float r, float g, float b)
        {
            string colors = "vec4(" + r + ", " + g + ", " + b + ", 1)";
            string vertexShader =
    @"
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    v_color = " + colors + @";
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}";
            string fragmentShader =
@"
#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
}";
            return new ShaderProgram(vertexShader, fragmentShader);
        }
        //public static ShaderProgram Plain = AlterChannels(1, 1, 1);
        //public static ShaderProgram Bright = AlterChannels(1.35f, 1.35f, 1.35f);
        //public static ShaderProgram[] Spectrum = { AlterChannels(1.4f, 0.8f, 0.8f), AlterChannels(1.4f, 1.4f, 0.7f), AlterChannels(0.8f, 1.4f, 0.8f), AlterChannels(0.85f, 0.85f, 1.4f)};
    }*/
    public delegate void NilFn ();
    public class NilTask : Timer.Task
    {
        NilFn runner;
        public NilTask(NilFn runner)
        {
            this.runner = runner;
        }
        public override void run()
        {
            runner();
        }
    }
    public class Speech
    {
        private bool initializedX = false, initializedY = false;
        private int _x, _y;
        public int x
        {
            get
            {
                return _x;
            }
            set
            {
                _x = value;
                initializedX = true;
                if (initializedX && initializedY)
                {
                    worldX = 64 + x * 64 + y * 64;
                    worldY = 146 + x * 32 - y * 32;
                }
            }
        }
        public int y
        {
            get
            {
                return _y;
            }
            set
            {
                _y = value;
                initializedY = true;
                if (initializedX && initializedY)
                {
                    worldX = 64 + x * 64 + y * 64;
                    worldY = 146 + x * 32 - y * 32;
                }
            }
        }
        public float worldX;
        public float worldY;
        public string text;
        public bool large;
    }
    public class Effects
    {
        private static Vector3 oldpos = Launcher.game.camera.position, newpos, midpos = oldpos;
        public static void CenterCamera(float gridX, float gridY, float stepPortion)
        {
            oldpos = new Vector3(Launcher.game.camera.position);
            newpos = new Vector3(64 * (gridX + gridY), 32 * (gridX - gridY) + 32, 0);
            NilTask n = new NilTask(() => {
                midpos = midpos.add((int)((newpos.x - oldpos.x) / 16F), (int)((newpos.y - oldpos.y) / 16F), 0);
                Launcher.game.camera.position.set(midpos);
                Launcher.game.camera.update();
            });
            Timer.instance().scheduleTask(n, 0, stepPortion * GameGDX.updateStep / 16F, 15);
            Timer.instance().start();
        }
        public static void CenterCamera(Position pos, float stepPortion)
        {
            CenterCamera(pos.x, pos.y, stepPortion);
        }
    }
}
