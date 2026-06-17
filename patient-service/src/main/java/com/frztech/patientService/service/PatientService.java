package com.frztech.patientService.service;

import com.frztech.patientService.dto.PatientRequestDTO;
import com.frztech.patientService.dto.PatientResponseDTO;
import com.frztech.patientService.exception.EmailAlreadyExistsException;
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

    //get all patients
    public List<PatientResponseDTO> getPatients() {
        //retrieve lists of patients
        List<Patient> patients = patientRepository.findAll();

        //convert list of Patient(domain entity model) to patient DTO and return
        return patients.stream()
                .map(PatientMapper::toDTO)
                .toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A person with this email already exists: " + patientRequestDTO.getEmail());
        }

        //create an entry in the db
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        //return the newly created patient
        return PatientMapper.toDTO(newPatient);
    }
}
