package com.api.library.utilities;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImple implements EmailService {

  @Value("${application.mail.from}")
  private String from;

  @Autowired
  private JavaMailSender javaMailSender;

  @Override
  public void sendMails(String message, List<String> mails) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();

    mailMessage.setFrom(from);
    mailMessage.setSubject("Livro com empréstimo atrasado");
    mailMessage.setText(message);

    String[] stringifyedEmails = mails.toArray(new String[mails.size()]);

    mailMessage.setTo(stringifyedEmails);

    javaMailSender.send(mailMessage);
  }

}
