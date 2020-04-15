package com.app.webapp.event.listener;

import com.app.webapp.event.OnRegistrationCompleteEvent;
import com.app.webapp.model.AuthToken;
import com.app.webapp.model.AuthTokenProperties;
import com.app.webapp.model.User;
import com.app.webapp.repository.AuthTokenRepository;
import com.app.webapp.service.EmailService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OnRegistrationCompleteListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    private final AuthTokenRepository authTokenRepository;
    private final EmailService emailService;

    public OnRegistrationCompleteListener(AuthTokenRepository authTokenRepository, EmailService emailService) {
        this.authTokenRepository = authTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        authTokenRepository.save(new AuthToken(user, token, AuthTokenProperties.VERIFICATION));
        sendEmail(user, token);
    }

    private void sendEmail(User user, String token) {
        String to = user.getEmail();
        String subject = "Registration Confirmation";
        String body = "Please activate your account by clicking on link.\n" +
                "http://localhost:12345/api/auth/confirm-registration?token=" + token;

        emailService.sendEmail(to, subject, body);
    }
}
