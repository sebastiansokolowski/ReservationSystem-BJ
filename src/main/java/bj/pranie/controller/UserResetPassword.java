package bj.pranie.controller;

import bj.pranie.config.exception.ResourceNotFoundException;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.model.ResetPasswordModel;
import bj.pranie.model.RestorePasswordModel;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        if (!checkResetKeyIsValid(resetPasswordKey)) {
            throw new ResourceNotFoundException();
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

        if (!checkResetKeyIsValid(resetPasswordKey)) {
            throw new ResourceNotFoundException();
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

        modelAndView.setViewName("user/resetPassword");
        return modelAndView;
    }

    private boolean checkResetKeyIsValid(String resetPasswordKey) {
        User user = userDao.findByResetPasswordKey(resetPasswordKey);
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }
}
