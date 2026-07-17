package tg.edtch.activEducation.cartemetiers.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.cartemetiers.domain.entite.MetierRegionData;
import tg.edtch.activEducation.cartemetiers.repository.MetierRegionDataRepository;
import java.util.List;
@Service @Transactional
public class CarteMetiersService {
    private final MetierRegionDataRepository repo;
    public CarteMetiersService(MetierRegionDataRepository repo) { this.repo = repo; }
    public List<MetierRegionData> getAll() { return repo.findAll(); }
    public List<MetierRegionData> getByRegion(String region) { return repo.findByRegion(region); }
    public MetierRegionData creer(MetierRegionData data) { return repo.save(data); }
}
