package bj.pranie.controller;

import bj.pranie.service.UserServiceImpl;
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
    private UserServiceImpl userService;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @RequestMapping(value = "/restore", method = RequestMethod.GET)
    public String restoreForm(Model model) {
        model.addAttribute("restorePasswordModel", new RestorePasswordModel());
        return "user/restore";
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public ModelAndView restorePassword(@ModelAttribute("restorePasswordModel") @Valid RestorePasswordModel restorePasswordModel,
                                        BindingResult bindingResult) throws MessagingException {
        ModelAndView modelAndView = new ModelAndView();

        User user = userService.findByEmail(restorePasswordModel.getEmail());

        if (!bindingResult.hasErrors()) {
            if (user != null) {
                String newPassword = RandomStringUtils.randomAlphanumeric(10);
                //TODO: save password
                sendMail(user, newPassword);
                modelAndView.addObject("successMessage", "Hasło tymczasowe zostało wysłane na podany adres email.");
            } else {
                bindingResult.rejectValue("email", "error.restorePasswordDto", "Podany adres email nie istnieje w bazie.");
            }
        }

        modelAndView.setViewName("user/restore");
        return modelAndView;
    }

    private void sendMail(User user, String newPassword) throws MessagingException {
        MimeMessage mail = emailSender.createMimeMessage();

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("newPassword", newPassword);

        String body = templateEngine.process("email/restorePassword", context);

        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setTo(user.getEmail());
        helper.setReplyTo("bursa.jagiellonska@gmail.com");
        helper.setFrom("bursa.jagiellonska@gmail.com");
        helper.setSubject("Resetowanie hasła www.bj-pranie.pl");
        helper.setText(body, true);

        emailSender.send(mail);
    }
}
