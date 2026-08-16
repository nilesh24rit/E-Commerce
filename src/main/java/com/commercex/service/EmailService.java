package com.commercex.service;

import com.commercex.event.CouponAppliedEvent;
import com.commercex.event.OrderCreatedEvent;
import com.commercex.event.PaymentCompletedEvent;
import com.commercex.event.ReviewAddedEvent;
import com.commercex.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@commercex.com");
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for {}", event.getEmail());
        String subject = "Welcome to CommerceX, " + event.getFirstName() + "!";
        String htmlBody = "<h1>Welcome to CommerceX</h1>" +
                "<p>Hi " + event.getFirstName() + ",</p>" +
                "<p>Thank you for registering. We are thrilled to have you!</p>";
        sendHtmlEmail(event.getEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent for {}", event.getOrderId());
        String subject = "Order Confirmation: " + event.getOrderId();
        String htmlBody = "<h1>Order Received</h1>" +
                "<p>We have successfully received your order.</p>" +
                "<p>Order ID: " + event.getOrderId() + "</p>";
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Handling PaymentCompletedEvent for {}", event.getOrderId());
        String subject = "Payment Received";
        String htmlBody = "<h1>Payment Successful</h1>" +
                "<p>Your payment of " + event.getAmount() + " has been processed.</p>";
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleCouponApplied(CouponAppliedEvent event) {
        log.info("Handling CouponAppliedEvent for {}", event.getCouponCode());
        String subject = "Coupon Successfully Applied";
        String htmlBody = "<h1>You got a discount!</h1>" +
                "<p>Coupon " + event.getCouponCode() + " was applied to your recent order.</p>";
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleReviewAdded(ReviewAddedEvent event) {
        log.info("Handling ReviewAddedEvent for {}", event.getProductId());
        String subject = "Thank you for your review!";
        String htmlBody = "<h1>We value your feedback</h1>" +
                "<p>Your review has been successfully posted. Thank you for helping the community!</p>";
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }
}
