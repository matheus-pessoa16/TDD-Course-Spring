package com.api.library.utilities;

import java.util.List;

public interface EmailService {

  public void sendMails(String message, List<String> mails);

}
