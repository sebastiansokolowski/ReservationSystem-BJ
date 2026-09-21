package bj.pranie.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.MessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Created by Sebastian Sokolowski on 05.09.17.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    private static final Locale POLISH = new Locale("pl");

    @Autowired
    private MessageSource messageSource;

    @Bean
    public LocaleResolver localeResolver() {
        return new SessionLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(HttpServletRequest request) {
                Enumeration<Locale> preferredLocales = request.getLocales();
                while (preferredLocales.hasMoreElements()) {
                    Locale locale = preferredLocales.nextElement();
                    if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
                        return Locale.ENGLISH;
                    }
                    if (POLISH.getLanguage().equals(locale.getLanguage())) {
                        return POLISH;
                    }
                }
                return POLISH;
            }
        };
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    @Override
    public Validator getValidator() {
        return validator();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("index");
        registry.addViewController("/regulations").setViewName("regulations");
        registry.addViewController("/user/regulations").setViewName("user/regulations");
        registry.addViewController("/user/rodo").setViewName("user/rodo");
    }

}
