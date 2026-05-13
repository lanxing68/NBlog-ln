package top.naccl.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 图形验证码生成工具
 */
public class CaptchaUtils {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成验证码图片和文字
     * @return [0]=图片Base64, [1]=验证码文字
     */
    public static String[] generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                    RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }

        // 验证码文字
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            String ch = String.valueOf(CHAR_SET.charAt(RANDOM.nextInt(CHAR_SET.length())));
            code.append(ch);
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(100)));
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(ch, 20 + i * 24, 30);
        }

        g.dispose();

        // 转 Base64
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            String base64 = "data:image/png;base64," +
                    java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            return new String[]{base64, code.toString()};
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }
}
