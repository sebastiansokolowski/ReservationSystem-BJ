package bj.pranie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Controller
public class LanguageController {

    private static final Locale POLISH = new Locale("pl");

    @Autowired
    private LocaleResolver localeResolver;

    @RequestMapping(value = "/language", method = RequestMethod.GET)
    public String changeLanguage(@RequestParam String lang,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        Locale locale = "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : POLISH;
        localeResolver.setLocale(request, response, locale);

        return "redirect:" + getSafeRedirectPath(referer, request);
    }

    private String getSafeRedirectPath(String referer, HttpServletRequest request) {
        if (referer == null) {
            return "/";
        }

        try {
            URI uri = new URI(referer);
            if (uri.getHost() == null || !uri.getHost().equalsIgnoreCase(request.getServerName())) {
                return "/";
            }

            String path = uri.getRawPath();
            if (path == null || !path.startsWith("/") || path.equals("/language")) {
                return "/";
            }

            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (URISyntaxException exception) {
            return "/";
        }
    }
}
