package com.hms.PaymentMS.api;

import com.hms.PaymentMS.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentAPI {

    private final PaymentService paymentService;

    // API 1: Tạo link thanh toán
    @PostMapping("/create-momo")
    public ResponseEntity<String> createMomo(@RequestParam String orderId,
                                             @RequestParam Double amount) {
        String payUrl = paymentService.createMomoPayment(orderId, amount, "Thanh toan vien phi");
        return ResponseEntity.ok(payUrl);
    }

    // API 2: IPN Callback (MoMo gọi vào đây để báo kết quả)
    // Lưu ý: Phải dùng Ngrok để public port 8086 thì MoMo mới gọi được
    @PostMapping("/ipn-callback")
    public ResponseEntity<Void> ipnCallback(@RequestBody Map<String, Object> response) {
        // Kiểm tra chữ ký (Signature) tại đây để đảm bảo an toàn (tương tự lúc tạo)
        System.out.println("MoMo Callback: " + response);

        if ("0".equals(response.get("resultCode").toString())) {
            // Thanh toán thành công -> Update DB -> Bắn Kafka Event
            System.out.println("Thanh toán THÀNH CÔNG cho đơn: " + response.get("orderId"));
        }
        return ResponseEntity.noContent().build();
    }
}