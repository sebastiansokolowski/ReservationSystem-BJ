package bj.pranie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Sebastian Sokolowski on 05.09.17.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/wm/week").setViewName("wm/week");
        registry.addViewController("/week/wm").setViewName("wm/wm");
        registry.addViewController("/user/register").setViewName("user/register");
        registry.addViewController("/user/restore").setViewName("user/restore");
        registry.addViewController("/user/settings").setViewName("user/settings");
    }

}
