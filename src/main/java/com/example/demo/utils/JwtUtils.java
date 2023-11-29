package com.example.demo.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.netty.handler.ssl.OpenSslCertificateCompressionConfig.AlgorithmConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Data
@ConfigurationProperties("ihrm.jwt.config")
public class JwtUtils {
	/**
	 * 私钥
	 */
	private static String key = Constants.SECRET_KEY;

	/**
	 * token失效时间
	 */
	private static Long ttl = Constants.DEFAULT_EXPIRED_SECONDS;

	/**
	 * 生成token
	 *
	 * @param id   用户id
	 * @param name 用户名称
	 * @param map  参数
	 * @return token
	 */
	public static String createJwt(String id, String name, Map<String, Object> map) {
		// 设置失效时间
		long l = System.currentTimeMillis();
		long exp = l + ttl;
		
		Key secretKey = Keys.hmacShaKeyFor(key.getBytes());

		// 创建 JwtBuilder
		JwtBuilder jwtBuilder = Jwts.builder().id(id).subject(name).issuedAt(new Date()).signWith(secretKey);
		// 设置claims
		map.forEach(jwtBuilder::claim);
		jwtBuilder.claim("userId", id);

		jwtBuilder.expiration(new Date(exp));
		// 生成token
		return jwtBuilder.compact();
	}
	
	public static String verifyToken(String token) {
//		Key secretKey = Keys.hmacShaKeyFor(key.getBytes());
		
//        JwtVerifier verifier = Jwts.require().build();
//        try {
//            // 校验token是否合法
//            verifier.verify(token);
//            return "OK";
//        } catch (JwtException e) {
//            e.printStackTrace();
//            return "NG";
//        }
		
		return "OK";
	}
}
