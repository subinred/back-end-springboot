package com.mycompany.backend.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@MapperScan(basePackages = {"com.mycompany.backend.dao"})//mapper interface 위치
public class MyBatisConfig {
	@Resource //매개 변수 주입으로 대체 가능
	private DataSource dataSource;
	
	@Resource
	WebApplicationContext wac;//di를 위한 container (객체 관리를 위한)
	
	@Bean
	public SqlSessionFactory sqlSessionFactory(/*DataSource dataSource*/) throws Exception {
		SqlSessionFactoryBean ssfb=new SqlSessionFactoryBean();
		ssfb.setDataSource(dataSource);//setter 주입 classpath:mybatis/mapper/*.xml
		ssfb.setConfigLocation(wac.getResource("classpath:/mybatis/mapper-config.xml"));
		ssfb.setMapperLocations(wac.getResources("classpath:mybatis/mapper/*.xml"));
		return ssfb.getObject();
	}
	
	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory ssf) {
		SqlSessionTemplate sst=new SqlSessionTemplate(ssf);
		return sst;
	}
}
