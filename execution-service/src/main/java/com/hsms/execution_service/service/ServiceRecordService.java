package com.hsms.execution_service.service;

import com.hsms.execution_service.model.ServiceRecordDetailResponseDTO;
import com.hsms.execution_service.model.ServiceRecordRequestDTO;
import com.hsms.execution_service.model.ServiceRecordResponseDTO;

public interface ServiceRecordService {
    ServiceRecordResponseDTO start(ServiceRecordRequestDTO dto);
    ServiceRecordDetailResponseDTO complete(Long id, ServiceRecordRequestDTO dto);
    ServiceRecordDetailResponseDTO get(Long id);
    ServiceRecordDetailResponseDTO updatePaymentStatus(Long id, String status, String method);
}
