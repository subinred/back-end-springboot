package com.mycompany.backend.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mycompany.backend.security.JwtjAuthenticationFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
@EnableWebSecurity//web security 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter{
  @Resource
  private RedisTemplate redisTemplate;
  
  @Override
  protected void configure(HttpSecurity http) throws Exception{
    log.info("실행");
    //--------------
    //서버 세션 비활성화
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);//=>jsession id 생성되지 않음
    //폼 로그인 비활성화(mpa에서는 disable 하면안됨)
    http.formLogin().disable();
    //사이트 간 요청 위조 방지 비활성화
    http.csrf().disable();
    //요청 경로 권한 설정
    http.authorizeRequests()
      .antMatchers("/board/**").authenticated()//인증이 된 사람만 접근 가능한 경로, **=모든을 의미하는 ant표현 방법
      .antMatchers("/**").permitAll();//이외의 경로들은 로그인 없어도 접근할 수 있도록 허용해줌
    //CORS 설정을 사용 활성화(다른 도메인의 javaScript로 접근 가능하도록 허용함, rest에선 필수적으로 설정해야함),mpa에서는 활용하지 않음
    http.cors(); //cors설정에 대한 상세 설정은 corsConfigurationSource에서 함(하단에 메소드 존재, 웬만하면 무조건 설정해줄 것)
    //--------------
    //jwt 인증 필터 추가, UsernamePasswordAuthenticationFilter는 db에 있는 친구들과 비교를 해서 맞으면 인정
    //UsernamePasswordAuthenticationFilter 위치에서 인증이 일어나야 되기 때문에 before로 filter 실행함
    //UsernamePasswordAuthenticationFilter는 사실상 사용하지 않고, 위치만 받아온는거임, 홈 로그인이 활성화 되었을 때 동작하게 됨
    http.addFilterBefore(jwtAuthentiactionFilter(), UsernamePasswordAuthenticationFilter.class);//addFilter()는 필터의 가장 마지막에 실행, 인증과 관련된 실행은 지정된 위치에 추가되어야 하기 때문에 addFilterBefore를 form 인증을 하기 전에 추가
  }
  
  @Bean
  public JwtjAuthenticationFilter jwtAuthentiactionFilter() {
    JwtjAuthenticationFilter jwtAuthentiactionFilter=new JwtjAuthenticationFilter();
    jwtAuthentiactionFilter.setRedisTemplate(redisTemplate);
    return jwtAuthentiactionFilter;
  } 
  
  
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception{
    log.info("실행");
    
    //폼 인증 방식에서 사용하는 방법, jwt 인증 방식에서는 사용하지 않음, mpa에서만 사용
    /*DaoAuthenticationProvider provider=new DaoAuthenticationProvider();//db에 접근해서 인증 정보를 가져오는 역할
    provider.setUserDetailsService(new CustomUserDetailsService());//db에 저장할 수 있도록 도움
    provider.setPasswordEncoder(passwordEncoder());
    auth.authenticationProvider(provider);*/
    
  }
  
  @Override
  public void configure(WebSecurity web) throws Exception{
    log.info("실행");
    DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
    defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchyImpl()); //roleHierarchyImpl 이름을 가지는 객체를 주입받아 매개값으로 이용  
    web.expressionHandler(defaultWebSecurityExpressionHandler);
    /*web.ignoring()//스프링 Security가 관여하지 않는 경로(동작 자체를 하지 않음) 
      //mpa가 사용하는 경로들, rest api는 css를 다운받지 않음, 사실상 이 방법은 rest에서 사용하지 않는 방식임,
      //mpa에서 시큐리티를 적용하지 않는 경로를 설정할 경우에 사용함
      .antMatchers("/images/**")
      .antMatchers("/css/**")
      .antMatchers("/js/**")
      .antMatchers("/bootstrap/**")
      .antMatchers("/jquery/**")
      .antMatchers("/favicon.ico");*/ //frontend의 url 자리
  }
  
  @Bean
  public PasswordEncoder passwordEncoder() {//회원가입할 때 사용
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();//db에서 암호화 되어있는것을 해석할 수 있음, 가장 최신의 암호화를 적용 하고 싶다면 이 방식
    //return new BCryptPasswordEncoder();//db에 저장이 될 때는 암호화한 것으로 저장하게 되면 {bcrypt}가 빠짐, 최신의 암호화가 아닌, 기존의 방식을 계속 유지
  }
  
  @Bean//메소드가 다시 실행되는게 아니라 roleHierarchyImpl으로 등록된 관리 객체를 찾아서 넣어주는 것임=>처음 application이 로딩이 될 때 한번만 실행되어야 함
  public RoleHierarchyImpl roleHierarchyImpl() {//권한
     log.info("실행");
     RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
     roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
     return roleHierarchyImpl;
  }
  //Rest API에서만 사용
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {//javaScript에서 무언가 요청했을 경우(ajax) 허용 여부를 판단하는 역할, 이름은 반드시 동일하게 corsConfigurationSource로 작성하기
    log.info("실행");
      CorsConfiguration configuration = new CorsConfiguration();
      //모든 요청 사이트 허용, 
      configuration.addAllowedOrigin("*"); //각기 다른 도메인에서 온 요청도 처리할 수 있음=("*")
      //모든 요청 방식 허용
      configuration.addAllowedMethod("*");//get, post, put, delete 등 모든 요청 방식에 대하여 허용을 할 것임
      //모든 요청 헤더 허용
      configuration.addAllowedHeader("*");//어떤 헤더명이든 받아줄 거임
      //모든 URL 요청에 대해서 위 내용을 적용
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
}
