﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CU;

namespace CU
{
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
    }   
}
