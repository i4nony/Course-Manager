package com.wumii.wechat.service;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.wumii.wechat.entity.ImageMessage;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

@Service
public class ImageHelper {
    private static final Logger logger = LoggerFactory.getLogger(ImageMessage.class);
    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;

    private BufferedImage thumb(BufferedImage source, int width, int height) {
        // targetW，targetH分别表示目标长和宽
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) width / source.getWidth();
        double sy = (double) height / source.getHeight();

        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(width,
                    height);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(width, height, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    private byte[] getPixels(BufferedImage thumbImage) {
        byte[] pixels = new byte[WIDTH * HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                pixels[i * HEIGHT + j] = rgbToGray(thumbImage.getRGB(i, j));
            }
        }
        return pixels;
    }

    //灰度值计算
    private byte rgbToGray(int pixels) {
        // int _alpha =(pixels >> 24) & 0xFF;
        int red = (pixels >> 16) & 0xFF;
        int green = (pixels >> 8) & 0xFF;
        int blue = (pixels) & 0xFF;
        return (byte) (0.3 * red + 0.59 * green + 0.11 * blue);
    }

    private byte[] compareGray(byte[] pixels, byte avgPixel) {
        byte[] comps = new byte[WIDTH * HEIGHT];
        for (int i = 0; i < comps.length; i++) {
            if (pixels[i] >= avgPixel) {
                comps[i] = 1;
            } else {
                comps[i] = 0;
            }
        }
        return comps;
    }

    private StringBuffer calHashCode(byte[] comps) {
        StringBuffer hashCode = new StringBuffer();
        for (int i = 0; i < comps.length; i += 4) {
            int result = comps[i] * (int) Math.pow(2, 3) + comps[i + 1] * (int) Math.pow(2, 2) +
                    comps[i + 2] * (int) Math.pow(2, 1) + comps[i + 2];
            byte[] bytes = Ints.toByteArray(result);
            hashCode.append(BaseEncoding.base16().encode(bytes));//二进制转为16进制
        }
        return hashCode;
    }

    //生成图片指纹
    private StringBuffer produceFingerPrint(String imageUrl) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(Request.Get(imageUrl).execute().returnContent().asBytes());
        BufferedImage source = ImageIO.read(in);                  // 读取图片
        BufferedImage thumb = thumb(source, WIDTH, HEIGHT);      // 第一步，缩小尺寸
        byte[] pixels = getPixels(thumb);                         // 第二步，简化色彩
        byte avgPixel = (byte) IntStream.range(0, pixels.length).// 第三步，计算平均值
                map(i -> pixels[i]).average().getAsDouble();
        byte[] comps = compareGray(pixels, avgPixel);             // 第四步，比较像素的灰度
        return calHashCode(comps);                                // 第五步，计算哈希值
    }

    //如果不相同的数据位不超过5，就说明两张图片很相似；如果大于10，就说明这是两张不同的图片。
    public int compareHashCode(String sourceUrl, String targetUrl) throws IOException {
        StringBuffer sourceHashCode = produceFingerPrint(sourceUrl);
        StringBuffer targetHashCode = produceFingerPrint(targetUrl);
        int difference = 0;
        for (int i = 0; i < sourceHashCode.length(); i++) {
            if (sourceHashCode.charAt(i) != targetHashCode.charAt(i)) {
                difference++;
            }
        }
        return difference;
    }
}
