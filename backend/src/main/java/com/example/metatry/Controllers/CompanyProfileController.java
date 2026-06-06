package com.example.metatry.Controllers;

import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-profiles")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileRepository repository;

    @GetMapping
    public List<CompanyProfile> getAll() {
        return repository.findAll();
    }

    @GetMapping("/by-name/{companyName}")
    public ResponseEntity<CompanyProfile> getByCompanyName(@PathVariable String companyName) {
        return repository.findByCompanyName(companyName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CompanyProfile profile) {
        if (profile.getCompanyName() == null || profile.getCompanyName().isBlank()) {
            return ResponseEntity.badRequest().body("companyName is required");
        }
        if (repository.existsByCompanyName(profile.getCompanyName())) {
            return ResponseEntity.badRequest().body("Company already exists");
        }
        CompanyProfile saved = repository.save(profile);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CompanyProfile profile) {
        return repository.findById(id).map(existing -> {
            existing.setCompanyName(profile.getCompanyName());
            existing.setInstagramUrl(profile.getInstagramUrl());
            existing.setFacebookUrl(profile.getFacebookUrl());
            existing.setLinkedinUrl(profile.getLinkedinUrl());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}