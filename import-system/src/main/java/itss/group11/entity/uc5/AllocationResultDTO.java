package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chá»©a káº¿t quáº£ tráº£ vá» sau khi thá»±c hiá»‡n xá»­ lÃ½ láº­p káº¿ hoáº¡ch Ä‘áº·t hÃ ng (UC005)
 * DÃ¹ng Ä‘á»ƒ Ä‘áº©y dá»¯ liá»‡u tá»« Service qua Controller vÃ  tráº£ vá» cho Frontend hiá»ƒn thá»‹.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResultDTO {
    
    private String requestCode;       // MÃ£ yÃªu cáº§u nháº­p hÃ ng (VD: REQ-9942A)
    private boolean isSuccess;        // Tráº¡ng thÃ¡i xá»­ lÃ½ (true: ThÃ nh cÃ´ng, false: Tháº¥t báº¡i)
    private String message;           // ThÃ´ng bÃ¡o chi tiáº¿t (VD: "PhÃ¢n bá»• thÃ nh cÃ´ng", "Thiáº¿u hÃ ng tá»“n kho")
    private List<String> generatedPoCodes; // Danh sÃ¡ch cÃ¡c mÃ£ Ä‘Æ¡n hÃ ng PO Ä‘Æ°á»£c tá»± Ä‘á»™ng sinh ra sau khi phÃ¢n bá»•
}
