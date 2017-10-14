package bj.pranie.controller;

import bj.pranie.dao.UserDao;
import bj.pranie.dto.RestorePasswordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Controller
@RequestMapping("/user")
public class UserRestorePassword {
    private final static String TAG = UserRestorePassword.class.getSimpleName();

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = "/restore", method = RequestMethod.GET)
    public String restoreForm(Model model) {
        model.addAttribute("restore", new RestorePasswordDto());
        return "user/restore";
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public String restorePassword(@ModelAttribute RestorePasswordDto restorePasswordDto, Model model) {
        //User user = userDao.findByEmail(restorePasswordDto.getEmail());

        if (true) {
            model.addAttribute("error", "The id selected is out of Range, please select another id within range");
        }
        return "user/restore";
//
//        //generate new password
//        String newPassword = randomString(10);
//        user.setPassword(newPassword);
//        userDao.save(user);
//
//        //send email with new password
//        sendEmail(user);
//
//        return model;
    }

//    private void sendEmail(User user) {
//        try {
//            SendGrid grid = new SendGrid(System.getenv("SENDGRID_USERNAME"), System.getenv("SENDGRID_PASSWORD"));
//            SendGrid.Email email = new SendGrid.Email();
//
//            String message = "Witaj!\nPoniżej znajduje się nowe hasło do serwisu www.bjpranie.pl, niezwłocznie po zalogowaniu zmień hasło na nowe!\nHasło:" + user.getPassword();
//
//            email.addTo(user.getEmail());
//            email.setFrom("bursa.jagiellonska@gmail.com");
//            email.setSubject("Nowe hasło do zapisów na pranie w D.S. Bursa Jagiellońska");
//            email.setHtml(message);
//
//            grid.send(email);
//        } catch (SendGridException ex) {
//            Logger.getLogger(TAG).log(null, ex);
//        }
//    }

    public static String randomString(int len) {
        char[] str = new char[100];

        for (int i = 0; i < len; i++) {
            str[i] = (char) (((int) (Math.random() * 26)) + (int) 'A');
        }

        return (new String(str, 0, len));
    }
}
