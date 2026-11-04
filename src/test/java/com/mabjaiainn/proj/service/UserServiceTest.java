package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.ChangePasswordRequest;
import com.mabjaiainn.proj.DTO.CreateUserRequest;
import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changingPasswordUpdatesTheAccountAndLinkedClient() throws BusinessException {
        Client client = new Client();
        AppUser user = new AppUser("client@example.com", "old-hash", "Client", Role.CLIENTE, client);
        user.setMustChangePassword(true);
        when(appUserRepository.findByUsername("client@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword1!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new-hash");

        userService.changePassword("client@example.com",
                new ChangePasswordRequest("OldPassword1!", "NewPassword1!"));

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals("new-hash", client.getClientPassword());
        assertFalse(user.isMustChangePassword());
        verify(appUserRepository).save(user);
    }

    @Test
    void changingPasswordRequiresTheCurrentPassword() {
        AppUser user = new AppUser("admin@mbj.inn", "old-hash", "Admin", Role.ADMIN, null);
        when(appUserRepository.findByUsername("admin@mbj.inn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.changePassword("admin@mbj.inn",
                new ChangePasswordRequest("wrong", "NewPassword1!")));
    }

    @Test
    void adminCanDisableAndReenableManagerAccount() throws BusinessException {
        AppUser manager = new AppUser("manager@mbj.inn", "hash", "Manager", Role.GESTOR, null);
        when(appUserRepository.findById(12L)).thenReturn(Optional.of(manager));

        var disabled = userService.setManagerEnabled(12L, false);
        assertFalse(disabled.enabled());
        assertFalse(manager.isEnabled());

        var enabled = userService.setManagerEnabled(12L, true);
        assertEquals(true, enabled.enabled());
        verify(appUserRepository, org.mockito.Mockito.times(2)).findById(12L);
    }

    @Test
    void managerDeactivationCannotBeUsedAgainstAdminAccounts() {
        AppUser admin = new AppUser("admin@mbj.inn", "hash", "Admin", Role.ADMIN, null);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BusinessException.class, () -> userService.setManagerEnabled(1L, false));
    }

    @Test
    void managerGetsGeneratedTemporaryPasswordAndMustChangeIt() throws BusinessException {
        when(appUserRepository.existsByUsername("manager@mbj.inn")).thenReturn(false);
        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("encoded");
        when(appUserRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var created = userService.createStaffUser(new CreateUserRequest(
                "Manager@MBJ.INN", null, null, "Manager", Role.GESTOR));

        assertEquals("manager@mbj.inn", created.username());
        assertEquals(Role.GESTOR, created.role());
        assertTrue(created.temporaryPassword().length() >= 8);
        var savedUser = org.mockito.ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(savedUser.capture());
        assertTrue(savedUser.getValue().isMustChangePassword());
    }

    @Test
    void administratorPasswordMustBeConfirmed() {
        assertThrows(BusinessException.class, () -> userService.createStaffUser(
                new CreateUserRequest("admin2@mbj.inn", "Password123", "Different123", "Admin 2", Role.ADMIN)));
    }

    @Test
    void staffUsernameMustUseTheTeamDomain() {
        assertThrows(BusinessException.class, () -> userService.createStaffUser(
                new CreateUserRequest("manager", null, null, "Manager", Role.GESTOR)));
    }
}
