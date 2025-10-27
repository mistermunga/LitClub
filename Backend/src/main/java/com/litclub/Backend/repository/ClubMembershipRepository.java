package com.litclub.Backend.repository;

import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipID> {
}
