package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.RadiologyResponse;
import com.simats.neoomfs.entity.Radiology;
import org.springframework.stereotype.Component;

@Component
public class RadiologyMapper {

    public RadiologyResponse toResponse(Radiology radiology) {
        if (radiology == null) return null;
        return RadiologyResponse.builder()
                .id(radiology.getId())
                .patientId(radiology.getPatient().getId())
                .iopaTaken(radiology.isIopaTaken())
                .iopaFileUrl(radiology.getIopaFileUrl())
                .iopaFindings(radiology.getIopaFindings())
                .opgTaken(radiology.isOpgTaken())
                .opgFileUrl(radiology.getOpgFileUrl())
                .opgFindings(radiology.getOpgFindings())
                .cbctTaken(radiology.isCbctTaken())
                .cbctFileUrl(radiology.getCbctFileUrl())
                .cbctFindings(radiology.getCbctFindings())
                .boneDensityHu(radiology.getBoneDensityHu())
                .generalRadiologyNotes(radiology.getGeneralRadiologyNotes())
                .build();
    }
}
