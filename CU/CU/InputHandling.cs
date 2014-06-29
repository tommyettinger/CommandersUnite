using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx.input;
using com.badlogic.gdx;
namespace CU
{
    public class MyInputProcessor : InputProcessor {

   public bool keyDown (int keycode) {
      return false;
   }
        public bool keyUp (int keycode) {

      return false;
   }

   public bool keyTyped (char character) {
      return false;
   }

   public bool touchDown (int x, int y, int pointer, int button) {
      return false;
   }

   public bool touchUp (int x, int y, int pointer, int button) {
      return false;
   }

   public bool touchDragged (int x, int y, int pointer) {
      return false;
   }

   public bool touchMoved (int x, int y) {
      return false;
   }

   public bool scrolled(int amount)
   {
      return false;
   }


   public bool mouseMoved(int i1, int i2)
   {
       return false;
   }
    }
}
