package com.campusmarket.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * 密钥与有效期来自 application.yaml 的 jwt.secret / jwt.expire-hours，
     * 不再硬编码在代码里：换密钥只需改配置（或用环境变量 JWT_SECRET 覆盖），不用重新编译。
     */
    private static String SECRET;
    private static long EXPIRATION;
    private static SecretKey KEY;

    @Value("${jwt.secret}")
    private String secretValue;

    @Value("${jwt.expire-hours:2}")
    private long expireHours;

    @PostConstruct
    public void init() {
        if (secretValue == null || secretValue.length() < 32) {
            throw new IllegalStateException("jwt.secret 长度不足 32 个字符，HS256 无法安全签名");
        }
        SECRET = secretValue;
        EXPIRATION = expireHours * 60 * 60 * 1000L;
        KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRATION);
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long getUserIdByToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public static boolean isTokenInvalid(String token) {
        try {
            getUserIdByToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /** 解析 token 得到 Claims，签名错误或已过期会抛出异常 */
    private static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * token 剩余有效秒数，已过期返回 0
     *
     * 用途：退出登录写入 token 黑名单时，把 TTL 设为剩余有效期。
     * 这样 token 自然过期后黑名单条目自动消失，无需定时清理任务。
     */
    public static long getRemainingSeconds(String token) {
        Claims claims = parse(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return 0;
        }
        long remain = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(remain, 0);
    }
}
