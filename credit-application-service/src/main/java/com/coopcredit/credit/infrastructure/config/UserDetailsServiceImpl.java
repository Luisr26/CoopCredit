package com.coopcredit.credit.infrastructure.config;

import com.coopcredit.credit.application.port.out.UsuarioRepositoryPort;
import com.coopcredit.credit.domain.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetailsService para Spring Security.
 * Carga usuarios desde el repositorio.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepositoryPort usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Set<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.name()))
                .collect(Collectors.toSet());

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .build();
    }
}
