package com.litclub.Backend.service.low;

import com.litclub.Backend.construct.meeting.RegisterDTO;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.MeetingRegister;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.repository.MeetingRegisterRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingRegisterService {

    private final MeetingRegisterRepository meetingRegisterRepository;

    public MeetingRegisterService(MeetingRegisterRepository meetingRegisterRepository) {
        this.meetingRegisterRepository = meetingRegisterRepository;
    }

    // ====== CREATE ======
    @Transactional
    public MeetingRegister addEntry(RegisterDTO registerDTO){
        if (meetingRegisterRepository.existsByMeetingAndUser(registerDTO.meeting(),  registerDTO.user())){
            throw new EntityExistsException("Register entry already exists");
        }
        MeetingRegister meetingRegister = convertDtoToMeetingRegister(registerDTO);
        return meetingRegisterRepository.save(meetingRegister);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(){
        return meetingRegisterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MeetingRegister findMeetingRegister(Meeting meeting, User user) {
        return meetingRegisterRepository.findByMeetingAndUser(meeting, user)
                .orElseThrow(() -> new EntityNotFoundException("Register not found"));
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(Meeting meeting) {
        return meetingRegisterRepository.findAllByMeeting(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(User user) {
        return meetingRegisterRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(String identifier, boolean variable) {
        switch (identifier){
            case "attended" -> { return meetingRegisterRepository.findAllByAttended(variable); }
            case "late" -> { return meetingRegisterRepository.findAllByLate(variable); }
            case "excused" -> { return meetingRegisterRepository.findAllByExcused(variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(User user, String identifier, boolean variable) {
        switch (identifier){
            case "attended" -> { return meetingRegisterRepository.findAllByUserAndAttended(user, variable); }
            case "late" -> { return meetingRegisterRepository.findAllByUserAndLate(user, variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAllByAttendanceBivariable(boolean attendance, String identifier, boolean variable) {
        switch (identifier){
            case "late" -> { return meetingRegisterRepository.findAllByAttendedAndLate(attendance, variable); }
            case "excused" -> { return meetingRegisterRepository.findAllByAttendedAndExcused(attendance, variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAllByAttendanceBivariable(User user, boolean attendance, String identifier, boolean variable) {
        switch (identifier){
            case "late" -> { return meetingRegisterRepository.findAllByUserAndAttendedAndLate(user, attendance, variable); }
            case "excused" -> { return meetingRegisterRepository.findAllByUserAndAttendedAndExcused(user, attendance, variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAll(Meeting meeting, String identifier, boolean variable) {
        switch (identifier){
            case "attended" -> { return meetingRegisterRepository.findAllByMeetingAndAttended(meeting, variable); }
            case "late" -> { return meetingRegisterRepository.findAllByMeetingAndLate(meeting, variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingRegister> findAllByAttendanceBivariable(Meeting meeting, boolean attendance, String identifier, boolean variable) {
        switch (identifier){
            case "late" -> { return meetingRegisterRepository.findAllByMeetingAndAttendedAndLate(meeting, attendance, variable); }
            case "excused" -> { return meetingRegisterRepository.findAllByMeetingAndAttendedAndExcused(meeting, attendance, variable); }
            default -> {throw new MalformedDTOException("identifier field incorrect");}
        }
    }

    // ------ Utility ------
    public MeetingRegister convertDtoToMeetingRegister(RegisterDTO registerDTO){
        MeetingRegister meetingRegister = new MeetingRegister();
        meetingRegister.setMeeting(registerDTO.meeting());
        meetingRegister.setUser(registerDTO.user());
        meetingRegister.setAttended(registerDTO.attended());
        meetingRegister.setLate(registerDTO.late());
        meetingRegister.setExcused(registerDTO.excused());

        return meetingRegister;
    }
}
