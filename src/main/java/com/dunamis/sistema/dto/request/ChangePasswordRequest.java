package com.dunamis.sistema.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "A password actual é obrigatória.")
    private String currentPassword;

    @NotBlank(message = "A nova password é obrigatória.")
    @Size(min = 6, max = 100, message = "A nova password deve ter entre 6 e 100 caracteres.")
    private String newPassword;

    @NotBlank(message = "A confirmação da password é obrigatória.")
    private String confirmNewPassword;

    @AssertTrue(message = "As passwords não coincidem.")
    public boolean isPasswordMatching() {
        if (newPassword == null || confirmNewPassword == null) {
            return true;
        }
        return newPassword.equals(confirmNewPassword);
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
