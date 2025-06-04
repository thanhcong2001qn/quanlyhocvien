package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.RoleModel;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Collections;



@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Đang tải thông tin user: {}", username);

        AccountModel accountModel = accountService.getAccountByUsername(username);

        if (accountModel == null) {
            logger.warn("Không tìm thấy user: {}", username);
            throw new UsernameNotFoundException("Không tìm thấy user: " + username);
        }

        Collection<? extends GrantedAuthority> authorities = getAuthorities(accountModel.getRole());
        logger.debug("User {} có role: {}", username, authorities);

        return new org.springframework.security.core.userdetails.User(
                accountModel.getUsername(),
                accountModel.getPassword(),
                authorities
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(RoleModel role) {
        if (role == null) {
            logger.debug("Role là null, trả về danh sách quyền trống");
            return Collections.emptyList();
        }

        String roleName = role.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            logger.debug("Tên role rỗng hoặc null");
            return Collections.emptyList();
        }

        // Thêm tiền tố ROLE_ nếu chưa có và chuyển thành chữ HOA
        String formattedRole = roleName.startsWith("ROLE_") ? roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();
        logger.debug("Đã format role thành: {}", formattedRole);

        return Collections.singletonList(new SimpleGrantedAuthority(formattedRole));
    }
}