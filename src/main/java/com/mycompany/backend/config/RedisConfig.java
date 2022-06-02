package com.mycompany.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class RedisConfig {
  /*@Resource
  RedisConnectionFactory redisConnectionFactory;*/
  
  //변경사항이 발생했을 때 application.properties값만 변경하면 되도록
  @Value("${spring.redis.hostName}")
  private String hostName;//@value를 이용해 의존성 주입
  
  @Value("${spring.redis.port}")
  private String port;
  
  @Value("${spring.redis.password}")
  private String password;
  
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    log.info("실행");
    RedisStandaloneConfiguration config=new RedisStandaloneConfiguration();
    config.setHostName(hostName);
    config.setPort(Integer.parseInt(port));
    config.setPassword(password);
    LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(config);
    return connectionFactory;
  }
  
  @Bean //실제로 의존 주입해서 사용할 객체
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory){//key와 value 타입 String으로 지정
    log.info("실행");
    RedisTemplate<String, String> redisTemplate=new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }
  
}
