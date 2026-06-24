package tg.edtch.activEducation.shared.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tg.edtch.activEducation.shared.security.jwt.JwtService;
import tg.edtch.activEducation.shared.security.userdetails.UtilisateurDetailsService;
import tg.edtch.activEducation.shared.util.AuditLogService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestBeansConfig {
    @Bean @Primary
    public JwtService jwtService() { return mock(JwtService.class); }
    @Bean @Primary
    public UtilisateurDetailsService utilisateurDetailsService() { return mock(UtilisateurDetailsService.class); }
    @Bean @Primary
    public AuditLogService auditLogService() { return mock(AuditLogService.class); }
    @Bean @Primary
    public StringRedisTemplate stringRedisTemplate() { return mock(StringRedisTemplate.class); }
    @Bean @Primary
    public PasswordEncoder passwordEncoder() { return mock(PasswordEncoder.class); }
}
