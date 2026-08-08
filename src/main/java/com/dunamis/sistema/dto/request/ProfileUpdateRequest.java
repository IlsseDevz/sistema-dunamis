package com.dunamis.sistema.dto.request;

import com.dunamis.sistema.entity.enums.ChurchSituation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
    private String fullName;

    @NotBlank(message = "O contacto é obrigatório.")
    @Size(max = 20, message = "O contacto não pode exceder 20 caracteres.")
    private String contacto;

    @Email(message = "Informe um email válido.")
    @Size(max = 150, message = "O email não pode exceder 150 caracteres.")
    private String email;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 100, message = "O bairro não pode exceder 100 caracteres.")
    private String bairro;

    @NotNull(message = "A situação na igreja é obrigatória.")
    private ChurchSituation churchSituation;

    @NotNull(message = "Indique se é batizado.")
    private Boolean baptized;

    @NotNull(message = "A função na igreja é obrigatória.")
    private Long churchFunctionId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public ChurchSituation getChurchSituation() {
        return churchSituation;
    }

    public void setChurchSituation(ChurchSituation churchSituation) {
        this.churchSituation = churchSituation;
    }

    public Boolean getBaptized() {
        return baptized;
    }

    public void setBaptized(Boolean baptized) {
        this.baptized = baptized;
    }

    public Long getChurchFunctionId() {
        return churchFunctionId;
    }

    public void setChurchFunctionId(Long churchFunctionId) {
        this.churchFunctionId = churchFunctionId;
    }
}
