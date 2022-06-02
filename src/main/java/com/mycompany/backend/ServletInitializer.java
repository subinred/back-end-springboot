package com.mycompany.backend;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BackEndSpringbootApplication.class);
	}
	//독립 실행용으로나는 사용 못하지만, was에 배치될 때 사용하는 파일
}
