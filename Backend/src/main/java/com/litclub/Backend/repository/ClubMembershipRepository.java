package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipID> {

    List<ClubMembership> findByClub(Club club);
    List<ClubMembership> findByMember(User user);
    Optional<ClubMembership> findByClubAndMember(Club club, User user);
    Optional<ClubMembership> findClubMembershipByClubMembershipID(ClubMembershipID clubMembershipID);
}
