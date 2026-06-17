package com.frztech.patientService.service;

import com.frztech.patientService.dto.PatientResponseDTO;
import com.frztech.patientService.mapper.PatientMapper;
import com.frztech.patientService.model.Patient;
import com.frztech.patientService.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    //create a repository object via dependency injection
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients() {
        //retrieve lists of patients
        List<Patient> patients = patientRepository.findAll();

        //convert list of Patient(domain entity model) to patient DTO and return
        return patients.stream()
                .map(PatientMapper::toDTO)
                .toList();
    }
}
