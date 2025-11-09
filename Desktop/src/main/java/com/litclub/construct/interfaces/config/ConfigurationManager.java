package com.litclub.construct.interfaces.config;

import java.util.Map;

public class ConfigurationManager {

    private static class InstanceConfiguration {
        public InstanceData instance;
        public Map<String, ClubFlagsData> clubs;
    }

    private static class InstanceData {
        public InstanceRegistrationMode registrationMode;
        public ClubCreationMode clubCreationMode;
        public int maxClubsPerUser;
        public int maxMembersPerClub;
    }

    private static class ClubFlagsData {
        public boolean allowPublicNotes;
        public boolean requireMeetingRSVP;
        public boolean allowMemberInvites;
        public boolean allowMemberDiscussion;
        public boolean enableRegister;
    }

    public enum InstanceRegistrationMode {
        OPEN, INVITE_ONLY, CLOSED
    }

    public enum ClubCreationMode {
        FREE, APPROVAL_REQUIRED, ADMIN_ONLY
    }

}
