package utils.helperClasses;

import processing.core.PApplet;
import processing.core.PImage;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class HelperFuncs {

    public static <T> Stream<T> filterForType(Stream stream, Class<T> tClass) {
        return stream.filter(o -> tClass.isAssignableFrom(o.getClass())).map(tClass::cast);
    }


    public static Stream filterWithoutType(Stream stream, Class tClass) {
        return stream.filter(o -> !tClass.isAssignableFrom(o.getClass()));
    }


    public static <T> Stream<T> reverseStream(Stream<T> stream) {
        List<T> list = stream.collect(Collectors.toList());
        Collections.reverse(list);
        return list.stream();
    }


    public static BufferedImage deepImageCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }


    public static double roundToPlaces(double n, int places) {
        return Math.round(n * Math.pow(10, places)) / Math.pow(10, places);
    }


    public static double lerp(double d1, double d2, double x) {
        return d1 * (1 - x) + d2 * x;
    }


    public static double[] lerp(double[] arr1, double[] arr2, double x) {
        double[] out = new double[arr1.length];
        for (int i = 0; i < out.length; i++)
            out[i] = lerp(arr1[i], arr2[i], x);
        return out;
    }


    public static double[][] lerp(double[][] arr1, double[][] arr2, double x) {
        double[][] out = new double[arr1.length][];
        for (int i = 0; i < out.length; i++)
            out[i] = lerp(arr1[i], arr2[i], x);
        return out;
    }
}
