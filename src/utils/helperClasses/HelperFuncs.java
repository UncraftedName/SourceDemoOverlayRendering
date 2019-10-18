package utils.helperClasses;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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
}
