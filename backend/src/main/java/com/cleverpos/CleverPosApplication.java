package com.cleverpos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.*;
@SpringBootApplication
public class CleverPosApplication {
  public static void main(String[] a){
    String tz = System.getenv("APP_TIMEZONE"); java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tz == null || tz.isBlank() ? "Asia/Kolkata" : tz));
    SpringApplication.run(CleverPosApplication.class,a); }
  @Bean WebMvcConfigurer cors(){ return new WebMvcConfigurer(){
    public void addCorsMappings(CorsRegistry r){ r.addMapping("/api/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*"); } }; }
}
