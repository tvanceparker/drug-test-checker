"""
Optional helper to send an email alert. Configure SMTP credentials locally when needed.
"""
import smtplib
from email.message import EmailMessage

def send_email(smtp_server, smtp_port, username, password, to_email, subject, body):
    msg = EmailMessage()
    msg['Subject'] = subject
    msg['From'] = username
    msg['To'] = to_email
    msg.set_content(body)

    with smtplib.SMTP_SSL(smtp_server, smtp_port) as s:
        s.login(username, password)
        s.send_message(msg)

if __name__ == '__main__':
    print('This is a helper module; call send_email(...)')
