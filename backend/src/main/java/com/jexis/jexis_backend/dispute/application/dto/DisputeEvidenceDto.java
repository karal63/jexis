package com.jexis.jexis_backend.dispute.application.dto;

import com.jexis.jexis_backend.dispute.application.dto.canceled.CanceledEvidenceDto;
import com.jexis.jexis_backend.dispute.application.dto.duplicate.DuplicateEvidenceDto;
import com.jexis.jexis_backend.dispute.application.dto.fraudulent.FraudulentEvidenceDto;
import com.jexis.jexis_backend.dispute.application.dto.noValidAuthorization.NoValidAuthorizationDto;
import com.jexis.jexis_backend.dispute.application.dto.notAsDescribed.MerchandiseNotAsDescribedDto;
import com.jexis.jexis_backend.dispute.application.dto.notAsDescribed.ServiceNotAsDescribedDto;
import com.jexis.jexis_backend.dispute.application.dto.notReceived.NotReceivedDto;
import com.jexis.jexis_backend.dispute.application.dto.other.OtherEvidenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DisputeEvidenceDto(
        @Valid
        CanceledEvidenceDto canceled,

        @Valid
        DuplicateEvidenceDto duplicate,

        @Valid
        FraudulentEvidenceDto fraudulent,

        @Valid
        MerchandiseNotAsDescribedDto merchandiseNotAsDescribed,

        @Valid
        NoValidAuthorizationDto noValidAuthorization,

        @Valid
        NotReceivedDto notReceived,

        @Valid
        OtherEvidenceDto other,

        @NotNull
        DisputeReason reason,

        @Valid
        ServiceNotAsDescribedDto serviceNotAsDescribed

) {

}
