package com.litclub.Backend.service.top.facilitator.util;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.security.roles.ClubRole;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ClubInviteGenerator {

    private static final long INVITE_MAX_AGE_MS = TimeUnit.DAYS.toMillis(7);

    private final ClubService clubService;
    private final UserService userService;
    private final ClubMembershipService clubMembershipService;

    // Thread-safe set storing already-redeemed tokens
    private final Set<String> redeemedInvites = ConcurrentHashMap.newKeySet();

    // Server-side secret for signing tokens
    @Value("${invite.secret}")
    private String secretKey;

    public ClubInviteGenerator(
            ClubService clubService,
            UserService userService,
            ClubMembershipService clubMembershipService
    ) {
        this.clubService = clubService;
        this.userService = userService;
        this.clubMembershipService = clubMembershipService;
    }


    /**
     * Generates a unique, signed, single-use invite token.
     * Format inside Base64:
     *     inviterID : clubID : timestamp | signature
     */
    public String generateInvite(User inviter, Club club) {
        long timestamp = System.currentTimeMillis();
        String payload = inviter.getUserID() + ":" + club.getClubID() + ":" + timestamp;

        String signature = hmacSha256(payload, secretKey);
        String tokenRaw = payload + "." + signature;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenRaw.getBytes(StandardCharsets.UTF_8));
    }


    public DecodedInvite decodeInvite(String token) {
        // Check if already redeemed
        boolean firstUse = redeemedInvites.add(token);
        if (!firstUse) {
            throw new MalformedDTOException("Invite already used");
        }

        // Base64 decode
        String decoded;
        try {
            decoded = new String(
                    Base64.getUrlDecoder().decode(token),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            throw new MalformedDTOException("Malformed invite token");
        }

        // Expect "payload.signature"
        String[] tokenParts = decoded.split("\\.");
        if (tokenParts.length != 2) {
            throw new MalformedDTOException("Malformed invite token");
        }

        String payload = tokenParts[0];
        String providedSig = tokenParts[1];

        // Verify signature
        String expectedSig = hmacSha256(payload, secretKey);
        if (!MessageDigest.isEqual(
                providedSig.getBytes(StandardCharsets.UTF_8),
                expectedSig.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new MalformedDTOException("Invalid invite signature");
        }

        // Parse fields from payload
        String[] payloadParts = payload.split(":");
        if (payloadParts.length != 3) {
            throw new MalformedDTOException("Malformed invite token");
        }

        long inviterID, clubID, timestamp;
        try {
            inviterID = Long.parseLong(payloadParts[0]);
            clubID   = Long.parseLong(payloadParts[1]);
            timestamp = Long.parseLong(payloadParts[2]);
        } catch (NumberFormatException e) {
            throw new MalformedDTOException("Malformed numeric fields");
        }

        // Verify expiration
        long age = System.currentTimeMillis() - timestamp;
        if (age > INVITE_MAX_AGE_MS) {
            throw new MalformedDTOException("Invite expired");
        }

        // Validate inviter permissions at redemption time
        Club club = clubService.requireClubById(clubID);
        User inviter = userService.requireUserById(inviterID);

        var roles = clubMembershipService.getRolesForUserInClub(inviter, club);
        if (!roles.contains(ClubRole.MODERATOR) && !roles.contains(ClubRole.OWNER)) {
            throw new AccessDeniedException("Inviter unauthorized to generate invites");
        }

        // Valid token
        return new DecodedInvite(inviterID, clubID, timestamp);
    }



    // --- HMAC helper ---------------------------------------------------------

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }


    public record DecodedInvite(Long inviterID, Long clubID, long timestamp) {}
}

