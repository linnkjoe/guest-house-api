package com.mabjaiainn.proj.DTO;

public record GoogleAuthResponse(
        boolean registrationRequired,
        LoginResponse session,
        String email,
        String firstName,
        String lastName,
        String completionCode
) {
    public static GoogleAuthResponse authenticated(LoginResponse session) {
        return new GoogleAuthResponse(false, session, null, null, null, null);
    }

    public static GoogleAuthResponse registrationRequired(
            String email, String firstName, String lastName, String completionCode) {
        return new GoogleAuthResponse(true, null, email, firstName, lastName, completionCode);
    }
}
