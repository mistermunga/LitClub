package com.litclub.Backend.repository;

import com.litclub.Backend.entity.MeetingRegister;
import com.litclub.Backend.entity.compositeKey.RegisterID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRegisterRepository extends JpaRepository<MeetingRegister, RegisterID> {
}
