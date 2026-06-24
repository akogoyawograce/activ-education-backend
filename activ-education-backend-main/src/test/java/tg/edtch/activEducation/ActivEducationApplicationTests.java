package tg.edtch.activEducation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Nécessite PostgreSQL+pgvector — exécuter uniquement avec DB de dev")
class ActivEducationApplicationTests {

	@Test
	void contextLoads() {
	}

}
