package tg.edtch.activEducation.shared.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OriaRequest {
    @NotBlank
    @Size(max = 2000)
    private String message;

    private String sessionId;

    private String contexteOrientation;

    public @NotBlank @Size(max = 2000) String getMessage() { return message; }
    public void setMessage(@NotBlank @Size(max = 2000) String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getContexteOrientation() { return contexteOrientation; }
    public void setContexteOrientation(String contexteOrientation) { this.contexteOrientation = contexteOrientation; }
}
