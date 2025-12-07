package com.hms.PharmacyMS.listener;

import com.hms.PharmacyMS.entity.Sale;
import com.hms.PharmacyMS.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final SaleRepository saleRepository;

    @KafkaListener(topics = "payment_success_topic", groupId = "pharmacy_group")
    public void handlePaymentSuccess(Map<String, Object> event) {
        System.out.println("PharmacyMS nhận được sự kiện thanh toán: " + event);

        try {
            // Kiểm tra xem event có phải trả cho Pharmacy không
            String source = (String) event.get("source");
            if ("PHARMACY".equals(source)) {

                // Lấy SaleID (cẩn thận ép kiểu vì Kafka gửi số có thể là Integer hoặc Long)
                String orderIdStr = String.valueOf(event.get("orderId"));
                Long saleId = Long.parseLong(orderIdStr);

                // Cập nhật Database
                Sale sale = saleRepository.findById(saleId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + saleId));

                if (!"PAID".equals(sale.getStatus())) {
                    sale.setStatus("PAID");
                    saleRepository.save(sale);
                    System.out.println("✅ Đã cập nhật trạng thái PAID cho đơn hàng: " + saleId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi khi xử lý message Kafka: " + e.getMessage());
        }
    }
}