package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import com.litclub.Backend.security.roles.ClubRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipID> {

    List<ClubMembership> findByClub(Club club);
    List<ClubMembership> findByMember(User user);
    List<ClubMembership> findClubMembershipsByRoles(Set<ClubRole> roles);
    List<ClubMembership> findClubMembershipsByClubAndRoles(Club club, Set<ClubRole> role);
    Optional<ClubMembership> findByClubAndMember(Club club, User user);
    Optional<ClubMembership> findClubMembershipByClubMembershipID(ClubMembershipID clubMembershipID);
}
