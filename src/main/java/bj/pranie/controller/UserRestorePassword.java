package bj.pranie.controller;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.model.RestorePasswordModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Controller
@RequestMapping("/user")
public class UserRestorePassword {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @RequestMapping(value = "/restorePassword", method = RequestMethod.GET)
    public String restoreForm(Model model) {
        model.addAttribute("restorePasswordModel", new RestorePasswordModel());
        return "user/restorePassword";
    }

    @RequestMapping(value = "/restorePassword", method = RequestMethod.POST)
    public ModelAndView restorePassword(@RequestHeader String host, @ModelAttribute("restorePasswordModel") @Valid RestorePasswordModel restorePasswordModel,
                                        BindingResult bindingResult) throws MessagingException {
        ModelAndView modelAndView = new ModelAndView();

        User user = userDao.findByEmail(restorePasswordModel.getEmail());

        if (!bindingResult.hasErrors()) {
            if (user != null) {
                String resetPasswordKey = RandomStringUtils.randomAlphanumeric(20);

                user.setResetPasswordKey(resetPasswordKey);
                userDao.save(user);

                sendMail(user, resetPasswordKey, host);
                modelAndView.addObject("host", host);
                modelAndView.addObject("successMessage", "Link do resetowania hasła został wysłany na podany adres email.");
            } else {
                bindingResult.rejectValue("email", "error.restorePasswordDto", "Podany adres email nie istnieje w bazie danych.");
            }
        }

        modelAndView.setViewName("user/restorePassword");
        return modelAndView;
    }

    private void sendMail(User user, String resetPasswordKey, String host) throws MessagingException {
        MimeMessage mail = emailSender.createMimeMessage();

        Context context = new Context();
        context.setVariable("host", host);
        context.setVariable("name", user.getName());
        context.setVariable("resetPasswordKey", resetPasswordKey);

        String body = templateEngine.process("email/restorePassword", context);

        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setTo(user.getEmail());
        helper.setReplyTo("rm.bursa@samorzad.uj.edu.pl");
        helper.setFrom("rm.bursa@samorzad.uj.edu.pl");
        helper.setSubject("Resetowanie hasła " + host);
        helper.setText(body, true);

        emailSender.send(mail);
    }
}
