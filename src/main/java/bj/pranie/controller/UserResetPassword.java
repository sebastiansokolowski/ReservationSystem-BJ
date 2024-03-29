package bj.pranie.controller;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.model.ResetPasswordModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.validation.Valid;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Controller
@RequestMapping("/user")
public class UserResetPassword {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public String resetPasswordPorm(@RequestParam String resetPasswordKey,
                                    Model model) {

        if (!isResetKeyValid(resetPasswordKey)) {
            return "user/invalidToken";
        }
        model.addAttribute("resetPasswordKey", resetPasswordKey);
        model.addAttribute("resetPasswordModel", new ResetPasswordModel());

        return "user/resetPassword";
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam String resetPasswordKey,
                                      @ModelAttribute("resetPasswordModel") @Valid ResetPasswordModel resetPasswordModel,
                                      BindingResult bindingResult) throws MessagingException {
        ModelAndView modelAndView = new ModelAndView();

        if (!isResetKeyValid(resetPasswordKey)) {
            modelAndView.setViewName("user/invalidToken");
            return modelAndView;
        }

        if (!resetPasswordModel.getNewPassword().equals(resetPasswordModel.getNewPasswordRepeat())) {
            bindingResult.rejectValue("newPasswordRepeat", "error.resetPasswordModel", "Powtórzone hasło jest różne od wpisanego.");
        }

        if (!bindingResult.hasErrors()) {
            User user = userDao.findByResetPasswordKey(resetPasswordKey);
            user.setResetPasswordKey(null);

            String hashedPassword = passwordEncoder.encode(resetPasswordModel.getNewPassword());
            user.setPassword(hashedPassword);

            userDao.save(user);

            modelAndView.addObject("successMessage", "Hasło zostało zmienione pomyślnie.");
        }

        modelAndView.addObject("resetPasswordKey", resetPasswordKey);
        modelAndView.setViewName("user/resetPassword");
        return modelAndView;
    }

    private boolean isResetKeyValid(String resetPasswordKey) {
        User user = userDao.findByResetPasswordKey(resetPasswordKey);
        return user != null;
    }
}
