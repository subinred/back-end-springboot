package com.mycompany.backend.controller;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@ControllerAdvice
public class ErrorHandlerController implements ErrorController {
  @RequestMapping("/error")
  public ResponseEntity<String> error(HttpServletResponse response) {
    int status = response.getStatus();//response의 상태 객체를 획득
    if(status == 404) {//상태가 404일 겨우(사용자의 엉뚱한 url 입력)
      return ResponseEntity
          .status(HttpStatus.MOVED_PERMANENTLY)//redirect 응답 코드: 301
          .location(URI.create("/"))
          .body("");
    } else {//404가 아닌 경우는 상태 그대로 front에 전달
      return ResponseEntity
          .status(status)
          .body("");
    }
  }
}
