package tg.edtch.activEducation.profil.domain.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;
import tg.edtch.activEducation.profil.application.mapper.EleveMapper;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.minio.service.MinioService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EleveServiceTest {

    @Mock private EleveRepository eleveRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EleveMapper eleveMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MinioService minioService;

    private EleveServiceImpl eleveService;

    @BeforeEach
    void setUp() {
        eleveService = new EleveServiceImpl(eleveRepository, utilisateurRepository,
                roleRepository, eleveMapper, passwordEncoder, minioService);
    }

    @Test
    void inscrireEleve_shouldCreateAndReturnResponse() {
        EleveRequest request = EleveRequest.builder()
                .email("test@test.com")
                .motDePasse("password123")
                .nom("Doe")
                .prenom("John")
                .typeApprenant(TypeApprenant.LYCEEN)
                .build();
        Role role = Role.builder().nom(RoleNom.ROLE_ELEVE).build();
        Eleve entity = new Eleve();
        entity.setTrackingId(UUID.randomUUID());
        Eleve saved = new Eleve();
        saved.setTrackingId(entity.getTrackingId());
        EleveResponse response = EleveResponse.builder()
                .trackingId(entity.getTrackingId())
                .email("test@test.com")
                .build();

        when(utilisateurRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(roleRepository.findByNom(RoleNom.ROLE_ELEVE)).thenReturn(Optional.of(role));
        when(eleveMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(eleveRepository.save(any(Eleve.class))).thenReturn(saved);
        when(eleveMapper.toResponse(saved)).thenReturn(response);

        EleveResponse result = eleveService.inscrireEleve(request);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        verify(eleveRepository).save(entity);
    }

    @Test
    void inscrireEleve_shouldThrowWhenEmailExists() {
        EleveRequest request = EleveRequest.builder()
                .email("exists@test.com")
                .motDePasse("password123")
                .nom("Doe")
                .prenom("John")
                .typeApprenant(TypeApprenant.LYCEEN)
                .build();

        when(utilisateurRepository.existsByEmail("exists@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> eleveService.inscrireEleve(request));
        verify(eleveRepository, never()).save(any());
    }

    @Test
    void inscrireEleve_shouldThrowWhenPasswordTooShort() {
        EleveRequest request = EleveRequest.builder()
                .email("test@test.com")
                .motDePasse("short")
                .nom("Doe")
                .prenom("John")
                .typeApprenant(TypeApprenant.LYCEEN)
                .build();

        when(utilisateurRepository.existsByEmail("test@test.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> eleveService.inscrireEleve(request));
    }

    @Test
    void getEleve_shouldReturnResponse() {
        UUID trackingId = UUID.randomUUID();
        Eleve entity = new Eleve();
        entity.setTrackingId(trackingId);
        EleveResponse response = EleveResponse.builder()
                .trackingId(trackingId)
                .email("test@test.com")
                .build();

        when(eleveRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(entity));
        when(eleveMapper.toResponse(entity)).thenReturn(response);

        EleveResponse result = eleveService.getEleve(trackingId);

        assertNotNull(result);
        assertEquals(trackingId, result.getTrackingId());
    }

    @Test
    void getEleve_shouldThrowWhenNotFound() {
        UUID trackingId = UUID.randomUUID();
        when(eleveRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eleveService.getEleve(trackingId));
    }

    @Test
    void desactiverEleve_shouldSoftDelete() {
        UUID trackingId = UUID.randomUUID();
        Eleve entity = new Eleve();
        entity.setTrackingId(trackingId);
        entity.setEstActif(true);

        when(eleveRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(entity));
        when(eleveRepository.save(any(Eleve.class))).thenAnswer(i -> i.getArgument(0));

        eleveService.desactiverEleve(trackingId);

        assertFalse(entity.getEstActif());
        verify(eleveRepository).save(entity);
    }
}
