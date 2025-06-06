package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.CertificateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICertificateRepository extends JpaRepository<CertificateModel, Long> {
    // Thêm custom method nếu cần
}
