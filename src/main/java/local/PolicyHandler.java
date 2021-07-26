package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{

    @Autowired
    ProductionRepository productionRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    // 결제완료시 제작시작 (-->제작완료)
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCompleted_ProductionComplete(@Payload PaymentCompleted paymentCompleted){

        if(paymentCompleted.isMe()){           
            System.out.println("##### 결제 완료로 인한 제조 확정: " + paymentCompleted.toJson());
            if(paymentCompleted.isMe()){
                Production temp = new Production();
                temp.setStatus("PRODUCTION_COMPLETED");
                temp.setCustNm(paymentCompleted.getCustNm());
                temp.setMenuId(paymentCompleted.getMenuId());
                temp.setMenuNm(paymentCompleted.getMenuNm());
                temp.setOrderId(paymentCompleted.getId());
                productionRepository.save(temp);
            }
        }
    }

    // 메뉴삭제시 강제제작취소
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMenuDeleted_ForcedProductionCancel(@Payload MenuDeleted menuDeleted){

        if(menuDeleted.isMe()){
            System.out.println("##### listener ForcedProductionCanceled : " + menuDeleted.toJson());
//            List<Production> list = productionRepository.findByMenuId(String.valueOf(menuDeleted.getId()));
            List<Production> list = productionRepository.findByMenuId(menuDeleted.getId());
            for(Production temp : list){
                if(!"CANCELED".equals(temp.getStatus())) {
                    temp.setStatus("FORCE_CANCELED");
                    productionRepository.save(temp);
                }
            }
        }
    }

    // 주문취소시 제작취소
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_ProductionCancel(@Payload OrderCanceled orderCanceled){

        if(orderCanceled.isMe()){
            Production temp = productionRepository.findByOrderId(orderCanceled.getId());
            temp.setStatus("CANCELED");
            productionRepository.save(temp);

        }
    }

}
