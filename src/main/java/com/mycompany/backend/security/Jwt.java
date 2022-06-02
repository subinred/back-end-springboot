package com.mycompany.backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Jwt {
  //상수 정의
  private static final String JWT_SECRET_KEY="kosa12345";// 비밀키 생성, "" 값은 암호화 해서 들어가야함
  private static final long ACCESS_TOKEN_DURATION=1000*60*30;//ACCESS_TOKEN의 유효 기간: 30분
  public static final long REFRESH_TOKEN_DURATION=1000*60*60*24;//REFRESH_TOKEN의 유효 기간: 24시간

  //AcessToken 생성
  public static String createAccessToken(String mid, String authority) {//AcessToken에 사용자의 정보와 권한 저장
    log.info("실행");
    String accessToken=null;
    try {
      accessToken = Jwts.builder()
          //헤더 설정
          .setHeaderParam("alg", "HS256")
          .setHeaderParam("typ", "JWT")
          //토큰의 유효기간 설정
          .setExpiration(new Date(new Date().getTime()+ACCESS_TOKEN_DURATION))
          //페이로드 설정
          .claim("mid", mid)
          .claim("authority", authority)
          //서명 설정
          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
          //토큰 생성
          .compact();//builder로 받았지만 String으로 return 되어야 하기 때문에 compact()를 이용함
    } catch(Exception e) {
      log.error(e.getMessage());
    }
    
    return accessToken;
  }
  
//RefreshToken 생성
  public static String createRefreshToken(String mid, String authority) {//AcessToken에 사용자의 정보와 권한 저장
    log.info("실행");
    String RefreshToken=null;
    try {
      RefreshToken = Jwts.builder()
          //헤더 설정
          .setHeaderParam("alg", "HS256")
          .setHeaderParam("typ", "JWT")
          //토큰의 유효기간 설정
          .setExpiration(new Date(new Date().getTime()+REFRESH_TOKEN_DURATION))
          //페이로드 설정
          .claim("mid", mid)
          .claim("authority", authority)
          //서명 설정
          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
          //토큰 생성
          .compact();//builder로 받았지만 String으로 return 되어야 하기 때문에 compact()를 이용함
    } catch(Exception e) {
      log.error(e.getMessage());
    }
    
    return RefreshToken;
  }
  
  //토큰 유효성 판단
  public static boolean validateToken(String token) {
    log.info("실행");
    boolean result=false;
    try {
      result=Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody() //
          .getExpiration() //토큰의 만료시간 받아옴
          .after(new Date());//getExpiration에서 얻는 날짜가 현재 날짜보다 나중인지 판단(기간이 유효한지 판단)
    }catch(Exception e) {
      log.info(e.getMessage());
    }
    
    return result;
  }
  //토큰 만료 시간(날짜) 얻기
  public static Date getExpiration(String token) {
    log.info("실행");
    Date result=null;
    try {
      result=Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody() //
          .getExpiration(); //토큰의 만료시간 받아옴
    }catch(Exception e) {
      log.info(e.getMessage());
    }
    return result;
  }
  
  //인증 사용자 정보 얻기
  public static Map<String, String> getUserInfo(String token){
    log.info("실행");
    Map<String, String> result=new HashMap<>();
    try {
      Claims claims=Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody(); //claims
      result.put("mid", claims.get("mid", String.class));
      result.put("authority", claims.get("authority", String.class));
      
    }catch(Exception e) {
      log.info(e.getMessage());
    }
    return result;
  }
  //요청 Authorization 헤더값에서 AccessToken 얻기
  //Bearer xxxxxxxxxxx.xxxxxxxxxxxx.xxxxxxxxxxxx    <- Bearer를 제외한 accesstoken을 얻어내기 위함
  public static String getAccessToken(String authorization) {
    String accessToken = null;
    if(authorization!=null&& authorization.startsWith("Bearer ")){
      accessToken=authorization.substring(7);
    }
    return accessToken;
  }
  
  public static void main(String[] arg) {
    String accessToken=createAccessToken("user", "ROLE_USER");
    log.info(accessToken);
    System.out.println(validateToken(accessToken));
    
    Date expiration=getExpiration(accessToken);
    System.out.println(expiration);
    
    Map<String,String> userInfo=getUserInfo(accessToken);
    System.out.println(userInfo);
  }
}
