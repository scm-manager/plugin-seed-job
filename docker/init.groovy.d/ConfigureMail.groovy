import jenkins.model.*;

def instance = Jenkins.getInstance();

// configure mail server
def mailer = instance.getDescriptor("hudson.tasks.Mailer");
// mailer.setReplyToAddress("");
mailer.setSmtpHost("mail");
mailer.setUseSsl(false);
mailer.setSmtpPort("1025");
mailer.setCharset("UTF-8");
// mailer.setSmtpAuth("", "");
mailer.save();


instance.save();
