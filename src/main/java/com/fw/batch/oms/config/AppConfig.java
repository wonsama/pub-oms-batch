package com.fw.batch.oms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class AppConfig {

  @Autowired
  private Environment env;

  /**
   * 현재 프로파일 가져오기
   *
   * @return
   */
  private String getActiveProfile() {
    System.out.println(env.getActiveProfiles()[0]);
    return env.getActiveProfiles()[0];
  }

  @Bean
  public PropertiesFactoryBean application() {
    PropertiesFactoryBean properties = new PropertiesFactoryBean();

    ClassPathResource application = new ClassPathResource("application.properties");
    ClassPathResource applicationProfile = new ClassPathResource(
        "application-" + this.getActiveProfile() + ".properties");

    properties.setLocations(application, applicationProfile);
    return properties;
  }

}
