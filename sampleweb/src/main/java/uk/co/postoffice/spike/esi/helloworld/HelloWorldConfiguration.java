package uk.co.postoffice.spike.esi.helloworld;

import jacoco.JacocoAgentProxyController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@ComponentScan
public class HelloWorldConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

//    @Bean
//    public JacocoAgentProxyController jacocoControllerHttpProxy() {
//        return new JacocoAgentProxyController();
//    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        final TemplateResolver servletContextTemplateResolver = new org.thymeleaf.templateresolver.ServletContextTemplateResolver() {{
            setOrder(20);
            setPrefix("/WEB-INF/templates/");
            setSuffix(".html");
            setTemplateMode("HTML5");
            setCharacterEncoding("UTF-8");
            setCacheable(false);
        }};

        final SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine() {{
            addTemplateResolver(servletContextTemplateResolver);
            addDialect(new SpringSecurityDialect());
        }};

        final ThymeleafMasterLayoutViewResolver viewResolver = new ThymeleafMasterLayoutViewResolver() {{
            setCharacterEncoding("UTF-8");
            setTemplateEngine(springTemplateEngine);
            setCache(false);
//            setFullPageLayout("layout/fullPageLayout");
        }};

        return viewResolver;
    }
}
