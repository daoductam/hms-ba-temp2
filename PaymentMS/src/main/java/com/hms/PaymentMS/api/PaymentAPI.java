package com.hms.PaymentMS.api;

import com.hms.PaymentMS.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentAPI {

    private final PaymentService paymentService;


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // API 1: Tạo link thanh toán
    @PostMapping("/create-momo")
    public ResponseEntity<String> createMomo(@RequestParam String orderId,
                                             @RequestParam Double amount) {
        String payUrl = paymentService.createMomoPayment(orderId, amount, "Thanh toan vien phi");
        return ResponseEntity.ok(payUrl);
    }

    @PostMapping("/ipn-callback")
    public ResponseEntity<Void> ipnCallback(@RequestBody Map<String, Object> response) {
        // ... (Logic kiểm tra chữ ký giữ nguyên) ...

        if ("0".equals(response.get("resultCode").toString())) {
            String orderId = response.get("orderId").toString();
            String transId = response.get("transId").toString();
            Long amount = Long.valueOf(response.get("amount").toString());

            System.out.println("Thanh toán THÀNH CÔNG cho đơn: " + orderId);

            // 2. TẠO EVENT & BẮN KAFKA (Phần bổ sung)
            // Lưu ý: orderId của bạn đang format là "SALE-123" hoặc "123", hãy thống nhất nhé.
            // Ở đây tôi giả sử orderId chính là saleId (Long)

            Map<String, Object> event = new HashMap<>();
            event.put("orderId", orderId);
            event.put("amount", amount);
            event.put("transactionId", transId);
            event.put("status", "SUCCESS");
            event.put("source", "PHARMACY"); // Đánh dấu nguồn là từ nhà thuốc

            // Gửi message vào topic "payment_success_topic"
            kafkaTemplate.send("payment_success_topic", event);
        }
        return ResponseEntity.noContent().build();
    }
}