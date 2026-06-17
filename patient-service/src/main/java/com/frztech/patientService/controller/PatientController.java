package com.frztech.patientService.controller;

import com.frztech.patientService.dto.PatientRequestDTO;
import com.frztech.patientService.dto.PatientResponseDTO;
import com.frztech.patientService.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")  //all requests having url: localhost:4000/patients will be handled by this controller
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getPatients();

        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
                                                            @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }
}
