package com.api.library.utilities;

import java.util.List;
import java.util.stream.Collectors;

import com.api.library.loan.Loan;
import com.api.library.loan.LoanService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private static final String CRON_LATE_LOANS = "0 0 0 1/1 * *";

  private final LoanService loanService;

  private final EmailService emailService;

  @Value("${application.mail.lateloans.message}")
  private String message;

  @Scheduled(cron = CRON_LATE_LOANS)
  public void sendMailToLateLoans() {
    List<Loan> lateLoans = loanService.findAllLateLoands();

    List<String> mails = lateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());

    emailService.sendMails(message, mails);
  }
}
