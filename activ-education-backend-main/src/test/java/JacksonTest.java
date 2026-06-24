import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DummyRequest {
    private String titre;
    private String resume;
}

public class JacksonTest {
    public static void main(String[] args) throws Exception {
        String json = "{\"titre\":\"test\", \"resume\":\"test2\"}";
        ObjectMapper mapper = new ObjectMapper();

        try {
            DummyRequest req = mapper.readValue(json, DummyRequest.class);
            System.out.println("Result: titre=" + req.getTitre() + " resume=" + req.getResume());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String emptyJson = "{}";
        try {
            DummyRequest req2 = mapper.readValue(emptyJson, DummyRequest.class);
            System.out.println("Result Empty: titre=" + req2.getTitre());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
