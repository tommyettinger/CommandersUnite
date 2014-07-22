package cu.game;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
public class Tools
{
    private static Random r = new Random();

    public static Unit randomFactionUnit(Unit[][] mat, int color)
    {
        if (mat.length == 0)
            return new Unit();
        Unit u = new Unit();
        Array<Unit> units = new Array<Unit>();
        for (int i = 0; i < mat.length; i++ )
        {
            for (int j = 0; j < mat[0].length; j++)
            {
                if (mat[i][j] != null && mat[i][j].color == color && mat[i][j].speed > 0)
                {
                    units.add(mat[i][j]);
                }
            }
        }
        return units.random();
    }
    public static <T> T[][] fill2D(T[][] mat, T item)
    {
        if(mat.length == 0)
            return mat;
        for (int i = 0; i < mat.length; i++ )
        {
            for (int j = 0; j < mat[0].length; j++)
            {
                mat[i][j] = item;
            }
        }
        return mat;
    }
    public static <T> T[] fill(T[] arr, T item)
    {
        if (arr.length == 0)
            return arr;

        for (int i = 0; i < arr.length; i++)
        {
            arr[i] = item;
        }
        return arr;
    }
    public static <T> Array<T> distinct(Array<T> arr)
    {
        ObjectSet<T> os = new ObjectSet<>(arr.size * 2);
        os.addAll(arr);
        Array<T> art = new Array<T>(arr.size);
        for (T ost : os)
        {
            art.add(ost);
        }
        return art;
    }
    public static IntArray distinct(IntArray arr)
    {
        IntSet os = new IntSet(arr.size * 2);
        for(int j = 0; j < arr.size; j++)
                os.add(arr.get(j));

        IntArray art = new IntArray(arr.size);
        for (IntSet.IntSetIterator it = os.iterator(); it.hasNext;)
        {
            art.add(it.next());
        }
        return art;
    }
    public static <T> boolean any(T[] arr, Predicate<T> pred)
    {
        for(int i = 0; i < arr.length; i++) {
            if (pred.test(arr[i])) return true;
        }
        return  false;
    }
}