using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CU;

namespace CU
{
    public struct Tuple<K, V>
    {
        public K Item1;
        public V Item2;
    }
    public static class Tools
    {
        private static Random r = new Random();
        public static T RandomElement<T>(this List<T> list)
        {
            if (list.Count == 0)
                return default(T);

            return list[r.Next(list.Count)];
        }
        public static T RandomElement<T>(this T[,] mat)
        {
            if (mat.Length == 0)
                return default(T);

            return mat[r.Next(mat.GetLength(0)), r.Next(mat.GetLength(1))];
        }
        public static T RandomElement<T>(this IEnumerable<T> list)
        {
            if (list.Count() == 0)
                return default(T);
            int idx = 0, tgt = r.Next(list.Count());
            foreach (T t in list)
            {
                if (tgt == idx)
                {
                    return t;
                }
                idx++;
            }
            return default(T);
        }
        public static Unit RandomFactionUnit(this Unit[,] mat, int color)
        {
            if (mat.Length == 0)
                return new Unit();
            Unit u = new Unit();
            List<Unit> units = new List<Unit>();
            for (int i = 0; i < mat.GetLength(0); i++ )
            {
                for (int j = 0; j < mat.GetLength(1); j++)
                {
                    if (mat[i, j] != null && mat[i, j].color == color && mat[i, j].speed > 0)
                    {
                        units.Add(mat[i, j]);
                    }
                }
            }
            return units.RandomElement();
        }
        public static T[,] Fill<T>(this T[,] mat, T item)
        {
            if (mat.Length == 0)
                return mat;

            for (int i = 0; i < mat.GetLength(0); i++)
            {
                for (int j = 0; j < mat.GetLength(1); j++)
                {
                    mat[i, j] = item;
                }
            }
            return mat;
        }
        public static T[] Fill<T>(this T[] arr, T item)
        {
            if (arr.Length == 0)
                return arr;

            for (int i = 0; i < arr.GetLength(0); i++)
            {
                arr[i] = item;
            }
            return arr;
        }
        public static Dictionary<K, V> Clone<K, V>(this Dictionary<K, V> dict)
            where K : ICloneable
            where V : ICloneable
        {
            if (dict.Count == 0)
                return new Dictionary<K,V>();
            Dictionary<K, V> ret = new Dictionary<K, V>(dict.Count);
            foreach (KeyValuePair<K, V> kv in dict)
            {
                ret.Add((K)(kv.Key.Clone()), (V)(kv.Value.Clone()));
            }
            return ret;
        }
        public static List<T> Clone<T>(this List<T> list)
            where T : ICloneable
        {
            if (list.Count == 0)
                return new List<T>();
            List<T> ret = new List<T>(list.Count);
            foreach (T elem in list)
            {
                ret.Add((T)(elem.Clone()));
            }
            return ret;
        }
    }
    public class JUComparator<T> : java.util.Comparator
    {
        private readonly Func<T, T, int> _lambdaComparer;
        private readonly Func<T, int> _lambdaHash;

        public JUComparator(Func<T, T, int> lambdaComparer) :
            this(lambdaComparer, EqualityComparer<T>.Default.GetHashCode)
        {

        }

        public JUComparator(Func<T, T, int> lambdaComparer,
            Func<T, int> lambdaHash)
        {
            if (lambdaComparer == null)
                throw new ArgumentNullException("lambdaComparer");
            if (lambdaHash == null)
                throw new ArgumentNullException("lambdaHash");

            _lambdaComparer = lambdaComparer;
            _lambdaHash = lambdaHash;
        }

        public int compare(T x, T y)
        {
            return _lambdaComparer(x, y);
        }
        public int compare(object x, object y)
        {
            if (x.GetType() != typeof(T))
                return Int32.MaxValue;
            else if (y.GetType() != typeof(T))
                return Int32.MaxValue;
            else
                return _lambdaComparer((T)x, (T)y);
        }
        public bool equals(object o)
        {
            return base.Equals(o);
        }
        public int GetHashCode(T obj)
        {
            return _lambdaHash(obj);
        }
    }
}
