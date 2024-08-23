package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a tenant
 */
@Schema(description = "Details of a tenant")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {

    @JsonProperty("id")
    @Size(min = 2, max = 128)
    private String id = null;

    @JsonProperty("code")
//    @NotNull
    @Size(min = 1, max = 20)
    private String code = null;

    @JsonProperty("tenantId")
    @Size(min = 1, max = 20)
    private String tenantId = null;

    // alphanumeric / alphabet
    @JsonProperty("name")
    @NotNull
    @Size(min = 1, max = 100)
    private String name = null;

    @JsonProperty("email")
    @NotNull
    @Email(message = "Email should be valid")
    private String email = null;

    @JsonProperty("additionalAttributes")
    private Object additionalAttributes = null;

    @JsonProperty("isActive")
    private Boolean isActive = Boolean.FALSE;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}