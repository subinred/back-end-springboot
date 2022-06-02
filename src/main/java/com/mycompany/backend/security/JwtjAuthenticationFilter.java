package com.mycompany.backend.security;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.log4j.Log4j2;
@Log4j2
public class JwtjAuthenticationFilter extends OncePerRequestFilter {//요청 하나 당 딱 한번만 실행하는 필터, security에서 사용할 인증 필터
  private RedisTemplate redisTemplate;
  public void setRedisTemplate(RedisTemplate redisTemplate) {//RedisTemplate setter 생성
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
      log.info("실행");
      //인증 처리, access token을 받아서 유효한지 확인하고 유효한 경우에만 로그인 인증, 그 이후에 다음 필터를 수행하도록 함
      
      //요청 헤더로부터 Authorization 헤더 값 얻기
      String authorization = request.getHeader("Authorization");
      //Access Token 추출
      String accessToken=Jwt.getAccessToken(authorization);

      //검증 작업
      if(accessToken!=null&&Jwt.validateToken(accessToken)) {
        
        //Redis에 존재 여부 확인, accesstoken이 redis key에 존재하지 않으면 유효하지 않다고 인식
        ValueOperations<String,String> vo=redisTemplate.opsForValue();
        String redisRefreshToken=vo.get(accessToken);//아래 if에서 검증
        if(redisRefreshToken!=null) {
        //인증 처리
          Map<String, String> userInfo=Jwt.getUserInfo(accessToken);//유저 정보 얻어오기
          String mid=userInfo.get("mid");
          String authority=userInfo.get("authority");
          UsernamePasswordAuthenticationToken authentication=//인증 객체
              new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority));//id, password, role에 대한 정보
          SecurityContext securityContext=SecurityContextHolder.getContext();//security 인증 정보를 관리해주는 환경 객체
          securityContext.setAuthentication(authentication);//인증 처리 완료
        }     
      }
      
      filterChain.doFilter(request, response);//다음 필터 동작을 수행하는 명령    
  }
}
