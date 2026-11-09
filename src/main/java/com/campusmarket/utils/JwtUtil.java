package com.campusmarket.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "abcdefghijklmnopqrstuvwxyz123456abcdefghijklmnopqrstuvwxyz123456";
    private static final long EXPIRATION = 2 * 60 * 60 * 1000L; // 2小时

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

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
