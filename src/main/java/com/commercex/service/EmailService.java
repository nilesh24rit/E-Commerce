package com.commercex.service;

import com.commercex.event.CouponAppliedEvent;
import com.commercex.event.OrderCancelledEvent;
import com.commercex.event.OrderCreatedEvent;
import com.commercex.event.PasswordResetEvent;
import com.commercex.event.PaymentCompletedEvent;
import com.commercex.event.RefundProcessedEvent;
import com.commercex.event.ReviewAddedEvent;
import com.commercex.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

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

    public String renderTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            return templateEngine.process("email/" + templateName, context);
        } catch (Exception e) {
            log.warn("Failed to render Thymeleaf template: email/{}. Using fallback HTML.", templateName, e);
            return null;
        }
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for {}", event.getEmail());
        String subject = "Welcome to CommerceX, " + event.getFirstName() + "!";
        String htmlBody = renderTemplate("user-registered", Map.of("firstName", event.getFirstName()));
        if (htmlBody == null) {
            htmlBody = "<h1>Welcome to CommerceX</h1><p>Hi " + event.getFirstName() + ", thank you for registering!</p>";
        }
        sendHtmlEmail(event.getEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent for {}", event.getOrderId());
        String subject = "Order Confirmation: " + event.getOrderId();
        String htmlBody = renderTemplate("order-created", Map.of("orderId", event.getOrderId()));
        if (htmlBody == null) {
            htmlBody = "<h1>Order Received</h1><p>Order ID: " + event.getOrderId() + "</p>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Handling PaymentCompletedEvent for {}", event.getOrderId());
        String subject = "Payment Received";
        String htmlBody = renderTemplate("payment-completed", Map.of("amount", event.getAmount()));
        if (htmlBody == null) {
            htmlBody = "<h1>Payment Successful</h1><p>Amount: " + event.getAmount() + "</p>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleCouponApplied(CouponAppliedEvent event) {
        log.info("Handling CouponAppliedEvent for {}", event.getCouponCode());
        String subject = "Coupon Successfully Applied";
        String htmlBody = renderTemplate("coupon-applied", Map.of("couponCode", event.getCouponCode()));
        if (htmlBody == null) {
            htmlBody = "<h1>Coupon Applied</h1><p>Code: " + event.getCouponCode() + "</p>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleReviewAdded(ReviewAddedEvent event) {
        log.info("Handling ReviewAddedEvent for {}", event.getProductId());
        String subject = "Thank you for your review!";
        String htmlBody = renderTemplate("review-added", Map.of());
        if (htmlBody == null) {
            htmlBody = "<h1>Thank you for your review!</h1>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Handling PasswordResetEvent for {}", event.getEmail());
        String subject = "Password Reset Request";
        String htmlBody = renderTemplate("password-reset", Map.of("resetToken", event.getResetToken()));
        if (htmlBody == null) {
            htmlBody = "<h1>Password Reset</h1><p>Token: " + event.getResetToken() + "</p>";
        }
        sendHtmlEmail(event.getEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent for {}", event.getOrderId());
        String subject = "Order Cancelled: " + event.getOrderId();
        String htmlBody = renderTemplate("order-cancelled", Map.of(
                "orderId", event.getOrderId(),
                "reason", event.getReason() != null ? event.getReason() : "Customer requested cancellation"
        ));
        if (htmlBody == null) {
            htmlBody = "<h1>Order Cancelled</h1><p>Order ID: " + event.getOrderId() + "</p>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }

    @Async
    @EventListener
    public void handleRefundProcessed(RefundProcessedEvent event) {
        log.info("Handling RefundProcessedEvent for payment {}", event.getPaymentId());
        String subject = "Refund Processed for Payment: " + event.getPaymentId();
        String htmlBody = renderTemplate("refund-processed", Map.of(
                "paymentId", event.getPaymentId(),
                "amount", event.getAmount(),
                "reason", event.getReason() != null ? event.getReason() : "Refund requested"
        ));
        if (htmlBody == null) {
            htmlBody = "<h1>Refund Processed</h1><p>Amount: $" + event.getAmount() + "</p>";
        }
        sendHtmlEmail(event.getUserEmail(), subject, htmlBody);
    }
}
