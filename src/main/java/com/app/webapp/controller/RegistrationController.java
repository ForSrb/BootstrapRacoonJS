package com.app.webapp.controller;

import com.app.webapp.event.OnRegistrationCompleteEvent;
import com.app.webapp.model.User;
import com.app.webapp.model.VerificationToken;
import com.app.webapp.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class RegistrationController {
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrationController(UserService userService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/login")
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView("registration/login");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    @GetMapping("/sign-up")
    public ModelAndView showSignUpForm() {
        ModelAndView modelAndView = new ModelAndView("registration/sign_up");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    @PostMapping("/sign-up")
    public ModelAndView signUp(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (userService.emailExist(user.getEmail()))
            bindingResult.rejectValue("email", "exist", "Exist.email");
        if (userService.usernameExist(user.getUsername()))
            bindingResult.rejectValue("username", "exist", "Exist.username");
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration/sign_up");
        } else {
            userService.save(user);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
            modelAndView.setViewName("registration/activate_account");
        }
        return modelAndView;
    }

    @GetMapping("/confirm-registration")
    public ModelAndView confirmRegistration(@RequestParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView();
        Optional<VerificationToken> verificationToken = userService.getVerificationToken(token);
        if (verificationToken.isPresent()) {
            User user = verificationToken.get().getUser();
            if (LocalDateTime.now().isBefore(verificationToken.get().getExpirationDate())) {
                user.setActivated(true);
                user.setConfirmPassword(user.getPassword());
                userService.save(user);
                modelAndView.setViewName("redirect:/login");
            } else {
                modelAndView.addObject("tokenExpired", "Token.expired");
                modelAndView.setViewName("registration/bad_user");
            }
        } else {
            modelAndView.addObject("invalidToken", "Token.invalid");
            modelAndView.setViewName("registration/bad_user");
        }
        return modelAndView;
    }
}
