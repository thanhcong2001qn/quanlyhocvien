package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.RoleModel;
import jakarta.validation.ValidationException;
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

    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        AccountModel accountModel = accountService.getAccountByUsername(username);
        return new org.springframework.security.core.userdetails.User(
                accountModel.getUsername(),
                accountModel.getPassword()
                , getAuthorities(accountModel.getRole())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(RoleModel role) {
        if (role == null) {
            // Trả về quyền mặc định hoặc danh sách trống
            return Collections.emptyList();
            // Hoặc: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        String roleName = role.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            return Collections.emptyList();
        }

        // Thêm tiền tố ROLE_ nếu chưa có
        String formattedRole = roleName.startsWith("ROLE_") ? roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();

        return Collections.singletonList(new SimpleGrantedAuthority(formattedRole));
    }
}
