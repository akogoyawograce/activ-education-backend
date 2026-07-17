package tg.edtch.activEducation.cartemetiers.application.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.cartemetiers.domain.entite.MetierRegionData;
import tg.edtch.activEducation.cartemetiers.domain.service.CarteMetiersService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/carte-metiers")
@PreAuthorize("hasRole('ADMIN')")
public class CarteMetiersController {
    private final CarteMetiersService service;
    public CarteMetiersController(CarteMetiersService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<MetierRegionData>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/region/{region}") public ResponseEntity<List<MetierRegionData>> getByRegion(@PathVariable String region) { return ResponseEntity.ok(service.getByRegion(region)); }
    @PostMapping public ResponseEntity<MetierRegionData> creer(@RequestBody MetierRegionData data) { return ResponseEntity.ok(service.creer(data)); }
}
