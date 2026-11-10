package com.campusmarket.service;

import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 图形验证码服务
 *
 * 采用"简单算术题"形式（如 3+5=?），相比纯字母数字更难被简单 OCR 识别，
 * 同时不需要用户辨认模糊字符，体验更好。
 *
 * 安全要点：
 * 1. 验证码答案只存服务端缓存，不随图片返回；
 * 2. 有效期 60 秒；
 * 3. 校验一次即删除，防止重放（同一个验证码不能反复试）。
 */
@Service
public class CaptchaService {

    /** 缓存键前缀 */
    private static final String KEY_PREFIX = CacheKeys.CAPTCHA;
    /** 有效期（秒） */
    private static final long TTL_SECONDS = 60;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final Random random = new Random();

    @Autowired
    private CacheStore cacheStore;

    /**
     * 生成验证码
     *
     * @return captchaKey（前端提交时原样带回）、imageBase64（PNG 图片的 base64）、expireIn（有效期秒数）
     */
    public Map<String, Object> create() {
        int a = random.nextInt(9) + 1;      // 1~9
        int b = random.nextInt(9) + 1;      // 1~9
        boolean plus = random.nextBoolean();
        // 减法保证结果非负，避免出现负数让用户困惑
        int answer = plus ? a + b : Math.max(a, b) - Math.min(a, b);
        String expression = plus
                ? a + "+" + b + "=?"
                : Math.max(a, b) + "-" + Math.min(a, b) + "=?";

        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        cacheStore.set(KEY_PREFIX + captchaKey, String.valueOf(answer), TTL_SECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("imageBase64", render(expression));
        result.put("expireIn", TTL_SECONDS);
        return result;
    }

    /**
     * 校验验证码
     *
     * 无论成功失败都会删除该 key，保证一次性使用。
     */
    public boolean verify(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        String key = KEY_PREFIX + captchaKey;
        String answer = cacheStore.get(key);
        cacheStore.delete(key);
        if (answer == null) {
            return false;
        }
        return answer.equals(captchaCode.trim());
    }

    /** 把算式渲染成图片并转 base64（不落盘） */
    private String render(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // 干扰线，降低机器识别成功率
            for (int i = 0; i < 6; i++) {
                g.setColor(randomColor(150, 210));
                int x1 = random.nextInt(WIDTH);
                int y1 = random.nextInt(HEIGHT);
                int x2 = random.nextInt(WIDTH);
                int y2 = random.nextInt(HEIGHT);
                g.drawLine(x1, y1, x2, y2);
            }

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(randomColor(20, 90));
            g.drawString(text, 12, 29);
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("验证码图片生成失败", e);
        }
    }

    private Color randomColor(int min, int max) {
        int range = max - min;
        return new Color(
                min + random.nextInt(range),
                min + random.nextInt(range),
                min + random.nextInt(range));
    }
}
