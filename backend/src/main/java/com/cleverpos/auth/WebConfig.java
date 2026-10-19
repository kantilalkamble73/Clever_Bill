package com.cleverpos.auth;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final AuthInterceptor ai;
  public WebConfig(AuthInterceptor ai){ this.ai = ai; }
  public void addInterceptors(InterceptorRegistry r){ r.addInterceptor(ai).addPathPatterns("/api/**"); }
}
