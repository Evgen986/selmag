package ru.maliutin.manager.securitu;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maliutin.manager.entity.Authority;
import ru.maliutin.manager.repository.SelmagUserRepository;

@Service
@RequiredArgsConstructor
public class SelmagUserDetailService implements UserDetailsService {

    private final SelmagUserRepository selmagUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.selmagUserRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getAuthorities().stream()
                                .map(Authority::toString)
                                .map(SimpleGrantedAuthority::new)
                                .toList())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User %s not found"
                                .formatted(username)));
    }
}
