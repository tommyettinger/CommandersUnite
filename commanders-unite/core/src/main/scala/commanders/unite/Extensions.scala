package commanders.unite

import scala.util.Random

/**
 * Created by Tommy Ettinger on 7/24/2014.
 */
object Extensions
{
  val r = new Random()

  implicit class RandElement[T](l: Seq[T])
  {
    def RandomElement(): Option[T] = if (l.isEmpty) None else Some(l(r.nextInt(l.length)))
  }

  /*
    class RandElement[T >: Null <: AnyRef](l: Seq[T]) {
      def RandomElement:T = if(l.isEmpty) null else l(r.nextInt(l.length))
    }
    implicit def richSeq[T](i: Seq[T]) = new RandElement[T](i)
  */
  implicit class RandSpot[T >: Null <: AnyRef](l: Array[Array[T]])
  {
    def RandomSpot(): T = if (l.isEmpty) null else l(r.nextInt(l.length))(r.nextInt(l(0).length))
  }

  //implicit def richMat[T](i: Array[Array[T]]) = new RandSpot[T](i)

  implicit class RandItem[T >: Null <: AnyRef](l: Array[T])
  {
    def RandomItem(): T = if (l.isEmpty) null else l(r.nextInt(l.length))
  }

  //implicit def richArr[T](i: Array[T]) = new RandItem[T](i)

  implicit class RandFactionPiece(p: Array[Array[Piece]])
  {
    def RandomFactionPiece(color: Int): Piece =
    {
      if (p.isEmpty) {
        null
      }
      else {
        p.flatten.toSeq.filter(pc => pc != null && pc.speed > 0 && pc.color == color).RandomElement().getOrElse(new Piece(0, 0, 0, 0, 0)())
      }
    }
  }

  //implicit def richFac(p: Array[Array[Piece]])(implicit color:Int) = new RandFactionPiece(p, color)

  implicit class MinRand(l: Random)
  {
    def nextIntMin(min: Int, max: Int): Int = r.nextInt(max - min) + min
  }

  //implicit def richRand(i: Random)(implicit args : (Int, Int)) = new MinRand(i, args._1, args._2)


}
